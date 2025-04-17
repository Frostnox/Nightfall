package frostnox.nightfall.world.generation.feature;

import com.mojang.math.Vector3d;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.math.noise.SimplexNoiseCached;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class OreVeinFeature extends Feature<OreVeinFeature.Configuration> {
    //Key and value lists function as a map and must be the same size and order
    //Radius base + (rand - 1) must not exceed 16 blocks to ensure feature stays within 3x3 chunk bounds
    public record Configuration(List<BlockState> oreKeys, List<BlockState> oreValues, double threshold,
                                float richnessBase, float richnessRand, int xzRadBase, int xzRadRand,
                                int yRadBase, int yRadRand, double xzFreq, double yFreq) implements FeatureConfiguration {}
    public static final Codec<Configuration> CODEC = RecordCodecBuilder.create((instance -> instance.group(
            BlockState.CODEC.listOf().fieldOf("oreKeys").forGetter(config -> config.oreKeys),
            BlockState.CODEC.listOf().fieldOf("oreValues").forGetter(config -> config.oreValues),
            Codec.DOUBLE.fieldOf("threshold").forGetter(config -> config.threshold),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("richnessBase").forGetter(config -> config.richnessBase),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("richnessRand").forGetter(config -> config.richnessRand),
            ExtraCodecs.POSITIVE_INT.fieldOf("xzRadBase").forGetter(config -> config.xzRadBase),
            ExtraCodecs.POSITIVE_INT.fieldOf("xzRadRand").forGetter(config -> config.xzRadRand),
            ExtraCodecs.POSITIVE_INT.fieldOf("yRadBase").forGetter(config -> config.yRadBase),
            ExtraCodecs.POSITIVE_INT.fieldOf("yRadRand").forGetter(config -> config.yRadRand),
            Codec.DOUBLE.fieldOf("xzFreq").forGetter(config -> config.xzFreq),
            Codec.DOUBLE.fieldOf("yFreq").forGetter(config -> config.yFreq)
    ).apply(instance, Configuration::new)));
    protected static final Map<Configuration, Pair<SimplexNoiseCached, SimplexNoiseCached>> NOISE = new Object2ObjectArrayMap<>();
    protected long levelSeed;

    public OreVeinFeature(String name) {
        super(CODEC);
        setRegistryName(name);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        Configuration config = context.config();
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        Random random = context.random();
        if(level.getSeed() != levelSeed) {
            NOISE.clear();
            levelSeed = level.getSeed();
        }
        Pair<SimplexNoiseCached, SimplexNoiseCached> noisePair = NOISE.get(config);
        if(noisePair == null) {
            noisePair = Pair.of(new SimplexNoiseCached(random.nextLong()), new SimplexNoiseCached(random.nextLong()));
            NOISE.put(config, noisePair);
        }
        int sectionX = SectionPos.blockToSectionCoord(origin.getX()), sectionZ = SectionPos.blockToSectionCoord(origin.getZ());
        ChunkAccess chunk = level.getChunk(sectionX, sectionZ);
        if(chunk.isOutsideBuildHeight(origin.getY())) return false;
        int sectionIndex = chunk.getSectionIndex(origin.getY());
        LevelChunkSection section = chunk.getSection(sectionIndex);
        //Avoid placing in areas where the resulting vein would be small regardless
        if(section.hasOnlyAir()) return false;
        //Dimensions of ellipsoid
        int xRad = config.xzRadBase + random.nextInt(config.xzRadRand);
        double xRadSqr = xRad * xRad;
        int yRad = config.yRadBase + random.nextInt(config.yRadRand);
        double yRadSqr = yRad * yRad;
        int zRad = config.xzRadBase + random.nextInt(config.xzRadRand);
        double zRadSqr = zRad * zRad;
        int minX = origin.getX() - xRad, minZ = origin.getZ() - zRad, minY = Math.max(origin.getY() - yRad, level.getMinBuildHeight());
        int maxX = origin.getX() + xRad, maxZ = origin.getZ() + zRad, maxY = Math.min(origin.getY() + yRad, level.getMaxBuildHeight() - 1);
        float richness = config.richnessBase + config.richnessRand * random.nextFloat();
        SimplexNoiseCached.DirectionalGenerator noiseGen1 = noisePair.left().directionalGenerator();
        SimplexNoiseCached.DirectionalGenerator noiseGen2 = noisePair.right().directionalGenerator();
        Vector3d noise1Dir = new Vector3d(0, 0, 0);
        Vector3d noise2Dir = new Vector3d(0, 0, 0);
        //Sample noise within bounds of ellipsoid
        //3D noise is expensive so limiting the sampled positions is key to performance
        //Note: two different chunks can create single, large veins if their y coords are similar since they use the same noise source
        for(int x = minX; x <= maxX; x++) {
            int xDist = origin.getX() - x;
            double xFactor = xDist * xDist / xRadSqr;
            double noiseX = x * config.xzFreq;
            int newSectionX = SectionPos.blockToSectionCoord(x);
            boolean newChunk = false;
            if(newSectionX != sectionX) {
                sectionX = newSectionX;
                newChunk = true;
            }
            int localX = x & 15;
            for(int z = minZ; z <= maxZ; z++) {
                int zDist = origin.getZ() - z;
                double zFactor = zDist * zDist / zRadSqr;
                double xzFactor = xFactor + zFactor;
                if(xzFactor > 1) continue;
                double noiseZ = z * config.xzFreq;
                int newSectionZ = SectionPos.blockToSectionCoord(z);
                if(newSectionZ != sectionZ) {
                    sectionZ = newSectionZ;
                    newChunk = true;
                }
                int localZ = z & 15;
                noiseGen1.setXZ(noiseX, noiseZ);
                noiseGen2.setXZ(noiseX, noiseZ);
                int surfaceY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ);
                for(int y = minY; y <= maxY; y++) {
                    //Skip block at surface to reduce generation in highly exposed areas
                    if(chunk.isOutsideBuildHeight(y) || y >= surfaceY) continue;
                    int yDist = origin.getY() - y;
                    if(xzFactor + yDist * yDist / yRadSqr > 1 || random.nextFloat() > richness) continue;
                    int newSectionIndex = chunk.getSectionIndex(y);
                    if(newSectionIndex != sectionIndex || newChunk) {
                        if(newChunk) {
                            chunk = level.getChunk(sectionX, sectionZ);
                            newChunk = false;
                        }
                        sectionIndex = newSectionIndex;
                        section = chunk.getSection(sectionIndex);
                    }
                    int localY = y & 15;
                    int index = config.oreKeys.indexOf(section.getBlockState(localX, localY, localZ));
                    if(index == -1) continue;
                    double noise1Y = y * config.yFreq;
                    double val1 = noiseGen1.getForY(noise1Y);
                    double density = val1 * val1;
                    if(density < config.threshold) {
                        double noise2Y = noise1Y + MathUtil.IDEAL_OPENSIMPLEX2_PAIR_VERTICAL_SAMPLING_OFFSET;
                        double val2 = noiseGen2.getForY(noise2Y);
                        density += val2 * val2;
                        if(density < config.threshold) {
                            noiseGen1.getDirection(noise1Dir, noise1Y);
                            noiseGen2.getDirection(noise2Dir, noise2Y);
                            density += MathUtil.getCaveClosingValue(noise1Dir, noise2Dir) * config.threshold * 8D;
                            if(density < config.threshold) section.setBlockState(localX, localY, localZ, config.oreValues.get(index), false);
                        }
                    }
                }
            }
        }
        return true;
    }
}