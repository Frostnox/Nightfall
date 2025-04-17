package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.IWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;

public abstract class WaterloggedBlock extends BlockNF implements IWaterloggedBlock {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;

    public WaterloggedBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(stateDefinition.any().setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WATER_LEVEL, WATERLOG_TYPE);
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
        return addLiquidToPlacement(super.getStateForPlacement(context), context);
    }
}
