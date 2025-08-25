package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.Impact;
import frostnox.nightfall.action.Poise;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

public class EctoplasmEntity extends MonsterEntity {
    protected static final EntityDataAccessor<Float> ESSENCE = SynchedEntityData.defineId(UndeadEntity.class, EntityDataSerializers.FLOAT);
    public enum Size {
        SMALL, MEDIUM, LARGE
    }
    public final Size size;

    protected EctoplasmEntity(EntityType<? extends MonsterEntity> type, Level worldIn, Size size) {
        super(type, worldIn);
        this.size = size;
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
                .add(Attributes.MOVEMENT_SPEED, 0.12F)
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
                .add(Attributes.MOVEMENT_SPEED, 0.12F)
                .add(AttributesNF.POISE.get(), Poise.MEDIUM.ordinal())
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    public static AttributeSupplier.Builder getMediumAttributeMap() {
        return getAttributeMap().add(Attributes.MAX_HEALTH, 20D)
                .add(AttributesNF.WILLPOWER.get(), 100)
                .add(Attributes.MOVEMENT_SPEED, 0.13F)
                .add(AttributesNF.POISE.get(), Poise.LOW.ordinal())
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D);
    }

    public static AttributeSupplier.Builder getSmallAttributeMap() {
        return getAttributeMap().add(Attributes.MAX_HEALTH, 10D)
                .add(AttributesNF.WILLPOWER.get(), 50)
                .add(Attributes.MOVEMENT_SPEED, 0.14F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D);
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
        return Math.min(0.6F, getEssencePercentage());
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        setEssence(getMaxEssence());
        return spawnDataIn;
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
    }

    @Override
    public Impact modifyIncomingImpact(DamageTypeSource source, Impact impact) {
        return source.isType(DamageType.SLASHING) ? impact : impact.decrease();
    }

    @Override
    protected void onKillRemoval() {

    }

    @Override
    public void onClientRemoval() {
        if(getRemovalReason() == RemovalReason.KILLED || getRemovalReason() == RemovalReason.DISCARDED) {
            level.playLocalSound(getX(), getY(), getZ(), SoundsNF.ENTITY_WARP.get(), getSoundSource(), 1F, 1F, false);
            for (int i = 0; i < getBbWidth() * 32 + level.random.nextInt(8); i++) {
                level.addParticle(ParticleTypesNF.ESSENCE.get(), getRandomX(0.5), getRandomY(), getRandomZ(0.5), 0, 0, 0);
            }
        }
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return EquipmentSlot.CHEST;
    }
}
