package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.Weather;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.TickPriority;

import java.util.Random;
import java.util.function.Supplier;

public class WallTorchBlockNF extends WallTorchBlock implements IWaterloggedBlock, ITimeSimulatedBlock {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    public final boolean lit;
    public final Supplier<? extends Block> oppositeBlock;

    public WallTorchBlockNF(boolean lit, Supplier<? extends Block> oppositeBlock, Properties pProperties) {
        super(pProperties, null);
        this.lit = lit;
        this.oppositeBlock = oppositeBlock;
        this.registerDefaultState(this.stateDefinition.any().setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(level.isRainingAt(pos) && random.nextBoolean()) level.setBlock(pos, oppositeBlock.get().defaultBlockState().setValue(FACING, state.getValue(FACING)), 3);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        tickLiquid(state, pos, level);
        return super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getLiquid(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if(state == null) return null;
        state = addLiquidToPlacement(state, context);
        if(state.getValue(WATER_LEVEL) > 0) {
            state = copyLiquid(state, oppositeBlock.get().defaultBlockState().setValue(FACING, state.getValue(FACING)));
            context.getLevel().playSound(context.getPlayer(), context.getClickedPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.2F + context.getLevel().random.nextFloat());
            if(context.getLevel() instanceof ServerLevel serverLevel) {
                Direction direction = state.getValue(FACING);
                double d0 = (double) context.getClickedPos().getX() + 0.5D;
                double d1 = (double) context.getClickedPos().getY() + 0.7D;
                double d2 = (double) context.getClickedPos().getZ() + 0.5D;
                Direction direction1 = direction.getOpposite();
                serverLevel.sendParticles(ParticleTypes.SMOKE, d0 + 0.27D * (double) direction1.getStepX(), d1 + 0.22D, d2 + 0.27D * (double) direction1.getStepZ(), 1, 0, 0, 0, 0);
            }
        }
        return state;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random pRand) {
        if(lit) {
            Direction direction = state.getValue(FACING);
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.7D;
            double d2 = (double) pos.getZ() + 0.5D;
            Direction direction1 = direction.getOpposite();
            level.addParticle(ParticleTypes.SMOKE, d0 + 0.27D * (double) direction1.getStepX(), d1 + 0.22D, d2 + 0.27D * (double) direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypesNF.FLAME_ORANGE.get(), d0 + 0.27D * (double) direction1.getStepX(), d1 + 0.22D, d2 + 0.27D * (double) direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WATER_LEVEL, WATERLOG_TYPE);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        boolean result = IWaterloggedBlock.super.placeLiquid(level, pos, state, fluid);
        if(result && level instanceof ServerLevel serverLevel && lit) { //Do this on server since client only runs this function when water is placed by the player
            level.setBlock(pos, copyLiquid(level.getBlockState(pos), oppositeBlock.get().defaultBlockState().setValue(FACING, state.getValue(FACING))), 3);
            serverLevel.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.2F + serverLevel.random.nextFloat());
            Direction direction = state.getValue(FACING);
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.7D;
            double d2 = (double) pos.getZ() + 0.5D;
            Direction direction1 = direction.getOpposite();
            serverLevel.sendParticles(ParticleTypes.SMOKE, d0 + 0.27D * (double) direction1.getStepX(), d1 + 0.22D, d2 + 0.27D * (double) direction1.getStepZ(), 1, 0, 0, 0, 0);
        }
        return result;
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && newState.isRandomlyTicking() && !oldState.is(this) && LevelData.isPresent(level)) {
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
        if(lit && LevelUtil.isSkyUnobstructed(level, pos) && MathUtil.getRandomSuccesses(randomTickChance / 2 *
                chunkData.getWeatherPercentageAboveIntensityOverTime(LevelData.get(level), pos, Weather.GLOBAL_CLOUDS_THRESHOLD, gameTime - elapsedTime, gameTime), elapsedTime, 1, random) >= 1) {
            level.setBlock(pos, oppositeBlock.get().defaultBlockState(), 3);
        }
    }
}
