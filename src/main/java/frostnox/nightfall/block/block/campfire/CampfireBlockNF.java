package frostnox.nightfall.block.block.campfire;

import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.block.*;
import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.data.recipe.CampfireRecipe;
import frostnox.nightfall.data.recipe.SingleRecipe;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.item.item.FireStarterItem;
import frostnox.nightfall.item.item.IgnitableItem;
import frostnox.nightfall.item.item.TorchItem;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Random;

public class CampfireBlockNF extends WaterloggedEntityBlock implements IIgnitable, IHeatSource, IAdjustableNodeType, ITimeSimulatedBlock {
    public static final int MAX_FUEL = 4;
    private static final VoxelShape INTERACTION_SHAPE_HIGH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);
    private static final VoxelShape INTERACTION_SHAPE_LOW = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    private static final VoxelShape INTERACTION_SHAPE_ZERO = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 1.0D, 14.0D);
    private static final VoxelShape COLLISION_SHAPE_Z1 = Block.box(1, 0, 0, 5, 4, 16);
    private static final VoxelShape COLLISION_SHAPE_Z2 = Shapes.or(COLLISION_SHAPE_Z1, Block.box(11, 0, 0, 15, 4, 16));
    private static final VoxelShape COLLISION_SHAPE_Z3 = Shapes.or(COLLISION_SHAPE_Z2, Block.box(0, 3, 11, 16, 7, 16));
    private static final VoxelShape COLLISION_SHAPE_Z4 = Shapes.or(COLLISION_SHAPE_Z3, Block.box(0, 3, 0, 16, 7, 5));
    private static final VoxelShape COLLISION_SHAPE_X1 = Block.box(0, 0, 1, 16, 4, 5);
    private static final VoxelShape COLLISION_SHAPE_X2 = Shapes.or(COLLISION_SHAPE_X1, Block.box(0, 0, 11, 16, 4, 15));
    private static final VoxelShape COLLISION_SHAPE_X3 = Shapes.or(COLLISION_SHAPE_X2, Block.box(0, 3, 0, 5, 7, 16));
    private static final VoxelShape COLLISION_SHAPE_X4 = Shapes.or(COLLISION_SHAPE_X3, Block.box(11, 3, 0, 16, 7, 16));
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty HAS_FOOD = BlockStatePropertiesNF.HAS_FOOD;
    public static final IntegerProperty FIREWOOD = BlockStatePropertiesNF.FIREWOOD;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public CampfireBlockNF(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(HAS_FOOD, false)
                .setValue(FIREWOOD, 4).setValue(AXIS, Direction.Axis.Z));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand pHand, BlockHitResult pHit) {
        if(level.getBlockEntity(pos) instanceof CampfireBlockEntityNF campfire) {
            ItemStack item = player.getItemInHand(pHand);
            if(item.is(ItemsNF.FIREWOOD.get())) {
                int firewood = state.getValue(FIREWOOD);
                if(firewood < MAX_FUEL) {
                    level.playSound(player, pos, SoundsNF.FIREWOOD_PLACE.get(), SoundSource.BLOCKS, 1F, 1F);
                    if(!level.isClientSide) {
                        level.setBlockAndUpdate(pos, state.setValue(FIREWOOD, firewood + 1));
                        if(!player.getAbilities().instabuild) item.shrink(1);
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            else if(item.getItem() instanceof FireStarterItem || item.getItem() instanceof IgnitableItem || item.getItem() instanceof TorchItem) return InteractionResult.PASS;
            else if((campfire.items.stream().noneMatch(ItemStack::isEmpty) ? Optional.empty() : level.getRecipeManager().getRecipeFor(CampfireRecipe.TYPE,
                    new RecipeWrapper(new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, item))), level)).isPresent()) {
                if(!level.isClientSide && campfire.placeFood(player.getAbilities().instabuild ? item.copy() : item)) return InteractionResult.SUCCESS;
                else return InteractionResult.CONSUME;
            }
            else if(campfire.items.stream().anyMatch((food) -> !food.isEmpty())) {
                if(!level.isClientSide) {
                    ItemStack food = campfire.takeFood();
                    if(!food.isEmpty()) {
                        LevelUtil.giveItemToPlayer(food, player, true);
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        spreadHeat(level, pos, getHeat(level, pos, state));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, level, pos, facingPos);
        if(isIgnited(state)) scheduleHeatTick(level, pos, this);
        return state;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity pEntity) {
        if(!pEntity.fireImmune() && state.getValue(LIT) && pEntity instanceof LivingEntity) {
            pEntity.hurt(DamageTypeSource.IN_FIRE, 5F);
        }
        super.entityInside(state, level, pos, pEntity);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return addLiquidToPlacement(defaultBlockState().setValue(AXIS, pContext.getHorizontalDirection().getAxis()), pContext);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        int firewood = state.getValue(FIREWOOD);
        return firewood <= 2 ? (firewood == 0 ? INTERACTION_SHAPE_ZERO : INTERACTION_SHAPE_LOW) : INTERACTION_SHAPE_HIGH;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        if(state.getValue(FIREWOOD) == 0) {
            if(pContext instanceof EntityCollisionContext entityContext && entityContext.getEntity() != null) {
                return Shapes.empty();
            }
            else return INTERACTION_SHAPE_ZERO;
        }
        if(state.getValue(AXIS) == Direction.Axis.Z) {
            return switch(state.getValue(FIREWOOD)) {
                case 1 -> COLLISION_SHAPE_Z1;
                case 2 -> COLLISION_SHAPE_Z2;
                case 3 -> COLLISION_SHAPE_Z3;
                default -> COLLISION_SHAPE_Z4;
            };
        }
        else {
            return switch(state.getValue(FIREWOOD)) {
                case 1 -> COLLISION_SHAPE_X1;
                case 2 -> COLLISION_SHAPE_X2;
                case 3 -> COLLISION_SHAPE_X3;
                default -> COLLISION_SHAPE_X4;
            };
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random pRand) {
        if(state.getValue(LIT)) {
            int x = pos.getX(), y = pos.getY(), z = pos.getZ();
            if(pRand.nextInt(10) == 0) {
                level.playLocalSound(x + 0.5D, y + 0.5D, z + 0.5D, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + pRand.nextFloat(), pRand.nextFloat() * 0.7F + 0.6F, false);
            }
            if(pRand.nextBoolean()) {
                int firewood = state.getValue(FIREWOOD);
                if(state.getValue(AXIS) == Direction.Axis.Z) {
                    spawnFireParticle(level, pRand, x + 1D/16D, y, z, false);
                    if(firewood >= 2) spawnFireParticle(level, pRand, x + 11D/16D, y, z, false);
                    if(firewood >= 3) spawnFireParticle(level, pRand, x, y + 3D/16D, z + 11D/16D, true);
                    if(firewood == 4) spawnFireParticle(level, pRand, x, y + 3D/16D, z + 1D/16D, true);
                }
                else {
                    spawnFireParticle(level, pRand, x, y, z + 1D/16D, true);
                    if(firewood >= 2) spawnFireParticle(level, pRand, x, y, z + 11D/16D, true);
                    if(firewood >= 3) spawnFireParticle(level, pRand, x + 11D/16D, y + 3D/16D, z, false);
                    if(firewood == 4) spawnFireParticle(level, pRand, x + 1D/16D, y + 3D/16D, z, false);
                }
            }
        }
    }

    private static void spawnFireParticle(Level level, Random rand, double xMin, double yMin, double zMin, boolean xAligned) {
        double x = xMin;
        double y = yMin;
        double z = zMin;
        switch(rand.nextInt(3)) {
            case 0 -> {
                x += (xAligned ? 1 : 0.25) * rand.nextDouble();
                y += 0.25;
                z += (xAligned ? 0.25 : 1) * rand.nextDouble();
            }
            case 1 -> {
                if(xAligned) x += rand.nextDouble();
                else z += rand.nextDouble();
                y += 0.25 * rand.nextDouble();
            }
            case 2 -> {
                if(xAligned) {
                    x += rand.nextDouble();
                    z += 0.25;
                }
                else {
                    z += rand.nextDouble();
                    x += 0.25;
                }
                y += 0.25 * rand.nextDouble();
            }
        }
        level.addParticle(ParticleTypesNF.FLAME_RED.get(), x, y, z, 0, 0, 0);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult pHit, Projectile pProjectile) {
        BlockPos blockpos = pHit.getBlockPos();
        if(!level.isClientSide && pProjectile.isOnFire() && pProjectile.mayInteract(level, blockpos) && !state.getValue(LIT) && state.getValue(WATER_LEVEL) == 0) {
            level.setBlock(blockpos, state.setValue(BlockStateProperties.LIT, true), 11);
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
        pBuilder.add(LIT, HAS_FOOD, FIREWOOD, AXIS);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> pBlockEntityType) {
        if(level.isClientSide) return state.getValue(LIT) ? createTickerHelper(pBlockEntityType, BlockEntitiesNF.CAMPFIRE.get(), CampfireBlockEntityNF::particleTick) : null;
        else return state.getValue(LIT) ? createTickerHelper(pBlockEntityType, BlockEntitiesNF.CAMPFIRE.get(), CampfireBlockEntityNF::cookTick) :
                createTickerHelper(pBlockEntityType, BlockEntitiesNF.CAMPFIRE.get(), CampfireBlockEntityNF::cooldownTick);
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        if(super.placeLiquid(level, pos, state, fluid)) {
            if(level.isClientSide()) return true;
            if(state.getValue(LIT)) {
                level.setBlock(pos, level.getBlockState(pos).setValue(LIT, false), 3);
                scheduleHeatTick(level, pos, this);
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.375F, 2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
                ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.25D, (double)pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.0D);
            }
            return true;
        }
        else return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CampfireBlockEntityNF(pos, state);
    }

    @Override
    public boolean tryToIgnite(Level level, BlockPos pos, BlockState state, ItemStack stack, TieredHeat heat) {
        if(level.isClientSide() || state.getValue(FIREWOOD) == 0) return false;
        if(!isIgnited(state)) {
            level.setBlockAndUpdate(pos, state.setValue(LIT, true));
            scheduleHeatTick(level, pos, this);
            return true;
        }
        return false;
    }

    @Override
    public boolean isIgnited(BlockState state) {
        return state.getValue(LIT);
    }

    @Override
    public TieredHeat getHeat(Level level, BlockPos pos, BlockState state) {
        return state.getValue(LIT) ? TieredHeat.RED : TieredHeat.NONE;
    }

    @Override
    public int getRemainingBurnTicks(Level level, BlockPos pos, BlockState state) {
        if(level.getBlockEntity(pos) instanceof CampfireBlockEntityNF campfire) {
            int firewood = state.getValue(CampfireBlockNF.FIREWOOD);
            return (CampfireBlockEntityNF.FIREWOOD_BURN_TICKS - campfire.burnTicks) + (firewood - 1) * CampfireBlockEntityNF.FIREWOOD_BURN_TICKS;
        }
        return 0;
    }

    @Override
    public NodeType adjustNodeType(NodeType type, BlockState state, LivingEntity entity) {
        return (!isIgnited(state) || entity.fireImmune()) ? type : (type.walkable ? NodeType.PASSABLE_DANGER_MINOR : NodeType.IMPASSABLE_DANGER_MINOR);
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && !oldState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).addSimulatableBlock(TickPriority.NORMAL, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!pNewState.is(this)) {
            if(level.getBlockEntity(pos) instanceof CampfireBlockEntityNF campfire) Containers.dropContents(level, pos, campfire.items);
            super.onRemove(state, level, pos, pNewState, pIsMoving);
            spreadHeat(level, pos, TieredHeat.NONE);
            if(LevelData.isPresent(level)) ChunkData.get(level.getChunkAt(pos)).removeSimulatableBlock(TickPriority.NORMAL, pos);
        }
    }


    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        if(level.getBlockEntity(pos) instanceof CampfireBlockEntityNF campfire) {
            int ticks = (elapsedTime > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) elapsedTime);
            boolean changed = false;
            if(state.getValue(LIT)) {
                int firewood = state.getValue(CampfireBlockNF.FIREWOOD);
                int maxBurnTicks = (CampfireBlockEntityNF.FIREWOOD_BURN_TICKS - campfire.burnTicks) + (firewood - 1) * CampfireBlockEntityNF.FIREWOOD_BURN_TICKS;
                int burnTicks = Math.min(ticks, maxBurnTicks);
                campfire.burnTicks += burnTicks;
                if(campfire.burnTicks >= CampfireBlockEntityNF.FIREWOOD_BURN_TICKS) {
                    int newFirewood = firewood - (campfire.burnTicks / CampfireBlockEntityNF.FIREWOOD_BURN_TICKS);
                    campfire.burnTicks %= CampfireBlockEntityNF.FIREWOOD_BURN_TICKS;
                    if(firewood != newFirewood) level.setBlockAndUpdate(pos, state.setValue(CampfireBlockNF.FIREWOOD, newFirewood).setValue(CampfireBlockNF.LIT, newFirewood > 0));
                }
                for(int i = 0; i < campfire.items.size(); i++) {
                    ItemStack item = campfire.items.get(i);
                    if(!item.isEmpty() && !item.is(ItemsNF.BURNT_FOOD.get())) {
                        changed = true;
                        campfire.cookTicks[i] += burnTicks;
                        RecipeWrapper container = new RecipeWrapper(new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, item)));
                        Optional<CampfireRecipe> campfireRecipe = level.getRecipeManager().getRecipeFor(CampfireRecipe.TYPE, container, level);
                        int cookTime = campfireRecipe.map(SingleRecipe::getCookTime).orElse(CampfireBlockEntityNF.COOK_TIME);
                        if(campfire.cookTicks[i] >= cookTime) {
                            ItemStack cookedItem = (campfire.cookTicks[i] - cookTime >= CampfireBlockEntityNF.COOK_TIME) ? new ItemStack(ItemsNF.BURNT_FOOD.get()) :
                                    campfireRecipe.map((recipe) -> recipe.assemble(container)).orElse(new ItemStack(ItemsNF.BURNT_FOOD.get()));
                            campfire.cookTicks[i] = 0;
                            campfire.items.set(i, cookedItem);
                            level.sendBlockUpdated(pos, state, state, 3);
                        }
                    }
                }
            }
            else {
                for(int i = 0; i < campfire.items.size(); i++) {
                    if(campfire.cookTicks[i] > 0) {
                        changed = true;
                        campfire.cookTicks[i] = Math.max(campfire.cookTicks[i] - 2 * ticks, 0);
                    }
                }
                if(campfire.burnTicks > 0) {
                    changed = true;
                    campfire.burnTicks = Math.max(0, campfire.burnTicks - ticks);
                }
            }
            if(changed) campfire.setChanged();
        }
    }
}
