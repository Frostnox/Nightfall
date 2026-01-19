package frostnox.nightfall.block.block;

import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.math.AxisDirection;
import frostnox.nightfall.world.Weather;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.ticks.TickPriority;

import java.util.Random;
import java.util.function.Supplier;

public class MudBlock extends UnstableBlock implements ITimeSimulatedBlock {
    public MudBlock(Supplier<SoundEvent> slideSound, Properties properties) {
        super(slideSound, properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        BlockPos abovePos = pos.above();
        if(!level.isRainingAt(abovePos)) {
            BlockPos.MutableBlockPos neighborPos = pos.mutable();
            for(AxisDirection dir : AxisDirection.values()) {
                if(level.getFluidState(neighborPos.set(pos.getX() + dir.x, pos.getY() + dir.y, pos.getZ() + dir.z)).is(FluidTags.WATER)) return;
            }
            LevelChunk chunk = level.getChunkAt(pos);
            float humidity = ChunkData.isPresent(chunk) ? ChunkData.get(chunk).getHumidity(pos) : 0.5F;
            if(humidity < 0.3F && random.nextBoolean()) {
                level.setBlock(pos, BlocksNF.DIRT.get().defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && !oldState.is(this) && LevelData.isPresent(level)) {
            IChunkData chunkData = ChunkData.get(level.getChunkAt(pos));
            if(chunkData.getHumidity(pos) < 0.3F) chunkData.addSimulatableBlock(TickPriority.NORMAL, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).removeSimulatableBlock(TickPriority.NORMAL, pos);
        }
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        if(chunkData.getHumidity(pos) < 0.3F) {
            double p = randomTickChance / 2;
            if(LevelUtil.isSkyUnobstructed(level, pos.above())) p *= 1F - chunkData.getWeatherPercentageAboveIntensityOverTime(LevelData.get(level), pos, Weather.GLOBAL_CLOUDS_THRESHOLD, gameTime - elapsedTime, gameTime);
            if(MathUtil.getRandomSuccesses(p, elapsedTime, 1, random) >= 1) {
                BlockPos.MutableBlockPos neighborPos = pos.mutable();
                for(AxisDirection dir : AxisDirection.values()) {
                    if(level.getFluidState(neighborPos.set(pos.getX() + dir.x, pos.getY() + dir.y, pos.getZ() + dir.z)).is(FluidTags.WATER)) return;
                }
                level.setBlock(pos, BlocksNF.DIRT.get().defaultBlockState(), 3);
            }
        }
    }
}
