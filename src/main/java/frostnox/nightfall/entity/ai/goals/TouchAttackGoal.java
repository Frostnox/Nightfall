package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.LivingEntity;

public class TouchAttackGoal extends PursueTargetGoal {
    public TouchAttackGoal(ActionableEntity entity, double speedIn) {
        super(entity, speedIn);
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = this.mob.getTarget();
        if(target != null && ActionTracker.isPresent(mob) && mob.getBoundingBox().intersects(target.getBoundingBox())) {

        }
    }
}
