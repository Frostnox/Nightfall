package frostnox.nightfall.block.block;

import frostnox.nightfall.block.*;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static frostnox.nightfall.block.BlockStatePropertiesNF.WATERLOG_TYPE;
import static frostnox.nightfall.block.BlockStatePropertiesNF.WATER_LEVEL;

public class LanternBlockNF extends LanternBlock implements IWaterloggedBlock, IIgnitable, ICustomPathfindable {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    protected static final VoxelShape COLLISION_SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 7.0D, 11.0D);
    protected static final VoxelShape HANGING_COLLISION_SHAPE = Block.box(5.0D, 1.0D, 5.0D, 11.0D, 8.0D, 11.0D);
    protected static final List<AABB> TOP_FACE = List.of(COLLISION_SHAPE.toAabbs().get(0));
    public final boolean lit;
    public final Supplier<? extends Block> oppositeBlock;

    public LanternBlockNF(boolean lit, Supplier<? extends Block> oppositeBlock, Properties pProperties) {
        super(pProperties);
        this.lit = lit;
        this.oppositeBlock = oppositeBlock;
        registerDefaultState(defaultBlockState().setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? HANGING_COLLISION_SHAPE : COLLISION_SHAPE;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getLiquid(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        tickLiquid(state, currentPos, level);
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = addLiquidToPlacement(super.getStateForPlacement(context), context);
        if(lit && state != null && state.getValue(WATER_LEVEL) > (state.getValue(HANGING) ? 1 : 0)) {
            state = copyLiquid(state, oppositeBlock.get().defaultBlockState());
            context.getLevel().playSound(context.getPlayer(), context.getClickedPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.2F + context.getLevel().random.nextFloat());
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WATER_LEVEL, WATERLOG_TYPE);
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
        if(state.getValue(HANGING)) {
            nodeManager.getNode(pos).partial = true;
            return NodeType.CLOSED;
        }
        else return getTypeForCenteredBottomShape(nodeManager, pos, 7F/16F);
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(HANGING) ? NodeType.CLOSED : NodeType.OPEN;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return state.getValue(HANGING) ? NO_BOXES : TOP_FACE;
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return NO_BOXES;
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

    @Override
    public BlockState copyLiquid(BlockState oldState, BlockState newState) {
        return IWaterloggedBlock.super.copyLiquid(oldState, newState).setValue(HANGING, oldState.getValue(HANGING));
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        boolean result = IWaterloggedBlock.super.placeLiquid(level, pos, state, fluid);
        if(result && level instanceof ServerLevel serverLevel && lit) { //Do this on server since client only runs this function when water is placed by the player
            level.setBlock(pos, copyLiquid(level.getBlockState(pos), oppositeBlock.get().defaultBlockState()), 3);
            serverLevel.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.2F + serverLevel.random.nextFloat());
        }
        return result;
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid pFluid) {
        return IWaterloggedBlock.super.canPlaceLiquid(level, pos, state, pFluid);
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
    public boolean tryToIgnite(Level level, BlockPos pos, BlockState state, ItemStack stack, TieredHeat heat) {
        if(level.isClientSide()) return false;
        if(!lit) {
            level.setBlockAndUpdate(pos, oppositeBlock.get().defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean isIgnited(BlockState state) {
        return lit;
    }
}
