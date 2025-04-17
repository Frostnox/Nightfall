package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.item.item.BuildingMaterialItem;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class ClusterBlock extends WaterloggedBlock {
    public static final IntegerProperty COUNT = BlockStatePropertiesNF.COUNT_4;
    public final Supplier<? extends Item> drop;
    private final VoxelShape[] shapes;

    public ClusterBlock(Supplier<? extends Item> drop, Properties pProperties, VoxelShape... shapes) {
        super(pProperties);
        this.drop = drop;
        this.shapes = shapes;
        registerDefaultState(defaultBlockState().setValue(COUNT, 1));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes[state.getValue(COUNT) - 1];
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(!player.isShiftKeyDown()) {
            ItemStack item = player.getItemInHand(hand);
            if(item.is(drop.get()) && item.getItem() instanceof BuildingMaterialItem) return InteractionResult.FAIL;
            else if(!level.isClientSide() && player.getInventory().add(new ItemStack(drop.get(), 1))) {
                if(state.getValue(COUNT) > 1) level.setBlock(pos, state.setValue(COUNT, state.getValue(COUNT) - 1), 3);
                else level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
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
        else return super.getStateForPlacement(context);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext pUseContext) {
        return !pUseContext.isSecondaryUseActive() && pUseContext.getItemInHand().getItem() == this.asItem() &&
                state.getValue(COUNT) < 4 || super.canBeReplaced(state, pUseContext);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportRigidBlock(level, pos.below());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(COUNT);
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(drop.get());
    }
}
