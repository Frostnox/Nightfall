package frostnox.nightfall.world.generation.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.registry.vanilla.PlacementModifierTypesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Random;

public class SurfaceHeightFilter extends PlacementFilter {
    public static final Codec<SurfaceHeightFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("minY").forGetter(placement -> placement.minY),
            Codec.INT.fieldOf("maxY").forGetter(placement -> placement.maxY)
    ).apply(instance, SurfaceHeightFilter::new));
    private final int minY, maxY;

    private SurfaceHeightFilter(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }

    public static SurfaceHeightFilter with(int minY, int maxY) {
        return new SurfaceHeightFilter(minY, maxY);
    }

    @Override
    protected boolean shouldPlace(PlacementContext pContext, Random random, BlockPos pos) {
        int y = pContext.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ());
        return y >= minY && y <= maxY;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierTypesNF.SURFACE_HEIGHT_FILTER;
    }
}
