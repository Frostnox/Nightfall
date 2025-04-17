package frostnox.nightfall.world.generation.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.registry.vanilla.PlacementModifierTypesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.storage.LevelData;

import java.util.Random;

public class NearSpawnFilter extends PlacementFilter {
    public static final Codec<NearSpawnFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("range").forGetter(placement -> placement.rangeSqr)
    ).apply(instance, NearSpawnFilter::new));
    private final float rangeSqr;

    private NearSpawnFilter(float rangeSqr) {
        this.rangeSqr = rangeSqr;
    }

    public static NearSpawnFilter with(float range) {
        return new NearSpawnFilter(range * range);
    }

    @Override
    protected boolean shouldPlace(PlacementContext pContext, Random random, BlockPos pos) {
        LevelData data = pContext.getLevel().getLevelData();
        return pos.distToCenterSqr(data.getXSpawn(), data.getYSpawn(), data.getZSpawn()) > rangeSqr;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierTypesNF.NEAR_SPAWN_FILTER;
    }
}
