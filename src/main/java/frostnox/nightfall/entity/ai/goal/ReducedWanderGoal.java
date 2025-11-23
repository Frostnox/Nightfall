package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;

public class ReducedWanderGoal extends RandomStrollGoal {
    protected final ActionableEntity entity;
    protected final int randMod;

    public ReducedWanderGoal(ActionableEntity pMob, double pSpeedModifier, int randMod) {
        super(pMob, pSpeedModifier);
        entity = pMob;
        this.randMod = randMod;
    }

    @Override
    public boolean canUse() {
        if(entity.reducedAI) return false;
        else if(entity.level.random.nextInt(randMod) == 0) return super.canUse();
        else return false;
    }
}
