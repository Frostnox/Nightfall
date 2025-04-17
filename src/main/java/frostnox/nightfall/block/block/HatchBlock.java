package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.util.math.OctalDirection;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class HatchBlock extends BlockNF implements IWaterloggedBlock, ICustomPathfindable {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    protected static final float HALF_THICKNESS = 1.5F / 16F;
    protected static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    protected static final VoxelShape WEST_SHAPE = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
    protected static final List<AABB> NORTH_FACE_Y = NORTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> SOUTH_FACE_Y = SOUTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> WEST_FACE_Y = WEST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> EAST_FACE_Y = EAST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    public final Supplier<SoundEvent> openSound, closeSound;

    public HatchBlock(Properties properties, Supplier<SoundEvent> openSound, Supplier<SoundEvent> closeSound) {
        super(properties);
        this.openSound = openSound;
        this.closeSound = closeSound;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(OPEN, false).setValue(HINGE, DoorHingeSide.LEFT).setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    public Direction getHingeDirection(BlockState state) {
        Direction direction = state.getValue(FACING);
        direction = state.getValue(HINGE) == DoorHingeSide.RIGHT ? direction.getClockWise() : direction.getCounterClockWise();
        return direction;
    }

    public Direction getFacingDirection(BlockState state) {
        Direction direction = state.getValue(FACING);
        if(!state.getValue(OPEN)) return direction;
        else return state.getValue(HINGE) == DoorHingeSide.RIGHT ? direction.getCounterClockWise() : direction.getClockWise();
    }

    protected void playSound(@Nullable Player player, Level level, BlockPos pos, boolean open) {
        level.playSound(player, pos, open ? openSound.get() : closeSound.get(), SoundSource.BLOCKS, 1F, 0.9F + 0.1F * level.random.nextFloat());
        level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, OPEN, HINGE, WATER_LEVEL, WATERLOG_TYPE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch(getFacingDirection(state)) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case NORTH -> NORTH_SHAPE;
            default -> EAST_SHAPE;
        };
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if(getHingeDirection(state) == facing && !state.canSurvive(level, currentPos)) return Blocks.AIR.defaultBlockState();
        tickLiquid(state, currentPos, level);
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction dir = context.getClickedFace();
        boolean vertical = dir.getAxis().isVertical();
        if(vertical) dir = context.getHorizontalDirection();
        Vec3 clickLoc = context.getClickLocation();
        double off = dir.getAxis() == Direction.Axis.Z ? clickLoc.x % 1D : clickLoc.z % 1D;
        if(off < 0D) off += 1D; //Flip negative coords
        DoorHingeSide hinge;
        if(vertical) {
            double off2 = dir.getAxis() != Direction.Axis.Z ? clickLoc.x % 1D : clickLoc.z % 1D;
            Direction.AxisDirection axisDir = dir.getAxisDirection();
            if(off2 < 0) axisDir = axisDir.opposite();
            off2 = Math.abs(off2);
            if(axisDir == Direction.AxisDirection.POSITIVE) {
                if(off2 > 0.5D) dir = dir.getOpposite();
            }
            else if(off2 < 0.5D) dir = dir.getOpposite();

            hinge = (off2 > 0.5D ? off <= 0.5D : off > 0.5D) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            if(!canSurvive(defaultBlockState().setValue(FACING, dir).setValue(HINGE, hinge), context.getLevel(), context.getClickedPos())) {
                hinge = hinge == DoorHingeSide.RIGHT ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            }
        }
        else if(dir == Direction.NORTH || dir == Direction.EAST) {
            hinge = off > 0.5D ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            dir = off > 0.5D ? dir.getCounterClockWise() : dir.getClockWise();
        }
        else {
            hinge = off > 0.5D ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            dir = off > 0.5D ? dir.getClockWise() : dir.getCounterClockWise();
        }
        return addLiquidToPlacement(defaultBlockState().setValue(FACING, dir).setValue(HINGE, hinge).setValue(OPEN, false), context);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getLiquid(state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        state = state.cycle(OPEN);
        level.setBlock(pos, state, 3);
        tickLiquid(state, pos, level);
        playSound(pPlayer, level, pos, state.getValue(OPEN));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = getHingeDirection(state);
        return LevelUtil.isBlockFullySupportedHorizontally(state.setValue(OPEN, false).getShape(level, pos).bounds(),
                level, pos.relative(direction), direction.getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation pRotation) {
        return state.setValue(FACING, pRotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror pMirror) {
        return pMirror == Mirror.NONE ? state : state.rotate(pMirror.getRotation(state.getValue(FACING))).cycle(HINGE);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return switch (pType) {
            case LAND, AIR -> true;
            case WATER -> level.getFluidState(pos).is(FluidTags.WATER);
        };
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        OctalDirection gapDirection = OctalDirection.fromDirection(getFacingDirection(state));
        return getTypeForThinSideClosedShape(nodeManager, state, level, pos, gapDirection, HALF_THICKNESS);
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return NodeType.CLOSED;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return switch(getFacingDirection(state)) {
            case SOUTH -> SOUTH_FACE_Y;
            case WEST -> WEST_FACE_Y;
            case NORTH -> NORTH_FACE_Y;
            default -> EAST_FACE_Y;
        };
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return getTopFaceShape(state);
    }

    @Override
    public OctalDirection getDirection(BlockState state) {
        return OctalDirection.fromDirection(getFacingDirection(state)).getOpposite();
    }
}
