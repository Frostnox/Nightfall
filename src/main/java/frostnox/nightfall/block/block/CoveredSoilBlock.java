package frostnox.nightfall.block.block;

import frostnox.nightfall.block.INaturalVegetation;
import frostnox.nightfall.block.ISoil;
import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.block.SoilCover;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.ILevelData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.world.UpdateBlockToClient;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.Season;
import frostnox.nightfall.world.ToolActionsNF;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.RegistryObject;

import java.util.Random;

public class CoveredSoilBlock extends BlockNF implements ITimeSimulatedBlock {
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;
    public final RegistryObject<? extends SoilBlock> soilBlock;
    public final RegistryObject<? extends TilledSoilBlock> tilledSoilBlock;
    public final ISoil soil;
    public final SoilCover soilCover;

    public CoveredSoilBlock(RegistryObject<? extends SoilBlock> soilBlock, RegistryObject<? extends TilledSoilBlock> tilledSoilBlock, ISoil soil, SoilCover soilCover, Properties properties) {
        super(properties);
        this.soilBlock = soilBlock;
        this.tilledSoilBlock = tilledSoilBlock;
        this.soil = soil;
        this.soilCover = soilCover;
        registerDefaultState(stateDefinition.any().setValue(SNOWY, Boolean.FALSE));
    }

    private static boolean isSnowySetting(BlockState state) {
        return state.is(BlockTags.SNOW);
    }

    private BlockState growVegetation(ServerLevel level, IChunkData chunkData, BlockPos pos, int skyLight) {
        ILevelData levelData = LevelData.get(level);
        float temperature = chunkData.getTemperature(pos), humidity = chunkData.getHumidity(pos);
        int density = 2;
        int range = Math.max(1, Math.round(5 - (humidity * 3 + 2 - Math.abs(temperature - 0.5F) * 2) + levelData.getVegetationNoise(pos.getX(), pos.getZ()) * 2.5F));
        int rangeSqr = range * range;
        int totalVegetation = 0;
        ObjectSet<INaturalVegetation> nearbyVegetation = new ObjectArraySet<>(density);
        //Search for nearby vegetation
        for(BlockPos p : BlockPos.betweenClosed(pos.offset(-range, 0, -range), pos.offset(range, 0, range))) {
            int xDist = p.getX() - pos.getX(), zDist = p.getZ() - pos.getZ();
            if(xDist * xDist + zDist * zDist > rangeSqr) continue;
            ChunkAccess chunk = level.getChunk(SectionPos.blockToSectionCoord(p.getX()), SectionPos.blockToSectionCoord(p.getZ()), ChunkStatus.FULL, false);
            if(chunk == null) continue;
            int i = 0;
            BlockPos.MutableBlockPos searchPos = (BlockPos.MutableBlockPos) p;
            while(!level.isOutsideBuildHeight(searchPos) && Math.abs(i) <= range) {
                BlockState searchBlock = chunk.getBlockState(searchPos);
                if(searchBlock.isAir()) {
                    if(i <= 0) i--;
                    else break;
                }
                else if(searchBlock.getBlock() instanceof INaturalVegetation vegetation) {
                    nearbyVegetation.add(vegetation);
                    totalVegetation++;
                    if(totalVegetation >= density) return null;
                    else break;
                }
                else {
                    if(i >= 0) i++;
                    else break;
                }
                searchPos.setY(pos.getY() + i);
            }
        }
        //Determine possible vegetation
        int totalWeight = 0;
        Object2IntMap<INaturalVegetation> possibleVegetation = new Object2IntArrayMap<>();
        for(INaturalVegetation vegetation : RegistriesNF.getNaturalVegetation()) {
            if(vegetation.canGrowAt(level, pos, soil, soilCover, skyLight, temperature, humidity)) {
                int weight = vegetation.getWeight();
                possibleVegetation.put(vegetation, weight);
                totalWeight += weight;
            }
        }
        int nearbyModifier = totalWeight * 4;
        for(INaturalVegetation vegetation : possibleVegetation.keySet()) {
            if(nearbyVegetation.contains(vegetation) && vegetation.hasClusteredGrowth()) {
                possibleVegetation.put(vegetation, possibleVegetation.getInt(vegetation) + nearbyModifier);
                totalWeight += nearbyModifier;
            }
        }
        //Pick and grow new vegetation
        if(totalWeight > 0) {
            INaturalVegetation vegetation = null;
            int weight = 0;
            int rand = level.random.nextInt(totalWeight);
            for(var entry : possibleVegetation.object2IntEntrySet()) {
                weight += entry.getIntValue();
                if(rand < weight) {
                    vegetation = entry.getKey();
                    break;
                }
            }
            BlockState newBlock = vegetation.getGrowthBlock();
            level.setBlock(pos, newBlock, 1 | 2 | 16);
            return newBlock;
        }
        return null;
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if(toolAction == ToolActionsNF.TILL) {
            if(context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
                return tilledSoilBlock.get().defaultBlockState();
            }
        }
        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.UP ? state.setValue(SNOWY, isSnowySetting(facingState)) : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos().above());
        return defaultBlockState().setValue(SNOWY, isSnowySetting(blockstate));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(SNOWY);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        BlockPos abovePos = pos.above();
        int skyLight = level.getBrightness(LightLayer.SKY, abovePos);
        if(!soilCover.canGrow(skyLight)) level.setBlockAndUpdate(pos, soilBlock.get().defaultBlockState());
        else {
            if(level.getBlockState(abovePos).isAir() && random.nextInt(100) == 0 && Season.get(level) != Season.WINTER) {
                growVegetation(level, ChunkData.get(level.getChunkAt(abovePos)), abovePos, skyLight);
            }
            else if(soilCover == SoilCover.GRASS && random.nextInt(128) == 0) NetworkHandler.toAllTrackingChunk(level.getChunkAt(pos), new UpdateBlockToClient(pos));
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return soilBlock.get().defaultBlockState().getCloneItemStack(target, level, pos, player);
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
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        BlockPos abovePos = pos.above();
        int skyLight = level.getBrightness(LightLayer.SKY, abovePos);
        if(!soilCover.canGrow(skyLight)) {
            if(MathUtil.getRandomSuccesses(randomTickChance, elapsedTime, 1, random) >= 1) {
                level.setBlockAndUpdate(pos, soilBlock.get().defaultBlockState());
            }
        }
        else if(level.getBlockState(abovePos).isAir()) {
            long validTime = Season.getTimePassedWithin(seasonTime, elapsedTime, Season.SPRING_START, Season.WINTER_START);
            IntLongPair successesAndTrials = MathUtil.getRandomSuccessesAndRemainingTrials(randomTickChance / 100, validTime, 1, random);
            if(successesAndTrials.firstInt() >= 1) {
                BlockState vegetation = growVegetation(level, chunkData, abovePos, skyLight);
                if(vegetation != null && vegetation.getBlock() instanceof ITimeSimulatedBlock simulatable) {
                    simulatable.simulateTime(level, chunk, chunkData, abovePos, vegetation, successesAndTrials.secondLong(),
                            gameTime, dayTime, seasonTime, seasonalTemp, randomTickChance, random);
                }
            }
        }
    }
}
