package frostnox.nightfall.block.block.tree;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.IBurnable;
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
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class TreeTrunkBlock extends BaseEntityBlock implements ITimeSimulatedBlock, IBurnable {
    public static final BooleanProperty CHARRED = BlockStatePropertiesNF.CHARRED;
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
        registerDefaultState(defaultBlockState().setValue(CHARRED, false));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(level.getBlockEntity(pos) instanceof TreeTrunkBlockEntity trunk) {
            long timePassed = level.getGameTime() - trunk.lastTick;
            if(timePassed < type.getGrowthIntervalTicks()) return;
            trunk.age += (int) timePassed;
            if(trunk.age >= type.getLifespan()) {
                trunk.removeTree();
                trunk.maxHeight = -1;
                trunk.hasFruited = false;
                trunk.age %= type.getLifespan();
            }
            TreeTrunkBlockEntity.updating = true;
            TreeGenerator.Data d = treeGenerator.grow(level, trunk, trunk.maxHeight == -1);
            if(trunk.isSpecial() && fruitBlock != null && trunk.maxHeight == d.maxHeight) {
                if(Season.get(level) == Season.SUMMER) {
                    if(!trunk.hasFruited) treeGenerator.tryFruit(level, d, trunk);
                }
                else trunk.hasFruited = false;
            }
            trunk.setChanged();
            TreeTrunkBlockEntity.updating = false;
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return !pState.getValue(CHARRED);
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

    public void generateAt(WorldGenLevel level, BlockPos pos, ChunkPos chunkPos, Random random, boolean charred) {
        level.setBlock(pos, defaultBlockState().setValue(CHARRED, charred), 4);
        if(level.getBlockEntity(pos) instanceof TreeTrunkBlockEntity trunk) {
            level.getChunk(chunkPos.x, chunkPos.z).markPosForPostprocessing(pos);
            trunk.initSeed(level, random);
            trunk.age = random.nextInt(type.getLifespan());
            trunk.setChanged();
            TreeGenerator.Data d = treeGenerator.grow(level, trunk, 1 + trunk.age / type.getGrowthIntervalTicks(), true, true, charred);
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
        if(!state.getValue(CHARRED) && elapsedTime != Long.MAX_VALUE && chunk.getBlockEntity(pos) instanceof TreeTrunkBlockEntity trunk) {
            int spacing = type.getGrowthIntervalTicks();
            long timePassed = gameTime - trunk.lastTick;
            if(timePassed > spacing) {
                long trials = elapsedTime - (timePassed > spacing ? 0 : (spacing - timePassed));
                int stages = MathUtil.getRandomSuccesses(randomTickChance, trials,
                        Math.max(treeGenerator.maxLeavesRadius, treeGenerator.maxLength - trunk.maxHeight - 1), spacing, random);
                if(stages > 0) {
                    trunk.age += (int) timePassed;
                    if(trunk.age >= type.getLifespan()) {
                        trunk.removeTree();
                        trunk.maxHeight = -1;
                        trunk.hasFruited = false;
                        trunk.age %= type.getLifespan();
                        stages = 1 + MathUtil.getRandomSuccesses(randomTickChance, trunk.age - spacing, (trunk.age - spacing) / type.getGrowthIntervalTicks(), spacing, random);
                    }
                    TreeTrunkBlockEntity.updating = true;
                    int growTime = Math.min((int) timePassed, trunk.age);
                    long startSeasonTime = seasonTime - growTime;
                    TreeGenerator.Data d = treeGenerator.growNoDrops(level, trunk, stages, seasonTime, trunk.maxHeight == -1);
                    //Simulate fruit
                    if(trunk.isSpecial() && fruitBlock != null && trunk.maxHeight == d.maxHeight) {
                        boolean doFruit = !d.decaying;
                        if(doFruit) {
                            long summerTime = Season.getTimePassedWithin(startSeasonTime, growTime, Season.SUMMER_START, Season.FALL_START);
                            if(MathUtil.getRandomSuccesses(randomTickChance, summerTime, 1, random) >= 1) {
                                if(Season.get(seasonTime) != Season.SUMMER && MathUtil.getRandomSuccesses(randomTickChance,
                                        Season.getTimePassedWithin(startSeasonTime, growTime, Season.FALL_START, seasonTime), 1, random) >= 1) {
                                    doFruit = false;
                                }
                            }
                            else doFruit = false;
                        }
                        if(doFruit) {
                            if(!trunk.hasFruited) treeGenerator.tryFruit(level, d, trunk);
                        }
                        else trunk.hasFruited = false;
                    }
                    TreeTrunkBlockEntity.updating = false;
                    //Not the most accurate way of doing this but don't have a way to simulate remaining trials with spacing
                    trunk.lastTick = gameTime;
                    trunk.setChanged();
                }
            }
        }
    }

    @Override
    public TickPriority getTickPriority() {
        return TickPriority.LOW;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(CHARRED);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(CHARRED) ? 0 : 5;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(CHARRED) ? 0 : 5;
    }

    @Override
    public BlockState getBurnedState(BlockState state) {
        return state.setValue(CHARRED, true);
    }
}
