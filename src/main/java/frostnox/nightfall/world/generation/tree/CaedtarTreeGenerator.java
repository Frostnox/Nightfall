package frostnox.nightfall.world.generation.tree;

import frostnox.nightfall.block.block.tree.TreeStemBlock;
import frostnox.nightfall.data.TagsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Random;

public class CaedtarTreeGenerator extends TreeGenerator {
    public CaedtarTreeGenerator(int baseHeight, int randHeight, int baseBranchLength, int randBranchLength, int leavesRadius) {
        super(baseHeight, randHeight, baseBranchLength, randBranchLength, leavesRadius);
    }

    @Override
    protected List<Direction> getBranchStartDirections(Data d, BlockPos centerPos, Random random, @Nullable List<Direction> lastDirections, @Nullable List<Direction> lastLastDirections) {
        List<Direction> directions = Lists.newArrayList(Direction.Plane.HORIZONTAL.iterator());
        if(lastDirections != null) directions.removeAll(lastDirections);
        while(directions.size() > 2) directions.remove((random.nextInt() & Integer.MAX_VALUE) % directions.size());
        if(random.nextBoolean()) directions.remove((random.nextInt() & Integer.MAX_VALUE) % directions.size());
        return directions;
    }

    @Override
    public boolean canPlaceOnBlock(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(TagsNF.TILLABLE_OR_AQUATIC_SOIL);
    }

    @Override
    protected void tickBranches(Data d, Random random) {
        super.tickBranches(d, random);
        //Roots
        if(d.height >= baseHeight - 1) {
            BlockPos.MutableBlockPos pos = d.trunkPos.mutable();
            EnumMap<Direction, BlockPos> growPositions = new EnumMap<>(Direction.class);
            for(Direction dir : Direction.Plane.HORIZONTAL) {
                pos.setWithOffset(d.trunkPos, dir);
                Integer bestY = null;
                for(int i = 0; true; i++) {
                    BlockState block = d.level.getBlockState(pos);
                    if(i == 2) {
                        if(!block.is(TagsNF.NATURAL_SOIL)) bestY = null;
                        break;
                    }
                    else {
                        if(block.is(d.trunk.stemBlock)) d.otherWood.add(pos.immutable());
                        else {
                            if(!block.is(TagsNF.TREE_REPLACEABLE)) {
                                if(!block.is(TagsNF.NATURAL_SOIL)) bestY = null;
                                break;
                            }
                            else if(bestY == null || d.ticks > 1 || d.simulateDetection) {
                                if(d.simulateDetection) d.otherWood.add(pos.immutable());
                                else if(d.noPlacement) break;
                                bestY = pos.getY();
                            }
                        }
                    }
                    pos.setY(pos.getY() - 1);
                }
                if(bestY != null) growPositions.put(dir, new BlockPos(pos.getX(), bestY, pos.getZ()));
            }
            if(d.stemsPlaced > 0) {
                for(var entry : growPositions.entrySet()) {
                    BlockPos growPos = entry.getValue();
                    d.otherWood.add(growPos);
                    if(growPos.getY() == d.trunkPos.getY()) d.level.setBlock(growPos, d.createStem(TreeStemBlock.Type.END, Direction.Axis.Y), 3);
                    else {
                        d.level.setBlock(growPos, d.createStem(TreeStemBlock.Type.TOP, Direction.Axis.Y), 3);
                        d.level.setBlock(growPos.above(), d.createStem(TreeStemBlock.Type.BOTTOM, Direction.Axis.Y), 3);
                    }
                }
            }
        }
    }
}
