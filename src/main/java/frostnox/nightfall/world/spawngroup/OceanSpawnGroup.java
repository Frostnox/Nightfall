package frostnox.nightfall.world.spawngroup;

import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.state.BlockState;

public class OceanSpawnGroup extends SimpleSpawnGroup {
    public OceanSpawnGroup(int weight, boolean friendly, EntityType<?> type, int yMin, int yMax, int lightMin, int lightMax, int sizeMin, int sizeMax, float tempMin, float tempMax, float humidityMin, float humidityMax) {
        super(weight, friendly, type, yMin, yMax, lightMin, lightMax, sizeMin, sizeMax, tempMin, tempMax, humidityMin, humidityMax);
    }

    @Override
    public SpawnPlacements.Type getPlacementType() {
        return SpawnPlacements.Type.IN_WATER;
    }

    @Override
    public boolean canSpawnAt(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity) {
        return super.canSpawnAt(level, pos, block, skyLight, temperature, humidity) && block.is(BlocksNF.SEAWATER.get());
    }
}
