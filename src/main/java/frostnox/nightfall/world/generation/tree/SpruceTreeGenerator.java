package frostnox.nightfall.world.generation.tree;

import frostnox.nightfall.block.block.tree.TreeStemBlock;
import frostnox.nightfall.util.data.WrappedInt;
import frostnox.nightfall.util.math.OctalDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class SpruceTreeGenerator extends TreeGenerator {
    public SpruceTreeGenerator(int baseHeight, int randHeight, int leavesRadius) {
        super(baseHeight, randHeight, 0, 0, leavesRadius);
    }

    protected boolean branchAt(Data d, int y) {
        if(d.maxHeight >= maxPossibleHeight - 2 && y == d.maxHeight * 2 / 3 - 1) return true;
        return y == 0 || y == d.maxHeight / 3 || y == d.maxHeight * 2 / 3 + 1;
    }

    protected int getBranchLengthReduction() {
        return 2;
    }

    protected int getBranchRadius(int y, int height, int maxHeight, int cutoff) {
        if(y >= height) return 1;
        int section1 = maxHeight / 3 + 1, section2 = maxHeight * 2 / 3 + 1;
        if(y == section2 + 1) return Math.max(2, getLeavesRadius(height) - 1 - (y - section2));
        if(y >= 1 && y < section1) return Math.max(1, getLeavesRadius(height) - (y - 1));
        if(y >= section1 && y <= section2) return Math.max(1, getLeavesRadius(height) - (y - section1));
        if(y > section2 && y < maxHeight) return Math.max(0, getLeavesRadius(height) - 1 - (y - section2));
        return 1;
    }

    @Override
    protected void tickBranches(Data d, Random random) {
        if(d.trunkWood.get(0).isEmpty() || d.height < 3) return;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for(int i = 0; i < d.height - 1; i++) {
            if(i >= d.trunkWood.get(0).size()) continue;
            BlockPos stemPos = d.trunkWood.get(0).get(i);
            if(branchAt(d, i)) {
                int radius = Math.max(0, getBranchRadius(i + 1, d.height, d.maxHeight, 0) - getBranchLengthReduction());
                for(Direction direction : Direction.Plane.HORIZONTAL) {
                    pos.setWithOffset(stemPos, direction);
                    BlockState branchState = d.level.getBlockState(pos);
                    if(d.stemsPlaced > 0 && d.canPlaceWood(branchState)) {
                        d.otherWood.add(pos.immutable());
                        d.level.setBlock(pos, d.createStem(TreeStemBlock.Type.END, direction.getAxis()), BLOCK_SET_FLAG);
                        tickBranch(d, random, pos, stemPos, radius, direction);
                    }
                    else if(d.isTreeWood(branchState)) {
                        d.otherWood.add(pos.immutable());
                        tickBranch(d, random, pos, stemPos, radius, direction);
                    }
                }
            }
        }
    }

    @Override
    protected void tickBranch(Data d, Random random, BlockPos startPos, BlockPos stemPos, int radius, Direction startDirection) {
        int minShortestPlacedSqr = d.ticks * d.ticks; //Layers of leaves grown should equal number of ticks
        BlockState startState = d.level.getBlockState(startPos);
        BlockState lastState = startState;
        BlockPos pos = startPos;
        int placed = 0;
        Direction.Axis axis = startDirection.getAxis();
        OctalDirection[] branchDirections;
        if(axis == Direction.Axis.X) branchDirections = new OctalDirection[] {OctalDirection.SOUTH, OctalDirection.NORTH};
        else branchDirections = new OctalDirection[] {OctalDirection.EAST, OctalDirection.WEST};
        //Measure branch length
        int maxLeavesRadius = 1;
        if(!d.woodOnly) {
            BlockPos.MutableBlockPos measurePos = startPos.mutable();
            for(int i = 0; i < radius; i++) {
                measurePos.move(startDirection);
                BlockState state = d.level.getBlockState(measurePos);
                if(i == radius - 1) maxLeavesRadius++;
                else if(d.isTreeWood(state) || d.canPlaceWood(state, lastState)) maxLeavesRadius++;
                else break;
                lastState = state;
            }
            lastState = startState;
            BlockPos growPos = startPos;
            tickBranchLeaves(d, pos, stemPos, maxLeavesRadius, minShortestPlacedSqr);
            for(int r = maxLeavesRadius - 1; r >= 0; r--) {
                growPos = growPos.relative(Direction.UP);
                if(r == 0) setBranchLeavesBlock(d, growPos, growPos, 1, new WrappedInt(Integer.MAX_VALUE), minShortestPlacedSqr, OctalDirection.DOWN, OctalDirection.DOWN, 1);
                else tickBranchLeaves(d, growPos, stemPos, r, minShortestPlacedSqr, true, branchDirections);
            }
        }
        for(int i = 0; i < radius; i++) {
            BlockPos lastPos = pos;
            pos = pos.relative(startDirection);
            BlockState state = d.level.getBlockState(pos);
            if(i == radius - 1) {
                if(!d.woodOnly) {
                    if(!d.noPlacement && lastState.is(d.trunk.stemBlock) && lastState.getValue(TreeStemBlock.TYPE) == TreeStemBlock.Type.END) {
                        d.level.setBlock(pos, d.createLeaves(isAltLeaves(d, pos)), BLOCK_SET_FLAG);
                        placed++;
                    }
                    else if(!d.simulateDetection && !state.is(d.trunk.leavesBlock)) break;
                    d.branchLeaves.add(pos);
                }
            }
            else {
                if(d.canPlaceWood(state, lastState)) {
                    d.level.setBlock(pos, d.createStem(TreeStemBlock.Type.END, axis), BLOCK_SET_FLAG);
                    d.level.setBlock(lastPos, d.createBranch(startDirection), BLOCK_SET_FLAG);
                    d.branchLeaves.remove(pos);
                    placed++;
                }
                else if(!d.isTreeWood(state)) break;
                d.otherWood.add(pos);
            }
            if(!d.woodOnly) {
                int leavesRadius = Math.max(1, maxLeavesRadius - i - 1);
                BlockPos growPos = pos;
                tickBranchLeaves(d, pos, stemPos, leavesRadius, minShortestPlacedSqr);
                for(int r = leavesRadius - 1; r >= 0; r--) {
                    growPos = growPos.relative(Direction.UP);
                    if(r == 0) setBranchLeavesBlock(d, growPos, growPos, 1, new WrappedInt(Integer.MAX_VALUE), minShortestPlacedSqr, OctalDirection.DOWN, OctalDirection.DOWN, 1);
                    else tickBranchLeaves(d, growPos, stemPos, r, minShortestPlacedSqr, true, branchDirections);
                }
            }
            if(placed > 0 && placed >= d.ticks) break;
            lastState = state;
        }
    }

    @Override
    protected OctalDirection[] getBranchLeavesDirections(Data d) {
        return OctalDirection.CARDINALS;
    }

    @Override
    protected boolean cancelBranchLeavesDirection(OctalDirection newDir, OctalDirection backDir, OctalDirection originDir) {
        return newDir != originDir.getOpposite();
    }

    @Override
    protected int getTrunkLeavesRadius(int y, int height, int maxHeight, int cutoff) {
        return 1;
    }

    @Override
    protected int getTrunkLeavesCutoff(int height) {
        return 1;
    }

    @Override
    protected int getLeavesRadius(int height) {
        if(height < baseHeight - 2) return Math.max(1, maxLeavesRadius - 1);
        if(height < baseHeight) return Math.max(1, maxLeavesRadius);
        if(height <= averageHeight) return Math.max(1, maxLeavesRadius + 1);
        return maxLeavesRadius + 2;
    }

    @Override
    protected float squareBranchLeavesRadius(int radius) {
        return radius * radius;
    }
}
