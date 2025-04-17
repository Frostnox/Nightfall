package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import frostnox.nightfall.util.LevelUtil;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class CrossCollisionBlockNF extends CrossCollisionBlock implements IWaterloggedBlock, ICustomPathfindable {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    protected final ObjectList<List<AABB>> yFaceByIndex;
    protected final VoxelShape[] blockEntityCollisionShapeByIndex;

    public CrossCollisionBlockNF(float nodeWidth, float extensionWidth, float nodeHeight, float extensionBottom, float extensionHeight, BlockBehaviour.Properties properties) {
        super(nodeWidth, extensionWidth, nodeHeight, extensionBottom, extensionHeight, properties);
        List<List<AABB>> temp = new ArrayList<>(collisionShapeByIndex.length);
        for(VoxelShape shape : collisionShapeByIndex) temp.add(shape.getFaceShape(Direction.UP).toAabbs());
        yFaceByIndex = new ObjectImmutableList<>(temp);
        if(extensionHeight > 16F) {
            blockEntityCollisionShapeByIndex = makeShapes(nodeWidth, extensionWidth, nodeHeight, extensionBottom, 16F);
        }
        else blockEntityCollisionShapeByIndex = collisionShapeByIndex;
    }

    public abstract boolean connectsTo(BlockState state, boolean pIsSideSolid, Direction pDirection);

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if(pContext instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof MovingBlockEntity) {
            return blockEntityCollisionShapeByIndex[getAABBIndex(pState)];
        }
        else return collisionShapeByIndex[getAABBIndex(pState)];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(NORTH, EAST, WEST, SOUTH, WATER_LEVEL, WATERLOG_TYPE);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getLiquid(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        tickLiquid(state, currentPos, level);
        return facing.getAxis().getPlane() == Direction.Plane.HORIZONTAL ? state.setValue(PROPERTY_BY_DIRECTION.get(facing), this.connectsTo(facingState, facingState.isFaceSturdy(level, facingPos, facing.getOpposite()), facing.getOpposite())) : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter pReader, BlockPos pos, CollisionContext context) {
        return this.getShape(state, pReader, pos, context);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter pReader, BlockPos pos) {
        return state.getValue(WATER_LEVEL) == 0;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return switch(pType) {
            case LAND, AIR -> true;
            case WATER -> level.getFluidState(pos).is(FluidTags.WATER);
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos northPos = pos.north(), eastPos = pos.east(), southPos = pos.south(), westPos = pos.west();
        BlockState northState = level.getBlockState(northPos), eastState = level.getBlockState(eastPos), southState = level.getBlockState(southPos), westState = level.getBlockState(westPos);
        return addLiquidToPlacement(super.getStateForPlacement(context)
                        .setValue(NORTH, this.connectsTo(northState, northState.isFaceSturdy(level, northPos, Direction.SOUTH), Direction.SOUTH))
                        .setValue(EAST, this.connectsTo(eastState, eastState.isFaceSturdy(level, eastPos, Direction.WEST), Direction.WEST))
                        .setValue(SOUTH, this.connectsTo(southState, southState.isFaceSturdy(level, southPos, Direction.NORTH), Direction.NORTH))
                        .setValue(WEST, this.connectsTo(westState, westState.isFaceSturdy(level, westPos, Direction.EAST), Direction.EAST)),
                context);
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
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
        return yFaceByIndex.get(getAABBIndex(state));
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return yFaceByIndex.get(getAABBIndex(state));
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid pFluid) {
        return IWaterloggedBlock.super.canPlaceLiquid(level, pos, state, pFluid);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState pFluidState) {
        return IWaterloggedBlock.super.placeLiquid(level, pos, state, pFluidState);
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        return IWaterloggedBlock.super.pickupBlock(level, pos, state);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return IWaterloggedBlock.super.getPickupSound();
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return LevelUtil.pickBuildingMaterial(state.getBlock(), player.level);
    }
}
