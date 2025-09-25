package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class StepUpFleeDamageGoal extends FleeDamageGoal {
    private static final AttributeModifier FLEE_STEP_MODIFIER = new AttributeModifier(UUID.fromString("042914f3-cfcc-44ec-b5d4-01a9b97dd514"),
            "Flee step height bonus", 0.4D, AttributeModifier.Operation.ADDITION);

    public StepUpFleeDamageGoal(ActionableEntity mob, double speedModifier) {
        super(mob, speedModifier);
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
