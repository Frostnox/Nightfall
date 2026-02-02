package frostnox.nightfall.block.block.shelf;

import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.math.OctalDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShelfBlock extends WaterloggedEntityBlock implements ICustomPathfindable {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final VoxelShape SOUTH_SHAPE = Shapes.or(Block.box(14, 0, 0, 16, 16, 8),
            Block.box(0, 0, 0, 2, 16, 8),
            Block.box(2, 0, 0, 14, 2, 8),
            Block.box(2, 7, 0, 14, 9, 8),
            Block.box(2, 14, 0, 14, 16, 8));
    protected static final VoxelShape NORTH_SHAPE = MathUtil.rotate(SOUTH_SHAPE, Rotation.CLOCKWISE_180);
    protected static final VoxelShape EAST_SHAPE = MathUtil.rotate(SOUTH_SHAPE, Rotation.COUNTERCLOCKWISE_90);
    protected static final VoxelShape WEST_SHAPE = MathUtil.rotate(SOUTH_SHAPE, Rotation.CLOCKWISE_90);
    protected static final List<AABB> NORTH_FACE_Y = NORTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> SOUTH_FACE_Y = SOUTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> WEST_FACE_Y = WEST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> EAST_FACE_Y = EAST_SHAPE.getFaceShape(Direction.UP).toAabbs();

    public ShelfBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.SOUTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.SHELF.get().create(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHit) {
        if(level.getBlockEntity(pos) instanceof ShelfBlockEntity shelf) {
            Vec3 loc = pHit.getLocation();
            double y = loc.y % 1D;
            Direction facing = state.getValue(FACING);
            double x = facing.getAxis() == Direction.Axis.Z ? loc.x % 1D : loc.z % 1D;
            if(y < 0) y++;
            if(x < 0) x++;
            if(x > 0 && x < 1 && y > 0 && y < 1) {
                if(level.isClientSide) return InteractionResult.SUCCESS;
                int index = ((facing == Direction.SOUTH || facing == Direction.WEST) ? x < 0.5 : x > 0.5) ? 0 : 1;
                if(y < 0.5) index += 2;
                ItemStack item = shelf.items.get(index);
                if(!item.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(item.copy());
                    shelf.items.set(index, ItemStack.EMPTY);
                    shelf.setChanged();
                    level.sendBlockUpdated(pos, state, state, 2);
                    return InteractionResult.CONSUME;
                }
                else {
                    ItemStack heldItem = player.getItemInHand(hand);
                    if(heldItem.getMaxStackSize() > 4) {
                        shelf.items.set(index, heldItem.copy());
                        if(!player.getAbilities().instabuild) player.setItemInHand(hand, ItemStack.EMPTY);
                        shelf.setChanged();
                        level.sendBlockUpdated(pos, state, state, 2);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return switch(state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            default -> WEST_SHAPE;
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!state.is(pNewState.getBlock()) && level.getBlockEntity(pos) instanceof ShelfBlockEntity shelf) {
            Containers.dropContents(level, pos, shelf.items);
        }
        super.onRemove(state, level, pos, pNewState, pIsMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if(state != null) {
            if(context.getClickedFace().getAxis() == Direction.Axis.Y) {
                Direction dir = context.getHorizontalDirection();
                Vec3 loc = context.getClickLocation();
                double x = dir.getAxis() == Direction.Axis.X ? loc.x % 1D : loc.z % 1D;
                if(x < 0) x++;
                if(dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? x < 0.5 : x > 0.5) dir = dir.getOpposite();
                return state.setValue(FACING, dir);
            }
            else return state.setValue(FACING, context.getClickedFace());
        }
        else return state;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation pRotation) {
        return state.setValue(FACING, pRotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror pMirror) {
        return state.rotate(pMirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 1;
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
        OctalDirection gapDirection = OctalDirection.fromDirection(state.getValue(FACING));
        return getTypeForSideClosedShape(nodeManager, state, level, pos, gapDirection);
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return NodeType.CLOSED;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return getBottomFaceShape(state);
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return switch(state.getValue(FACING)) {
            case NORTH -> NORTH_FACE_Y;
            case SOUTH -> SOUTH_FACE_Y;
            case WEST -> WEST_FACE_Y;
            default -> EAST_FACE_Y;
        };
    }

    @Override
    public OctalDirection getDirection(BlockState state) {
        return switch(state.getValue(FACING)) {
            case NORTH -> OctalDirection.SOUTH;
            case SOUTH -> OctalDirection.NORTH;
            case WEST -> OctalDirection.EAST;
            default -> OctalDirection.WEST;
        };
    }
}
