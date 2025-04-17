package frostnox.nightfall.registry.vanilla;

import com.mojang.serialization.Codec;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.world.generation.placement.*;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class PlacementModifierTypesNF {
    public static PlacementModifierType<ChanceFilter> CHANCE_FILTER;
    public static PlacementModifierType<ExposureChanceFilter> EXPOSURE_CHANCE_FILTER;
    public static PlacementModifierType<ElevationPreciseFilter> ELEVATION_PRECISE_FILTER;
    public static PlacementModifierType<NearSpawnFilter> NEAR_SPAWN_FILTER;
    public static PlacementModifierType<SurfaceHeightFilter> SURFACE_HEIGHT_FILTER;
    public static PlacementModifierType<RectangleClimateFilter> RECTANGLE_CLIMATE_FILTER;
    public static PlacementModifierType<ForestationCountPlacement> FORESTATION_COUNT_PLACEMENT;
    public static PlacementModifierType<ExposureCountPlacement> EXPOSURE_COUNT_PLACEMENT;
    public static PlacementModifierType<AdjacentScanPlacement> ADJACENT_SCAN_PLACEMENT;

    public static void register() {
        CHANCE_FILTER = register("chance_filter", ChanceFilter.CODEC);
        EXPOSURE_CHANCE_FILTER = register("exposure_chance_filter", ExposureChanceFilter.CODEC);
        ELEVATION_PRECISE_FILTER = register("elevation_precise_filter", ElevationPreciseFilter.CODEC);
        NEAR_SPAWN_FILTER = register("near_spawn_filter", NearSpawnFilter.CODEC);
        SURFACE_HEIGHT_FILTER = register("surface_height_filter", SurfaceHeightFilter.CODEC);
        RECTANGLE_CLIMATE_FILTER = register("rectangle_climate_filter", RectangleClimateFilter.CODEC);
        FORESTATION_COUNT_PLACEMENT = register("forestation_count_placement", ForestationCountPlacement.CODEC);
        EXPOSURE_COUNT_PLACEMENT = register("exposure_count_placement", ExposureCountPlacement.CODEC);
        ADJACENT_SCAN_PLACEMENT = register("adjacent_scan_placement", AdjacentScanPlacement.CODEC);
    }

    private static <T extends PlacementModifier> PlacementModifierType<T> register(String name, Codec<T> codec) {
        return Registry.register(Registry.PLACEMENT_MODIFIERS, Nightfall.MODID + ":" + name, () -> codec);
    }
}
