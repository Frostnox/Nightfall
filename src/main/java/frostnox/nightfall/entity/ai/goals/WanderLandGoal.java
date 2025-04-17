package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

public class WanderLandGoal extends WaterAvoidingRandomStrollGoal {
    protected final ActionableEntity entity;

    public WanderLandGoal(ActionableEntity pMob, double pSpeedModifier) {
        super(pMob, pSpeedModifier);
        entity = pMob;
    }

    public WanderLandGoal(ActionableEntity pMob, double pSpeedModifier, float pProbability) {
        super(pMob, pSpeedModifier, pProbability);
        entity = pMob;
    }

    @Override
    public boolean canUse() {
        if(entity.reducedAI) return false;
        else return super.canUse();
    }
}
