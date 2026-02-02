package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IFallable;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

public class SlabBlockNF extends SlabBlock implements IWaterloggedBlock, ICustomPathfindable, IFallable {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    public final BlockState baseState;
    public final Block base;

    public SlabBlockNF(Supplier<? extends Block> baseBlock, BlockBehaviour.Properties properties) {
        super(properties);
        this.baseState = baseBlock.get().defaultBlockState();
        this.base = baseBlock.get();
        registerDefaultState(defaultBlockState().setValue(TYPE, SlabType.BOTTOM).setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WATER_LEVEL, WATERLOG_TYPE);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if(state.getValue(TYPE) == SlabType.DOUBLE) return state.setValue(WATER_LEVEL, 0);
        else return addLiquidToPlacement(state, context);
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
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return switch(pType) {
            case LAND, AIR -> state.getValue(TYPE) != SlabType.DOUBLE;
            case WATER -> level.getFluidState(pos).is(FluidTags.WATER);
        };
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        SlabType type = state.getValue(TYPE);
        if(type == SlabType.BOTTOM) return 4;
        else if(type == SlabType.DOUBLE) return 7;
        else return 0;
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        SlabType type = state.getValue(TYPE);
        if(type == SlabType.TOP) return getTypeForTopClosedShape(nodeManager, pos, 0.5F);
        else if(type == SlabType.BOTTOM) return getTypeForBottomClosedShape(nodeManager, level, pos, 0.5F);
        else return NodeType.CLOSED; //Double, shouldn't reach here
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(TYPE) == SlabType.BOTTOM ? NodeType.OPEN : NodeType.CLOSED;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return switch(state.getValue(TYPE)) {
            case DOUBLE, TOP -> FULL_BOXES;
            default -> NO_BOXES;
        };
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return switch(state.getValue(TYPE)) {
            case DOUBLE, BOTTOM -> FULL_BOXES;
            default -> NO_BOXES;
        };
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
    public void onMovingBlockEntityCreated(BlockState state, Level level, BlockPos pos, MovingBlockEntity entity) {
        if(entity.getBlockState().getValue(TYPE) == SlabType.TOP) {
            entity.setBlockState(entity.getBlockState().setValue(TYPE, SlabType.BOTTOM));
            entity.setPos(entity.getX(), entity.getY() + 0.5, entity.getZ());
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        baseState.getBlock().fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random pRand) {
        base.animateTick(state, level, pos, pRand);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        baseState.attack(level, pos, player);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        base.destroy(level, pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        if (!state.is(state.getBlock())) {
            baseState.neighborChanged(level, pos, Blocks.AIR, pos, false);
            base.onPlace(baseState, level, pos, pOldState, false);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if (!state.is(pNewState.getBlock())) {
            baseState.onRemove(level, pos, pNewState, pIsMoving);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity pEntity) {
        base.stepOn(level, pos, state, pEntity);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return base.isRandomlyTicking(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        base.randomTick(state, level, pos, random);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random pRand) {
        base.tick(state, level, pos, pRand);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHit) {
        return baseState.use(level, player, hand, pHit);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion pExplosion) {
        base.wasExploded(level, pos, pExplosion);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState pAdjacentBlockState, Direction pSide) {
        return base.skipRendering(state, pAdjacentBlockState, pSide);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return base.getVisualShape(state, level, pos, context);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return base.getShadeBrightness(state, level, pos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter pReader, BlockPos pos) {
        return base.propagatesSkylightDown(state, pReader, pos);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult pHit, Projectile pProjectile) {
        base.onProjectileHit(level, state, pHit, pProjectile);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return LevelUtil.pickCloneItem(state.getBlock(), player);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(WATER_LEVEL) != 0 ? 0 : baseState.getFireSpreadSpeed(level, pos, direction);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(WATER_LEVEL) != 0 ? 0 : baseState.getFlammability(level, pos, direction);
    }
}
