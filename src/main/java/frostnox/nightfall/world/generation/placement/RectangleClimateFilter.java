package frostnox.nightfall.world.generation.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.registry.vanilla.PlacementModifierTypesNF;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Random;

public class RectangleClimateFilter extends PlacementFilter {
    public static final Codec<RectangleClimateFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("tempMin").forGetter(placement -> placement.tempMin),
            Codec.FLOAT.fieldOf("tempMax").forGetter(placement -> placement.tempMax),
            Codec.FLOAT.fieldOf("humidityMin").forGetter(placement -> placement.humidityMin),
            Codec.FLOAT.fieldOf("humidityMax").forGetter(placement -> placement.humidityMax)
    ).apply(instance, RectangleClimateFilter::new));
    private final float tempMin, tempMax, humidityMin, humidityMax;

    private RectangleClimateFilter(float tempMin, float tempMax, float humidityMin, float humidityMax) {
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.humidityMin = humidityMin;
        this.humidityMax = humidityMax;
    }

    public static RectangleClimateFilter with(float tempMin, float tempMax, float humidityMin, float humidityMax) {
        return new RectangleClimateFilter(tempMin, tempMax, humidityMin, humidityMax);
    }

    @Override
    protected boolean shouldPlace(PlacementContext pContext, Random random, BlockPos pos) {
        ContinentalChunkGenerator gen = (ContinentalChunkGenerator) pContext.generator();
        ChunkPos chunkPos = new ChunkPos(pos);
        float temp = gen.getCachedTemperature(chunkPos);
        if(temp >= tempMin && temp <= tempMax) {
            float humidity = gen.getCachedHumidity(chunkPos);
            return humidity >= humidityMin && humidity <= humidityMax;
        }
        else return false;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierTypesNF.RECTANGLE_CLIMATE_FILTER;
    }
}
