package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.Impact;
import frostnox.nightfall.action.Poise;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.ai.goal.RushAttackGoal;
import frostnox.nightfall.entity.ai.goal.WanderLandGoal;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.*;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.animation.AnimationData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class EctoplasmEntity extends MonsterEntity {
    protected static final EntityDataAccessor<Float> ESSENCE = SynchedEntityData.defineId(UndeadEntity.class, EntityDataSerializers.FLOAT);
    public enum Size {
        SMALL, MEDIUM, LARGE
    }
    public final Size size;
    protected final Goal attackGoal = new RushAttackGoal(this, 1);
    protected final Goal targetGoal = new HurtByTargetGoal(this);

    protected EctoplasmEntity(EntityType<? extends MonsterEntity> type, Level worldIn, Size size) {
        super(type, worldIn);
        this.size = size;
        updateGoals();
    }

    public static EctoplasmEntity createLarge(EntityType<? extends MonsterEntity> type, Level worldIn) {
        return new EctoplasmEntity(type, worldIn, Size.LARGE);
    }

    public static EctoplasmEntity createMedium(EntityType<? extends MonsterEntity> type, Level worldIn) {
        return new EctoplasmEntity(type, worldIn, Size.MEDIUM);
    }

    public static EctoplasmEntity createSmall(EntityType<? extends MonsterEntity> type, Level worldIn) {
        return new EctoplasmEntity(type, worldIn, Size.SMALL);
    }

    private static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 40D)
                .add(AttributesNF.WILLPOWER.get(), 200)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 10)
                .add(AttributesNF.HEARING_RANGE.get(), 0)
                .add(AttributesNF.STRIKING_DEFENSE.get(), 0.75)
                .add(AttributesNF.PIERCING_DEFENSE.get(), 0.75)
                .add(AttributesNF.FIRE_DEFENSE.get(), 0.25)
                .add(AttributesNF.FROST_DEFENSE.get(), 0.25)
                .add(AttributesNF.ELECTRIC_DEFENSE.get(), 0.25);
    }

    public static AttributeSupplier.Builder getLargeAttributeMap() {
        return getAttributeMap().add(Attributes.MAX_HEALTH, 40D)
                .add(AttributesNF.WILLPOWER.get(), 200)
                .add(Attributes.MOVEMENT_SPEED, 0.06F)
                .add(AttributesNF.POISE.get(), Poise.MEDIUM.ordinal())
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    public static AttributeSupplier.Builder getMediumAttributeMap() {
        return getAttributeMap().add(Attributes.MAX_HEALTH, 20D)
                .add(AttributesNF.WILLPOWER.get(), 100)
                .add(Attributes.MOVEMENT_SPEED, 0.065F)
                .add(AttributesNF.POISE.get(), Poise.LOW.ordinal())
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D);
    }

    public static AttributeSupplier.Builder getSmallAttributeMap() {
        return getAttributeMap().add(Attributes.MAX_HEALTH, 10D)
                .add(AttributesNF.WILLPOWER.get(), 50)
                .add(Attributes.MOVEMENT_SPEED, 0.07F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D);
    }

    public static EnumMap<EntityPart, AnimationData> getClubAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.HAND_RIGHT, new AnimationData(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0)));
        return map;
    }

    public float getMaxEssence() {
        return (float) AttributesNF.getMaxEssence(this);
    }

    public float getEssencePercentage() {
        return getEssence() / getMaxEssence();
    }

    public float getEssence() {
        return getEntityData().get(ESSENCE);
    }

    public void setEssence(float essence) {
        getEntityData().set(ESSENCE, Mth.clamp(essence, 0, getMaxEssence()));
    }

    public void addEssence(float essence) {
        setEssence(getEssence() + essence);
    }

    public float getTransparency() {
        return Math.min(0.5F, getEssencePercentage());
    }

    public void updateGoals() {
        if(!level.isClientSide) {
            goalSelector.removeGoal(attackGoal);
            targetSelector.removeGoal(targetGoal);
            if(size != Size.SMALL) {
                goalSelector.addGoal(2, attackGoal);
                targetSelector.addGoal(1, targetGoal);
            }
        }
    }

    protected void split() {
        if(size != Size.SMALL) {
            float splitAngle = random.nextFloat(MathUtil.PI * 2);
            float halfWidth = getBbWidth() * 0.35F;
            for(int i = 0; i < 2; i++) {
                EctoplasmEntity ectoplasm = size == Size.LARGE ? EntitiesNF.ECTOPLASM_MEDIUM.get().create(level) : EntitiesNF.ECTOPLASM_SMALL.get().create(level);
                if(isPersistenceRequired()) ectoplasm.setPersistenceRequired();
                ectoplasm.setNoAi(isNoAi());
                ectoplasm.setInvulnerable(isInvulnerable());
                ectoplasm.setEssence(getEssence() / 2);
                if(i == 0) ectoplasm.moveTo(getX() + halfWidth * Mth.cos(splitAngle), getY(), getZ() + halfWidth * Mth.sin(splitAngle), random.nextFloat() * 360.0F, 0.0F);
                else ectoplasm.moveTo(getX() + halfWidth * Mth.sin(splitAngle), getY(), getZ() + halfWidth * Mth.cos(splitAngle), random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(ectoplasm);
            }
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(3, new WanderLandGoal(this, 0.5, Integer.MAX_VALUE));
    }

    @Override
    public ResourceLocation pickActionEnemy(double distanceSqr, Entity target) {
        if(getHealth() / getMaxHealth() <= 0.5F || random.nextInt(10) == 0) {
            return size == Size.LARGE ? ActionsNF.ECTOPLASM_EXPLODE_LARGE.getId() : ActionsNF.ECTOPLASM_EXPLODE_MEDIUM.getId();
        }
        else return size == Size.LARGE ? ActionsNF.ECTOPLASM_CLUB_LARGE.getId() : ActionsNF.ECTOPLASM_CLUB_MEDIUM.getId();
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        setEssence(getMaxEssence());
        updateGoals();
        return spawnDataIn;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(!level.isClientSide && getEssence() < getMaxEssence() && random.nextFloat() < 0.01F) {
            for(ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, getBoundingBox())) {
                ItemStack item = entity.getItem();
                if(item.is(TagsNF.CRUSHABLE_TO_BONE_SHARD)) {
                    addEssence(40 * item.getCount());
                    entity.discard();
                    if(getEssence() >= getMaxEssence()) break;
                }
            }
        }
    }

    @Override
    public float getAttackYRot(float partial) {
        return Mth.rotLerp(partial, yHeadRotO, yHeadRot);
    }

    @Override
    public boolean dropLootFromSkinning() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ESSENCE, 100F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("essence", getEssence());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setEssence(tag.getFloat("essence"));
        updateGoals();
    }

    @Override
    public float getPushResistance() {
        return switch(size) {
            case LARGE -> PUSH_HIGH;
            case MEDIUM -> PUSH_MEDIUM;
            case SMALL -> PUSH_LOW;
        };
    }

    @Override
    public float getPushForce() {
        return getPushResistance();
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height / 2;
    }

    @Override
    public float getVisionAngle() {
        return 180F;
    }

    @Override
    public boolean hasLineOfSight(Entity target) {
        return true;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target.canBeSeenAsEnemy() && !(target instanceof EctoplasmEntity);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance pEffectInstance) {
        MobEffect effect = pEffectInstance.getEffect();
        if(effect == EffectsNF.BLEEDING.get() || effect == EffectsNF.POISON.get()) return false;
        else return super.canBeAffected(pEffectInstance);
    }

    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return super.calculateFallDamage(pFallDistance, pDamageMultiplier) - 40;
    }

    @Override
    public void push(double pX, double pY, double pZ) {
        switch(size) {
            case LARGE -> super.push(pX * 0.5, pY * 0.75, pZ * 0.5);
            case MEDIUM -> super.push(pX, pY, pZ);
            case SMALL -> super.push(pX * 2, pY * 1.5, pZ * 2);
        }
    }

    @Override
    protected float getSoundVolume() {
        return switch(size) {
            case LARGE -> 1;
            case MEDIUM -> 0.75F;
            case SMALL -> 0.5F;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return size == Size.SMALL ? SoundsNF.ECTOPLASM_HURT_SMALL.get() : SoundsNF.ECTOPLASM_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return size == Size.SMALL ? SoundsNF.ECTOPLASM_DEATH_SMALL.get() : SoundsNF.ECTOPLASM_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {

    }

    @Override
    public ParticleOptions getHurtParticle() {
        return ParticleTypesNF.ECTOPLASM.get();
    }

    @Override
    public Impact modifyIncomingImpact(DamageTypeSource source, Impact impact) {
        return source.isType(DamageType.SLASHING) ? impact : impact.decrease();
    }

    @Override
    public void onKillRemoval() {
        split();
    }

    @Override
    public void onClientRemoval() {
        if(deathTime > 15) {
            if(size == Size.SMALL) level.playLocalSound(getX(), getY(), getZ(), SoundsNF.ENTITY_WARP.get(), getSoundSource(), 1F, 1F, false);
            for(int i = 0; i < getBbWidth() * 32 + level.random.nextInt(8); i++) {
                level.addParticle(ParticleTypesNF.ESSENCE.get(), getRandomX(0.5), getRandomY(), getRandomZ(0.5), 0, 0, 0);
            }
        }
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return EquipmentSlot.CHEST;
    }
}
