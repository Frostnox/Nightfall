package frostnox.nightfall.world.generation.tree;

import frostnox.nightfall.block.block.tree.TreeTrunkBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.WorldGenLevel;

import java.util.Random;

public class JungleTreeGenerator extends CurvedTreeGenerator {
    public JungleTreeGenerator(int baseHeight, int randHeight, int baseBranchLength, int randBranchLength, int leavesRadius, double curveChance, boolean doubleCurve) {
        super(baseHeight, randHeight, baseBranchLength, randBranchLength, leavesRadius, curveChance, doubleCurve);
    }

    @Override
    protected int getMinBranchHeight(int maxHeight, Random random) {
        return maxHeight * 3 / 5;
    }

    @Override
    protected int getTrunkLeavesCutoff(int height) {
        return Math.max(1, height / 3);
    }

    @Override
    public void tryFruit(WorldGenLevel level, Data d, TreeTrunkBlockEntity entity) {
        if(!d.trunkLeaves.isEmpty()) tryFruitBranchLeaves(level, d, entity, 4, new ObjectArrayList<>(d.trunkLeaves));
    }
}
