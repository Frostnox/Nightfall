package frostnox.nightfall.block.block.pile;

import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.block.block.TimeDataBlockEntity;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public class DryingPileBlock extends PileBlock implements EntityBlock, ITimeSimulatedBlock {
    public final Supplier<? extends PileBlock> dryBlock;
    public final int ticksToDry, minLightToDry;

    public DryingPileBlock(Supplier<? extends Item> drop, Supplier<? extends PileBlock> dryBlock, int ticksToDry, int minLightToDry, VoxelShape shapeZ, Properties properties) {
        super(drop, shapeZ, properties);
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
        if(level.getBlockEntity(pos) instanceof TimeDataBlockEntity pile) {
            if(!level.isRainingAt(pos) && level.getRawBrightness(pos, 0) >= minLightToDry) {
                LevelChunk chunk = level.getChunkAt(pos);
                float humidity = ChunkData.isPresent(chunk) ? ChunkData.get(chunk).getHumidity(pos) : 0.5F;
                if(level.getGameTime() - pile.lastProcessedTick >= ticksToDry * (1.5 - humidity)) {
                    level.setBlock(pos, dryBlock.get().defaultBlockState().setValue(COUNT, state.getValue(COUNT)).setValue(AXIS, state.getValue(AXIS)), 3);
                }
            }
            else pile.lastProcessedTick = level.getGameTime();
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
