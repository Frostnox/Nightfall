package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;

public class WanderGoal extends RandomStrollGoal {
    protected final ActionableEntity entity;

    public WanderGoal(ActionableEntity pMob, double pSpeedModifier) {
        super(pMob, pSpeedModifier);
        entity = pMob;
    }

    @Override
    public boolean canUse() {
        if(entity.reducedAI) return false;
        else return super.canUse();
    }
}
