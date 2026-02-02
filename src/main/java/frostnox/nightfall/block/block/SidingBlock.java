package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.math.OctalDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class SidingBlock extends BlockNF implements IWaterloggedBlock, ICustomPathfindable {
    public enum Type implements StringRepresentable {
        NORTH("north", Direction.NORTH),
        SOUTH("south", Direction.SOUTH),
        WEST("west", Direction.WEST),
        EAST("east", Direction.EAST),
        DOUBLE("double", Direction.UP);

        private final String name;
        private final Direction direction;

        Type(String name, Direction direction) {
            this.name = name;
            this.direction = direction;
        }

        public static Type fromDirection(Direction direction) {
            for(Type type : values()) if(type.direction == direction) return type;
            return DOUBLE;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }

        public Direction getDirection() {
            return direction;
        }
    }

    public enum Shape implements StringRepresentable {
        FULL("full", true, true),
        POSITIVE_QUARTET("pos_quartet", true, false),
        NEGATIVE_QUARTET("neg_quartet", false, false),
        POSITIVE_INNER("pos_inner", true, true),
        NEGATIVE_INNER("neg_inner", false, true);

        private final String name;
        public final boolean positive;
        public final boolean inner;

        Shape(String name, boolean positive, boolean inner) {
            this.name = name;
            this.positive = positive;
            this.inner = inner;
        }

        public Shape getOpposite() {
            if(this == FULL) return this;
            else return positive ? values()[ordinal() + 1] : values()[ordinal() - 1];
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

    public static final EnumProperty<Type> TYPE = BlockStatePropertiesNF.SIDING_TYPE;
    public static final EnumProperty<Shape> SHAPE = BlockStatePropertiesNF.SIDING_SHAPE;
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    protected static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_SHAPE = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape NORTH_WEST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 8.0D);
    protected static final VoxelShape NORTH_EAST_SHAPE = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    protected static final VoxelShape SOUTH_WEST_SHAPE = Block.box(0.0D, 0.0D, 8.0D, 8.0D, 16.0D, 16.0D);
    protected static final VoxelShape SOUTH_EAST_SHAPE = Block.box(8.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape NORTH_WEST_INNER_SHAPE = Shapes.or(Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D),
            Block.box(0.0D, 0.0D, 8.0D, 8.0D, 16.0D, 16.0D));
    protected static final VoxelShape NORTH_EAST_INNER_SHAPE = MathUtil.rotate(NORTH_WEST_INNER_SHAPE, Rotation.CLOCKWISE_90);
    protected static final VoxelShape SOUTH_WEST_INNER_SHAPE = MathUtil.rotate(NORTH_WEST_INNER_SHAPE, Rotation.COUNTERCLOCKWISE_90);
    protected static final VoxelShape SOUTH_EAST_INNER_SHAPE = MathUtil.rotate(NORTH_WEST_INNER_SHAPE, Rotation.CLOCKWISE_180);
    protected static final List<AABB> NORTH_FACE_Y = NORTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> SOUTH_FACE_Y = SOUTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> WEST_FACE_Y = WEST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> EAST_FACE_Y = EAST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> NORTH_WEST_FACE_Y = NORTH_WEST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> NORTH_EAST_FACE_Y = NORTH_EAST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> SOUTH_WEST_FACE_Y = SOUTH_WEST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> SOUTH_EAST_FACE_Y = SOUTH_EAST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> NORTH_WEST_INNER_FACE_Y = NORTH_WEST_INNER_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> NORTH_EAST_INNER_FACE_Y = NORTH_EAST_INNER_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> SOUTH_WEST_INNER_FACE_Y = SOUTH_WEST_INNER_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> SOUTH_EAST_INNER_FACE_Y = SOUTH_EAST_INNER_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final EnumMap<Direction, Direction[]> UPDATE_SHAPE_DIRECTIONS = new EnumMap<>(Direction.class);
    static {
        for(Direction dir : Direction.Plane.HORIZONTAL) UPDATE_SHAPE_DIRECTIONS.put(dir, new Direction[] {dir, dir.getClockWise(), dir.getCounterClockWise()});
    }
    public final BlockState baseState;
    public final Block base;

    public SidingBlock(Supplier<? extends Block> baseBlock, Properties properties) {
        super(properties);
        this.baseState = baseBlock.get().defaultBlockState();
        this.base = baseBlock.get();
        this.registerDefaultState(this.defaultBlockState().setValue(TYPE, Type.EAST).setValue(SHAPE, Shape.FULL).setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    protected Shape getSidingShape(LevelAccessor level, BlockPos pos, Direction curDir) {
        BlockState state1 = level.getBlockState(pos.relative(curDir));
        if(state1.getBlock() instanceof SidingBlock && state1.getValue(SHAPE).inner && state1.getValue(TYPE) != Type.DOUBLE) {
            Direction dir1 = state1.getValue(TYPE).direction;
            if(dir1.getAxis() != curDir.getAxis()) {
                BlockState state2 = level.getBlockState(pos.relative(dir1));
                if(state2.getBlock() instanceof SidingBlock && state2.getValue(SHAPE).inner && state2.getValue(TYPE) != Type.DOUBLE) {
                    Direction dir2 = state2.getValue(TYPE).direction;
                    if(curDir == dir2) {
                        if(curDir == Direction.SOUTH || curDir == Direction.WEST) {
                            return dir1.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? Shape.POSITIVE_QUARTET : Shape.NEGATIVE_QUARTET;
                        }
                        else return dir1.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? Shape.NEGATIVE_QUARTET : Shape.POSITIVE_QUARTET;
                    }
                }
            }
        }
        //Inner shape needs only one connection point
        curDir = curDir.getOpposite();
        state1 = level.getBlockState(pos.relative(curDir));
        if(state1.getBlock() instanceof SidingBlock && state1.getValue(SHAPE).inner && state1.getValue(TYPE) != Type.DOUBLE) {
            Direction dir1 = state1.getValue(TYPE).direction.getOpposite();
            if(dir1.getAxis() != curDir.getAxis()) {
                if(curDir == Direction.SOUTH || curDir == Direction.WEST) {
                    return dir1.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? Shape.POSITIVE_INNER : Shape.NEGATIVE_INNER;
                }
                else return dir1.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? Shape.NEGATIVE_INNER : Shape.POSITIVE_INNER;
            }
        }
        return Shape.FULL;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return state.getValue(TYPE) != Type.DOUBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(TYPE, SHAPE, WATER_LEVEL, WATERLOG_TYPE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Type type = state.getValue(TYPE);
        if(type == Type.DOUBLE) return Shapes.block();
        Shape shape = state.getValue(SHAPE);
        if(shape == Shape.FULL) {
            return switch(type) {
                case NORTH -> NORTH_SHAPE;
                case SOUTH -> SOUTH_SHAPE;
                case WEST -> WEST_SHAPE;
                default -> EAST_SHAPE;
            };
        }
        else {
            return switch(type) {
                case NORTH -> shape.positive ? (shape.inner ? NORTH_EAST_INNER_SHAPE : NORTH_EAST_SHAPE) : (shape.inner ? NORTH_WEST_INNER_SHAPE : NORTH_WEST_SHAPE);
                case SOUTH -> shape.positive ? (shape.inner ? SOUTH_WEST_INNER_SHAPE : SOUTH_WEST_SHAPE) : (shape.inner ? SOUTH_EAST_INNER_SHAPE : SOUTH_EAST_SHAPE);
                case WEST -> shape.positive ? (shape.inner ? NORTH_WEST_INNER_SHAPE : NORTH_WEST_SHAPE) : (shape.inner ? SOUTH_WEST_INNER_SHAPE : SOUTH_WEST_SHAPE);
                default -> shape.positive ? (shape.inner ? SOUTH_EAST_INNER_SHAPE : SOUTH_EAST_SHAPE) : (shape.inner ? NORTH_EAST_INNER_SHAPE : NORTH_EAST_SHAPE);
            };
        }
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos clickPos = context.getClickedPos();
        BlockState clickState = context.getLevel().getBlockState(clickPos);
        if(clickState.is(this)) {
            return clickState.setValue(TYPE, Type.DOUBLE).setValue(WATER_LEVEL, 0);
        }
        else {
            Direction dir = context.getClickedFace();
            boolean vertical = dir.getAxis().isVertical();
            if(vertical) dir = context.getHorizontalDirection();
            Vec3 clickLoc = context.getClickLocation();
            double off = dir.getAxis() == Direction.Axis.Z ? clickLoc.x % 1D : clickLoc.z % 1D;
            if(off < 0D) off += 1D; //Flip negative coords
            if(vertical) {
                double off2 = dir.getAxis() != Direction.Axis.Z ? clickLoc.x % 1D : clickLoc.z % 1D;
                Direction.AxisDirection axisDir = dir.getAxisDirection();
                if(off2 < 0) axisDir = axisDir.opposite();
                off2 = Math.abs(off2);
                if(axisDir == Direction.AxisDirection.NEGATIVE) {
                    if(off2 > 0.5D) dir = dir.getOpposite();
                }
                else if(off2 < 0.5D) dir = dir.getOpposite();
            }
            else if(dir == Direction.SOUTH || dir == Direction.WEST) dir = off > 0.5D ? dir.getCounterClockWise() : dir.getClockWise();
            else dir = off > 0.5D ? dir.getClockWise() : dir.getCounterClockWise();
            Shape shape = getSidingShape(context.getLevel(), clickPos, dir);
            return addLiquidToPlacement(defaultBlockState().setValue(TYPE, Type.fromDirection(dir)).setValue(SHAPE, shape), context);
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext pUseContext) {
        ItemStack itemstack = pUseContext.getItemInHand();
        Type type = state.getValue(TYPE);
        if(type != Type.DOUBLE && state.getValue(SHAPE) == Shape.FULL && itemstack.is(this.asItem())) {
            if(pUseContext.replacingClickedOnBlock()) {
                Direction direction = pUseContext.getClickedFace();
                if(type == Type.NORTH) return direction == Direction.SOUTH;
                else if(type == Type.SOUTH) return direction == Direction.NORTH;
                else if(type == Type.WEST) return direction == Direction.EAST;
                else if(type == Type.EAST) return direction == Direction.WEST;
                else return false;
            }
            else return true;
        }
        else return false;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getLiquid(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        Direction curDir = state.getValue(TYPE).direction;
        if(curDir.getAxis().isHorizontal()) {
            for(Direction dir : UPDATE_SHAPE_DIRECTIONS.get(curDir)) {
                if(dir.getAxis() == facing.getAxis()) {
                    Shape shape = getSidingShape(level, currentPos, curDir);
                    if(state.getValue(SHAPE) != shape) {
                        state = state.setValue(SHAPE, getSidingShape(level, currentPos, curDir));
                        break;
                    }
                }
            }
        }
        tickLiquid(state, currentPos, level);
        return state;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return switch(pType) {
            case LAND, AIR -> state.getValue(TYPE) != Type.DOUBLE;
            case WATER -> level.getFluidState(pos).is(FluidTags.WATER);
        };
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        Type type = state.getValue(TYPE);
        if(type == Type.DOUBLE) return 7;
        else return 0;
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        Type type = state.getValue(TYPE);
        if(type != Type.DOUBLE) {
            OctalDirection[] gapDirections;
            Shape shape = state.getValue(SHAPE);
            if(shape == Shape.FULL) gapDirections = switch(type) {
                case NORTH -> OctalDirection.SOUTH_SINGLE;
                case SOUTH -> OctalDirection.NORTH_SINGLE;
                case WEST -> OctalDirection.EAST_SINGLE;
                default -> OctalDirection.WEST_SINGLE;
            };
            else if(shape.inner) gapDirections = switch(type) {
                case NORTH -> shape.positive ? OctalDirection.SOUTHWEST_SINGLE : OctalDirection.SOUTHEAST_SINGLE;
                case SOUTH -> shape.positive ? OctalDirection.NORTHEAST_SINGLE : OctalDirection.NORTHWEST_SINGLE;
                case WEST -> shape.positive ? OctalDirection.SOUTHEAST_SINGLE : OctalDirection.NORTHEAST_SINGLE;
                default -> shape.positive ? OctalDirection.NORTHWEST_SINGLE : OctalDirection.SOUTHWEST_SINGLE;
            };
            else gapDirections = switch(type) {
                case NORTH -> shape.positive ? OctalDirection.SOUTH_AND_WEST : OctalDirection.SOUTH_AND_EAST;
                case SOUTH -> shape.positive ? OctalDirection.NORTH_AND_EAST : OctalDirection.NORTH_AND_WEST;
                case WEST -> shape.positive ? OctalDirection.SOUTH_AND_EAST : OctalDirection.NORTH_AND_EAST;
                default -> shape.positive ? OctalDirection.NORTH_AND_WEST : OctalDirection.SOUTH_AND_WEST;
            };
            float y = pos.getY();
            for(OctalDirection gapDirection : gapDirections) {
                float x = pos.getX() + 0.5F + gapDirection.xStepHalf;
                float z = pos.getZ() + 0.5F + gapDirection.zStepHalf;
                if(!nodeManager.collidesWith(nodeManager.getEntityBox(x, y, z, 1D))) {
                    nodeManager.getNode(pos).setPartialPath(x, y, z);
                    return hasAnyFloorAt(state, pos, level) ? NodeType.WALKABLE : NodeType.OPEN;
                }
            }
            nodeManager.getNode(pos).partial = true;
        }
        return NodeType.CLOSED;
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return NodeType.CLOSED;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        Type type = state.getValue(TYPE);
        if(type == Type.DOUBLE) return FULL_BOXES;
        Shape shape = state.getValue(SHAPE);
        if(shape == Shape.FULL) {
            return switch(type) {
                case NORTH -> NORTH_FACE_Y;
                case SOUTH -> SOUTH_FACE_Y;
                case WEST -> WEST_FACE_Y;
                default -> EAST_FACE_Y;
            };
        }
        else {
            return switch(type) {
                case NORTH -> shape.positive ? (shape.inner ? NORTH_EAST_INNER_FACE_Y : NORTH_EAST_FACE_Y) : (shape.inner ? NORTH_WEST_INNER_FACE_Y : NORTH_WEST_FACE_Y);
                case SOUTH -> shape.positive ? (shape.inner ? SOUTH_WEST_INNER_FACE_Y : SOUTH_WEST_FACE_Y) : (shape.inner ? SOUTH_EAST_INNER_FACE_Y : SOUTH_EAST_FACE_Y);
                case WEST -> shape.positive ? (shape.inner ? NORTH_WEST_INNER_FACE_Y : NORTH_WEST_FACE_Y) : (shape.inner ? SOUTH_WEST_INNER_FACE_Y : SOUTH_WEST_FACE_Y);
                default -> shape.positive ? (shape.inner ? SOUTH_EAST_INNER_FACE_Y : SOUTH_EAST_FACE_Y) : (shape.inner ? NORTH_EAST_INNER_FACE_Y : NORTH_EAST_FACE_Y);
            };
        }
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return getTopFaceShape(state);
    }

    @Override
    public OctalDirection getDirection(BlockState state) {
        Type type = state.getValue(TYPE);
        Shape shape = state.getValue(SHAPE);
        if(shape == Shape.FULL) return switch(type) {
            case NORTH -> OctalDirection.NORTH;
            case SOUTH -> OctalDirection.SOUTH;
            case WEST -> OctalDirection.WEST;
            default -> OctalDirection.EAST;
        };
        else return switch(type) {
            case SOUTH -> shape.positive ? OctalDirection.SOUTHWEST : OctalDirection.SOUTHEAST;
            case NORTH -> shape.positive ? OctalDirection.NORTHEAST : OctalDirection.NORTHWEST;
            case EAST -> shape.positive ? OctalDirection.SOUTHEAST : OctalDirection.NORTHEAST;
            default -> shape.positive ? OctalDirection.NORTHWEST : OctalDirection.SOUTHWEST;
        };
    }

    @Override
    public BlockState rotate(BlockState state, Rotation pRot) {
        return state.setValue(TYPE, Type.fromDirection(pRot.rotate(state.getValue(TYPE).direction)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror pMirror) {
        Direction direction = state.getValue(TYPE).direction;
        Shape shape = state.getValue(SHAPE);
        switch(pMirror) {
            case LEFT_RIGHT:
                if(direction.getAxis() == Direction.Axis.Z) return state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, shape.getOpposite());
                else return state.setValue(SHAPE, shape.getOpposite());
            case FRONT_BACK:
                if(direction.getAxis() == Direction.Axis.X) return state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, shape.getOpposite());
                else return state.setValue(SHAPE, shape.getOpposite());
        }
        return super.mirror(state, pMirror);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        baseState.getBlock().fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random pRand) {
        base.animateTick(state, level, pos, pRand);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        baseState.attack(level, pos, player);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        base.destroy(level, pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        if (!state.is(state.getBlock())) {
            baseState.neighborChanged(level, pos, Blocks.AIR, pos, false);
            base.onPlace(baseState, level, pos, pOldState, false);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if (!state.is(pNewState.getBlock())) {
            baseState.onRemove(level, pos, pNewState, pIsMoving);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity pEntity) {
        base.stepOn(level, pos, state, pEntity);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return base.isRandomlyTicking(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        base.randomTick(state, level, pos, random);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random pRand) {
        base.tick(state, level, pos, pRand);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHit) {
        return baseState.use(level, player, hand, pHit);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion pExplosion) {
        base.wasExploded(level, pos, pExplosion);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState pAdjacentBlockState, Direction pSide) {
        return base.skipRendering(state, pAdjacentBlockState, pSide);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return base.getVisualShape(state, level, pos, context);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return base.getShadeBrightness(state, level, pos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter pReader, BlockPos pos) {
        return base.propagatesSkylightDown(state, pReader, pos);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult pHit, Projectile pProjectile) {
        base.onProjectileHit(level, state, pHit, pProjectile);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(WATER_LEVEL) != 0 ? 0 : baseState.getFireSpreadSpeed(level, pos, direction);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(WATER_LEVEL) != 0 ? 0 : baseState.getFlammability(level, pos, direction);
    }
}
