package frostnox.nightfall.block.block.pile;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.IWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class WaterloggedPileBlock extends PileBlock implements IWaterloggedBlock {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<IWaterloggedBlock.WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;

    public WaterloggedPileBlock(Supplier<? extends Item> drop, VoxelShape shapeZ, Properties properties) {
        super(drop, shapeZ, properties);
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

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }
}
