package frostnox.nightfall.world.generation.tree;

import frostnox.nightfall.util.math.OctalDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class IronwoodTreeGenerator extends TreeGenerator {
    public IronwoodTreeGenerator(int baseHeight, int randHeight, int baseBranchLength, int randBranchLength, int leavesRadius) {
        super(baseHeight, randHeight, baseBranchLength, randBranchLength, leavesRadius);
    }

    @Override
    protected int getTrunkLeavesCutoff(int height) {
        return height > averageHeight - 2 ? 2 : 1;
    }

    @Override
    protected List<Direction> getBranchStartDirections(Data d, BlockPos centerPos, Random random, @Nullable List<Direction> lastDirections, @Nullable List<Direction> lastLastDirections) {
        List<Direction> directions = Lists.newArrayList(Direction.Plane.HORIZONTAL.iterator());
        Direction direction = directions.remove((random.nextInt() & Integer.MAX_VALUE) % directions.size());
        if(lastDirections != null) {
            while(lastDirections.contains(direction)) direction = directions.remove((random.nextInt() & Integer.MAX_VALUE) % directions.size());
        }
        if(lastLastDirections != null) {
            while(lastLastDirections.contains(direction)) direction = directions.remove((random.nextInt() & Integer.MAX_VALUE) % directions.size());
        }
        return List.of(direction);
    }

    @Override
    protected Direction selectBranchDirection(Random random, int length, Direction startDirection, Direction lastDirection) {
        return startDirection;
    }

    @Override
    protected OctalDirection[] getBranchLeavesDirections(Data d) {
        return OctalDirection.CARDINALS_UP;
    }

    @Override
    protected int getBranchLength(Data d, Random random) {
        int length = baseBranchLength + (randBranchLength > 0 ? ((random.nextInt() & Integer.MAX_VALUE) % randBranchLength) : 0);
        if(length == baseBranchLength + randBranchLength - 1) length -= random.nextInt(3);
        if(d.height < averageHeight / 2 && length > 2) length--;
        return length;
    }

    @Override
    protected int getMinBranchHeight(int maxHeight, Random random) {
        return (baseHeight / 2) - 1 + random.nextInt(3);
    }

    @Override
    protected int getMaxBranchHeight(int height, int minBranchHeight) {
        return Math.max(0, height - (height > averageHeight - 2 ? 3 : 2));
    }

    @Override
    protected void tickBranchLeaves(Data d, BlockPos branchPos, BlockPos stemPos, int radius, int minShortestPlacedSqr, boolean placeOriginLeaves, OctalDirection[] directions) {
        if(branchPos.distManhattan(stemPos) >= 2) super.tickBranchLeaves(d, branchPos, stemPos, radius, minShortestPlacedSqr, placeOriginLeaves, directions);
    }
}
