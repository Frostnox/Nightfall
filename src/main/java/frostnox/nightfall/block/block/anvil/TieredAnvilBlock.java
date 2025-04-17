package frostnox.nightfall.block.block.anvil;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IFallable;
import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class TieredAnvilBlock extends BaseEntityBlock implements IFallable {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty HAS_METAL = BlockStatePropertiesNF.HAS_METAL;
    public final int tier;

    public TieredAnvilBlock(int tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HAS_METAL, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext placeContext) {
        return defaultBlockState().setValue(FACING, placeContext.getHorizontalDirection().getClockWise());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, HAS_METAL);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(level.isClientSide()) {
            if(!state.getValue(HAS_METAL)) return InteractionResult.SUCCESS;
        }
        else if(!level.isClientSide()) {
            if(level.getBlockEntity(pos) instanceof TieredAnvilBlockEntity blockEntity) {
                ItemStack resultItem = blockEntity.getResult();
                if(!resultItem.isEmpty()) {
                    LevelUtil.giveItemToPlayer(resultItem.copy(), player, true);
                    resultItem.setCount(0);
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, state, state, 2);
                    return InteractionResult.CONSUME;
                }
                else if(!blockEntity.inProgress) {
                    NetworkHooks.openGui((ServerPlayer) player, blockEntity, pos);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean p_48717_) {
        if(!state.is(newState.getBlock())) {
            if(level.getBlockEntity(pos) instanceof TieredAnvilBlockEntity entity) {
                Containers.dropContents(level, pos, entity.getContainerDrops());
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, p_48717_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.ANVIL.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entity) {
        if(level.isClientSide()) return null;
        else return state.getValue(HAS_METAL) ? createTickerHelper(entity, BlockEntitiesNF.ANVIL.get(), TieredAnvilBlockEntity::serverTick) : null;
    }

    @Override
    public void onFall(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        if(blockEntity instanceof TieredAnvilBlockEntity anvil && anvil.inProgress) anvil.destroyGrid();
    }
}
