package frostnox.nightfall.block.block.nest;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class OverlayBurrowBlock extends NestBlock {
    public static final DirectionProperty FACING = BlockStatePropertiesNF.FACING_NOT_UP;

    public OverlayBurrowBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.DOWN));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if(facing == state.getValue(FACING) && !facingState.isFaceSturdy(level, facingPos, facing.getOpposite(), SupportType.FULL)) {
            return Blocks.AIR.defaultBlockState();
        }
        else return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext).setValue(FACING,
                pContext.getClickedFace().getAxis().isVertical() ? Direction.DOWN : pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public Object getRenderPropertiesInternal() {
        return BlocksNF.NO_BREAK_PARTICLES;
    }
}
