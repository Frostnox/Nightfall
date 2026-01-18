package frostnox.nightfall.world.generation.tree;

import frostnox.nightfall.util.data.WrappedInt;
import frostnox.nightfall.util.math.OctalDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Random;

public class WillowTreeGenerator extends SplitTreeGenerator {
    protected static final int MAX_DROOP = 5;

    public WillowTreeGenerator(int baseHeight, int randHeight, int baseBranchLength, int randBranchLength, int leavesRadius, int minSplits, int maxSplits, float curveChance, boolean curveRoot) {
        super(baseHeight, randHeight, baseBranchLength, randBranchLength, leavesRadius, minSplits, maxSplits, curveChance, curveRoot);
    }

    @Override
    protected void setupData(Data d, Random random) {
        d.longData = new long[] {random.nextLong()};
    }

    @Override
    protected int getMaxDistXZ() {
        return super.getMaxDistXZ() + 1;
    }

    @Override
    protected int getTrunkLeavesRadius(int y, int height, int maxHeight, int cutoff) {
        return (height < baseHeight || maxHeight <= averageHeight) ? Math.max(1, maxLeavesRadius - 1) : maxLeavesRadius;
    }

    @Override
    protected int getTrunkLeavesCutoff(int height) {
        return Math.max(1, height * 3 / 4);
    }

    @Override
    protected boolean checkBranchLeaves(Data d, BlockPos branchPos, BlockPos pos, float radiusSqr, WrappedInt shortestPlaced, int minShortestPlacedSqr, OctalDirection backDir, OctalDirection originDir, int dist) {
        if(backDir == OctalDirection.UP && dist <= Math.floorMod(hash(d.longData[0], pos.getX(), 0, pos.getZ()), MAX_DROOP)) return true;
        else return super.checkBranchLeaves(d, branchPos, pos, radiusSqr, shortestPlaced, minShortestPlacedSqr, backDir, originDir, dist);
    }

    @Override
    protected void tickBranchLeaves(Data d, BlockPos branchPos, BlockPos pos, float radiusSqr, WrappedInt shortestPlaced, int minShortestPlacedSqr, OctalDirection backDir, OctalDirection originDir, int dist) {
        super.tickBranchLeaves(d, branchPos, pos, radiusSqr, shortestPlaced, minShortestPlacedSqr, backDir, originDir, dist);
        if(backDir == OctalDirection.UP && dist <= MAX_DROOP && d.branchLeaves.contains(pos)) {
            if(!d.trunkWood.get(0).contains(pos)) tickBranchLeaves(d, branchPos, pos.below(), radiusSqr, shortestPlaced, minShortestPlacedSqr, OctalDirection.UP, OctalDirection.UP, dist + 1);
        }
    }

    @Override
    protected Direction selectBranchDirection(Data d, Random random, BlockPos pos, int length, Direction startDirection, Direction lastDirection) {
        if(length != 2) {
            if(lastDirection == startDirection || random.nextBoolean()) {
                Direction dir = random.nextBoolean() ? lastDirection.getClockWise() : lastDirection.getCounterClockWise();
                Direction rootDir = Direction.getNearest(d.trunkPos.getX() - pos.getX(), 0, d.trunkPos.getZ() - pos.getZ());
                if(dir == startDirection.getOpposite() || rootDir.getAxis() == dir.getAxis()) return startDirection;
                else return dir;
            }
            else return startDirection;
        }
        else return lastDirection;
    }

    @Override
    protected int getMinBranchHeight(int maxHeight, Random random) {
        return baseHeight / 2 + 2 + random.nextInt(2);
    }
}
