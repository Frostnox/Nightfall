package frostnox.nightfall.block.block;

import frostnox.nightfall.block.*;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.ILevelData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.data.Vec2f;
import frostnox.nightfall.world.Season;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public class FruitBushBlock extends BushBlock implements EntityBlock, ITimeSimulatedBlock, IFoodBlock, INaturalVegetation {
    public static final IntegerProperty STAGE = BlockStatePropertiesNF.STAGE_4;
    private static final VoxelShape SAPLING_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 10.0D, 13.0D);
    private static final VoxelShape FULL_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
    public final Supplier<? extends Item> fruitItem;
    public final float minHumidity, maxHumidity, minTemp, maxTemp;
    public final int minSunlight, ticksToGrow;

    public FruitBushBlock(float minHumidity, float maxHumidity, float minTemp, float maxTemp, int minSunlight, int ticksToGrow, Supplier<? extends Item> fruitItem, Properties pProperties) {
        super(pProperties);
        this.fruitItem = fruitItem;
        this.minHumidity = minHumidity;
        this.maxHumidity = maxHumidity;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.minSunlight = minSunlight;
        this.ticksToGrow = ticksToGrow;
        registerDefaultState(getStateDefinition().any().setValue(STAGE, 2));
    }

    protected boolean checkSoilAndSky(Level level, BlockPos pos, BlockState belowState) {
        if(level.getBrightness(LightLayer.SKY, pos) >= minSunlight) return belowState.is(TagsNF.TILLABLE_SOIL);
        else return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        int stage = state.getValue(STAGE);
        if(stage < 3 || pPlayer.getItemInHand(pHand).is(Items.DEBUG_STICK)) return InteractionResult.PASS;
        else {
            if(level instanceof ServerLevel serverLevel) {
                getDrops(state, serverLevel, pos, null, pPlayer, ItemStack.EMPTY).forEach((drop) -> {
                    if(drop.is(fruitItem.get())) LevelUtil.giveItemToPlayer(drop, pPlayer, true);
                });
            }
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1F, 0.9F + level.random.nextFloat() * 0.2F);
            level.setBlock(pos, state.setValue(STAGE, 2), 2);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(TagsNF.TILLABLE_SOIL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return state.getValue(STAGE) == 1 ? SAPLING_SHAPE : FULL_SHAPE;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        int stage = state.getValue(STAGE);
        if(checkSoilAndSky(level, pos, level.getBlockState(pos.below())) && level.getBlockEntity(pos) instanceof TimeDataBlockEntity crop) {
            if(LevelData.isPresent(level)) {
                ILevelData levelData = LevelData.get(level);
                IChunkData chunkData = ChunkData.get(level.getChunkAt(pos));
                float temp = levelData.getSeasonalTemperature(chunkData, pos);
                if(temp >= minTemp && temp <= maxTemp) {
                    float humidity = chunkData.getHumidity(pos);
                    if(humidity >= minHumidity && humidity <= maxHumidity) {
                        if(level.getGameTime() - crop.lastProcessedTick >= ticksToGrow && stage < 4) {
                            level.setBlock(pos, state.setValue(STAGE, stage + 1), 2);
                            crop.lastProcessedTick = level.getGameTime();
                        }
                        return;
                    }
                }
            }
            else {
                if(level.getGameTime() - crop.lastProcessedTick >= ticksToGrow && stage < 4) {
                    level.setBlock(pos, state.setValue(STAGE, stage + 1), 2);
                    crop.lastProcessedTick = level.getGameTime();
                }
                return;
            }
        }
        level.setBlock(pos, defaultBlockState().setValue(STAGE, 1), 3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(STAGE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.TIME_DATA.get().create(pos, state);
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && !oldState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).addSimulatableBlock(TickPriority.LOW, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).removeSimulatableBlock(TickPriority.LOW, pos);
        }
    }

    protected int simulateGrowth(int stage, long elapsedTime, double randomTickChance, Random random) {
        if(stage < 4 && elapsedTime > 0) {
            int stages = MathUtil.getRandomSuccesses(randomTickChance, elapsedTime, 4 - stage, ticksToGrow, random);
            if(stages > 0) return stage + stages;
        }
        return stage;
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        if(chunk.getBlockEntity(pos) instanceof TimeDataBlockEntity crop) {
            int stage = state.getValue(STAGE);
            BlockState belowBlock = chunk.getBlockState(pos.below());
            if(checkSoilAndSky(level, pos, belowBlock)) {
                int tickAdjustedSpacing = ticksToGrow + (int) (1 / randomTickChance);
                float temp = chunkData.getTemperature(pos);
                //Could be too hot or too cold or both
                float coldSeasonTemp = this.minTemp - temp;
                //Always too cold
                if(coldSeasonTemp >= Season.TEMP_INFLUENCE) {
                    if(MathUtil.getRandomSuccesses(randomTickChance, elapsedTime, 1, random) >= 1) {
                        crop.lastProcessedTick = gameTime / tickAdjustedSpacing * tickAdjustedSpacing;
                        level.setBlock(pos, defaultBlockState().setValue(STAGE, 1), 3);
                    }
                    return;
                }
                float hotSeasonTemp = this.maxTemp - temp;
                //Always too hot
                if(hotSeasonTemp <= -Season.TEMP_INFLUENCE) {
                    if(MathUtil.getRandomSuccesses(randomTickChance, elapsedTime, 1, random) >= 1) {
                        crop.lastProcessedTick = gameTime / tickAdjustedSpacing * tickAdjustedSpacing;
                        level.setBlock(pos, defaultBlockState().setValue(STAGE, 1), 3);
                    }
                    return;
                }
                boolean hasCold = Math.abs(coldSeasonTemp) < Season.TEMP_INFLUENCE;
                boolean hasHot = Math.abs(hotSeasonTemp) < Season.TEMP_INFLUENCE;
                //Only simulate growth for up to 1 year
                if(elapsedTime > Season.YEAR_LENGTH) elapsedTime = Season.YEAR_LENGTH;
                //Adjust for initial spacing
                long adjustedElapsedTime = elapsedTime - Math.max(0, ticksToGrow - Math.max(0, (gameTime - elapsedTime - crop.lastProcessedTick)));
                //4 distinct windows
                if(hasCold && hasHot) {
                    LongLongPair coldWindow = Season.getTimesAtTemperatureInfluence(coldSeasonTemp);
                    LongLongPair hotWindow = Season.getTimesAtTemperatureInfluence(hotSeasonTemp);
                    long coldTime = Season.getTimePassedWithin(seasonTime, elapsedTime, coldWindow.firstLong(), coldWindow.secondLong());
                    long hotTime = Season.getTimePassedWithin(seasonTime, elapsedTime, hotWindow.firstLong(), hotWindow.secondLong());
                    long warmTime = Season.getTimePassedWithin(seasonTime, adjustedElapsedTime, coldWindow.secondLong(), hotWindow.firstLong());
                    long coolTime = Season.getTimePassedWithin(seasonTime, adjustedElapsedTime, hotWindow.secondLong(), coldWindow.firstLong());
                    //cold -> temp -> hot -> temp...
                    //Order of windows depends on where season started
                    long modSeasonTime = seasonTime % Season.YEAR_LENGTH;
                    long lastSeasonTime = Math.floorMod(modSeasonTime - elapsedTime, Season.YEAR_LENGTH);
                    float lastSeasonalTemp = Season.getTemperatureInfluence(lastSeasonTime);
                    int newStage = stage;
                    //Cold window first
                    if(lastSeasonalTemp < coldSeasonTemp) {
                        if(MathUtil.getRandomSuccesses(randomTickChance, coldTime, 1, random) >= 1) newStage = 1;
                        newStage = simulateGrowth(newStage, warmTime, randomTickChance, random);
                        if(MathUtil.getRandomSuccesses(randomTickChance, hotTime, 1, random) >= 1) newStage = 1;
                        newStage = simulateGrowth(newStage, coolTime, randomTickChance, random);
                    }
                    //Hot window first
                    else if(lastSeasonalTemp > hotSeasonTemp) {
                        if(MathUtil.getRandomSuccesses(randomTickChance, hotTime, 1, random) >= 1) newStage = 1;
                        newStage = simulateGrowth(newStage, coolTime, randomTickChance, random);
                        if(MathUtil.getRandomSuccesses(randomTickChance, coldTime, 1, random) >= 1) newStage = 1;
                        newStage = simulateGrowth(newStage, warmTime, randomTickChance, random);
                    }
                    //Moving towards hot
                    else if(lastSeasonTime > Math.floorMod(coldWindow.secondLong(), Season.YEAR_LENGTH) && lastSeasonTime < Math.floorMod(hotWindow.firstLong(), Season.YEAR_LENGTH)) {
                        newStage = simulateGrowth(newStage, warmTime, randomTickChance, random);
                        if(MathUtil.getRandomSuccesses(randomTickChance, hotTime, 1, random) >= 1) newStage = 1;
                        newStage = simulateGrowth(newStage, coolTime, randomTickChance, random);
                        if(MathUtil.getRandomSuccesses(randomTickChance, coldTime, 1, random) >= 1) newStage = 1;
                    }
                    //Moving towards cold
                    else {
                        newStage = simulateGrowth(newStage, coolTime, randomTickChance, random);
                        if(MathUtil.getRandomSuccesses(randomTickChance, coldTime, 1, random) >= 1) newStage = 1;
                        newStage = simulateGrowth(newStage, warmTime, randomTickChance, random);
                        if(MathUtil.getRandomSuccesses(randomTickChance, hotTime, 1, random) >= 1) newStage = 1;
                    }
                    if(newStage != stage) {
                        crop.lastProcessedTick = gameTime / tickAdjustedSpacing * tickAdjustedSpacing;
                        level.setBlock(pos, state.setValue(STAGE, newStage), 2);
                    }
                }
                //3 distinct windows
                else if(hasCold || hasHot) {
                    //temp -> hot or cold -> temp...
                    long modSeasonTime = seasonTime % Season.YEAR_LENGTH;
                    long lastSeasonTime = Math.floorMod(modSeasonTime - elapsedTime, Season.YEAR_LENGTH);
                    float lastSeasonalTemp = Season.getTemperatureInfluence(lastSeasonTime);
                    int newStage = stage;
                    if(hasCold) {
                        LongLongPair coldWindow = Season.getTimesAtTemperatureInfluence(coldSeasonTemp);
                        long coldTime = Season.getTimePassedWithin(seasonTime, elapsedTime, coldWindow.firstLong(), coldWindow.secondLong());
                        //cold -> warm & cool
                        if(lastSeasonalTemp < coldSeasonTemp) {
                            if(MathUtil.getRandomSuccesses(randomTickChance, coldTime, 1, random) >= 1) newStage = 1;
                            newStage = simulateGrowth(newStage, Season.getTimePassedWithin(seasonTime, adjustedElapsedTime, coldWindow.secondLong(), modSeasonTime), randomTickChance, random);
                        }
                        //cool -> cold -> warm
                        else if(lastSeasonTime < Math.floorMod(coldWindow.firstLong(), Season.YEAR_LENGTH)) {
                            newStage = simulateGrowth(newStage, Season.getTimePassedWithin(seasonTime, adjustedElapsedTime, lastSeasonTime, coldWindow.firstLong()), randomTickChance, random);
                            if(MathUtil.getRandomSuccesses(randomTickChance, coldTime, 1, random) >= 1) newStage = 1;
                            newStage = simulateGrowth(newStage, Season.getTimePassedWithin(seasonTime, adjustedElapsedTime, coldWindow.secondLong(), modSeasonTime), randomTickChance, random);
                        }
                        //warm & cool -> cold
                        else {
                            newStage = simulateGrowth(newStage, Season.getTimePassedWithin(seasonTime, adjustedElapsedTime, lastSeasonTime, coldWindow.firstLong()), randomTickChance, random);
                            if(MathUtil.getRandomSuccesses(randomTickChance, coldTime, 1, random) >= 1) newStage = 1;
                        }
                    }
                    else {
                        LongLongPair hotWindow = Season.getTimesAtTemperatureInfluence(hotSeasonTemp);
                        long hotTime = Season.getTimePassedWithin(seasonTime, elapsedTime, hotWindow.firstLong(), hotWindow.secondLong());
                        //hot -> cool & warm
                        if(lastSeasonalTemp > hotSeasonTemp) {
                            if(MathUtil.getRandomSuccesses(randomTickChance, hotTime, 1, random) >= 1) newStage = 1;
                            newStage = simulateGrowth(newStage, Season.getTimePassedWithin(seasonTime, adjustedElapsedTime, hotWindow.secondLong(), modSeasonTime), randomTickChance, random);
                        }
                        //warm -> hot -> cool
                        else if(lastSeasonTime < Math.floorMod(hotWindow.firstLong(), Season.YEAR_LENGTH)) {
                            newStage = simulateGrowth(newStage, Season.getTimePassedWithin(seasonTime, adjustedElapsedTime, lastSeasonTime, hotWindow.firstLong()), randomTickChance, random);
                            if(MathUtil.getRandomSuccesses(randomTickChance, hotTime, 1, random) >= 1) newStage = 1;
                            newStage = simulateGrowth(newStage, Season.getTimePassedWithin(seasonTime, adjustedElapsedTime, hotWindow.secondLong(), modSeasonTime), randomTickChance, random);
                        }
                        //cool & warm -> hot
                        else {
                            newStage = simulateGrowth(newStage, Season.getTimePassedWithin(seasonTime, adjustedElapsedTime, lastSeasonTime, hotWindow.firstLong()), randomTickChance, random);
                            if(MathUtil.getRandomSuccesses(randomTickChance, hotTime, 1, random) >= 1) newStage = 1;
                        }
                    }
                    if(newStage != stage) {
                        crop.lastProcessedTick = gameTime / tickAdjustedSpacing * tickAdjustedSpacing;
                        level.setBlock(pos, state.setValue(STAGE, newStage), 2);
                    }
                }
                //1 distinct window
                else {
                    int newStage = simulateGrowth(stage, adjustedElapsedTime, randomTickChance, random);
                    if(newStage != stage) {
                        crop.lastProcessedTick = gameTime / tickAdjustedSpacing * tickAdjustedSpacing;
                        level.setBlock(pos, state.setValue(STAGE, newStage), 2);
                    }
                }
            }
            //Crop died due to poor soil or light
            else if(MathUtil.getRandomSuccesses(randomTickChance, elapsedTime, 1, random) >= 1) {
                level.setBlock(pos, defaultBlockState().setValue(STAGE, 1), 3);
            }
        }
    }

    @Override
    public TickPriority getTickPriority() {
        return TickPriority.LOW;
    }

    @Override
    public boolean isEatable(BlockState state) {
        return state.getValue(STAGE) >= 3;
    }

    @Override
    public void eat(Entity eater, Level level, BlockPos pos) {
        level.setBlock(pos, level.getBlockState(pos).setValue(STAGE, 2), 2);
    }

    @Override
    public PlantType getPlantType(BlockGetter world, BlockPos pos) {
        return null;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 100;
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Override
    public BlockState getGrowthBlock() {
        return defaultBlockState().setValue(STAGE, 1);
    }

    @Override
    public boolean canGrowAt(ServerLevel level, BlockPos pos, ISoil soil, SoilCover cover, int skyLight, float temperature, float humidity) {
        return skyLight >= minSunlight && temperature >= minTemp && temperature <= maxTemp && humidity >= minHumidity && humidity <= maxHumidity;
    }
}
