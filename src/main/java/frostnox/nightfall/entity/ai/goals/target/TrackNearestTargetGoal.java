package frostnox.nightfall.entity.ai.goals.target;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Supports use of AudioSensing to locate targets and updates the last seen position of the target if it is lost.
 */
public class TrackNearestTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    protected final ActionableEntity actionableMob;
    protected final Predicate<LivingEntity> selector;
    protected int lostTargetTicks;

    public TrackNearestTargetGoal(ActionableEntity pMob, Class<T> pTargetType, boolean pMustSee) {
        this(pMob, pTargetType, 10, pMustSee, false, (Predicate<LivingEntity>)null);
    }

    public TrackNearestTargetGoal(ActionableEntity mob, Class<T> targetType, boolean mustSee, Predicate<LivingEntity> targetPredicate) {
        this(mob, targetType, 10, mustSee, false, targetPredicate);
    }

    public TrackNearestTargetGoal(ActionableEntity pMob, Class<T> pTargetType, boolean pMustSee, boolean pMustReach) {
        this(pMob, pTargetType, 10, pMustSee, pMustReach, (Predicate<LivingEntity>)null);
    }

    public TrackNearestTargetGoal(ActionableEntity pMob, Class<T> pTargetType, int pRandomInterval, boolean pMustSee, boolean pMustReach, @Nullable Predicate<LivingEntity> pTargetPredicate) {
        super(pMob, pTargetType, pRandomInterval, pMustSee, pMustReach, pTargetPredicate);
        this.actionableMob = pMob;
        this.selector = pTargetPredicate;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        if(target == null) target = targetMob;
        if(target == null) return false;
        else if(!mob.canAttack(target)) return false;
        else {
            Team team = mob.getTeam();
            Team team1 = target.getTeam();
            if(team != null && team1 == team) return false;
            else {
                double dist = getFollowDistance();
                if(mob.distanceToSqr(target) > dist * dist) return false;
                else {
                    if(mustSee && !mob.getSensing().hasLineOfSight(target) && !actionableMob.getAudioSensing().hasHeard(target)) {
                        lostTargetTicks++;
                        if(lostTargetTicks >= 20) return false;
                    }
                    else lostTargetTicks = 0;
                    mob.setTarget(target);
                    return true;
                }
            }
        }
    }

    @Override
    public boolean canUse() {
        double distSqrMin = Double.MAX_VALUE;
        target = null;
        for(Entity entity : actionableMob.getAudioSensing().getHeardEntities()) {
            if(!(entity instanceof LivingEntity livingEntity)) continue;
            double distSqr = mob.distanceToSqr(entity);
            if(distSqr < distSqrMin) {
                distSqrMin = distSqr;
                target = livingEntity;
            }
        }
        if(target != null) return true;
        return super.canUse();
    }

    @Override
    protected void findTarget() {
        targetConditions = TargetingConditions.forCombat().range(getFollowDistance()).selector(selector);
        super.findTarget();
    }

    @Override
    public void start() {
        lostTargetTicks = 0;
        super.start();
    }

    @Override
    public void stop() {
        if(target.isAlive()) actionableMob.lastTargetPos = target.blockPosition();
        super.stop();
    }
}