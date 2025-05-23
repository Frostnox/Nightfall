package frostnox.nightfall.world.generation.tree;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class MapleTreeGenerator extends TreeGenerator {
    public MapleTreeGenerator(int baseHeight, int randHeight, int baseBranchLength, int randBranchLength, int leavesRadius) {
        super(baseHeight, randHeight, baseBranchLength, randBranchLength, leavesRadius);
    }

    @Override
    protected List<Direction> getBranchStartDirections(Data d, BlockPos centerPos, Random random, @Nullable List<Direction> lastDirections, @Nullable List<Direction> lastLastDirections) {
        List<Direction> directions = Lists.newArrayList(Direction.Plane.HORIZONTAL.iterator());
        if(lastDirections != null) directions.removeAll(lastDirections);
        while(directions.size() > 2) directions.remove((random.nextInt() & Integer.MAX_VALUE) % directions.size());
        return directions;
    }

    @Override
    protected int getMinBranchHeight(int maxHeight, Random random) {
        return super.getMinBranchHeight(maxHeight, random) + 1;
    }
}
