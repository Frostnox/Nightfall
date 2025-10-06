package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.Poise;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.IHomeEntity;
import frostnox.nightfall.entity.ai.goal.*;
import frostnox.nightfall.entity.ai.goal.target.TrackNearestTargetGoal;
import frostnox.nightfall.entity.entity.Diet;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.EffectsNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.data.Wrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class SkaraSwarmEntity extends HungryMonsterEntity implements IHomeEntity {
    protected BlockPos homePos = null;

    public SkaraSwarmEntity(EntityType<? extends HungryMonsterEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.MOVEMENT_SPEED, 0.285F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 15)
                .add(AttributesNF.HEARING_RANGE.get(), 10)
                .add(AttributesNF.POISE.get(), Poise.MAXIMUM.ordinal())
                .add(AttributesNF.SLASHING_DEFENSE.get(), 0.25)
                .add(AttributesNF.PIERCING_DEFENSE.get(), 0.25);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new PursueTargetGoal(this, 1) {
            @Override
            public void tick() {
                super.tick();
                LivingEntity target = this.mob.getTarget();
                if(target != null && ActionTracker.isPresent(mob) && mob.getBoundingBox().intersects(target.getBoundingBox()) && !target.hasEffect(EffectsNF.INFESTED.get())) {
                    target.hurt(DamageTypeSource.createEntitySource(mob, DamageType.ABSOLUTE), 0); //Hurt so target can retaliate
                    MobEffectInstance effect = new MobEffectInstance(EffectsNF.INFESTED.get(), 20 * 15);
                    target.addEffect(effect, mob);
                    ((ServerLevel) target.level).getChunkSource().broadcast(target, new ClientboundUpdateMobEffectPacket(target.getId(), effect));
                    mob.setTarget(null);
                    mob.lastTargetPos = null;
                    mob.setAggressive(false);
                }
            }

            @Override
            public boolean canContinueToUse() {
                if(super.canContinueToUse()) return mob.getTarget() != null && !mob.getTarget().hasEffect(EffectsNF.INFESTED.get());
                else return false;
            }

            @Override
            protected int getAccuracy() {
                return 0;
            }
        });
        goalSelector.addGoal(3, new MoveToNestGoal(this, 1D, 1D));
        goalSelector.addGoal(4, new FleeEntityGoal<>(this, LivingEntity.class, 0.8D, 1D, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else return entity.hasEffect(EffectsNF.INFESTED.get()) || !entity.getType().is(TagsNF.SKARA_SWARM_PREY);
        }));
        goalSelector.addGoal(5, new FleeDamageGoal(this, 1));
        goalSelector.addGoal(6, new EatEntityGoal(this, 1D, 10, 2));
        goalSelector.addGoal(7, new WanderLandGoal(this, 0.8));
        targetSelector.addGoal(1, new TrackNearestTargetGoal<>(this, LivingEntity.class, true, (entity) -> {
            if(entity.isDeadOrDying() || entity.hasEffect(EffectsNF.INFESTED.get())) return false;
            else if(entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
            else return entity.getType().is(TagsNF.SKARA_SWARM_PREY);
        }));
    }

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide && isAlive()) {
            float chance = Math.max(0.35F, getHealth() / getMaxHealth());
            if(chance == 1 || random.nextFloat() < chance) {
                level.addParticle(ParticleTypesNF.SKARA.get(), getX() + (random.nextFloat() - 0.5F), getY(), getZ() + (random.nextFloat() - 0.5F),
                        MathUtil.toRadians(getYRot()), (xo != getX() || zo != getZ()) ? 1 : 0, getId());
            }
            if(randTickCount % 37 == 0) {
                ClientEngine.get().playEntitySound(this, SoundsNF.SKARA_SWARM_AMBIENT.get(), SoundSource.HOSTILE, 0.35F, 0.95F + random.nextFloat() * 0.1F);
            }
        }
    }

    @Override
    protected int getMaxSatiety() {
        return 20 * 3;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if(homePos != null) tag.put("homePos", NbtUtils.writeBlockPos(homePos));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if(tag.contains("homePos")) homePos = NbtUtils.readBlockPos(tag.getCompound("homePos"));
    }

    @Override
    public float getPushResistance() {
        return PUSH_ZERO;
    }

    @Override
    public float getPushForce() {
        return PUSH_ZERO;
    }

    @Override
    public float getNavigatorWaypointDist() {
        return 4F/16F;
    }

    @Override
    protected void pushEntities() {

    }

    @Override
    protected void doPush(Entity pEntity) {

    }

    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return super.calculateFallDamage(pFallDistance, pDamageMultiplier) - 80;
    }

    @Override
    public void push(Entity pusher) {
        if(!isPassengerOfSameVehicle(pusher) && !pusher.noPhysics && !noPhysics && hurtTime <= 0 && pusher.fallDistance > 0 && pusher instanceof LivingEntity entity) {
            hurt(DamageTypeSource.createEntitySource(entity, "stomp", DamageType.ABSOLUTE), pusher.fallDistance * 2);
        }
    }

    @Override
    public boolean dropLootFromSkinning() {
        return false;
    }

    @Override
    public float getVisionAngle() {
        return 180F;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target.canBeSeenAsEnemy() && !(target instanceof SkaraSwarmEntity);
    }

    @Override
    public boolean canTargetFromSound(LivingEntity target) {
        return !target.hasEffect(EffectsNF.INFESTED.get()) && (target.getType().is(TagsNF.SKARA_SWARM_PREY) || target instanceof Player);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance pEffectInstance) {
        MobEffect effect = pEffectInstance.getEffect();
        if(effect == EffectsNF.BLEEDING.get() || effect == EffectsNF.POISON.get()) return false;
        else return super.canBeAffected(pEffectInstance);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {

    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.SKARA_SWARM_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.SKARA_SWARM_DEATH.get();
    }

    @Override
    public ParticleOptions getHurtParticle() {
        return null;
    }

    @Override
    public void onKillRemoval() {

    }

    @Override
    public boolean isImmuneTo(DamageTypeSource source, float damage) {
        if(source.isExplosion() || source.isFromBlock()) return false;
        else if(source.getEntity() instanceof Player || source.getEntity() instanceof UndeadEntity) {
            return source.isType(DamageType.STRIKING) || source.isType(DamageType.PIERCING) || source.isType(DamageType.SLASHING);
        }
        else return false;
    }

    @Override
    public float modifyIncomingDamage(DamageTypeSource source, float damage, Wrapper<Poise> poise) {
        //Different from defense as this will reduce all damage from a multi-type source
        if(source.isType(DamageType.STRIKING) || source.isType(DamageType.SLASHING) || source.isType(DamageType.PIERCING)) return damage / 4;
        else return damage;
    }

    @Override
    public @Nullable BlockPos getHomePos() {
        return homePos;
    }

    @Override
    public void setHomePos(@Nullable BlockPos pos) {
        homePos = pos;
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return EquipmentSlot.CHEST;
    }

    @Override
    public Diet getDiet() {
        return Diet.CARNIVORE;
    }

    @Override
    public boolean canEat(BlockState state) {
        return false;
    }

    @Override
    public boolean canEat(Entity entity) {
        if(entity instanceof LivingEntity livingEntity) return livingEntity.deathTime > 20 && entity.getType().is(TagsNF.EDIBLE_CORPSE);
        else return false;
    }

    @Override
    public SoundEvent getEatSound() {
        return null;
    }
}
