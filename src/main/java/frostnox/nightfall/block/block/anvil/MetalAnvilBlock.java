package frostnox.nightfall.block.block.anvil;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class MetalAnvilBlock extends TieredAnvilBlock implements ICustomPathfindable, IWaterloggedBlock {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    private static final VoxelShape BASE = Block.box(2.0D, 0.0D, 3.0D, 14.0D, 4.0D, 13.0D);
    private static final VoxelShape LEG1 = Block.box(3.0D, 4.0D, 4.0D, 13.0D, 5.0D, 12.0D);
    private static final VoxelShape LEG2 = Block.box(4.0D, 5.0D, 6.0D, 12.0D, 10.0D, 10.0D);
    private static final VoxelShape TOP = Block.box(0.0D, 10.0D, 3.0D, 16.0D, 16.0D, 13.0D);
    protected static final VoxelShape X_AXIS_SHAPE = Shapes.or(BASE, LEG1, LEG2, TOP);
    protected static final VoxelShape Z_AXIS_SHAPE = MathUtil.rotate(X_AXIS_SHAPE, Rotation.CLOCKWISE_90);
    protected static final List<AABB> X_TOP_FACE = X_AXIS_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> Z_TOP_FACE = Z_AXIS_SHAPE.getFaceShape(Direction.UP).toAabbs();

    public MetalAnvilBlock(int tier, Properties properties) {
        super(tier, properties);
        registerDefaultState(defaultBlockState().setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
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
    public BlockState getStateForPlacement(BlockPlaceContext placeContext) {
        return addLiquidToPlacement(defaultBlockState().setValue(FACING, placeContext.getHorizontalDirection().getClockWise()), placeContext);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        Direction direction = state.getValue(FACING);
        return direction.getAxis() == Direction.Axis.X ? X_AXIS_SHAPE : Z_AXIS_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATER_LEVEL, WATERLOG_TYPE);
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState contactState, MovingBlockEntity entity) {
        if(!entity.isSilent()) {
            level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.4F, level.random.nextFloat() * 0.1F + 0.8F);
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return switch(pType) {
            case LAND, AIR -> true;
            case WATER -> level.getFluidState(pos).is(FluidTags.WATER);
        };
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        nodeManager.getNode(pos).partial = true;
        return NodeType.CLOSED;
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return NodeType.CLOSED;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? X_TOP_FACE : Z_TOP_FACE;
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return FULL_BOXES;
    }
}
