package frostnox.nightfall.world.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.block.SoilCover;
import frostnox.nightfall.block.Stone;
import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FlatChunkGenerator extends ChunkGenerator {
    public static final Codec<FlatChunkGenerator> CODEC = RecordCodecBuilder.create((instance) ->
            commonCodec(instance).and(BiomeSource.CODEC.fieldOf("biome_source").forGetter((gen) -> gen.biomeSource))
                    .apply(instance, instance.stable(FlatChunkGenerator::new)));
    private static final BlockState GRASSY_DIRT = BlocksNF.COVERED_DIRT.get(SoilCover.GRASS).get().defaultBlockState();
    private static final BlockState DIRT = BlocksNF.DIRT.get().defaultBlockState();
    private static final BlockState SHALE = BlocksNF.STONE_BLOCKS.get(Stone.SHALE).get().defaultBlockState();
    private static final BlockState BEDROCK = BlocksNF.BEDROCK.get().defaultBlockState();
    private static final NoiseColumn NOISE_COLUMN;
    static {
        BlockState[] column = new BlockState[ContinentalChunkGenerator.SEA_LEVEL + 1];
        for(int y = 0; y <= ContinentalChunkGenerator.SEA_LEVEL; y++) {
            if(y == 0) column[y] = BEDROCK;
            else if(y < ContinentalChunkGenerator.SEA_LEVEL - 2) column[y] = SHALE;
            else if(y < ContinentalChunkGenerator.SEA_LEVEL) column[y] = DIRT;
            else column[y] = GRASSY_DIRT;
        }
        NOISE_COLUMN = new NoiseColumn(ContinentalChunkGenerator.MIN_Y, column);
    }

    public FlatChunkGenerator(Registry<StructureSet> structureSets, BiomeSource biomeSource) {
        super(structureSets, Optional.empty(), biomeSource, biomeSource, 0);
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor level) {
        return ContinentalChunkGenerator.SEA_LEVEL + 1;
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess pChunk, StructureFeatureManager pStructureFeatureManager) {

    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long pSeed) {
        return this;
    }

    @Override
    public Climate.Sampler climateSampler() {
        return Climate.empty();
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long pSeed, BiomeManager pBiomeManager, StructureFeatureManager pStructureFeatureManager, ChunkAccess pChunk, GenerationStep.Carving pStep) {

    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureFeatureManager pStructureFeatureManager, ChunkAccess pChunk) {

    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {

    }

    @Override
    public int getSeaLevel() {
        return ContinentalChunkGenerator.SEA_LEVEL;
    }

    @Override
    public int getMinY() {
        return ContinentalChunkGenerator.MIN_Y;
    }

    @Override
    public int getGenDepth() {
        return ContinentalChunkGenerator.MAX_Y;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor pExecutor, Blender pBlender, StructureFeatureManager pStructureFeatureManager, ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Heightmap oceanFloorMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurfaceMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for(int y = 0; y <= ContinentalChunkGenerator.SEA_LEVEL; y++) {
            BlockState block;
            if(y == 0) block = BEDROCK;
            else if(y < ContinentalChunkGenerator.SEA_LEVEL - 2) block = SHALE;
            else if(y < ContinentalChunkGenerator.SEA_LEVEL) block = DIRT;
            else block = GRASSY_DIRT;
            int worldY = chunk.getMinBuildHeight() + y;
            for(int x = 0; x < 16; x++) {
                for(int z = 0; z < 16; z++) {
                    chunk.setBlockState(pos.set(x, worldY, z), block, false);
                    oceanFloorMap.update(x, worldY, z, block);
                    worldSurfaceMap.update(x, worldY, z, block);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int pX, int pZ, Heightmap.Types pType, LevelHeightAccessor level) {
        return ContinentalChunkGenerator.SEA_LEVEL;
    }

    @Override
    public NoiseColumn getBaseColumn(int pX, int pZ, LevelHeightAccessor level) {
        return NOISE_COLUMN;
    }

    @Override
    public void addDebugScreenInfo(List<String> pInfo, BlockPos pos) {

    }
}
