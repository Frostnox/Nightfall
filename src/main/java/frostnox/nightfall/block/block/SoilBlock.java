package frostnox.nightfall.block.block;

import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.block.SoilCover;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.util.DataUtil;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.ToolActionsNF;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

public class SoilBlock extends UnstableBlock implements ITimeSimulatedBlock {
    protected final Lazy<Map<SoilCover, BlockState>> coverMap;
    protected final RegistryObject<? extends TilledSoilBlock> tilledSoilBlock;

    public SoilBlock(Supplier<SoundEvent> slideSound, Map<SoilCover, RegistryObject<? extends CoveredSoilBlock>> map, RegistryObject<? extends TilledSoilBlock> tilledSoilBlock, Properties properties) {
        super(slideSound, properties);
        coverMap = Lazy.of(() -> DataUtil.mapEnum(SoilCover.class, cover -> map.get(cover).get().defaultBlockState()));
        this.tilledSoilBlock = tilledSoilBlock;
    }

    public @Nullable BlockState getCoveredBlock(SoilCover cover) {
        return coverMap.get().get(cover);
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if(toolAction == ToolActionsNF.TILL) {
            if(context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
                return tilledSoilBlock.get().defaultBlockState();
            }
        }
        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if(facing == Direction.UP && !level.isClientSide() && LevelData.isPresent((Level) level)) {
            if(facingState.getLightBlock(level, facingPos) < 15) ChunkData.get(((Level) level).getChunkAt(pos)).addSimulatableBlock(TickPriority.NORMAL, pos);
            else ChunkData.get(((Level) level).getChunkAt(pos)).removeSimulatableBlock(TickPriority.NORMAL, pos);
        }
        return state;
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && !oldState.is(this) && LevelData.isPresent(level)) {
            BlockPos abovePos = pos.above();
            if(level.getBlockState(abovePos).getLightBlock(level, abovePos) < 15) {
                ChunkData.get(level.getChunkAt(pos)).addSimulatableBlock(TickPriority.NORMAL, pos);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).removeSimulatableBlock(TickPriority.NORMAL, pos);
        }
    }

    protected void tryGrowCover(BlockState state, ServerLevel level, BlockPos pos, BlockPos abovePos, BlockState aboveState, int light) {
        if(aboveState.getFluidState().isEmpty() && LayerLightEngine.getLightBlockInto(level, state, pos, aboveState, abovePos, Direction.UP, aboveState.getLightBlock(level, abovePos)) < level.getMaxLightLevel()) {
            //Use exact biome since level only saves biomes in 4x4x4 pieces that are blended together
            SoilCover cover = SoilCover.getForBiome(level.getChunkSource().getGenerator() instanceof ContinentalChunkGenerator chunkGen ?
                    chunkGen.calculateBiome(pos.getX(), 832, pos.getZ()).getHolder().get() : level.getBiome(pos));
            BlockState coveredBlock = getCoveredBlock(cover);
            if(coveredBlock != null && cover.canGrow(light)) level.setBlockAndUpdate(pos, coveredBlock);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        BlockPos abovePos = pos.above();
        int light = level.getBrightness(LightLayer.SKY, abovePos);
        if(light > 0 && LevelUtil.isDay(level) && random.nextInt(10) == 0) tryGrowCover(state, level, pos, abovePos, level.getBlockState(abovePos), light);
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        BlockPos abovePos = pos.above();
        int light = level.getBrightness(LightLayer.SKY, abovePos);
        if(light > 0 && MathUtil.getRandomSuccesses(randomTickChance / 10, LevelUtil.getDayTimePassed(level, dayTime, elapsedTime), 1, random) >= 1) {
            tryGrowCover(state, level, pos, abovePos, chunk.getBlockState(abovePos), light);
        }
    }
}
