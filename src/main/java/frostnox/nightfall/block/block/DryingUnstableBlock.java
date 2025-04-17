package frostnox.nightfall.block.block;

import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.math.AxisDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public class DryingUnstableBlock extends UnstableBlock implements EntityBlock, ITimeSimulatedBlock {
    public final Supplier<? extends Block> dryBlock;
    public final int ticksToDry, minLightToDry;

    public DryingUnstableBlock(Supplier<? extends Block> dryBlock, int ticksToDry, int minLightToDry, Supplier<SoundEvent> slideSound, Properties properties) {
        super(slideSound, properties);
        this.dryBlock = dryBlock;
        this.ticksToDry = ticksToDry;
        this.minLightToDry = minLightToDry;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(level.getBlockEntity(pos) instanceof TimeDataBlockEntity entity) {
            BlockPos abovePos = pos.above();
            if(!level.isRainingAt(abovePos)) {
                boolean canDry = false;
                BlockPos.MutableBlockPos neighborPos = pos.mutable();
                for(AxisDirection dir : AxisDirection.values()) {
                    if(!level.getFluidState(neighborPos.set(pos.getX() + dir.x, pos.getY() + dir.y, pos.getZ() + dir.z)).is(FluidTags.WATER)
                            && level.getRawBrightness(neighborPos, 0) >= minLightToDry) {
                        canDry = true;
                        break;
                    }
                }
                if(canDry) {
                    LevelChunk chunk = level.getChunkAt(pos);
                    float humidity = ChunkData.isPresent(chunk) ? ChunkData.get(chunk).getHumidity(pos) : 0.5F;
                    if(level.getGameTime() - entity.lastProcessedTick >= ticksToDry * (1.5 - humidity)) {
                        level.setBlock(pos, dryBlock.get().defaultBlockState(), 3);
                    }
                }
                else entity.lastProcessedTick = level.getGameTime();
            }
            else entity.lastProcessedTick = level.getGameTime();
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.TIME_DATA.get().create(pos, state);
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && !oldState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).addSimulatableBlock(TickPriority.NORMAL, pos);
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
        //Assumes weather at current time and not time of random tick
        if(MathUtil.getRandomSuccesses(randomTickChance, elapsedTime, 1, random) >= 1) {
            randomTick(state, level, pos, random);
        }
    }
}
