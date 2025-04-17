package frostnox.nightfall.world.generation.tree;

import frostnox.nightfall.util.data.WrappedInt;
import frostnox.nightfall.util.math.OctalDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class PalmTreeGenerator extends CurvedTreeGenerator {
    protected static int TOP_FRONDS_INDEX = 0;
    protected static int TOP_FRONDS = 1;

    public PalmTreeGenerator(int baseHeight, int randHeight, double curveChance, boolean doubleCurve) {
        super(baseHeight, randHeight, 1, 0, 5, curveChance, doubleCurve);
    }

    @Override
    protected int getMaxPossibleHeight() {
        return super.getMaxPossibleHeight() + 1;
    }

    @Override
    protected int getTrunkLeavesCutoff(int height) {
        return 1;
    }

    @Override
    protected OctalDirection[] getTrunkLeavesDirections(int y, int height) {
        return OctalDirection.OCTALS_UP;
    }

    @Override
    protected int getLeavesRadius(int height) {
        return 0;
    }

    @Override
    protected int getMinBranchHeight(int maxHeight, Random random) {
        return maxHeight - 2;
    }

    @Override
    protected int getMaxBranchHeight(int height, int minBranchHeight) {
        return super.getMaxBranchHeight(height, minBranchHeight);
    }

    @Override
    protected int getTrunkLeavesRadius(int y, int height, int maxHeight, int cutoff) {
        int radius;
        if(y < averageHeight / 2) radius = 1;
        else if(y > averageHeight - 1 && y >= maxHeight - 3) radius = 3;
        else radius = 2;
        if(y == maxHeight - 1 && y > maxPossibleHeight - 4) radius++;
        return radius;
    }

    @Override
    protected List<Direction> getBranchStartDirections(Data d, BlockPos centerPos, Random random, @Nullable List<Direction> lastDirections, @Nullable List<Direction> lastLastDirections) {
        return Direction.Plane.HORIZONTAL.stream().collect(Collectors.toList());
    }

    @Override
    protected boolean isAltLeaves(Data d, BlockPos pos) {
        return pos.getY() != d.trunkPos.getY() + d.height;
    }

    @Override
    protected void setupData(Data d, Random random) {
        d.intData = new int[1];
        d.intData[TOP_FRONDS_INDEX] = random.nextFloat() > 0.4F ? TOP_FRONDS : -1;
    }

    @Override
    protected void tickTrunkLeaves(Data d, boolean old) {
        int cutoff = getTrunkLeavesCutoff(d.height);
        WrappedInt shortestPlaced = new WrappedInt(Integer.MAX_VALUE);
        int minShortestPlaced = !d.oldTrunkLeaves.isEmpty() ? maxLeavesRadius : d.ticks; //If old leaves are shifting up, let new leaves grow fully to next stage
        int y = d.height - cutoff;
        if(y < 0 || y >= d.trunkWood.size()) return;
        BlockPos pos = d.trunkWood.get(y);
        int radius = getTrunkLeavesRadius(y, d.height, d.maxHeight, cutoff);
        if(radius == 1) {
            for(OctalDirection dir : getTrunkLeavesDirections(y, d.height)) {
                setTrunkLeavesBlock(d, dir.move(pos), old, 1, minShortestPlaced, shortestPlaced);
            }
        }
        else {
            radius = Math.max(1, radius - 1);
            int diagonalRadius = (radius) / 2;
            for(OctalDirection dir : getTrunkLeavesDirections(y, d.height)) {
                tickTrunkLeaves(d, pos, dir.move(pos), dir.isDiagonal() ? diagonalRadius : radius,
                        old, 1, minShortestPlaced, shortestPlaced, dir.getOpposite());
            }
        }
    }

    @Override
    protected void tickTrunkLeaves(Data d, BlockPos lastPos, BlockPos pos, int radius, boolean old, int dist, int minShortestPlaced, WrappedInt shortestPlaced, OctalDirection originDir) {
        if(dist <= radius) {
            if(!setTrunkLeavesBlock(d, pos, old, dist, minShortestPlaced, shortestPlaced) && originDir == OctalDirection.DOWN) return;
            if(radius == 3 && originDir != OctalDirection.DOWN) setTrunkLeavesBlock(d, originDir.getOpposite().move(pos), old, dist + 1, minShortestPlaced, shortestPlaced);
        }
        //Top fronds
        if(originDir == OctalDirection.DOWN) {
            if(d.intData[TOP_FRONDS_INDEX] == TOP_FRONDS) {
                //Tip
                setTrunkLeavesBlock(d, pos.above(), old, dist + 1, minShortestPlaced, shortestPlaced);
                for(OctalDirection dir : OctalDirection.CARDINALS) {
                    BlockPos movePos = pos;
                    int length = radius == 3 ? 3 : 2;
                    for(int i = 1; i <= length; i++) {
                        movePos = dir.move(movePos);
                        //Plane
                        if(!setTrunkLeavesBlock(d, movePos, old, dist + i, minShortestPlaced, shortestPlaced)) break;
                        //Tip
                        if(i == length && radius >= 2) {
                            setTrunkLeavesBlock(d, movePos.above(), old, dist + i, minShortestPlaced, shortestPlaced);
                        }
                    }
                }
            }
        }
        //Bottom fronds
        else {
            BlockPos movePos = pos.below();
            OctalDirection dir = originDir.getOpposite();
            if(dir.isDiagonal() && radius > 0) {
                //Long diagonal
                if(!setTrunkLeavesBlock(d, movePos, old, dist, minShortestPlaced, shortestPlaced)) return;
                boolean x = setTrunkLeavesBlock(d, movePos.offset(dir.xStepInt, 0, 0), old, dist + 1, minShortestPlaced, shortestPlaced);
                boolean z = setTrunkLeavesBlock(d, movePos.offset(0, 0, dir.zStepInt), old, dist + 1, minShortestPlaced, shortestPlaced);
                if(!x && !z) return;
                movePos = pos.below();
            }
            else if(dir.isCardinal()) {
                //Trunk
                if(radius >= 2) setTrunkLeavesBlock(d, movePos.below(), old, dist, minShortestPlaced, shortestPlaced);
                else setTrunkLeavesBlock(d, movePos, old, dist, minShortestPlaced, shortestPlaced);
            }
            //Short diagonal
            if(radius == 0) setTrunkLeavesBlock(d, movePos, old, dist, minShortestPlaced, shortestPlaced);
            else for(int i = 1; i <= radius; i++) {
                movePos = dir.move(movePos);
                //Plane
                if(!setTrunkLeavesBlock(d, movePos, old, dist + i - 1, minShortestPlaced, shortestPlaced)) break;
                //Droop
                if(i == radius) setTrunkLeavesBlock(d, movePos.below(), old, dist + i, minShortestPlaced, shortestPlaced);
            }
        }
    }
}
