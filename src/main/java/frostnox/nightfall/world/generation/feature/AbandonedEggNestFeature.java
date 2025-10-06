package frostnox.nightfall.world.generation.feature;

import frostnox.nightfall.block.block.eggnest.EggNestBlock;
import frostnox.nightfall.block.block.eggnest.EggNestBlockEntity;
import frostnox.nightfall.data.TagsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class AbandonedEggNestFeature extends Feature<BlockStateConfiguration> {
    public AbandonedEggNestFeature(String name) {
        super(BlockStateConfiguration.CODEC);
        setRegistryName(name);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        BlockPos pos = context.origin(), posBelow = pos.below();
        WorldGenLevel level = context.level();
        BlockState blockBelow = level.getBlockState(posBelow);
        if(level.getBlockState(pos).isAir() && blockBelow.is(TagsNF.STRUCTURE_REPLACEABLE) && blockBelow.isFaceSturdy(level, posBelow, Direction.UP, SupportType.RIGID)) {
            int eggs = context.random().nextBoolean() ? 2 : 1;
            level.setBlock(pos, context.config().state.setValue(EggNestBlock.EGGS, eggs), 16 | 4);
            if(level.getBlockEntity(pos) instanceof EggNestBlockEntity nest) {
                level.getChunk(pos).markPosForPostprocessing(pos);
                for(int i = 0; i < eggs; i++) nest.hatchTimes[i] = -1;
                return true;
            }
        }
        return false;
    }
}