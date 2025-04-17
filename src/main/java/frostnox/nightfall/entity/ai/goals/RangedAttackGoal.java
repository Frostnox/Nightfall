package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.CombatUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Similar to RangedBowAttackGoal.
 * Entity paths to target within specified range and performs an attack chosen by its class if it can see its target.
 * The attack to use is decided before appropriate range is reached and kept for a short while before expiring and choosing another.
 * If the entity cannot see its target, it will try to move closer than its specified range.
 * Once the entity is in range limit and can see its target, it stops pathing and strafes randomly.
 */
public class RangedAttackGoal extends Goal {
    protected ResourceLocation attackID;
    protected int heldAttackTicks = 1000;
    protected final ActionableEntity mob;
    protected final double speedModifier;
    protected long lastCanUseCheck;
    protected int seeTime;
    protected boolean strafingClockwise;
    protected boolean strafingBackwards;
    protected int strafingTime = -1;
    protected final float distanceLimitSqr;
    protected int cooldownTicks;
    protected int cooldownTime;

    public RangedAttackGoal(ActionableEntity entity, double speedIn, float distanceLimit) {
        this.mob = entity;
        this.speedModifier = speedIn;
        this.distanceLimitSqr = distanceLimit * distanceLimit;
        this.cooldownTime = 8 + entity.getRandom().nextInt() % 5;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long time = mob.level.getGameTime();
        if(time - lastCanUseCheck < 5L) return false;
        else {
            lastCanUseCheck = time;
            LivingEntity target = mob.getTarget();
            if(target == null) return false;
            else if(!target.isAlive()) return false;
            else return mob.canDoRangedAction();
        }
    }

    @Override
    public boolean canContinueToUse() {
        if(!mob.canDoRangedAction()) return false;
        LivingEntity target = mob.getTarget();
        if(target == null || !target.isAlive()) return false;
        else return !(target instanceof Player) || !target.isSpectator() && !((Player) target).isCreative();
    }

    @Override
    public void start() {
        super.start();
        mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        LivingEntity livingentity = mob.getTarget();
        if(!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) mob.setTarget(null);
        mob.setAggressive(false);
        mob.getNavigation().stop();
        seeTime = 0;
        Action action = mob.getActionTracker().getAction();
        if(mob.getActionTracker().getState() == action.getChargeState()) mob.queueAction();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if(target != null && ActionTracker.isPresent(mob)) {
            double distSqr = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            boolean canSee = mob.getSensing().hasLineOfSight(target);
            boolean hasSeen = seeTime > 0;
            if(canSee != hasSeen) seeTime = 0;
            if(canSee) seeTime++;
            else seeTime--;
            if(!(distSqr > distanceLimitSqr) && seeTime >= 20) {
                mob.getNavigation().stop();
                strafingTime++;
            }
            else {
                mob.getNavigation().moveTo(target, speedModifier);
                mob.getMoveControl().strafe(0, 0);
                strafingTime = -1;
            }
            if(strafingTime >= 20) {
                if(mob.getRandom().nextFloat() < 0.3F) strafingClockwise = !strafingClockwise;
                if(mob.getRandom().nextFloat() < 0.3F) strafingBackwards = !strafingBackwards;
                strafingTime = 0;
            }
            if(strafingTime > -1) {
                if(distSqr > (distanceLimitSqr * 0.75F)) strafingBackwards = false;
                else if(distSqr < (distanceLimitSqr * 0.25F)) strafingBackwards = true;
                if(!mob.isInWater()) mob.getMoveControl().strafe(strafingBackwards ? -0.5F : 0.5F, strafingClockwise ? 0.5F : -0.5F);
                mob.lookAt(target, mob.getMaxYRotPerTick(), mob.getMaxXRotPerTick());
            }
            mob.getLookControl().setLookAt(target, mob.getMaxYRotPerTick(), mob.getMaxXRotPerTick());

            IActionTracker capA = mob.getActionTracker();
            if(capA.isInactive()) cooldownTicks++;
            double dist = mob.getEyePosition().distanceToSqr(target.getEyePosition());
            //Re-choose attack every 3 seconds
            if(heldAttackTicks >= 60 || capA.isStunned()) {
                attackID = mob.pickActionEnemy(dist, target);
                heldAttackTicks = 0;
            }
            heldAttackTicks++;
            double reach = ActionsNF.get(attackID).getMaxDistToStart(mob);
            float lookAngle = CombatUtil.getRelativeHorizontalAngle(mob.getEyePosition(), target.getEyePosition(), mob.getYHeadRot());
            if(cooldownTicks >= cooldownTime && dist <= reach * reach && mob.isInterruptible() && lookAngle >= -25F && lookAngle <= 25F && canSee) {
                heldAttackTicks = 1000;
                cooldownTime = 8 + mob.getRandom().nextInt() % 5;
                cooldownTicks = 0;
                mob.startAction(attackID);
            }
        }
        else heldAttackTicks = 1000;
    }
}