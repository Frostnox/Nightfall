package frostnox.nightfall.world.generation.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.registry.vanilla.PlacementModifierTypesNF;
import frostnox.nightfall.util.math.Easing;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class ForestationCountPlacement extends NoiseCountPlacement {
    public static final Codec<ForestationCountPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("count").forGetter(placement -> placement.count),
            Codec.INT.fieldOf("min").forGetter(placement -> placement.min)
    ).apply(instance, ForestationCountPlacement::new));

    protected ForestationCountPlacement(float count, int min) {
        super(count, min);
    }

    @Override
    protected float getScalar(ContinentalChunkGenerator gen, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        float humidity = gen.getCachedHumidity(chunkPos) * 0.65F;
        float temperature = (1 - Math.abs(gen.getCachedTemperature(chunkPos) - 0.5F) * 2) * 0.35F;
        return Easing.inOutSine.apply(humidity + temperature) + gen.getForestation(pos.getX(), pos.getZ()) * 0.3F;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierTypesNF.FORESTATION_COUNT_PLACEMENT;
    }

    public static ForestationCountPlacement of(float count) {
        return new ForestationCountPlacement(count, 0);
    }

    public static ForestationCountPlacement of(float count, int min) {
        return new ForestationCountPlacement(count, min);
    }
}
