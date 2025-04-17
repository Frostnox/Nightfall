package frostnox.nightfall.block.block;

import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.ILevelData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.Season;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;

import java.util.Random;

public class FrazilBlock extends BlockNF implements ITimeSimulatedBlock {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
    public final float meltTemp;

    public FrazilBlock(float meltTemp, Properties properties) {
        super(properties);
        this.meltTemp = meltTemp;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(LevelData.isPresent(level)) {
            ILevelData levelData = LevelData.get(level);
            BlockPos belowPos = pos.below();
            float temp = levelData.getSeasonalTemperature(ChunkData.get(level.getChunkAt(belowPos)), belowPos);
            if(temp > meltTemp) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
            else {
                if(temp <= LevelData.WATER_FREEZE_TEMP) {
                    Block water = level.getBlockState(belowPos).getBlock();
                    if(water == BlocksNF.WATER.get()) level.setBlockAndUpdate(belowPos, BlocksNF.ICE.get().defaultBlockState());
                    else if(water == BlocksNF.SEAWATER.get() && temp <= LevelData.SEAWATER_FREEZE_TEMP) level.setBlockAndUpdate(belowPos, BlocksNF.SEA_ICE.get().defaultBlockState());
                }
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        FluidState fluid = level.getFluidState(pos.below());
        return fluid.isSource() && fluid.is(FluidTags.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if(!state.canSurvive(level, pos)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 19);
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        float temperature = chunkData.getTemperature(pos);
        float seasonalFreezeTemp = meltTemp - temperature;
        LongLongPair freezeWindow = Season.getTimesAtTemperatureInfluence(seasonalFreezeTemp);
        if(seasonalTemp > seasonalFreezeTemp && MathUtil.getRandomSuccesses(randomTickChance, Season.getTimePassedWithin(
                seasonTime, elapsedTime % (Season.YEAR_LENGTH + 1L), freezeWindow.secondLong(), seasonTime % Season.YEAR_LENGTH), 1, random) >= 1) {
            level.removeBlock(pos, false);
        }
        else {
            BlockPos belowPos = pos.below();
            Block water = level.getBlockState(belowPos).getBlock();
            if(water == BlocksNF.WATER.get()) {
                LongLongPair iceWindow = Season.getTimesAtTemperatureInfluence(LevelData.WATER_FREEZE_TEMP - temperature);
                if(MathUtil.getRandomSuccesses(randomTickChance, Season.getTimePassedWithin(seasonTime, elapsedTime, iceWindow.firstLong(), iceWindow.secondLong()), 1, random) >= 1) {
                    level.setBlock(belowPos, BlocksNF.ICE.get().defaultBlockState(), 18);
                }
            }
            else if(water == BlocksNF.SEAWATER.get()) {
                LongLongPair iceWindow = Season.getTimesAtTemperatureInfluence(LevelData.SEAWATER_FREEZE_TEMP - temperature);
                if(MathUtil.getRandomSuccesses(randomTickChance, Season.getTimePassedWithin(seasonTime, elapsedTime, iceWindow.firstLong(), iceWindow.secondLong()), 1, random) >= 1) {
                    level.setBlock(belowPos, BlocksNF.SEA_ICE.get().defaultBlockState(), 18);
                }
            }
        }
    }
}
