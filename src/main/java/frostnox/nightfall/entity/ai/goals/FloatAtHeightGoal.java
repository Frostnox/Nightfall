package frostnox.nightfall.entity.ai.goals;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;

public class FloatAtHeightGoal extends FloatGoal {
    protected final Mob mob;
    protected final double floatHeight;

    public FloatAtHeightGoal(Mob pMob, double floatHeight) {
        super(pMob);
        this.mob = pMob;
        this.floatHeight = floatHeight;
    }

    @Override
    public boolean canUse() {
        return mob.isInWater() && (mob.getFluidHeight(FluidTags.WATER) > floatHeight || mob.horizontalCollision) || mob.isInLava();
    }
}
