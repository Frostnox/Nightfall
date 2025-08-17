package frostnox.nightfall.entity.ai.goals;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;

public class RandomLookGoal extends RandomLookAroundGoal {
    protected final float chance;
    protected final Mob mob;

    public RandomLookGoal(Mob pMob, float chance) {
        super(pMob);
        this.chance = chance;
        this.mob = pMob;
    }

    @Override
    public boolean canUse() {
        return mob.getRandom().nextFloat() < chance;
    }
}
