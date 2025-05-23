package frostnox.nightfall.world.generation.tree;

import frostnox.nightfall.util.math.OctalDirection;

public class AcaciaTreeGenerator extends SplitTreeGenerator {
    public AcaciaTreeGenerator(int baseHeight, int randHeight, int leavesRadius, int minSplits, int maxSplits, float curveChance, boolean curveRoot) {
        super(baseHeight, randHeight, 0, 0, leavesRadius, minSplits, maxSplits, curveChance, curveRoot);
    }

    @Override
    protected OctalDirection[] getInitialTrunkLeavesDirections(int y, int height) {
        return OctalDirection.CARDINALS_UP;
    }

    @Override
    protected int getTrunkLeavesCutoff(int height) {
        return 1;
    }

    @Override
    protected int getTrunkLeavesRadius(int y, int height, int maxHeight, int cutoff) {
        return (height < baseHeight || maxHeight <= averageHeight) ? Math.max(1, maxLeavesRadius - 1) : maxLeavesRadius;
    }
}
