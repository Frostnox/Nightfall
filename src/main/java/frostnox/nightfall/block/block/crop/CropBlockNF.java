package frostnox.nightfall.block.block.crop;

import frostnox.nightfall.block.*;
import frostnox.nightfall.block.block.TilledSoilBlock;
import frostnox.nightfall.block.block.TimeDataBlockEntity;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.ILevelData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.Season;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.PlantType;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public class CropBlockNF extends BushBlock implements EntityBlock, ITimeSimulatedBlock, IFoodBlock, INaturalVegetation {
    public static final IntegerProperty STAGE = BlockStatePropertiesNF.STAGE_8;
    public static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    public final Supplier<? extends Item> seedItem;
    public final Fertility minFertility;
    public final float minHumidity, maxHumidity, minTemp, maxTemp;
    public final int minSunlight, ticksToGrow;
    public final boolean dieAsPlant;

    public CropBlockNF(Fertility minFertility, float minHumidity, float maxHumidity, float minTemp, float maxTemp, int minSunlight, int ticksToGrow, Supplier<? extends Item> seedItem, boolean dieAsPlant, Properties pProperties) {
        super(pProperties);
        this.seedItem = seedItem;
        this.minFertility = minFertility;
        this.minHumidity = minHumidity;
        this.maxHumidity = maxHumidity;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.minSunlight = minSunlight;
        this.ticksToGrow = ticksToGrow;
        this.dieAsPlant = dieAsPlant;
        registerDefaultState(getStateDefinition().any().setValue(STAGE, 1));
    }

    protected boolean checkSoilAndSky(Level level, BlockPos pos, BlockState belowBlock) {
        if(level.getBrightness(LightLayer.SKY, pos) >= minSunlight) {
            if(belowBlock.getBlock() instanceof TilledSoilBlock tilledSoil && !tilledSoil.fertility.poorerThan(minFertility)) {
                float humidity = tilledSoil.getHumidity(belowBlock);
                return humidity >= minHumidity && humidity <= maxHumidity;
            }
            else return true; //Wasn't placed by player, so soil is okay
        }
        return false;
    }

    protected void killCrop(Level level, BlockPos pos, int stage) {
        if(level.getBlockState(pos.below()).getBlock() instanceof TilledSoilBlock) {
            level.setBlock(pos, dieAsPlant ? BlocksNF.DEAD_PLANT.get().defaultBlockState() :
                    BlocksNF.DEAD_CROP.get().defaultBlockState().setValue(STAGE, stage), 3);
        }
        else level.setBlock(pos, defaultBlockState(), 3);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return !pLevel.getBlockState(pCurrentPos.below()).is(TagsNF.SOIL) ? Blocks.AIR.defaultBlockState() : pState;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(TagsNF.SOIL);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos.below()).is(TagsNF.TILLED_SOIL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(level.getBlockEntity(pos) instanceof TimeDataBlockEntity crop) {
            int stage = state.getValue(STAGE);
            if(checkSoilAndSky(level, pos, level.getBlockState(pos.below()))) {
                if(LevelData.isPresent(level)) {
                    ILevelData levelData = LevelData.get(level);
                    IChunkData chunkData = ChunkData.get(level.getChunkAt(pos));
                    float temp = levelData.getSeasonalTemperature(chunkData, pos);
                    if(temp >= minTemp && temp <= maxTemp) {
                        if(level.getGameTime() - crop.lastProcessedTick >= ticksToGrow && stage < 8) {
                            level.setBlock(pos, state.setValue(STAGE, stage + 1), 2);
                            crop.lastProcessedTick = level.getGameTime();
                        }
                        return;
                    }
                }
                else {
                    if(level.getGameTime() - crop.lastProcessedTick >= ticksToGrow && stage < 8) {
                        level.setBlock(pos, state.setValue(STAGE, stage + 1), 2);
                        crop.lastProcessedTick = level.getGameTime();
                    }
                    return;
                }
            }
            killCrop(level, pos, stage);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(STAGE);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(seedItem.get());
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
        if(stage < 8 && elapsedTime > 0) {
            int stages = MathUtil.getRandomSuccesses(randomTickChance, elapsedTime, 8 - stage, ticksToGrow, random);
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
                        killCrop(level, pos, stage);
                    }
                    return;
                }
                float hotSeasonTemp = this.maxTemp - temp;
                //Always too hot
                if(hotSeasonTemp <= -Season.TEMP_INFLUENCE) {
                    if(MathUtil.getRandomSuccesses(randomTickChance, elapsedTime, 1, random) >= 1) {
                        crop.lastProcessedTick = gameTime / tickAdjustedSpacing * tickAdjustedSpacing;
                        killCrop(level, pos, stage);
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
                    //Tilled soil, ordering doesn't matter
                    if(belowBlock.getBlock() instanceof TilledSoilBlock) {
                        if(MathUtil.getRandomSuccesses(randomTickChance, coldTime + hotTime, 1, random) >= 1) {
                            level.setBlock(pos, dieAsPlant ? BlocksNF.DEAD_PLANT.get().defaultBlockState() :
                                    BlocksNF.DEAD_CROP.get().defaultBlockState().setValue(STAGE, stage), 3);
                            return;
                        }
                        int newStage = simulateGrowth(stage, warmTime + coolTime, randomTickChance, random);
                        if(newStage != stage) {
                            crop.lastProcessedTick = gameTime / tickAdjustedSpacing * tickAdjustedSpacing;
                            level.setBlock(pos, state.setValue(STAGE, newStage), 2);
                        }
                    }
                    //Wild has cold -> temp -> hot -> temp...
                    else {
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
                }
                //3 distinct windows
                else if(hasCold || hasHot) {
                    //Tilled soil, ordering doesn't matter
                    if(belowBlock.getBlock() instanceof TilledSoilBlock) {
                        LongLongPair killWindow = Season.getTimesAtTemperatureInfluence(hasCold ? coldSeasonTemp : hotSeasonTemp);
                        long killTime = Season.getTimePassedWithin(seasonTime, elapsedTime, killWindow.firstLong(), killWindow.secondLong());
                        if(MathUtil.getRandomSuccesses(randomTickChance, killTime, 1, random) >= 1) {
                            level.setBlock(pos, dieAsPlant ? BlocksNF.DEAD_PLANT.get().defaultBlockState() :
                                    BlocksNF.DEAD_CROP.get().defaultBlockState().setValue(STAGE, stage), 3);
                            return;
                        }
                        long modSeasonTime = seasonTime % Season.YEAR_LENGTH;
                        long lastSeasonTime = Math.floorMod(modSeasonTime - elapsedTime, Season.YEAR_LENGTH);
                        int newStage = simulateGrowth(stage,
                                Season.getTimePassedWithin(seasonTime, adjustedElapsedTime, lastSeasonTime, killWindow.firstLong()) +
                                        Season.getTimePassedWithin(seasonTime, adjustedElapsedTime, killWindow.secondLong(), modSeasonTime), randomTickChance, random);
                        if(newStage != stage) {
                            crop.lastProcessedTick = gameTime / tickAdjustedSpacing * tickAdjustedSpacing;
                            level.setBlock(pos, state.setValue(STAGE, newStage), 2);
                        }
                    }
                    //Wild has temp -> hot or cold -> temp...
                    else {
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
                killCrop(level, pos, stage);
            }
        }
    }

    @Override
    public TickPriority getTickPriority() {
        return TickPriority.LOW;
    }

    @Override
    public boolean isEatable(BlockState state) {
        return state.getValue(STAGE) == 8;
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
        return defaultBlockState();
    }

    @Override
    public boolean canGrowAt(ServerLevel level, BlockPos pos, ISoil soil, SoilCover cover, int skyLight, float temperature, float humidity) {
        return skyLight >= minSunlight && minFertility.poorerThanOrEqual(soil.getFertility()) && temperature >= minTemp && temperature <= maxTemp
                && humidity >= minHumidity && humidity <= maxHumidity;
    }

    @Override
    public void eat(Entity eater, Level level, BlockPos pos) {
        level.setBlock(pos, level.getBlockState(pos).setValue(STAGE, 1), 2);
    }
}
