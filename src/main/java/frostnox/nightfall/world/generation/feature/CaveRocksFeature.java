package frostnox.nightfall.world.generation.feature;

import frostnox.nightfall.block.block.ClusterBlock;
import frostnox.nightfall.block.block.StoneBlock;
import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CaveRocksFeature extends Feature<NoneFeatureConfiguration> {
    public CaveRocksFeature(String name) {
        super(NoneFeatureConfiguration.CODEC);
        setRegistryName(name);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos pos = context.origin();
        WorldGenLevel level = context.level();
        if(level.getBlockState(pos).getBlock() == Blocks.CAVE_AIR) {
            if(level.getBlockState(pos.below()).getBlock() instanceof StoneBlock stone) {
                ClusterBlock block = BlocksNF.ROCK_CLUSTERS.get(stone.type).get();
                int count;
                float rand = context.random().nextFloat();
                if(rand < 0.4F) count = 1;
                else if(rand < 0.7F) count = 2;
                else if(rand < 0.9F) count = 3;
                else count = 4;
                level.setBlock(pos, block.addLiquidToPlacementNoUpdate(block.defaultBlockState().setValue(ClusterBlock.COUNT, count), level.getFluidState(pos)), 16 | 4);
                return true;
            }
        }
        return false;
    }
}