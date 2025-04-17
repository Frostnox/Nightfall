package frostnox.nightfall.world.generation.tree;

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
}
