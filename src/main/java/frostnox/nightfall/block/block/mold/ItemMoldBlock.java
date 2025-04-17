package frostnox.nightfall.block.block.mold;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemMoldBlock extends WaterloggedEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty TICKING = BlockStatePropertiesNF.TICKING;
    public final TagKey<Item> matchingItemTag;
    public final int maxUnits;
    private final VoxelShape shapeX, shapeZ;

    public ItemMoldBlock(VoxelShape shape, TagKey<Item> matchingItemTag, int maxUnits, Properties properties) {
        super(properties);
        this.shapeZ = shape;
        this.shapeX = Shapes.box(shape.min(Direction.Axis.Z), shape.min(Direction.Axis.Y), shape.min(Direction.Axis.X), shape.max(Direction.Axis.Z), shape.max(Direction.Axis.Y), shape.max(Direction.Axis.X));
        this.matchingItemTag = matchingItemTag;
        this.maxUnits = maxUnits;
        this.registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(TICKING, false));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        pTooltip.add(new TranslatableComponent("block.capacity", maxUnits).withStyle(ChatFormatting.DARK_GREEN));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        return level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP, SupportType.CENTER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction pFacing, BlockState pFacingState, LevelAccessor level, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if(pFacing == Direction.DOWN && !state.canSurvive(level, pCurrentPos)) return Blocks.AIR.defaultBlockState();
        else return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? shapeZ : shapeX;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, TICKING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(player.isCrouching() && player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if(level.getBlockEntity(pos) instanceof ItemMoldBlockEntity mold) {
            if(mold.isFull() && mold.isCool()) {
                if(level.isClientSide) return InteractionResult.SUCCESS;
                else {
                    LevelUtil.giveItemToPlayer(mold.getCastItem().copy(), player, true);
                    mold.getCastItem().setCount(0);
                    mold.clearFluid();
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if(state != null) {
            return state.canSurvive(context.getLevel(), context.getClickedPos()) ?
                    state.setValue(FACING, context.getHorizontalDirection().getOpposite()) : null;
        }
        else return state;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean p_48717_) {
        if(!state.is(newState.getBlock())) {
            if(level.getBlockEntity(pos) instanceof ItemMoldBlockEntity entity) {
                Containers.dropContents(level, pos, entity.getContainerDrops());
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, p_48717_);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation pRotation) {
        return state.setValue(FACING, pRotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror pMirror) {
        return state.rotate(pMirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.ITEM_MOLD.get().create(pos, state);
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entity) {
        if(!state.getValue(TICKING)) return null;
        else if(level.isClientSide()) return createTickerHelper(entity, BlockEntitiesNF.ITEM_MOLD.get(), ItemMoldBlockEntity::clientTick);
        else return createTickerHelper(entity, BlockEntitiesNF.ITEM_MOLD.get(), ItemMoldBlockEntity::serverTick);
    }
}
