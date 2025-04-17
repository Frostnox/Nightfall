package frostnox.nightfall.block.block.crucible;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IHeatable;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrucibleBlock extends WaterloggedEntityBlock implements ICustomPathfindable, IHeatable {
    public static final IntegerProperty HEAT = BlockStatePropertiesNF.HEAT_FULL;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape SHAPE_Z = Shapes.join(Shapes.join(box(4D, 0.0D, 4D, 12D, 8.0D, 12D),
                    box(6D, 8D, 6D, 10D, 10.0D, 10D), BooleanOp.OR),
                    box(2, 5, 6, 14, 6, 10), BooleanOp.OR);
    private static final VoxelShape SHAPE_X = MathUtil.rotate(SHAPE_Z, Rotation.CLOCKWISE_90);
    protected static final List<AABB> AABB_TOP_Z = SHAPE_Z.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> AABB_TOP_X = SHAPE_X.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> AABB_BOTTOM_Z = SHAPE_Z.getFaceShape(Direction.DOWN).toAabbs();
    protected static final List<AABB> AABB_BOTTOM_X = SHAPE_X.getFaceShape(Direction.DOWN).toAabbs();
    public final int fluidCapacity;
    public final float maxTemp;

    public CrucibleBlock(int fluidCapacity, float maxTemp, Properties properties) {
        super(properties);
        this.fluidCapacity = fluidCapacity;
        this.maxTemp = maxTemp;
        this.registerDefaultState(this.stateDefinition.any().setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH)
                .setValue(HEAT, 0).setValue(AXIS, Direction.Axis.Z));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        pTooltip.add(new TranslatableComponent("block.capacity", fluidCapacity).withStyle(ChatFormatting.DARK_GREEN));
        pTooltip.add(new TranslatableComponent("block.crucible.pour"));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return state.getValue(AXIS) == Direction.Axis.Z ? SHAPE_Z : SHAPE_X;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HEAT, AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return addLiquidToPlacement(defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis()), context);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(player.isCrouching() && player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if(level.isClientSide()) return InteractionResult.SUCCESS;
        else {
            if(level.getBlockEntity(pos) instanceof CrucibleBlockEntity blockEntity) {
                NetworkHooks.openGui((ServerPlayer) player, blockEntity, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean p_48717_) {
        if(!state.is(newState.getBlock())) {
            if(level.getBlockEntity(pos) instanceof CrucibleBlockEntity entity) {
                Containers.dropContents(level, pos, entity.getContainerDrops());
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, p_48717_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.CRUCIBLE.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entity) {
        if(level.isClientSide()) return null;
        else return createTickerHelper(entity, BlockEntitiesNF.CRUCIBLE.get(), CrucibleBlockEntity::serverTick);
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
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return switch(pType) {
            case LAND, AIR -> true;
            case WATER -> level.getFluidState(pos).is(FluidTags.WATER);
        };
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return getTypeForCenteredBottomShape(nodeManager, pos, 10F/16F);
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return NodeType.OPEN;
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

    @Override
    public void applyHeat(Level level, BlockPos pos, BlockState state, TieredHeat heat, Direction fromDir) {
        if(fromDir == Direction.DOWN && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
            crucible.targetTemperature = Math.min(heat.getBaseTemp(), maxTemp);
        }
    }
}
