package frostnox.nightfall.block.block.crucible;

import frostnox.nightfall.block.*;
import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.block.block.fuel.BurningFuelBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.data.recipe.CrucibleRecipe;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.data.WrappedBool;
import frostnox.nightfall.world.inventory.ItemStackHandlerNF;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class CrucibleBlock extends WaterloggedEntityBlock implements ICustomPathfindable, IHeatable, ITimeSimulatedBlock {
    public static final IntegerProperty HEAT = BlockStatePropertiesNF.HEAT_FULL;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape SHAPE_Z = Shapes.join(Shapes.join(box(4D, 0.0D, 4D, 12D, 8.0D, 12D),
                    box(6D, 8D, 6D, 10D, 10.0D, 10D), BooleanOp.OR),
                    box(2, 5, 6, 14, 6, 10), BooleanOp.OR);
    private static final VoxelShape SHAPE_X = MathUtil.rotate(SHAPE_Z, Rotation.CLOCKWISE_90);
    protected static final List<AABB> AABB_TOP_Z = SHAPE_Z.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> AABB_TOP_X = SHAPE_X.getFaceShape(Direction.UP).toAabbs();
    protected static final List<AABB> AABB_BOTTOM_Z = SHAPE_Z.getFaceShape(Direction.DOWN).toAabbs();
    protected static final List<AABB> AABB_BOTTOM_X = SHAPE_X.getFaceShape(Direction.DOWN).toAabbs();
    public final int fluidCapacity;
    public final float maxTemp;

    public CrucibleBlock(int fluidCapacity, float maxTemp, Properties properties) {
        super(properties);
        this.fluidCapacity = fluidCapacity;
        this.maxTemp = maxTemp;
        this.registerDefaultState(this.stateDefinition.any().setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH)
                .setValue(HEAT, 0).setValue(AXIS, Direction.Axis.Z));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        pTooltip.add(new TranslatableComponent("block.capacity", fluidCapacity).withStyle(ChatFormatting.DARK_GREEN));
        pTooltip.add(new TranslatableComponent("block.crucible.pour"));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return state.getValue(AXIS) == Direction.Axis.Z ? SHAPE_Z : SHAPE_X;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HEAT, AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return addLiquidToPlacement(defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis()), context);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(player.isCrouching() && player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if(level.isClientSide()) return InteractionResult.SUCCESS;
        else {
            if(level.getBlockEntity(pos) instanceof CrucibleBlockEntity blockEntity) {
                NetworkHooks.openGui((ServerPlayer) player, blockEntity, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.CRUCIBLE.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entity) {
        if(level.isClientSide()) return null;
        else return createTickerHelper(entity, BlockEntitiesNF.CRUCIBLE.get(), CrucibleBlockEntity::serverTick);
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
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return switch(pType) {
            case LAND, AIR -> true;
            case WATER -> level.getFluidState(pos).is(FluidTags.WATER);
        };
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return getTypeForCenteredBottomShape(nodeManager, pos, 10F/16F);
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return NodeType.OPEN;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return state.getValue(AXIS) == Direction.Axis.Z ? AABB_TOP_Z : AABB_TOP_X;
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return state.getValue(AXIS) == Direction.Axis.Z ? AABB_BOTTOM_Z : AABB_BOTTOM_X;
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

    @Override
    public void applyHeat(Level level, BlockPos pos, BlockState state, TieredHeat heat, Direction fromDir) {
        if(fromDir == Direction.DOWN && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
            crucible.targetTemperature = Math.min(heat.getBaseTemp(), maxTemp);
        }
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && !oldState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).addSimulatableBlock(TickPriority.HIGH, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!pNewState.is(this)) {
            if(level.getBlockEntity(pos) instanceof Container container) {
                Containers.dropContents(level, pos, container);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            if(LevelData.isPresent(level)) ChunkData.get(level.getChunkAt(pos)).removeSimulatableBlock(TickPriority.HIGH, pos);
        }
        super.onRemove(state, level, pos, pNewState, pIsMoving);
    }

    @Override
    public TickPriority getTickPriority() {
        return TickPriority.HIGH;
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        if(level.getBlockEntity(pos) instanceof CrucibleBlockEntity entity) {
            WrappedBool changed = new WrappedBool(false);
            int elapsedTicks = (elapsedTime > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) elapsedTime);
            int burnTicks = elapsedTicks;
            float heatTemp, targetHeatTemp;
            BlockPos heatPos = pos.below();
            BlockState heatBlock = level.getBlockState(heatPos);
            if(heatBlock.getBlock() instanceof IHeatSource heatSource) {
                burnTicks = Math.min(burnTicks, heatSource.getRemainingBurnTicks(level, heatPos, heatBlock));
                heatTemp = heatSource.getTemperature(level, heatPos, state);
                if(heatBlock.getBlock() instanceof BurningFuelBlock fuel) targetHeatTemp = fuel.getTargetTemperature(level, state, heatPos);
                else targetHeatTemp = heatTemp;
            }
            else {
                heatTemp = 0;
                targetHeatTemp = 0;
            }
            float targetTemp = Math.max(entity.targetTemperature, targetHeatTemp);
            if(level.isRainingAt(pos.above())) targetTemp -= 200;

            List<IntIntPair> completedIndexes = new ObjectArrayList<>(CrucibleBlockEntity.ITEM_CAPACITY);
            for(int i = 0; i < CrucibleBlockEntity.ITEM_CAPACITY; i++) {
                ResourceLocation recipeLocation = entity.recipeLocations.get(i);
                if(recipeLocation != null) {
                    CrucibleRecipe recipe = (CrucibleRecipe) level.getRecipeManager().byKey(recipeLocation).orElseThrow();
                    float cookTemp = recipe.getTemperature();
                    float temp = entity.temperature;
                    int ticks = burnTicks;
                    //Fast heat
                    if(temp < heatTemp) {
                        int heatTicks = Math.min(ticks, Mth.ceil((heatTemp - temp) / 2));
                        int cookTicks = Math.max(0, heatTicks - Math.max(0, Mth.ceil(cookTemp - temp) / 2) + 1);
                        if(tickItem(entity, heatTicks, cookTicks, heatTicks - cookTicks, true, recipe, i, changed, completedIndexes)) continue;
                        temp = Math.min(heatTemp, temp + heatTicks * 2);
                        ticks -= heatTicks;
                    }
                    //Slow heat (throttled by heat source increasing slower)
                    if(temp < targetTemp && ticks > 0) {
                        int heatTicks = Math.min(ticks, (int) (targetTemp - temp));
                        int cookTicks = Math.max(0, heatTicks - Math.max(0, Mth.ceil(cookTemp - temp)) + 1);
                        if(tickItem(entity, burnTicks - ticks + heatTicks, cookTicks, heatTicks - cookTicks, true, recipe, i, changed, completedIndexes)) continue;
                        temp = Math.min(targetTemp, temp + heatTicks);
                        ticks -= heatTicks;
                    }
                    //Stable heat
                    if(temp >= cookTemp && ticks > 0) {
                        if(tickItem(entity, burnTicks, ticks, 0, true, recipe, i, changed, completedIndexes)) continue;
                    }
                    //Cool down
                    if(burnTicks < elapsedTicks) {
                        int coolTicks = Math.min(elapsedTicks - burnTicks, (int) (temp));
                        int cookTicks = Math.max(0, Math.min(coolTicks, Mth.ceil(cookTemp - temp)) + 1);
                        tickItem(entity, elapsedTicks, cookTicks, coolTicks - cookTicks, false, recipe, i, changed, completedIndexes);
                    }
                }
            }
            //Sort by completion time
            completedIndexes.sort((p1, p2) -> p1.secondInt() == p2.secondInt() ? Integer.compare(p1.firstInt(), p2.firstInt()) : Integer.compare(p1.secondInt(), p2.secondInt()));
            for(IntIntPair pair : completedIndexes) {
                int i = pair.firstInt();
                RecipeWrapper inventory = new RecipeWrapper(new ItemStackHandlerNF(entity.inventory.getStackInSlot(i)));
                CrucibleRecipe recipe = (CrucibleRecipe) level.getRecipeManager().byKey(entity.recipeLocations.get(i)).orElseThrow();
                FluidStack fluid = recipe.assembleFluid(inventory);
                if(fluid.isEmpty() || entity.getFluidUnits() != entity.getFluidCapacity(entity.getBlockState())) {
                    entity.addFluid(fluid, Integer.MAX_VALUE);
                    entity.setItem(i, recipe.assemble(inventory));
                    entity.tryAlloying();
                }
            }

            float finalTemp = entity.temperature;
            int ticks = burnTicks;
            //Fast heat
            if(finalTemp < heatTemp) {
                int heatTicks = Math.min(ticks, Mth.ceil((heatTemp - finalTemp) / 2));
                finalTemp = Math.min(heatTemp, finalTemp + heatTicks * 2);
                ticks -= heatTicks;
            }
            //Slow heat (throttled by heat source increasing slower)
            if(finalTemp < targetTemp && ticks > 0) {
                int heatTicks = Math.min(ticks, (int) (targetTemp - finalTemp));
                finalTemp = Math.min(targetTemp, finalTemp + heatTicks);
            }
            //Cool down
            if(burnTicks < elapsedTicks) {
                int coolTicks = Math.min(elapsedTicks - burnTicks, (int) (finalTemp));
                finalTemp = Math.max(0, finalTemp - coolTicks);
            }
            if(entity.temperature != finalTemp) {
                entity.temperature = finalTemp;
                TieredHeat heat = TieredHeat.fromTemp(entity.temperature);
                if(state.getValue(CrucibleBlock.HEAT) != heat.getTier()) {
                    level.setBlock(pos, state.setValue(CrucibleBlock.HEAT, heat.getTier()), 2);
                }
                changed.val = true;
            }

            if(changed.val) entity.setChanged();
        }
    }

    private static boolean tickItem(CrucibleBlockEntity entity, int passedTicks, int cookTicks, int coolTicks, boolean coolFirst, CrucibleRecipe recipe, int i, WrappedBool changed, List<IntIntPair> completedIndexes) {
        if(coolFirst) {
            if(coolTicks > 0) {
                int newTicks = entity.cookTicks.getInt(i);
                if(newTicks > 0) {
                    entity.cookTicks.set(i, Math.max(0, newTicks - coolTicks));
                    changed.val = true;
                }
            }
        }
        if(cookTicks > 0) {
            int newTicks = entity.cookTicks.getInt(i) + cookTicks;
            entity.cookTicks.set(i, newTicks);
            changed.val = true;
            if(newTicks >= entity.cookDurations.getInt(i)) {
                RecipeWrapper inventory = new RecipeWrapper(new ItemStackHandlerNF(entity.inventory.getStackInSlot(i)));
                FluidStack fluid = recipe.assembleFluid(inventory);
                if(fluid.isEmpty() || entity.getFluidUnits() != entity.getFluidCapacity(entity.getBlockState())) {
                    completedIndexes.add(IntIntPair.of(i, passedTicks - (newTicks - entity.cookDurations.getInt(i))));
                    return true;
                }
            }
        }
        if(!coolFirst) {
            if(coolTicks > 0) {
                int newTicks = entity.cookTicks.getInt(i);
                if(newTicks > 0) {
                    entity.cookTicks.set(i, Math.max(0, newTicks - coolTicks));
                    changed.val = true;
                }
            }
        }
        return false;
    }
}
