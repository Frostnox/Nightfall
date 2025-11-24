package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class StepUpFleeTargetGoal extends FleeTargetGoal {
    private static final AttributeModifier FLEE_STEP_MODIFIER = new AttributeModifier(UUID.fromString("f269eb89-d078-4f00-b2ae-6f2a5ee4ddaf"),
            "Flee step height bonus", 0.4D, AttributeModifier.Operation.ADDITION);

    public StepUpFleeTargetGoal(ActionableEntity mob, double walkSpeedModifier, double sprintSpeedModifier) {
        super(mob, walkSpeedModifier, sprintSpeedModifier);
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
