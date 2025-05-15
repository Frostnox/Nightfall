package frostnox.nightfall.world.generation.feature;

import frostnox.nightfall.block.Tree;
import frostnox.nightfall.block.block.tree.TreeTrunkBlock;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LoneTreeFeature extends Feature<NoneFeatureConfiguration> {
    public LoneTreeFeature(String name) {
        super(NoneFeatureConfiguration.CODEC);
        setRegistryName(name);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        ContinentalChunkGenerator gen = (ContinentalChunkGenerator) context.chunkGenerator();
        ChunkPos chunkPos = new ChunkPos(context.origin());
        Tree tree = Tree.chooseTree(Tree.LONE_TREES, gen.getCachedTemperature(chunkPos), gen.getCachedHumidity(chunkPos));
        if(tree == null) return false;
        TreeTrunkBlock trunk = BlocksNF.TRUNKS.get(tree).get();
        if(trunk.treeGenerator.canPlaceOnBlock(level, origin.below()) && !level.getBlockState(origin.below(2)).isAir()) {
            trunk.generateAt(level, origin, chunkPos, context.random());
            return true;
        }
        else return false;
    }
}