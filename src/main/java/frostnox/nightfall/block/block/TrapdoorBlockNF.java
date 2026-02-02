package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.util.math.OctalDirection;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class TrapdoorBlockNF extends TrapDoorBlock implements IWaterloggedBlock, ICustomPathfindable {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<IWaterloggedBlock.WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    protected static final List<AABB> NORTH_FACE_Y = NORTH_OPEN_AABB.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> SOUTH_FACE_Y = SOUTH_OPEN_AABB.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> WEST_FACE_Y = WEST_OPEN_AABB.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> EAST_FACE_Y = EAST_OPEN_AABB.getFaceShape(Direction.UP).toAabbs();
    protected static final float THICKNESS = 3F/16F, HALF_THICKNESS = THICKNESS / 2F;
    public final Supplier<SoundEvent> openSound, closeSound;

    public TrapdoorBlockNF(Properties properties, Supplier<SoundEvent> openSound, Supplier<SoundEvent> closeSound) {
        super(properties);
        this.openSound = openSound;
        this.closeSound = closeSound;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(OPEN, false).setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WATER_LEVEL, WATERLOG_TYPE);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return addLiquidToPlacement(super.getStateForPlacement(context), context);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHit) {
        state = state.cycle(OPEN);
        level.setBlock(pos, state, 3);
        tickLiquid(state, pos, level);
        playSound(player, level, pos, state.getValue(OPEN));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void playSound(@Nullable Player player, Level level, BlockPos pos, boolean open) {
        level.playSound(player, pos, open ? openSound.get() : closeSound.get(), SoundSource.BLOCKS, 1F, 0.9F + 0.1F * level.random.nextFloat());
        level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getLiquid(state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        return LevelUtil.isBlockFullySupportedHorizontally(state.setValue(OPEN, false).getShape(level, pos).bounds(),
                level, pos.relative(direction.getOpposite()), direction);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if(state.getValue(FACING).getOpposite() == facing && !state.canSurvive(level, currentPos)) return Blocks.AIR.defaultBlockState();
        tickLiquid(state, currentPos, level);
        return state;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {

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
        if(state.getValue(OPEN)) {
            OctalDirection gapDirection = OctalDirection.fromDirection(state.getValue(FACING));
            return getTypeForThinSideClosedShape(nodeManager, state, level, pos, gapDirection, HALF_THICKNESS);
        }
        else {
            if(state.getValue(HALF) == Half.TOP) return getTypeForTopClosedShape(nodeManager, pos, 1F - THICKNESS);
            else return getTypeForBottomClosedShape(nodeManager, level, pos, THICKNESS);
        }
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        if(state.getValue(OPEN)) return NodeType.CLOSED;
        return state.getValue(HALF) == Half.BOTTOM ? NodeType.OPEN : NodeType.CLOSED;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        if(state.getValue(OPEN)) {
            return switch(state.getValue(FACING)) {
                case NORTH -> NORTH_FACE_Y;
                case SOUTH -> SOUTH_FACE_Y;
                case WEST -> WEST_FACE_Y;
                default -> EAST_FACE_Y;
            };
        }
        else return state.getValue(HALF) == Half.BOTTOM ? NO_BOXES : FULL_BOXES;
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        if(state.getValue(OPEN)) {
            return switch(state.getValue(FACING)) {
                case NORTH -> NORTH_FACE_Y;
                case SOUTH -> SOUTH_FACE_Y;
                case WEST -> WEST_FACE_Y;
                default -> EAST_FACE_Y;
            };
        }
        else return state.getValue(HALF) == Half.TOP ? NO_BOXES : FULL_BOXES;
    }

    @Override
    public OctalDirection getDirection(BlockState state) {
        if(state.getValue(OPEN)) return OctalDirection.fromDirection(state.getValue(FACING)).getOpposite();
        else return OctalDirection.CENTER;
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return state.getValue(HALF) == Half.BOTTOM && !state.getValue(OPEN) ? 1 : 0;
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
        return LevelUtil.pickCloneItem(state.getBlock(), player);
    }
}
