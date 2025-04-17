package frostnox.nightfall.world.generation.placement;

import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class NoiseCountPlacement extends PlacementModifier {
    protected final float count;
    protected final int min;

    protected NoiseCountPlacement(float count, int min) {
        this.count = count;
        this.min = min;
    }

    protected abstract float getScalar(ContinentalChunkGenerator gen, BlockPos pos);

    @Override
    public Stream<BlockPos> getPositions(PlacementContext pContext, Random random, BlockPos pos) {
        ContinentalChunkGenerator gen = (ContinentalChunkGenerator) pContext.generator();
        float scalar = getScalar(gen, pos);
        return IntStream.range(0, scalar < 0 ? min : Math.max(min, Math.round(scalar * count))).mapToObj((i) -> pos);
    }
}
