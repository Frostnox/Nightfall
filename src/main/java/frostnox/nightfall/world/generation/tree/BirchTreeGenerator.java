package frostnox.nightfall.world.generation.tree;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class BirchTreeGenerator extends TreeGenerator {
    public BirchTreeGenerator(int baseHeight, int randHeight, int baseBranchLength, int randBranchLength, int leavesRadius) {
        super(baseHeight, randHeight, baseBranchLength, randBranchLength, leavesRadius);
    }

    @Override
    protected int getLeavesRadius(int height) {
        return maxLeavesRadius;
    }

    @Override
    protected List<Direction> getBranchStartDirections(Data d, BlockPos centerPos, Random random, @Nullable List<Direction> lastDirections, @Nullable List<Direction> lastLastDirections) {
        List<Direction> directions = super.getBranchStartDirections(d, centerPos, random, lastDirections, lastLastDirections);
        if(random.nextBoolean()) return List.of();
        else return directions;
    }
}
