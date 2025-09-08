package frostnox.nightfall.entity.entity;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.*;
import frostnox.nightfall.block.IAdjustableNodeType;
import frostnox.nightfall.capability.GlobalChunkData;
import frostnox.nightfall.capability.IGlobalChunkData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.ai.pathfinding.ActionableMoveControl;
import frostnox.nightfall.entity.ai.pathfinding.EntityNavigator;
import frostnox.nightfall.entity.ai.pathfinding.LandEntityNavigator;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.entity.ai.sensing.AudioSensing;
import frostnox.nightfall.item.item.TieredArmorItem;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.network.message.world.DigBlockToClient;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.ActionToClient;
import frostnox.nightfall.registry.forge.AttributesNF;

import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.vanilla.GameEventsNF;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.data.Wrapper;
import frostnox.nightfall.world.ToolActionsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;

public abstract class ActionableEntity extends PathfinderMob {
    protected static final EntityDataAccessor<Integer> RANDOM = SynchedEntityData.defineId(ActionableEntity.class, EntityDataSerializers.INT);
    public @Nullable BlockPos lastInteractPos = null; //Last block that was mined, used, etc
    public @Nullable BlockPos lastTargetPos = null; //Last position of target before it was lost
    public boolean refreshPath = true; //Set to true to force movement goals to recalculate their path
    public boolean reactToDamage = false;
    protected final float reach, reachSqr;
    protected float lastSyncedHealth = Float.MIN_VALUE;
    protected final AudioSensing audioSensing;
    public int noDespawnTicks = -1;
    public boolean reducedAI = false;
    protected long lastTickedGameTime;

    public ActionableEntity(EntityType<? extends ActionableEntity> type, Level level) {
        super(type, level);
        audioSensing = createAudioSensing();
        moveControl = new ActionableMoveControl(this);
        setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0);
        reach = getBaseReach();
        reachSqr = reach * reach;
    }

    public static EnumMap<EntityPart, AnimationData> getGenericAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        for(EntityPart part : EntityPart.values()) map.put(part, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F)));
        return map;
    }

    public ActionableEntity getEntity() {
        return this;
    }

    protected float getBaseReach() {
        return 1F;
    }

    protected AudioSensing createAudioSensing() {
        return new AudioSensing(this, 20);
    }

    public boolean canTargetFromSound(LivingEntity target) {
        return canAttack(target);
    }

    public ResourceLocation pickActionEnemy(double distanceSqr, Entity target) {
        return ActionsNF.EMPTY.getId();
    }

    public ResourceLocation pickActionAlly(double distanceSqr, Entity ally) {
        return ActionsNF.EMPTY.getId();
    }

    public boolean panicsOnFireDamage() {
        return true;
    }

    public long getLastTickedGameTime() {
        return lastTickedGameTime;
    }

    public float getMineSpeed(BlockState block) {
        return getItemBySlot(EquipmentSlot.MAINHAND).getDestroySpeed(block);
    }

    public float getNaturalRegen() {
        return 20F / (20 * 60 * 5);
    }

    public boolean mineBlock(Level level, BlockPos pos) {
        LevelChunk chunk = level.getChunkAt(pos);
        IGlobalChunkData chunkData = GlobalChunkData.get(chunk);
        BlockState block = chunk.getBlockState(pos);
        //Formula for one tick of block break progress from player * multiplier
        float progress = chunkData.getBreakProgress(pos) + getMineSpeed(block) / block.getDestroySpeed(level, pos) / 30 * 4;
        if(progress >= 1) {
            FakePlayer player = FakePlayerFactory.get((ServerLevel) level, LevelUtil.FAKE_PROFILE);
            block.getBlock().playerDestroy(level, player, pos, block, level.getBlockEntity(pos), getItemBySlot(EquipmentSlot.MAINHAND));
            block.onDestroyedByPlayer(level, pos, player, true, level.getFluidState(pos));
            chunkData.removeBreakProgress(pos);
            NetworkHandler.toAllTrackingChunk(level.getChunkAt(pos), new DigBlockToClient(pos.getX(), pos.getY(), pos.getZ(), -1));
            refreshPath = true;
            return true;
        }
        else {
            SoundType sound = block.getSoundType(level, pos, this);
            level.playSound(null, pos, sound.getHitSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2F, sound.getPitch() * 0.75F);
            chunkData.setBreakProgress(pos, progress);
            NetworkHandler.toAllTrackingChunk(level.getChunkAt(pos), new DigBlockToClient(pos.getX(), pos.getY(), pos.getZ(), progress));
            return false;
        }
    }

    /**
     * @param hitPos   relative to bounding box
     * @param boxIndex index of box that was hit (-1 for bounding box)
     */
    public abstract EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex);

    public Impact modifyIncomingImpact(DamageTypeSource source, Impact impact) {
        return impact;
    }

    public boolean isImmuneTo(DamageTypeSource source, float damage) {
        return false;
    }

    protected float modifyIncomingDamageBySlot(EquipmentSlot slot, float damage) {
        if(slot == EquipmentSlot.HEAD) return damage * 1.2F;
        else return damage;
    }

    /**
     * @return modified damage to receive
     */
    public float modifyIncomingDamage(DamageTypeSource source, float damage, Wrapper<Poise> poise) {
        //Pick body part based on y collision
        if(source.hasHitCoords()) {
            float durabilityDmg = Math.max(1, damage / 5F);
            EquipmentSlot slot = getHitSlot(source.getHitCoords(), source.getHitBoxIndex());
            damage = modifyIncomingDamageBySlot(slot, damage);

            ItemStack stack = this.getItemBySlot(slot);
            if(stack.getItem() instanceof TieredArmorItem armor) {
                if(poise.val.ordinal() < armor.material.getPoise().ordinal()) poise.val = armor.material.getPoise();
                if(armor.material.isMetal()) source.tryArmorSoundConversion();
                damage = armor.material.getFinalDamage(armor.slot, source.types, stack.getDamageValue(), damage, false);
                int index = armor.slot.getIndex();
                stack.hurtAndBreak((int)durabilityDmg, this, (p_214023_1_) -> {
                    p_214023_1_.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, index));
                });
            }
        }
        //Take weighted averages of slots if source had no location
        else {
            float durabilityDmg = Math.max(1, damage / 20F);
            boolean isMetal = false;
            int totalPoise = 0;
            for(ItemStack stack : this.getArmorSlots()) {
                if(stack.getItem() instanceof TieredArmorItem armor) {
                    totalPoise += armor.material.getPoise().ordinal();
                    if(!isMetal && armor.material.isMetal()) isMetal = true;
                    damage = armor.material.getFinalDamage(armor.slot, source.types, stack.getDamageValue(), damage, true);
                    int index = armor.slot.getIndex();
                    stack.hurtAndBreak((int)durabilityDmg, this, (p_214023_1_) -> {
                        p_214023_1_.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, index));}
                    );
                }
            }
            totalPoise /= 4;
            if(poise.val.ordinal() < totalPoise) poise.val = Poise.values()[totalPoise];
            if(isMetal) source.tryArmorSoundConversion();
        }
        return damage;
    }

    public ParticleOptions getHurtParticle() {
        return ParticleTypesNF.BLOOD_RED.get();
    }

    public boolean canMineAnything() {
        return false;
    }

    public boolean canBuild() {
        return false;
    }

    public boolean canDoRangedAction() {
        return false;
    }

    public ActionableEntity getAlly() {
        return null;
    }

    public void setAlly(ActionableEntity ally) {

    }

    public float getMaxXRotPerTick() {
        return 30F;
    }

    public float getMaxYRotPerTick() {
        return 30F;
    }

    /**
     * @return absolute angle relative to center of vision that this entity can see (encompasses positive and negative rotation)
     */
    public float getVisionAngle() {
        return 75F;
    }

    public boolean canMineBlock(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        boolean climbable = state.is(BlockTags.CLIMBABLE);
        if(!climbable && state.getShape(level, pos).isEmpty()) return false;
        double x = pos.getX() + 0.5 - getX();
        double y = pos.getY() + 0.5 - getEyeY();
        double z = pos.getZ() + 0.5 - getZ();
        if(x * x + y * y + z * z > getReachSqr()) return false;
        return (climbable || state.getMaterial().blocksMotion()) && state.getDestroySpeed(level, pos) >= 0F;
    }

    public float getReach() {
        return reach;
    }

    public float getReachSqr() {
        return reachSqr;
    }

    /**
     * @return the closest distance to the target that is considered safe
     */
    public float getSafeDistanceToTarget() {
        return 0;
    }

    /**
     * @return the target's position, current or last known (null position if neither)
     */
    public @Nullable BlockPos getTargetPos() {
        return getTarget() != null ? getTarget().blockPosition() : lastTargetPos;
    }

    public boolean isInterruptible() {
        if(!isAlive()) return false;
        IActionTracker capA = getActionTracker();
        return capA.isInactive() || capA.getAction().isInterruptible();
    }

    public IActionTracker getActionTracker() {
        return ActionTracker.get(this);
    }

    public void startAction(ResourceLocation actionID) {
        if(!isAlive()) return;
        IActionTracker capA = getActionTracker();
        if(!capA.isInactive()) return;
        capA.startAction(actionID);
        capA.setFrame(0); //Tick update runs after other logic so start at 0 instead
        if(!level.isClientSide()) {
            NetworkHandler.toAllTracking(this, new ActionToClient(actionID, getId()));
        }
    }

    public void queueAction() {
        getActionTracker().queue();
        if(!level.isClientSide()) NetworkHandler.toAllTracking(this, new GenericEntityToClient(NetworkHandler.Type.QUEUE_ACTION_TRACKER, getId()));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        setLeftHanded(false);
        lastTickedGameTime = worldIn.getLevelData().getGameTime();
        entityData.set(RANDOM, worldIn.getRandom().nextInt() & Integer.MAX_VALUE);
        if(noDespawnTicks == -1 && reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION && reason != MobSpawnType.STRUCTURE
        && reason != MobSpawnType.BREEDING && reason != MobSpawnType.EVENT) noDespawnTicks = 0;
        return spawnDataIn;
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return super.shouldDespawnInPeaceful();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new LandEntityNavigator(this, level);
    }

    @Override
    @Deprecated
    public PathNavigation getNavigation() {
        return super.getNavigation();
    }

    public EntityNavigator getNavigator() {
        return (EntityNavigator) getNavigation();
    }

    public NodeType adjustPathType(NodeType type, BlockState state) {
        if(state.getBlock() instanceof IAdjustableNodeType adjustable) type = adjustable.adjustNodeType(type, state, this);
        else if(type == NodeType.CLOSED) return type;
        else if(state.getBlock() instanceof BaseFireBlock) return fireImmune() ? type : NodeType.PASSABLE_DANGER_MAJOR;
        FluidState fluid = state.getFluidState();
        if(!fluid.isEmpty()) {
            if(fluid.is(FluidTags.LAVA)) return fireImmune() ? NodeType.PASSABLE_FLUID : NodeType.IMPASSABLE_DANGER;
            else if(fluid.is(FluidTags.WATER)) return isSensitiveToWater() ? NodeType.IMPASSABLE_DANGER : NodeType.PASSABLE_FLUID;
        }
        return type;
    }

    public float getMaxNodeCost() {
        if(getTargetPos() != null) return 50F;
        else return 35F;
    }

    public boolean shouldFleeFrom(LivingEntity target) {
        return getHealth() / getMaxHealth() < 0.25F;
    }

    public void onKillRemoval() {
        level.broadcastEntityEvent(this, (byte)60);
    }

    public AudioSensing getAudioSensing() {
        return audioSensing;
    }

    public MobType getMobTypeSecondary() {
        return MobType.UNDEFINED;
    }

    public int getSynchedRandom() {
        return getEntityData().get(RANDOM);
    }

    public abstract boolean dropLootFromSkinning();

    public boolean canBeSkinned() {
        return dropLootFromSkinning() && !isAlive();
    }

    public boolean tryToolAction(ItemStack stack, Player player, InteractionHand hand, ToolAction toolAction) {
        if(toolAction == ToolActionsNF.SKIN) {
            if(canBeSkinned()) {
                if(level.isClientSide) return true;
                DamageSource damageSource = getLastDamageSource();
                forceDropAllDeathLoot(damageSource == null ? DamageTypeSource.GENERIC : damageSource);
                onKillRemoval();
                remove(RemovalReason.KILLED);
                return true;
            }
        }
        return false;
    }

    public float getAttackYRot(float partial) {
        return Mth.lerp(partial, yBodyRotO, yBodyRot);
    }

    public double getReducedAIThresholdSqr() {
        return 150 * 150;
    }

    public float getBaseJumpPower() {
        return 0.42F;
    }

    @Override
    protected float getJumpPower() {
        return getBaseJumpPower() * getBlockJumpFactor();
    }

    public void forceDropAllDeathLoot(DamageSource damageSource) {
        super.dropAllDeathLoot(damageSource);
    }

    @Override
    protected void dropAllDeathLoot(DamageSource pDamageSource) {
        if(!dropLootFromSkinning()) super.dropAllDeathLoot(pDamageSource);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader pLevel) {
        return !pLevel.containsAnyLiquid(this.getBoundingBox()) && pLevel.noCollision(this);
    }

    @Override
    public void travel(Vec3 target) {
        //Increase movement through lava and allow it to be controlled by swim speed and slowdown, similar to water
        if(isEffectiveAi() && isInLava() && isAffectedByFluids() && !canStandOnFluid(level.getFluidState(blockPosition()))) {
            double speed = 0.7D * getLavaSlowDown();
            double gravity = getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
            boolean falling = getDeltaMovement().y <= 0D;
            double oldY = this.getY();
            this.moveRelative(0.02F * (float) this.getAttributeValue(ForgeMod.SWIM_SPEED.get()), target);
            this.move(MoverType.SELF, this.getDeltaMovement());

            if(this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(speed, 0.8D, speed));
                Vec3 fluidDir = this.getFluidFallingAdjustedMovement(gravity, falling, this.getDeltaMovement());
                this.setDeltaMovement(fluidDir);
            }
            else this.setDeltaMovement(this.getDeltaMovement().scale(speed));

            if(!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -gravity / 4.0D, 0.0D));
            }

            Vec3 vec34 = this.getDeltaMovement();
            if(this.horizontalCollision && this.isFree(vec34.x, vec34.y + 0.6D - this.getY() + oldY, vec34.z)) {
                this.setDeltaMovement(vec34.x, 0.3F, vec34.z);
            }
        }
        else super.travel(target);
    }

    @Override
    protected float tickHeadTurn(float pYRot, float pAnimStep) {
        float oldYBodyRot = yBodyRot;
        float step = super.tickHeadTurn(pYRot, pAnimStep);
        if(isAlive() && !getActionTracker().isInactive()) {
            yBodyRot = oldYBodyRot;
            yBodyRotO = oldYBodyRot;
        }
        return step;
    }

    @Override
    public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> pFluidTag, double pMotionScale) {
        //Reduce influence of flowing water
        return super.updateFluidHeightAndDoFluidPushing(pFluidTag, pFluidTag == FluidTags.LAVA ? pMotionScale : pMotionScale * 0.2D);
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.9F;
    }

    protected float getLavaSlowDown() {
        return getWaterSlowDown();
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        if(isDeadOrDying() && getType().getHeight() > getType().getWidth()) {
            return super.getDimensions(pPose).scale(1F, getType().getWidth() / getType().getHeight());
        }
        else return super.getDimensions(pPose);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if(DATA_HEALTH_ID.equals(pKey)) {
            float health = getHealth();
            if(Math.signum(lastSyncedHealth) != Math.signum(health)) refreshDimensions();
            lastSyncedHealth = health;
        }
        super.onSyncedDataUpdated(pKey);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(RANDOM, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("random", getSynchedRandom());
        if(deathTime > 0) tag.putInt("deathTime", deathTime);
        if(noDespawnTicks != -1) tag.putInt("noDespawnTicks", noDespawnTicks);
        if(lastDamageSource != null) {
            tag.putString("lastDamageSourceId", lastDamageSource.getMsgId());
            if(lastDamageSource instanceof DamageTypeSource typeSource) {
                tag.putInt("lastDamageSourceTypes", typeSource.types.length);
                for(int i = 0; i < typeSource.types.length; i++) {
                    tag.putString("lastDamageSourceType_" + i, typeSource.types[i].toString());
                }
            }
        }
        tag.putLong("lastGameTime", lastTickedGameTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(RANDOM, tag.getInt("random"));
        deathTime = tag.getInt("deathTime");
        if(tag.contains("noDespawnTicks")) noDespawnTicks = tag.getInt("noDespawnTicks");
        if(tag.contains("lastDamageSourceId")) {
            String sourceId = tag.getString("lastDamageSourceId");
            DamageType[] types;
            if(tag.contains("lastDamageSourceTypes")) {
                int size = tag.getInt("lastDamageSourceTypes");
                types = new DamageType[size];
                for(int i = 0; i < size; i++) {
                    String typeName = tag.getString("lastDamageSourceType_" + i);
                    for(DamageType type : DamageType.values()) {
                        if(type.toString().equals(typeName)) {
                            types[i] = type;
                            break;
                        }
                    }
                    if(types[i] == null) types[i] = DamageType.ABSOLUTE;
                }
            }
            else types = new DamageType[] { DamageType.ABSOLUTE };
            lastDamageSource = new DamageTypeSource(sourceId, types);
        }
        lastTickedGameTime = tag.getLong("lastGameTime");
    }

    @Override
    public @Nullable DamageSource getLastDamageSource() {
        if(!isAlive()) return lastDamageSource;
        else return super.getLastDamageSource();
    }

    @Override
    protected void pushEntities() {
        if(!isDeadOrDying()) {
            List<Entity> entities = level.isClientSide ? ClientEngine.get().getPlayerToPush(this) :
                level.getEntities(this, getBoundingBox(), EntitySelector.pushableBy(this));
            if(!entities.isEmpty()) {
                int maxGroup = level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
                if(maxGroup > 0 && entities.size() > maxGroup - 1 && random.nextInt(4) == 0) {
                    int count = 0;
                    for(Entity entity : entities) {
                        if(!entity.isPassenger()) ++count;
                    }
                    if(count > maxGroup - 1) hurt(DamageSource.CRAMMING, 6.0F);
                }
                for(Entity entity : entities) doPush(entity);
            }
        }
    }

    @Override
    public boolean isInWall() {
        if(level.isClientSide) return false;
        else return super.isInWall();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        audioSensing.tick();
        noActionTime = 0; //This is mainly used for random walking goals
        if(noDespawnTicks > 0) noDespawnTicks--;
        LivingEntity target = getTarget();
        if(target != null && target.isRemoved()) setTarget(null);
    }

    protected void simulateTime(long timePassed) {
        if(noDespawnTicks > 0) {
            noDespawnTicks = (int) Math.max(0, noDespawnTicks - timePassed - 1);
            if(noDespawnTicks == 0) discard();
        }
        if(!isRemoved()) {
            heal(getNaturalRegen() * timePassed);
        }
    }

    @Override
    public void tick() {
        if(!level.isClientSide) {
            if(tickCount % 200 == 0) {
                Entity nearPlayer = level.getNearestPlayer(this, -1);
                if(nearPlayer == null) reducedAI = true;
                else reducedAI = nearPlayer.distanceToSqr(this) > getReducedAIThresholdSqr();
            }
            long timePassed = level.getGameTime() - lastTickedGameTime;
            if(timePassed > 1) simulateTime(timePassed);
            lastTickedGameTime = level.getGameTime();
        }
        float yRot = yBodyRot;
        super.tick();
        if(isRemoved()) return;
        IActionTracker capA = getActionTracker();
        if(!isAlive()) {
            if(!capA.isInactive()) capA.tick();
            if(capA.isInactive()) capA.startAction(ActionsNF.EMPTY.getId());
            yBodyRot = yRot;
            capA.setLastPosition(position());
            return;
        }
        if(!level.isClientSide && tickCount == 1) { //Make sure listener is registered even if entity hasn't moved (setPosRaw will call this function but the entity doesn't exist in the level yet so it won't be registered)
            GameEventListenerRegistrar listener = getGameEventListenerRegistrar();
            if(listener != null) listener.onListenerMove(level);
        }
        if(getHealth() < getMaxHealth()) heal(getNaturalRegen());
        capA.tick();
        if(capA.isInactive()) capA.startAction(ActionsNF.EMPTY.getId());
        if(capA.isStunned()) stopUsingItem();
        Action action = capA.getAction();
        action.onTick(this);
        //yBodyRot = yBodyRotO;
        //yHeadRot = yHeadRotO;
        float maxRotX = getMaxXRotPerTick();
        float maxRotY = getMaxYRotPerTick();
        if(action instanceof Attack attack && !capA.isInactive() && capA.getActionID() != ActionsNF.EMPTY.getId()) {
            maxRotX = attack.getMaxXRot(capA.getState());
            maxRotY = attack.getMaxYRot(capA.getState());
        }
        setXRot(Mth.clamp(getXRot(), xRotO - maxRotX, xRotO + maxRotX));
        setYRot(Mth.clamp(getYRot(), yRotO - maxRotY, yRotO + maxRotY));
        yBodyRot = Mth.clamp(yBodyRot, yBodyRotO - maxRotY, yBodyRotO + maxRotY);
        yHeadRot = Mth.clamp(yHeadRot, yHeadRotO - maxRotY, yHeadRotO + maxRotY);
        //yBodyRot = Mth.clamp(yBodyRot, yBodyRotO - maxRot, yBodyRotO + maxRot);
        //yHeadRot = Mth.clamp(yHeadRot, yBodyRot - 45, yBodyRot + 45);
        //yHeadRot = Mth.clamp(yHeadRot, yHeadRotO - maxRot, yHeadRotO + maxRot);
        if(!capA.isInactive() && action instanceof Attack && capA.getActionID() != ActionsNF.EMPTY.getId()) {
            CombatUtil.alignBodyRotWithHead(this, capA);
        }
        if(action instanceof Attack attack) {
            if(capA.isDamaging() && !level.isClientSide()) {
                List<HitData> targets = capA.getEntitiesInAttack(attack, 1F);
                for(HitData hitData : targets) {
                    Entity target = hitData.hitEntity;
                    if(target instanceof LivingEntity livingTarget && !canAttack(livingTarget)) continue;
                    target.hurt(DamageTypeSource.createAttackSource(this, attack, hitData).setImpactSoundType(attack.getImpactSoundType(this), target),
                            attack.getDamage(this) * capA.getChargeAttackMultiplier());
                    if(target instanceof LivingEntity) ((LivingEntity) target).setLastHurtByMob(this);
                }
            }
        }
        //Play attack sound on start of damage state
        if(action.getSound() != null && capA.getFrame() == 1 && !capA.hasHitPause() && capA.isDamaging()) {
            playSound(action.isChargeable() && capA.getCharge() >=  Math.round(action.getMaxCharge() * 0.75F) ? action.getExtraSound().get() : action.getSound().get(), 1F, 1F + level.random.nextFloat(-0.03F, 0.03F));
            gameEvent(GameEventsNF.ACTION_SOUND);
        }
        capA.setLastPosition(position());
    }

    @Override
    public boolean skipAttackInteraction(Entity pEntity) {
        return deathTime > 0;
    }

    @Override
    public void die(DamageSource pCause) {
        if(net.minecraftforge.common.ForgeHooks.onLivingDeath(this, pCause)) return;
        super.die(pCause);
        getActionTracker().stun(CombatUtil.STUN_LONG, false);
        Vec3 move = new Vec3(0.22 * (getSynchedRandom() % 2 == 0 ? 1 : -1), 0, 0).yRot(-MathUtil.toRadians(getYRot()));
        setDeltaMovement(getDeltaMovement().add(move));
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        if(deathTime == (dropLootFromSkinning() ? 6000 : 20) && !level.isClientSide()) {
            onKillRemoval();
            remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public boolean hasLineOfSight(Entity target) {
        if(target.level != level) {
            return false;
        }
        else {
            Vec3 start = new Vec3(getX(), getEyeY(), getZ());
            Vec3 end = new Vec3(target.getX(), target.getEyeY(), target.getZ());
            if(end.distanceToSqr(start) > 128.0D * 128.0D) return false;
            float angle = getVisionAngle();
            if(angle < 180F && CombatUtil.getRelativeHorizontalAngle(start, end, getYHeadRot()) > angle) return false;
            else {
                return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
            }
        }
    }

    @Override
    public int getMaxFallDistance() {
        if(getTarget() == null) return 3;
        else if(lastTargetPos != null) return 3 + (int) Math.max(0, (getHealth() / 10F - getMaxHealth() * 0.033F));
        else return 3 + (int) Math.max(0, (getHealth() / 5F - getMaxHealth() * 0.066F));
    }

    public boolean hasAnyLineOfSight(Entity target) {
        if(target.level != level) {
            return false;
        }
        else {
            Vec3 start = new Vec3(getX(), getEyeY(), getZ());
            Vec3 end = new Vec3(target.getX(), target.getEyeY(), target.getZ());
            if(end.distanceToSqr(start) > 128.0D * 128.0D) return false;
            else {
                return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
            }
        }
    }

    @Override
    @Nullable
    public GameEventListenerRegistrar getGameEventListenerRegistrar() {
        if(getAttribute(AttributesNF.HEARING_RANGE.get()).getValue() > 0.0) return audioSensing.getEventListenerRegistrar();
        else return null;
    }

    //Damage is handled outside the entity class
    @Override
    public boolean doHurtTarget(Entity entityIn) {
        return true;
    }

    @Override
    public boolean canBeLeashed(Player pPlayer) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        if(!isAlive() || noDespawnTicks == 0) return pDistanceToClosestPlayer > 80 * 80;
        else return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(AttributesNF.STRENGTH.get()).add(AttributesNF.HEARING_RANGE.get()).add(AttributesNF.ACTION_SPEED.get())
                .add(AttributesNF.STRIKING_DEFENSE.get()).add(AttributesNF.SLASHING_DEFENSE.get()).add(AttributesNF.PIERCING_DEFENSE.get())
                .add(AttributesNF.FIRE_DEFENSE.get()).add(AttributesNF.FROST_DEFENSE.get()).add(AttributesNF.ELECTRIC_DEFENSE.get())
                .add(AttributesNF.WITHER_DEFENSE.get()).add(AttributesNF.POISE.get())
                .add(AttributesNF.BLEEDING_RESISTANCE.get()).add(AttributesNF.POISON_RESISTANCE.get())
                .add(ForgeMod.SWIM_SPEED.get(), 1.4D);
    }
}
