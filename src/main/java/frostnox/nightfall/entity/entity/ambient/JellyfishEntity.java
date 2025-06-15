package frostnox.nightfall.entity.entity.ambient;

import com.mojang.math.Vector3d;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.registry.forge.EffectsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class JellyfishEntity extends AquaticAmbientEntity {
    public enum Type {
        MOON(false), AMBER(false), ROSE(true), SCARLET(true);

        public final boolean paralyzing;

        Type(boolean paralyzing) {
            this.paralyzing = paralyzing;
        }
    }
    public static final int PROPULSION_DURATION = 24;
    protected static final EntityDataAccessor<Type> TYPE = SynchedEntityData.defineId(JellyfishEntity.class, DataSerializersNF.JELLYFISH_TYPE);
    protected static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(JellyfishEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> DEFLATING = SynchedEntityData.defineId(JellyfishEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Integer> PROPULSION_TICKS = SynchedEntityData.defineId(JellyfishEntity.class, EntityDataSerializers.INT);
    protected boolean wasInWaterOrBubble = true;
    protected int driftSeed;
    protected float driftX, driftZ;
    protected int lightSensitivity;
    protected int stingCooldown;

    public JellyfishEntity(EntityType<? extends ActionableEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 1D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 4)
                .add(Attributes.FOLLOW_RANGE, 0)
                .add(AttributesNF.HEARING_RANGE.get(), 0)
                .add(AttributesNF.FIRE_ABSORPTION.get(), 0.5)
                .add(AttributesNF.ELECTRIC_ABSORPTION.get(), -0.5);
    }

    public Type getJellyfishType() {
        return getEntityData().get(TYPE);
    }

    public boolean isSpecial() {
        return getEntityData().get(SPECIAL);
    }

    public boolean isDeflating() {
        return getEntityData().get(DEFLATING);
    }

    protected void setDeflating(boolean deflating) {
        getEntityData().set(DEFLATING, deflating);
    }

    public int getPropulsionTicks() {
        return getEntityData().get(PROPULSION_TICKS);
    }

    protected void setPropulsionTicks(int ticks) {
        getEntityData().set(PROPULSION_TICKS, ticks);
    }

    protected void updateDriftSeed(int seed) {
        driftSeed = seed;
        XoroshiroRandomSource random = new XoroshiroRandomSource(driftSeed);
        float angle = random.nextFloat() * 2 * MathUtil.PI;
        float speed = 0.0005F + random.nextFloat() * 0.00025F;
        driftX = Mth.cos(angle) * speed;
        driftZ = Mth.sin(angle) * speed;
    }

    protected boolean shouldPropel() {
        if(tickCount % 8 == 0 && random.nextBoolean()) {
            //Sink at day to low sky light
            if(LevelUtil.isDayTimeWithin(level, LevelUtil.SUNRISE_TIME, LevelUtil.NIGHT_TIME)) {
                return level.getBrightness(LightLayer.SKY, eyeBlockPosition()) <= lightSensitivity
                        && level.getFluidState(eyeBlockPosition().above()).is(FluidTags.WATER);
            }
            //Rise at night to high sky light
            else return level.getBrightness(LightLayer.SKY, eyeBlockPosition()) < 15 - lightSensitivity
                    && level.getFluidState(eyeBlockPosition().above()).is(FluidTags.WATER);
        }
        else return false;
    }

    @Override
    public void tick() {
        super.tick();
        if(isInWaterOrBubble() != wasInWaterOrBubble || tickCount == 1) refreshDimensions();
        wasInWaterOrBubble = isInWaterOrBubble();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(!level.isClientSide) {
            if(stingCooldown > 0) stingCooldown--;
            int propulsionTicks = getPropulsionTicks();
            if(propulsionTicks >= 0) {
                setPropulsionTicks(propulsionTicks - 1);
                if(propulsionTicks == 0 && !isDeflating()) {
                    Vec3 velocity = getDeltaMovement();
                    setDeltaMovement(velocity.x, velocity.y + 0.15, velocity.z);
                    hasImpulse = true;
                    setDeflating(true);
                    setPropulsionTicks(PROPULSION_DURATION);
                }
            }
            else if(shouldPropel()) {
                setPropulsionTicks(PROPULSION_DURATION);
                setDeflating(false);
            }
        }
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        if(isEffectiveAi() && isInWater()) {
            moveRelative(0.1F, pTravelVector);
            setDeltaMovement(getDeltaMovement().scale(0.9D));
            setDeltaMovement(getDeltaMovement().add(driftX, -0.0025D, driftZ));
            move(MoverType.SELF, getDeltaMovement());
        }
        else super.travel(pTravelVector);
    }

    @Override
    protected void pushEntities() {
        if(level.isClientSide) return;
        List<Entity> list = this.level.getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this));
        if(!list.isEmpty()) {
            int i = this.level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
                int j = 0;
                for(int k = 0; k < list.size(); ++k) {
                    if (!list.get(k).isPassenger()) {
                        ++j;
                    }
                }
                if (j > i - 1) {
                    this.hurt(DamageSource.CRAMMING, 6.0F);
                }
            }

            DamageTypeSource damageSource = DamageTypeSource.createEntitySource(this, "sting", DamageType.ABSOLUTE).setSound(SoundsNF.JELLYFISH_STING);
            if(getJellyfishType().paralyzing) damageSource.setEffects(new AttackEffect(EffectsNF.PARALYSIS, 60 * 20, 0, 1));
            boolean stung = false;
            for(Entity entity : list) {
                if(entity instanceof AmbientEntity) doPush(entity);
                if(stingCooldown <= 0 && !entity.getType().is(TagsNF.JELLYFISH_IMMUNE)) {
                    stung = true;
                    entity.hurt(damageSource, 5);
                }
            }
            if(stung) stingCooldown = 10;
        }
    }

    @Override
    public ParticleOptions getHurtParticle() {
        return null;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height * 0.95F;
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        if(!isInWaterOrBubble()) return new EntityDimensions(getType().getWidth(), 5.1F/16F, false);
        else if(getJellyfishType() == Type.MOON) return new EntityDimensions(getType().getWidth(), 7.1F/16F, false);
        else return super.getDimensions(pPose);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.JELLYFISH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.JELLYFISH_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {

    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, Type.MOON);
        entityData.define(SPECIAL, false);
        entityData.define(DEFLATING, false);
        entityData.define(PROPULSION_TICKS, -1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        int type = getJellyfishType().ordinal();
        if(type != 0) tag.putInt("type", type);
        boolean special = isSpecial();
        if(special) tag.putBoolean("special", special);
        tag.putBoolean("deflating", isDeflating());
        if(getPropulsionTicks() > -1) tag.putInt("propulsion", getPropulsionTicks());
        tag.putInt("driftSeed", driftSeed);
        tag.putInt("sensitivity", lightSensitivity);
        tag.putInt("stingCooldown", stingCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, Type.values()[tag.getInt("type")]);
        getEntityData().set(SPECIAL, tag.getBoolean("special"));
        getEntityData().set(DEFLATING, tag.getBoolean("deflating"));
        if(tag.contains("propulsion")) getEntityData().set(PROPULSION_TICKS, tag.getInt("propulsion"));
        if(tag.contains("driftSeed")) updateDriftSeed(tag.getInt("driftSeed"));
        lightSensitivity = tag.getInt("sensitivity");
        stingCooldown = tag.getInt("stingCooldown");
    }

    public static class GroupData extends AgeableMob.AgeableMobGroupData {
        public final Type type;
        public final int driftSeed;

        public GroupData(Type type, int driftSeed) {
            super(0F);
            this.type = type;
            this.driftSeed = driftSeed;
        }

        public static GroupData create(Random random) {
            return new GroupData(Type.values()[random.nextInt(Type.values().length)], random.nextInt());
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        Type type;
        if(spawnDataIn instanceof GroupData data) type = data.type;
        else {
            spawnDataIn = GroupData.create(worldIn.getRandom());
            type = ((GroupData) spawnDataIn).type;
        }
        updateDriftSeed(((GroupData) spawnDataIn).driftSeed);
        getEntityData().set(TYPE, type);
        lightSensitivity = random.nextInt(5);
        if(random.nextInt() % 8192 == 0) getEntityData().set(SPECIAL, true);
        setYRot(random.nextInt(4) * 90);
        return spawnDataIn;
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return EquipmentSlot.CHEST;
    }
}
