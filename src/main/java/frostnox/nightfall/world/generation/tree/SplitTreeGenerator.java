package frostnox.nightfall.world.generation.tree;

import frostnox.nightfall.block.block.tree.TreeStemBlock;
import frostnox.nightfall.util.math.OctalDirection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SplitTreeGenerator extends TreeGenerator {
    protected final int minSplits, maxSplits;
    protected final float curveChance;
    protected final boolean curveRoot;

    public SplitTreeGenerator(int baseHeight, int randHeight, int baseBranchLength, int randBranchLength, int leavesRadius, int minSplits, int maxSplits, float curveChance, boolean curveRoot) {
        super(baseHeight, randHeight, baseBranchLength, randBranchLength, leavesRadius);
        this.minSplits = minSplits;
        this.maxSplits = maxSplits;
        this.curveChance = curveChance;
        this.curveRoot = curveRoot;
    }

    protected int getSplitHeight(Data d, Random random) {
        return d.maxHeight / 3 + 1 + random.nextInt(2);
    }

    @Override
    protected int getMaxDistXZ() {
        return super.getMaxDistXZ() + maxPossibleHeight - 1;
    }

    @Override
    protected void tickTrunk(Data d, Random random, int maxHeightReached) {
        Direction curveDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction.Axis axis = curveDirection.getAxis();
        boolean positive = curveDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        if(axis == Direction.Axis.Z) positive = !positive;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(d.trunkPos.getX(), d.trunkPos.getY(), d.trunkPos.getZ());
        BlockState lastState = null;
        int splits = minSplits + random.nextInt(maxSplits - minSplits + 1);
        List<Direction> splitDirections = Lists.newArrayList(Direction.Plane.HORIZONTAL.iterator());
        while(splitDirections.size() > splits) splitDirections.remove(random.nextInt(splitDirections.size()));
        Map<Direction, Random> splitRandoms = new EnumMap<>(Direction.class);
        for(Direction dir : Direction.Plane.HORIZONTAL) splitRandoms.put(dir, new Random(random.nextLong()));
        int splitHeight = getSplitHeight(d, random);
        //Root trunk
        for(int i = 0; i < Math.min(splitHeight, (d.simulateDetection ? maxHeightReached : d.maxHeight)); i++) {
            boolean curve = curveRoot && i > 0 && i < splitHeight - 1 && random.nextFloat() < curveChance;
            if(curve) pos.move(curveDirection);
            pos.setY(pos.getY() + 1);
            BlockState centerState = d.level.getBlockState(pos);
            if(d.isTreeWood(centerState)) {
                if(curve) {
                    BlockPos belowPos = pos.below();
                    if(d.isTreeWood(d.level.getBlockState(belowPos))) d.otherWood.add(belowPos);
                    else return;
                }
                d.trunkWood.get(0).add(pos.immutable());
                d.height++;
            }
            else if(d.canPlaceWood(centerState, lastState)) {
                d.trunkWood.get(0).add(pos.immutable());
                d.level.setBlock(pos.immutable(), d.createStem(TreeStemBlock.Type.END), BLOCK_SET_FLAG);
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
                if(d.stemsPlaced >= d.ticks) return;
            }
            else {
                if(curve) {
                    BlockPos belowPos = pos.below();
                    if(d.isTreeWood(d.level.getBlockState(belowPos))) d.otherWood.add(belowPos);
                }
                return;
            }
            lastState = centerState;
        }
        //Split trunks
        int trunkIndex = 1;
        boolean convertedCenter = false;
        int tallestPlacedHeight = 0;
        int mostStemsPlaced = 0;
        for(Direction splitDir : splitDirections) {
            d.trunkWood.add(new ObjectArrayList<>());
            BlockState lastSplitState = lastState;
            BlockPos lastSplitPos = null;
            BlockPos.MutableBlockPos splitPos = pos.mutable();
            float splitCurveChance = curveChance;
            int placedHeight = 0;
            int stemsPlaced = 0;
            for(int i = splitHeight; i < (d.simulateDetection ? maxHeightReached : d.maxHeight); i++) {
                boolean moveUp = i > splitHeight + 1 && splitRandoms.get(splitDir).nextFloat() > splitCurveChance; //Reverse chance since growth is horizontal instead of vertical
                if(moveUp) {
                    splitPos.setY(splitPos.getY() + 1);
                    splitCurveChance = curveChance;
                }
                else {
                    splitPos.move(splitDir);
                    splitCurveChance /= 2;
                }
                BlockState splitState = d.level.getBlockState(splitPos);
                if(d.isTreeWood(splitState)) {
                    placedHeight++;
                    d.trunkWood.get(trunkIndex).add(splitPos.immutable());
                }
                else if(d.canPlaceWood(splitState, lastSplitState)) {
                    d.level.setBlock(splitPos.immutable(), d.createStem(TreeStemBlock.Type.END, moveUp ? Direction.Axis.Y : splitDir.getAxis()), BLOCK_SET_FLAG);
                    if(i > splitHeight) d.level.setBlock(lastSplitPos, d.createBranch(moveUp ? Direction.UP : splitDir), BLOCK_SET_FLAG);
                    else if(!convertedCenter) {
                        convertedCenter = true;
                        d.level.setBlock(pos, d.createStem(TreeStemBlock.Type.FAKE_END), BLOCK_SET_FLAG);
                    }
                    placedHeight++;
                    d.trunkWood.get(trunkIndex).add(splitPos.immutable());
                    stemsPlaced++;
                    if(stemsPlaced >= d.ticks) break;
                }
                else break;
                lastSplitPos = splitPos.immutable();
                lastSplitState = splitState;
            }
            trunkIndex++;
            if(placedHeight > tallestPlacedHeight) tallestPlacedHeight = placedHeight;
            if(stemsPlaced > mostStemsPlaced) mostStemsPlaced = stemsPlaced;
        }
        d.height += tallestPlacedHeight;
        d.stemsPlaced += mostStemsPlaced;
    }

    @Override
    protected OctalDirection[] getInitialTrunkLeavesDirections(int y, int height) {
        return OctalDirection.STRAIGHTS;
    }

    @Override
    protected List<Direction> getBranchStartDirections(Data d, BlockPos centerPos, Random random, @Nullable List<Direction> lastDirections, @Nullable List<Direction> lastLastDirections) {
        List<Direction> directions = Lists.newArrayList(Direction.Plane.HORIZONTAL.iterator());
        Direction direction = directions.remove((random.nextInt() & Integer.MAX_VALUE) % directions.size());
        //Ensure branches don't take up future trunk spots
        Direction rootDir = Direction.getNearest(d.trunkPos.getX() - centerPos.getX(), 0, d.trunkPos.getZ() - centerPos.getZ());
        while((lastDirections != null && lastDirections.contains(direction)) || direction == rootDir || direction == rootDir.getOpposite()) {
            if(directions.isEmpty()) return List.of();
            direction = directions.remove((random.nextInt() & Integer.MAX_VALUE) % directions.size());
        }
        return List.of(direction);
    }

    @Override
    protected int getTrunkLeavesRadius(int y, int height, int maxHeight, int cutoff) {
        return height < baseHeight ? Math.max(1, maxLeavesRadius - 1) : maxLeavesRadius;
    }
}
