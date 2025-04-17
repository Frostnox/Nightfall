package frostnox.nightfall.block.block.pile;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.block.BlockNF;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class PileBlock extends BlockNF {
    public static final IntegerProperty COUNT = BlockStatePropertiesNF.COUNT_8;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public final Supplier<? extends Item> drop;
    private final VoxelShape shapeZ, shapeX;

    public PileBlock(Supplier<? extends Item> drop, VoxelShape shapeZ, Properties properties) {
        super(properties);
        this.drop = drop;
        this.shapeZ = shapeZ;
        this.shapeX = MathUtil.rotate(shapeZ, Rotation.CLOCKWISE_90);
        registerDefaultState(defaultBlockState().setValue(COUNT, 1).setValue(AXIS, Direction.Axis.Z));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.Z ? shapeZ : shapeX;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(!player.isShiftKeyDown() && !player.getItemInHand(hand).is(drop.get())) {
            if(!level.isClientSide() && player.getInventory().add(new ItemStack(drop.get(), 1))) {
                if(state.getValue(COUNT) > 1) level.setBlock(pos, state.setValue(COUNT, state.getValue(COUNT) - 1), 3);
                else level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, state.getSoundType().getVolume(), state.getSoundType().getPitch());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, level, pos, facingPos);
        if(!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        else return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if(state.is(this)) return state.cycle(COUNT);
        else state = super.getStateForPlacement(context);
        return state == null ? state : state.setValue(AXIS, context.getHorizontalDirection().getAxis());
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
    public boolean canBeReplaced(BlockState state, BlockPlaceContext pUseContext) {
        return !pUseContext.isSecondaryUseActive() && pUseContext.getItemInHand().getItem() == this.asItem() &&
                state.getValue(COUNT) < 8 || super.canBeReplaced(state, pUseContext);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(COUNT, AXIS);
    }
}
