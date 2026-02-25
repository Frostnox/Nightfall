package frostnox.nightfall.block.block.anvil;

import frostnox.nightfall.block.*;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class TieredAnvilBlock extends BaseEntityBlock implements IFallable, ITimeSimulatedBlock, IWaterloggedBlock, ICustomPathfindable {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<IWaterloggedBlock.WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty HAS_METAL = BlockStatePropertiesNF.HAS_METAL;
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(Block.box(2.0D, 0.0D, 4.0D, 14.0D, 16.0D, 16.0D),
            Block.box(3.0D, 8.0D, 0.0D, 13.0D, 15.0D, 4.0D));
    private static final VoxelShape NORTH_SHAPE = MathUtil.rotate(SOUTH_SHAPE, Rotation.CLOCKWISE_180);
    private static final VoxelShape EAST_SHAPE = MathUtil.rotate(SOUTH_SHAPE, Rotation.COUNTERCLOCKWISE_90);
    private static final VoxelShape WEST_SHAPE = MathUtil.rotate(SOUTH_SHAPE, Rotation.CLOCKWISE_90);
    private static final List<AABB> NORTH_FACE_Y = NORTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    private static final List<AABB> SOUTH_FACE_Y = SOUTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    private static final List<AABB> WEST_FACE_Y = WEST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    private static final List<AABB> EAST_FACE_Y = EAST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    public final int tier;

    public TieredAnvilBlock(int tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HAS_METAL, false).setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, IWaterloggedBlock.WaterlogType.FRESH));
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
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
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, HAS_METAL, WATER_LEVEL, WATERLOG_TYPE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.ANVIL.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entity) {
        return state.getValue(HAS_METAL) ? createTickerHelper(entity, BlockEntitiesNF.ANVIL.get(), TieredAnvilBlockEntity::tick) : null;
    }

    @Override
    public void onFall(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        if(blockEntity instanceof TieredAnvilBlockEntity anvil) anvil.destroyWorkpiece();
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && (!oldState.is(this) || oldState.getValue(HAS_METAL) != newState.getValue(HAS_METAL)) && LevelData.isPresent(level)) {
            if(!newState.getValue(HAS_METAL)) ChunkData.get(level.getChunkAt(pos)).removeSimulatableBlock(TickPriority.NORMAL, pos);
            else ChunkData.get(level.getChunkAt(pos)).addSimulatableBlock(TickPriority.NORMAL, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!pNewState.is(this)) {
            if(state.getValue(HAS_METAL)) {
                if(level.getBlockEntity(pos) instanceof TieredAnvilBlockEntity anvil && anvil.hasWorkpiece()) {
                    Containers.dropContents(level, pos, NonNullList.of(ItemStack.EMPTY, new ItemStack(Metal.fromString(anvil.getWorkpiece().toString()).getMatchingItem(TagsNF.SCRAP))));
                }
                if(LevelData.isPresent(level)) ChunkData.get(level.getChunkAt(pos)).removeSimulatableBlock(TickPriority.NORMAL, pos);
            }
        }
        super.onRemove(state, level, pos, pNewState, pIsMoving);
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        if(state.getValue(HAS_METAL) && level.getBlockEntity(pos) instanceof TieredAnvilBlockEntity anvil) {
            TieredAnvilBlockEntity.tick(level, pos, state, anvil, (elapsedTime > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) elapsedTime));
        }
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
        nodeManager.getNode(pos).partial = true;
        return NodeType.CLOSED;
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
        return FULL_BOXES;
    }
}