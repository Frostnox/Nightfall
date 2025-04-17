package frostnox.nightfall.world.generation.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.registry.vanilla.PlacementModifierTypesNF;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class ExposureCountPlacement extends NoiseCountPlacement {
    public static final Codec<ExposureCountPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("count").forGetter(placement -> placement.count),
            Codec.INT.fieldOf("min").forGetter(placement -> placement.min)
    ).apply(instance, ExposureCountPlacement::new));

    private ExposureCountPlacement(float count, int min) {
        super(count, min);
    }

    @Override
    protected float getScalar(ContinentalChunkGenerator gen, BlockPos pos) {
        return gen.getCachedExposure(new ChunkPos(pos)) + 0.5F;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierTypesNF.EXPOSURE_COUNT_PLACEMENT;
    }

    public static ExposureCountPlacement of(float count) {
        return new ExposureCountPlacement(count, 0);
    }

    public static ExposureCountPlacement of(float count, int min) {
        return new ExposureCountPlacement(count, min);
    }
}
