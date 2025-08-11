package frostnox.nightfall.world.generation.feature;

import frostnox.nightfall.block.block.StoneBlock;
import frostnox.nightfall.block.block.nest.RockwormNestBlockEntity;
import frostnox.nightfall.entity.entity.monster.RockwormEntity;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RockwormNestFeature extends Feature<NoneFeatureConfiguration> {
    public RockwormNestFeature(String name) {
        super(NoneFeatureConfiguration.CODEC);
        setRegistryName(name);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        var chunk = level.getChunk(origin);
        BlockPos.MutableBlockPos pos = origin.mutable();
        if(chunk.getBlockState(pos.setY(pos.getY() + 1)).isAir() && chunk.getBlockState(pos.setY(pos.getY() + 1)).isAir()) {
            pos.setY(origin.getY() + 1);
            int depth = context.random().nextBoolean() ? 4 : 5;
            for(int i = 0; i < depth; i++) {
                if(!(chunk.getBlockState(pos.setY(pos.getY() - 1)).getBlock() instanceof StoneBlock)) return false;
            }
            pos.setY(origin.getY() + 1);
            for(int i = 0; i < depth - 1; i++) {
                pos.setY(pos.getY() - 1);
                chunk.setBlockState(pos, BlocksNF.STONE_TUNNELS.get(((StoneBlock) chunk.getBlockState(pos).getBlock()).type).get().defaultBlockState(), false);
            }
            BlockPos nestPos = pos.setY(pos.getY() - 1).immutable();
            level.setBlock(nestPos, BlocksNF.ANCHORING_RESIN.get().defaultBlockState(), 16 | 4);
            if(level.getBlockEntity(nestPos) instanceof RockwormNestBlockEntity nest) {
                chunk.markPosForPostprocessing(nestPos);
                RockwormEntity rockworm = EntitiesNF.ROCKWORM.get().create(level.getLevel());
                rockworm.finalizeSpawn(level, level.getCurrentDifficultyAt(nestPos), MobSpawnType.STRUCTURE, null, null);
                nest.addEntity(rockworm);
                return true;
            }
        }
        return false;
    }
}