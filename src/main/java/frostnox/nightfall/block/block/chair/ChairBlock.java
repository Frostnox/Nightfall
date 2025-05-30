package frostnox.nightfall.block.block.chair;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.block.WaterloggedBlock;
import frostnox.nightfall.entity.entity.SeatEntity;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChairBlock extends WaterloggedBlock {
    public enum Type implements StringRepresentable {
        SINGLE, LEFT, RIGHT, MIDDLE;

        private final String name;

        Type() {
            this.name = name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Type> TYPE = BlockStatePropertiesNF.CHAIR_TYPE;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    protected static final Map<Type, Map<Direction, VoxelShape>> BOTTOM_SHAPES, TOP_SHAPES;
    static {
        BOTTOM_SHAPES = new EnumMap<>(Type.class);
        for(Type type : Type.values()) {
            VoxelShape shape = switch(type) {
                case SINGLE -> Shapes.or(Block.box(2, 0, 2, 4, 8, 4),
                        Block.box(12, 0, 2, 14, 8, 4),
                        Block.box(2, 0, 12, 4, 8, 14),
                        Block.box(12, 0, 12, 14, 8, 14),
                        Block.box(2, 8, 2, 14, 10, 14),
                        Block.box(2, 10, 12, 14, 16, 14));
                case LEFT -> Shapes.or(Block.box(2, 0, 2, 4, 8, 4),
                        Block.box(2, 0, 12, 4, 8, 14),
                        Block.box(2, 8, 2, 16, 10, 14),
                        Block.box(2, 10, 12, 16, 16, 14));
                case RIGHT -> Shapes.or(Block.box(12, 0, 2, 14, 8, 4),
                        Block.box(12, 0, 12, 14, 8, 14),
                        Block.box(0, 8, 2, 14, 10, 14),
                        Block.box(0, 10, 12, 14, 16, 14));
                case MIDDLE -> Shapes.or(Block.box(0, 8, 2, 16, 10, 14),
                        Block.box(0, 10, 12, 16, 16, 14));
            };
            EnumMap<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
            map.put(Direction.NORTH, shape);
            map.put(Direction.SOUTH, MathUtil.rotate(shape, Rotation.CLOCKWISE_180));
            map.put(Direction.WEST, MathUtil.rotate(shape, Rotation.COUNTERCLOCKWISE_90));
            map.put(Direction.EAST, MathUtil.rotate(shape, Rotation.CLOCKWISE_90));
            BOTTOM_SHAPES.put(type, map);
        }
        TOP_SHAPES = new EnumMap<>(Type.class);
        for(Type type : Type.values()) {
            VoxelShape shape = switch(type) {
                case SINGLE -> Block.box(2, 0, 12, 14, 6, 14);
                case LEFT -> Block.box(2, 0, 12, 16, 6, 14);
                case RIGHT -> Block.box(0, 0, 12, 14, 6, 14);
                case MIDDLE -> Block.box(0, 0, 12, 16, 6, 14);
            };
            EnumMap<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
            map.put(Direction.NORTH, shape);
            map.put(Direction.SOUTH, MathUtil.rotate(shape, Rotation.CLOCKWISE_180));
            map.put(Direction.WEST, MathUtil.rotate(shape, Rotation.COUNTERCLOCKWISE_90));
            map.put(Direction.EAST, MathUtil.rotate(shape, Rotation.CLOCKWISE_90));
            TOP_SHAPES.put(type, map);
        }
    }

    public ChairBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(TYPE, Type.SINGLE).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    protected Type getTypeAt(BlockState block, BlockGetter level, BlockPos pos) {
        Direction facing = block.getValue(FACING);
        boolean right = canConnect(block, level.getBlockState(pos.relative(facing.getClockWise())));
        boolean left = canConnect(block, level.getBlockState(pos.relative(facing.getCounterClockWise())));
        if(right && left) return Type.MIDDLE;
        else if(left) return Type.RIGHT;
        else if(right) return Type.LEFT;
        else return Type.SINGLE;
    };

    protected boolean canConnect(BlockState block, BlockState neighbor) {
        return neighbor.is(this) && neighbor.getValue(FACING) == block.getValue(FACING);
    }

    protected boolean isConnected(BlockState block, BlockState neighbor, boolean left) {
        if(canConnect(block, neighbor)) {
            Type type = neighbor.getValue(TYPE);
            return type == Type.MIDDLE || (left ? type == Type.RIGHT : type == Type.LEFT);
        }
        else return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? BOTTOM_SHAPES.get(state.getValue(TYPE)).get(state.getValue(FACING)) :
                TOP_SHAPES.get(state.getValue(TYPE)).get(state.getValue(FACING));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if(state.getValue(HALF) == DoubleBlockHalf.UPPER) return level.getBlockState(pos.below()).is(this);
        else return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP, SupportType.RIGID);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player pPlayer) {
        if(!level.isClientSide && pPlayer.isCreative()) LevelUtil.preventBlockLowerHalfDrop(level, pos, state, pPlayer);
        super.playerWillDestroy(level, pos, state, pPlayer);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if(pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            BlockState state = super.getStateForPlacement(context);
            if(state == null) return null;
            Direction dir = context.getHorizontalDirection().getOpposite();
            state = state.setValue(FACING, dir);
            if(!context.isSecondaryUseActive()) state = state.setValue(TYPE, getTypeAt(state, level, pos));
            return state.setValue(HALF, DoubleBlockHalf.LOWER);
        }
        else return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity pPlacer, ItemStack pStack) {
        state = state.setValue(WATER_LEVEL, 0).setValue(HALF, DoubleBlockHalf.UPPER);
        BlockPos abovePos = pos.above();
        level.setBlock(abovePos, addLiquidToPlacement(state, abovePos, level), 3);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, level, currentPos, facingPos);
        DoubleBlockHalf half = state.getValue(HALF);
        if(half == DoubleBlockHalf.LOWER) {
            Type originalType = state.getValue(TYPE);
            Direction chairDir = state.getValue(FACING);
            Direction leftDir = chairDir.getClockWise(), rightDir = chairDir.getCounterClockWise();
            if(leftDir == facing) {
                if(isConnected(state, facingState, true)) {
                    Type type = state.getValue(TYPE);
                    if(type == Type.SINGLE) state = state.setValue(TYPE, Type.LEFT);
                    else if(type != Type.LEFT) state = state.setValue(TYPE, Type.MIDDLE);
                }
            }
            else if(rightDir == facing) {
                if(isConnected(state, facingState, false)) {
                    Type type = state.getValue(TYPE);
                    if(type == Type.SINGLE) state = state.setValue(TYPE, Type.RIGHT);
                    else if(type != Type.RIGHT) state = state.setValue(TYPE, Type.MIDDLE);
                }
            }
            Type type = state.getValue(TYPE);
            if(type == Type.LEFT) {
                if(leftDir == facing) {
                    if(!isConnected(state, facingState, true)) state = state.setValue(TYPE, Type.SINGLE);
                }
            }
            else if(type == Type.RIGHT) {
                if(rightDir == facing) {
                    if(!isConnected(state, facingState, false)) state = state.setValue(TYPE, Type.SINGLE);
                }
            }
            else if(type == Type.MIDDLE) {
                if(leftDir == facing) {
                    if(!isConnected(state, facingState, true)) state = state.setValue(TYPE, Type.RIGHT);
                }
                else if(rightDir == facing) {
                    if(!isConnected(state, facingState, false)) state = state.setValue(TYPE, Type.LEFT);
                }
            }
            if(state.getValue(TYPE) != Type.MIDDLE && (facing == Direction.DOWN || state.getValue(TYPE) != originalType)) {
                if(!state.canSurvive(level, currentPos)) state = Blocks.AIR.defaultBlockState();
            }
        }
        if(facing.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (facing == Direction.UP)) {
            state = facingState.is(this) && facingState.getValue(HALF) != half ? state.setValue(FACING, facingState.getValue(FACING)).setValue(TYPE, facingState.getValue(TYPE)) : Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(state.getValue(HALF) == DoubleBlockHalf.UPPER) return InteractionResult.PASS;
        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(!pPlayer.isPassenger() && pos.distToCenterSqr(pPlayer.getX(), pPlayer.getY() + pPlayer.getBbHeight() / 2, pPlayer.getZ()) <= 2.5 * 2.5) {
            List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
            if(seats.isEmpty()) {
                SeatEntity seat = new SeatEntity(level, pos.getX() + 0.5, pos.getY() - pPlayer.getMyRidingOffset(), pos.getZ() + 0.5,
                        state.getValue(FACING).toYRot());
                level.addFreshEntity(seat);
                pPlayer.startRiding(seat);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this)) {
            List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
            for(SeatEntity seat : seats) seat.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(TYPE);
        builder.add(HALF);
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
        return 0;
    }
}
