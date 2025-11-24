package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class StepUpLandFleeEntityGoal<T extends LivingEntity> extends StepUpFleeEntityGoal<T> {
    public StepUpLandFleeEntityGoal(ActionableEntity mob, Class<T> fleeClass, double walkSpeedModifier, double sprintSpeedModifier) {
        super(mob, fleeClass, walkSpeedModifier, sprintSpeedModifier);
    }

    public StepUpLandFleeEntityGoal(ActionableEntity mob, Class<T> fleeClass, double walkSpeedModifier, double sprintSpeedModifier, Predicate<LivingEntity> fleePredicate) {
        super(mob, fleeClass, walkSpeedModifier, sprintSpeedModifier, fleePredicate);
    }

    @Override
    protected Vec3 getRandomPos() {
        return LandRandomPos.getPosAway(mob, 32, 8, avoidPos);
    }
}
