package frostnox.nightfall.block.block.rack;

import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.data.TagsNF;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RackBlock extends WaterloggedEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final VoxelShape NORTH_SHAPE = Block.box(3, 2, 13, 13, 14, 16);
    protected static final VoxelShape EAST_SHAPE = MathUtil.rotate(NORTH_SHAPE, Rotation.CLOCKWISE_90);
    protected static final VoxelShape SOUTH_SHAPE = MathUtil.rotate(NORTH_SHAPE, Rotation.CLOCKWISE_180);
    protected static final VoxelShape WEST_SHAPE = MathUtil.rotate(NORTH_SHAPE, Rotation.COUNTERCLOCKWISE_90);

    public RackBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.SOUTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.RACK.get().create(pos, state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return Block.isFaceFull(level.getBlockState(supportPos).getBlockSupportShape(level, supportPos), facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, level, pos, facingPos);
        if(!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        else return state;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHit) {
        if(level.getBlockEntity(pos) instanceof RackBlockEntity rack) {
            if(level.isClientSide) return InteractionResult.SUCCESS;
            else {
                ItemStack heldItem = player.getItemInHand(hand);
                double y = Math.abs(pHit.getLocation().y) % 1D;
                int index;
                if(y < 5D/16D) index = 0;
                else if(y < 11D/16D) index = 1;
                else index = 2;
                ItemStack item = rack.items.get(index);
                if(!item.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(item.copy());
                    rack.items.set(index, ItemStack.EMPTY);
                    rack.setChanged();
                    level.sendBlockUpdated(pos, state, state, 2);
                    return InteractionResult.CONSUME;
                }
                else if(heldItem.is(TagsNF.RACK_ITEM)) {
                    rack.items.set(index, heldItem.copy());
                    if(!player.getAbilities().instabuild) player.setItemInHand(hand, ItemStack.EMPTY);
                    rack.setChanged();
                    level.sendBlockUpdated(pos, state, state, 2);
                    return InteractionResult.CONSUME;
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
            default -> WEST_SHAPE;
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!state.is(pNewState.getBlock()) && level.getBlockEntity(pos) instanceof RackBlockEntity rack) {
            Containers.dropContents(level, pos, rack.items);
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
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }
}
