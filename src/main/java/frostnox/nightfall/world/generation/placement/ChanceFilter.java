package frostnox.nightfall.world.generation.placement;

import com.mojang.serialization.Codec;
import frostnox.nightfall.registry.vanilla.PlacementModifierTypesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Random;

public class ChanceFilter extends PlacementFilter {
    public static final Codec<ChanceFilter> CODEC = ExtraCodecs.POSITIVE_FLOAT.fieldOf("chance").xmap(ChanceFilter::new, (filter) -> filter.chance).codec();
    private final float chance;

    protected ChanceFilter(float chance) {
        this.chance = chance;
    }

    public static ChanceFilter with(float chance) {
        return new ChanceFilter(chance);
    }

    @Override
    protected boolean shouldPlace(PlacementContext pContext, Random random, BlockPos pos) {
        return random.nextFloat() < chance;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierTypesNF.CHANCE_FILTER;
    }
}
