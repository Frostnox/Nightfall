package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class StepUpRushAttackGoal extends RushAttackGoal {
    private static final AttributeModifier RUSH_STEP_MODIFIER = new AttributeModifier(UUID.fromString("93997f5f-6336-4aa4-92c9-a0b468c9f7c0"),
            "Rush attack step height bonus", 0.4D, AttributeModifier.Operation.ADDITION);

    public StepUpRushAttackGoal(ActionableEntity entity, double speedIn) {
        super(entity, speedIn);
    }

    @Override
    public void start() {
        super.start();
        AttributeInstance stepHeight = mob.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        stepHeight.removeModifier(RUSH_STEP_MODIFIER);
        stepHeight.addTransientModifier(RUSH_STEP_MODIFIER);
    }

    @Override
    public void stop() {
        super.stop();
        mob.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()).removeModifier(RUSH_STEP_MODIFIER);
    }
}
