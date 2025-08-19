package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.IHungerEntity;
import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;

public class EatBlockGoal extends MoveToBlockGoal {
    private final IHungerEntity hungerEntity;

    public EatBlockGoal(IHungerEntity hungerEntity, double pSpeedModifier, int pSearchRange) {
        this(hungerEntity, pSpeedModifier, pSearchRange, 1);
    }

    public EatBlockGoal(IHungerEntity hungerEntity, double pSpeedModifier, int pSearchRange, int pVerticalSearchRange) {
        super(hungerEntity.getEntity(), pSpeedModifier, pSearchRange, pVerticalSearchRange);
        this.hungerEntity = hungerEntity;
    }

    @Override
    public boolean canUse() {
        if(!hungerEntity.isHungry()) return false;
        else return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return hungerEntity.isHungry() && super.canContinueToUse();
    }

    @Override
    public void tick() {
        super.tick();
        mob.getLookControl().setLookAt(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D, 10.0F, mob.getMaxHeadXRot());
        if(isReachedTarget()) {
            hungerEntity.eatBlock(mob.level.getBlockState(blockPos), blockPos);
            nextStartTick = 10;
        }
    }

    @Override
    protected BlockPos getMoveToTarget() {
        return blockPos;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        if(hungerEntity.canEat(level.getBlockState(pos))) {
            ReversePath path = hungerEntity.getEntity().getNavigator().findPath(pos, 0);
            return path != null && path.reachesGoal();
        }
        else return false;
    }
}
