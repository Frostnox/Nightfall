package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.ai.goal.*;
import frostnox.nightfall.entity.entity.Diet;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.animation.AnimationData;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class WolfEntity extends AnimalEntity implements IOrientedHitBoxes {
    public enum Type {
        DIRE, STRIPED, TIMBER
    }
    public static final int GROWL_DURATION = 8 * 20;
    private static final EntityPart[] OBB_PARTS = new EntityPart[]{EntityPart.BODY, EntityPart.NECK, EntityPart.HEAD};
    protected static final EntityDataAccessor<WolfEntity.Type> TYPE = SynchedEntityData.defineId(WolfEntity.class, DataSerializersNF.WOLF_TYPE);
    protected static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(WolfEntity.class, EntityDataSerializers.BOOLEAN);
    public int growlTicks;

    public WolfEntity(EntityType<? extends WolfEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 90D)
                .add(Attributes.MOVEMENT_SPEED, 0.275F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 4)
                .add(Attributes.FOLLOW_RANGE, 30)
                .add(AttributesNF.HEARING_RANGE.get(), 24);
    }

    public static EnumMap<EntityPart, AnimationData> getHeadAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -2.5F/16F, -7.5F/16F), new Vector3f(0, 0, 0)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, -1F/16F, -1.5F/16F), new Vector3f(0, 0, 0)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(0, 0, 0)));
        return map;
    }

    public WolfEntity.Type getWolfType() {
        return getEntityData().get(TYPE);
    }

    public boolean isSpecial() {
        return getEntityData().get(SPECIAL);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatAtHeightGoal(this, 0.8D));
//        goalSelector.addGoal(2, new StepUpLandFleeEntityGoal<>(this, LivingEntity.class, 1.6D, 1.8D, (entity) -> {
//            if(entity.isDeadOrDying()) return false;
//            else return entity.getType().is(TagsNF.WOLF_PREDATOR);
//        }));
//        goalSelector.addGoal(3, new StepUpRushAttackGoal(this, 1.7D));
//        goalSelector.addGoal(4, new StepUpFleeDamageGoal(this, 1.7D));
//        goalSelector.addGoal(5, new EatEntityGoal(this, 1D, 15, 2));
//        goalSelector.addGoal(6, new EatBlockGoal(this, 1D, 15, 2));
//        goalSelector.addGoal(7, new ReducedWanderLandGoal(this, 0.75D, 6));
//        goalSelector.addGoal(8, new RandomLookGoal(this, 0.02F / 8));
//        targetSelector.addGoal(1, new HurtByTargetGoal(this));
//        targetSelector.addGoal(2, new TrackNearestTargetGoal<>(this, LivingEntity.class, true, (entity) -> {
//            if(entity.isDeadOrDying()) return false;
//            else if(entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
//            else return entity.getType().is(TagsNF.WOLF_PREY);
//        }) {
//            @Override
//            protected double getFollowDistance() {
//                return super.getFollowDistance() * Math.max(0.3, (1D - getSatietyPercent()));
//            }
//        });
    }

    @Override
    public boolean canTargetFromSound(LivingEntity target) {
        return target.getType().is(TagsNF.WOLF_SOLO_PREY) || target instanceof Player;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target.canBeSeenAsEnemy() && !(target instanceof WolfEntity || target instanceof DrakefowlEntity || target instanceof DrakefowlBabyEntity);
    }

    @Override
    public boolean shouldFleeFrom(LivingEntity target) {
        return false;
    }

    @Override
    public ResourceLocation pickActionEnemy(double distanceSqr, Entity target) {
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if(!isRemoved() && getActionTracker().isInactive() && getTarget() == null && growlTicks > 0) growlTicks--;
    }

    @Override
    protected void simulateTime(int timePassed) {
        growlTicks = Math.max(0, growlTicks - timePassed);
        super.simulateTime(timePassed);
    }

    @Override
    public float getVisionAngle() {
        return 100F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, Type.DIRE);
        entityData.define(SPECIAL, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        int type = getWolfType().ordinal();
        if(type != 0) tag.putInt("type", type);
        boolean special = isSpecial();
        if(special) tag.putBoolean("special", special);
        tag.putInt("growlTicks", growlTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, Type.values()[tag.getInt("type")]);
        getEntityData().set(SPECIAL, tag.getBoolean("special"));
        growlTicks = tag.getInt("growlTicks");
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
            if(temperature < 0.3F) type = Type.DIRE;
            else if(temperature > 0.7F) type = Type.STRIPED;
            else type = Type.TIMBER;
            spawnDataIn = new GroupData(type);
        }
        getEntityData().set(TYPE, type);
        if(type == Type.DIRE) getAttribute(AttributesNF.FROST_DEFENSE.get()).setBaseValue(0.5);
        else if(type == Type.TIMBER) getAttribute(AttributesNF.FROST_DEFENSE.get()).setBaseValue(0.25);
        if(random.nextInt() % (type == Type.TIMBER ? 4 : 10) == 0) getEntityData().set(SPECIAL, true);
        return spawnDataIn;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height - 0.01F;
    }

    @Override
    public float getVoicePitch() {
        return (switch(getWolfType()) {
            case DIRE -> 1.04F;
            case STRIPED -> 0.96F;
            case TIMBER -> 1F;
        }) + (random.nextFloat() - random.nextFloat()) * 0.1F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.WOLF_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.WOLF_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {
        playSound(SoundsNF.WOLF_STEP.get(), 0.15F, 1.0F);
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
        return SoundsNF.WOLF_EAT.get();
    }

    @Override
    public boolean includeAABB() {
        return !isAlive();
    }

    @Override
    public float getModelScale() {
        return getWolfType() == Type.DIRE ? 17F/16F : 1F;
    }

    @Override
    public Vector3f getOBBTranslation() {
        return new Vector3f(0, 10F/16F, 0);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDefaultAnimMap() {
        EnumMap<EntityPart, AnimationData> map = getGenericAnimMap();
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -2.5F/16F, -7.5F/16F), new Vector3f(0, 0, 0), new Vector3f(0, 13.5F, 0)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, -1F/16F, -1.5F/16F), new Vector3f(0, 0, 0), new Vector3f(0, -1.5F, -6F)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0F)));
        return map;
    }

    @Override
    public EntityPart[] getOrderedOBBParts() {
        return OBB_PARTS;
    }

    @Override
    public OBB[][] getDefaultOBBs() {
        return new OBB[][] {
                new OBB[] {
                        new OBB(7.5F/16F, 6.5F/16F, 14.5F/16F, 0, 0F/16F, 0F/16F)
                },
                new OBB[0],
                new OBB[] {
                        new OBB(6.5F/16F, 5.5F/16F, 4.5F/16F, 0, 1F/16F, 0.5F/16F),
                        new OBB(3.5F/16F, 3.5F/16F, 3.5F/16F, 0, 0F/16F, 1.5F/16F + 2.5F/16F)
                }
        };
    }

    @Override
    public AABB getEnclosingAABB() {
        AABB bb = getBoundingBox();
        return new AABB(bb.minX - 0.5, bb.minY, bb.minZ - 0.5, bb.maxX + 0.5, bb.maxY + 0.5, bb.maxZ + 0.5);
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return boxIndex >= 1 ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
    }
}
