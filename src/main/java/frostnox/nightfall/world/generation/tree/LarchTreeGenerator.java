package frostnox.nightfall.world.generation.tree;

public class LarchTreeGenerator extends SpruceTreeGenerator {
    public LarchTreeGenerator(int baseHeight, int randHeight, int leavesRadius) {
        super(baseHeight, randHeight, leavesRadius);
    }

    @Override
    protected int getBranchLengthReduction() {
        return 2;
    }

    @Override
    protected boolean branchAt(Data d, int y) {
        return y == 0 || y == d.maxHeight / 4 + 1 || y == d.maxHeight / 2 + 1 || y == d.maxHeight * 3 / 4 + 1;
    }

    @Override
    protected int getBranchRadius(int y, int height, int maxHeight, int cutoff) {
        if(y == height) return 1;
        int section1 = maxHeight / 4 + 1, section2 = maxHeight / 2 + 1, section3 = maxHeight * 3 / 4 + 1;
        if(y == section3 + 1) return Math.max(2, getLeavesRadius(height) - 1 - (y - section3));
        if(y >= 1 && y < section1) return Math.max(1, getLeavesRadius(height) - (y - 1));
        if(y >= section1 && y <= section2) return Math.max(1, getLeavesRadius(height) - (y - section1));
        if(y > section2 && y <= section3) return Math.max(1, getLeavesRadius(height) - (y - section2));
        if(y > section3 && y < maxHeight) return Math.max(0, getLeavesRadius(height) - 1 - (y - section3));
        return 1;
    }
}
