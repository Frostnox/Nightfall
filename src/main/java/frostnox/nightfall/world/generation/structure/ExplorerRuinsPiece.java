package frostnox.nightfall.world.generation.structure;

import frostnox.nightfall.block.Soil;
import frostnox.nightfall.block.Stone;
import frostnox.nightfall.block.Tree;
import frostnox.nightfall.block.block.SidingBlock;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.StructuresNF;
import frostnox.nightfall.registry.vanilla.LootTablesNF;
import frostnox.nightfall.util.math.noise.FractalSimplexNoiseFast;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.List;
import java.util.Random;

public class ExplorerRuinsPiece extends StructurePieceNF {
    public static final int SIZE = 11;
    public static final int ITEM_COLOR = 0xe3d1a5;
    private static final int[] xWall = new int[] {3, 3, 3, 4, 6, 7, 7, 7};
    private static final int[] zWall = new int[] {2, 1, 0, 0, 0, 0, 1, 2};
    protected static FractalSimplexNoiseFast heightNoise;
    protected long lastSeed;

    public ExplorerRuinsPiece(Random random, int x, int z) {
        this(x, z, getRandomHorizontalDirection(random));
    }

    public ExplorerRuinsPiece(int x, int z, Direction orientation) {
        super(StructuresNF.EXPLORER_RUINS_PIECE, 0, makeBoundingBox(x, 0, z, orientation, SIZE, 5, SIZE));
        setOrientation(orientation);
    }

    public ExplorerRuinsPiece(CompoundTag pTag) {
        super(StructuresNF.EXPLORER_RUINS_PIECE, pTag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {

    }

    @Override
    public void postProcess(WorldGenLevel level, StructureFeatureManager pStructureFeatureManager, ChunkGenerator gen, Random random, BoundingBox box, ChunkPos pChunkPos, BlockPos centerPos) {
        if(updateHeightAverage(level, 0)) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            long seed = level.getSeed();
            if(heightNoise == null || lastSeed != seed) {
                heightNoise = new FractalSimplexNoiseFast(seed, 0.09F, 2, 0.5F, 2.0F);
                lastSeed = seed;
            }
            BoundingBox surfaceBox = getSurfaceBox(level, box);
            Stone stone;
            Soil soil;
            if(gen instanceof ContinentalChunkGenerator contGen) {
                BlockPos center = pChunkPos.getMiddleBlockPosition(surfaceBox.minY());
                stone = contGen.getCachedSurfaceStone(center);
                soil = contGen.getCachedSoil(center);
            }
            else {
                stone = Stone.SHALE;
                soil = Soil.DIRT;
            }
            //Walls
            for(int i = 0; i < xWall.length; i++) {
                float height = getHeight(xWall[i], zWall[i]);
                for(int y = -1; y <= height; y++) {
                    placeBlock(level, pickBrick(stone, y, height, random), pos, xWall[i], y, zWall[i], box);
                }
                height = getHeight(zWall[i], xWall[i]);
                for(int y = -1; y <= height; y++) {
                    placeBlock(level, pickBrick(stone, y, height, random), pos, zWall[i], y, xWall[i], box);
                }
                height = getHeight(SIZE - 1 - xWall[i], SIZE - 1 - zWall[i]);
                for(int y = -1; y <= height; y++) {
                    placeBlock(level, pickBrick(stone, y, height, random), pos, SIZE - 1 - xWall[i], y, SIZE - 1 - zWall[i], box);
                }
                height = getHeight(SIZE - 1 - zWall[i], SIZE - 1 - xWall[i]);
                for(int y = -1; y <= height; y++) {
                    placeBlock(level, pickBrick(stone, y, height, random), pos, SIZE - 1 - zWall[i], y, SIZE - 1 - xWall[i], box);
                }
            }
            //Loot
            BlockState strangeSoil = BlocksNF.STRANGE_SOILS.get(soil).get().defaultBlockState();
            int tries = 4 + random.nextInt(3);
            for(int i = 0; i < tries; i++) {
                int soilX = 3 + random.nextInt(5), soilZ = 3 + random.nextInt(5);
                if(getRubbleHeight(soilX, soilZ) > 0F) {
                    placeStrangeSoil(level, adjustBoxMinY(surfaceBox, -1), random, pos.set(getWorldX(soilX, soilZ),
                            level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, getWorldX(soilX, soilZ), getWorldZ(soilX, soilZ)) - 1,
                            getWorldZ(soilX, soilZ)), strangeSoil, LootTablesNF.EXPLORER_RUINS_LOOT);
                }
            }
            //Rubble
            for(int x = SIZE/2 - 1; x <= SIZE/2 + 1; x++) {
                for(int z = 1; z <= SIZE - 2; z++) {
                    float height = getRubbleHeight(x, z);
                    if(height < 0F) continue;
                    BlockState[] blocks = new BlockState[Mth.ceil(height)];
                    for(int y = 0; y <= height; y++) blocks[y] = pickRubble(stone, y, height, random);
                    stackSurfaceBlocks(level, pos, x, z, surfaceBox, blocks);
                }
            }
            for(int x = 1; x <= 3; x++) {
                for(int z = SIZE/2 - 1; z <= SIZE/2 + 1; z++) {
                    float height = getRubbleHeight(x, z);
                    if(height < 0F) continue;
                    BlockState[] blocks = new BlockState[Mth.ceil(height)];
                    for(int y = 0; y <= height; y++) blocks[y] = pickRubble(stone, y, height, random);
                    stackSurfaceBlocks(level, pos, x, z, surfaceBox, blocks);
                }
            }
            for(int x = 7; x <= 9; x++) {
                for(int z = SIZE/2 - 1; z <= SIZE/2 + 1; z++) {
                    float height = getRubbleHeight(x, z);
                    if(height < 0F) continue;
                    BlockState[] blocks = new BlockState[Mth.ceil(height)];
                    for(int y = 0; y <= height; y++) blocks[y] = pickRubble(stone, y, height, random);
                    stackSurfaceBlocks(level, pos, x, z, surfaceBox, blocks);
                }
            }
            for(int x = 3; x <= 7; x += 4) {
                for(int z = 3; z <= 7; z += 4) {
                    float height = getRubbleHeight(x, z);
                    if(height < 0F) continue;
                    BlockState[] blocks = new BlockState[Mth.ceil(height)];
                    for(int y = 0; y <= height; y++) blocks[y] = pickRubble(stone, y, height, random);
                    stackSurfaceBlocks(level, pos, x, z, surfaceBox, blocks);
                }
            }
        }
    }

    private float getHeight(int x, int z) {
        return 1.1F + Math.abs(heightNoise.noise2D(getWorldX(x, z), getWorldZ(x, z))) * 1.5F;
    }

    private float getRubbleHeight(int x, int z) {
        float dist = (float) Math.sqrt((x - SIZE/2) * (x - SIZE/2) + (z - SIZE/2) * (z - SIZE/2)) / 2.6F;
        return 1.55F * (1F - dist) - 0.15F + Math.abs(heightNoise.noise2D(getWorldX(x, z), getWorldZ(x, z))) * 1.8F;
    }

    private static BlockState pickBrick(Stone stone, int y, float height, Random random) {
        if(y < (int) height) return BlocksNF.STONE_BRICK_BLOCKS.get(stone).get().defaultBlockState();
        else {
            float fraction = height % 1F;
            if(fraction < 0.5F) return BlocksNF.STONE_BRICK_SLABS.get(stone).get().defaultBlockState();
            else {
                return BlocksNF.STONE_BRICK_STAIRS.get(stone).get().defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
            }
        }
    }

    private static BlockState pickRubble(Stone stone, int y, float height, Random random) {
        if(y < (int) height) return random.nextFloat() < 0.33F ? BlocksNF.TERRACOTTA_TILES.get().defaultBlockState() :
                BlocksNF.COBBLED_STONE.get(stone).get().defaultBlockState();
        else {
            float fraction = height % 1F;
            if(fraction < 0.5F) return random.nextFloat() < 0.33F ? BlocksNF.TERRACOTTA_TILE_SLAB.get().defaultBlockState() :
                    BlocksNF.COBBLED_STONE_SLABS.get(stone).get().defaultBlockState();
            else {
                return (random.nextFloat() < 0.33F ? BlocksNF.TERRACOTTA_TILE_STAIRS.get().defaultBlockState() :
                        BlocksNF.COBBLED_STONE_STAIRS.get(stone).get().defaultBlockState())
                        .setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
            }
        }
    }
}