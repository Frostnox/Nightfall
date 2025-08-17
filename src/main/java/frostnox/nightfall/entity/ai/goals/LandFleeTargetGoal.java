package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class LandFleeTargetGoal extends FleeTargetGoal {
    public LandFleeTargetGoal(ActionableEntity mob, double walkSpeedModifier, double sprintSpeedModifier) {
        super(mob, walkSpeedModifier, sprintSpeedModifier);
    }

    @Override
    protected Vec3 getRandomPos() {
        return LandRandomPos.getPosAway(mob, 32, 8, avoidPos);
    }
}
