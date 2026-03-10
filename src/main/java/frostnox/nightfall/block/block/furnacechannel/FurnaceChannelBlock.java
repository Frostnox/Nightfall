package frostnox.nightfall.block.block.furnacechannel;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.math.OctalDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FurnaceChannelBlock extends WaterloggedEntityBlock implements ICustomPathfindable {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static BooleanProperty SEALED = BlockStatePropertiesNF.SEALED;
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(Block.box(5.0D, 5.0D, 9.0D, 11.0D, 7.0D, 16.0D),
            Block.box(9.0D, 7.0D, 9.0D, 11.0D, 9.0D, 16.0D),
            Block.box(5.0D, 7.0D, 9.0D, 7.0D, 9.0D, 16.0D));
    private static final VoxelShape NORTH_SHAPE = MathUtil.rotate(SOUTH_SHAPE, Rotation.CLOCKWISE_180);
    private static final VoxelShape EAST_SHAPE = MathUtil.rotate(SOUTH_SHAPE, Rotation.COUNTERCLOCKWISE_90);
    private static final VoxelShape WEST_SHAPE = MathUtil.rotate(SOUTH_SHAPE, Rotation.CLOCKWISE_90);
    private static final List<AABB> NORTH_FACE_Y = NORTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    private static final List<AABB> SOUTH_FACE_Y = SOUTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    private static final List<AABB> WEST_FACE_Y = WEST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    private static final List<AABB> EAST_FACE_Y = EAST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    private static final VoxelShape SOUTH_SHAPE_SEALED = Shapes.or(SOUTH_SHAPE, Block.box(7, 7, 14, 9, 9, 16),
            Block.box(5, 9, 14, 11, 11, 16));
    private static final VoxelShape NORTH_SHAPE_SEALED = MathUtil.rotate(SOUTH_SHAPE_SEALED, Rotation.CLOCKWISE_180);
    private static final VoxelShape EAST_SHAPE_SEALED = MathUtil.rotate(SOUTH_SHAPE_SEALED, Rotation.COUNTERCLOCKWISE_90);
    private static final VoxelShape WEST_SHAPE_SEALED = MathUtil.rotate(SOUTH_SHAPE_SEALED, Rotation.CLOCKWISE_90);
    public final TieredHeat maxHeat;

    public FurnaceChannelBlock(TieredHeat maxHeat, Properties pProperties) {
        super(pProperties);
        this.maxHeat = maxHeat;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(SEALED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        if(state.getValue(SEALED)) return switch(state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE_SEALED;
            case EAST -> EAST_SHAPE_SEALED;
            case SOUTH -> SOUTH_SHAPE_SEALED;
            default -> WEST_SHAPE_SEALED;
        };
        else return switch(state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            default -> WEST_SHAPE;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHit) {
        level.playSound(player, pos, SoundsNF.CERAMIC_OPEN_SMALL.get(), SoundSource.BLOCKS, 1F, 1F);
        if(level.isClientSide) return InteractionResult.SUCCESS;
        else {
            if(!state.getValue(SEALED)) ((FurnaceChannelBlockEntity) level.getBlockEntity(pos)).stopCasting();
            level.setBlock(pos, state.setValue(SEALED, !state.getValue(SEALED)), 2);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing);
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing, SupportType.FULL);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, level, pos, facingPos);
        if(facing == state.getValue(FACING) && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        else return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext placeContext) {
        return addLiquidToPlacement(defaultBlockState().setValue(FACING, placeContext.getHorizontalDirection()), placeContext);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror pMirror) {
        return state.rotate(pMirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, SEALED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return BlockEntitiesNF.FURNACE_CHANNEL.get().create(pPos, pState);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entity) {
        return !level.isClientSide && !state.getValue(SEALED) ? createTickerHelper(entity, BlockEntitiesNF.FURNACE_CHANNEL.get(), FurnaceChannelBlockEntity::serverTick) : null;
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
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
        OctalDirection gapDirection = OctalDirection.fromDirection(state.getValue(FACING).getOpposite());
        return getTypeForSideClosedShape(nodeManager, state, level, pos, gapDirection);
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return NodeType.CLOSED;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return switch(state.getValue(FACING)) {
            case NORTH -> NORTH_FACE_Y;
            case SOUTH -> SOUTH_FACE_Y;
            case WEST -> WEST_FACE_Y;
            default -> EAST_FACE_Y;
        };
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return getTopFaceShape(state);
    }

    @Override
    public OctalDirection getDirection(BlockState state) {
        return switch(state.getValue(FACING)) {
            case NORTH -> OctalDirection.NORTH;
            case SOUTH -> OctalDirection.SOUTH;
            case WEST -> OctalDirection.WEST;
            default -> OctalDirection.EAST;
        };
    }
}
