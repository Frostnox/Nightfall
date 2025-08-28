package frostnox.nightfall.world.spawngroup;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class LandBiomeTagSpawnGroup extends BiomeTagSpawnGroup {
    protected final TagKey<Block> spawnBlocks;

    public LandBiomeTagSpawnGroup(int weight, boolean friendly, EntityType<?> type, int yMin, int yMax, int lightMin, int lightMax, int sizeMin, int sizeMax, TagKey<Biome> biome, TagKey<Block> spawnBlocks) {
        super(weight, friendly, type, yMin, yMax, lightMin, lightMax, sizeMin, sizeMax, biome);
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
