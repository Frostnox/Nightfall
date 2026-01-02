package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class StepUpStrafeAttackGoal extends StrafeAttackGoal {
    private static final AttributeModifier STRAFE_STEP_MODIFIER = new AttributeModifier(UUID.fromString("97a6bbfe-4fb4-40c9-9630-573ebdd52a6d"),
            "Strafe attack step height bonus", 0.4D, AttributeModifier.Operation.ADDITION);
    
    public StepUpStrafeAttackGoal(ActionableEntity entity, double speedIn, int strafeSwitchTime, double strafeStartDist) {
        super(entity, speedIn, strafeSwitchTime, strafeStartDist);
    }

    @Override
    public void start() {
        super.start();
        AttributeInstance stepHeight = mob.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        stepHeight.removeModifier(STRAFE_STEP_MODIFIER);
        stepHeight.addTransientModifier(STRAFE_STEP_MODIFIER);
    }

    @Override
    public void stop() {
        super.stop();
        mob.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()).removeModifier(STRAFE_STEP_MODIFIER);
    }
}