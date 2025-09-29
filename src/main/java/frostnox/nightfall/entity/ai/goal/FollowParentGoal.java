package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FollowParentGoal extends PursueTargetGoal {
    public FollowParentGoal(ActionableEntity entity, double speedIn) {
        super(entity, speedIn);
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        if(mob.lastTargetPos != null) return !mob.getNavigator().isDone();
        else if(target == null) return false;
        else if(!target.isAlive()) return false;
        else if(!mob.isWithinRestriction(target.blockPosition())) return false;
        else return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if(target != null) {
            recalcTicks = Math.max(recalcTicks - 1, 0);
            if(canPursue()) {
                if(recalcTicks == 0 || mob.refreshPath) {
                    recalcTicks = 20;
                    if(!mob.getNavigator().moveTo(target, speedModifier, getAccuracy())) recalcTicks += 10;
                    if(mob.refreshPath) mob.refreshPath = false;
                }
            }
        }
        else if(mob.lastTargetPos != null) {
            if(mob.getOnPos().above().equals(mob.lastTargetPos)) {
                mob.lastTargetPos = null;
            }
            else if(lastLastPos != null && lastLastPos.equals(mob.lastTargetPos)) {
                pursueTime++;
                if(pursueTime > 600) {
                    mob.lastTargetPos = null;
                    pursueTime = 0;
                }
            }
            else if(canPursue()) {
                mob.getNavigator().moveTo(mob.lastTargetPos.getX(), mob.lastTargetPos.getY(), mob.lastTargetPos.getZ(), speedModifier, getAccuracy());
                pursueTime = 0;
            }
        }
        if(!canPursue()) mob.getNavigator().stop();
        lastLastPos = mob.lastTargetPos;
    }
}
