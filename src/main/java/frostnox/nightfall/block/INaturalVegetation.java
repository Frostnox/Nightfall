package frostnox.nightfall.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface INaturalVegetation {
    int getWeight();

    BlockState getGrowthBlock();

    /**
     * @param temperature at world position, season independent
     * @param humidity at world position, season independent
     */
    boolean canGrowAt(ServerLevel level, BlockPos pos, ISoil soil, SoilCover cover, int skyLight, float temperature, float humidity);

    default boolean hasClusteredGrowth() {
        return true;
    }
}