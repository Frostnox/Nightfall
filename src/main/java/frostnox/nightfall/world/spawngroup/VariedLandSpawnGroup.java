package frostnox.nightfall.world.spawngroup;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class VariedLandSpawnGroup extends VariedSpawnGroup {
    protected final TagKey<Block> spawnBlocks;

    public VariedLandSpawnGroup(int weight, boolean friendly, int yMin, int yMax, int lightMin, int lightMax, float tempMin, float tempMax, float humidityMin, float humidityMax, TagKey<Block> spawnBlocks) {
        super(weight, friendly, yMin, yMax, lightMin, lightMax, tempMin, tempMax, humidityMin, humidityMax);
        this.spawnBlocks = spawnBlocks;
    }

    @Override
    public SpawnPlacements.Type getPlacementType() {
        return SpawnPlacements.Type.ON_GROUND;
    }

    @Override
    public boolean canSpawnAt(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity) {
        return super.canSpawnAt(level, pos, block, skyLight, temperature, humidity) && block.is(spawnBlocks);
    }
}
