package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.math.OctalDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class LadderBlockNF extends HorizontalDirectionalBlock implements IWaterloggedBlock, ICustomPathfindable {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<IWaterloggedBlock.WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    protected static final float HALF_THICKNESS = 1.5F / 16F;
    protected static final VoxelShape EAST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_SHAPE = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    protected static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    protected static final List<AABB> NORTH_FACE_Y = NORTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> SOUTH_FACE_Y = SOUTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> WEST_FACE_Y = WEST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> EAST_FACE_Y = EAST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    
    public LadderBlockNF(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, IWaterloggedBlock.WaterlogType.FRESH));
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> EAST_SHAPE;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, WATER_LEVEL, WATERLOG_TYPE);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getLiquid(state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos oppPos = pos.relative(direction.getOpposite());
        return level.getBlockState(oppPos).isFaceSturdy(level, oppPos, direction);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if(facing.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        else {
            tickLiquid(state, currentPos, level);
            return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!context.replacingClickedOnBlock()) {
            BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
            if (blockstate.is(this) && blockstate.getValue(FACING) == context.getClickedFace()) {
                return null;
            }
        }

        LevelReader levelreader = context.getLevel();
        BlockPos blockpos = context.getClickedPos();

        for(Direction direction : context.getNearestLookingDirections()) {
            if(direction.getAxis().isHorizontal()) {
                BlockState blockstate1 = defaultBlockState().setValue(FACING, direction.getOpposite());
                if(blockstate1.canSurvive(levelreader, blockpos)) {
                    return addLiquidToPlacement(blockstate1, context);
                }
            }
        }

        return null;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return switch(pType) {
            case LAND, AIR -> true;
            case WATER -> level.getFluidState(pos).is(FluidTags.WATER);
        };
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return !entity.isOnGround();
    }

    @Override
    public boolean makesOpenTrapdoorAboveClimbable(BlockState state, LevelReader level, BlockPos pos, BlockState trapdoorState) {
        return state.getValue(FACING) == trapdoorState.getValue(TrapDoorBlock.FACING);
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        OctalDirection gapDirection = OctalDirection.fromDirection(state.getValue(FACING));
        return getTypeForThinSideClosedShape(nodeManager, state, level, pos, gapDirection, HALF_THICKNESS);
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
            case NORTH -> OctalDirection.SOUTH;
            case SOUTH -> OctalDirection.NORTH;
            case WEST -> OctalDirection.EAST;
            default -> OctalDirection.WEST;
        };
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return LevelUtil.pickBuildingMaterial(state.getBlock(), player.level);
    }
}
