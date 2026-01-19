package frostnox.nightfall.block.block.tree;

import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.Season;
import frostnox.nightfall.world.generation.tree.TreeGenerator;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.PlantType;

import java.util.Random;

public class TreeSeedBlock extends BushBlock implements ITimeSimulatedBlock {
    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    public final TreeTrunkBlock treeBlock;
    public static final VoxelShape SHAPE_0 = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 8.0D, 11.0D);
    public static final VoxelShape SHAPE_1 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 15.0D, 14.0D);

    public TreeSeedBlock(TreeTrunkBlock treeBlock, Properties properties) {
        super(properties);
        this.treeBlock = treeBlock;
        registerDefaultState(defaultBlockState().setValue(STAGE, 0));
    }

    @Override
    public float getDestroyProgress(BlockState state, Player pPlayer, BlockGetter level, BlockPos pos) {
        if(state.getValue(STAGE) == 0) return Float.POSITIVE_INFINITY;
        else return super.getDestroyProgress(state, pPlayer, level, pos);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return treeBlock.treeGenerator.canPlaceOnBlock(level, pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(STAGE) == 0 ? SHAPE_0 : SHAPE_1;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(STAGE);
    }

    @Override
    public PlantType getPlantType(BlockGetter world, BlockPos pos) {
        return null;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(random.nextBoolean()) {
            if(treeBlock.treeGenerator.canGrowAt(level, pos)) {
                if(state.getValue(STAGE) == 1) treeBlock.growSaplingAt(level, pos);
                else level.setBlock(pos, state.setValue(STAGE, 1), 3);
            }
            else {
                level.setBlock(pos, BlocksNF.DEAD_BUSH.get().defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        int successes = 2 - state.getValue(STAGE);
        IntLongPair result = MathUtil.getRandomSuccessesAndRemainingTrials(randomTickChance / 2, elapsedTime, successes, random);
        if(result.firstInt() >= 1) {
            if(!treeBlock.treeGenerator.canGrowAt(level, pos)) {
                level.setBlock(pos, BlocksNF.DEAD_BUSH.get().defaultBlockState(), 3);
                return;
            }
        }
        if(successes == 2 && result.firstInt() == 1) {
            level.setBlock(pos, state.setValue(STAGE, 1), 3);
        }
        else if(result.firstInt() >= successes) {
            level.setBlockAndUpdate(pos, treeBlock.defaultBlockState());
            if(chunk.getBlockEntity(pos) instanceof TreeTrunkBlockEntity trunk) {
                trunk.initSeed();
                treeBlock.treeGenerator.growNoDrops(level, trunk, 1, seasonTime, true);
                int spacing = treeBlock.type.getGrowthIntervalTicks();
                long trials = result.secondLong() - spacing;
                int stages = MathUtil.getRandomSuccesses(randomTickChance, trials,
                        treeBlock.treeGenerator.maxLength - trunk.maxHeight - 1, spacing, random);
                TreeTrunkBlockEntity.updating = true;
                if(stages > 1) {
                    trunk.age += (int) trials;
                    if(trunk.age >= treeBlock.type.getLifespan()) {
                        trunk.removeTree();
                        trunk.maxHeight = -1;
                        trunk.hasFruited = false;
                        trunk.age %= treeBlock.type.getLifespan();
                        stages = 1 + MathUtil.getRandomSuccesses(randomTickChance, trunk.age - spacing, (trunk.age - spacing) / treeBlock.type.getGrowthIntervalTicks(), spacing, random);
                    }
                    TreeGenerator.Data d = treeBlock.treeGenerator.growNoDrops(level, trunk, stages - 1, seasonTime, trunk.maxHeight == -1);
                    int growTime = Math.min((int) trials, trunk.age);
                    long startSeasonTime = seasonTime - growTime;
                    if(trunk.isSpecial() && treeBlock.fruitBlock != null && trunk.maxHeight == d.maxHeight) {
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
                            if(!trunk.hasFruited) treeBlock.treeGenerator.tryFruit(level, d, trunk);
                        }
                        else trunk.hasFruited = false;
                    }
                }
                TreeTrunkBlockEntity.updating = false;
                //Not the most accurate way of doing this but don't have a way to simulate remaining trials with spacing
                trunk.lastTick = gameTime;
                trunk.setChanged();
            }
        }
    }
}
