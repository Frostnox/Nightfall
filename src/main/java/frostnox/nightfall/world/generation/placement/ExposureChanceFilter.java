package frostnox.nightfall.world.generation.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.registry.vanilla.PlacementModifierTypesNF;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Random;

public class ExposureChanceFilter extends PlacementFilter {
    public static final Codec<ExposureChanceFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("chance").forGetter(placement -> placement.chance),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("minChance").forGetter(placement -> placement.minChance)
    ).apply(instance, ExposureChanceFilter::new));
    private final float chance, minChance;

    protected ExposureChanceFilter(float chance, float minChance) {
        this.chance = chance;
        this.minChance = minChance;
    }

    public static ExposureChanceFilter with(float chance, float minChance) {
        return new ExposureChanceFilter(chance, minChance);
    }

    @Override
    protected boolean shouldPlace(PlacementContext pContext, Random random, BlockPos pos) {
        ContinentalChunkGenerator gen = (ContinentalChunkGenerator) pContext.generator();
        return random.nextFloat() < Math.max(minChance, gen.getCachedExposure(new ChunkPos(pos)) * chance);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierTypesNF.EXPOSURE_CHANCE_FILTER;
    }
}
