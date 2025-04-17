package frostnox.nightfall.world.generation.feature;

import frostnox.nightfall.block.Stone;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Random;

public class MeteoriteFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockState STONE = BlocksNF.STONE_BLOCKS.get(Stone.MOONSTONE).get().defaultBlockState();
    private static final BlockState ORE = BlocksNF.METEORITE_ORE.get().defaultBlockState();

    public MeteoriteFeature(String name) {
        super(NoneFeatureConfiguration.CODEC);
        setRegistryName(name);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos.MutableBlockPos center = context.origin().mutable();
        WorldGenLevel level = context.level();
        Random random = context.random();
        while(!level.getBlockState(center).is(TagsNF.STRUCTURE_REPLACEABLE)) {
            center.setY(center.getY() - 1);
            if(center.getY() <= level.getMinBuildHeight()) return false;
        }
        int yRad;
        float f = random.nextFloat();
        if(f < 0.45F) yRad = 2;
        else if(f < 0.85F) yRad = 3;
        else if(f < 0.97F) yRad = 4;
        else yRad = 5;
        center.setY(center.getY() - 1 - random.nextInt(yRad));
        if(center.getY() - yRad <= level.getMinBuildHeight() || center.getY() + yRad > level.getMaxBuildHeight()) return false;
        else {
            int xRad = random.nextBoolean() ? yRad + 1 : yRad, zRad = random.nextBoolean() ? yRad + 1 : yRad;
            double xRadSqr = xRad * xRad, yRadSqr = yRad * yRad, zRadSqr = zRad * zRad;
            int xMin = center.getX() - xRad + 1, xMax = center.getX() + xRad - 1;
            int zMin = center.getZ() - zRad + 1, zMax = center.getZ() + zRad - 1;
            int yMin = center.getY() - yRad + 1, yMax = center.getY() + yRad - 1;
            //Ensure meteorite is reasonably embedded
            if(Math.abs(center.getY() - level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, xMin, zMin)) >= yRad - 1) return false;
            if(Math.abs(center.getY() - level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, xMax, zMin)) >= yRad - 1) return false;
            if(Math.abs(center.getY() - level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, xMin, zMax)) >= yRad - 1) return false;
            if(Math.abs(center.getY() - level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, xMax, zMax)) >= yRad - 1) return false;
            int sectionX = SectionPos.blockToSectionCoord(center.getX()), sectionZ = SectionPos.blockToSectionCoord(center.getZ());
            ChunkAccess chunk = level.getChunk(sectionX, sectionZ);
            int sectionIndex = chunk.getSectionIndex(center.getY());
            LevelChunkSection section = chunk.getSection(sectionIndex);
            Heightmap motionBlockingMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING);
            Heightmap noLeavesMotionBlockingMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);
            Heightmap oceanMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR);
            Heightmap worldMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE);
            for(int x = xMin; x <= xMax; x++) {
                int xDist = center.getX() - x;
                double xFactor = xDist * xDist / xRadSqr;
                boolean maybeSkipX = x == xMin || x == xMax;
                int newSectionX = SectionPos.blockToSectionCoord(x);
                boolean newChunk = false;
                if(newSectionX != sectionX) {
                    sectionX = newSectionX;
                    newChunk = true;
                }
                int localX = x & 15;
                for(int z = zMin; z <= zMax; z++) {
                    boolean maybeSkipZ = z == zMin || z == zMax;
                    int zDist = center.getZ() - z;
                    double xzFactor = xFactor + (zDist * zDist / zRadSqr);
                    if(xzFactor > 1) continue;
                    int newSectionZ = SectionPos.blockToSectionCoord(z);
                    if(newSectionZ != sectionZ) {
                        sectionZ = newSectionZ;
                        newChunk = true;
                    }
                    int localZ = z & 15;
                    for(int y = yMin; y <= yMax; y++) {
                        if(maybeSkipX && maybeSkipZ && (y == yMin || y == yMax)) continue;
                        int yDist = center.getY() - y;
                        double dist = xzFactor + (yDist * yDist / yRadSqr);
                        if(dist > 1) continue;
                        else if(dist >= 0.8 && random.nextInt(3) == 0) continue;
                        int newSectionIndex = chunk.getSectionIndex(y);
                        if(newSectionIndex != sectionIndex || newChunk) {
                            if(newChunk) {
                                chunk = level.getChunk(sectionX, sectionZ);
                                newChunk = false;
                                motionBlockingMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING);
                                noLeavesMotionBlockingMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);
                                oceanMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR);
                                worldMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE);
                            }
                            sectionIndex = newSectionIndex;
                            section = chunk.getSection(sectionIndex);
                        }
                        int localY = y & 15;
                        if(section.getBlockState(localX, localY, localZ).is(TagsNF.STRUCTURE_REPLACEABLE)) {
                            section.setBlockState(localX, localY, localZ, dist <= 0.2 && random.nextBoolean() ? ORE : STONE, false);
                            motionBlockingMap.update(localX, y, localZ, STONE);
                            noLeavesMotionBlockingMap.update(localX, y, localZ, STONE);
                            oceanMap.update(localX, y, localZ, STONE);
                            worldMap.update(localX, y, localZ, STONE);
                        }
                    }
                }
            }
            return true;
        }
    }
}
