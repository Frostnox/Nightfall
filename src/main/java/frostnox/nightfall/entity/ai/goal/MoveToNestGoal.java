package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.block.block.nest.NestBlockEntity;
import frostnox.nightfall.entity.IHomeEntity;
import net.minecraft.core.BlockPos;

public class MoveToNestGoal extends MoveToHomeGoal {
    protected final IHomeEntity homeMob;
    public MoveToNestGoal(IHomeEntity mob, double speedModifier, double requiredDist) {
        super(mob, speedModifier, requiredDist);
        this.homeMob = mob;
    }

    @Override
    protected void onReachGoal() {
        if(!mob.isRemoved()) {
            if(mob.level.getBlockEntity(new BlockPos(pos)) instanceof NestBlockEntity nest) {
                nest.addEntity(mob);
            }
            else homeMob.setHomePos(null);
        }
    }
}
