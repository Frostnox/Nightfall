package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.math.OctalDirection;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class StairBlockNF extends StairBlock implements IWaterloggedBlock, ICustomPathfindable {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;
    protected static final ObjectList<List<AABB>> TOP_FACE_BOTTOM, BOTTOM_FACE_TOP;
    public final BlockState baseState;
    public final Block base;

    static {
        List<List<AABB>> bottom = new ArrayList<>(BOTTOM_SHAPES.length);
        for(VoxelShape shape : BOTTOM_SHAPES) bottom.add(shape.getFaceShape(Direction.UP).toAabbs());
        TOP_FACE_BOTTOM = new ObjectImmutableList<>(bottom);

        List<List<AABB>> top = new ArrayList<>(TOP_SHAPES.length);
        for(VoxelShape shape : TOP_SHAPES) top.add(shape.getFaceShape(Direction.DOWN).toAabbs());
        BOTTOM_FACE_TOP = new ObjectImmutableList<>(top);
    }

    public StairBlockNF(Supplier<? extends Block> baseBlock, Properties properties) {
        super(() -> baseBlock.get().defaultBlockState(), properties);
        this.baseState = baseBlock.get().defaultBlockState();
        this.base = baseBlock.get();
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM)
                .setValue(SHAPE, StairsShape.STRAIGHT).setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    @Override
    public float getExplosionResistance() {
        return explosionResistance;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return addLiquidToPlacement(super.getStateForPlacement(context), context);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        tickLiquid(state, currentPos, level);
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WATER_LEVEL, WATERLOG_TYPE);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getLiquid(state);
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return state.getValue(StairBlockNF.HALF) == Half.TOP ? 0 : 4;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        StairsShape shape = state.getValue(SHAPE);
        return switch(pType) {
            case LAND, AIR -> shape != StairsShape.INNER_LEFT && shape != StairsShape.INNER_RIGHT;
            case WATER -> level.getFluidState(pos).is(FluidTags.WATER);
        };
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        Half half = state.getValue(HALF);
        boolean top = half == Half.TOP;
        float y = top ? pos.getY() : pos.getY() + 0.5F;
        for(OctalDirection gapDirection : getGapDirections(state)) {
            float x = pos.getX() + 0.5F + gapDirection.xStepHalf, z = pos.getZ() + 0.5F + gapDirection.zStepHalf;
            if(top) {
                if(nodeManager.getEntityHeight() <= 0.5F) {
                    nodeManager.getNode(pos).setPartialPath(x, y, z);
                    return NodeType.OPEN_OR_WALKABLE;
                }
                else break;
            }
            else if(!nodeManager.collidesWith(nodeManager.getEntityBox(x, y, z))) {
                nodeManager.getNode(pos).setPartialPath(x, y, z);
                return NodeType.WALKABLE;
            }
        }
        nodeManager.getNode(pos).partial = true;
        return NodeType.CLOSED; //All collision checks failed
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return NodeType.CLOSED;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        if(state.getValue(HALF) == Half.TOP) return FULL_BOXES;
        else return TOP_FACE_BOTTOM.get(SHAPE_BY_STATE[getShapeIndex(state)]);
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        if(state.getValue(HALF) == Half.BOTTOM) return FULL_BOXES;
        else return BOTTOM_FACE_TOP.get(SHAPE_BY_STATE[getShapeIndex(state)]);
    }

    @Override
    public OctalDirection getDirection(BlockState state) {
        StairsShape shape = state.getValue(SHAPE);
        if(shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT) return OctalDirection.CENTER;
        Direction direction = state.getValue(FACING);
        if(shape == StairsShape.STRAIGHT) {
            return switch(direction) {
                case SOUTH -> OctalDirection.SOUTH;
                case NORTH -> OctalDirection.NORTH;
                case EAST -> OctalDirection.EAST;
                default -> OctalDirection.WEST;
            };
        }
        else if(shape == StairsShape.OUTER_RIGHT) {
            return switch(direction) {
                case SOUTH -> OctalDirection.SOUTHWEST;
                case NORTH -> OctalDirection.NORTHEAST;
                case EAST -> OctalDirection.SOUTHEAST;
                default -> OctalDirection.NORTHWEST;
            };
        }
        else {
            return switch(direction) {
                case SOUTH -> OctalDirection.SOUTHEAST;
                case NORTH -> OctalDirection.NORTHWEST;
                case EAST -> OctalDirection.NORTHEAST;
                default -> OctalDirection.SOUTHWEST;
            };
        }
    }

    protected OctalDirection[] getGapDirections(BlockState state) {
        StairsShape shape = state.getValue(SHAPE);
        if(shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT) return OctalDirection.CENTER_SINGLE;
        Direction direction = state.getValue(FACING);
        if(shape == StairsShape.STRAIGHT) {
            return switch(direction) {
                case NORTH -> OctalDirection.SOUTH_SINGLE;
                case SOUTH -> OctalDirection.NORTH_SINGLE;
                case WEST -> OctalDirection.EAST_SINGLE;
                default -> OctalDirection.WEST_SINGLE;
            };
        }
        else if(shape == StairsShape.OUTER_RIGHT) {
            return switch(direction) {
                case NORTH -> OctalDirection.SOUTH_AND_WEST;
                case SOUTH -> OctalDirection.NORTH_AND_EAST;
                case WEST -> OctalDirection.SOUTH_AND_EAST;
                default -> OctalDirection.NORTH_AND_WEST;
            };
        }
        else {
            return switch(direction) {
                case NORTH -> OctalDirection.SOUTH_AND_EAST;
                case SOUTH -> OctalDirection.NORTH_AND_WEST;
                case WEST -> OctalDirection.NORTH_AND_EAST;
                default -> OctalDirection.SOUTH_AND_WEST;
            };
        }
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
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        base.fallOn(level, state, pos, entity, fallDistance);
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