package frostnox.nightfall.world.generation.structure;

import frostnox.nightfall.block.block.CoveredSoilBlock;
import frostnox.nightfall.block.block.MenuContainerBlockEntity;
import frostnox.nightfall.block.block.strangesoil.StrangeSoilBlockEntity;
import frostnox.nightfall.data.TagsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.FluidState;

import java.util.Random;

public abstract class StructurePieceNF extends StructurePiece {
    protected StructurePieceNF(StructurePieceType pType, int pGenDepth, BoundingBox pBox) {
        super(pType, pGenDepth, pBox);
    }

    public StructurePieceNF(StructurePieceType pType, CompoundTag pTag) {
        super(pType, pTag);
    }

    @Override
    protected void placeBlock(WorldGenLevel level, BlockState block, int x, int y, int z, BoundingBox box) {
        BlockPos worldPos = getWorldPos(x, y, z);
        if(box.isInside(worldPos)) {
            if(getMirror() != Mirror.NONE) block = block.mirror(getMirror());
            if(getRotation() != Rotation.NONE) block = block.rotate(getRotation());
            level.setBlock(worldPos, block, 2);
            FluidState fluidstate = level.getFluidState(worldPos);
            if(!fluidstate.isEmpty()) level.scheduleTick(worldPos, fluidstate.getType(), 0);
            if(block.is(TagsNF.STRUCTURE_POST_PROCESS)) level.getChunk(worldPos).markPosForPostprocessing(worldPos);
        }
    }

    protected void placeAirBox(WorldGenLevel level, BlockPos.MutableBlockPos pos, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BoundingBox box) {
        for(int y = minY; y <= maxY; y++) {
            for(int x = minX; x <= maxX; x++) {
                for(int z = minZ; z <= maxZ; z++) {
                    placeBlock(level, Blocks.AIR.defaultBlockState(), pos, x, y, z, box);
                }
            }
        }
    }

    protected void placeBlock(WorldGenLevel level, BlockState block, BlockPos.MutableBlockPos pos, int x, int y, int z, BoundingBox box, boolean againstSurface) {
        placeBlock(level, block, pos.set(getWorldX(x, z), getWorldY(y), getWorldZ(x, z)), box, againstSurface);
    }

    protected void placeBlock(WorldGenLevel level, BlockState block, BlockPos.MutableBlockPos pos, int x, int y, int z, BoundingBox box) {
        placeBlock(level, block, pos.set(getWorldX(x, z), getWorldY(y), getWorldZ(x, z)), box, false);
    }

    protected void tryReplaceBlock(WorldGenLevel level, BlockState block, BlockPos.MutableBlockPos pos, int x, int y, int z, BoundingBox box, boolean againstSurface) {
        pos.set(getWorldX(x, z), getWorldY(y), getWorldZ(x, z));
        BlockState currentBlock = level.getBlockState(pos);
        if(currentBlock.isAir() || currentBlock.getMaterial().isReplaceable()) placeBlock(level, block, pos, box, againstSurface);
    }

    protected void tryReplaceBlock(WorldGenLevel level, BlockState block, BlockPos.MutableBlockPos pos, int x, int y, int z, BoundingBox box) {
        tryReplaceBlock(level, block, pos, x, y, z, box, false);
    }

    protected void placeBlock(WorldGenLevel level, BlockState block, BlockPos worldPos, BoundingBox box, boolean againstSurface) {
        if(box.isInside(worldPos)) {
            if(getMirror() != Mirror.NONE) block = block.mirror(getMirror());
            if(getRotation() != Rotation.NONE) block = block.rotate(getRotation());
            level.setBlock(worldPos, block, 2);
            FluidState fluidstate = level.getFluidState(worldPos);
            if(!fluidstate.isEmpty()) level.scheduleTick(worldPos, fluidstate.getType(), 0);
            if(block.is(TagsNF.STRUCTURE_POST_PROCESS)) level.getChunk(worldPos).markPosForPostprocessing(worldPos);
            if(againstSurface) {
                BlockPos surfacePos = worldPos.below();
                if(level.getBlockState(surfacePos).getBlock() instanceof CoveredSoilBlock coveredSoil) {
                    int light = 15;
                    if(block.canOcclude()) {
                        if(block.useShapeForLightOcclusion()) {
                            if(Block.isFaceFull(block.getOcclusionShape(level, worldPos), Direction.DOWN)) {
                                light = 0;
                            }
                        }
                        else light -= block.getLightBlock(level, worldPos);
                    }
                    if(!coveredSoil.soilCover.canGrow(light)) {
                        level.setBlock(surfacePos, coveredSoil.soilBlock.get().defaultBlockState(), 2);
                    }
                }
            }
        }
    }
    
    protected void stackSurfaceBlocks(WorldGenLevel level, BlockState pBlockstate, BlockPos.MutableBlockPos pos, int x, int amount, int z, BoundingBox pBoundingbox) {
        int surfaceY = getSurfaceY(level, x, z);
        pos.setX(getWorldX(x, z)).setZ(getWorldZ(x, z));
        for(int y = 0; y < amount; y++) {
            placeBlock(level, pBlockstate, pos.setY(surfaceY + y), pBoundingbox, y == 0);
        }
    }

    protected void stackSurfaceBlocks(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, BoundingBox pBoundingbox, BlockState... blocks) {
        int surfaceY = getSurfaceY(level, x, z);
        pos.setX(getWorldX(x, z)).setZ(getWorldZ(x, z));
        for(int y = 0; y < blocks.length; y++) {
            placeBlock(level, blocks[y], pos.setY(surfaceY + y), pBoundingbox, y == 0);
        }
    }

    protected void embedSurfaceBlocks(WorldGenLevel level, BlockState pBlockstate, BlockPos.MutableBlockPos pos, int x, int yOff, int z, int amount, BoundingBox box) {
        int surfaceY = getSurfaceY(level, x, z);
        pos.setX(getWorldX(x, z)).setZ(getWorldZ(x, z));
        BoundingBox adjustedBox = new BoundingBox(box.minX(), Math.max(1, box.minY() + yOff - amount), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
        for(int y = 0; y < amount; y++) {
            placeBlock(level, pBlockstate, pos.setY(surfaceY - 1 - y + yOff), adjustedBox, false);
        }
    }

    protected void embedSurfaceBlocks(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int yOff, int z, BoundingBox box, BlockState... blocks) {
        int surfaceY = getSurfaceY(level, x, z);
        pos.setX(getWorldX(x, z)).setZ(getWorldZ(x, z));
        BoundingBox adjustedBox = new BoundingBox(box.minX(), Math.max(1, box.minY() + yOff - blocks.length), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
        for(int y = 0; y < blocks.length; y++) {
            placeBlock(level, blocks[y], pos.setY(surfaceY - 1 - y + yOff), adjustedBox, false);
        }
    }

    protected int getSurfaceY(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, getWorldX(x, z), getWorldZ(x, z));
    }

    protected static int getWorldSurfaceY(WorldGenLevel level, int worldX, int worldZ) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ);
    }

    protected BoundingBox getSurfaceBox(WorldGenLevel level, BoundingBox box) {
        return new BoundingBox(box.minX(),
                Math.min(Math.min(getWorldSurfaceY(level, box.minX(), box.minZ()), getWorldSurfaceY(level, box.minX(), box.maxZ())),
                        Math.min(getWorldSurfaceY(level, box.maxX(), box.minZ()), getWorldSurfaceY(level, box.maxX(), box.maxZ()))),
                box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }

    protected BoundingBox adjustBoxMinY(BoundingBox box, int y) {
        if(y < 0) return new BoundingBox(box.minX(), Math.max(1, box.minY() + y), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
        else return box;
    }

    protected void placeSurfaceBlock(WorldGenLevel level, BlockState pBlockstate, BlockPos.MutableBlockPos pos, int x, int y, int z, BoundingBox box) {
        int worldX = getWorldX(x, z), worldZ = getWorldZ(x, z);
        placeBlock(level, pBlockstate, pos.set(worldX, y + level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ), worldZ),
                adjustBoxMinY(box, y), y == 0);
    }

    protected void placeStrangeSoil(WorldGenLevel level, BoundingBox box, Random random, BlockPos worldPos, BlockState strangeSoilBlock, ResourceLocation lootTable) {
        if(box.isInside(worldPos)) {
            BlockState block = level.getBlockState(worldPos);
            if(block.is(TagsNF.STRUCTURE_REPLACEABLE)) {
                level.setBlock(worldPos, strangeSoilBlock, 2);
                if(level.getBlockEntity(worldPos) instanceof StrangeSoilBlockEntity entity) {
                    entity.lootTableSeed = random.nextLong();
                    entity.lootTableLoc = lootTable;
                }
            }
        }
    }

    protected void placeContainer(WorldGenLevel level, BoundingBox box, Random random, int x, int y, int z, BlockState block, ResourceLocation lootTable, boolean orderedLoot) {
        BlockPos worldPos = getWorldPos(x, y, z);
        if(box.isInside(worldPos) && level.getBlockEntity(worldPos) == null) {
            if(getMirror() != Mirror.NONE) block = block.mirror(getMirror());
            if(getRotation() != Rotation.NONE) block = block.rotate(getRotation());
            level.setBlock(worldPos, block, 2);
            if(level.getBlockEntity(worldPos) instanceof RandomizableContainerBlockEntity entity) {
                entity.setLootTable(lootTable, random.nextLong());
                if(entity instanceof MenuContainerBlockEntity menuEntity) menuEntity.orderedLoot = orderedLoot;
            }
        }
    }

    protected boolean updateHeightAverage(LevelAccessor level, int pHeight) {
        int sum = 0;
        int count = 0;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for(int z = boundingBox.minZ(); z <= boundingBox.maxZ(); z++) {
            for(int x = boundingBox.minX(); x <= boundingBox.maxX(); x++) {
                blockpos$mutableblockpos.set(x, 0, z);
                sum += level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockpos$mutableblockpos).getY();
                count++;
            }
        }

        if(count == 0) return false;
        else {
            boundingBox.move(0, sum / count - boundingBox.minY() + pHeight, 0);
            return true;
        }
    }

    protected boolean updateHeightMin(LevelAccessor level, int yOffset) {
        int minHeight = level.getMaxBuildHeight();
        boolean foundAny = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for(int j = boundingBox.minZ(); j <= boundingBox.maxZ(); ++j) {
            for(int k = boundingBox.minX(); k <= boundingBox.maxX(); ++k) {
                pos.set(k, 0, j);
                minHeight = Math.min(minHeight, level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY());
                foundAny = true;
            }
        }

        if(!foundAny) return false;
        else {
            boundingBox.move(0, minHeight - boundingBox.minY() + yOffset, 0);
            return true;
        }
    }
}
