package frostnox.nightfall.block.block.nest;

import frostnox.nightfall.block.block.SpiderWebBlock;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.entity.entity.monster.SpiderEntity;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.math.AxisDirection;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class SpiderNestBlock extends GuardedNestBlock {
    public static final int WEB_RADIUS_SQR = 20;

    public SpiderNestBlock(Properties pProperties) {
        super(pProperties);
    }

    public void growWebs(WorldGenLevel level, BlockPos center, int maxCycles) {
        ObjectArraySet<BlockPos> visited = new ObjectArraySet<>(WEB_RADIUS_SQR * 2);
        visited.add(center);
        ObjectArrayFIFOQueue<BlockPos> positions = new ObjectArrayFIFOQueue<>(WEB_RADIUS_SQR * 2);
        for(Direction dir : Direction.values()) {
            BlockPos adjPos = center.relative(dir);
            positions.enqueue(adjPos);
            visited.add(adjPos);
        }
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        double shortestWebDist = 0;
        int cycles = 0;
        SpiderWebBlock webBlock = BlocksNF.SPIDER_WEB.get();
        while(!positions.isEmpty()) {
            BlockPos pos = positions.dequeue();
            double distSqr = pos.distSqr(center);
            if(distSqr > WEB_RADIUS_SQR) continue;
            BlockState state = level.getBlockState(pos);
            if(webBlock.canSpreadAt(level, pos, state)) {
                if(distSqr <= shortestWebDist || cycles < maxCycles) {
                    if(distSqr > shortestWebDist) {
                        if(cycles < maxCycles) cycles++;
                        shortestWebDist = distSqr + 0.001D;
                    }
                    level.setBlock(pos, webBlock.getFullyAttachedState(level, pos), 3);
                }
                else continue;
            }
            else if(!state.is(webBlock)) {
                if(state.getMaterial().blocksMotion()) mutablePos.set(pos.getX(), pos.getY() + 1, pos.getZ());
                else mutablePos.set(pos.getX(), pos.getY() - 1, pos.getZ());
                if(!visited.contains(mutablePos)) {
                    BlockPos belowPos = mutablePos.immutable();
                    visited.add(belowPos);
                    positions.enqueue(belowPos);
                }
                continue;
            }
            for(AxisDirection dir : AxisDirection.values()) {
                mutablePos.set(pos.getX() + dir.x, pos.getY() + dir.y, pos.getZ() + dir.z);
                if(!visited.contains(mutablePos)) {
                    BlockPos neighborPos = mutablePos.immutable();
                    visited.add(neighborPos);
                    positions.enqueue(neighborPos);
                }
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        super.randomTick(state, level, pos, random);
        if(level.getBlockEntity(pos) instanceof GuardedNestBlockEntity nest) {
            if(nest.scout == null || !nest.trackedEntities.contains(nest.scout)) {
                if(nest.removeEntityToSafePos(null) != null && nest.popEntity(false) instanceof SpiderEntity spider) {
                    spider.isScout = true;
                    nest.scout = spider.getUUID();
                }
            }
            if(!nest.canRespawn() && random.nextInt(3) == 0) {
                growWebs(level, pos, 1);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.SPIDER_NEST.get().create(pos, state);
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        if(elapsedTime != Long.MAX_VALUE && chunk.getBlockEntity(pos) instanceof GuardedNestBlockEntity nest) {
            if(nest.hasAnyEntities() && (nest.scout == null || !nest.trackedEntities.contains(nest.scout))) {
                if(MathUtil.getRandomSuccesses(randomTickChance, elapsedTime, 1, random) >= 1) {
                    if(nest.removeEntityToSafePos(null) != null && nest.popEntity(false) instanceof SpiderEntity spider) {
                        spider.isScout = true;
                        nest.scout = spider.getUUID();
                    }
                }
            }
            long startTime = Math.max(0, gameTime - elapsedTime);
            long firstRespawnTime = nest.lastFullTime + nest.respawnTime;
            if(firstRespawnTime > startTime) elapsedTime -= firstRespawnTime - startTime;
            while(nest.canRespawn()) {
                IntLongPair successesAndTicks = MathUtil.getRandomSuccessesAndRemainingTrials(randomTickChance, elapsedTime, 1, random);
                if(successesAndTicks.firstInt() >= 1) {
                    nest.lastFullTime = gameTime - successesAndTicks.secondLong();
                    nest.respawnEntity(pos);
                    if(nest.scout == null || !nest.trackedEntities.contains(nest.scout)) {
                        if(nest.removeEntityToSafePos(null) != null && nest.popEntity(false) instanceof SpiderEntity spider) {
                            spider.isScout = true;
                            nest.scout = spider.getUUID();
                        }
                    }
                    if(successesAndTicks.secondLong() - nest.respawnTime <= 0) {
                        elapsedTime = successesAndTicks.secondLong();
                        break;
                    }
                    else elapsedTime = successesAndTicks.secondLong() - nest.respawnTime;
                }
                else return;
            }
            if(elapsedTime > 0) {
                IntLongPair successesAndTicks = MathUtil.getRandomSuccessesAndRemainingTrials(randomTickChance, elapsedTime, 1, random);
                if(successesAndTicks.firstInt() >= 1) {
                    nest.lastFullTime = gameTime - (elapsedTime - successesAndTicks.secondLong());
                    nest.setChanged();
                }
                growWebs(level, pos, MathUtil.getRandomSuccesses(randomTickChance / 3, elapsedTime, 8, random));
            }
        }
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 30;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }
}
