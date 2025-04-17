package frostnox.nightfall.entity.ai.goals;

import com.mojang.authlib.GameProfile;
import frostnox.nightfall.capability.GlobalChunkData;
import frostnox.nightfall.capability.IGlobalChunkData;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.ai.pathfinding.Node;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.world.DigBlockToClient;
import frostnox.nightfall.registry.ActionsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

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
        entity.lastInteractPos = blockPos;

        LevelChunk chunk = entity.level.getChunkAt(blockPos);
        IGlobalChunkData chunkData = GlobalChunkData.get(chunk);
        //Formula for one tick of block break progress from player * multiplier
        float progress = chunkData.getBreakProgress(blockPos) + entity.getItemBySlot(EquipmentSlot.MAINHAND).getDestroySpeed(entity.level.getBlockState(blockPos))
                / entity.level.getBlockState(blockPos).getDestroySpeed(entity.level, blockPos) / 30 * 4;
        if(progress >= 1) {
            FakePlayer player = FakePlayerFactory.get((ServerLevel) entity.level, new GameProfile(UUID.fromString("f8e91fce-7ddd-47d3-a0fe-d4992193510f"), "fakeMiningPlayer"));
            entity.level.getBlockState(blockPos).getBlock().playerDestroy(entity.level, player, blockPos, entity.level.getBlockState(blockPos), entity.level.getBlockEntity(blockPos), entity.getItemBySlot(EquipmentSlot.MAINHAND));
            entity.level.getBlockState(blockPos).onDestroyedByPlayer(entity.level, blockPos, player, true, entity.level.getFluidState(blockPos));
            chunkData.removeBreakProgress(blockPos);
            NetworkHandler.toAllTrackingChunk(entity.level.getChunkAt(blockPos), new DigBlockToClient(blockPos.getX(), blockPos.getY(), blockPos.getZ(), -1));
            entity.lastInteractPos = null;
            entity.refreshPath = true;
            blockPos = null;
        }
        else {
            chunkData.setBreakProgress(blockPos, progress);
            NetworkHandler.toAllTrackingChunk(entity.level.getChunkAt(blockPos), new DigBlockToClient(blockPos.getX(), blockPos.getY(), blockPos.getZ(), progress));
        }
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
