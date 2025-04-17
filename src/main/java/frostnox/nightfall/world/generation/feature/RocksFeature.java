package frostnox.nightfall.world.generation.feature;

import frostnox.nightfall.block.block.ClusterBlock;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RocksFeature extends Feature<NoneFeatureConfiguration> {
    public RocksFeature(String name) {
        super(NoneFeatureConfiguration.CODEC);
        setRegistryName(name);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos pos = context.origin(), posBelow = pos.below();
        WorldGenLevel level = context.level();
        BlockState blockBelow = level.getBlockState(posBelow);
        if(level.getBlockState(pos).isAir() && blockBelow.is(TagsNF.STRUCTURE_REPLACEABLE) && blockBelow.isFaceSturdy(level, posBelow, Direction.UP, SupportType.RIGID)) {
            ContinentalChunkGenerator gen = (ContinentalChunkGenerator) context.chunkGenerator();
            boolean flint = context.random().nextInt(25) == 0 || 
                    (pos.getY() < gen.getSeaLevel() + 4 && pos.getY() > gen.getSeaLevel() - 50 && context.random().nextInt(3) == 0);
            ClusterBlock block = flint ? BlocksNF.FLINT_CLUSTER.get() : BlocksNF.ROCK_CLUSTERS.get(gen.getCachedSurfaceStone(pos)).get();
            int count;
            if(flint) count = context.random().nextBoolean() ? 1 : 2;
            else {
                float rand = context.random().nextFloat();
                if(rand < 0.4F) count = 1;
                else if(rand < 0.7F) count = 2;
                else if(rand < 0.9F) count = 3;
                else count = 4;
            }
            level.setBlock(pos, block.addLiquidToPlacementNoUpdate(block.defaultBlockState().setValue(ClusterBlock.COUNT, count), level.getFluidState(pos)), 16 | 4);
            return true;
        }
        else return false;
    }
}