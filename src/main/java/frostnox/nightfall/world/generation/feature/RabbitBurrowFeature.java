package frostnox.nightfall.world.generation.feature;

import frostnox.nightfall.block.block.nest.NestBlockEntity;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.entity.animal.RabbitEntity;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RabbitBurrowFeature extends Feature<NoneFeatureConfiguration> {
    public RabbitBurrowFeature(String name) {
        super(NoneFeatureConfiguration.CODEC);
        setRegistryName(name);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos pos = context.origin();
        WorldGenLevel level = context.level();
        if(!level.isEmptyBlock(pos) || !level.getBlockState(pos.below()).is(TagsNF.TILLABLE_SOIL)) return false;
        ContinentalChunkGenerator gen = (ContinentalChunkGenerator) context.chunkGenerator();
        ChunkPos chunkPos = new ChunkPos(context.origin());
        level.setBlock(pos, BlocksNF.RABBIT_BURROW.get().defaultBlockState(), 16 | 4);
        if(level.getBlockEntity(pos) instanceof NestBlockEntity burrow) {
            level.getChunk(chunkPos.x, chunkPos.z).markPosForPostprocessing(pos);
            RabbitEntity rabbit = EntitiesNF.RABBIT.get().create(level.getLevel());
            rabbit.setHomePos(pos);
            rabbit.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE,
                    RabbitEntity.GroupData.create(gen.getCachedTemperature(chunkPos), gen.getCachedHumidity(chunkPos)), null);
            burrow.addEntity(rabbit);
        }
        return true;
    }
}