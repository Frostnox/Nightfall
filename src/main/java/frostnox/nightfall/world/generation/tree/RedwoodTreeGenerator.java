package frostnox.nightfall.world.generation.tree;

import frostnox.nightfall.util.math.OctalDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class RedwoodTreeGenerator extends TreeGenerator {
    public RedwoodTreeGenerator(int baseHeight, int randHeight, int baseBranchLength, int randBranchLength, int leavesRadius) {
        super(baseHeight, randHeight, baseBranchLength, randBranchLength, leavesRadius);
    }

    @Override
    protected int getTrunkLeavesCutoff(int height) {
        return 1;
    }

    @Override
    protected OctalDirection[] getBranchLeavesDirections(Data d) {
        return OctalDirection.CARDINALS_UP;
    }

    @Override
    protected List<Direction> getBranchStartDirections(Data d, BlockPos centerPos, Random random, @Nullable List<Direction> lastDirections, @Nullable List<Direction> lastLastDirections) {
        int y = d.trunkPos.getY() + d.maxHeight - 1 - centerPos.getY();
        if(y % 3 != 0) return List.of();
        List<Direction> directions = Lists.newArrayList(Direction.Plane.HORIZONTAL.iterator());
        if(random.nextFloat() < 0.35F) directions.remove(random.nextInt(directions.size()));
        return directions;
    }

    @Override
    protected Direction selectBranchDirection(Data d, Random random, BlockPos pos, int length, Direction startDirection, Direction lastDirection) {
        return startDirection;
    }

    @Override
    protected int getMinBranchHeight(int maxHeight, Random random) {
        return (baseHeight / 2);
    }

    @Override
    protected int getBranchLength(Data d, BlockPos stemPos, Random random) {
        if(d.height < baseHeight + (d.maxHeight - baseHeight) / 2 || d.trunkPos.getY() + d.maxHeight - 1 == stemPos.getY()) return 1;
        else return super.getBranchLength(d, stemPos, random);
    }
}