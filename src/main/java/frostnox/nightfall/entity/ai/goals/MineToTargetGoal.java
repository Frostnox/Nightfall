package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.ai.pathfinding.Node;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.registry.ActionsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Mines towards target when not moving. This should be paired with a follow goal.
 * Assumes an entity width < 1 and 1 < height < 2.
 */
public class MineToTargetGoal extends Goal {
    private final ActionableEntity entity;
    private BlockPos blockPos = null;

    public MineToTargetGoal(ActionableEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean canUse() {
        if(!entity.isInterruptible() || !entity.canMineAnything()) return false;
        else return lookForBlocks();
    }

    @Override
    public boolean canContinueToUse() {
        return entity.canMineAnything() && (entity.getActionTracker().getActionID().equals(ActionsNF.HUSK_MINE.getId()) || lookForBlocks());
    }

    @Override
    public void stop() {
        blockPos = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if(blockPos == null || !entity.getActionTracker().isDamaging()) return;
        if(entity.getActionTracker().getFrame() != entity.getActionTracker().getDuration() / 2) return;

        lookForBlocks();
        if(blockPos.distToCenterSqr(entity.getEyePosition()) > entity.getReachSqr() * 1.25) {
            blockPos = null;
            return;
        }

        if(entity.mineBlock(entity.level, blockPos)) blockPos = null;
    }

    protected boolean lookForBlocks() {
        Node node = entity.getNavigator().getCurrentNode();
        if(node != null) {
            if(node.mineable) {
                Node nextNode = entity.getNavigator().getNextNode();
                BlockPos nextPos = nextNode != null ? nextNode.getBlockPos() : entity.getTargetPos();
                if(nextPos != null) {
                    BlockPos.MutableBlockPos blockedPos = node.getBlockPos().mutable();
                    if(nextPos.getY() > blockedPos.getY()) {
                        BlockPos aboveEntityPos = entity.blockPosition().above(2);
                        if(entity.canMineBlock(aboveEntityPos)) return setBlockPos(aboveEntityPos);
                        else if(entity.canMineBlock(blockedPos.move(Direction.UP, 2))) return setBlockPos(blockedPos);
                        else if(entity.canMineBlock(blockedPos.move(Direction.DOWN))) return setBlockPos(blockedPos);
                        else if(entity.canMineBlock(blockedPos.move(Direction.DOWN))) return setBlockPos(blockedPos);
                    }
                    else {
                        if(entity.canMineBlock(blockedPos.move(Direction.UP))) return setBlockPos(blockedPos);
                        else if(entity.canMineBlock(blockedPos.move(Direction.DOWN))) return setBlockPos(blockedPos);
                    }
                }
            }
        }
        else {
            BlockPos targetPos = entity.getTargetPos();
            if(targetPos != null) { //Dig below target if path ended with no way up, but can still reach upwards
                if(targetPos.getY() >= Mth.ceil(entity.getEyeY())) {
                    if(entity.canMineBlock(targetPos)) return setBlockPos(targetPos);
                    targetPos = targetPos.below();
                    if(entity.canMineBlock(targetPos)) return setBlockPos(targetPos);
                }
            }
        }
        return false;
    }

    private boolean setBlockPos(BlockPos pos) {
        blockPos = pos.immutable();
        doMineAction();
        return true;
    }

    protected void doMineAction() {
        entity.startAction(ActionsNF.HUSK_MINE.getId());
    }
}
