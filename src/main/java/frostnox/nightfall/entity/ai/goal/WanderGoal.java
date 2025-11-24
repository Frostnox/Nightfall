package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;

public class WanderGoal extends RandomStrollGoal {
    protected final ActionableEntity entity;
    protected final int timeoutTicks;

    public WanderGoal(ActionableEntity pMob, double pSpeedModifier) {
        this(pMob, pSpeedModifier, 20 * 3);
    }

    public WanderGoal(ActionableEntity pMob, double pSpeedModifier, int timeoutTicks) {
        super(pMob, pSpeedModifier);
        entity = pMob;
        this.timeoutTicks = timeoutTicks;
    }

    @Override
    public boolean canUse() {
        if(entity.reducedAI) return false;
        else return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && entity.getNavigator().getAdjustedTicksOnCurrentNode() < timeoutTicks;
    }
}
