package frostnox.nightfall.block.block.itemframe;

import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ItemFrameBlock extends WaterloggedEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    protected static final VoxelShape NORTH_SHAPE = Block.box(2, 2, 15, 14, 14, 16);
    protected static final VoxelShape EAST_SHAPE = MathUtil.rotate(NORTH_SHAPE, Rotation.CLOCKWISE_90);
    protected static final VoxelShape SOUTH_SHAPE = MathUtil.rotate(NORTH_SHAPE, Rotation.CLOCKWISE_180);
    protected static final VoxelShape WEST_SHAPE = MathUtil.rotate(NORTH_SHAPE, Rotation.COUNTERCLOCKWISE_90);
    protected static final VoxelShape UP_SHAPE = Block.box(2, 0, 2, 14, 1, 14);
    protected static final VoxelShape DOWN_SHAPE = Block.box(2, 15, 2, 14, 16, 14);

    public ItemFrameBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.ITEM_FRAME.get().create(pos, state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing, SupportType.RIGID);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, level, pos, facingPos);
        if(facing == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        else return state;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        super.attack(state, level, pos, player);
        if(level.getBlockEntity(pos) instanceof ItemFrameBlockEntity frame && !frame.isEmpty()) {
            Containers.dropContents(level, pos, frame.items);
            frame.items.set(0, ItemStack.EMPTY);
            frame.resetRotation();
            level.sendBlockUpdated(pos, state, state, 2);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(level.getBlockEntity(pos) instanceof ItemFrameBlockEntity frame) {
            if(level.isClientSide) return InteractionResult.SUCCESS;
            else {
                ItemStack item = frame.items.get(0);
                if(!item.isEmpty()) {
                    frame.incrementRotation();
                    level.sendBlockUpdated(pos, state, state, 2);
                    return InteractionResult.CONSUME;
                }
                else {
                    ItemStack heldItem = pPlayer.getItemInHand(pHand);
                    if(!heldItem.isEmpty()) {
                        ItemStack copyItem;
                        if(!pPlayer.getAbilities().instabuild) copyItem = heldItem.split(1);
                        else {
                            copyItem = heldItem.copy();
                            copyItem.setCount(1);
                        }
                        frame.items.set(0, copyItem);
                        frame.setChanged();
                        level.sendBlockUpdated(pos, state, state, 2);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return switch(state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case UP -> UP_SHAPE;
            case DOWN -> DOWN_SHAPE;
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!state.is(pNewState.getBlock()) && level.getBlockEntity(pos) instanceof ItemFrameBlockEntity frame) {
            Containers.dropContents(level, pos, frame.items);
        }
        super.onRemove(state, level, pos, pNewState, pIsMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if(state != null) return state.setValue(FACING, context.getClickedFace());
        else return state;
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
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if(level.getBlockEntity(pos) instanceof ItemFrameBlockEntity frame) {
            if(!frame.isEmpty()) return frame.items.get(0).copy();
        }
        return super.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }
}
