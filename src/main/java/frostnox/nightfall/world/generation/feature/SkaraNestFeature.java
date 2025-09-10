package frostnox.nightfall.world.generation.feature;

import frostnox.nightfall.block.block.StoneBlock;
import frostnox.nightfall.block.block.nest.SkaraNestBlockEntity;
import frostnox.nightfall.entity.entity.monster.SkaraSwarmEntity;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SkaraNestFeature extends Feature<NoneFeatureConfiguration> {
    public SkaraNestFeature(String name) {
        super(NoneFeatureConfiguration.CODEC);
        setRegistryName(name);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos pos = context.origin();
        WorldGenLevel level = context.level();
        if(level.getBlockState(pos).getBlock() == Blocks.CAVE_AIR) {
            if(level.getBlockState(pos.below()).getBlock() instanceof StoneBlock stone) {
                level.setBlock(pos, BlocksNF.SKARA_ROCK_CLUSTERS.get(stone.type).get().defaultBlockState(), 16 | 4);
                if(level.getBlockEntity(pos) instanceof SkaraNestBlockEntity nest) {
                    level.getChunk(pos).markPosForPostprocessing(pos);
                    SkaraSwarmEntity skara = EntitiesNF.SKARA_SWARM.get().create(level.getLevel());
                    skara.setHomePos(pos);
                    skara.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
                    nest.addEntity(skara);
                    return true;
                }
            }
        }
        return false;
    }
}