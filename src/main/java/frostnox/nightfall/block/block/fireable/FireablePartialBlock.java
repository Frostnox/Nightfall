package frostnox.nightfall.block.block.fireable;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.FluidsNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FireablePartialBlock extends SimpleFireableBlock implements IWaterloggedBlock, ICustomPathfindable {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    public final int excludedWaterLevel;

    public FireablePartialBlock(int cookTicks, TieredHeat cookHeat, RegistryObject<? extends Block> firedBlock, int excludedWaterLevel, Properties properties) {
        super(cookTicks, cookHeat, firedBlock, properties);
        this.excludedWaterLevel = excludedWaterLevel;
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    @Override
    public boolean isStructureValid(Level level, BlockPos pos, BlockState state) {
        if(cookHeat.getTier() <= 1) return true;
        else return LevelUtil.getNearbyKilnTier(level, pos) >= cookHeat.getTier() - 1;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return firedBlock.get().getShape(state, blockGetter, pos, collisionContext);
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter pReader, BlockPos pos) {
        return this.getCollisionShape(state, pReader, pos, CollisionContext.empty());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.hasCollision ? firedBlock.get().getCollisionShape(state, level, pos, context) : Shapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATER_LEVEL, WATERLOG_TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = this.defaultBlockState();
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        if(fluidstate.getType() == FluidsNF.WATER.get()) return blockstate.setValue(WATER_LEVEL, 7);
        else if(fluidstate.getType() == FluidsNF.WATER_FLOWING.get() && fluidstate.getAmount() > getExcludedWaterLevel(blockstate)) return blockstate.setValue(WATER_LEVEL, fluidstate.getAmount());
        else return blockstate;
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
    public int getExcludedWaterLevel(BlockState state) {
        return excludedWaterLevel;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.FIREABLE_POTTERY.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return level.isClientSide() ? null : createTickerHelper(blockEntity, BlockEntitiesNF.FIREABLE_POTTERY.get(), FireableBlock::serverEntityTick);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return firedBlock.get().isPathfindable(state, level, pos, pType);
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        if(firedBlock.get() instanceof ICustomPathfindable pathfindable) {
            return pathfindable.getRawNodeType(nodeManager, state, level, pos);
        }
        return NodeType.CLOSED;
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        if(firedBlock.get() instanceof ICustomPathfindable pathfindable) {
            return pathfindable.getFloorNodeType(nodeManager, state, level, pos);
        }
        return NodeType.CLOSED;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        if(firedBlock.get() instanceof ICustomPathfindable pathfindable) {
            return pathfindable.getTopFaceShape(state);
        }
        return FULL_BOXES;
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        if(firedBlock.get() instanceof ICustomPathfindable pathfindable) {
            return pathfindable.getBottomFaceShape(state);
        }
        return FULL_BOXES;
    }
}
