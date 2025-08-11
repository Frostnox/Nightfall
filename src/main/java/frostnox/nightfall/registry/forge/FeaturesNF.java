package frostnox.nightfall.registry.forge;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.Stone;
import frostnox.nightfall.block.block.crop.CropBlockNF;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import frostnox.nightfall.world.generation.feature.*;
import frostnox.nightfall.world.generation.placement.*;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.event.RegistryEvent;

import java.util.List;
import java.util.Set;

import static frostnox.nightfall.world.generation.ContinentalChunkGenerator.SEA_LEVEL;

/**
 * Deferred register is not used here since features are needed immediately and registry objects are not accessible until later.
 * Features are instead registered directly using the registry event.
 */
public class FeaturesNF {
    //Flora
    public static final RandomTreeFeature TREE_FEATURE = new RandomTreeFeature(name("tree"));
    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> TREE_CONFIG = register("tree", TREE_FEATURE, FeatureConfiguration.NONE);
    public static final Holder<PlacedFeature> TREE = register("tree", TREE_CONFIG, ForestationCountPlacement.of(13F),
            InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome(), AdjacentScanPlacement.of(BlockPredicate.matchesTag(TagsNF.TREE_WOOD), true));
    public static final LoneTreeFeature LONE_TREE_FEATURE = new LoneTreeFeature(name("lone_tree"));
    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> LONE_TREE_CONFIG = register("lone_tree", LONE_TREE_FEATURE, FeatureConfiguration.NONE);
    public static final Holder<PlacedFeature> LONE_TREE = register("lone_tree", LONE_TREE_CONFIG, CountPlacement.of(1), ChanceFilter.with(0.002F),
            InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome(), AdjacentScanPlacement.of(BlockPredicate.matchesTag(TagsNF.TREE_WOOD), true));
//    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> SHORT_GRASS_CONFIG = register("short_grass",
//            Feature.RANDOM_PATCH, patch(BlockStateProvider.simple(BlocksNF.SHORT_GRASS.get()), 32));
//    public static final Holder<PlacedFeature> SHORT_GRASS = register("short_grass", SHORT_GRASS_CONFIG, BiomeFilter.biome(), InSquarePlacement.spread(),
//            PlacementUtils.HEIGHTMAP_WORLD_SURFACE);
//    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> GRASS_CONFIG = register("grass",
//            Feature.RANDOM_PATCH, patch(BlockStateProvider.simple(BlocksNF.GRASS.get()), 32));
//    public static final Holder<PlacedFeature> GRASS = register("grass", GRASS_CONFIG, BiomeFilter.biome(), InSquarePlacement.spread(),
//            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, RectangleClimateFilter.with(0.25F, 0.75F, 0.05F, 0.75F));
//    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> TALL_GRASS_CONFIG = register("tall_grass",
//            Feature.RANDOM_PATCH, patch(BlockStateProvider.simple(BlocksNF.TALL_GRASS.get()), 32));
//    public static final Holder<PlacedFeature> TALL_GRASS = register("tall_grass", TALL_GRASS_CONFIG, BiomeFilter.biome(), InSquarePlacement.spread(),
//            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, RectangleClimateFilter.with(0.42F, 0.58F, 0.05F, 0.3F));
//    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> SMALL_FERN_CONFIG = register("small_fern",
//            Feature.RANDOM_PATCH, patch(BlockStateProvider.simple(BlocksNF.SMALL_FERN.get()), 32));
//    public static final Holder<PlacedFeature> SMALL_FERN = register("small_fern", SMALL_FERN_CONFIG, BiomeFilter.biome(), InSquarePlacement.spread(),
//            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, RectangleClimateFilter.with(0.05F, 0.95F, 0.68F, 1F));
//    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> FERN_CONFIG = register("fern",
//            Feature.RANDOM_PATCH, patch(BlockStateProvider.simple(BlocksNF.FERN.get()), 32));
//    public static final Holder<PlacedFeature> FERN = register("fern", FERN_CONFIG, BiomeFilter.biome(), InSquarePlacement.spread(),
//            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, RectangleClimateFilter.with(0.05F, 0.95F, 0.72F, 1F));
//    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> LARGE_FERN_CONFIG = register("large_fern",
//            Feature.RANDOM_PATCH, patch(BlockStateProvider.simple(BlocksNF.LARGE_FERN.get()), 32));
//    public static final Holder<PlacedFeature> LARGE_FERN = register("large_fern", LARGE_FERN_CONFIG, BiomeFilter.biome(), InSquarePlacement.spread(),
//            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, RectangleClimateFilter.with(0.05F, 0.95F, 0.75F, 1F));
//
//    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> POTATOES_CONFIG = register("potatoes",
//            Feature.RANDOM_PATCH, patch(BlockStateProvider.simple(BlocksNF.POTATOES.get().defaultBlockState().setValue(CropBlockNF.STAGE, 8)),
//                    16, 4, 2));
//    public static final Holder<PlacedFeature> POTATOES = register("potatoes", POTATOES_CONFIG, BiomeFilter.biome(), InSquarePlacement.spread(),
//            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, ChanceFilter.with(0.011F), cropClimateFilter(BlocksNF.POTATOES.get()));
//    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> CARROTS_CONFIG = register("carrots",
//            Feature.RANDOM_PATCH, patch(BlockStateProvider.simple(BlocksNF.CARROTS.get().defaultBlockState().setValue(CropBlockNF.STAGE, 8)),
//                    16, 4, 2));
//    public static final Holder<PlacedFeature> CARROTS = register("carrots", CARROTS_CONFIG, BiomeFilter.biome(), InSquarePlacement.spread(),
//            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, ChanceFilter.with(0.011F), cropClimateFilter(BlocksNF.CARROTS.get()));
//    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> FLAX_CONFIG = register("flax",
//            Feature.RANDOM_PATCH, patch(BlockStateProvider.simple(BlocksNF.FLAX.get().defaultBlockState().setValue(CropBlockNF.STAGE, 8)),
//                    16, 4, 2));
//    public static final Holder<PlacedFeature> FLAX = register("flax", FLAX_CONFIG, BiomeFilter.biome(), InSquarePlacement.spread(),
//            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, ChanceFilter.with(0.01F), cropClimateFilter(BlocksNF.FLAX.get()));
//    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> YARROW_CONFIG = register("yarrow",
//            Feature.RANDOM_PATCH, patch(BlockStateProvider.simple(BlocksNF.YARROW.get().defaultBlockState().setValue(CropBlockNF.STAGE, 8)),
//                    16, 4, 2));
//    public static final Holder<PlacedFeature> YARROW = register("yarrow", YARROW_CONFIG, BiomeFilter.biome(), InSquarePlacement.spread(),
//            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, ChanceFilter.with(0.01F), cropClimateFilter(BlocksNF.YARROW.get()));
//    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> BERRY_BUSH_CONFIG = register("berry_bush",
//            Feature.RANDOM_PATCH, patch(BlockStateProvider.simple(BlocksNF.BERRY_BUSH.get().defaultBlockState().setValue(FruitBushBlock.STAGE, 4)),
//                    16, 5, 2));
//    public static final Holder<PlacedFeature> BERRY_BUSH = register("berry_bush", BERRY_BUSH_CONFIG, BiomeFilter.biome(), InSquarePlacement.spread(),
//            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, ChanceFilter.with(0.009F),
//            RectangleClimateFilter.with(BlocksNF.BERRY_BUSH.get().minTemp, BlocksNF.BERRY_BUSH.get().maxTemp, BlocksNF.BERRY_BUSH.get().minHumidity, BlocksNF.BERRY_BUSH.get().maxHumidity));
    //Stone
    public static final RocksFeature ROCKS_FEATURE = new RocksFeature(name("rocks"));
    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> ROCKS_CONFIG = register("rocks", ROCKS_FEATURE, FeatureConfiguration.NONE);
    public static final Holder<PlacedFeature> SURFACE_ROCKS = register("rocks", ROCKS_CONFIG, ExposureCountPlacement.of(5F, 1),
            InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome());
    public static final BoulderFeature BOULDER_FEATURE = new BoulderFeature(name("boulder"));
    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> BOULDER_CONFIG = register("boulder", BOULDER_FEATURE, FeatureConfiguration.NONE);
    public static final Holder<PlacedFeature> BOULDER = register("boulder", BOULDER_CONFIG, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
            ExposureChanceFilter.with(0.62F, 0.04F), BiomeFilter.biome());
    //Surface misc
    public static final SingleBlockFeature SINGLE_BLOCK_FEATURE = new SingleBlockFeature(name("single_block"));
    public static final Holder<ConfiguredFeature<BlockStateConfiguration, ?>> SEASHELLS_CONFIG = register("seashells", SINGLE_BLOCK_FEATURE, new BlockStateConfiguration(BlocksNF.SEASHELL.get().defaultBlockState()));
    public static final Holder<PlacedFeature> SEASHELLS = register("seashells", SEASHELLS_CONFIG,
            ElevationPreciseFilter.with(-0.647F, 0.3F), SurfaceHeightFilter.with(SEA_LEVEL - 30, SEA_LEVEL + 1),
            HeightRangePlacement.triangle(VerticalAnchor.absolute(SEA_LEVEL - 30), VerticalAnchor.absolute(SEA_LEVEL + 30)),
            InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome());
    //Ore
    public static final OreVeinFeature ORE_VEIN_FEATURE = new OreVeinFeature(name("ore_vein"));

    private static final List<BlockState> TIN_ORE_VALUES = BlocksNF.TIN_ORES.values().stream().map(block -> block.get().defaultBlockState()).toList();
    public static final Holder<ConfiguredFeature<OreVeinFeature.Configuration, ?>> TIN_VEIN_CONFIG = register("tin_vein", ORE_VEIN_FEATURE,
            new OreVeinFeature.Configuration(getOreKeys(BlocksNF.TIN_ORES.keySet()), TIN_ORE_VALUES, 0.09 * 0.09, 0.6F,
                    0.2F, 3, 3, 3, 3, 0.026, 0.03));
    public static final Holder<PlacedFeature> TIN_VEIN = register("tin_vein", TIN_VEIN_CONFIG,
            orePlacement(28, 38, HeightRangePlacement.uniform(VerticalAnchor.absolute(270), VerticalAnchor.TOP)));

    private static final List<BlockState> COPPER_ORE_VALUES = BlocksNF.COPPER_ORES.values().stream().map(block -> block.get().defaultBlockState()).toList();
    public static final Holder<ConfiguredFeature<OreVeinFeature.Configuration, ?>> COPPER_VEIN_CONFIG = register("copper_vein", ORE_VEIN_FEATURE,
            new OreVeinFeature.Configuration(getOreKeys(BlocksNF.COPPER_ORES.keySet()), COPPER_ORE_VALUES, 0.09 * 0.09, 0.6F,
                    0.2F, 3, 3, 3, 3, 0.026, 0.03));
    public static final Holder<PlacedFeature> COPPER_VEIN = register("copper_vein", COPPER_VEIN_CONFIG,
            orePlacement(33, 43, HeightRangePlacement.uniform(VerticalAnchor.absolute(240), VerticalAnchor.TOP)));

    private static final List<BlockState> AZURITE_ORE_VALUES = BlocksNF.AZURITE_ORES.values().stream().map(block -> block.get().defaultBlockState()).toList();
    public static final Holder<ConfiguredFeature<OreVeinFeature.Configuration, ?>> AZURITE_VEIN_CONFIG = register("azurite_vein", ORE_VEIN_FEATURE,
            new OreVeinFeature.Configuration(getOreKeys(BlocksNF.AZURITE_ORES.keySet()), AZURITE_ORE_VALUES, 0.072 * 0.072, 0.4F,
                    0.2F, 8, 9, 7, 8, 0.02, 0.024));
    public static final Holder<PlacedFeature> AZURITE_VEIN = register("azurite_vein", AZURITE_VEIN_CONFIG,
            orePlacement(2, 7, HeightRangePlacement.triangle(VerticalAnchor.absolute(350), VerticalAnchor.TOP)));

    private static final List<BlockState> HEMATITE_ORE_VALUES = BlocksNF.HEMATITE_ORES.values().stream().map(block -> block.get().defaultBlockState()).toList();
    public static final Holder<ConfiguredFeature<OreVeinFeature.Configuration, ?>> HEMATITE_VEIN_CONFIG = register("hematite_vein", ORE_VEIN_FEATURE,
            new OreVeinFeature.Configuration(getOreKeys(BlocksNF.HEMATITE_ORES.keySet()), HEMATITE_ORE_VALUES, 0.072 * 0.072, 0.4F,
                    0.2F, 8, 9, 7, 8, 0.02, 0.024));
    public static final Holder<PlacedFeature> HEMATITE_VEIN = register("hematite_vein", HEMATITE_VEIN_CONFIG,
            orePlacement(0, 4, HeightRangePlacement.triangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(320))));

    private static final List<BlockState> COAL_ORE_VALUES = BlocksNF.COAL_ORES.values().stream().map(block -> block.get().defaultBlockState()).toList();
    public static final Holder<ConfiguredFeature<OreVeinFeature.Configuration, ?>> COAL_VEIN_CONFIG = register("coal_vein", ORE_VEIN_FEATURE,
            new OreVeinFeature.Configuration(getOreKeys(BlocksNF.COAL_ORES.keySet()), COAL_ORE_VALUES, 0.072 * 0.072, 0.5F,
                    0.25F, 8, 9, 6, 9, 0.02, 0.027));
    public static final Holder<PlacedFeature> COAL_VEIN = register("coal_vein", COAL_VEIN_CONFIG,
            orePlacement(0, 2, HeightRangePlacement.triangle(VerticalAnchor.absolute(160), VerticalAnchor.absolute(440))));

    private static final List<BlockState> HALITE_ORE_VALUES = BlocksNF.HALITE_ORES.values().stream().map(block -> block.get().defaultBlockState()).toList();
    public static final Holder<ConfiguredFeature<OreVeinFeature.Configuration, ?>> HALITE_VEIN_CONFIG = register("halite_vein", ORE_VEIN_FEATURE,
            new OreVeinFeature.Configuration(getOreKeys(BlocksNF.HALITE_ORES.keySet()), HALITE_ORE_VALUES, 0.1 * 0.1, 0.55F,
                    0.25F, 10, 7, 2, 3, 0.014, 0.014));
    public static final Holder<PlacedFeature> HALITE_VEIN = register("halite_vein", HALITE_VEIN_CONFIG,
            orePlacement(0, 2, HeightRangePlacement.uniform(VerticalAnchor.absolute(300), VerticalAnchor.absolute(800))));
    //Underground
    public static final RuleTest NATURAL_STONE = new TagMatchTest(TagsNF.NATURAL_STONE);
    public static final Holder<ConfiguredFeature<OreConfiguration, ?>> FIRE_CLAY_CONFIG = FeatureUtils.register("fire_clay", Feature.ORE,
            new OreConfiguration(NATURAL_STONE, BlocksNF.FIRE_CLAY.get().defaultBlockState(), 30));
    public static final Holder<PlacedFeature> FIRE_CLAY = register("fire_clay", FIRE_CLAY_CONFIG, orePlacement(0, 1,
            HeightRangePlacement.uniform(VerticalAnchor.absolute(130), VerticalAnchor.absolute(290))));
    public static final CaveRocksFeature CAVE_ROCKS_FEATURE = new CaveRocksFeature(name("cave_rocks"));
    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> CAVE_ROCKS_CONFIG = register("cave_rocks", CAVE_ROCKS_FEATURE, FeatureConfiguration.NONE);
    public static final Holder<PlacedFeature> CAVE_ROCKS = register("cave_rocks", CAVE_ROCKS_CONFIG, CountPlacement.of(256),
            HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(16), VerticalAnchor.belowTop(16)), InSquarePlacement.spread(), BiomeFilter.biome());
    public static final RockwormNestFeature ROCKWORM_NEST_FEATURE = new RockwormNestFeature(name("rockworm_nest"));
    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> ROCKWORM_NEST_CONFIG = register("rockworm_nest", ROCKWORM_NEST_FEATURE, FeatureConfiguration.NONE);
    public static final Holder<PlacedFeature> ROCKWORM_NEST = register("rockworm_nest", ROCKWORM_NEST_CONFIG, CountPlacement.of(22),
            HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(16), VerticalAnchor.belowTop(16)), InSquarePlacement.spread(), BiomeFilter.biome());
    //Special
    public static final MeteoriteFeature METEORITE_FEATURE = new MeteoriteFeature(name("meteorite"));
    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> METEORITE_CONFIG = register("meteorite", METEORITE_FEATURE, FeatureConfiguration.NONE);
    public static final Holder<PlacedFeature> METEORITE = register("meteorite", METEORITE_CONFIG, ChanceFilter.with(0.0135F),
            CountPlacement.of(1), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome());
    public static final RabbitBurrowFeature RABBIT_BURROW_FEATURE = new RabbitBurrowFeature(name("rabbit_burrow"));
    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> RABBIT_BURROW_CONFIG = register("rabbit_burrow", RABBIT_BURROW_FEATURE, FeatureConfiguration.NONE);
    public static final Holder<PlacedFeature> RABBIT_BURROW = register("rabbit_burrow", RABBIT_BURROW_CONFIG, ChanceFilter.with(0.035F),
            CountPlacement.of(1), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
    public static final SpiderNestFeature SPIDER_NEST_FEATURE = new SpiderNestFeature(name("spider_nest"));
    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> SPIDER_NEST_CONFIG = register("spider_nest", SPIDER_NEST_FEATURE, FeatureConfiguration.NONE);
    public static final Holder<PlacedFeature> SPIDER_NEST_SURFACE = register("spider_nest_surface", SPIDER_NEST_CONFIG, CountPlacement.of(1), InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, NearSpawnFilter.with(50F), ChanceFilter.with(0.0025F), SurfaceHeightFilter.with(421, 475),
            RectangleClimateFilter.with(0F, 1F, 0.29F, 1F), BiomeFilter.biome());
    public static final Holder<PlacedFeature> SPIDER_NEST_CAVES = register("spider_nest_caves", SPIDER_NEST_CONFIG, CountPlacement.of(18), InSquarePlacement.spread(),
            HeightRangePlacement.uniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(412)), BiomeFilter.biome());

    public static void registerEvent(RegistryEvent.Register<Feature<?>> event) {
        event.getRegistry().registerAll(TREE_FEATURE, LONE_TREE_FEATURE, ROCKS_FEATURE, SINGLE_BLOCK_FEATURE, BOULDER_FEATURE, ORE_VEIN_FEATURE,
                CAVE_ROCKS_FEATURE, ROCKWORM_NEST_FEATURE,
                METEORITE_FEATURE, RABBIT_BURROW_FEATURE, SPIDER_NEST_FEATURE);
    }

    private static String name(String name) {
        return Nightfall.MODID + ":" + name;
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<ConfiguredFeature<FC, ?>> register(String name, F feature, FC config) {
        return FeatureUtils.register(name(name), feature, config);
    }
    
    private static Holder<PlacedFeature> register(String name, Holder<? extends ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placements) {
        return PlacementUtils.register(name(name), feature, placements);
    }

    private static Holder<PlacedFeature> register(String name, Holder<? extends ConfiguredFeature<?, ?>> feature, PlacementModifier... placements) {
        return PlacementUtils.register(name(name), feature, placements);
    }

    private static RandomPatchConfiguration patch(BlockStateProvider stateProvider, int tries) {
        return FeatureUtils.simpleRandomPatchConfiguration(tries, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(stateProvider)));
    }

    private static RandomPatchConfiguration patch(BlockStateProvider stateProvider, int tries, int xzSpread, int ySpread) {
        return new RandomPatchConfiguration(tries, xzSpread, ySpread, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(stateProvider)));
    }

    private static RectangleClimateFilter cropClimateFilter(CropBlockNF crop) {
        return RectangleClimateFilter.with(crop.minTemp, crop.maxTemp, crop.minHumidity, crop.maxHumidity);
    }

    private static List<PlacementModifier> orePlacement(int minCount, int maxCount, PlacementModifier heightRange) {
        return List.of(BiomeFilter.biome(), minCount == maxCount ? CountPlacement.of(minCount) : CountPlacement.of(UniformInt.of(minCount, maxCount)), InSquarePlacement.spread(), heightRange);
    }

    private static List<BlockState> getOreKeys(Set<Stone> oreValues) {
        BlockState[] keys = new BlockState[oreValues.size()];
        int i = 0;
        for(Stone stone : oreValues) {
            keys[i] = BlocksNF.STONE_BLOCKS.get(stone).get().defaultBlockState();
            i++;
        }
        return List.of(keys);
    }
}
