package frostnox.nightfall.world.generation.structure;

import frostnox.nightfall.block.Soil;
import frostnox.nightfall.block.Stone;
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

import java.util.Random;

public class CottageRuinsPiece extends StructurePieceNF {
    public static final int X_SIZE = 10;
    public static final int Z_SIZE = 10;
    protected static FractalSimplexNoiseFast heightNoise;
    protected long lastSeed;

    public CottageRuinsPiece(int x, int z) {
        super(StructuresNF.COTTAGE_RUINS_PIECE, 0, makeBoundingBox(x, 0, z, Direction.SOUTH, X_SIZE, 2, Z_SIZE));
        setOrientation(Direction.SOUTH);
    }

    public CottageRuinsPiece(CompoundTag pTag) {
        super(StructuresNF.COTTAGE_RUINS_PIECE, pTag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {

    }

    @Override
    public void postProcess(WorldGenLevel level, StructureFeatureManager pStructureFeatureManager, ChunkGenerator gen, Random random, BoundingBox box, ChunkPos pChunkPos, BlockPos centerPos) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        long seed = level.getSeed();
        if(heightNoise == null || lastSeed != seed) {
            heightNoise = new FractalSimplexNoiseFast(seed, 0.085F, 2, 0.5F, 2.0F);
            lastSeed = seed;
        }
        BoundingBox surfaceBox = getSurfaceBox(level, box);
        int xSize = 5 + random.nextInt(6);
        int zSize = 5 + random.nextInt(6);
        //Offsetting the positions like this is unnecessary, don't do this in the future
        int xHalf1 = xSize / 2;
        int xHalf2 = xSize % 2 == 0 ? xHalf1 : (xSize / 2 + 1);
        int zHalf1 = zSize / 2;
        int zHalf2 = zSize % 2 == 0 ? zHalf1 : (zSize / 2 + 1);
        int xEdge1 = X_SIZE - xHalf1, xEdge2 = X_SIZE + xHalf2;
        int zEdge1 = Z_SIZE - zHalf1, zEdge2 = Z_SIZE + zHalf2;
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
        int doorX, doorZ;
        switch(random.nextInt(4)) {
            case 0 -> {
                doorX = X_SIZE;
                doorZ = zEdge1;
            }
            case 1 -> {
                doorX = X_SIZE;
                doorZ = zEdge2;
            }
            case 2 -> {
                doorX = xEdge1;
                doorZ = Z_SIZE;
            }
            default -> {
                doorX = xEdge2;
                doorZ = Z_SIZE;
            }
        }
        //Walls
        for(int x = X_SIZE - xHalf1 + 1; x <= X_SIZE + xHalf2 - 1; x++) {
            int worldX = getWorldX(x, zEdge1), worldZ = getWorldZ(x, zEdge1);
            float height = getHeight(worldX, worldZ);
            int floorY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ);
            pos.setX(worldX).setZ(worldZ);
            for(int y = 0; y <= (doorX == x && doorZ == zEdge1 ? 0 : Mth.ceil(height)); y++) {
                placeBlock(level, pickBlock(stone, y, height, random), pos.setY(floorY + y - 1), surfaceBox, y == 1);
            }
        }
        for(int x = X_SIZE - xHalf1 + 1; x <= X_SIZE + xHalf2 - 1; x++) {
            int worldX = getWorldX(x, zEdge2), worldZ = getWorldZ(x, zEdge2);
            float height = getHeight(worldX, worldZ);
            int floorY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ);
            pos.setX(worldX).setZ(worldZ);
            for(int y = 0; y <= (doorX == x && doorZ == zEdge2 ? 0 : Mth.ceil(height)); y++) {
                placeBlock(level, pickBlock(stone, y, height, random), pos.setY(floorY + y - 1), surfaceBox, y == 1);
            }
        }
        for(int z = Z_SIZE - zHalf1 + 1; z <= Z_SIZE + zHalf2 - 1; z++) {
            int worldX = getWorldX(xEdge1, z), worldZ = getWorldZ(xEdge1, z);
            float height = getHeight(worldX, worldZ);
            int floorY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ);
            pos.setX(worldX).setZ(worldZ);
            for(int y = 0; y <= (doorX == xEdge1 && doorZ == z ? 0 : Mth.ceil(height)); y++) {
                placeBlock(level, pickBlock(stone, y, height, random), pos.setY(floorY + y - 1), surfaceBox, y == 1);
            }
        }
        for(int z = Z_SIZE - zHalf1 + 1; z <= Z_SIZE + zHalf2 - 1; z++) {
            int worldX = getWorldX(xEdge2, z), worldZ = getWorldZ(xEdge2, z);
            float height = getHeight(worldX, worldZ);
            int floorY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ);
            pos.setX(worldX).setZ(worldZ);
            for(int y = 0; y <= (doorX == xEdge2 && doorZ == z ? 0 : Mth.ceil(height)); y++) {
                placeBlock(level, pickBlock(stone, y, height, random), pos.setY(floorY + y - 1), surfaceBox, y == 1);
            }
        }
        //Loot
        BlockState strangeSoil = BlocksNF.STRANGE_SOILS.get(soil).get().defaultBlockState();
        for(int i = 0; i < (xSize * zSize >= 60 ? 2 : 1); i++) {
            int soilX, soilZ;
            switch(random.nextInt(4)) {
                case 0 -> {
                    soilX = X_SIZE - xHalf1 + 1 + random.nextInt(xSize - 1);
                    soilZ = zEdge1 + 1;
                }
                case 1 -> {
                    soilX = X_SIZE - xHalf1 + 1 + random.nextInt(xSize - 1);
                    soilZ = zEdge2 - 1;
                }
                case 2 -> {
                    soilX = xEdge1 + 1;
                    soilZ = Z_SIZE - zHalf1 + 1 + random.nextInt(zSize - 1);
                }
                default -> {
                    soilX = xEdge2 - 1;
                    soilZ = Z_SIZE - zHalf1 + 1 + random.nextInt(zSize - 1);
                }
            }
            placeStrangeSoil(level, adjustBoxMinY(surfaceBox, -2), random, pos.set(getWorldX(soilX, soilZ),
                    level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, getWorldX(soilX, soilZ), getWorldZ(soilX, soilZ)) - 2,
                    getWorldZ(soilX, soilZ)), strangeSoil, LootTablesNF.COTTAGE_RUINS_LOOT);
        }
    }

    private static float getHeight(int worldX, int worldZ) {
        return Math.abs(heightNoise.noise2D(worldX, worldZ)) * 1.6F;
    }

    private static BlockState pickBlock(Stone stone, int y, float height, Random random) {
        if(y < (int) height + 1) {
            float f = random.nextFloat();
            if(f < (y == 0 ? 0.05F : 0.1F)) {
                return BlocksNF.STACKED_STONE_SIDINGS.get(stone).get().defaultBlockState()
                        .setValue(SidingBlock.TYPE, SidingBlock.Type.values()[random.nextInt(4)]);
            }
            else if(f < (y == 0 ? 0.15F : 0.3F)) {
                return BlocksNF.STACKED_STONE_STAIRS.get(stone).get().defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random))
                        .setValue(StairBlock.HALF, random.nextBoolean() ? Half.BOTTOM : Half.TOP);
            }
            else return BlocksNF.STACKED_STONE.get(stone).get().defaultBlockState();
        }
        else {
            float fraction = height % 1F;
            if(fraction < 0.5F) return BlocksNF.STACKED_STONE_SLABS.get(stone).get().defaultBlockState();
            else {
                return BlocksNF.STACKED_STONE_STAIRS.get(stone).get().defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
            }
        }
    }
}