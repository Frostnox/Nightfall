package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * Fence gates with waterlogging support
 */
public class FenceGateBlockNF extends FenceGateBlock implements IWaterloggedBlock, ICustomPathfindable {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    protected static final List<AABB> Y_FACE_X = X_COLLISION_SHAPE.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> Y_FACE_Z = Z_COLLISION_SHAPE.getFaceShape(Direction.UP).toAabbs();

    public FenceGateBlockNF(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATER_LEVEL, WATERLOG_TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if(!state.canSurvive(context.getLevel(), context.getClickedPos())) state = state.setValue(FACING, state.getValue(FACING).getClockWise());
        return addLiquidToPlacement(state, context);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, level, pos, facingPos);
        tickLiquid(state, pos, level);
        if(!state.getValue(IN_WALL) && state.getValue(FACING).getClockWise().getAxis() == facing.getAxis() && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction dirCW = state.getValue(FACING).getClockWise();
        Direction dirCC = state.getValue(FACING).getCounterClockWise();
        BlockPos posCW = pos.relative(dirCW), posCC = pos.relative(dirCC);
        BlockState stateCW = level.getBlockState(posCW), stateCC = level.getBlockState(posCC);
        if((stateCW.is(BlockTags.FENCES) || stateCW.is(BlockTags.WALLS)) && (stateCC.is(BlockTags.FENCES) || stateCC.is(BlockTags.WALLS))) return true;
        AABB box = state.setValue(OPEN, false).getShape(level, pos).bounds();
        return LevelUtil.isBlockFullySupportedHorizontally(box, level, posCW, dirCC) && LevelUtil.isBlockFullySupportedHorizontally(box, level, posCC, dirCW);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter pReader, BlockPos pos) {
        return state.getValue(WATER_LEVEL) == 0;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getLiquid(state);
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        if(state.getValue(OPEN)) return NodeType.OPEN_OR_WALKABLE;
        else {
            nodeManager.getNode(pos).partial = true;
            return NodeType.CLOSED;
        }
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(OPEN) ? NodeType.OPEN : NodeType.CLOSED;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        if(state.getValue(OPEN)) return NO_BOXES;
        else return state.getValue(FACING).getAxis() == Direction.Axis.Z ? Y_FACE_Z : Y_FACE_X;
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return getTopFaceShape(state);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return LevelUtil.pickCloneItem(state.getBlock(), player);
    }
}
