package frostnox.nightfall.block;

import frostnox.nightfall.block.fluid.WaterFluidNF;
import frostnox.nightfall.registry.forge.FluidsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

import static frostnox.nightfall.block.BlockStatePropertiesNF.WATERLOG_TYPE;
import static frostnox.nightfall.block.BlockStatePropertiesNF.WATER_LEVEL;

/**
 * Waterlogging with support for various levels of water.
 * 0 = empty. 6 = falling. 7 = source.
 * Functions otherwise identically to SimpleWaterloggedBlock.
 */
public interface IWaterloggedBlock extends BucketPickup, LiquidBlockContainer {
    enum WaterlogType implements StringRepresentable {
        FRESH(FluidsNF.WATER),
        SEA(FluidsNF.SEAWATER);

        public final Supplier<WaterFluidNF> fluid;

        WaterlogType(Supplier<WaterFluidNF> fluid) {
            this.fluid = fluid;
        }

        public static WaterlogType fromFluid(Fluid fluid) {
            if(fluid == FluidsNF.WATER.get() || fluid == FluidsNF.WATER_FLOWING.get()) return FRESH;
            else return SEA;
        }

        @Override
        public String getSerializedName() {
            return toString().toLowerCase();
        }
    }

    /**
     * @return water level that will not cause waterlogging at or below
     */
    int getExcludedWaterLevel(BlockState state);

    /**
     * @return blockstate for placement with adjusted water levels
     */
    default @Nullable BlockState addLiquidToPlacement(BlockState state, BlockPlaceContext context) {
        return addLiquidToPlacement(state, context.getClickedPos(), context.getLevel());
    }

    /**
     * @return blockstate for placement with adjusted water levels
     */
    default @Nullable BlockState addLiquidToPlacement(BlockState state, BlockPos pos, LevelAccessor level) {
        if(state == null) return null;
        FluidState fluid = level.getFluidState(pos);
        Fluid type = fluid.getType();
        if(!type.isSame(FluidsNF.WATER.get())) return state;
        if(fluid.getType().isSource(fluid)) return state.setValue(WATER_LEVEL, 7).setValue(WATERLOG_TYPE, WaterlogType.fromFluid(type));
        else if(fluid.getAmount() > getExcludedWaterLevel(state)) {
            if(fluid.getAmount() > 0) level.scheduleTick(pos, type, type.getTickDelay(level));
            return state.setValue(WATER_LEVEL, fluid.getAmount()).setValue(WATERLOG_TYPE, WaterlogType.fromFluid(type));
        }
        else return state;
    }

    /**
     * @return blockstate for placement with adjusted water levels
     */
    default BlockState addLiquidToPlacementNoUpdate(BlockState state, FluidState fluid) {
        Fluid type = fluid.getType();
        if(!type.isSame(FluidsNF.WATER.get())) return state;
        if(fluid.getType().isSource(fluid)) return state.setValue(WATER_LEVEL, 7).setValue(WATERLOG_TYPE, WaterlogType.fromFluid(type));
        else if(fluid.getAmount() > getExcludedWaterLevel(state)) {
            return state.setValue(WATER_LEVEL, fluid.getAmount()).setValue(WATERLOG_TYPE, WaterlogType.fromFluid(type));
        }
        else return state;
    }

    default FluidState getLiquid(BlockState state) {
        int water = state.getValue(WATER_LEVEL);
        if(water != 0) {
            WaterFluidNF type = state.getValue(WATERLOG_TYPE).fluid.get();
            return water == 7 ? type.getSource(false) : type.getFlowing(water, water == 6);
        }
        return Fluids.EMPTY.defaultFluidState();
    }

    default void tickLiquid(BlockState state, BlockPos pos, LevelAccessor level) {
        int waterLevel = state.getValue(WATER_LEVEL);
        if(waterLevel != 0) {
            int excludedLevel = getExcludedWaterLevel(state);
            if(waterLevel <= excludedLevel) level.setBlock(pos, state.setValue(WATER_LEVEL, 0), 3);
            FluidState fluidState = state.getFluidState();
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }

    default BlockState copyLiquid(BlockState oldState, BlockState newState) {
        return newState.setValue(WATERLOG_TYPE, oldState.getValue(WATERLOG_TYPE)).setValue(WATER_LEVEL, oldState.getValue(WATER_LEVEL));
    }

    @Override
    default boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return (state.getValue(WATER_LEVEL) != 7 || state.getValue(WATERLOG_TYPE) != WaterlogType.fromFluid(fluid))
                && fluid.isSame(FluidsNF.WATER.get()) && fluid.getAmount(fluid.defaultFluidState()) > getExcludedWaterLevel(state);
    }

    @Override
    default boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        if(fluid.getType().isSame(FluidsNF.WATER.get()) && (fluid.getAmount() > Math.max(state.getValue(WATER_LEVEL), getExcludedWaterLevel(state)) || fluid.isSource())) {
            level.setBlock(pos, state.setValue(WATER_LEVEL, fluid.isSource() ? 7 : fluid.getAmount()).setValue(WATERLOG_TYPE, WaterlogType.fromFluid(fluid.getType())), 3);
            if(!level.isClientSide()) level.scheduleTick(pos, fluid.getType(), fluid.getType().getTickDelay(level));
            return true;
        }
        else return false;
    }

    @Override
    default ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        if(state.getValue(WATER_LEVEL) == 7) {
            level.setBlock(pos, state.setValue(WATER_LEVEL, 0), 3);
            if(!state.canSurvive(level, pos)) {
                level.destroyBlock(pos, true);
            }
            return new ItemStack(state.getValue(WATERLOG_TYPE).fluid.get().getBucket());
        }
        else return ItemStack.EMPTY;
    }

    @Override
    default Optional<SoundEvent> getPickupSound(BlockState state) {
        return state.getValue(WATERLOG_TYPE).fluid.get().getPickupSound();
    }

    @Override
    @Deprecated
    default Optional<SoundEvent> getPickupSound() {
        return FluidsNF.WATER.get().getPickupSound();
    }
}