package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.Poise;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.ai.goals.FleeDamageGoal;
import frostnox.nightfall.entity.ai.goals.MoveToNestGoal;
import frostnox.nightfall.entity.ai.goals.TouchAttackGoal;
import frostnox.nightfall.entity.ai.goals.WanderLandGoal;
import frostnox.nightfall.entity.ai.goals.target.TrackNearestTargetGoal;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.EffectsNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.data.Wrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
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

public class SkaraSwarmEntity extends MonsterEntity {
    public SkaraSwarmEntity(EntityType<? extends MonsterEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.MOVEMENT_SPEED, 0.2675F)
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
        goalSelector.addGoal(2, new TouchAttackGoal(this, 1));
        goalSelector.addGoal(3, new MoveToNestGoal(this, 1D, 0.1D));
        goalSelector.addGoal(4, new FleeDamageGoal(this, 1));
        goalSelector.addGoal(5, new WanderLandGoal(this, 0.8));
        targetSelector.addGoal(1, new TrackNearestTargetGoal<>(this, LivingEntity.class, true, (entity) -> {
            if(entity.isDeadOrDying()) return false;
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
        }
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
    public boolean canBeAffected(MobEffectInstance pEffectInstance) {
        MobEffect effect = pEffectInstance.getEffect();
        if(effect == EffectsNF.BLEEDING.get() || effect == EffectsNF.POISON.get()) return false;
        else return super.canBeAffected(pEffectInstance);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {

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
        return source.isType(DamageType.PIERCING) || source.isType(DamageType.SLASHING);
    }

    @Override
    public float modifyIncomingDamage(DamageTypeSource source, float damage, Wrapper<Poise> poise) {
        //Different from defense as this will reduce all damage from a multi-type source
        if(source.isType(DamageType.STRIKING) || source.isType(DamageType.SLASHING) || source.isType(DamageType.PIERCING)) return damage / 4;
        else return damage;
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return EquipmentSlot.CHEST;
    }
}
