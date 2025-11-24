package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.Poise;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.ai.goal.*;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.entity.Diet;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.ContinentalWorldType;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class MerborEntity extends TamableAnimalEntity implements IOrientedHitBoxes {
    public enum Type {
        BOG, BRINE, RIVER
    }
    private static final EntityPart[] OBB_PARTS = new EntityPart[]{EntityPart.BODY, EntityPart.NECK, EntityPart.HEAD};
    protected static final EntityDataAccessor<Type> TYPE = SynchedEntityData.defineId(MerborEntity.class, DataSerializersNF.MERBOR_TYPE);
    private final Goal breedGoal = new BreedGoal(this, 0.8);
    public @Nullable Type fatherType;

    public MerborEntity(EntityType<? extends AnimalEntity> type, Level level, Sex sex) {
        super(type, level, sex);
        updateGoals();
    }

    public static MerborEntity createFemale(EntityType<? extends TamableAnimalEntity> type, Level level) {
        return new MerborEntity(type, level, Sex.FEMALE);
    }

    public static MerborEntity createMale(EntityType<? extends TamableAnimalEntity> type, Level level) {
        return new MerborEntity(type, level, Sex.MALE);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 100D)
                .add(Attributes.MOVEMENT_SPEED, 0.275F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 20)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.FIRE_DEFENSE.get(), 0.5)
                .add(AttributesNF.ELECTRIC_DEFENSE.get(), -0.5)
                .add(AttributesNF.POISE.get(), Poise.LOW.ordinal());
    }

    public static EnumMap<EntityPart, AnimationData> getHeadAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, 0F/16F, -7F/16F)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F)));
        return map;
    }

    public Type getMerborType() {
        return getEntityData().get(TYPE);
    }

    @Override
    public int getMaxAirSupply() {
        return 20 * 60;
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.92F;
    }

    @Override
    public float getVisionAngle() {
        return 115F;
    }

    @Override
    public void push(Entity pEntity) {
        super.push(pEntity);
        if(!isTamed() && pEntity instanceof Player player && canAttack(player)) {
            setLastHurtByMob(player);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, Type.RIVER);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("type", getMerborType().ordinal());
        if(fatherType != null) tag.putInt("fatherType", fatherType.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, Type.values()[tag.getInt("type")]);
        if(tag.contains("fatherType")) fatherType = Type.values()[tag.getInt("fatherType")];
        updateGoals();
    }

    @Override
    public @Nullable ResourceLocation getCollapseAction() {
        return ActionsNF.MERBOR_COLLAPSE.getId();
    }

    @Override
    public ResourceLocation pickActionEnemy(double distanceSqr, Entity target) {
        return ActionsNF.MERBOR_GORE.getId();
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
        Type type = null;
        if(spawnDataIn instanceof GroupData data) type = data.type;
        else {
            if(worldIn.getLevel().getChunkSource().getGenerator() instanceof ContinentalChunkGenerator gen) {
                float elevation = gen.getElevation(getBlockX(), getBlockZ());
                if(elevation > -0.6F && elevation < 0.295F) type = Type.BRINE;
            }
            if(type == null) {
                float temperature = ChunkData.get(worldIn.getLevel().getChunkAt(blockPosition())).getTemperature(blockPosition());
                if(temperature > 0.7F) type = Type.BOG;
                else type = Type.RIVER;
            }
            spawnDataIn = new GroupData(type);
        }
        getEntityData().set(TYPE, type);
        if(random.nextInt() % 2048 == 0) getEntityData().set(SPECIAL, true);
        updateGoals();
        return spawnDataIn;
    }

    @Override
    public float getVoicePitch() {
        if(sex == Sex.MALE) return super.getVoicePitch() * 0.96F;
        else return super.getVoicePitch() * 1.04F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if(getActionTracker().getActionID().equals(getCollapseAction())) return null;
        else return SoundsNF.MERBOR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.MERBOR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.MERBOR_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        playSound(SoundsNF.MERBOR_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    public boolean shouldFleeFrom(LivingEntity target) {
        if(target.getType().is(TagsNF.MERBOR_PREDATOR)) return true;
        else if(target instanceof Player player && !player.isCreative() && !player.isSpectator()) return (sex == Sex.FEMALE && target == getTarget()) || super.shouldFleeFrom(target);
        else return false;
    }

    @Override
    public boolean canReceiveAlert(ActionableEntity alerter) {
        return alerter instanceof MerborBabyEntity || (!isTamed() && alerter instanceof MerborEntity);
    }

    @Override
    protected void updateGoals() {
        if(!level.isClientSide) {
            goalSelector.removeGoal(breedGoal);
            if(isTamed()) goalSelector.addGoal(6, breedGoal);
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatAtHeightGoal(this, 0.7D));
        goalSelector.addGoal(2, new StepUpFleeTargetGoal(this, 1.25D, 1.35D));
        goalSelector.addGoal(3, new StepUpFleeEntityGoal<>(this, LivingEntity.class, 1.25D, 1.35D, this::shouldFleeFrom));
        goalSelector.addGoal(5, new StepUpFleeDamageGoal(this, 1.35D));
        goalSelector.addGoal(7, new LureGoal(this, 10, 0.9D));
        goalSelector.addGoal(9, new EatEntityGoal(this, 1D, 15, 2));
        goalSelector.addGoal(10, new EatBlockGoal(this, 1D, 15, 2));
        goalSelector.addGoal(11, new ReducedWanderGoal(this, 0.8D, 3));
        goalSelector.addGoal(12, new RandomLookGoal(this, 0.02F / 4));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        if(getType() == EntitiesNF.MERBOR_TUSKER.get()) { //Function gets called mid-constructor so can't use sex
            goalSelector.addGoal(4, new StepUpRushAttackGoal(this, 1.25D));
        }
    }

    @Override
    public Diet getDiet() {
        return Diet.OMNIVORE;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundsNF.MERBOR_EAT.get();
    }

    @Override
    public boolean canEat(Entity entity) {
        if(super.canEat(entity)) return true;
        else if(entity instanceof LivingEntity livingEntity) return livingEntity.deathTime > 20 && entity.getType().is(TagsNF.EDIBLE_CORPSE);
        else return false;
    }

    @Override
    protected int getMaxSatiety() {
        return (int) ContinentalWorldType.DAY_LENGTH;
    }

    @Override
    public boolean canBreedWith(TamableAnimalEntity other) {
        if(other instanceof MerborEntity merbor) return sex != merbor.sex;
        else return false;
    }

    @Override
    protected void breedWith(TamableAnimalEntity other) {
        if(sex == Sex.FEMALE) {
            fatherType = ((MerborEntity) other).getMerborType();
            gestationTime = (int) ContinentalWorldType.DAY_LENGTH * 5;
        }
    }

    @Override
    protected void onGestationEnd() {
        if(!level.isClientSide) {
            for(int i = 0; i < (random.nextBoolean() ? 2 : 1); i++) {
                MerborBabyEntity piglet =  EntitiesNF.MERBOR_PIGLET.get().create(level);
                piglet.moveTo(getX(), getY(), getZ(), level.random.nextFloat() * 360, 0);
                piglet.finalizeSpawn((ServerLevel) level, level.getCurrentDifficultyAt(blockPosition()), MobSpawnType.BREEDING, new GroupData(fatherType), null);
                level.addFreshEntity(piglet);
            }
        }
        fatherType = null;
    }

    @Override
    protected boolean checkComfort() {
        if(level.getEntitiesOfClass(TamableAnimalEntity.class, getBoundingBox().inflate(8, 1, 8)).size() <= 16) {
            if(LevelData.isPresent(level)) {
                IChunkData chunkData = ChunkData.get(level.getChunkAt(blockPosition()));
                return LevelData.get(level).getSeasonalTemperature(chunkData, blockPosition()) > 0.35F && chunkData.getHumidity(blockPosition()) > 0.2F;
            }
        }
        return false;
    }

    @Override
    public boolean isFeedItem(ItemStack item) {
        return item.is(TagsNF.OMNIVORE_FOOD);
    }

    @Override
    public ResourceLocation getBreedAction() {
        return ActionsNF.MERBOR_BREED.getId();
    }

    @Override
    public boolean includeAABB() {
        return true;
    }

    @Override
    public float getModelScale() {
        return sex == Sex.MALE ? 17F/16F : 1F;
    }

    @Override
    public Vector3f getOBBTranslation() {
        return new Vector3f(0, 10.5F/16F, 0);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDefaultAnimMap() {
        EnumMap<EntityPart, AnimationData> map = getGenericAnimMap();
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, 0F/16F, -7F/16F), new Vector3f(0, 0, 0), new Vector3f(0, 13.5F, 0)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(0, 0, 0), new Vector3f(0, 0F, -7F)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0)));
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
                        new OBB(8.5F/16F, 7.5F/16F, 5F/16F, 0, 0F/16F, 2.5F/16F),
                        new OBB(4.5F/16F, 5.5F/16F, 5.5F/16F, 0, -1F/16F, 5/16F + 2.5F/16F)
                }
        };
    }

    @Override
    public AABB getEnclosingAABB() {
        AABB bb = getBoundingBox();
        return new AABB(bb.minX - 0.7, bb.minY - 0.2, bb.minZ - 0.7, bb.maxX + 0.7, bb.maxY + 0.6, bb.maxZ + 0.7);
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return boxIndex >= 0 ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
    }
}
