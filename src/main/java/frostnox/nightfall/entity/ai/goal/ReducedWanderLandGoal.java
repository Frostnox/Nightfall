package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class ReducedWanderLandGoal extends ReducedWanderGoal {
    public ReducedWanderLandGoal(ActionableEntity pMob, double pSpeedModifier, int randMod) {
        super(pMob, pSpeedModifier, randMod);
    }

    public ReducedWanderLandGoal(ActionableEntity pMob, double pSpeedModifier, int timeoutTicks, int randMod) {
        super(pMob, pSpeedModifier, timeoutTicks, randMod);
    }

    @Override
    protected @Nullable Vec3 getPosition() {
        if(mob.isInWaterOrBubble()) {
            Vec3 pos = LandRandomPos.getPos(mob, 15, 7);
            return pos == null ? super.getPosition() : pos;
        }
        else {
            return mob.getRandom().nextFloat() >= 0.001F ? LandRandomPos.getPos(mob, 10, 7) : super.getPosition();
        }
    }
}
