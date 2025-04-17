package frostnox.nightfall.block.block;

import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.Season;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;

import java.util.Random;

public class SnowBlock extends BlockNF implements ITimeSimulatedBlock {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    
    public SnowBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if((LevelData.isPresent(level) && LevelData.get(level).getSeasonalTemperature(ChunkData.get(level.getChunkAt(pos)), pos) > LevelData.SNOW_TEMP)
                || level.getBrightness(LightLayer.BLOCK, pos) > 12) level.removeBlock(pos, false);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
    
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        return Block.isFaceFull(level.getBlockState(belowPos).getBlockSupportShape(level, belowPos), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && !oldState.is(this) && LevelData.isPresent(level)) {
            IChunkData chunkData = ChunkData.get(level.getChunkAt(pos));
            if(chunkData.getTemperature(pos) >= LevelData.SNOW_TEMP - Season.TEMP_INFLUENCE) {
                chunkData.addSimulatableBlock(TickPriority.NORMAL, pos);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this) && LevelData.isPresent(level)) {
            IChunkData chunkData = ChunkData.get(level.getChunkAt(pos));
            if(chunkData.getTemperature(pos) >= LevelData.SNOW_TEMP - Season.TEMP_INFLUENCE) {
                chunkData.removeSimulatableBlock(TickPriority.NORMAL, pos);
            }
        }
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        float temperature = chunkData.getTemperature(pos);
        float seasonalFreezeTemp = LevelData.SNOW_TEMP - temperature;
        LongLongPair freezeWindow = Season.getTimesAtTemperatureInfluence(seasonalFreezeTemp);
        if(seasonalTemp > seasonalFreezeTemp && MathUtil.getRandomSuccesses(randomTickChance, Season.getTimePassedWithin(
                seasonTime, elapsedTime % (Season.YEAR_LENGTH + 1L), freezeWindow.secondLong(), seasonTime % Season.YEAR_LENGTH), 1, random) >= 1) {
            level.removeBlock(pos, false);
        }
    }
}
