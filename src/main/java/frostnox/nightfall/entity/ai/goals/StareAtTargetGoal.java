package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class StareAtTargetGoal extends Goal {
    protected final ActionableEntity mob;

    public StareAtTargetGoal(ActionableEntity entity, boolean lockMovement) {
        this.mob = entity;
        setFlags(lockMovement ? EnumSet.of(Flag.MOVE, Flag.LOOK) : EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if(target != null) mob.getLookControl().setLookAt(target, mob.getMaxYRotPerTick(), mob.getMaxXRotPerTick());
    }
}
