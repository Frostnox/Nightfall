package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;
import java.util.function.Predicate;

public class StepUpFleeEntityGoal<T extends LivingEntity> extends LandFleeEntityGoal<T> {
    private static final AttributeModifier FLEE_STEP_MODIFIER = new AttributeModifier(UUID.fromString("ea9f0510-9330-412a-9626-8e179c9ca2fc"),
            "Flee step height bonus", 0.4D, AttributeModifier.Operation.ADDITION);

    public StepUpFleeEntityGoal(ActionableEntity mob, Class<T> fleeClass, double walkSpeedModifier, double sprintSpeedModifier) {
        super(mob, fleeClass, walkSpeedModifier, sprintSpeedModifier);
    }

    public StepUpFleeEntityGoal(ActionableEntity mob, Class<T> fleeClass, double walkSpeedModifier, double sprintSpeedModifier, Predicate<LivingEntity> fleePredicate) {
        super(mob, fleeClass, walkSpeedModifier, sprintSpeedModifier, fleePredicate);
    }

    @Override
    protected void onFindPath() {
        AttributeInstance stepHeight = mob.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        stepHeight.removeModifier(FLEE_STEP_MODIFIER);
        stepHeight.addTransientModifier(FLEE_STEP_MODIFIER);
    }

    @Override
    public void stop() {
        super.stop();
        mob.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()).removeModifier(FLEE_STEP_MODIFIER);
    }
}
