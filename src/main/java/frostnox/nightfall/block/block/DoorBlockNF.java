package frostnox.nightfall.block.block;

import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class DoorBlockNF extends HatchBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public DoorBlockNF(Properties properties, Supplier<SoundEvent> openSound, Supplier<SoundEvent> closeSound) {
        super(properties, openSound, closeSound);
        registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER).setValue(OPEN, false).setValue(HINGE, DoorHingeSide.LEFT).setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if(pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            BlockState state = super.getStateForPlacement(context);
            if(state == null) return null;
            else return state.setValue(HALF, DoubleBlockHalf.LOWER);
        }
        else return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity pPlacer, ItemStack pStack) {
        state = state.setValue(WATER_LEVEL, 0).setValue(HALF, DoubleBlockHalf.UPPER);
        BlockPos abovePos = pos.above();
        level.setBlock(abovePos, addLiquidToPlacement(state, abovePos, level), 3);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        state = state.cycle(OPEN);
        level.setBlock(pos, state, 11);
        tickLiquid(state, pos, level);
        Direction updateDirection = state.getValue(HALF) == DoubleBlockHalf.UPPER ? Direction.DOWN : Direction.UP;
        level.updateNeighborsAtExceptFromFacing(pos.relative(updateDirection), this, updateDirection.getOpposite());
        playSound(pPlayer, level, pos, state.getValue(OPEN));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, level, currentPos, facingPos);
        if(state.isAir()) return state;
        DoubleBlockHalf half = state.getValue(HALF);
        if(facing.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (facing == Direction.UP)) {
            state = facingState.is(this) && facingState.getValue(HALF) != half ? state.setValue(FACING, facingState.getValue(FACING)).setValue(OPEN, facingState.getValue(OPEN)).setValue(HINGE, facingState.getValue(HINGE)) : Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if(state.getValue(HALF) == DoubleBlockHalf.UPPER) return level.getBlockState(pos.below()).is(this);
        else return super.canSurvive(state, level, pos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player pPlayer) {
        if(!level.isClientSide && pPlayer.isCreative()) LevelUtil.preventBlockLowerHalfDrop(level, pos, state, pPlayer);
        super.playerWillDestroy(level, pos, state, pPlayer);
    }

    @Override
    public long getSeed(BlockState state, BlockPos pos) {
        return Mth.getSeed(pos.getX(), pos.below(state.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pos.getZ());
    }

    @Override
    public boolean hasAnyFloorAt(BlockState state, BlockPos pos, BlockGetter level) {
        if(state.getValue(HALF) == DoubleBlockHalf.UPPER) return false;
        else return super.hasAnyFloorAt(state, pos, level);
    }
}
