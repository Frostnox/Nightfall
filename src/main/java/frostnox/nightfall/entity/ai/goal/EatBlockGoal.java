package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.IHungerEntity;
import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;

import java.util.EnumSet;

public class EatBlockGoal extends MoveToBlockGoal {
    private final IHungerEntity hungerEntity;

    public EatBlockGoal(IHungerEntity hungerEntity, double pSpeedModifier, int pSearchRange) {
        this(hungerEntity, pSpeedModifier, pSearchRange, 1);
    }

    public EatBlockGoal(IHungerEntity hungerEntity, double pSpeedModifier, int pSearchRange, int pVerticalSearchRange) {
        super(hungerEntity.getEntity(), pSpeedModifier, pSearchRange, pVerticalSearchRange);
        this.hungerEntity = hungerEntity;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
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
    public double acceptedDistance() {
        return 1.5D;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        mob.getLookControl().setLookAt(mob.getEyePosition());
    }

    @Override
    public void tick() {
        BlockPos pos = getMoveToTarget();
        if(MathUtil.getShortestDistanceSqrPointToBox(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, mob.getBoundingBox()) < acceptedDistance()) {
            reachedTarget = true;
            tryTicks--;
            hungerEntity.eatBlock(mob.level.getBlockState(blockPos), blockPos);
            nextStartTick = 10;
        }
        else {
            reachedTarget = false;
            tryTicks++;
            if(shouldRecalculatePath()) mob.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, speedModifier);
        }
        mob.getLookControl().setLookAt(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D, 10.0F, mob.getMaxHeadXRot());
    }

    @Override
    protected BlockPos getMoveToTarget() {
        return blockPos;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        if(hungerEntity.canEat(level.getBlockState(pos))) {
            ReversePath path = hungerEntity.getEntity().getNavigator().findPath(pos, 1);
            return path != null && path.reachesGoal();
        }
        else return false;
    }
}
