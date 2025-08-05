package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.entity.ai.goals.FleeDamageGoal;
import frostnox.nightfall.entity.ai.goals.PursueTargetGoal;
import frostnox.nightfall.entity.ai.goals.WanderLandGoal;
import frostnox.nightfall.entity.ai.goals.target.TrackNearestTargetGoal;
import frostnox.nightfall.entity.ai.pathfinding.FlankingLandEntityNavigator;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class CreeperEntity extends MonsterEntity {
    public static final int MAX_SWELL = 20;
    public static final float EXPLOSION_RADIUS = 3.25F;
    private static final EntityDataAccessor<Boolean> SWELLING = SynchedEntityData.defineId(CreeperEntity.class, EntityDataSerializers.BOOLEAN);
    private int oldSwell, swell;

    public CreeperEntity(EntityType<? extends MonsterEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 40D)
                .add(Attributes.MOVEMENT_SPEED, 0.2675F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.STRIKING_DEFENSE.get(), 0.2)
                .add(AttributesNF.SLASHING_DEFENSE.get(), -0.2)
                .add(AttributesNF.FIRE_DEFENSE.get(), -0.25);
    }

    public boolean isSwelling() {
        return entityData.get(SWELLING);
    }

    public void setSwelling(boolean swelling) {
        entityData.set(SWELLING, swelling);
    }

    public float getSwelling(float pPartialTicks) {
        return Mth.lerp(pPartialTicks, (float) oldSwell, (float) swell) / (float) (MAX_SWELL - 2);
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        double y = hitPos.y;
        if(y > 6D/16D && y <= 18D/16D) return EquipmentSlot.CHEST;
        else if(y > 18D/16D) return EquipmentSlot.HEAD;
        else return EquipmentSlot.LEGS;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new Goal() {
            @Override
            public boolean canUse() {
                LivingEntity target = getTarget();
                return isSwelling() || target != null && distanceToSqr(target) < 3D * 3D;
            }

            @Override
            public boolean requiresUpdateEveryTick() {
                return true;
            }

            @Override
            public void tick() {
                LivingEntity target = getTarget();
                if(target == null || getActionTracker().isStunned() || distanceToSqr(target) > 3.5D * 3.5D || !hasAnyLineOfSight(target)) {
                    setSwelling(false);
                }
                else setSwelling(true);
            }
        });
        goalSelector.addGoal(3, new PursueTargetGoal(this, 1F));
        goalSelector.addGoal(4, new WanderLandGoal(this, 0.8D));
        goalSelector.addGoal(5, new FleeDamageGoal(this, 1D));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new TrackNearestTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        oldSwell = swell;
        if(isAlive()) {
            boolean swelling = isSwelling();
            if(swelling && swell == 0) {
                playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
                gameEvent(GameEvent.PRIME_FUSE);
            }
            if(swelling) swell++;
            else swell--;
            if(swell < 0) swell = 0;
            else if(swell >= MAX_SWELL) {
                swell = MAX_SWELL;
                if(!level.isClientSide) {
                    Explosion.BlockInteraction interaction = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level, this)
                            ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE;
                    dead = true;
                    level.explode(this, DamageTypeSource.createExplosionSource(this), null, getX(), getY(), getZ(),
                            EXPLOSION_RADIUS, false, interaction);
                    discard();
                }
            }
        }
        else if(swell > 0) swell--;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SWELLING, false);
    }

    @Override
    public ParticleOptions getHurtParticle() {
        return ParticleTypesNF.FRAGMENT_CREEPER.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        return true;
    }

    @Override
    public boolean canAttack(LivingEntity pTarget) {
        return pTarget instanceof Player player && !player.isCreative() && !player.isSpectator();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlankingLandEntityNavigator(this, level);
    }
}
