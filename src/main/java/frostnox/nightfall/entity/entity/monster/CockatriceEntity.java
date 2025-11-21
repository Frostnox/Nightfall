package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.Poise;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IChaser;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.ai.goal.*;
import frostnox.nightfall.entity.ai.goal.target.TrackNearestTargetGoal;
import frostnox.nightfall.entity.entity.Diet;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class CockatriceEntity extends HungryMonsterEntity implements IOrientedHitBoxes, IChaser {
    public enum Type {
        BRONZE, EMERALD
    }
    private static final EntityPart[] OBB_PARTS = new EntityPart[]{EntityPart.BODY, EntityPart.NECK, EntityPart.HEAD};
    protected static final EntityDataAccessor<CockatriceEntity.Type> TYPE = SynchedEntityData.defineId(CockatriceEntity.class, DataSerializersNF.COCKATRICE_TYPE);
    protected static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(CockatriceEntity.class, EntityDataSerializers.BOOLEAN);
    public int targetTime, targetTimeLast;
    public boolean isChasingTarget;

    public CockatriceEntity(EntityType<? extends CockatriceEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 100D)
                .add(Attributes.MOVEMENT_SPEED, 0.275F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 4)
                .add(Attributes.FOLLOW_RANGE, 30)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.SLASHING_DEFENSE.get(), 0.35)
                .add(AttributesNF.PIERCING_DEFENSE.get(), 0.2)
                .add(AttributesNF.FIRE_DEFENSE.get(), 0.3)
                .add(AttributesNF.POISE.get(), Poise.LOW.ordinal());
    }

    public static EnumMap<EntityPart, AnimationData> getHeadAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -7.5F/16F, 5F/16F)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, -10F/16F, 0F/16F), new Vector3f(35, 0, 0)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(-35, 0, 0)));
        return map;
    }

    public CockatriceEntity.Type getCockatriceType() {
        return getEntityData().get(TYPE);
    }

    public boolean isSpecial() {
        return getEntityData().get(SPECIAL);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatAtHeightGoal(this, 0.8D));
        goalSelector.addGoal(2, new StepUpFleeEntityGoal<>(this, LivingEntity.class, 1.6D, 1.8D, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else return entity.getType().is(TagsNF.COCKATRICE_PREDATOR);
        }));
        goalSelector.addGoal(3, new StepUpRushAttackGoal(this, 1.7D) {
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
        goalSelector.addGoal(4, new StepUpFleeDamageGoal(this, 1.7D));
        goalSelector.addGoal(5, new EatEntityGoal(this, 1D, 15, 2));
        goalSelector.addGoal(6, new EatBlockGoal(this, 1D, 15, 2));
        goalSelector.addGoal(7, new ReducedWanderLandGoal(this, 0.75D, 6));
        goalSelector.addGoal(8, new RandomLookGoal(this, 0.02F / 8));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new TrackNearestTargetGoal<>(this, LivingEntity.class, true, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else if(entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
            else return entity.getType().is(TagsNF.COCKATRICE_PREY);
        }) {
            @Override
            protected double getFollowDistance() {
                return super.getFollowDistance() * Math.max(0.3, (1D - getSatietyPercent()));
            }
        });
    }

    @Override
    public double getReducedAIThresholdSqr() {
        return 250 * 250;
    }

    @Override
    public boolean canTargetFromSound(LivingEntity target) {
        return target.getType().is(TagsNF.COCKATRICE_PREY) || target instanceof Player;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target.canBeSeenAsEnemy() && !(target instanceof CockatriceEntity);
    }

    @Override
    public ResourceLocation pickActionEnemy(double distanceSqr, Entity target) {
        if(distanceSqr > 2.75 * 2.75 && random.nextFloat() < Math.min(0.95, distanceSqr / (6 * 6))) return ActionsNF.COCKATRICE_SPIT.getId();
        else {
            if(target.getBbHeight() < 5F/16F) return ActionsNF.COCKATRICE_BITE.getId();
            else if(distanceSqr > 1.75 * 1.75) return random.nextFloat() < 0.75F ? ActionsNF.COCKATRICE_BITE.getId() : ActionsNF.COCKATRICE_CLAW.getId();
            else return random.nextFloat() < 0.5F ? ActionsNF.COCKATRICE_BITE.getId() : ActionsNF.COCKATRICE_CLAW.getId();
        }
    }

    @Override
    public void tick() {
        super.tick();
        targetTimeLast = targetTime;
        if(isAlive()) {
            if(isChasingTarget) {
                if(targetTime < 9) targetTime++;
            }
            else if(targetTime > 0) targetTime--;
        }
    }

    @Override
    public float getVisionAngle() {
        return 100F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, Type.EMERALD);
        entityData.define(SPECIAL, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        int type = getCockatriceType().ordinal();
        if(type != 0) tag.putInt("type", type);
        boolean special = isSpecial();
        if(special) tag.putBoolean("special", special);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, Type.values()[tag.getInt("type")]);
        getEntityData().set(SPECIAL, tag.getBoolean("special"));
    }

    public static class GroupData extends AgeableMob.AgeableMobGroupData {
        public final Type type;

        public GroupData(Type type) {
            super(0F);
            this.type = type;
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        Type type;
        if(spawnDataIn instanceof GroupData data) type = data.type;
        else {
            float temperature = ChunkData.get(worldIn.getLevel().getChunkAt(blockPosition())).getTemperature(blockPosition());
            if(temperature > 0.7F) type = Type.BRONZE;
            else type = Type.EMERALD;
            spawnDataIn = new GroupData(type);
        }
        getEntityData().set(TYPE, type);
        if(random.nextInt() % 8192 == 0) getEntityData().set(SPECIAL, true);
        return spawnDataIn;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height - 0.01F;
    }

    @Override
    public float getVoicePitch() {
        return 1.0F + (random.nextFloat() - random.nextFloat()) * 0.1F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if(!isChasingTarget) return SoundsNF.COCKATRICE_AMBIENT.get();
        else return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.COCKATRICE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.COCKATRICE_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {
        playSound(SoundsNF.COCKATRICE_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    protected int getMaxSatiety() {
        return (int) (ContinentalWorldType.DAY_LENGTH * 1);
    }

    @Override
    public Diet getDiet() {
        return Diet.CARNIVORE;
    }

    @Override
    public boolean canEat(Entity entity) {
        if(super.canEat(entity)) return true;
        else if(entity instanceof LivingEntity livingEntity) return livingEntity.deathTime > 20 && entity.getType().is(TagsNF.EDIBLE_CORPSE);
        else return false;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundsNF.COCKATRICE_BITE.get();
    }

    @Override
    public boolean includeAABB() {
        return true;
    }

    @Override
    public Vector3f getOBBTranslation() {
        return new Vector3f(0, 12F/16F, 0);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDefaultAnimMap() {
        EnumMap<EntityPart, AnimationData> map = getGenericAnimMap();
        float trackAmount = AnimationUtil.applyEasing(targetTime / 9F, Easing.inOutSine);
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -7.5F/16F, -5F/16F)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, -8F/16F, 0F/16F), new Vector3f(35 + trackAmount * 45, 0, 0)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 1 * trackAmount/16F), new Vector3f(-35 + trackAmount * -45, 0, 0)));
        return map;
    }

    @Override
    public EntityPart[] getOrderedOBBParts() {
        return OBB_PARTS;
    }

    @Override
    public OBB[][] getDefaultOBBs() {
        return new OBB[][] {
                new OBB[] { new OBB(3.5F/16F, 10.5F/16F, 3.5F/16F, 0, 4F/16F, -0.5F/16F)},
                new OBB[] { new OBB(4.5F/16F, 4.5F/16F, 6.5F/16F, 0, 2F/16F, 0.5F/16F),
                        new OBB(2.5F/16F, 3.5F/16F, 3.5F/16F, 0, 1.5F/16F, 4.5F/16F + 0.5F/16F)}
        };
    }

    @Override
    public AABB getEnclosingAABB() {
        AABB bb = getBoundingBox();
        return new AABB(bb.minX - 0.55, bb.minY, bb.minZ - 0.55, bb.maxX + 0.55, bb.maxY + 0.55, bb.maxZ + 0.55);
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return boxIndex >= 0 ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
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
