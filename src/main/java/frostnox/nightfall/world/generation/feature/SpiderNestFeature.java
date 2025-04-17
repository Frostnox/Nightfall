package frostnox.nightfall.world.generation.feature;

import frostnox.nightfall.block.block.nest.GuardedNestBlockEntity;
import frostnox.nightfall.block.block.nest.NestBlockEntity;
import frostnox.nightfall.entity.entity.monster.SpiderEntity;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Collections;
import java.util.List;

public class SpiderNestFeature extends Feature<NoneFeatureConfiguration> {
    public SpiderNestFeature(String name) {
        super(NoneFeatureConfiguration.CODEC);
        setRegistryName(name);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos pos = context.origin();
        WorldGenLevel level = context.level();
        if(!level.isEmptyBlock(pos) || !level.getBlockState(pos.below()).getMaterial().blocksMotion()) return false;
        List<Direction> directions = new ObjectArrayList<>(LevelUtil.HORIZONTAL_DIRECTIONS);
        Collections.shuffle(directions, context.random());
        Direction exitDirection = null;
        SpiderEntity testSpider = EntitiesNF.SPIDER.get().create(level.getLevel());
        BlockPos.MutableBlockPos mutablePos = pos.mutable();
        for(Direction direction : directions) {
            mutablePos.set(pos.getX() + direction.getStepX(), pos.getY(), pos.getZ() + direction.getStepZ());
            testSpider.moveTo(mutablePos.getX() + 0.5, mutablePos.getY(), mutablePos.getZ() + 0.5);
            if(NestBlockEntity.canPlaceEntityAt(testSpider, mutablePos, level)) {
                exitDirection = direction;
                break;
            }
        }
        if(exitDirection == null) return false;
        ContinentalChunkGenerator gen = (ContinentalChunkGenerator) context.chunkGenerator();
        ChunkPos chunkPos = new ChunkPos(context.origin());
        level.setBlock(pos, BlocksNF.SPIDER_NEST.get().defaultBlockState(), 16 | 4);
        if(level.getBlockEntity(pos) instanceof GuardedNestBlockEntity nest) {
            level.getChunk(chunkPos.x, chunkPos.z).markPosForPostprocessing(pos);
            while(nest.canRespawn()) {
                SpiderEntity spider = EntitiesNF.SPIDER.get().create(level.getLevel());
                spider.setHomePos(pos);
                spider.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE,
                        SpiderEntity.GroupData.create(gen.getCachedHumidity(chunkPos)), null);
                if(nest.scout == null) {
                    spider.isScout = true;
                    nest.scout = spider.getUUID();
                    nest.startTrackingEntity(spider.getUUID());
                    spider.moveTo(pos.relative(exitDirection), exitDirection.toYRot() % 360, 0);
                    spider.setYBodyRot(spider.getYRot());
                    spider.setYHeadRot(spider.getYRot());
                    level.addFreshEntity(spider);
                    spider.setYRot(spider.getYRot());
                }
                else nest.addEntity(spider);
            }
            BlocksNF.SPIDER_NEST.get().growWebs(level, pos, 8);
        }
        return true;
    }
}