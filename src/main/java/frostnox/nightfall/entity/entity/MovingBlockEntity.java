package frostnox.nightfall.entity.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.IDropsItems;
import frostnox.nightfall.block.IFallable;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Similar to FallingBlockEntity, far less prone to breaking during a fall. Supports sliding.
 */
public class MovingBlockEntity extends Entity {
    public static final int SLIDE_DURATION = 4, SOUND_DURATION = 18;
    public static final double SLIDE_SPEED = 1D / SLIDE_DURATION;
    private static final Logger LOGGER = Nightfall.LOGGER;
    private BlockState blockState = BlocksNF.DIRT.get().defaultBlockState();
    private SoundEvent sound = null;
    public int time;
    protected int slideTime, soundTime;
    public boolean dropItem = true;
    private boolean hurtEntities;
    private int fallDamageMax = 40;
    private float fallDamagePerDistance;
    private boolean queueDiscard = false, triedPlacement = false;
    @Nullable
    public CompoundTag blockData;
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(MovingBlockEntity.class, EntityDataSerializers.BLOCK_POS);
    public Direction slideDir = Direction.DOWN;
    public boolean makeSlideParticles = true, canRender = true;

    public MovingBlockEntity(EntityType<? extends MovingBlockEntity> type, Level level) {
        super(type, level);
    }

    protected MovingBlockEntity(Level level, BlockPos pos, BlockState state) {
        this(EntitiesNF.MOVING_BLOCK.get(), level);
        double x = pos.getX() + 0.5D, y = pos.getY() + 0.005D, z = pos.getZ() + 0.5D;
        setBlockState(state);
        blocksBuilding = true;
        setPos(x, y, z);
        setDeltaMovement(Vec3.ZERO);
        xo = x;
        yo = y;
        zo = z;
        setStartPos(blockPosition());
        float strength = state.getBlock().defaultDestroyTime();
        setHurtsEntities(strength < 0F ? 15F : Math.min(strength * 2.5F, 17.5F), 250);
        if(state.getBlock() instanceof IFallable fallable) fallable.onMovingBlockEntityCreated(state, level, pos, this);
    }

    public static MovingBlockEntity fall(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        if(state.hasProperty(BlockStatePropertiesNF.WATER_LEVEL)) state = state.setValue(BlockStatePropertiesNF.WATER_LEVEL, 0);
        MovingBlockEntity entity = new MovingBlockEntity(level, pos, state);
        level.addFreshEntity(entity);
        entity.playSound();
        return entity;
    }

    public static MovingBlockEntity slide(Level level, BlockPos pos, Direction direction, BlockState state) {
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        if(state.hasProperty(BlockStatePropertiesNF.WATER_LEVEL)) state = state.setValue(BlockStatePropertiesNF.WATER_LEVEL, 0);
        MovingBlockEntity entity = new MovingBlockEntity(level, pos, state);
        entity.slideDir = direction;
        level.addFreshEntity(entity);
        return entity;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public void setBlockState(BlockState state) {
        blockState = state;
        if(state.getBlock() instanceof IFallable fallable) sound = fallable.getFallSound(state);
        else sound = null;
    }

    public void setStartPos(BlockPos pOrigin) {
        entityData.set(DATA_START_POS, pOrigin);
    }

    public BlockPos getStartPos() {
        return entityData.get(DATA_START_POS);
    }

    public int getSlideTime() {
        return slideTime;
    }

    public void setSlideTime(int slideTime) {
        this.slideTime = slideTime;
    }

    public boolean isSliding() {
        return slideTime > 0 && slideTime <= SLIDE_DURATION;
    }

    public void recenterPos() {
        if(getX() % 1D != 0.5 || getZ() % 1D != 0.5) setPos(Mth.floor(getX()) + 0.5, getY(),Mth.floor(getZ()) + 0.5);
    }

    public void queueDiscard() {
        //Let block render for one more frame unimpeded on the client to partially fix bug:
        //MC-237040: Falling blocks still sometimes disappear for a moment when landing
        queueDiscard = true;
        if(!level.isClientSide()) NetworkHandler.toAllTracking(this, new GenericEntityToClient(NetworkHandler.Type.MOVING_BLOCK_STOP_PHYSICS_CLIENT, getId()));
    }

    public void playSound() {
        if(!level.isClientSide) {
            soundTime = sound != null ? 1 : 0;
            level.playSound(null, this, sound, SoundSource.BLOCKS, 1.5F, 0.94F + level.random.nextFloat() * 0.12F);
        }
    }

    @Override
    public boolean shouldRender(double x, double pY, double z) {
        if(blockState.isAir()) return false;
        else return super.shouldRender(x, pY, z);
    }

    @Override
    protected float getEyeHeight(Pose pPose, EntityDimensions pSize) {
        return pSize.height * 0.5F;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    protected AABB makeBoundingBox() {
        if(queueDiscard) return AABB.ofSize(position(), 0, 0, 0);
        if(blockState == null) return super.makeBoundingBox();
        VoxelShape shape = blockState.getCollisionShape(level, blockPosition());
        if(shape.isEmpty()) return AABB.ofSize(position(), 0, 0, 0);
        else if(shape == Shapes.block()) return super.makeBoundingBox();
        else {
            AABB box = shape.bounds();
            double xSize = box.getXsize() * 0.99 / 2, zSize = box.getZsize() * 0.99 / 2;
            return new AABB(getX() - xSize, getY(), getZ() - zSize, getX() + xSize, getY() + box.getYsize() * 0.99, getZ() + zSize);
        }
    }

    @Override
    public BlockPos getOnPos() {
        //Skip check for vertically extended collisions
        return new BlockPos(Mth.floor(position().x), Mth.floor(position().y - 0.2), Mth.floor(position().z));
    }

    @Override
    public void tick() {
        triedPlacement = false;
        if(queueDiscard && !level.isClientSide()) {
            blockState = Blocks.AIR.defaultBlockState();
            NetworkHandler.toAllTracking(this, new GenericEntityToClient(NetworkHandler.Type.MOVING_BLOCK_SET_AIR_CLIENT, getId()));
            queueDiscard = false;
        }
        if(blockState.isAir()) {
            if(!level.isClientSide) {
                if(soundTime == 0) discard();
                soundTime++;
                if(soundTime > SOUND_DURATION) discard();
            }
            makeBoundingBox();
        }
        else {
            Block block = blockState.getBlock();
            ++time;
            boolean canSlide = slideDir != Direction.DOWN;
            if(canSlide) slideTime++;
            if(soundTime > 0) {
                soundTime++;
                if(soundTime > SOUND_DURATION) soundTime = 0;
            }
            boolean isSliding = isSliding() && canSlide;
            BlockPos blockPos = blockPosition();
            if(isSliding) {
                if(slideTime == 1) {
                    noPhysics = true;
                    setDeltaMovement(getDeltaMovement().add(slideDir.getStepX() * SLIDE_SPEED, slideDir.getStepY() * SLIDE_SPEED, slideDir.getStepZ() * SLIDE_SPEED));
                    playSound();
                }
                //Slide particles
                if(level.isClientSide && makeSlideParticles) {
                    double x = getX() - 0.5 + level.random.nextDouble();
                    double y = getY() + 0.05D;
                    double z = getZ() - 0.5 + level.random.nextDouble();
                    BlockState insideState = level.getBlockState(blockPos);
                    if(!insideState.isAir()) {
                        VoxelShape shape = insideState.getCollisionShape(level, blockPos).move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                        if(!shape.isEmpty() && shape.bounds().contains(x, getY(), z)) {
                            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), x, y, z,
                                    (level.random.nextDouble() - 0.5) * 2D,
                                    1D + level.random.nextDouble() * 0.5,
                                    (level.random.nextDouble() - 0.5) * 2D);
                        }
                    }
                }
            }
            else if(slideTime == SLIDE_DURATION + 1 && canSlide) {
                noPhysics = false;
                setDeltaMovement(getDeltaMovement().subtract(slideDir.getStepX() * SLIDE_SPEED, slideDir.getStepY() * SLIDE_SPEED + 0.04, slideDir.getStepZ() * SLIDE_SPEED));
            }
            if(!isNoGravity() && slideDir != Direction.UP) setDeltaMovement(getDeltaMovement().add(0.0D, -0.04D, 0.0D));
            move(MoverType.SELF, getDeltaMovement());

            if(!onGround) {
                if(!level.isClientSide && (time > 100 && (blockPos.getY() <= level.getMinBuildHeight() || blockPos.getY() > level.getMaxBuildHeight()) || time > 6000)) {
                    if(dropItem && level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                        spawnAtLocation(block);
                    }
                    queueDiscard();
                }
            }
            else if(!isSliding) {
                recenterPos();
                setDeltaMovement(getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
            }

            if(!blockState.isAir()) {
                if(isSliding) setDeltaMovement(getDeltaMovement().multiply(1D, 0.98D, 1D));
                else {
                    setDeltaMovement(getDeltaMovement().scale(0.98D));
                    //Compensate for falling inside of landing block
                    if(tickCount > 0 && !noPhysics) {
                        Vec3 entityPos = getPosition(1F);
                        if(onGround || !LevelUtil.canFallThrough(level.getBlockState(new BlockPos(entityPos)))) {
                            tryPlacement();
                            setPos(entityPos.x, Math.ceil(entityPos.y), entityPos.z);
                        }
                    }
                }
            }
        }
    }

    public void tryPlacement() {
        if(triedPlacement || queueDiscard) return;
        triedPlacement = true;
        Block block = blockState.getBlock();
        BlockPos blockPos = blockPosition();
        BlockState inState = level.getBlockState(blockPos);
        if(!inState.is(Blocks.MOVING_PISTON)) {
            boolean canReplace = inState.is(TagsNF.FALLING_DESTROYABLE) || inState.canBeReplaced(new DirectionalPlaceContext(level, blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
            boolean yChanged = false;
            while(!canReplace && blockPos.getY() < level.getMaxBuildHeight()) {
                yChanged = true;
                blockPos = blockPos.above();
                inState = level.getBlockState(blockPos);
                canReplace = inState.is(TagsNF.FALLING_DESTROYABLE) || inState.canBeReplaced(new DirectionalPlaceContext(level, blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
            }
            if(yChanged) setPos(getX(), blockPos.getY(), getZ());
            if(!level.isClientSide) {
                if(canReplace && blockState.canSurvive(level, blockPos)) {
                    if((!(block instanceof IFallable movable) || movable.canLand(level, blockPos, blockState, inState, this))) {
                        if(inState.is(TagsNF.FALLING_DESTROYABLE)) level.destroyBlock(blockPos, true);
                        blockState = Block.updateFromNeighbourShapes(blockState, level, blockPos);
                        if(blockState.getBlock() instanceof IWaterloggedBlock waterloggable) blockState = waterloggable.addLiquidToPlacement(blockState, blockPos, level);
                        if(level.setBlock(blockPos, blockState, 3)) {
                            ((ServerLevel) level).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(blockPos, level.getBlockState(blockPos)));
                            queueDiscard();
                            if(blockData != null && blockState.hasBlockEntity()) {
                                BlockEntity entity = level.getBlockEntity(blockPos);
                                loadBlockEntityData(entity);
                            }
                            if(block instanceof IFallable movable) {
                                movable.onLand(level, blockPos, blockState, inState, this);
                            }
                            for(MovingBlockEntity other : level.getEntitiesOfClass(this.getClass(), getBoundingBox())) {
                                if(other != this && other.tickCount > 0) other.tryPlacement();
                            }
                            if(blockState.is(TagsNF.SHATTER_ON_FALL)) level.destroyBlock(blockPos, true);
                            else level.playSound(null, blockPos, blockState.getSoundType().getStepSound(), SoundSource.BLOCKS, 0.55F, level.random.nextFloat() * 0.1F + 0.9F);
                        }
                        else if(dropItem && level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            queueDiscard();
                            callOnBrokenAfterFall(block, blockPos);
                        }
                    }
                }
                else {
                    queueDiscard();
                    if(dropItem && level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                        callOnBrokenAfterFall(block, blockPos);
                    }
                }
            }
        }
    }

    protected void loadBlockEntityData(BlockEntity entity) {
        if(entity != null) {
            CompoundTag tag = entity.saveWithoutMetadata();
            for(String s : blockData.getAllKeys()) {
                tag.put(s, blockData.get(s).copy());
            }
            try {
                entity.load(tag);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to load block entity from moving block", exception);
            }
            entity.setChanged();
        }
    }

    public void callOnBrokenAfterFall(Block block, BlockPos pos) {
        if(block instanceof IFallable movable) movable.onBrokenAfterFall(level, pos, this);
        BlockEntity blockEntity;
        if(blockData != null && block instanceof EntityBlock entityBlock) {
            blockEntity = entityBlock.newBlockEntity(pos, blockState);
            loadBlockEntityData(blockEntity);
        }
        else blockEntity = null;
        Block.dropResources(blockState, level, pos, blockEntity);
        if(blockEntity instanceof IDropsItems droppable) {
            Containers.dropContents(level, pos, droppable.getContainerDrops());
        }
    }

    public void setHurtsEntities(float fallDamagePerDistance, int fallDamageMax) {
        hurtEntities = true;
        this.fallDamagePerDistance = fallDamagePerDistance;
        this.fallDamageMax = fallDamageMax;
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState state, BlockPos pos) {
        super.checkFallDamage(pY, pOnGround, state, pos);
        if(hurtEntities && !level.isClientSide()) {
            int fallDist = Math.max(1, Mth.ceil(fallDistance));
            DamageTypeSource damageSource;
            if(blockState.getBlock() instanceof IFallable movable) damageSource = DamageTypeSource.createFallingSource(movable.getFallDamageType());
            else damageSource = DamageTypeSource.createFallingSource(DamageType.STRIKING);

            int damage = Math.min(Mth.floor(fallDist * fallDamagePerDistance), fallDamageMax);
            List<Entity> entities = level.getEntities(this, getBoundingBox(), LevelUtil.ALIVE_ACTION_TRACKER_ENTITY);
            for(Entity entity : entities) {
                IActionTracker capA = ActionTracker.get(entity);
                if(capA.getMovingBlockInvulnerableTime() > 0) continue;
                capA.setMovingBlockInvulnerableTime(10);
                damageSource.setSound(() -> damage >= 20 ? SoundEvents.PLAYER_BIG_FALL : SoundEvents.PLAYER_SMALL_FALL);
                entity.hurt(damageSource, damage);
            }
        }
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return pFallDistance >= 3F * pMultiplier;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.put("BlockState", NbtUtils.writeBlockState(blockState));
        pCompound.putInt("Time", time);
        pCompound.putBoolean("DropItem", dropItem);
        pCompound.putBoolean("HurtEntities", hurtEntities);
        pCompound.putBoolean("QueueDiscard", queueDiscard);
        pCompound.putFloat("FallHurtAmount", fallDamagePerDistance);
        pCompound.putInt("FallHurtMax", fallDamageMax);
        if(slideDir != Direction.DOWN) pCompound.putInt("Direction", slideDir.ordinal());
        pCompound.putInt("SlideTime", getSlideTime());
        if(blockData != null) pCompound.put("TileEntityData", blockData);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        setBlockState(NbtUtils.readBlockState(pCompound.getCompound("BlockState")));
        time = pCompound.getInt("Time");
        if (pCompound.contains("HurtEntities", 99)) {
            hurtEntities = pCompound.getBoolean("HurtEntities");
            fallDamagePerDistance = pCompound.getFloat("FallHurtAmount");
            fallDamageMax = pCompound.getInt("FallHurtMax");
        } else if (blockState.is(BlockTags.ANVIL)) {
            hurtEntities = true;
        }
        queueDiscard = pCompound.getBoolean("QueueDiscard");
        if (pCompound.contains("DropItem", 99)) {
            dropItem = pCompound.getBoolean("DropItem");
        }
        if (pCompound.contains("TileEntityData", 10)) {
            blockData = pCompound.getCompound("TileEntityData");
        }
        if (blockState.isAir()) {
            setBlockState(BlocksNF.DIRT.get().defaultBlockState());
        }

        if(pCompound.contains("Direction", Tag.TAG_INT)) slideDir = Direction.values()[pCompound.getInt("Direction")];
        setSlideTime(pCompound.getInt("SlideTime"));
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory pCategory) {
        super.fillCrashReportCategory(pCategory);
        pCategory.setDetail("Imitating BlockState", blockState.toString());
    }

    public BlockState getBlockState() {
        return blockState;
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, Block.getId(getBlockState()));
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        setBlockState(Block.stateById(pPacket.getData()));
        blocksBuilding = true;
        double d0 = pPacket.getX();
        double d1 = pPacket.getY();
        double d2 = pPacket.getZ();
        setPos(d0, d1, d2);
        setStartPos(blockPosition());
    }
}
