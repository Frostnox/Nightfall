package frostnox.nightfall.block.block.tree;

import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.block.ITree;
import frostnox.nightfall.block.block.CoveredSoilBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.Season;
import frostnox.nightfall.world.generation.tree.TreeGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class TreeTrunkBlock extends BaseEntityBlock implements ITimeSimulatedBlock {
    public final TreeStemBlock stemBlock;
    public final TreeLeavesBlock leavesBlock;
    public final @Nullable TreeBranchesBlock branchesBlock;
    public final @Nullable TreeLeavesBlock fruitBlock;
    public final TreeGenerator treeGenerator;
    public final ITree type;

    public TreeTrunkBlock(TreeStemBlock stemBlock, TreeLeavesBlock leavesBlock, @Nullable TreeBranchesBlock branchesBlock, @Nullable TreeLeavesBlock fruitBlock, TreeGenerator treeGenerator, Properties properties) {
        super(properties);
        this.stemBlock = stemBlock;
        this.leavesBlock = leavesBlock;
        this.branchesBlock = branchesBlock;
        this.fruitBlock = fruitBlock;
        this.treeGenerator = treeGenerator;
        this.type = leavesBlock.type;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(level.getBlockEntity(pos) instanceof TreeTrunkBlockEntity trunk) {
            //if(level.getGameTime() - trunk.lastTick < type.getGrowthIntervalTicks()) return; TODO: temp
            TreeTrunkBlockEntity.updating = true;
            TreeGenerator.Data d = treeGenerator.grow(level, trunk, false);
            if(trunk.isSpecial() && fruitBlock != null && trunk.maxHeight == d.maxHeight) {
                if(Season.get(level) == Season.SUMMER) {
                    if(!trunk.hasFruited) treeGenerator.tryFruit(level, d, trunk);
                }
                else {
                    trunk.hasFruited = false;
                    trunk.setChanged();
                }
            }
            TreeTrunkBlockEntity.updating = false;
        }
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
        //Let trunk update blocks before it is removed
        if(!state.is(pNewState.getBlock()) && level.getBlockEntity(pos) instanceof TreeTrunkBlockEntity trunk) {
            trunk.updateBlocks(pos, null, false);
        }
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).removeSimulatableBlock(TickPriority.LOW, pos);
        }
    }

    public void generateAt(WorldGenLevel level, BlockPos pos, ChunkPos chunkPos, Random random) {
        level.setBlock(pos, defaultBlockState(), 4);
        if(level.getBlockEntity(pos) instanceof TreeTrunkBlockEntity trunk) {
            level.getChunk(chunkPos.x, chunkPos.z).markPosForPostprocessing(pos);
            trunk.initSeed(level, random);
            trunk.setChanged();
            TreeGenerator.Data d = treeGenerator.grow(level, trunk, Integer.MAX_VALUE, true);
            if(trunk.isSpecial() && fruitBlock != null && trunk.maxHeight == d.maxHeight) {
                if(Season.get(level.getLevel()) == Season.SUMMER) {
                    if(!trunk.hasFruited) treeGenerator.tryFruit(level, d, trunk);
                }
                else {
                    trunk.hasFruited = false;
                    trunk.setChanged();
                }
            }
            BlockPos belowPos = pos.below();
            if(level.getBlockState(belowPos).getBlock() instanceof CoveredSoilBlock coveredSoil) {
                level.setBlock(belowPos, coveredSoil.soilBlock.get().defaultBlockState(), 4);
            }

        }
    }

    public void growSaplingAt(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, defaultBlockState());
        if(level.getBlockEntity(pos) instanceof TreeTrunkBlockEntity trunk) {
            trunk.initSeed();
            trunk.setChanged();
            treeGenerator.grow(level, trunk, true);
            BlockPos belowPos = pos.below();
            if(level.getBlockState(belowPos).getBlock() instanceof CoveredSoilBlock coveredSoil) {
                level.setBlock(belowPos, coveredSoil.soilBlock.get().defaultBlockState(), 1 | 2 | 16);
            }
        }
    }

    public boolean isTreeBase(BlockState state) {
        return state.is(stemBlock) || state.is(this);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.TREE_TRUNK.get().create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        if(elapsedTime != Long.MAX_VALUE && chunk.getBlockEntity(pos) instanceof TreeTrunkBlockEntity trunk) {
            int spacing = type.getGrowthIntervalTicks();
            long timePassed = gameTime - trunk.lastTick;
            if(timePassed > spacing) {
                long trials = elapsedTime - (timePassed > spacing ? 0 : (spacing - timePassed));
                int stages = MathUtil.getRandomSuccesses(randomTickChance, trials,
                        Math.max(treeGenerator.maxLeavesRadius, treeGenerator.maxLength - trunk.maxHeight - 1), spacing, random);
                if(stages > 0) {
                    TreeTrunkBlockEntity.updating = true;
                    int tickAdjustedSpacing = spacing + (int) (1 / randomTickChance);
                    long startSeasonTime = seasonTime - elapsedTime;
                    long finalTickSeasonTime = seasonTime - elapsedTime + stages * tickAdjustedSpacing;
                    TreeGenerator.Data d = treeGenerator.grow(level, trunk, stages, finalTickSeasonTime, false);
                    //Simulate fruit
                    if(trunk.isSpecial() && fruitBlock != null && trunk.maxHeight == d.maxHeight) {
                        boolean doFruit = !d.decaying;
                        if(doFruit) {
                            long summerTime = Season.getTimePassedWithin(startSeasonTime, stages * tickAdjustedSpacing, Season.SUMMER_START, Season.FALL_START);
                            if(MathUtil.getRandomSuccesses(randomTickChance, summerTime, 1, random) >= 1) {
                                if(Season.get(finalTickSeasonTime) != Season.SUMMER && MathUtil.getRandomSuccesses(randomTickChance,
                                        Season.getTimePassedWithin(startSeasonTime, stages * tickAdjustedSpacing, Season.FALL_START, seasonTime), 1, random) >= 1) {
                                    doFruit = false;
                                }
                            }
                            else doFruit = false;
                        }
                        if(doFruit) {
                            if(!trunk.hasFruited) treeGenerator.tryFruit(level, d, trunk);
                        }
                        else {
                            trunk.hasFruited = false;
                            trunk.setChanged();
                        }
                    }
                    TreeTrunkBlockEntity.updating = false;
                    //Not the most accurate way of doing this but don't have a way to simulate remaining trials with spacing
                    trunk.lastTick = gameTime / tickAdjustedSpacing * tickAdjustedSpacing;
                }
            }
        }
    }

    @Override
    public TickPriority getTickPriority() {
        return TickPriority.LOW;
    }
}
