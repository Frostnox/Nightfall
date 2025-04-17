package frostnox.nightfall.block.block.barrel;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.block.block.crucible.CrucibleBlockEntity;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class BarrelBlockNF extends WaterloggedEntityBlock implements ICustomPathfindable {
    public static final DirectionProperty FACING = BlockStatePropertiesNF.FACING_NOT_DOWN;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    protected static final VoxelShape SHAPE_X = Block.box(0, 0, 2, 16, 12, 14);
    protected static final VoxelShape SHAPE_Y = Block.box(2, 0, 2, 14, 16, 14);
    protected static final VoxelShape SHAPE_Z = Block.box(2, 0, 0, 14, 12, 16);
    protected static final List<AABB> AABB_X = List.of(SHAPE_X.toAabbs().get(0));
    protected static final List<AABB> AABB_Y = List.of(SHAPE_Y.toAabbs().get(0));
    protected static final List<AABB> AABB_Z = List.of(SHAPE_Z.toAabbs().get(0));

    public BarrelBlockNF(Properties pProperties) {
        super(pProperties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(OPEN, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.BARREL.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entity) {
        if(level.isClientSide()) return null;
        else return createTickerHelper(entity, BlockEntitiesNF.BARREL.get(), BarrelBlockEntityNF::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        else {
            Direction facing = state.getValue(FACING);
            BlockPos facingPos = pos.relative(facing);
            if(!level.getBlockState(facingPos).isFaceSturdy(level, facingPos, facing.getOpposite(), SupportType.FULL) &&
                    level.getBlockEntity(pos) instanceof BarrelBlockEntityNF barrel) {
                NetworkHooks.openGui((ServerPlayer) pPlayer, barrel, pos);
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return switch(state.getValue(FACING).getAxis()) {
            case X -> SHAPE_X;
            case Y -> SHAPE_Y;
            case Z -> SHAPE_Z;
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!state.is(pNewState.getBlock()) && level.getBlockEntity(pos) instanceof Container container) {
            Containers.dropContents(level, pos, container);
        }
        super.onRemove(state, level, pos, pNewState, pIsMoving);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @javax.annotation.Nullable LivingEntity pPlacer, ItemStack pStack) {
        if(pStack.hasCustomHoverName() && level.getBlockEntity(pos) instanceof BaseContainerBlockEntity container) {
            container.setCustomName(pStack.getHoverName());
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(level.getBlockEntity(pos) instanceof BarrelBlockEntityNF barrel) barrel.recheckOpen();
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext).setValue(FACING,
                pContext.getClickedFace().getAxis().isVertical() ? Direction.UP : pContext.getClickedFace());
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
        if(state.getValue(FACING) == Direction.UP) return NodeType.CLOSED;
        else return getTypeForCenteredBottomShape(nodeManager, pos, 12F/16F);
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FACING) == Direction.UP ? NodeType.CLOSED : NodeType.OPEN;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return switch(state.getValue(FACING).getAxis()) {
            case X -> AABB_X;
            case Y -> AABB_Y;
            case Z -> AABB_Z;
        };
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return switch(state.getValue(FACING).getAxis()) {
            case X -> AABB_X;
            case Y -> AABB_Y;
            case Z -> AABB_Z;
        };
    }
}
