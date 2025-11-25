package frostnox.nightfall.block.block.eggnest;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.ILevelData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.entity.entity.Diet;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.world.Season;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public abstract class EggNestBlock extends BaseEntityBlock implements ITimeSimulatedBlock, IFoodBlock {
    public static final IntegerProperty EGGS = BlockStatePropertiesNF.EGGS;
    public final Supplier<? extends Item> eggItem;
    public final int hatchDuration;
    public final float minTemp, maxTemp;

    public EggNestBlock(Supplier<? extends Item> eggItem, int hatchDuration, float minTemp, float maxTemp, Properties properties) {
        super(properties);
        this.hatchDuration = hatchDuration;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.eggItem = eggItem;
        registerDefaultState(defaultBlockState().setValue(EGGS, 0));
    }

    protected abstract LivingEntity hatchBaby(ServerLevel level, BlockPos pos, int data, int age);

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return BlockEntitiesNF.EGG_NEST.get().create(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        int eggs = state.getValue(EGGS);
        if(eggs == 0 || pPlayer.isSecondaryUseActive() || pPlayer.getItemInHand(pHand).is(Items.DEBUG_STICK)) return InteractionResult.PASS;
        else {
            level.setBlock(pos, state.setValue(EGGS, eggs - 1), 2);
            if(!level.isClientSide) {
                LevelUtil.giveItemToPlayer(new ItemStack(eggItem.get()), pPlayer, true);
                if(level.getBlockEntity(pos) instanceof EggNestBlockEntity nest) {
                    int bestTime = 0, bestIndex = 0;
                    for(int i = 0; i < nest.hatchTimes.length; i++) {
                        if(nest.hatchTimes[i] < 0) {
                            nest.hatchTimes[i] = 0;
                            return InteractionResult.CONSUME;
                        }
                        else if(nest.hatchTimes[i] > 0) {
                            if(nest.hatchTimes[i] > bestTime) {
                                bestTime = nest.hatchTimes[i];
                                bestIndex = i;
                            }
                        }
                    }
                    nest.hatchTimes[bestIndex] = 0;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(EGGS) > 0;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(level.getBlockEntity(pos) instanceof EggNestBlockEntity nest) {
            int passedTicks = (int) (level.getGameTime() - nest.lastProcessedTick);
            int eggs = state.getValue(EGGS);
            if(LevelData.isPresent(level)) {
                ILevelData levelData = LevelData.get(level);
                IChunkData chunkData = ChunkData.get(level.getChunkAt(pos));
                float temp = levelData.getSeasonalTemperature(chunkData, pos);
                if(temp >= minTemp && temp <= maxTemp) for(int i = 0; i < nest.hatchTimes.length; i++) {
                    if(nest.hatchTimes[i] > 0) {
                        nest.hatchTimes[i] -= passedTicks;
                        if(nest.hatchTimes[i] <= 0) {
                            hatchBaby(level, pos, nest.eggData[i], Math.abs(nest.hatchTimes[i]));
                            nest.hatchTimes[i] = 0;
                            eggs--;
                        }
                    }
                }
                else for(int i = 0; i < nest.hatchTimes.length; i++) {
                    if(nest.hatchTimes[i] > 0) nest.hatchTimes[i] = -1; //Eggs died
                }
            }
            else for(int i = 0; i < nest.hatchTimes.length; i++) {
                if(nest.hatchTimes[i] > 0) {
                    nest.hatchTimes[i] -= passedTicks;
                    if(nest.hatchTimes[i] <= 0) {
                        hatchBaby(level, pos, nest.eggData[i], Math.abs(nest.hatchTimes[i]));
                        nest.hatchTimes[i] = 0;
                        eggs--;
                    }
                }
            }
            nest.lastProcessedTick = level.getGameTime();
            if(eggs != state.getValue(EGGS)) level.setBlock(pos, state.setValue(EGGS, eggs), 3);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, level, pos, facingPos);
        if(!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        else return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportRigidBlock(level, pos.below());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(EGGS);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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
        if(!pNewState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).removeSimulatableBlock(TickPriority.NORMAL, pos);
        }
    }

    @Override
    public boolean isEatable(BlockState state, Diet diet) {
        return state.getValue(EGGS) > 0 && diet != Diet.HERBIVORE;
    }

    @Override
    public void eat(Entity eater, Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        level.setBlock(pos, state.setValue(EGGS, state.getValue(EGGS) - 1), 2);
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        if(level.getBlockEntity(pos) instanceof EggNestBlockEntity nest) {
            int eggs = state.getValue(EGGS);
            elapsedTime = (gameTime - nest.lastProcessedTick) % (Season.YEAR_LENGTH + 1L); //Use time from nest, not chunk
            float temperature = chunkData.getTemperature(pos);
            float seasonalFreezeTemp = minTemp - temperature;
            LongLongPair freezeWindow = Season.getTimesAtTemperatureInfluence(seasonalFreezeTemp);

            long preFreezeTicks = Season.getTimePassedWithin(seasonTime, elapsedTime, (seasonTime - elapsedTime) % Season.YEAR_LENGTH, freezeWindow.firstLong());
            for(int i = 0; i < nest.hatchTimes.length; i++) {
                if(nest.hatchTimes[i] > 0) {
                    int hatchTime = nest.hatchTimes[i];
                    nest.hatchTimes[i] -= preFreezeTicks > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) preFreezeTicks;
                    if(nest.hatchTimes[i] <= 0) {
                        hatchBaby(level, pos, nest.eggData[i], (int) elapsedTime - hatchTime);
                        nest.hatchTimes[i] = 0;
                        eggs--;
                    }
                }
            }
            long freezeTicks = Season.getTimePassedWithin(seasonTime, elapsedTime, freezeWindow.firstLong(), freezeWindow.secondLong());
            if(freezeTicks > 0) {
                for(int i = 0; i < nest.hatchTimes.length; i++) {
                    if(nest.hatchTimes[i] > 0) nest.hatchTimes[i] = -1; //Eggs died
                }
            }
            else {
                long postFreezeTicks = elapsedTime - preFreezeTicks - freezeTicks;
                for(int i = 0; i < nest.hatchTimes.length; i++) {
                    if(nest.hatchTimes[i] > 0) {
                        int hatchTime = nest.hatchTimes[i];
                        nest.hatchTimes[i] -= postFreezeTicks > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) postFreezeTicks;
                        if(nest.hatchTimes[i] <= 0) {
                            hatchBaby(level, pos, nest.eggData[i], (int) elapsedTime - hatchTime);
                            nest.hatchTimes[i] = 0;
                            eggs--;
                        }
                    }
                }
            }

            nest.lastProcessedTick = level.getGameTime();
            if(eggs != state.getValue(EGGS)) level.setBlock(pos, state.setValue(EGGS, eggs), 3);
        }
    }
}
