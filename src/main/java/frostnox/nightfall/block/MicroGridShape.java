package frostnox.nightfall.block;

import frostnox.nightfall.block.block.anvil.TieredAnvilBlockEntity;


/**
 * Shapes for micro-grids to use
 */
public class MicroGridShape {
    public static final MicroGridShape CHUNK = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.CHUNK_13, Levels.CHUNK_2, Levels.CHUNK_13, Levels.CHUNK_4);
    public static final MicroGridShape INGOT = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.INGOT_LEVEL, Levels.INGOT_LEVEL);
    public static final MicroGridShape PLATE = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.PLATE_LEVEL);
    public static final MicroGridShape SCALES = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.SCALES_LEVEL);
    public static final MicroGridShape WIRES = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.WIRES_LEVEL);
    public static final MicroGridShape DOUBLE_INGOT_TALL = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.DOUBLE_INGOT_TALL_LEVEL, Levels.DOUBLE_INGOT_TALL_LEVEL);
    public static final MicroGridShape BILLET = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.BILLET_LEVEL, Levels.BILLET_LEVEL, Levels.BILLET_LEVEL);
    public static final MicroGridShape DOUBLE_BILLET_TALL = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.DOUBLE_BILLET_TALL_LEVEL, Levels.DOUBLE_BILLET_TALL_LEVEL, Levels.DOUBLE_BILLET_TALL_LEVEL);
    public static final MicroGridShape BLOCK = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.BLOCK_LEVEL, Levels.BLOCK_LEVEL, Levels.BLOCK_LEVEL, Levels.BLOCK_LEVEL, Levels.BLOCK_LEVEL);
    public static final MicroGridShape ANVIL = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.ANVIL_LEVEL_0, Levels.ANVIL_LEVEL_1, Levels.ANVIL_LEVEL_2, Levels.ANVIL_LEVEL_34, Levels.ANVIL_LEVEL_34);
    public static final MicroGridShape ADZE = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.ADZE_BASE, Levels.ADZE_BASE);
    public static final MicroGridShape AXE = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.AXE_BASE, Levels.AXE_TOP);
    public static final MicroGridShape CHISEL = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.CHISEL_BASE);
    public static final MicroGridShape KNIFE = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.KNIFE_BASE);
    public static final MicroGridShape MACE = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.MACE_BASE, Levels.MACE_MIDDLE, Levels.MACE_BASE, Levels.MACE_TOP);
    public static final MicroGridShape PICKAXE = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.PICKAXE_BASE, Levels.PICKAXE_TOP);
    public static final MicroGridShape SABRE = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.SABRE_BASE);
    public static final MicroGridShape SHOVEL = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.SHOVEL_BASE);
    public static final MicroGridShape SICKLE = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.SICKLE_BASE);
    public static final MicroGridShape SPEAR = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.SPEAR_BASE);
    public static final MicroGridShape SWORD = new MicroGridShape(TieredAnvilBlockEntity.GRID_Z, Levels.SWORD_BASE);

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
