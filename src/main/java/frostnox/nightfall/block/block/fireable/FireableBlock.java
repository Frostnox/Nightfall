package frostnox.nightfall.block.block.fireable;

import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.block.IAdjustableNodeType;
import frostnox.nightfall.block.IIgnitable;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.fuel.BurningFuelBlockEntity;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public abstract class FireableBlock extends BaseEntityBlock implements IAdjustableNodeType {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final VoxelShape COLLISION_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.95D, 16.0D);
    public final int cookTicks;
    public final TieredHeat cookHeat;

    public FireableBlock(int cookTicks, TieredHeat cookHeat, Properties properties) {
        super(properties);
        this.cookTicks = cookTicks;
        this.cookHeat = cookHeat;
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    public abstract BlockState getFiredBlock(Level level, BlockPos pos, BlockState state);

    public abstract boolean isStructureValid(Level level, BlockPos pos, BlockState state);

    public static void serverEntityTick(Level level, BlockPos pos, BlockState state, IFireableBlockEntity entity) {
        FireableBlock fireable = (FireableBlock) state.getBlock();
        boolean updated = false;
        if(!state.getValue(LIT)) {
            if(level.getGameTime() % 67L == 0L) {
                entity.setInStructure(fireable.isStructureValid(level, pos, state));
                if(entity.inStructure() && level.getBlockEntity(pos.below()) instanceof BurningFuelBlockEntity fuel && fuel.temperature >= fireable.cookHeat.getBaseTemp()) {
                    updated = true;
                    level.setBlockAndUpdate(pos, state.setValue(LIT, true));
                }
            }
        }
        if(updated || entity.getCookTicks() > 0) {
            if(!updated && level.getGameTime() % 67L == 0L) {
                entity.setInStructure(fireable.isStructureValid(level, pos, state));
                if(state.getValue(LIT)) level.setBlockAndUpdate(pos, state.setValue(LIT, false));
            }
            if(updated || (entity.inStructure() && level.getBlockEntity(pos.below()) instanceof BurningFuelBlockEntity fuel && fuel.temperature >= fireable.cookHeat.getBaseTemp())) {
                entity.setCookTicks(entity.getCookTicks() + 1);
                ((BlockEntity) entity).setChanged();
            }
            else if(entity.getCookTicks() > 0) {
                entity.setCookTicks(entity.getCookTicks() - 1);
                ((BlockEntity) entity).setChanged();
                if(entity.getCookTicks() == 0) level.setBlockAndUpdate(pos, state.setValue(LIT, false));
            }
            if(entity.getCookTicks() >= fireable.cookTicks) level.setBlockAndUpdate(pos, fireable.getFiredBlock(level, pos, state));
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        pTooltip.add(new TranslatableComponent("block.fireable").append(
                new TranslatableComponent("heat.tier." + cookHeat.getTier()).withStyle(Style.EMPTY.withColor(cookHeat.color.getRGB()))));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(LIT);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter pReader, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity pEntity) {
        if(!pEntity.fireImmune() && pEntity.invulnerableTime <= 10 && isIgnited(state) && pEntity instanceof LivingEntity) {
            pEntity.hurt(DamageTypeSource.HOT_FLOOR, 1F);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if(state.getValue(LIT)) level.scheduleTick(pos, this, 4 + level.getRandom().nextInt(9), TickPriority.HIGH);
        return super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        if(state.getValue(LIT)) level.scheduleTick(pos, this, 4 + level.getRandom().nextInt(9), TickPriority.HIGH);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(state.getValue(LIT)) {
            BlockPos spreadPos = pos.above();
            BlockState spreadState = level.getBlockState(spreadPos);
            if(spreadState.getBlock() instanceof IIgnitable ignitable) {
                ignitable.tryToIgnite(level, spreadPos, spreadState, ItemStack.EMPTY, TieredHeat.RED);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        if(state.getValue(LIT)) {
            AABB box = state.getShape(level, pos).bounds();
            double xSize = box.maxX - box.minX, zSize = box.maxZ - box.minZ;
            double size = box.getSize();
            double xSizeHalf = xSize / 2, zSizeHalf = zSize / 2;
            double x = pos.getX() + 0.5;
            double y = pos.getY() + box.minY;
            double z = pos.getZ() + 0.5;
            for(Direction dir : Direction.Plane.HORIZONTAL) {
                if(rand.nextDouble() < 0.85D * size) {
                    double xPos = x + (dir.getStepX() == 0 ? (-xSizeHalf + rand.nextDouble() * xSize) : (xSizeHalf * dir.getStepX()));
                    double yPos = y + (rand.nextDouble() * box.maxY);
                    double zPos = z + (dir.getStepZ() == 0 ? (-zSizeHalf + rand.nextDouble() * zSize) : (zSizeHalf * dir.getStepZ()));
                    VoxelShape shape = level.getBlockState(pos.relative(dir)).getCollisionShape(level, pos.relative(dir));
                    if(shape.isEmpty() || !shape.bounds().move(pos.relative(dir)).contains(xPos, yPos, zPos)) {
                        level.addParticle(cookHeat.getFlameParticle().get(), xPos, yPos, zPos, 0, 0, 0);
                        level.addParticle(ParticleTypes.SMOKE, xPos, yPos, zPos, 0, 0, 0);
                    }
                }
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborChanged(state, level, pos, pBlock, pFromPos, pIsMoving);
        if(pFromPos.getY() >= pos.getY() && isIgnited(state) && level.getFluidState(pFromPos).is(FluidTags.WATER)) {
            tryToExtinguish(state, pos, level, ItemStack.EMPTY);
        }
        else if(level.getBlockEntity(pos) instanceof FireableBlockEntity fireable) {
            fireable.inStructure = isStructureValid(level, pos, state);
        }
    }

    public boolean tryToExtinguish(BlockState state, BlockPos pos, Level level, ItemStack stack) {
        if(level.isClientSide() || !state.getValue(LIT)) return false;
        if(level.getBlockEntity(pos) instanceof IFireableBlockEntity fireable) {
            level.setBlockAndUpdate(pos, state.setValue(LIT, false));
            fireable.setCookTicks(0);
            ((BlockEntity) fireable).setChanged();
        }
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.35F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.25D, (double)pos.getZ() + 0.5D, 6, 0.25D, 0.25D, 0.25D, 0.0D);
        return true;
    }

    public boolean isIgnited(BlockState state) {
        return state.getValue(LIT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.FIREABLE.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return level.isClientSide() ? null : createTickerHelper(blockEntity, BlockEntitiesNF.FIREABLE.get(), FireableBlock::serverEntityTick);
    }

    @Override
    public NodeType adjustNodeType(NodeType type, BlockState state, LivingEntity entity) {
        return (!isIgnited(state) || entity.fireImmune()) ? type : (type.walkable ? NodeType.PASSABLE_DANGER_MINOR : NodeType.IMPASSABLE_DANGER_MINOR);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return LevelUtil.pickBuildingMaterial(state.getBlock(), player.level);
    }
}
