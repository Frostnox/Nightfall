package frostnox.nightfall.world.generation.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.registry.vanilla.PlacementModifierTypesNF;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Random;

public class ElevationPreciseFilter extends PlacementFilter {
    public static final Codec<ElevationPreciseFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("elevationMin").forGetter(placement -> placement.elevationMin),
            Codec.FLOAT.fieldOf("elevationMax").forGetter(placement -> placement.elevationMax)
    ).apply(instance, ElevationPreciseFilter::new));
    private final float elevationMin, elevationMax;

    private ElevationPreciseFilter(float elevationMin, float elevationMax) {
        this.elevationMin = elevationMin;
        this.elevationMax = elevationMax;
    }

    public static ElevationPreciseFilter with(float elevationMin, float elevationMax) {
        return new ElevationPreciseFilter(elevationMin, elevationMax);
    }

    @Override
    protected boolean shouldPlace(PlacementContext pContext, Random random, BlockPos pos) {
        float elevation = ((ContinentalChunkGenerator) pContext.generator()).getElevation(pos.getX(), pos.getZ());
        return elevation >= elevationMin && elevation <= elevationMax;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierTypesNF.ELEVATION_PRECISE_FILTER;
    }
}
