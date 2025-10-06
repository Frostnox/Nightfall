package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.block.block.nest.GuardedNestBlockEntity;
import frostnox.nightfall.block.block.nest.NestBlockEntity;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IChaser;
import frostnox.nightfall.entity.IHomeEntity;
import frostnox.nightfall.entity.ai.goal.*;
import frostnox.nightfall.entity.ai.goal.target.TrackNearestTargetGoal;
import frostnox.nightfall.entity.entity.Diet;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.world.ContinentalWorldType;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class SpiderEntity extends HungryMonsterEntity implements IChaser, IHomeEntity {
    public enum Type {
        BLACK, BANDED, BROWN
    }
    protected static final EntityDataAccessor<SpiderEntity.Type> TYPE = SynchedEntityData.defineId(SpiderEntity.class, DataSerializersNF.SPIDER_TYPE);
    protected static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(SpiderEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> CLIMBING = SynchedEntityData.defineId(SpiderEntity.class, EntityDataSerializers.BOOLEAN);
    public int targetTime, targetTimeLast, alertedTime;
    public boolean isChasingTarget, isScout;
    protected BlockPos homePos = null;

    public SpiderEntity(EntityType<? extends SpiderEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 25D)
                .add(Attributes.MOVEMENT_SPEED, 0.28F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 4)
                .add(Attributes.FOLLOW_RANGE, 15)
                .add(AttributesNF.HEARING_RANGE.get(), 8)
                .add(AttributesNF.SLASHING_DEFENSE.get(), 0.25)
                .add(AttributesNF.PIERCING_DEFENSE.get(), 0.25);
    }

    public static EnumMap<EntityPart, AnimationData> getHeadAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F / 16F, 0F / 16F, 4F / 16F), new Vector3f(0, 0, 0)));
        return map;
    }

    public SpiderEntity.Type getSpiderType() {
        return getEntityData().get(TYPE);
    }

    public boolean isSpecial() {
        return getEntityData().get(SPECIAL);
    }

    public void setClimbing(boolean climbing) {
        getEntityData().set(CLIMBING, climbing);
    }

    public boolean isClimbing() {
        return getEntityData().get(CLIMBING);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new FleeEntityOrHomeGoal<>(this, LivingEntity.class, 1.1D, 1.2D, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else return entity.getType().is(TagsNF.SPIDER_PREDATOR);
        }));
        goalSelector.addGoal(3, new RushAttackGoal(this, 1.1D) {
            @Override
            public void start() {
                super.start();
                isChasingTarget = true;
                NetworkHandler.toAllTracking(mob, new GenericEntityToClient(NetworkHandler.Type.CHASER_ACQUIRE_TARGET_CLIENT, mob.getId()));
            }

            @Override
            public void stop() {
                super.stop();
                isChasingTarget = false;
                NetworkHandler.toAllTracking(mob, new GenericEntityToClient(NetworkHandler.Type.CHASER_REMOVE_TARGET_CLIENT, mob.getId()));
            }
        });
        goalSelector.addGoal(4, new MoveToNestGoal(this, 1.3D, 1.1D) {
            @Override
            public boolean canUse() {
                if(isScout || alertedTime > 0) return false;
                else return super.canUse();
            }
        });
        goalSelector.addGoal(5, new FleeDamageGoal(this, 1.1D));
        goalSelector.addGoal(6, new EatEntityGoal(this, 1D, 15, 2));
        goalSelector.addGoal(7, new EatBlockGoal(this, 1D, 15, 2));
        goalSelector.addGoal(8, new WanderLandNestGoal(this, 0.9D));
        goalSelector.addGoal(9, new RandomLookGoal(this, 0.02F / 6));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new TrackNearestTargetGoal<>(this, LivingEntity.class, true, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else if(entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
            else return entity.getType().is(TagsNF.SPIDER_PREY);
        }));
    }

    @Override
    public double getReducedAIThresholdSqr() {
        return 100 * 100;
    }

    @Override
    public void die(DamageSource pCause) {
        super.die(pCause);
        if(dead && homePos != null && !level.isClientSide && level.getBlockEntity(homePos) instanceof NestBlockEntity nest) {
            nest.stopTrackingEntity(getUUID());
            if(isScout && nest instanceof GuardedNestBlockEntity guardedNest && getUUID().equals(guardedNest.scout)) {
                guardedNest.scout = null;
                guardedNest.setChanged();
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason pReason) {
        super.remove(pReason);
        if(pReason.shouldDestroy() && homePos != null && !level.isClientSide && level.getBlockEntity(homePos) instanceof NestBlockEntity nest) {
            nest.stopTrackingEntity(getUUID());
            if(isScout && nest instanceof GuardedNestBlockEntity guardedNest && getUUID().equals(guardedNest.scout)) {
                guardedNest.scout = null;
                guardedNest.setChanged();
            }
        }
    }

    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    @Override
    public boolean canTargetFromSound(LivingEntity target) {
        return target.getType().is(TagsNF.SPIDER_PREY) || target instanceof Player;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target.canBeSeenAsEnemy() && !(target instanceof SpiderEntity);
    }

    @Override
    public ResourceLocation pickActionEnemy(double distanceSqr, Entity target) {
        return switch(getSpiderType()) {
            case BLACK -> ActionsNF.SPIDER_BITE_POISONOUS.getId();
            case BANDED -> ActionsNF.SPIDER_BITE_PARALYZING.getId();
            case BROWN -> ActionsNF.SPIDER_BITE_STRONG.getId();
        };
    }

    @Override
    public void tick() {
        super.tick();
        targetTimeLast = targetTime;
        if(isAlive()) {
            if(alertedTime > 0) alertedTime--;
            if(isChasingTarget) {
                if(targetTime < 9) targetTime++;
            }
            else if(targetTime > 0) targetTime--;
        }
        if(!level.isClientSide) {
            setClimbing(horizontalCollision);
            if(isScout && homePos != null && homePos.distToCenterSqr(position()) > 30 * 30 && level.getBlockEntity(homePos) instanceof GuardedNestBlockEntity nest) {
                nest.scout = null;
                nest.setChanged();
                isScout = false;
            }
        }
    }

    @Override
    public boolean onClimbable() {
        return false;
        //return isClimbing();
    }

    @Override
    public void makeStuckInBlock(BlockState pState, Vec3 pMotionMultiplier) {
        if(!pState.is(TagsNF.SPIDER_FREE_TRAVEL_BLOCK)) {
            super.makeStuckInBlock(pState, pMotionMultiplier);
        }
    }

    @Override
    public float getBlockJumpFactor() {
        BlockState inBlock = level.getBlockState(blockPosition());
        if(inBlock.is(TagsNF.SPIDER_FREE_TRAVEL_BLOCK)) return 1F;
        float jump = inBlock.getBlock().getJumpFactor();
        if(jump == 1F) {
            BlockState onBlock = level.getBlockState(getBlockPosBelowThatAffectsMyMovement());
            return onBlock.is(TagsNF.SPIDER_FREE_TRAVEL_BLOCK) ? 1F : onBlock.getBlock().getJumpFactor();
        }
        else return jump;
    }

    @Override
    public float getBlockSpeedFactor() {
        BlockState inBlock = level.getBlockState(blockPosition());
        if(inBlock.is(TagsNF.SPIDER_FREE_TRAVEL_BLOCK)) return 1F;
        float speed = inBlock.getBlock().getSpeedFactor();
        if(speed == 1F) {
            BlockState onBlock = level.getBlockState(getBlockPosBelowThatAffectsMyMovement());
            return onBlock.is(TagsNF.SPIDER_FREE_TRAVEL_BLOCK) ? 1F : onBlock.getBlock().getSpeedFactor();
        }
        else return speed;
    }

    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return super.calculateFallDamage(pFallDistance, pDamageMultiplier) - 30;
    }

    @Override
    public void push(double pX, double pY, double pZ) {
        super.push(pX * 2, pY * 1.5, pZ * 2);
    }

    @Override
    public float getVisionAngle() {
        return 150F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, Type.BLACK);
        entityData.define(SPECIAL, false);
        entityData.define(CLIMBING, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        int type = getSpiderType().ordinal();
        if(type != 0) tag.putInt("type", type);
        boolean special = isSpecial();
        if(special) tag.putBoolean("special", special);
        boolean climbing = isClimbing();
        if(climbing) tag.putBoolean("climbing", climbing);
        if(homePos != null) tag.put("homePos", NbtUtils.writeBlockPos(homePos));
        tag.putBoolean("isScout", isScout);
        tag.putInt("alertedTime", alertedTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, Type.values()[tag.getInt("type")]);
        getEntityData().set(SPECIAL, tag.getBoolean("special"));
        getEntityData().set(CLIMBING, tag.getBoolean("climbing"));
        if(tag.contains("homePos")) homePos = NbtUtils.readBlockPos(tag.getCompound("homePos"));
        isScout = tag.getBoolean("isScout");
        alertedTime = tag.getInt("alertedTime");
    }

    @Override
    public float getPushResistance() {
        return PUSH_LOW;
    }

    @Override
    public float getPushForce() {
        return PUSH_LOW;
    }

    public static class GroupData extends AgeableMob.AgeableMobGroupData {
        public final Type type;

        public GroupData(Type type) {
            super(0F);
            this.type = type;
        }

        public static GroupData create(float humidity) {
            Type type;
            if(humidity >= ContinentalChunkGenerator.HIGH_CLIMATE) type = Type.BLACK;
            else if(humidity > ContinentalChunkGenerator.LOW_CLIMATE) type = Type.BANDED;
            else type = Type.BROWN;
            return new GroupData(type);
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        if(!(spawnDataIn instanceof GroupData)) {
            spawnDataIn = GroupData.create(ChunkData.get(worldIn.getLevel().getChunkAt(blockPosition())).getHumidity(blockPosition()));
        }
        getEntityData().set(TYPE, ((GroupData) spawnDataIn).type);
        if(random.nextInt() % 8192 == 0) getEntityData().set(SPECIAL, true);
        return spawnDataIn;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height * 0.5F;
    }

    @Override
    public ParticleOptions getHurtParticle() {
        return ParticleTypesNF.BLOOD_PALE_BLUE.get();
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() + 0.1F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if(!isChasingTarget) return SoundEvents.SPIDER_AMBIENT;
        else return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {
        playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    @Override
    protected int getMaxSatiety() {
        return (int) (ContinentalWorldType.DAY_LENGTH * 2);
    }

    @Override
    public Diet getDiet() {
        return Diet.CARNIVORE;
    }

    @Override
    public boolean canEat(Entity entity) {
        if(super.canEat(entity)) return true;
        else if(entity instanceof LivingEntity livingEntity) return livingEntity.deathTime > 20 && !(entity instanceof SpiderEntity) && entity.getType().is(TagsNF.EDIBLE_CORPSE);
        else return false;
    }

    @Override
    public SoundEvent getEatSound() {
        return null; //TODO:
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return EquipmentSlot.CHEST;
    }

    @Override
    public @Nullable BlockPos getHomePos() {
        return homePos;
    }

    @Override
    public void setHomePos(@Nullable BlockPos pos) {
        homePos = pos;
        if(homePos == null) isScout = false;
    }

    @Override
    public void onExitHome() {
        IHomeEntity.super.onExitHome();
        alertedTime = 20 * 20 + random.nextInt(20 * 5);
    }

    @Override
    public void setChasing(boolean value) {
        isChasingTarget = value;
    }

    @Override
    public boolean isChasing() {
        return isChasingTarget;
    }
}
