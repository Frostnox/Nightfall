package frostnox.nightfall.entity.entity.monster;

import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.item.TieredArmorMaterial;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.ContinentalWorldType;
import frostnox.nightfall.world.MoonPhase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public abstract class UndeadEntity extends MonsterEntity {
    protected static final EntityDataAccessor<Float> ESSENCE = SynchedEntityData.defineId(UndeadEntity.class, EntityDataSerializers.FLOAT);
    private static final AttributeModifier FOLLOW_RANGE_MODIFIER = new AttributeModifier(UUID.fromString("a93ac4ad-6c06-4d90-9ce0-49c997c2ecc2"),
            "Full moon follow range bonus", 40D, AttributeModifier.Operation.ADDITION);
    protected long lastLoadedDayTime;
    protected int resurrectTimer;
    protected boolean resurrecting, fresh, hasDregAlly;

    public UndeadEntity(EntityType<? extends UndeadEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public int getRandomResurrectTime() {
        return 20 * 4 + random.nextInt(20 * 3);
    }

    public boolean isResurrecting() {
        return resurrecting;
    }

    public boolean canResurrect() {
        if(level.isClientSide) return !resurrecting;
        else return !resurrecting && !isAlive() && deathTime > 20 && !isOnFire() && LevelUtil.isNight(level) && (isOnGround() || isInWater());
    }

    public void resurrect() {
        if(level.isClientSide()) {
            resurrecting = true;
            return;
        }
        resurrecting = true;
        NetworkHandler.toAllTracking(this, new GenericEntityToClient(NetworkHandler.Type.RESURRECT_UNDEAD_CLIENT, getId()));
        Vec3 move = new Vec3(-0.22 * (getSynchedRandom() % 2 == 0 ? 1 : -1), 0, 0).yRot(-MathUtil.toRadians(getYRot()));
        setDeltaMovement(getDeltaMovement().add(move));
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
        return Math.min(1F, getEssencePercentage() + 0.75F);
    }

    @Override
    public double getReducedAIThresholdSqr() {
        return 200 * 200;
    }

    @Override
    public boolean panicsOnFireDamage() {
        return false;
    }

    @Override
    public boolean dropLootFromSkinning() {
        return false;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        setEssence(getMaxEssence());
        lastLoadedDayTime = worldIn.getLevelData().getDayTime();
        fresh = true;
        return spawnDataIn;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
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
        tag.putBoolean("resurrecting", isResurrecting());
        tag.putLong("lastDayTime", lastLoadedDayTime);
        tag.putInt("resurrectTimer", resurrectTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setEssence(tag.getFloat("essence"));
        resurrecting = tag.getBoolean("resurrecting");
        lastLoadedDayTime = tag.getLong("lastDayTime");
        resurrectTimer = tag.getInt("resurrectTimer");
    }

    @Override
    public boolean canTargetFromSound(LivingEntity target) {
        return target instanceof Player player && !player.isCreative() && !player.isSpectator();
    }

    @Override
    public boolean canAttack(LivingEntity pTarget) {
        if(pTarget instanceof Player player) return !player.isCreative() && !player.isSpectator();
        return pTarget.getMobType() != MobType.UNDEAD && pTarget.canBeSeenAsEnemy();
    }

    @Override
    public boolean shouldFleeFrom(LivingEntity target) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        if(!level.isClientSide) {
            long timePassed = level.getDayTime() - lastLoadedDayTime;
            if(timePassed > 1) {
                if(timePassed > ContinentalWorldType.DAY_LENGTH) discard();
                else {
                    //Opt for speed & simplicity over precision here, prefer discarding to keeping
                    long essenceLost = LevelUtil.getDayTimePassed(level, timePassed) / (isDeadOrDying() ? 5 : 10);
                    addEssence(-essenceLost);
                    if(getEssence() <= 0F) discard();
                }
            }
            lastLoadedDayTime = level.getDayTime();
            if(MoonPhase.get(level) == MoonPhase.FULL) {
                if(tickCount == 1) {
                    AttributeInstance followRange = getAttribute(Attributes.FOLLOW_RANGE);
                    followRange.removeModifier(FOLLOW_RANGE_MODIFIER);
                    followRange.addTransientModifier(FOLLOW_RANGE_MODIFIER);
                }
            }
            else getAttribute(Attributes.FOLLOW_RANGE).removeModifier(FOLLOW_RANGE_MODIFIER);
        }
        super.tick();
        if(isAlive() && deathTime > 0) {
            deathTime--;
            if(deathTime == 0) resurrecting = false;
        }
        float essence = getEssence();
        if(level.isClientSide) {
            if(randTickCount % 5 == 0 && random.nextFloat() > getTransparency()) {
                level.addParticle(ParticleTypesNF.ESSENCE_MOON.get(), getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0, 0, 0);
            }
        }
        else if(level instanceof ServerLevel serverLevel) {
            if(fresh) {
                fresh = false;
                playSound(SoundsNF.UNDEAD_WARP.get(), 1F, 1F);
                serverLevel.sendParticles(ParticleTypesNF.ESSENCE_MOON.get(), getX(), getY(), getZ(), Math.round(14F * getBbHeight()) + level.random.nextInt(8), getBbWidth() * 0.5D, getBbHeight() * 0.5D, getBbWidth() * 0.5D, 0);
            }
            if(LevelUtil.isDay(level) || isDeadOrDying()) {
                if(essence <= 0F) remove(RemovalReason.KILLED);
                else setEssence(essence - (isDeadOrDying() ? (5F/20F) : (2F/20F)));
            }
            else if(essence < AttributesNF.getMaxEssence(this)) {
                setEssence(essence + 1F/20F * MoonPhase.get(level).fullness);
            }
        }
    }

    @Override
    protected void tickDeath() {
        if(!isResurrecting()) {
            deathTime++;
            if(!level.isClientSide) {
                if(canResurrect()) {
                    if(resurrectTimer == 0) resurrectTimer = getRandomResurrectTime();
                    resurrectTimer--;
                    if(resurrectTimer <= 0) resurrect();
                }
            }
        }
        else {
            if(deathTime > 20) deathTime = 20;
            else deathTime--;
            if(deathTime == 0) resurrecting = false;
            else if(deathTime == 6) {
                dead = false;
                if(!level.isClientSide) setHealth(getMaxHealth() / 2);
            }
            else if(deathTime == 20 && getAmbientSound() != null) {
                level.playLocalSound(getX(), getY(), getZ(), getAmbientSound(), getSoundSource(), getSoundVolume(), getVoicePitch(), false);
            }
        }
    }

    @Override
    protected void dropAllDeathLoot(DamageSource pDamageSource) {

    }

    @Override
    protected void dropEquipment() {
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = getItemBySlot(slot);
            float dropChance = getEquipmentDropChance(slot);
            if(!item.isEmpty() && random.nextFloat() < dropChance) {
                if(dropChance <= 1F && item.isDamageableItem()) {
                    item.setDamageValue((int) (item.getMaxDamage() * (1F - random.nextFloat() * 0.05F)));
                }
                spawnAtLocation(item);
                setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason pReason) {
        setRemoved(pReason);
        if(pReason == Entity.RemovalReason.KILLED && dead) {
            gameEvent(GameEvent.ENTITY_KILLED);
            DamageSource source = getLastDamageSource();
            forceDropAllDeathLoot(source == null ? DamageSource.GENERIC : source);
            for(Player player : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(getAttributeValue(Attributes.FOLLOW_RANGE)))) {
                if(player.isSpectator()) continue;
                IPlayerData capP = PlayerData.get(player);
                capP.setUndeadKilledThisNight(capP.getUndeadKilledThisNight() + 1);
            }
        }
        invalidateCaps();
    }

    @Override
    public void onClientRemoval() {
        if(getEssence() > 0F) return;
        level.playLocalSound(getX(), getY(), getZ(), SoundsNF.UNDEAD_WARP.get(), getSoundSource(), 1F, 1F, false);
        for(int i = 0; i < Math.round(14F * getBbHeight()) + level.random.nextInt(8); i++) {
            level.addParticle(ParticleTypesNF.ESSENCE_MOON.get(), getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0, 0, 0);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ActionableEntity.createAttributes().add(AttributesNF.WILLPOWER.get(), 15)
                .add(AttributesNF.FROST_DEFENSE.get(), 0.5);
    }

    protected void equipHumanoid(Random random, float headChance, float chestChance, float legsFeetChance, float metalChance, float extraChance) {
        metalChance += extraChance;
        if(random.nextFloat() < headChance + extraChance) setItemSlot(EquipmentSlot.HEAD,
                new ItemStack(ItemsNF.HELMETS.get(random.nextFloat() < metalChance ? TieredArmorMaterial.RUSTED : TieredArmorMaterial.RAGGED).get()));
        if(random.nextFloat() < chestChance + extraChance) setItemSlot(EquipmentSlot.CHEST,
                new ItemStack(ItemsNF.CHESTPLATES.get(random.nextFloat() < metalChance ? TieredArmorMaterial.RUSTED : TieredArmorMaterial.RAGGED).get()));
        if(random.nextFloat() < legsFeetChance + extraChance) {
            setItemSlot(EquipmentSlot.LEGS,
                    new ItemStack(ItemsNF.LEGGINGS.get(random.nextFloat() < metalChance ? TieredArmorMaterial.RUSTED : TieredArmorMaterial.RAGGED).get()));
            setItemSlot(EquipmentSlot.FEET,
                    new ItemStack(ItemsNF.BOOTS.get(random.nextFloat() < metalChance ? TieredArmorMaterial.RUSTED : TieredArmorMaterial.RAGGED).get()));
        }
    }
}
