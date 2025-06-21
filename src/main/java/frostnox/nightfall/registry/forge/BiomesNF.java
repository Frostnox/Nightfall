package frostnox.nightfall.registry.forge;

import frostnox.nightfall.Nightfall;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.sounds.Music;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BiomesNF {
    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, Nightfall.MODID);
    public static final RegistryObject<Biome> TUNDRA = BIOMES.register("tundra", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.ICY).temperature(0.0F).downfall(0.0F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x414D6B).waterFogColor(0x212739)
                        .fogColor(0xC0D8FF).skyColor(0x78A8FF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(landFeatureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> TAIGA = BIOMES.register("taiga", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.TAIGA).temperature(0.0F).downfall(0.5F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x414D6B).waterFogColor(0x212739)
                        .fogColor(0xC0D8FF).skyColor(0x729DFF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(landFeatureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> OLDWOODS = BIOMES.register("oldwoods", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.EXTREME_HILLS).temperature(0.0F).downfall(1.0F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x38385F).waterFogColor(0x1C1C37)
                        .fogColor(0xC0D8FF).skyColor(0x6984FA)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(landFeatureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> GRASSLANDS = BIOMES.register("grasslands", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.PLAINS).temperature(0.5F).downfall(0.0F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x273752).waterFogColor(0x121B2B)
                        .fogColor(0xC0D8FF).skyColor(0x6494FF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(landFeatureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> FOREST = BIOMES.register("forest", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.FOREST).temperature(0.5F).downfall(0.5F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x273752).waterFogColor(0x121B2B)
                        .fogColor(0xC0D8FF).skyColor(0x6E99FF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
                        .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(landFeatureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> JUNGLE = BIOMES.register("jungle", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.JUNGLE).temperature(0.5F).downfall(1.0F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x26345B).waterFogColor(0x151D35)
                        .fogColor(0xC0D8FF).skyColor(0x5E99FF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(landFeatureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> DESERT = BIOMES.register("desert", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.DESERT).temperature(1.0F).downfall(0.0F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x254455).waterFogColor(0x11212B)
                        .fogColor(0xC0D8FF).skyColor(0x76A6FF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(landFeatureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> BADLANDS = BIOMES.register("badlands", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.MESA).temperature(1.0F).downfall(0.5F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x254455).waterFogColor(0x11212B)
                        .fogColor(0xC0D8FF).skyColor(0x84A6FF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(landFeatureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> SWAMP = BIOMES.register("swamp", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.SWAMP).temperature(1.0F).downfall(1.0F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x50665A).waterFogColor(0x26332A)
                        .fogColor(0xC0D8FF).skyColor(0x6AA8FF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(landFeatureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> OCEAN = BIOMES.register("ocean", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.OCEAN).temperature(0.5F).downfall(0.5F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x31677F).waterFogColor(0x05182B)
                        .fogColor(0xC0D8FF).skyColor(0x6496FF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(new Music(SoundsNF.MUSIC_OCEAN.get(), 20 * 60 * 5, 20 * 60 * 10, false)).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(featureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> ISLAND = BIOMES.register("island", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.BEACH).temperature(0.5F).downfall(0.5F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x21807D).waterFogColor(0x165755)
                        .fogColor(0xC0D8FF).skyColor(0x6193FF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(landFeatureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> TUNNELS = BIOMES.register("tunnels", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.UNDERGROUND).temperature(0.0F).downfall(0.0F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x254455).waterFogColor(0x11212B)
                        .fogColor(0xC0D8FF).skyColor(0x76A6FF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(new Music(SoundsNF.MUSIC_TUNNELS.get(), 20 * 60 * 10, 20 * 60 * 15, false)).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(featureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> CAVERNS = BIOMES.register("caverns", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.UNDERGROUND).temperature(0.0F).downfall(0.0F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x254455).waterFogColor(0x11212B)
                        .fogColor(0xC0D8FF).skyColor(0x76A6FF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(new Music(SoundsNF.MUSIC_CAVERNS.get(), 20 * 60 * 10, 20 * 60 * 15, false)).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(featureBuilder().build()).build();
    });
    public static final RegistryObject<Biome> DEPTHS = BIOMES.register("depths", () -> {
        return new Biome.BiomeBuilder().precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.UNDERGROUND).temperature(0.0F).downfall(0.0F)
                .specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x254455).waterFogColor(0x11212B)
                        .fogColor(0xC0D8FF).skyColor(0x76A6FF)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(new Music(SoundsNF.MUSIC_DEPTHS.get(), 20 * 60 * 10, 20 * 60 * 15, false)).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(featureBuilder().build()).build();
    });

    public static HolderSet<Biome> getDefaultBiomes(RegistryAccess registryAccess) {
        var registry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        return HolderSet.direct(registry.getHolderOrThrow(BiomesNF.TUNDRA.getKey()), registry.getHolderOrThrow(BiomesNF.TAIGA.getKey()),
                registry.getHolderOrThrow(BiomesNF.OLDWOODS.getKey()), registry.getHolderOrThrow(BiomesNF.GRASSLANDS.getKey()),
                registry.getHolderOrThrow(BiomesNF.FOREST.getKey()), registry.getHolderOrThrow(BiomesNF.JUNGLE.getKey()),
                registry.getHolderOrThrow(BiomesNF.DESERT.getKey()), registry.getHolderOrThrow(BiomesNF.BADLANDS.getKey()),
                registry.getHolderOrThrow(BiomesNF.SWAMP.getKey()), registry.getHolderOrThrow(BiomesNF.OCEAN.getKey()),
                registry.getHolderOrThrow(BiomesNF.ISLAND.getKey()), registry.getHolderOrThrow(BiomesNF.TUNNELS.getKey()),
                registry.getHolderOrThrow(BiomesNF.CAVERNS.getKey()), registry.getHolderOrThrow(BiomesNF.DEPTHS.getKey()));
    }

    public static BiomeGenerationSettings.Builder featureBuilder() {
        return new BiomeGenerationSettings.Builder().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, FeaturesNF.FIRE_CLAY)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, FeaturesNF.TIN_VEIN)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, FeaturesNF.COPPER_VEIN)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, FeaturesNF.AZURITE_VEIN)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, FeaturesNF.HEMATITE_VEIN)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, FeaturesNF.COAL_VEIN)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, FeaturesNF.HALITE_VEIN)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, FeaturesNF.SURFACE_ROCKS)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, FeaturesNF.SEASHELLS)
                .addFeature(GenerationStep.Decoration.RAW_GENERATION, FeaturesNF.METEORITE)
                .addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, FeaturesNF.SPIDER_NEST_CAVES);
    }

    public static BiomeGenerationSettings.Builder landFeatureBuilder() {
        return featureBuilder().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, FeaturesNF.TREE)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, FeaturesNF.LONE_TREE)
                .addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, FeaturesNF.BOULDER)
                .addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, FeaturesNF.RABBIT_BURROW)
                .addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, FeaturesNF.SPIDER_NEST_SURFACE);
    }

    public static void register() {
        BIOMES.register(Nightfall.MOD_EVENT_BUS);
    }
}
