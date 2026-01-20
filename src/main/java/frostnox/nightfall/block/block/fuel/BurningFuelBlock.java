package frostnox.nightfall.block.block.fuel;

import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.block.*;
import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class BurningFuelBlock extends BaseEntityBlock implements IHeatSource, IAdjustableNodeType, ITimeSimulatedBlock {
    public static final IntegerProperty HEAT = BlockStatePropertiesNF.HEAT;
    public final @Nullable BiFunction<Level, BlockPos, Block> cookChecker;
    public final int burnTicks;
    public final float burnTemp;
    public final Lazy<BlockState> baseFuel;

    public BurningFuelBlock(int burnTicks, float burnTemp, @Nullable BiFunction<Level, BlockPos, Block> cookChecker, Supplier<? extends Block> baseFuel, Properties properties) {
        super(properties);
        this.burnTicks = burnTicks;
        this.burnTemp = burnTemp;
        this.cookChecker = cookChecker;
        this.baseFuel = Lazy.of(() -> baseFuel.get().defaultBlockState());
        this.registerDefaultState(this.stateDefinition.any().setValue(HEAT, 1));
    }

    public BurningFuelBlock(int burnTicks, float burnTemp, Supplier<? extends Block> baseFuel, Properties properties) {
        this(burnTicks, burnTemp, null, baseFuel, properties);
    }

    public float getTargetTemperature(Level level, BlockState state, BlockPos pos) {
        if(!(level.getBlockEntity(pos) instanceof BurningFuelBlockEntity fuel)) return 0;
        return burnTemp + fuel.structureTempBonus + getRainTempPenalty(level, pos);
    }

    protected BlockState createBurningState(BlockState originalState) {
        return defaultBlockState().setValue(HEAT, TieredHeat.fromTemp(burnTemp).getTier());
    }

    protected void createAt(BlockState originalState, BlockPos pos, Level level) {
        level.setBlockAndUpdate(pos, createBurningState(originalState));
        if(level.getBlockEntity(pos) instanceof BurningFuelBlockEntity fuel && fuel.burnTicks != -1) {
            fuel.temperature = burnTemp;
            fuel.setChanged();
        }
    }

    public static float getRainTempPenalty(Level level, BlockPos pos) {
        return level.isRainingAt(pos.above()) ? -200 : 0;
    }

    public void setHeat(Level level, BlockState state, BlockPos pos, TieredHeat heat) {
        if(heat == TieredHeat.NONE) level.setBlockAndUpdate(pos, BlocksNF.ASH.get().defaultBlockState());
        else {
            level.setBlockAndUpdate(pos, state.setValue(HEAT, heat.getTier()));
            scheduleHeatTick(level, pos, this);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity pEntity) {
        if(!pEntity.fireImmune() && pEntity.invulnerableTime <= 10 && pEntity instanceof LivingEntity && level.getBlockEntity(pos) instanceof BurningFuelBlockEntity fuel) {
            pEntity.hurt(DamageTypeSource.HOT_FLOOR, fuel.temperature / 600F);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        double x = (double)pos.getX() + 0.5D;
        double y = pos.getY();
        double z = (double)pos.getZ() + 0.5D;
        TieredHeat heat = getHeat(level, pos, state);
        if(rand.nextDouble() < 0.15D) {
            level.playLocalSound(x, y, z, SoundsNF.FIRE_CRACKLE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
        if(rand.nextDouble() < 0.9D) {
            double xPos = x + (rand.nextDouble() - 0.5);
            double yPos = y + 1.05D;
            double zPos = z + (rand.nextDouble() - 0.5);
            level.addParticle(heat.getFlameParticle().get(), xPos, yPos, zPos, 0, 0, 0);
            level.addParticle(ParticleTypes.LARGE_SMOKE, xPos, yPos, zPos, 0, 0, 0);
        }
        for(Direction direction : Direction.Plane.HORIZONTAL) {
            if(rand.nextDouble() < 0.85D) {
                double xPos = x + (direction.getStepX() == 0 ? (rand.nextDouble() - 0.5) : direction.getStepX()/1.9F);
                double yPos = y + (rand.nextDouble());
                double zPos = z + (direction.getStepZ() == 0 ? (rand.nextDouble() - 0.5) : direction.getStepZ()/1.9F);
                VoxelShape shape = level.getBlockState(pos.relative(direction)).getCollisionShape(level, pos.relative(direction));
                if(shape.isEmpty() || !shape.bounds().move(pos.relative(direction)).contains(xPos, yPos, zPos)) {
                    level.addParticle(heat.getFlameParticle().get(), xPos, yPos, zPos, 0, 0, 0);
                    level.addParticle(ParticleTypes.SMOKE, xPos, yPos, zPos, 0, 0, 0);
                }
            }
        }
        if(rand.nextDouble() < 0.55D) {
            BlockPos blockpos = pos.above();
            if(level.isRainingAt(blockpos)) {
                level.playLocalSound(x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.175F, 2.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F, false);
                //for(int i = 0; i < 5; i++) level.addParticle(ParticleTypes.SMOKE, x + (rand.nextDouble() - 0.5), y + 0.75D + rand.nextDouble() * 0.25D, z + (rand.nextDouble() - 0.5), 0, 0, 0);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        spreadHeat(level, pos, getHeat(level, pos, state));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        scheduleHeatTick(level, pos, this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HEAT);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        scheduleHeatTick(level, pos, this);
        return super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborChanged(state, level, pos, pBlock, pFromPos, pIsMoving);
        if(pFromPos.getY() >= pos.getY() && state.getValue(HEAT) > 0 && level.getFluidState(pFromPos).is(FluidTags.WATER)) {
            tryToExtinguish(state, pos, level, ItemStack.EMPTY);
        }
    }

    public boolean tryToExtinguish(BlockState state, BlockPos pos, Level level, ItemStack stack) {
        if(level.isClientSide()) return false;
        if(level.getBlockEntity(pos) instanceof BurningFuelBlockEntity fuel) {
            fuel.burnTicks -= burnTicks * 0.1F;
            fuel.setChanged();
            if(fuel.burnTicks <= 0) level.setBlock(pos, BlocksNF.ASH.get().defaultBlockState(), 3);
            else scheduleHeatTick(level, pos, this);
        }
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.375F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.25D, (double)pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.0D);
        return true;
    }

    @Override
    public TieredHeat getHeat(Level level, BlockPos pos, BlockState state) {
        return TieredHeat.fromTier(state.getValue(HEAT));
    }

    @Override
    public int getRemainingBurnTicks(Level level, BlockPos pos, BlockState state) {
        if(level.getBlockEntity(pos) instanceof BurningFuelBlockEntity fuel) return fuel.burnTicks;
        return 0;
    }

    @Override
    public float getTemperature(Level level, BlockPos pos, BlockState state) {
        if(level.getBlockEntity(pos) instanceof BurningFuelBlockEntity fuel) return fuel.temperature;
        else return getHeat(level, pos, state).getBaseTemp();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.FUEL.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return level.isClientSide() ? null : createTickerHelper(blockEntity, BlockEntitiesNF.FUEL.get(), BurningFuelBlockEntity::serverTick);
    }

    @Override
    public NodeType adjustNodeType(NodeType type, BlockState state, LivingEntity entity) {
        return entity.fireImmune() ? type : NodeType.IMPASSABLE_DANGER_MINOR;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return baseFuel.get().getCloneItemStack(target, level, pos, player);
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
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this)) {
            spreadHeat(level, pos, TieredHeat.NONE);
            if(LevelData.isPresent(level)) ChunkData.get(level.getChunkAt(pos)).removeSimulatableBlock(TickPriority.NORMAL, pos);
        }
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        if(level.getBlockEntity(pos) instanceof BurningFuelBlockEntity entity) {
            int ticks = (elapsedTime > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) elapsedTime);
            BurningFuelBlock block = (BurningFuelBlock) state.getBlock();
            if(block.cookChecker != null && block.cookChecker.apply(level, pos) != null) entity.cookTicks += ticks;
            entity.burnTicks -= ticks;
            if(entity.burnTicks <= 0) {
                block.spreadHeat(level, pos, TieredHeat.NONE);
                if(entity.cookTicks >= block.burnTicks * 0.9F && block.cookChecker != null) {
                    Block cookBlock = block.cookChecker.apply(level, pos);
                    if(cookBlock != null) {
                        level.setBlock(pos, cookBlock.defaultBlockState(), 19); //Suppress neighbor updates so new fuel doesn't cook immediately
                    }
                    else level.setBlock(pos, BlocksNF.ASH.get().defaultBlockState(), 3);
                }
                else level.setBlock(pos, BlocksNF.ASH.get().defaultBlockState(), 3);
            }
            else {
                if(LevelUtil.getNearbySmelterTier(level, pos) >= TieredHeat.fromTemp(block.burnTemp).getTier()) entity.structureTempBonus = 200F;
                else entity.structureTempBonus = 0F;

                float oldTemp = entity.temperature;
                float targetTemp = block.getTargetTemperature(level, state, pos);
                if(oldTemp < targetTemp) entity.temperature = Math.min(targetTemp, oldTemp + ticks);
                else if(oldTemp > targetTemp) entity.temperature = Math.max(targetTemp, oldTemp - ticks);
                if(entity.temperature != oldTemp) {
                    TieredHeat heat = TieredHeat.fromTemp(entity.temperature);
                    if(block.getHeat(level, pos, state) != heat) block.setHeat(level, state, pos, heat);
                }
            }
            entity.setChanged();
        }
    }
}
