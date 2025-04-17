package frostnox.nightfall.block.block;

import frostnox.nightfall.block.IHeatable;
import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.Season;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.ticks.TickPriority;

import java.util.Random;
import java.util.function.Supplier;

public class MeltableBlock extends BlockNF implements IHeatable, ITimeSimulatedBlock {
    public final Supplier<? extends Block> meltBlock;
    public final float meltTemp;

    public MeltableBlock(Supplier<? extends Block> meltBlock, float meltTemp, Properties pProperties) {
        super(pProperties);
        this.meltBlock = meltBlock;
        this.meltTemp = meltTemp;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(LevelData.isPresent(level) && LevelData.get(level).getSeasonalTemperature(ChunkData.get(level.getChunkAt(pos)), pos) > meltTemp) {
            level.setBlockAndUpdate(pos, meltBlock.get().defaultBlockState());
        }
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && !oldState.is(this) && LevelData.isPresent(level)) {
            IChunkData chunkData = ChunkData.get(level.getChunkAt(pos));
            if(chunkData.getTemperature(pos) >= meltTemp - Season.TEMP_INFLUENCE) {
                chunkData.addSimulatableBlock(TickPriority.NORMAL, pos);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this) && LevelData.isPresent(level)) {
            IChunkData chunkData = ChunkData.get(level.getChunkAt(pos));
            if(chunkData.getTemperature(pos) >= meltTemp - Season.TEMP_INFLUENCE) {
                chunkData.removeSimulatableBlock(TickPriority.NORMAL, pos);
            }
        }
    }

    @Override
    public void applyHeat(Level level, BlockPos pos, BlockState state, TieredHeat heat, Direction fromDir) {
        if(heat != TieredHeat.NONE) level.setBlockAndUpdate(pos, meltBlock.get().defaultBlockState());
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        float temperature = chunkData.getTemperature(pos);
        float seasonalFreezeTemp = meltTemp - temperature;
        LongLongPair freezeWindow = Season.getTimesAtTemperatureInfluence(seasonalFreezeTemp);
        if(seasonalTemp > seasonalFreezeTemp && MathUtil.getRandomSuccesses(randomTickChance, Season.getTimePassedWithin(
                seasonTime, elapsedTime % (Season.YEAR_LENGTH + 1L), freezeWindow.secondLong(), seasonTime % Season.YEAR_LENGTH), 1, random) >= 1) {
            level.setBlockAndUpdate(pos, meltBlock.get().defaultBlockState());
        }
    }
}
