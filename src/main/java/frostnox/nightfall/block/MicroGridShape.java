package frostnox.nightfall.block;

/**
 * Shapes for micro-grids to use
 */
public class MicroGridShape {
    private final boolean[][][] grid;
    private final String[] levels;

    public MicroGridShape(int zSize, String... levels) {
        this.levels = levels;
        int xSize = levels[0].length() / zSize;
        boolean[][][] grid = new boolean[xSize][levels.length][zSize];
        for(int y = 0; y < levels.length; y++) {
            String level = levels[y];
            for(int i = 0; i < level.length(); i++) {
                if(level.charAt(i) == 'X') {
                    int x = i % xSize;
                    int z = i / xSize;
                    grid[x][y][z] = true;
                }
            }
        }
        this.grid = grid;
    }

    public boolean[][][] getGrid() {
        return IMicroGrid.copyGrid(grid);
    }

    public String[] getGridAsString() {
        return levels;
    }

    private static class Levels {
        private static final String CHUNK_13 =
                "oooooooooooooo" +
                "oooooooooooooo" +
                "oooooo??oooooo" +
                "ooooo?XX?ooooo" +
                "oooo?XXXX?oooo" +
                "ooooo?XX?ooooo" +
                "oooooo??oooooo" +
                "oooooooooooooo";

        private static final String CHUNK_2 =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooo??oooooo" +
                        "oooooXXXXooooo" +
                        "ooooXXXXXXoooo" +
                        "oooooXXXXooooo" +
                        "oooooo??oooooo" +
                        "oooooooooooooo";

        private static final String CHUNK_4 =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooo??oooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";

        private static final String INGOT_LEVEL =
                "oooooooooooooo" +
                "oooooooooooooo" +
                "oooooooooooooo" +
                "ooooXXXXXXoooo" +
                "ooooXXXXXXoooo" +
                "ooooXXXXXXoooo" +
                "oooooooooooooo" +
                "oooooooooooooo";

        private static final String PLATE_LEVEL =
                        "oooooooooooooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "oooooooooooooo";

        private static final String SCALES_LEVEL =
                        "oooooooooooooo" +
                        "ooooXXooXXoooo" +
                        "ooooXXooXXoooo" +
                        "ooooooXXoooooo" +
                        "ooooooXXoooooo" +
                        "ooooXXooXXoooo" +
                        "ooooXXooXXoooo" +
                        "oooooooooooooo";

        private static final String WIRES_LEVEL =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooooXXXXXXoooo" +
                        "oooooooooooooo" +
                        "ooooXXXXXXoooo" +
                        "oooooooooooooo" +
                        "ooooXXXXXXoooo" +
                        "oooooooooooooo";

        private static final String DOUBLE_INGOT_TALL_LEVEL =
                        "oooooooooooooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "oooooooooooooo";

        private static final String BILLET_LEVEL =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";

        private static final String DOUBLE_BILLET_TALL_LEVEL =
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo";

        private static final String BLOCK_LEVEL =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooooXXXXXooooo" +
                        "ooooXXXXXooooo" +
                        "ooooXXXXXooooo" +
                        "ooooXXXXXooooo" +
                        "ooooXXXXXooooo" +
                        "oooooooooooooo";

        private static final String ANVIL_LEVEL_0 =
                        "oooooooooooooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "ooooXXXXXXoooo" +
                        "oooooooooooooo";
        private static final String ANVIL_LEVEL_1 =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooXXXXooooo" +
                        "oooooXXXXooooo" +
                        "oooooXXXXooooo" +
                        "oooooXXXXooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";
        private static final String ANVIL_LEVEL_2 =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooooooXXoooooo" +
                        "ooooooXXoooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";
        private static final String ANVIL_LEVEL_34 =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooXXXXXXXXooo" +
                        "oooXXXXXXXXooo" +
                        "oooXXXXXXXXooo" +
                        "oooXXXXXXXXooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";

        private static final String ADZE_BASE =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooooooXXXXoooo" +
                        "oooooXXoXXoooo" +
                        "ooooXXoooooooo" +
                        "ooooXooooooooo" +
                        "oooooooooooooo";

        private static final String AXE_BASE =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooooXXoooooooo" +
                        "ooooXXXXXooooo" +
                        "ooooXXXXXooooo" +
                        "ooooXXoooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";
        private static final String AXE_TOP =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooXXooooo" +
                        "oooooooXXooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";

        private static final String CHISEL_BASE =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooXXXXXXXooooo" +
                        "ooXXXXXXXXXXoo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";

        private static final String KNIFE_BASE =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooooXXXXXXXooo" +
                        "oooXXXXXXXXooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";

        private static final String MACE_BASE =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooXXXoooooo" +
                        "oooooXXXoooooo" +
                        "oooooXXXoooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";
        private static final String MACE_MIDDLE =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooooooXooooooo" +
                        "oooooXXXoooooo" +
                        "ooooXXXXXooooo" +
                        "oooooXXXoooooo" +
                        "ooooooXooooooo" +
                        "oooooooooooooo";
        private static final String MACE_TOP =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooooooXooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";

        private static final String PICKAXE_BASE =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooooXXXXXXoooo" +
                        "ooXXXXXXXXXXoo" +
                        "oXXooooooooXXo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";
        private static final String PICKAXE_TOP =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooooooXXoooooo" +
                        "ooooooXXoooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";

        private static final String SABRE_BASE =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "XXXooooooooXXX" +
                        "oXXXXXXXXXXXXX" +
                        "oooXXXXXXXXooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";

        private static final String SHOVEL_BASE =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooXXXXooooo" +
                        "ooooXXXXXooooo" +
                        "ooooXXXXXooooo" +
                        "oooooXXXXooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";

        private static final String SICKLE_BASE =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooXXXXXXo" +
                        "ooooXXXXXXXXXo" +
                        "ooXXXXXXoooooo" +
                        "oXXXoooooooooo" +
                        "oXoooooooooooo" +
                        "oooooooooooooo";

        private static final String SPEAR_BASE =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooXXXoooooooo" +
                        "oXXXXXXooooooo" +
                        "ooooooooXXXooo" +
                        "oooooooXXXXXXo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";

        private static final String SWORD_BASE =
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "ooXXXXXXXXXXXX" +
                        "XXXXXXXXXXXXXX" +
                        "oooooooooooooo" +
                        "oooooooooooooo" +
                        "oooooooooooooo";
    }
}
