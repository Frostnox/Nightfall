package frostnox.nightfall.block.block;

import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.MoonPhase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class WardingEffigyBlock extends WaterloggedBlock implements ICustomPathfindable {
    public static final VoxelShape SHAPE_Z = Shapes.or(Block.box(4.5F, 0, 4.5F, 11.5F, 2, 11.5F),
            Block.box(6.5F, 2, 6.5F, 9.5F, 9, 9.5F),
            Block.box(1.5F, 9, 6.5F, 14.5F, 11, 9.5F),
            Block.box(5.5F, 11, 5.5F, 10.5F, 16, 10.5F));
    public static final VoxelShape SHAPE_X = MathUtil.rotate(SHAPE_Z, Rotation.CLOCKWISE_90);
    protected static final List<AABB> AABB_TOP_Z = SHAPE_Z.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> AABB_TOP_X = SHAPE_X.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> AABB_BOTTOM_Z = SHAPE_Z.getFaceShape(Direction.DOWN).toAabbs();
    protected static final List<AABB> AABB_BOTTOM_X = SHAPE_X.getFaceShape(Direction.DOWN).toAabbs();
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public WardingEffigyBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.Z));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        pTooltip.add(new TranslatableComponent("block.warding_effigy.info").withStyle(ChatFormatting.BLUE));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction pFacing, BlockState pFacingState, LevelAccessor level, BlockPos pCurrentPos, BlockPos pFacingPos) {
        state = super.updateShape(state, pFacing, pFacingState, level, pCurrentPos, pFacingPos);
        if(pFacing == Direction.DOWN && !state.canSurvive(level, pCurrentPos)) return Blocks.AIR.defaultBlockState();
        else return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        return level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP, SupportType.CENTER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return !defaultBlockState().canSurvive(pContext.getLevel(), pContext.getClickedPos()) ? null :
                super.getStateForPlacement(pContext).setValue(AXIS, pContext.getHorizontalDirection().getAxis());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return state.getValue(AXIS) == Direction.Axis.Z ? SHAPE_Z : SHAPE_X;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation pRot) {
        return switch(pRot) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch(state.getValue(AXIS)) {
                case Z -> state.setValue(AXIS, Direction.Axis.X);
                case X -> state.setValue(AXIS, Direction.Axis.Z);
                default -> state;
            };
            default -> state;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(AXIS);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random pRand) {
        if(LevelUtil.isNight(level) && MoonPhase.get(level) != MoonPhase.NEW && pRand.nextInt() % 4 == 0) {
            level.addParticle(ParticleTypesNF.ESSENCE_MOON.get(), pos.getX() + 0.35 + pRand.nextDouble() * 0.3, pos.getY() + 0.1 + pRand.nextDouble(),
                    pos.getZ() + 0.35 + pRand.nextDouble() * 0.3, 0, 0, 0);
        }
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && !oldState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).addWardingEffigy(pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).removeWardingEffigy(pos);
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
        return state.getValue(AXIS) == Direction.Axis.Z ? AABB_TOP_Z : AABB_TOP_X;
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return state.getValue(AXIS) == Direction.Axis.Z ? AABB_BOTTOM_Z : AABB_BOTTOM_X;
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }
}
