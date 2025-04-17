package frostnox.nightfall.world.generation.feature;

import frostnox.nightfall.block.IWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class SingleBlockFeature extends Feature<BlockStateConfiguration> {
    public SingleBlockFeature(String name) {
        super(BlockStateConfiguration.CODEC);
        setRegistryName(name);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        BlockPos pos = context.origin();
        WorldGenLevel level = context.level();
        BlockState state = context.config().state;
        if(state.getBlock() instanceof IWaterloggedBlock block) {
            level.setBlock(pos, block.addLiquidToPlacementNoUpdate(state, level.getFluidState(pos)), 16 | 4);
        }
        else level.setBlock(pos, state, 16 | 4);
        return true;
    }
}