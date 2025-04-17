package frostnox.nightfall.world.generation.tree;

import frostnox.nightfall.block.block.tree.TreeStemBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class CurvedTreeGenerator extends TreeGenerator {
    protected final double curveChance;
    protected final boolean doubleCurve;

    protected CurvedTreeGenerator(int baseHeight, int randHeight, int baseBranchLength, int randBranchLength, int leavesRadius, double curveChance, boolean doubleCurve) {
        super(baseHeight, randHeight, baseBranchLength, randBranchLength, leavesRadius);
        this.curveChance = curveChance;
        this.doubleCurve = doubleCurve;
    }

    @Override
    protected int getMaxDistXZ() {
        return super.getMaxDistXZ() + (doubleCurve ? 2 : 1);
    }

    @Override
    protected void tickTrunk(Data d, Random random, int maxHeightReached) {
        int curve1 = getFirstCurveHeight(d.maxHeight, random);
        int curve2 = getSecondCurveHeight(curve1, d.maxHeight, random);
        Direction curveDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction.Axis axis = curveDirection.getAxis();
        boolean positive = curveDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        if(axis == Direction.Axis.Z) positive = !positive;
        boolean hasCurve1 = random.nextDouble() <= curveChance;
        boolean hasCurve2 = doubleCurve ? random.nextDouble() <= curveChance : false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(d.trunkPos.getX(), d.trunkPos.getY(), d.trunkPos.getZ());
        BlockState lastState = null;
        for(int i = 0; i < (d.simulateDetection ? maxHeightReached : d.maxHeight); i++) {
            boolean curve = (hasCurve1 && i == curve1) || (hasCurve2 && i == curve2);
            if(curve) pos.move(curveDirection);
            pos.setY(pos.getY() + 1);
            BlockState centerState = d.level.getBlockState(pos);
            if(d.isTreeWood(centerState)) {
                if(curve) {
                    BlockPos belowPos = pos.below();
                    if(d.isTreeWood(d.level.getBlockState(belowPos))) d.otherWood.add(belowPos);
                    else break;
                }
                d.trunkWood.add(pos.immutable());
                d.height++;
            }
            else if(d.canPlaceWood(centerState, lastState)) {
                d.trunkWood.add(pos.immutable());
                d.level.setBlock(pos, d.createStem(TreeStemBlock.Type.END), BLOCK_SET_FLAG);
                if(d.height != 0) {
                    BlockPos belowPos = pos.below();
                    if(curve) {
                        d.otherWood.add(belowPos);
                        d.level.setBlock(belowPos.relative(curveDirection.getOpposite()), d.createStem(positive ? TreeStemBlock.Type.ROTATED_TOP : TreeStemBlock.Type.ROTATED_BOTTOM, axis), BLOCK_SET_FLAG);
                    }
                    d.level.setBlock(belowPos, d.trunk.stemBlock.defaultBlockState(), BLOCK_SET_FLAG);
                }
                d.height++;
                d.stemsPlaced++;
                if(d.stemsPlaced >= d.ticks) break;
            }
            else {
                if(curve) {
                    BlockPos belowPos = pos.below();
                    if(d.isTreeWood(d.level.getBlockState(belowPos))) d.otherWood.add(belowPos);
                }
                break;
            }
            lastState = centerState;
        }
    }

    @Override
    protected List<Direction> getBranchStartDirections(Data d, BlockPos centerPos, Random random, @Nullable List<Direction> lastDirections, @Nullable List<Direction> lastLastDirections) {
        List<Direction> directions = Lists.newArrayList(Direction.Plane.HORIZONTAL.iterator());
        Direction direction = directions.remove((random.nextInt() & Integer.MAX_VALUE) % directions.size());
        BlockPos newPos = centerPos.relative(direction);
        while((lastDirections != null && lastDirections.contains(direction)) || d.trunkWood.contains(newPos.above()) ||  d.trunkWood.contains(newPos.below())) {
            direction = directions.remove((random.nextInt() & Integer.MAX_VALUE) % directions.size());
            newPos = centerPos.relative(direction);
        }
        return List.of(direction);
    }

    protected int getFirstCurveHeight(int maxHeight, Random random) {
        return Math.max(1, (maxHeight / (doubleCurve ? 3 : 2)) + ((random.nextInt() & Integer.MAX_VALUE) % 3) - 2);
    }

    protected int getSecondCurveHeight(int curveHeight, int maxHeight, Random random) {
        return Math.min(maxHeight, curveHeight + ((random.nextInt() & Integer.MAX_VALUE) % 3) + 1);
    }
}
