package frostnox.nightfall.block.block.cauldron;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IHeatable;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.item.item.ChangeOnUseFinishItem;
import frostnox.nightfall.item.item.FilledBucketItem;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class CauldronBlockNF extends WaterloggedEntityBlock implements IHeatable, ICustomPathfindable {
    private static final VoxelShape SHAPE_Z = Shapes.or(Block.box(3, 0, 3, 13, 8, 13),
            Block.box(6, 8, 7.5, 10, 9, 8.5), Block.box(13, 5, 6, 14, 6, 10),
            Block.box(2, 5, 6, 3, 6, 10));
    private static final VoxelShape SHAPE_X = Shapes.or(Block.box(3, 0, 3, 13, 8, 13),
            Block.box(7.5, 8, 6, 8.5, 9, 10), Block.box(6, 5, 13, 10, 6, 14),
            Block.box(6, 5, 2, 10, 6, 3));
    private static final VoxelShape SHAPE_Z_OPEN = Shapes.or(Block.box(3, 0, 3, 13, 7, 13),
            Block.box(13, 5, 6, 14, 6, 10), Block.box(2, 5, 6, 3, 6, 10));
    private static final VoxelShape SHAPE_X_OPEN = Shapes.or(Block.box(3, 0, 3, 13, 7, 13),
            Block.box(6, 5, 13, 10, 6, 14), Block.box(6, 5, 2, 10, 6, 3));
    private static final VoxelShape COLLISION_SHAPE = Block.box(3, 0, 3, 13, 8, 13);
    private static final VoxelShape COLLISION_SHAPE_OPEN = Block.box(3, 0, 3, 13, 7, 13);
    protected static final List<AABB> AABB = List.of(COLLISION_SHAPE.toAabbs().get(0));
    protected static final List<AABB> AABB_OPEN = List.of(COLLISION_SHAPE_OPEN.toAabbs().get(0));
    public static final EnumProperty<Task> TASK = EnumProperty.create("task", Task.class);
    public static final BooleanProperty SUPPORT = BlockStatePropertiesNF.SUPPORT;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public CauldronBlockNF(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(TASK, Task.IDLE).setValue(SUPPORT, false).setValue(AXIS, Direction.Axis.Z));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(level.isClientSide()) return InteractionResult.SUCCESS;
        else {
            if(level.getBlockEntity(pos) instanceof CauldronBlockEntity cauldron) {
                if(cauldron.hasMeal()) {
                    if(cauldron.meal.getItem() instanceof ChangeOnUseFinishItem item) {
                        ItemStack useItem = player.getItemInHand(hand);
                        if(useItem.is(item.changeItem.get())) {
                            if(!player.getAbilities().instabuild) useItem.shrink(1);
                            LevelUtil.giveItemToPlayer(cauldron.takeMeal(), player, true);
                        }
                        else if(player.canEat(false)) {
                            player.eat(level, cauldron.takeMeal());
                        }
                    }
                    else LevelUtil.giveItemToPlayer(cauldron.takeMeal(), player, true);
                }
                else {
                    //Try placing water directly before opening gui
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    ItemStack item = player.getItemInHand(hand);
                    if(item.getItem() instanceof FilledBucketItem bucket) {
                        cauldron.createMenu(serverPlayer.containerCounter, player.getInventory()).quickMoveStack(player, 27 + player.getInventory().selected);
                        if(item.isEmpty()) bucket.playEmptySound(null, level, pos);
                    }
                    else NetworkHooks.openGui((ServerPlayer) player, cauldron, pos);
                }
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!state.is(pNewState.getBlock()) && level.getBlockEntity(pos) instanceof Container container) {
            Containers.dropContents(level, pos, container);
        }
        super.onRemove(state, level, pos, pNewState, pIsMoving);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @javax.annotation.Nullable LivingEntity pPlacer, ItemStack pStack) {
        if(pStack.hasCustomHoverName() && level.getBlockEntity(pos) instanceof BaseContainerBlockEntity container) {
            container.setCustomName(pStack.getHoverName());
        }
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return addLiquidToPlacement(defaultBlockState().setValue(AXIS, pContext.getHorizontalDirection().getAxis())
                .setValue(SUPPORT, pContext.getLevel().getBlockState(pContext.getClickedPos().below()).is(BlocksNF.CAMPFIRE.get())), pContext);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, level, currentPos, facingPos);
        if(facing == Direction.DOWN) state = state.setValue(SUPPORT, facingState.is(BlocksNF.CAMPFIRE.get()));
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        if(!state.hasProperty(TASK)) return state.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
        else return state.getValue(AXIS) == Direction.Axis.X ? (state.getValue(TASK) == Task.IDLE ? SHAPE_X : SHAPE_X_OPEN) :
                (state.getValue(TASK) == Task.IDLE ? SHAPE_Z : SHAPE_Z_OPEN);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        if(!state.hasProperty(TASK)) return COLLISION_SHAPE;
        else return state.getValue(TASK) == Task.DONE ? COLLISION_SHAPE_OPEN : COLLISION_SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random pRand) {
        if(level.getBlockEntity(pos) instanceof CauldronBlockEntity cauldron) {
            int x = pos.getX(), y = pos.getY(), z = pos.getZ();
            if(cauldron.hot && pRand.nextInt(6) == 0) {
                //TODO: Custom particle?
                level.addParticle(ParticleTypes.SMOKE, x + 0.5, y + 7D/16D, z + 0.5, 0, 0, 0);
                //level.playLocalSound(x + 0.5D, y + 0.5D, z + 0.5D, SoundEvents.CAULDRON_CRACKLE, SoundSource.BLOCKS, 0.5F + pRand.nextFloat(), pRand.nextFloat() * 0.7F + 0.6F, false);
            }
        }
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(TASK, SUPPORT, AXIS);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> pBlockEntityType) {
        if(level.isClientSide) return createTickerHelper(pBlockEntityType, BlockEntitiesNF.CAULDRON.get(), CauldronBlockEntity::clientTick);
        else return switch(state.getValue(TASK)) {
            case IDLE -> createTickerHelper(pBlockEntityType, BlockEntitiesNF.CAULDRON.get(), CauldronBlockEntity::idleTick);
            case COOK -> createTickerHelper(pBlockEntityType, BlockEntitiesNF.CAULDRON.get(), CauldronBlockEntity::cookTick);
            case DONE -> null;
        };
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        if(super.placeLiquid(level, pos, state, fluid)) {
            if(level.isClientSide()) return true;
            if(state.getValue(TASK) == Task.COOK) level.setBlock(pos, level.getBlockState(pos).setValue(TASK, Task.IDLE), 3);
            if(level.getBlockEntity(pos) instanceof CauldronBlockEntity cauldron && cauldron.hot) {
                cauldron.hot = false;
                cauldron.setChanged();
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.375F, 2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
                ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.0D);
            }
            return true;
        }
        else return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CauldronBlockEntity(pos, state);
    }

    @Override
    public void applyHeat(Level level, BlockPos pos, BlockState state, TieredHeat heat, Direction fromDir) {
        if(fromDir == Direction.DOWN) {
            if(level.getBlockEntity(pos) instanceof CauldronBlockEntity cauldron) cauldron.applyHeat(heat != TieredHeat.NONE);
        }
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
        return getTypeForCenteredBottomShape(nodeManager, pos, state.hasProperty(TASK) ? (state.getValue(TASK) == Task.IDLE ? 8F/16F : 7F/16F) : 8F/16F);
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return NodeType.OPEN;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return state.hasProperty(TASK) ? (state.getValue(TASK) == Task.IDLE ? AABB : AABB_OPEN) : AABB;
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return state.hasProperty(TASK) ? (state.getValue(TASK) == Task.IDLE ? AABB : AABB_OPEN) : AABB;
    }
}
