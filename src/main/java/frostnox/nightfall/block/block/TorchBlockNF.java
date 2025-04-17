package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;

import java.util.Random;
import java.util.function.Supplier;

public class TorchBlockNF extends TorchBlock implements IWaterloggedBlock {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    public final boolean lit;
    public final Supplier<? extends Block> oppositeBlock;

    public TorchBlockNF(boolean lit, Supplier<? extends Block> oppositeBlock, Properties pProperties) {
        super(pProperties, null);
        this.lit = lit;
        this.oppositeBlock = oppositeBlock;
        this.registerDefaultState(this.stateDefinition.any().setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(level.isRainingAt(pos) && random.nextBoolean()) level.setBlock(pos, oppositeBlock.get().defaultBlockState(), 3);
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
        BlockState state = addLiquidToPlacement(super.getStateForPlacement(context), context);
        if(lit && state.getValue(WATER_LEVEL) > 0) {
            state = copyLiquid(state, oppositeBlock.get().defaultBlockState());
            double d0 = (double)context.getClickedPos().getX() + 0.5D;
            double d1 = (double)context.getClickedPos().getY() + 0.7D;
            double d2 = (double)context.getClickedPos().getZ() + 0.5D;
            context.getLevel().playSound(context.getPlayer(), context.getClickedPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.2F + context.getLevel().random.nextFloat());
            if(context.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, d0, d1, d2, 1, 0, 0, 0, 0);
            }
        }
        return state;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        if(lit) {
            double d0 = (double)pos.getX() + 0.5D;
            double d1 = (double)pos.getY() + 0.7D;
            double d2 = (double)pos.getZ() + 0.5D;
            level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypesNF.FLAME_ORANGE.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
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
            level.setBlock(pos, copyLiquid(level.getBlockState(pos), oppositeBlock.get().defaultBlockState()), 3);
            serverLevel.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.2F + serverLevel.random.nextFloat());
            double d0 = (double)pos.getX() + 0.5D;
            double d1 = (double)pos.getY() + 0.7D;
            double d2 = (double)pos.getZ() + 0.5D;
            serverLevel.sendParticles(ParticleTypes.SMOKE, d0, d1, d2, 1, 0, 0, 0, 0);
        }
        return result;
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }
}
