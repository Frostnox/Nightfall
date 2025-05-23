package frostnox.nightfall.world.generation.feature;

import frostnox.nightfall.block.Tree;
import frostnox.nightfall.block.block.tree.TreeTrunkBlock;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.FluidState;

public class RandomTreeFeature extends Feature<NoneFeatureConfiguration> {
    public RandomTreeFeature(String name) {
        super(NoneFeatureConfiguration.CODEC);
        setRegistryName(name);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        ContinentalChunkGenerator gen = (ContinentalChunkGenerator) context.chunkGenerator();
        ChunkPos chunkPos = new ChunkPos(context.origin());
        Tree tree = Tree.pickRandomTree(gen.getCachedTreePool(chunkPos), context.random());
        if(tree == null) return false;
        TreeTrunkBlock trunk = BlocksNF.TRUNKS.get(tree).get();
        if(trunk.treeGenerator.canPlaceOnBlock(level, origin.below()) && !level.getBlockState(origin.below(2)).isAir()) {
            FluidState fluid = level.getFluidState(origin);
            //Let caedtar trees be in 2 blocks deep of water
            if(trunk.type == Tree.CAEDTAR) {
                if(!fluid.isEmpty() && fluid.is(FluidTags.WATER)) {
                    fluid = level.getFluidState(origin.above());
                    if(!fluid.isEmpty() && fluid.is(FluidTags.WATER)) {
                        if(!level.getFluidState(origin.above(2)).isEmpty()) return false;
                    }
                }
            }
            else if(!fluid.isEmpty()) return false;
            trunk.generateAt(level, origin, chunkPos, context.random());
            return true;
        }
        else return false;
    }
}