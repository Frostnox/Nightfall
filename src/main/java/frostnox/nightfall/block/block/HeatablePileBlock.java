package frostnox.nightfall.block.block;

import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.IHeatSource;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.registry.forge.FluidsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Comparator;
import java.util.Random;

//Needs adjusting to new heat values if used
public abstract class HeatablePileBlock extends Block implements IWaterloggedBlock {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    public static final IntegerProperty HEAT = BlockStatePropertiesNF.HEAT;
    public static final BooleanProperty CHANGED = BlockStatePropertiesNF.CHANGED;
    public final IntegerProperty count;
    private final VoxelShape[] shapes;

    public HeatablePileBlock(IntegerProperty count, Properties properties, VoxelShape... shapes) {
        super(properties);
        this.count = count;
        this.shapes = shapes;
        this.registerDefaultState(this.stateDefinition.any().setValue(count, 1).setValue(HEAT, 1).setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH).setValue(CHANGED, false));
    }

    public boolean isHeated(BlockState state, Level level, BlockPos pos) {
        if(state.getValue(WATER_LEVEL) <= 0 && level.getBlockState(pos.below()).getBlock() instanceof IHeatSource heatSource) return true;
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(player.getItemInHand(hand).isEmpty()) {
            if(!level.isClientSide() && player.getInventory().add(new ItemStack(this.asItem(), 1))) {
                state = state.setValue(CHANGED, true);
                if(state.getValue(count) > 1) level.setBlock(pos, state.setValue(count, state.getValue(count) - 1), 3);
                else level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, state.getSoundType().getVolume(), state.getSoundType().getPitch());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        level.scheduleTick(pos, this, 60);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        if(state.getValue(HEAT) > 0 && level.isRainingAt(pos.above()) && level.getRandom().nextFloat() < 0.025F * state.getValue(count)) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.15F, 2.0F + (level.getRandom().nextFloat()) * 0.5F, false);
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + rand.nextDouble(), pos.getY() + rand.nextDouble() * 0.25D, pos.getZ() + rand.nextDouble(), 0, 0, 0);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity pEntity) {
        int heat = state.getValue(HEAT);
        if(!pEntity.fireImmune() && pEntity.invulnerableTime <= 10 && heat > 0 && pEntity instanceof LivingEntity) {
            pEntity.hurt(DamageTypeSource.HOT_FLOOR, 0.75F + 0.25F * heat);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        if(true || isHeated(state, level, pos)) level.scheduleTick(pos, this, 60);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext pUseContext) {
        return !pUseContext.isSecondaryUseActive() && pUseContext.getItemInHand().getItem() == this.asItem() && state.getValue(count) < count.getPossibleValues().stream().max(Comparator.naturalOrder()).get() || super.canBeReplaced(state, pUseContext);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if(blockstate.is(this)) {
            blockstate = blockstate.setValue(HEAT, 1);
            return blockstate.cycle(count);
        }
        else {
            blockstate = super.getStateForPlacement(context);
            FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
            if(fluidstate.getType() == FluidsNF.WATER.get()) return blockstate.setValue(WATER_LEVEL, 7);
            else if(fluidstate.getType() == FluidsNF.WATER_FLOWING.get() && fluidstate.getAmount() > getExcludedWaterLevel(blockstate)) return blockstate.setValue(WATER_LEVEL, fluidstate.getAmount());
            else return blockstate;
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if(!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        tickLiquid(state, pos, level);
        if(!level.isClientSide() && facing == Direction.DOWN && facingState.getBlock() instanceof IHeatSource heatSource) {
            if(true) level.scheduleTick(pos, this, 60);
            else if(!state.getValue(CHANGED)) level.setBlock(pos, state.setValue(CHANGED, true), 3);
        }
        return state;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getLiquid(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes[state.getValue(count) - 1];
    }

    //Override and add the count property here
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HEAT, WATER_LEVEL, CHANGED, WATERLOG_TYPE);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        boolean result = IWaterloggedBlock.super.placeLiquid(level, pos, state, fluid);
        if(result && level instanceof ServerLevel serverLevel && state.getValue(HEAT) > 0) { //Do this on server since client only runs this function when water is placed by the player
            level.setBlock(pos, level.getBlockState(pos).setValue(HEAT, 1), 3);
            serverLevel.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.8F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.SMOKE, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.2D, (double)pos.getZ() + 0.5D, 10 + level.getRandom().nextInt() % 4, 0.3D, 0.1D, 0.3D, 0.0D);
        }
        return result;
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

}
