package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.CombatUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Similar to MeleeAttackGoal.
 * Entity paths to target as closely as possible and performs an attack chosen by its class.
 * The attack to use is decided before appropriate range is reached and kept for a short while before expiring and choosing another.
 */
public class RushAttackGoal extends PursueTargetGoal {
    private ResourceLocation attackID;
    private int heldAttackTicks = 1000;

    public RushAttackGoal(ActionableEntity entity, double speedIn) {
        super(entity, speedIn);
    }

    @Override
    public void stop() {
        super.stop();
        heldAttackTicks = 1000;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = this.mob.getTarget();
        if(target != null && ActionTracker.isPresent(mob)) {
            IActionTracker capA = mob.getActionTracker();
            double dist = mob.getEyePosition().distanceToSqr(target.getEyePosition());
            //Re-choose attack every 3 seconds
            if(heldAttackTicks >= 60 || capA.isStunned()) {
                attackID = mob.pickActionEnemy(dist, target);
                heldAttackTicks = 0;
            }
            heldAttackTicks++;
            double reach = ActionsNF.get(attackID).getMaxDistToStart(mob);
            float lookAngle = CombatUtil.getRelativeHorizontalAngle(mob.getEyePosition(), target.getEyePosition(), mob.getYHeadRot());
            if(dist <= reach * reach && mob.isInterruptible() && lookAngle >= -20F && lookAngle <= 20F && mob.hasAnyLineOfSight(target)) {
                heldAttackTicks = 1000;
                mob.startAction(attackID);
            }
        }
        else {
            heldAttackTicks = 1000;
        }
    }
}