package frostnox.nightfall.block;

import com.mojang.math.Vector3f;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * Interactable micro-block grid for classes of type BlockEntity
 */
public interface IMicroGrid {
    boolean[][][] getGrid();

    int getGridXSize();

    int getGridYSize();

    int getGridZSize();

    /**
     * @return offset from block in world scale
     */
    Vector3f getWorldGridOffset();

    /**
     * @return visual y-rotation of grid in degrees, positive and divisible by 90
     */
    float getRotationDegrees();

    ResourceLocation getRecipeID();

    void setRecipeID(ResourceLocation id);

    boolean canUseGrid(Action action);

    /**
     * @return whether type passed into useGrid is valid
     */
    boolean isValidActionType(int type);

    /**
     * Used for interactions on the grid once it is ready
     *
     * @param type   interaction type
     * @param hitPos world position of cube
     * @param item
     * @return true if use succeeds
     */
    boolean useGrid(int type, Vec3i hitPos, Player user, ItemStack item);

    default void clearGrid() {
        for(int x = 0; x < getGridXSize(); x++) {
            for(int y = 0; y < getGridYSize(); y++) {
                for(int z = 0; z < getGridZSize(); z++) {
                    getGrid()[x][y][z] = false;
                }
            }
        }
    }

    default boolean isGridEmpty() {
        for(int x = 0; x < getGridXSize(); x++) {
            for(int y = 0; y < getGridYSize(); y++) {
                for(int z = 0; z < getGridZSize(); z++) {
                    if(getGrid()[x][y][z]) return false;
                }
            }
        }
        return true;
    }

    default boolean gridHas(int x, int y, int z) {
        if(gridContainsLocation(x, y, z)) return getGrid()[x][y][z];
        return false;
    }

    default boolean gridContainsLocation(int x, int y, int z) {
        return x >= 0 && x < getGridXSize() && y >= 0 && y < getGridYSize() && z >= 0 && z < getGridZSize();
    }

    default boolean hasAdjacency(int x, int y, int z) {
        if(!gridContainsLocation(x, y, z)) return false;
        if(x + 1 < getGridXSize() && getGrid()[x+1][y][z]) return true;
        if(x - 1 >= 0 && getGrid()[x-1][y][z]) return true;
        if(y + 1 < getGridYSize() && getGrid()[x][y+1][z]) return true;
        if(y - 1 >= 0 && getGrid()[x][y-1][z]) return true;
        if(z + 1 < getGridZSize() && getGrid()[x][y][z+1]) return true;
        if(z - 1 >= 0 && getGrid()[x][y][z-1]) return true;
        return false;
    }

    default boolean hasAdjacencyExcluding(int x, int y, int z, int xExclude, int yExclude, int zExclude) {
        if(!gridContainsLocation(x, y, z)) return false;
        boolean adj;
        if(gridContainsLocation(xExclude, yExclude, zExclude) && getGrid()[xExclude][yExclude][zExclude]) {
            getGrid()[xExclude][yExclude][zExclude] = false;
            adj = hasAdjacency(x, y, z);
            getGrid()[xExclude][yExclude][zExclude] = true;
        }
        else adj = hasAdjacency(x, y, z);
        return adj;
    }

    default Vec3 getWorldPos(BlockPos blockPos, float offX, float offY, float offZ) {
        float rot = -getRotationDegrees();
        Vec2 rotOffset = new Vec2(-(float) getGridXSize() / 32F - getWorldGridOffset().x(), -(float) getGridZSize() / 32F - getWorldGridOffset().z());
        Vector3f gridOffset = getWorldGridOffset();
        gridOffset.add(offX, offY, offZ);
        Vector3f offset = MathUtil.rotatePointByYaw(gridOffset, rot, rotOffset);
        return new Vec3(blockPos.getX() + offset.x(),
                blockPos.getY() + offset.y(), blockPos.getZ() + offset.z());
    }

    static String idFromPos(int x, int y, int z) {
        return String.format("%02d%02d%02d", x, y, z);
    }

    static int packedPosFromId(String id) {
        return (Integer.parseInt(id.substring(0, 2)) << 4) & (Integer.parseInt(id.substring(2, 4)) << 2) & Integer.parseInt(id.substring(4, 6));
    }

    static Vec3i posFromId(String id) {
        return new Vec3i(Integer.parseInt(id.substring(0, 2)), Integer.parseInt(id.substring(2, 4)), Integer.parseInt(id.substring(4, 6)));
    }

    static void copyGridData(boolean[][][] from, boolean[][][] to) {
        int xSize = to.length;
        int ySize = to[0].length;
        int zSize = to[0][0].length;
        if(from.length != xSize || from[0].length != ySize || from[0][0].length != zSize) {
            Nightfall.LOGGER.warn("Failed to copy micro-grid data due to unequal dimensions.");
            return;
        }
        for(int x = 0; x < xSize; x++) {
            for(int y = 0; y < ySize; y++) {
                for(int z = 0; z < zSize; z++) {
                    to[x][y][z] = from[x][y][z];
                }
            }
        }
    }

    static boolean[][][] copyGrid(boolean[][][] grid) {
        boolean[][][] gridCopy = new boolean[grid.length][grid[0].length][grid[0][0].length];
        for(int x = 0; x < grid.length; x++) {
            for(int y = 0; y < grid[0].length; y++) {
                for(int z = 0; z < grid[0][0].length; z++) {
                    gridCopy[x][y][z] = grid[x][y][z];
                }
            }
        }
        return gridCopy;
    }

    static void clearGrid(boolean[][][] grid) {
        for(int x = 0; x < grid.length; x++) {
            for(int y = 0; y < grid[0].length; y++) {
                for(int z = 0; z < grid[0][0].length; z++) {
                    grid[x][y][z] = false;
                }
            }
        }
    }

    static boolean isEmpty(boolean[][][] grid) {
        for(int x = 0; x < grid.length; x++) {
            for(int y = 0; y < grid[0].length; y++) {
                for(int z = 0; z < grid[0][0].length; z++) {
                    if(grid[x][y][z]) return false;
                }
            }
        }
        return true;
    }

    static boolean equals(boolean[][][] grid1, boolean[][][] grid2) {
        int xSize = grid1.length;
        int ySize = grid1[0].length;
        int zSize = grid1[0][0].length;
        if(grid2.length != xSize || grid2[0].length != ySize || grid2[0][0].length != zSize) return false;
        for(int x = 0; x < xSize; x++) {
            for(int y = 0; y < ySize; y++) {
                for(int z = 0; z < zSize; z++) {
                    if(grid1[x][y][z] != grid2[x][y][z]) return false;
                }
            }
        }
        return true;
    }

    static int compare(boolean[][][] grid1, boolean[][][] grid2) {
        int size1 = 0, size2 = 0;
        for(int x = 0; x < grid1.length; x++) {
            for(int y = 0; y < grid1[0].length; y++) {
                for(int z = 0; z < grid1[0][0].length; z++) {
                    if(grid1[x][y][z]) size1++;
                }
            }
        }
        for(int x = 0; x < grid2.length; x++) {
            for(int y = 0; y < grid2[0].length; y++) {
                for(int z = 0; z < grid2[0][0].length; z++) {
                    if(grid2[x][y][z]) size2++;
                }
            }
        }
        return Integer.compare(size1, size2);
    }
}
