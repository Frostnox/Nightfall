package frostnox.nightfall.block.block.nest;

import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.util.MathUtil;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.ticks.TickPriority;

import java.util.Random;

public abstract class NestBlock extends BaseEntityBlock implements ITimeSimulatedBlock {
    public NestBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(level.getBlockEntity(pos) instanceof NestBlockEntity nest) {
            nest.randomTick(level, pos);
        }
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && !oldState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).addSimulatableBlock(TickPriority.NORMAL, pos);
            if(levelReader.getBlockEntity(pos) instanceof NestBlockEntity nest) nest.lastFullTime = level.getGameTime();
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!state.is(pNewState.getBlock()) && level.getBlockEntity(pos) instanceof NestBlockEntity nest) {
            nest.removeAllEntities(true);
        }
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).removeSimulatableBlock(TickPriority.NORMAL, pos);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        if(elapsedTime != Long.MAX_VALUE && chunk.getBlockEntity(pos) instanceof NestBlockEntity nest) {
            long startTime = Math.max(0, gameTime - elapsedTime);
            long firstRespawnTime = nest.lastFullTime + nest.respawnTime;
            if(firstRespawnTime > startTime) elapsedTime -= firstRespawnTime - startTime;
            while(nest.canRespawn()) {
                IntLongPair successesAndTicks = MathUtil.getRandomSuccessesAndRemainingTrials(randomTickChance, elapsedTime, 1, random);
                if(successesAndTicks.firstInt() >= 1) {
                    nest.lastFullTime = gameTime - successesAndTicks.secondLong();
                    nest.respawnEntity(pos);
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
            }
        }
    }
}
