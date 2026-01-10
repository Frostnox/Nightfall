package frostnox.nightfall.world.generation;

import com.google.common.collect.Sets;
import com.mojang.math.Vector3d;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.block.*;
import frostnox.nightfall.block.block.SoilBlock;
import frostnox.nightfall.registry.forge.BiomesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.DataUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.data.WrappedBool;
import frostnox.nightfall.util.data.WrappedInt;
import frostnox.nightfall.util.math.Easing;
import frostnox.nightfall.util.math.Spline;
import frostnox.nightfall.util.math.noise.FractalSimplexNoiseCached;
import frostnox.nightfall.util.math.noise.FractalSimplexNoiseFast;
import frostnox.nightfall.util.math.noise.SimplexNoiseCached;
import frostnox.nightfall.world.StoneGroup;
import frostnox.nightfall.world.biome.ContinentalBiomeSource;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import static frostnox.nightfall.util.math.Spline.point;

public class ContinentalChunkGenerator extends ChunkGenerator {
    public static final Codec<ContinentalChunkGenerator> CODEC = RecordCodecBuilder.create((instance) ->
            commonCodec(instance).and(instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((gen) -> gen.biomeSource),
            Codec.LONG.fieldOf("seed").stable().forGetter((gen) -> gen.seed)))
            .apply(instance, instance.stable(ContinentalChunkGenerator::new)));
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    private static final BlockState BEDROCK = BlocksNF.BEDROCK.get().defaultBlockState();
    private static final BlockState WATER = BlocksNF.WATER.get().defaultBlockState();
    private static final BlockState SEAWATER = BlocksNF.SEAWATER.get().defaultBlockState();
    private static final BlockState LAVA = BlocksNF.LAVA.get().defaultBlockState();
    private static final BlockState SILT = BlocksNF.SILT.get().defaultBlockState();
    private static final BlockState DIRT = BlocksNF.DIRT.get().defaultBlockState();
    private static final BlockState LOAM = BlocksNF.LOAM.get().defaultBlockState();
    private static final Map<Soil, BlockState> SOILS = DataUtil.mapEnum(Soil.class, type -> switch(type) {
        case SILT -> BlocksNF.SILT.get().defaultBlockState();
        case DIRT -> BlocksNF.DIRT.get().defaultBlockState();
        case LOAM -> BlocksNF.LOAM.get().defaultBlockState();
        case ASH -> BlocksNF.ASH.get().defaultBlockState();
        case GRAVEL -> BlocksNF.GRAVEL.get().defaultBlockState();
        case BLUE_GRAVEL -> BlocksNF.BLUE_GRAVEL.get().defaultBlockState();
        case BLACK_GRAVEL -> BlocksNF.BLACK_GRAVEL.get().defaultBlockState();
        case SAND -> BlocksNF.SAND.get().defaultBlockState();
        case RED_SAND -> BlocksNF.RED_SAND.get().defaultBlockState();
        case WHITE_SAND -> BlocksNF.WHITE_SAND.get().defaultBlockState();
    });
    public static final float LOW_CLIMATE = 0.3F, HIGH_CLIMATE = 0.7F;
    private static final float PILLAR_Y = 0.12F;
    public static final int SEA_LEVEL = 416, LAVA_LEVEL = 16;
    public static final int MIN_Y = 0, MAX_Y = 832;
    private static final ThreadLocal<Object2FloatMap<ChunkPos>> temperatureCache = new ThreadLocal<>();
    private static final ThreadLocal<Object2FloatMap<ChunkPos>> humidityCache = new ThreadLocal<>();
    private static final ThreadLocal<Object2FloatMap<ChunkPos>> exposureCache = new ThreadLocal<>();
    private static final ThreadLocal<Map<ChunkPos, StoneGroup>> stoneGroupCache = new ThreadLocal<>();
    private static final ThreadLocal<Map<ChunkPos, Stone>> surfaceStoneCache = new ThreadLocal<>();
    private static final ThreadLocal<Map<ChunkPos, Soil>> soilCache = new ThreadLocal<>();
    private static final ThreadLocal<Map<ChunkPos, TreePool>> treePoolCache = new ThreadLocal<>();
    protected final long seed;
    protected final FractalSimplexNoiseFast elevation; //Large-scale shapes - oceans, continents, islands
    protected final FractalSimplexNoiseFast roughness; //Small-scale shapes - hills, lakes, mountains
    protected final FractalSimplexNoiseFast detail; //Amplifier - expands/reduces roughness
    protected final FractalSimplexNoiseCached weathering; //3D surface formations
    protected final FractalSimplexNoiseFast exposure; //Amplifier - expands/reduces weathering
    protected final FractalSimplexNoiseFast temperature, humidity; //Biome factors
    protected final FractalSimplexNoiseFast forestation, clearing, tree1, tree2, tree3, tree4; //Tree factors
    protected final FractalSimplexNoiseFast clayPatches, mudPatches;
    protected final SimplexNoiseCached bigTunnels1, bigTunnels2, smallTunnels1, smallTunnels2; //Long winding caves with varying size, concentrated at top
    protected final FractalSimplexNoiseFast bigTunnelsWarp, smallTunnelsWarp; //Warp tunnels to make them more irregular
    protected final FractalSimplexNoiseCached caverns; //Big open caves, concentrated at bottom
    protected final FractalSimplexNoiseCached pillars; //Pillars for caverns
    protected final FractalSimplexNoiseFast bedrock, bedrockCover; //Height for bedrock layer
    protected final FractalSimplexNoiseFast stoneType; //Independent variable to randomize stone selection a bit
    protected final FractalSimplexNoiseFast igneousHeight, metamorphicHeight; //Warp height to simulate stone deposits on top of different terrain
    protected final Spline elevationSpline, roughnessSpline, detailSpline, exposureSpline;

    public ContinentalChunkGenerator(Registry<StructureSet> structureSets, BiomeSource biomeSource, long seed) {
        super(structureSets, Optional.empty(), biomeSource, biomeSource, seed);
        if(biomeSource instanceof ContinentalBiomeSource cBiomeSource) cBiomeSource.generator = this;
        this.seed = seed;
        WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(seed));
        this.elevation = new FractalSimplexNoiseFast(random.nextLong(), 0.00017F, 8, 0.55F, 2.0F);
        this.roughness = new FractalSimplexNoiseFast(random.nextLong(), 0.0007F, 7, 0.5F, 2.0F);
        this.detail = new FractalSimplexNoiseFast(random.nextLong(), 0.0025F, 3, 0.5F, 2.0F);
        this.weathering = new FractalSimplexNoiseCached(random.nextLong(), 0.0056F, 0.64F, 2.0F);
        this.exposure = new FractalSimplexNoiseFast(random.nextLong(), 0.00065F, 6, 0.5F, 2.0F);
        this.temperature = new FractalSimplexNoiseFast(random.nextLong(), 0.0002F, 7, 0.5F, 2.0F);
        this.humidity = new FractalSimplexNoiseFast(random.nextLong(), 0.0002F, 7, 0.5F, 2.0F);
        this.forestation = new FractalSimplexNoiseFast(random.nextLong(), 0.0002F, 7, 0.5F, 2.0F);
        this.clearing = new FractalSimplexNoiseFast(random.nextLong(), 0.003F, 3, 0.5F, 2.0F);
        tree1 = new FractalSimplexNoiseFast(random.nextLong(), 0.0002F, 7, 0.5F, 2.0F);
        tree2 = new FractalSimplexNoiseFast(random.nextLong(), 0.0002F, 7, 0.5F, 2.0F);
        tree3 = new FractalSimplexNoiseFast(random.nextLong(), 0.0002F, 7, 0.5F, 2.0F);
        tree4 = new FractalSimplexNoiseFast(random.nextLong(), 0.0002F, 7, 0.5F, 2.0F);
        this.clayPatches = new FractalSimplexNoiseFast(random.nextLong(), 0.011F, 2, 0.5F, 2.0F);
        this.mudPatches = new FractalSimplexNoiseFast(random.nextLong(), 0.011F, 2, 0.5F, 2.0F);
        this.bigTunnelsWarp = new FractalSimplexNoiseFast(random.nextLong(), 0.014F, 2, 0.5F, 2.0F);
        this.smallTunnelsWarp = new FractalSimplexNoiseFast(random.nextLong(), 0.011F, 2, 0.5F, 2.0F);
        this.bigTunnels1 = new SimplexNoiseCached(random.nextLong());
        this.bigTunnels2 = new SimplexNoiseCached(random.nextLong());
        this.smallTunnels1 = new SimplexNoiseCached(random.nextLong());
        this.smallTunnels2 = new SimplexNoiseCached(random.nextLong());
        this.caverns = new FractalSimplexNoiseCached(random.nextLong(), 0.0063F, 0.5F, 2.0F);
        this.pillars = new FractalSimplexNoiseCached(random.nextLong(), 0.06F, 0.5F, 2.0F);
        this.bedrock = new FractalSimplexNoiseFast(random.nextLong(), 0.012F, 3, 0.52F, 2.24F);
        this.bedrockCover = new FractalSimplexNoiseFast(random.nextLong(), 0.016F, 3, 0.52F, 2.24F);
        this.stoneType = new FractalSimplexNoiseFast(random.nextLong(), 0.0002F, 7, 0.5F, 2.0F);
        this.igneousHeight = new FractalSimplexNoiseFast(random.nextLong(), 0.0002F, 7, 0.5F, 2.0F);
        this.metamorphicHeight = new FractalSimplexNoiseFast(random.nextLong(), 0.0002F, 7, 0.5F, 2.0F);
        this.elevationSpline = new Spline(point(-1, 423), point(-0.67, 423), point(-0.57, 364, Easing.inSine), point(-0.505, 64),
                point(0.115, 88), point(0.178, 364, Easing.inSine), point(0.278, 423, Easing.inSine), point(1.0, 423));
        this.roughnessSpline = new Spline(point(-1.0, 416), point(-0.7, 380),
                point(-0.3, 80, Easing.inOutSine), point(-0.1, 35, Easing.inOutSine),  point(0.07, 17),
                point(0.3, -12, Easing.inSine), point(0.45, 90, Easing.inOutSine), point(0.5, 96), point(1, 0, Easing.inSine));
        this.detailSpline = new Spline(point(-1, 0), point(-0.65, 0.2, Easing.inOutSine), point(0, 0.5, Easing.inOutSine),
                point(0.7, 1, Easing.inOutSine), point(1, 1, Easing.outSine));
        this.exposureSpline = new Spline(point(-1, 2), point(-0.2, 2), point(0, 0.5),
                point(0.2, 0), point(1, 0));

        if(false) {
            int size = 100000;
            float pass = 0, fail = 0;
            double threshold = 0.213;
            for(int x = 0; x < size; x++) {
                float noise = tree1.noise2D(random.nextInt(), random.nextInt());
                if(noise < -threshold || noise > threshold) pass++;
                else fail++;
            }
            System.out.println(pass / (pass + fail));
        }
        if(false) {
            int size = 2000;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            File output = new File("D:/mods/nightfall/generated/height" + seed + ".png");
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    int height = getHeight(x * 8, z * 8);
                    int r = elevation.noise2D(x * 8, z * 8) < -0.57 ? 255 : 0;
                    int g = height > SEA_LEVEL ? 128 + (int) (((double) (height - SEA_LEVEL) / (double) 500) * 127D) : 0;
                    int b = height <= SEA_LEVEL ? 255 - (int) ((1D - (double) height / (double) SEA_LEVEL) * 191D) : 0;
                    image.setRGB(x, z, new Color(r, g, b).getRGB());
                }
            }
            try {
                ImageIO.write(image, "png", output);
            } catch (IOException e) {
                System.out.println("Failed to draw height map.");
            }
        }
        if(false) {
            int size = 2000;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            File output = new File("D:/mods/nightfall/generated/climate" + seed + ".png");
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    int height = getHeight(x * 8, z * 8);
                    int r = (int) (getTemperature(x * 8, z * 8) * 255);
                    int g = (int) (getHumidity(x * 8, z * 8) * 255);
                    image.setRGB(x, z, height <= SEA_LEVEL ? Color.BLUE.getRGB() : new Color(r, g, 0).getRGB());
                }
            }
            try {
                ImageIO.write(image, "png", output);
            } catch (IOException e) {
                System.out.println("Failed to draw climate map.");
            }
        }
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return new ContinentalChunkGenerator(structureSets, biomeSource.withSeed(seed), seed);
    }

    @Override
    public Climate.Sampler climateSampler() {
        return Climate.empty();
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor level) {
        return SEA_LEVEL + 1;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long pSeed, BiomeManager pBiomeManager, StructureFeatureManager pStructureFeatureManager, ChunkAccess pChunk, GenerationStep.Carving pStep) {

    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureFeatureManager featureManager, ChunkAccess chunk) {

    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess pChunk, StructureFeatureManager pStructureFeatureManager) {
        temperatureCache.set(new Object2FloatArrayMap<>(4));
        humidityCache.set(new Object2FloatArrayMap<>(4));
        exposureCache.set(new Object2FloatArrayMap<>(4));
        stoneGroupCache.set(new Object2ObjectArrayMap<>(4));
        surfaceStoneCache.set(new Object2ObjectArrayMap<>(4));
        soilCache.set(new Object2ObjectArrayMap<>(4));
        treePoolCache.set(new Object2ObjectArrayMap<>(4));
        super.applyBiomeDecoration(level, pChunk, pStructureFeatureManager);
        temperatureCache.remove();
        humidityCache.remove();
        exposureCache.remove();
        stoneGroupCache.remove();
        surfaceStoneCache.remove();
        soilCache.remove();
        treePoolCache.remove();
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {

    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunk) {
        int maxIndex = chunk.getSectionIndex(MAX_Y - 1);
        int minIndex = chunk.getSectionIndex(MIN_Y);
        Set<LevelChunkSection> set = Sets.newHashSet();
        for(int i = maxIndex; i >= minIndex; i--) {
            LevelChunkSection chunkSection = chunk.getSection(i);
            chunkSection.acquire();
            set.add(chunkSection);
        }
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_fill_noise", () -> {
            return fill(chunk);
        }), Util.backgroundExecutor()).whenCompleteAsync((p_209132_, p_209133_) -> {
            for(LevelChunkSection chunkSection : set) chunkSection.release();
        }, executor);
    }

    public float getElevation(int worldX, int worldZ) {
        return elevation.noise2D(worldX, worldZ);
    }

    public float getCachedTemperature(ChunkPos pos) {
        var map = temperatureCache.get();
        if(map.containsKey(pos)) return map.getFloat(pos);
        else {
            float value = getTemperature(pos.getMinBlockX(), pos.getMinBlockZ());
            map.put(pos, value);
            return value;
        }
    }

    public float getCachedHumidity(ChunkPos pos) {
        var map = humidityCache.get();
        if(map.containsKey(pos)) return map.getFloat(pos);
        else {
            float value = getHumidity(pos.getMinBlockX(), pos.getMinBlockZ());
            map.put(pos, value);
            return value;
        }
    }

    public float getCachedExposure(ChunkPos pos) {
        var map = exposureCache.get();
        if(map.containsKey(pos)) return map.getFloat(pos);
        else {
            float value = exposure.noise2D(pos.getMinBlockX(), pos.getMinBlockZ());
            map.put(pos, value);
            return value;
        }
    }

    public StoneGroup getCachedStoneGroup(ChunkPos pos) {
        var map = stoneGroupCache.get();
        if(map.containsKey(pos)) return map.get(pos);
        else {
            StoneGroup value = getStoneGroup(getCachedTemperature(pos), stoneType.noise2D(pos.getMiddleBlockX(), pos.getMiddleBlockZ()));
            map.put(pos, value);
            return value;
        }
    }

    public Stone getCachedSurfaceStone(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        var map = surfaceStoneCache.get();
        if(map.containsKey(chunkPos)) return map.get(chunkPos);
        else {
            float igneousHeight = getIgneousHeight(chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ(), pos.getY());
            Stone value = (Stone) getStoneType(pos.getY(), getCachedStoneGroup(chunkPos),
                    igneousHeight, getMetamorphicHeight(chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ(), pos.getY(), igneousHeight));
            map.put(chunkPos, value);
            return value;
        }
    }

    public Soil getCachedSoil(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        var map = soilCache.get();
        if(map.containsKey(chunkPos)) return map.get(chunkPos);
        else {
            int soilDepth = getSoilDepth(pos.getY(), getCachedHumidity(chunkPos), getCachedExposure(chunkPos));
            int soilQuality = getSoilQuality(soilDepth, getCachedTemperature(chunkPos), getCachedHumidity(chunkPos));
            Soil value = switch(soilQuality) {
                case 1 -> Soil.SILT;
                case 2 -> Soil.DIRT;
                case 3 -> Soil.LOAM;
                default -> (Soil) getCachedSurfaceStone(pos).getSoil();
            };
            map.put(chunkPos, value);
            return value;
        }
    }

    public TreePool getCachedTreePool(ChunkPos pos) {
        var map = treePoolCache.get();
        if(map.containsKey(pos)) return map.get(pos);
        else {
            TreePool value = getTreePool(pos.getMiddleBlockX(), pos.getMiddleBlockZ(), getCachedTemperature(pos), getCachedHumidity(pos));
            map.put(pos, value);
            return value;
        }
    }

    public TreePool getTreePool(int x, int z, float temp, float humidity) {
        TreePool.Entry[] trees = new TreePool.Entry[4];
        Tree primary = null, secondary = null, tertiary = null, quaternary = null;
        float noise = tree1.noise2D(x, z);
        if(noise < -0.14 || noise > 0.14) primary = Tree.chooseTree(Tree.PRIMARY_TREES, temp, humidity); //0.7
        noise = tree2.noise2D(x, z);
        if(noise < -0.164 || noise > 0.164) secondary = Tree.chooseTree(Tree.SECONDARY_TREES, temp, humidity); //0.65
        noise = tree3.noise2D(x, z);
        if(noise < -0.213 || noise > 0.213) tertiary = Tree.chooseTree(Tree.TERTIARY_TREES, temp, humidity); //0.55
        noise = tree4.noise2D(x, z);
        if(noise < -0.293 || noise > 0.293) quaternary = Tree.chooseTree(Tree.QUATERNARY_TREES, temp, humidity); //0.4
        int totalWeight = 0;
        int size = 0;
        if(primary != null) {
            int weight = 4;
            totalWeight += weight;
            trees[size] = new TreePool.Entry(primary, weight);
            size++;
        }
        if(secondary != null) {
            int weight = 3;
            totalWeight += weight;
            trees[size] = new TreePool.Entry(secondary, weight);
            size++;
        }
        if(tertiary != null) {
            int weight = 2;
            totalWeight += weight;
            trees[size] = new TreePool.Entry(tertiary, weight);
            size++;
        }
        if(quaternary != null) {
            int weight = 1;
            totalWeight += weight;
            trees[size] = new TreePool.Entry(quaternary, weight);
            size++;
        }
        return new TreePool(trees, size, totalWeight);
    }

    /**
     * @return temperature with standard uniform distribution, strictly 0 to 1
     */
    public float getTemperature(int x, int z) {
        float noise = temperature.noise2D(x, z), noise2 = noise * noise, noise3 = noise2 * noise, noise4 = noise3 * noise;
        noise = 0.401713F * noise4 * noise + 0.000344293F * noise4 - 1.01173F * noise3 - 0.000535267F * noise2 + 1.10971F * noise + 0.500192F;
        if(noise > 1F) noise = 1F;
        else if(noise < 0F) noise = 0F;
        return noise;
    }

    /**
     * @return humidity with standard uniform distribution, strictly 0 to 1
     */
    public float getHumidity(int x, int z) {
        float noise = humidity.noise2D(x, z), noise2 = noise * noise, noise3 = noise2 * noise, noise4 = noise3 * noise;
        noise = 0.401713F * noise4 * noise + 0.000344293F * noise4 - 1.01173F * noise3 - 0.000535267F * noise2 + 1.10971F * noise + 0.500192F;
        if(noise > 1F) noise = 1F;
        else if(noise < 0F) noise = 0F;
        return noise;
    }

    public float getForestation(int x, int z) {
        return forestation.noise2D(x, z);
    }

    public float getClearing(int x, int z) {
        return clearing.noise2D(x, z);
    }

    public boolean isClearing(int x, int z) {
        return Math.abs(clearing.noise2D(x, z)) > 0.55F;
    }

    private int getHeight(float elevation, float roughness, float detail) {
        return (int) (elevationSpline.fit(elevation) + roughnessSpline.fit(roughness) * detailSpline.fit(detail));
    }

    public int getHeight(int x, int z) {
        //return (int) (elevationSpline.fit(elevation.noise2D(x, z)) + roughnessSpline.fit(roughness.noise2D(x, z)) * ((detail.noise2D(x, z) + 1) * 0.7));
        return getHeight(elevation.noise2D(x, z), roughness.noise2D(x, z), detail.noise2D(x, z));
        //return (int) (elevationSpline.fit(elevation.noise2D(x, z)) + roughnessSpline.fit(roughness.noise2D(x, z)));
    }

    private StoneGroup getStoneGroup(float temperature, float noise) {
        //Pick igneous group
        float groupValue = 1F; //0.5 = stygfel, 1 = deepslate, 1.5 = basalt, 2 = granite
        groupValue += noise * 0.8F + (temperature - 0.5F) * 0.8F;
        //Favor limestone near oceans & beaches
        //if(groupValue < 1F && humidity > 0.5F && biome.is(BiomesNF.OCEAN.getKey())) groupValue += 0.8F * (humidity - 0.5F);
        return groupValue > 1.5F ? StoneGroup.GRANITE : (groupValue < 0.5F ? StoneGroup.STYGFEL : (groupValue < 1F ? StoneGroup.DEEPSLATE : StoneGroup.BASALT));
    }

    private float getIgneousHeight(int x, int z, int height) {
        float randHeight = this.igneousHeight.noise2D(x, z);
        float igneousOffset = randHeight > 0F ? (height - SEA_LEVEL + randHeight * 160F) * 0.07F : (height - SEA_LEVEL) * 0.07F;
        return 128 + igneousOffset * igneousOffset;
    }

    private float getMetamorphicHeight(int x, int z, int height, float igneousHeight) {
        float randHeight = this.metamorphicHeight.noise2D(x, z);
        float metamorphicOffset = randHeight > 0F ? (height - SEA_LEVEL + randHeight * 160F) * 0.07F : (height - SEA_LEVEL) * 0.07F;
        return igneousHeight + 128 + metamorphicOffset * metamorphicOffset;
    }

    private float adjustCavernsNoise(float noise, int y, int height) {
        /*if(height < SEA_LEVEL - 32) { //Close caverns off past continental shelf
            if(y > height - 8F) noise = 0F;
            else if(y > height - 16F) noise *= 1F - ((y - (height - 16)) / 8D);
        }*/
        //Shrink size linearly as height increases
        if(y > 63) noise *= Math.max(0F, 1F - (((float) (y - 63)) / (height - 63)) * 1.4F);
        return noise;
    }

    private boolean isPillarAir(float pillarNoise, float erosion, int worldY) {
        if(worldY <= (LAVA_LEVEL * 2.5F)) return pillarNoise * (1.12F - erosion * (worldY / (LAVA_LEVEL * 2.5F))) < 0.55F;
        else return pillarNoise * (1.12F - erosion) < 0.55F;
    }

    private boolean carveTunnels(int worldY, int height, SimplexNoiseCached.DirectionalGenerator tunnelsBig1, SimplexNoiseCached.DirectionalGenerator tunnelsBig2, SimplexNoiseCached.DirectionalGenerator tunnelsSmall1, SimplexNoiseCached.DirectionalGenerator tunnelsSmall2, Vector3d tunnelsBig1Dir, Vector3d tunnelsBig2Dir, Vector3d tunnelsSmall1Dir, Vector3d tunnelsSmall2Dir) {
        //Big tunnels
        double y1 = worldY * 0.0075 * 1.51;
        double value1 = tunnelsBig1.getForY(y1);
        double densityBig = value1 * value1;
        double thresholdBig = 0.131 * 0.131;
        if(height < SEA_LEVEL - 32) { //Close tunnels off past continental shelf
            if(worldY > height - 16F) thresholdBig *= (8 - (worldY - (height - 16))) / 8D;
        }
        else if(worldY > height - 32F) thresholdBig *= Math.max(0.23F, (32 - (worldY - (height - 32))) / 32D);
        if(densityBig < thresholdBig) {
            double y2 = worldY * 0.0075 * 1.51 + MathUtil.IDEAL_OPENSIMPLEX2_PAIR_VERTICAL_SAMPLING_OFFSET;
            double value2 = tunnelsBig2.getForY(y2);
            densityBig += value2 * value2;
            if(densityBig < thresholdBig) {
                tunnelsBig1.getDirection(tunnelsBig1Dir, y1);
                tunnelsBig2.getDirection(tunnelsBig2Dir, y2);
                densityBig += MathUtil.getCaveClosingValue(tunnelsBig1Dir, tunnelsBig2Dir) * (thresholdBig * 10D);
                if(densityBig < thresholdBig) return true;
            }
        }
        //Small tunnels (similar to above but narrower and longer)
        if(thresholdBig > 0D) {
            double y3 = worldY * 0.0058 * 1.51;
            double value3 = tunnelsSmall1.getForY(y3);
            double densitySmall = value3 * value3;
            double thresholdSmall = thresholdBig * 0.35F;
            if(worldY > height - 32F) thresholdSmall *= (16 - (worldY - (height - 32))) / 16D; //Close small tunnels completely near surface
            if(densitySmall < thresholdSmall) {
                double y4 = worldY * 0.0058 * 1.51 + MathUtil.IDEAL_OPENSIMPLEX2_PAIR_VERTICAL_SAMPLING_OFFSET;
                double value4 = tunnelsSmall2.getForY(y4);
                densitySmall += value4 * value4;
                if(densitySmall < thresholdSmall) {
                    tunnelsSmall1.getDirection(tunnelsSmall1Dir, y3);
                    tunnelsSmall2.getDirection(tunnelsSmall2Dir, y4);
                    densitySmall += MathUtil.getCaveClosingValue(tunnelsSmall1Dir, tunnelsSmall2Dir) * (thresholdSmall * 12D);
                    if(densitySmall < thresholdSmall) return true;
                }
            }
        }
        return false;
    }

    private static IStone getStoneType(int worldY, StoneGroup stoneGroup, float igneousHeight, float metamorphicHeight) {
        if(worldY < igneousHeight) return stoneGroup.igneousType;
        else if(worldY < metamorphicHeight) return stoneGroup.metamorphicType;
        else return stoneGroup.sedimentaryType;
    }

    private BlockState getStone(int worldY, StoneGroup stoneGroup, float igneousHeight, float metamorphicHeight) {
        if(worldY < igneousHeight) return stoneGroup.igneousStone;
        else if(worldY < metamorphicHeight) return stoneGroup.metamorphicStone;
        else return stoneGroup.sedimentaryStone;
    }

    private BlockState getBlock(int worldY, int height, float elevation, float exposure, float temperature, float humidity, int soilDepth, BlockState soil, BlockState coveredSoil, BlockState water, StoneGroup stoneGroup, float igneousHeight, float metamorphicHeight, int bedrockHeight, int bedrockLayerHeight, WrappedInt lastSurfaceAirHeight, boolean[] pillarsCache, boolean[] cavernsCache, WrappedBool doFluidUpdate, WrappedBool wasAboveTunneled, WrappedInt lastCavernBlock, WrappedInt pillarGaps, WrappedInt pillarGapCount, SimplexNoiseCached.DirectionalGenerator tunnelsBig1, SimplexNoiseCached.DirectionalGenerator tunnelsBig2, SimplexNoiseCached.DirectionalGenerator tunnelsSmall1, SimplexNoiseCached.DirectionalGenerator tunnelsSmall2, Vector3d tunnelsBig1Dir, Vector3d tunnelsBig2Dir, Vector3d tunnelsSmall1Dir, Vector3d tunnelsSmall2Dir) {
        //Caves & weathering (ordered by most likely to create air as this allows for optimal skipping)
        if(worldY < height) {
            //Bedrock
            if(worldY < bedrockLayerHeight) {
                return worldY >= bedrockHeight ? getStone(worldY, stoneGroup, igneousHeight, metamorphicHeight) : BEDROCK;
            }
            float noise;
            //Caverns
            if(worldY < SEA_LEVEL) {
                boolean isCached = pillarGaps.val != -1;
                boolean isCavern;
                float pillarErosion = 0F;
                if(isCached) isCavern = cavernsCache[worldY];
                else {
                    noise = caverns.getForY(worldY * 1.65);
                    pillarErosion = noise;
                    noise = adjustCavernsNoise(noise, worldY, height);
                    isCavern = noise > 0.15F;
                }
                if(isCavern) {
                    boolean firstAir;
                    if(isCached) firstAir = pillarsCache[worldY];
                    //Compute gaps if needed
                    else {
                        //Check for pillar once inside a cavern
                        float pillarsNoise = pillars.getForY(worldY * PILLAR_Y);
                        firstAir = isPillarAir(pillarsNoise, pillarErosion, worldY);
                        boolean lastAir = firstAir;
                        lastCavernBlock.val = wasAboveTunneled.val ? 0 : 2; //0 = air, 1 = replaced stone, 2 = stone
                        int gaps = lastAir != wasAboveTunneled.val ? 1 : 0;
                        int yOffset = 1;
                        float cavernsNoiseNext = 1F, pillarsNoiseNext;
                        while(cavernsNoiseNext > 0.12F) {
                            int y = worldY - yOffset;
                            if(y < bedrockLayerHeight) {
                                if(lastAir) gaps++;
                                break;
                            }
                            cavernsNoiseNext = caverns.getForY(y * 1.65);
                            float pillarErosionNext = cavernsNoiseNext;
                            cavernsNoiseNext = adjustCavernsNoise(cavernsNoiseNext , y, height);
                            if(cavernsNoiseNext <= 0.15F) {
                                //Check for tunnels in case the bottom of the cavern is gone
                                if(lastAir != carveTunnels(y, height, tunnelsBig1, tunnelsBig2, tunnelsSmall1, tunnelsSmall2, tunnelsBig1Dir, tunnelsBig2Dir, tunnelsSmall1Dir, tunnelsSmall2Dir)) {
                                    gaps++;
                                }
                                break;
                            }
                            else cavernsCache[y] = true;
                            pillarsNoiseNext = pillars.getForY(y * PILLAR_Y);
                            boolean air = isPillarAir(pillarsNoiseNext, pillarErosionNext, y);
                            pillarsCache[y] = air;
                            if(lastAir != air) gaps++;
                            lastAir = air;
                            yOffset++;
                        }
                        pillarGaps.val = gaps;
                        pillarGapCount.val = 0;
                    }
                    if(!firstAir) {
                        if(lastCavernBlock.val < 2) { //Was air
                            if(lastCavernBlock.val == 0) pillarGapCount.val++; //Was originally air
                            if(pillarGapCount.val < pillarGaps.val) {
                                lastCavernBlock.val = 1; //Replace stone with air
                                return worldY > LAVA_LEVEL ? CAVE_AIR : LAVA;
                            }
                        }
                        lastCavernBlock.val = 2; //Stone
                        return getStone(worldY, stoneGroup, igneousHeight, metamorphicHeight);
                    }
                    else {
                        if(lastCavernBlock.val == 2) pillarGapCount.val++; //Was stone
                        lastCavernBlock.val = 0; //Air
                        return worldY > LAVA_LEVEL ? CAVE_AIR : LAVA;
                    }
                }
                pillarGaps.val = -1; //Reset pillar variables if past cavern
            }
            //Weathering
            else {
                noise = weathering.getForY(worldY * 0.79D);
                if(worldY < SEA_LEVEL + 32) noise *= (worldY - SEA_LEVEL) / 32F; //Stop beaches from getting destroyed
                else noise += (worldY - SEA_LEVEL - 32) / 465F; //Mitigate floatiness, especially in tall mountains
                if(noise > 0F) {
                    noise -= (height - worldY) / 150F; //Stop mountains from being too sunk in
                    if(noise * Math.abs(exposure) > 0.085) {
                        lastSurfaceAirHeight.val = worldY;
                        return AIR;
                    }
                }
            }
            //Tunnels
            if(carveTunnels(worldY, height, tunnelsBig1, tunnelsBig2, tunnelsSmall1, tunnelsSmall2, tunnelsBig1Dir, tunnelsBig2Dir, tunnelsSmall1Dir, tunnelsSmall2Dir)) {
                wasAboveTunneled.val = true;
                if(worldY > LAVA_LEVEL) {
                    if(worldY + 1 == lastSurfaceAirHeight.val) lastSurfaceAirHeight.val = worldY;
                    return CAVE_AIR;
                }
                else return LAVA;
            }
            wasAboveTunneled.val = false;
            //Pick soil if within range
            if(worldY >= lastSurfaceAirHeight.val - soilDepth) return worldY == (lastSurfaceAirHeight.val - 1) ? coveredSoil : soil;
            //Pick stone block
            else return getStone(worldY, stoneGroup, igneousHeight, metamorphicHeight);
        }
        lastSurfaceAirHeight.val = worldY;
        if(worldY <= SEA_LEVEL) {
            //No clean solution to this as blocks outside the chunk could influence an update
            //Forcing the fluid to update for any possible scenario will work, but is wasteful since these ticks are rarely needed
            if(worldY - 1 < height && height >= SEA_LEVEL - 32) doFluidUpdate.val = true;
            return water;
        }
        else return AIR;
    }

    private static RegistryObject<Biome> getBiome(int worldY, int height, float elevation, float temperature, float humidity) {
        if(worldY < height && worldY < SEA_LEVEL - 32) {
            if(worldY < 128) return BiomesNF.DEPTHS;
            else if(worldY < 256) return BiomesNF.CAVERNS;
            else return BiomesNF.TUNNELS;
        }
        else if(elevation < -0.575F && height > SEA_LEVEL) return BiomesNF.ISLAND;
        else if(elevation < 0.278F && height <= SEA_LEVEL) return BiomesNF.OCEAN;
        else if(temperature > HIGH_CLIMATE) {
            if(humidity > HIGH_CLIMATE) return BiomesNF.SWAMP;
            else if(humidity > LOW_CLIMATE) return BiomesNF.BADLANDS;
            else return BiomesNF.DESERT;
        }
        else if(temperature > LOW_CLIMATE) {
            if(humidity > HIGH_CLIMATE) return BiomesNF.JUNGLE;
            else if(humidity > LOW_CLIMATE) return BiomesNF.FOREST;
            else return BiomesNF.GRASSLANDS;
        }
        else {
            if(humidity > HIGH_CLIMATE) return BiomesNF.OLDWOODS;
            else if(humidity > LOW_CLIMATE) return BiomesNF.TAIGA;
            else return BiomesNF.TUNDRA;
        }
    }

    public RegistryObject<Biome> calculateBiome(int worldX, int worldY, int worldZ) {
        float elevation = this.elevation.noise2D(worldX, worldZ);
        float roughness = this.roughness.noise2D(worldX, worldZ);
        float detail = this.detail.noise2D(worldX, worldZ);
        int height = getHeight(elevation, roughness, detail);
        return getBiome(worldY, height, elevation, getTemperature(worldX, worldZ), getHumidity(worldX, worldZ));
    }

    private static int getSoilDepth(int height, float humidity, float exposure) {
        int soilDepth;
        if(humidity < LOW_CLIMATE) soilDepth = 1;
        else if(humidity < HIGH_CLIMATE) soilDepth = 2;
        else soilDepth = 3;
        if(exposure > 0.5F) soilDepth--;
        if(height > SEA_LEVEL + 192 && soilDepth > 0) soilDepth--;
        return soilDepth;
    }

    private static int getSoilQuality(int soilDepth, float temperature, float humidity) {
        return (temperature < LOW_CLIMATE && humidity > LOW_CLIMATE) || (temperature > HIGH_CLIMATE && humidity < HIGH_CLIMATE) ? soilDepth - 1 : soilDepth;
    }

    private static BlockState getSoil(int soilQuality, int height, StoneGroup stoneGroup, float igneousHeight, float metamorphicHeight) {
        if(height < SEA_LEVEL + 4) {
            return SOILS.get(getStoneType(height, stoneGroup, igneousHeight, metamorphicHeight).getSoil());
        }
        else return switch(soilQuality) {
            case 1 -> SILT;
            case 2 -> DIRT;
            case 3 -> LOAM;
            default -> SOILS.get(getStoneType(height, stoneGroup, igneousHeight, metamorphicHeight).getSoil());
        };
    }

    private ChunkAccess fill(ChunkAccess chunk) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        Heightmap oceanMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunk.getPos();
        int chunkX = chunkPos.getMinBlockX();
        int chunkZ = chunkPos.getMinBlockZ();
        SimplexNoiseCached.DirectionalGenerator bigTunnelsGen1 = bigTunnels1.directionalGenerator();
        SimplexNoiseCached.DirectionalGenerator bigTunnelsGen2 = bigTunnels2.directionalGenerator();
        SimplexNoiseCached.DirectionalGenerator smallTunnelsGen1 = smallTunnels1.directionalGenerator();
        SimplexNoiseCached.DirectionalGenerator smallTunnelsGen2 = smallTunnels2.directionalGenerator();
        pillars.initGenerators(2);
        caverns.initGenerators(3);
        weathering.initGenerators(4);
        Vector3d bigTunnelsDir1 = new Vector3d(0, 0, 0);
        Vector3d bigTunnelsDir2 = new Vector3d(0, 0, 0);
        Vector3d smallTunnelsDir1 = new Vector3d(0, 0, 0);
        Vector3d smallTunnelsDir2 = new Vector3d(0, 0, 0);
        LevelChunkSection section = chunk.getSection(chunk.getSectionsCount() / 2);
        WrappedBool doFluidUpdate = new WrappedBool(false);
        WrappedBool wasAboveTunneled = new WrappedBool(false);
        WrappedInt lastCavernBlock = new WrappedInt(2);
        WrappedInt pillarGaps = new WrappedInt(-1);
        WrappedInt cavernBlocks = new WrappedInt(0);
        Random rand = new Random(seed + ((long) chunkX << 32L) + chunkZ);
        for(int x = 0; x < 16; x++) {
            int worldX = x + chunkX;
            for(int z = 0; z < 16; z++) {
                int worldZ = z + chunkZ;
                float elevation = this.elevation.noise2D(worldX, worldZ);
                float roughness = this.roughness.noise2D(worldX, worldZ);
                float detail = this.detail.noise2D(worldX, worldZ);
                int height = getHeight(elevation, roughness, detail);
                float exposure = this.exposure.noise2D(worldX, worldZ);
                float temperature = getTemperature(worldX, worldZ);
                float humidity = getHumidity(worldX, worldZ);
                float stoneType = this.stoneType.noise2D(worldX, worldZ);
                StoneGroup stoneGroup = getStoneGroup(temperature, stoneType);
                //Layer heights
                int bedrockHeight = bedrock.noise2D(worldX, worldZ) < 0F ? 1 : 2;
                int bedrockLayerHeight = bedrockHeight + (bedrockCover.noise2D(worldX, worldZ) < 0F ? 1 : 2);
                float igneousHeight = getIgneousHeight(worldX, worldZ, height);
                float metamorphicHeight = getMetamorphicHeight(worldX, worldZ, height, igneousHeight);
                //Pick soil
                int soilDepth = getSoilDepth(height, humidity, exposure);
                int soilQuality = getSoilQuality(soilDepth, temperature, humidity);
                BlockState soil = getSoil(soilQuality, height, stoneGroup, igneousHeight, metamorphicHeight);
                Holder<Biome> biome = getBiome(832, height, elevation, temperature, humidity).getHolder().get();
                float coverTemp = temperature + rand.nextFloat() * 0.025F;
                float coverHumidity = humidity + rand.nextFloat() * 0.025F;
                int coverSoilDepth = getSoilDepth(height, coverHumidity, exposure);
                int coverSoilQuality = getSoilQuality(coverSoilDepth, coverTemp, coverHumidity);
                BlockState coveredSoil = getSoil(coverSoilQuality, height, stoneGroup, igneousHeight, metamorphicHeight);
                if(height < SEA_LEVEL + 4) {
                    if(clayPatches.noise2D(worldX, worldZ) > 0.77F) coveredSoil = BlocksNF.CLAY.get().defaultBlockState();
                    else if(coverSoilQuality >= 2 && mudPatches.noise2D(worldX, worldZ) > 0.77F) coveredSoil = BlocksNF.MUD.get().defaultBlockState();
                }
                else if(coverSoilQuality > 0) {
                    SoilCover cover = SoilCover.getForBiome(getBiome(832, height, elevation, coverTemp, coverHumidity).getHolder().get());
                    if(cover != null) coveredSoil = ((SoilBlock) coveredSoil.getBlock()).getCoveredBlock(cover);
                }
                //Water
                BlockState water = biome.is(BiomesNF.OCEAN.getKey()) ? SEAWATER : WATER;
                //Setup caves
                double bigX = worldX * 0.0075 + bigTunnelsWarp.noise2D(worldX, worldZ) * 0.055D;
                double bigZ = worldZ * 0.0075 + bigTunnelsWarp.noise2D(worldX, worldZ + 149) * 0.055D;
                bigTunnelsGen1.setXZ(bigX, bigZ);
                bigTunnelsGen2.setXZ(bigX, bigZ);
                double smallX = worldX * 0.0075 + smallTunnelsWarp.noise2D(worldX, worldZ) * 0.065D;
                double smallZ = worldZ * 0.0075 + smallTunnelsWarp.noise2D(worldX, worldZ + 149) * 0.065D;
                smallTunnelsGen1.setXZ(smallX, smallZ);
                smallTunnelsGen2.setXZ(smallX, smallZ);
                caverns.setXZ(worldX, worldZ);
                pillars.setXZ(worldX, worldZ);
                weathering.setXZ(worldX, worldZ);
                boolean[] pillarsCache = new boolean[SEA_LEVEL], cavernsCache = new boolean[SEA_LEVEL];
                pillarGaps.val = -1;
                wasAboveTunneled.val = false;
                int startHeight = Math.max(SEA_LEVEL, height - 1);
                WrappedInt lastSurfaceAirHeight = new WrappedInt(startHeight + 1);
                for(int worldY = startHeight; worldY >= 0; worldY--) {
                    int sectionIndex = chunk.getSectionIndex(worldY);
                    if(chunk.getSectionIndex(section.bottomBlockY()) != sectionIndex) section = chunk.getSection(sectionIndex);
                    BlockState state = getBlock(worldY, height, elevation, exposure, temperature, humidity, soilDepth, soil, coveredSoil, water, stoneGroup, igneousHeight,
                            metamorphicHeight, bedrockHeight, bedrockLayerHeight, lastSurfaceAirHeight, pillarsCache, cavernsCache, doFluidUpdate, wasAboveTunneled,
                            lastCavernBlock, pillarGaps, cavernBlocks, bigTunnelsGen1, bigTunnelsGen2, smallTunnelsGen1, smallTunnelsGen2,
                            bigTunnelsDir1, bigTunnelsDir2, smallTunnelsDir1, smallTunnelsDir2);
                    if(state != AIR) {
                        if(state.getLightEmission() != 0 && chunk instanceof ProtoChunk protoChunk) {
                            blockPos.set(worldX, worldY, worldZ);
                            protoChunk.addLight(blockPos);
                        }
                        section.setBlockState(x, worldY & 15, z, state, false);
                        worldMap.update(x, worldY, z, state);
                        oceanMap.update(x, worldY, z, state);
                        if(doFluidUpdate.val) {
                            chunk.markPosForPostprocessing(blockPos.set(worldX, worldY, worldZ));
                            doFluidUpdate.val = false;
                        }
                        else if(worldY == startHeight && state.getBlock() instanceof ITimeSimulatedBlock) {
                            chunk.markPosForPostprocessing(blockPos.set(worldX, worldY, worldZ));
                        }
                    }
                }
            }
        }
        caverns.deleteGenerators();
        pillars.deleteGenerators();
        weathering.deleteGenerators();
        return chunk;
    }

    @Override
    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    @Override
    public int getMinY() {
        return MIN_Y;
    }

    @Override
    public int getGenDepth() {
        return MAX_Y;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types pType, LevelHeightAccessor level) {
        return iterateNoiseColumn(x, z, null, pType.isOpaque()).orElse(level.getMinBuildHeight());
    }

    @Override
    public @NotNull NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level) {
        BlockState[] column = new BlockState[MAX_Y];
        iterateNoiseColumn(x, z, column, null);
        return new NoiseColumn(level.getMinBuildHeight(), column);
    }

    protected OptionalInt iterateNoiseColumn(int worldX, int worldZ, @Nullable BlockState[] column, @Nullable Predicate<BlockState> stopState) {
        SimplexNoiseCached.DirectionalGenerator bigTunnelsGen1 = bigTunnels1.directionalGenerator();
        SimplexNoiseCached.DirectionalGenerator bigTunnelsGen2 = bigTunnels2.directionalGenerator();
        SimplexNoiseCached.DirectionalGenerator smallTunnelsGen1 = smallTunnels1.directionalGenerator();
        SimplexNoiseCached.DirectionalGenerator smallTunnelsGen2 = smallTunnels2.directionalGenerator();
        pillars.initGenerators(2);
        caverns.initGenerators(3);
        weathering.initGenerators(4);
        Vector3d bigTunnelsDir1 = new Vector3d(0, 0, 0);
        Vector3d bigTunnelsDir2 = new Vector3d(0, 0, 0);
        Vector3d smallTunnelsDir1 = new Vector3d(0, 0, 0);
        Vector3d smallTunnelsDir2 = new Vector3d(0, 0, 0);
        WrappedBool doFluidUpdate = new WrappedBool(false);
        WrappedBool wasAboveTunneled = new WrappedBool(false);
        WrappedInt lastCavernBlock = new WrappedInt(2);
        WrappedInt pillarGaps = new WrappedInt(-1);
        WrappedInt cavernBlocks = new WrappedInt(0);
        Random rand = new Random(seed + ((long) SectionPos.blockToSectionCoord(worldX) << 32L) + SectionPos.blockToSectionCoord(worldZ));
        float elevation = this.elevation.noise2D(worldX, worldZ);
        float roughness = this.roughness.noise2D(worldX, worldZ);
        float detail = this.detail.noise2D(worldX, worldZ);
        int height = getHeight(elevation, roughness, detail);
        float exposure = this.exposure.noise2D(worldX, worldZ);
        float temperature = getTemperature(worldX, worldZ);
        float humidity = getHumidity(worldX, worldZ);
        float stoneType = this.stoneType.noise2D(worldX, worldZ);
        StoneGroup stoneGroup = getStoneGroup(temperature, stoneType);
        //Layer heights
        int bedrockHeight = bedrock.noise2D(worldX, worldZ) < 0F ? 1 : 2;
        int bedrockLayerHeight = bedrockHeight + (bedrockCover.noise2D(worldX, worldZ) < 0F ? 1 : 2);
        float igneousHeight = getIgneousHeight(worldX, worldZ, height);
        float metamorphicHeight = getMetamorphicHeight(worldX, worldZ, height, igneousHeight);
        //Pick soil
        int soilDepth = getSoilDepth(height, humidity, exposure);
        int soilQuality = getSoilQuality(soilDepth, temperature, humidity);
        BlockState soil = getSoil(soilQuality, height, stoneGroup, igneousHeight, metamorphicHeight);
        Holder<Biome> biome = getBiome(832, height, elevation, temperature, humidity).getHolder().get();
        float coverTemp = temperature + rand.nextFloat() * 0.025F;
        float coverHumidity = humidity + rand.nextFloat() * 0.025F;
        int coverSoilDepth = getSoilDepth(height, coverHumidity, exposure);
        int coverSoilQuality = getSoilQuality(coverSoilDepth, coverTemp, coverHumidity);
        BlockState coveredSoil = getSoil(coverSoilQuality, height, stoneGroup, igneousHeight, metamorphicHeight);
        if(height < SEA_LEVEL + 4) {
            if(clayPatches.noise2D(worldX, worldZ) > 0.77F) coveredSoil = BlocksNF.CLAY.get().defaultBlockState();
            else if(coverSoilQuality >= 2 && mudPatches.noise2D(worldX, worldZ) > 0.77F) coveredSoil = BlocksNF.MUD.get().defaultBlockState();
        }
        else if(coverSoilQuality > 0) {
            SoilCover cover = SoilCover.getForBiome(getBiome(832, height, elevation, coverTemp, coverHumidity).getHolder().get());
            if(cover != null) coveredSoil = ((SoilBlock) coveredSoil.getBlock()).getCoveredBlock(cover);
        }
        //Water
        BlockState water = biome.is(BiomesNF.OCEAN.getKey()) ? SEAWATER : WATER;
        //Setup caves
        double bigX = worldX * 0.0075 + bigTunnelsWarp.noise2D(worldX, worldZ) * 0.055D;
        double bigZ = worldZ * 0.0075 + bigTunnelsWarp.noise2D(worldX, worldZ + 149) * 0.055D;
        bigTunnelsGen1.setXZ(bigX, bigZ);
        bigTunnelsGen2.setXZ(bigX, bigZ);
        double smallX = worldX * 0.0075 + smallTunnelsWarp.noise2D(worldX, worldZ) * 0.065D;
        double smallZ = worldZ * 0.0075 + smallTunnelsWarp.noise2D(worldX, worldZ + 149) * 0.065D;
        smallTunnelsGen1.setXZ(smallX, smallZ);
        smallTunnelsGen2.setXZ(smallX, smallZ);
        caverns.setXZ(worldX, worldZ);
        pillars.setXZ(worldX, worldZ);
        weathering.setXZ(worldX, worldZ);
        boolean[] pillarsCache = new boolean[SEA_LEVEL], cavernsCache = new boolean[SEA_LEVEL];
        pillarGaps.val = -1;
        wasAboveTunneled.val = false;
        int startHeight = Math.max(SEA_LEVEL, height - 1);
        WrappedInt lastSurfaceAirHeight = new WrappedInt(startHeight + 1);
        for(int worldY = MAX_Y - 1; worldY >= 0; worldY--) {
            BlockState state = getBlock(worldY, height, elevation, exposure, temperature, humidity, soilDepth, soil, coveredSoil, water, stoneGroup, igneousHeight,
                    metamorphicHeight, bedrockHeight, bedrockLayerHeight, lastSurfaceAirHeight, pillarsCache, cavernsCache, doFluidUpdate, wasAboveTunneled,
                    lastCavernBlock, pillarGaps, cavernBlocks, bigTunnelsGen1, bigTunnelsGen2, smallTunnelsGen1, smallTunnelsGen2,
                    bigTunnelsDir1, bigTunnelsDir2, smallTunnelsDir1, smallTunnelsDir2);
            if(column != null) column[worldY] = state;
            if(stopState != null && stopState.test(state)) {
                caverns.deleteGenerators();
                pillars.deleteGenerators();
                weathering.deleteGenerators();
                return OptionalInt.of(worldY + 1);
            }
        }
        caverns.deleteGenerators();
        pillars.deleteGenerators();
        weathering.deleteGenerators();
        return OptionalInt.empty();
    }

    @Override
    public void addDebugScreenInfo(List<String> info, BlockPos pos) {
        DecimalFormat format = new DecimalFormat("0.000");
        info.add("Terrain: E: " + format.format(elevation.noise2D(pos.getX(), pos.getZ())) + " R: " + format.format(roughness.noise2D(pos.getX(), pos.getZ())) +
                " D: " + format.format(detail.noise2D(pos.getX(), pos.getZ())) + " EX: " + format.format(exposure.noise2D(pos.getX(), pos.getZ())));
        info.add("Climate: T: " + format.format(getTemperature(pos.getX(), pos.getZ())) + " H: " + format.format(getHumidity(pos.getX(), pos.getZ())));
        info.add("Stone: ST: " + format.format(stoneType.noise2D(pos.getX(), pos.getZ())) + " IH: " + format.format(igneousHeight.noise2D(pos.getX(), pos.getZ())) +
                " MH: " + format.format(metamorphicHeight.noise2D(pos.getX(), pos.getZ())));
        info.add("Trees: F: " + format.format(forestation.noise2D(pos.getX(), pos.getZ())) + " C: " + format.format(clearing.noise2D(pos.getX(), pos.getZ())) +
                " 1: " + format.format(tree1.noise2D(pos.getX(), pos.getZ())) + " 2: " + format.format(tree2.noise2D(pos.getX(), pos.getZ())) +
                " 3: " + format.format(tree3.noise2D(pos.getX(), pos.getZ())) + " 4: " + format.format(tree4.noise2D(pos.getX(), pos.getZ())));
    }
}
