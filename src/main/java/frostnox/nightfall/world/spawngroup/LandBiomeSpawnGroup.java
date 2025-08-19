package frostnox.nightfall.world.spawngroup;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public class LandBiomeSpawnGroup extends BiomeSpawnGroup {
    protected final TagKey<Block> spawnBlocks;

    public LandBiomeSpawnGroup(int weight, boolean friendly, EntityType<?> type, int yMin, int yMax, int lightMin, int lightMax, int sizeMin, int sizeMax, RegistryObject<Biome> biome, TagKey<Block> spawnBlocks) {
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
