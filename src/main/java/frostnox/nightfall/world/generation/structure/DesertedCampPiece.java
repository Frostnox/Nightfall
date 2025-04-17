package frostnox.nightfall.world.generation.structure;

import frostnox.nightfall.block.Stone;
import frostnox.nightfall.block.Tree;
import frostnox.nightfall.block.block.barrel.BarrelBlockNF;
import frostnox.nightfall.block.block.fuel.HorizontalFuelBlock;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.StructuresNF;
import frostnox.nightfall.registry.vanilla.LootTablesNF;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import frostnox.nightfall.world.generation.TreePool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.Random;

public class DesertedCampPiece extends StructurePieceNF {
    public static final int SIZE = 9;

    public DesertedCampPiece(Random random, int x, int z) {
        this(x, z, getRandomHorizontalDirection(random));
    }

    public DesertedCampPiece(int x, int z, Direction orientation) {
        super(StructuresNF.DESERTED_CAMP_PIECE, 0, makeBoundingBox(x, 0, z, orientation, SIZE, 3, SIZE));
        setOrientation(orientation);
    }

    public DesertedCampPiece(CompoundTag pTag) {
        super(StructuresNF.DESERTED_CAMP_PIECE, pTag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {

    }

    @Override
    public void postProcess(WorldGenLevel level, StructureFeatureManager pStructureFeatureManager, ChunkGenerator gen, Random random, BoundingBox box, ChunkPos pChunkPos, BlockPos centerPos) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BoundingBox surfaceBox = getSurfaceBox(level, box);
        Tree tree;
        Stone stone;
        if(gen instanceof ContinentalChunkGenerator contGen) {
            TreePool pool = contGen.getCachedTreePool(pChunkPos);
            if(pool.trees()[0] == null) tree = Tree.OAK;
            else tree = pool.trees()[0].tree();
            stone = contGen.getCachedSurfaceStone(surfaceBox.getCenter());
        }
        else {
            tree = Tree.OAK;
            stone = Stone.SHALE;
        }
        BlockState log = BlocksNF.LOGS.get(tree).get().defaultBlockState();
        BlockState stackedStone = BlocksNF.STACKED_STONE.get(stone).get().defaultBlockState();
        int breachX, breachZ;
        switch(random.nextInt(4)) {
            case 0 -> {
                breachX = 3 + random.nextInt(3);
                breachZ = 0;
            }
            case 1 -> {
                breachX = 3 + random.nextInt(3);
                breachZ = SIZE - 1;
            }
            case 2 -> {
                breachX = 0;
                breachZ = 3 + random.nextInt(3);
            }
            default -> {
                breachX = SIZE - 1;
                breachZ = 3 + random.nextInt(3);
            }
        }
        //Edges
        for(int i = 2; i < SIZE - 2; i++) {
            int height = i % 2 == 0 ? 3 : 2;
            if(i != SIZE/2) placeEdge(level, log, pos, i, height, 0, breachX, breachZ, surfaceBox);
            placeEdge(level, log, pos, i, height, SIZE - 1, breachX, breachZ, surfaceBox);
            placeEdge(level, log, pos, 0, height, i, breachX, breachZ, surfaceBox);
            placeEdge(level, log, pos, SIZE - 1, height, i, breachX, breachZ, surfaceBox);
        }
        //Corners
        stackSurfaceBlocks(level, log, pos, 1, 2, 1, surfaceBox);
        stackSurfaceBlocks(level, log, pos, SIZE - 2, 2, 1, surfaceBox);
        stackSurfaceBlocks(level, log, pos, 1, 2, SIZE - 2, surfaceBox);
        stackSurfaceBlocks(level, log, pos, SIZE - 2, 2, SIZE - 2, surfaceBox);
        //Fire pit
        placeSurfaceBlock(level, stackedStone, pos, SIZE/2, -1, 5, surfaceBox);
        for(int x = SIZE/2 - 1; x <= SIZE/2 + 1; x++) {
            for(int z = 4; z <= 6; z++) {
                if((x != SIZE/2 || z != 5) && random.nextBoolean()) {
                    placeSurfaceBlock(level, stackedStone, pos, x, -1, z, surfaceBox);
                }
            }
        }
        //Firewood
        BlockState firewood = BlocksNF.FIREWOOD.get().defaultBlockState().setValue(HorizontalFuelBlock.AXIS, Direction.Axis.X);
        placeSurfaceBlock(level, firewood, pos, 1, 0, 5, surfaceBox);
        placeSurfaceBlock(level, firewood, pos, 1, 0, 6, surfaceBox);
        if(random.nextBoolean()) placeSurfaceBlock(level, firewood, pos, 1, 0, 6, surfaceBox);
        //Charcoal pit
        embedSurfaceBlocks(level, stackedStone, pos, 2, 0, 1, 3, surfaceBox);
        embedSurfaceBlocks(level, stackedStone, pos, 3, 0, 1, 3, surfaceBox);
        embedSurfaceBlocks(level, stackedStone, pos, 2, 0, 4, 3, surfaceBox);
        embedSurfaceBlocks(level, stackedStone, pos, 3, 0, 4, 3, surfaceBox);
        embedSurfaceBlocks(level, stackedStone, pos, 1, 0, 2, 3, surfaceBox);
        embedSurfaceBlocks(level, stackedStone, pos, 1, 0, 3, 3, surfaceBox);
        embedSurfaceBlocks(level, stackedStone, pos, 4, 0, 2, 3, surfaceBox);
        embedSurfaceBlocks(level, stackedStone, pos, 4, 0, 3, 3, surfaceBox);
        BlockState charcoal = BlocksNF.CHARCOAL.get().defaultBlockState();
        embedSurfaceBlocks(level, pos, 2, -1, 2, surfaceBox, charcoal, charcoal, stackedStone);
        embedSurfaceBlocks(level, pos, 3, -1, 2, surfaceBox, charcoal, charcoal, stackedStone);
        embedSurfaceBlocks(level, pos, 2, -1, 3, surfaceBox, charcoal, charcoal, stackedStone);
        embedSurfaceBlocks(level, pos, 3, -1, 3, surfaceBox, charcoal, charcoal, stackedStone);

        int barrelZ = 3 + random.nextInt(3);
        placeContainer(level, surfaceBox, random, 7, getSurfaceY(level, 7, barrelZ), barrelZ,
                BlocksNF.BARRELS.get(tree).get().defaultBlockState().setValue(BarrelBlockNF.FACING, Direction.values()[1 + random.nextInt(4)]),
                LootTablesNF.DESERTED_CAMP_LOOT, true);
    }

    protected void placeEdge(WorldGenLevel level, BlockState pBlockstate, BlockPos.MutableBlockPos pos, int x, int amount, int z, int breachX, int breachZ, BoundingBox pBoundingbox) {
        int breach = Math.abs(x - breachX) + Math.abs(z - breachZ);
        if(breach == 0) amount -= 2;
        else if(breach == 1) amount -= 1;
        int surfaceY = getSurfaceY(level, x, z);
        pos.setX(getWorldX(x, z)).setZ(getWorldZ(x, z));
        for(int y = 0; y < amount; y++) {
            placeBlock(level, pBlockstate, pos.setY(surfaceY + y), pBoundingbox, y == 0);
        }
    }
}