package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.entity.ActionableEntity;

public class ReducedWanderGoal extends WanderGoal {
    protected final int randMod;

    public ReducedWanderGoal(ActionableEntity pMob, double pSpeedModifier, int randMod) {
        super(pMob, pSpeedModifier);
        this.randMod = randMod;
    }

    public ReducedWanderGoal(ActionableEntity pMob, double pSpeedModifier, int timeoutTicks, int randMod) {
        super(pMob, pSpeedModifier, timeoutTicks);
        this.randMod = randMod;
    }

    @Override
    public boolean canUse() {
        if(entity.level.random.nextInt(randMod) == 0) return super.canUse();
        else return false;
    }
}
