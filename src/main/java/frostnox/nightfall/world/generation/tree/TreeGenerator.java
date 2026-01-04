package frostnox.nightfall.world.generation.tree;

import frostnox.nightfall.block.block.tree.*;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.world.UpdateBlockToClient;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.data.WrappedInt;
import frostnox.nightfall.util.math.OctalDirection;
import frostnox.nightfall.world.Season;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreeGenerator {
    public final int baseHeight, randHeight, maxPossibleHeight, averageHeight, baseBranchLength, randBranchLength,
            maxLeavesRadius, maxLength, maxDistXZ, maxLeavesDistXZ;
    protected static final int BLOCK_SET_FLAG = 1 | 2 | 16;

    public static class Data {
        public final List<List<BlockPos>> trunkWood;
        public final ObjectSet<BlockPos> trunkLeaves, otherWood, branchLeaves, oldTrunkLeaves, changingLeaves;
        protected final WorldGenLevel level;
        protected final TreeTrunkBlock trunk;
        protected final BlockPos trunkPos;
        protected final BlockState newLeaves;
        public final boolean generating, decaying, woodOnly, forceGrowth, simulateDetection;
        public boolean noPlacement;
        public int ticks, stemsPlaced, height, maxHeight;
        public int[] intData = null;

        protected Data(WorldGenLevel level, TreeTrunkBlock trunk, BlockPos trunkPos, int ticks, boolean decaying, int stemsPlaced, int height, int maxHeight, boolean simulateDetection, boolean woodOnly, boolean forceGrowth, boolean generating) {
            this.level = level;
            this.trunk = trunk;
            this.trunkPos = trunkPos;
            this.ticks = ticks;
            this.stemsPlaced = stemsPlaced;
            this.height = height;
            this.maxHeight = maxHeight;
            this.simulateDetection = simulateDetection;
            this.woodOnly = woodOnly;
            this.forceGrowth = forceGrowth;
            this.trunkWood = new ObjectArrayList<>(5);
            trunkWood.add(new ObjectArrayList<>());
            this.trunkLeaves = new ObjectArraySet<>();
            this.otherWood =new ObjectArraySet<>();
            this.branchLeaves = new ObjectArraySet<>();
            this.oldTrunkLeaves = new ObjectArraySet<>();
            this.decaying = decaying;
            this.changingLeaves = new ObjectArraySet<>();
            this.noPlacement = ticks == 0;
            this.generating = generating;
            newLeaves = (decaying ? (trunk.branchesBlock == null ? trunk.leavesBlock : trunk.branchesBlock) : trunk.leavesBlock).defaultBlockState();
        }

        public ObjectSet<BlockPos> collectLeaves() {
            return new ObjectOpenHashSet<>(Stream.of(trunkLeaves, branchLeaves).flatMap(Collection::stream).collect(Collectors.toSet()));
        }

        public ObjectSet<BlockPos> collectWood() {
            return new ObjectOpenHashSet<>(Stream.concat(trunkWood.stream(), Stream.of(otherWood)).flatMap(Collection::stream).collect(Collectors.toSet()));
        }

        public ObjectSet<BlockPos> collectTree() {
            return new ObjectOpenHashSet<>(Stream.concat(trunkWood.stream(), Stream.of(otherWood, trunkLeaves, branchLeaves)).flatMap(Collection::stream).collect(Collectors.toSet()));
        }

        public boolean hasTrunkWood(BlockPos pos) {
            for(List<BlockPos> positions : trunkWood) {
                if(positions.contains(pos)) return true;
            }
            return false;
        }

        protected boolean visitedTrunkLeaves(BlockPos pos, boolean old) {
            if(old) return oldTrunkLeaves.contains(pos);
            else return trunkLeaves.contains(pos);
        }

        protected void collectChangingLeaves(BlockPos pos, BlockState state) {
            if(!generating) {
                if(decaying) {
                    if(state.is(trunk.leavesBlock) || state.is(trunk.fruitBlock)) changingLeaves.add(pos);
                }
                else if(state.is(trunk.branchesBlock)) changingLeaves.add(pos);
            }
        }

        protected boolean isTreeWood(BlockState state) {
            return simulateDetection || trunk.isTreeBase(state);
        }

        protected boolean isTreeLeaves(BlockState state) {
            return state.is(trunk.leavesBlock) || state.is(trunk.branchesBlock) || state.is(trunk.fruitBlock);
        }

        protected boolean canPlaceWood(BlockState state) {
            if(noPlacement) return false;
            return state.isAir() || isTreeLeaves(state);
        }

        protected boolean canPlaceWood(BlockState state, @Nullable BlockState lastState) {
            if(noPlacement) return false;
            if(state.isAir() || isTreeLeaves(state)) {
                if(lastState == null || !lastState.is(trunk.stemBlock)) return forceGrowth;
                else if(lastState.getValue(TreeStemBlock.TYPE) == TreeStemBlock.Type.END) return true;
                else return forceGrowth;
            }
            else return false;
        }

        protected BlockState createLeaves(boolean alt) {
            return newLeaves.setValue(TreeBranchesBlock.ALTERNATE, alt);
        }

        protected BlockState createStem(TreeStemBlock.Type type) {
            return trunk.stemBlock.defaultBlockState().setValue(TreeStemBlock.TYPE, type);
        }

        protected BlockState createStem(TreeStemBlock.Type type, Direction.Axis axis) {
            return trunk.stemBlock.defaultBlockState().setValue(TreeStemBlock.TYPE, type).setValue(TreeStemBlock.AXIS, axis);
        }

        protected BlockState createBranch(Direction newDir) {
            Direction.Axis axis = newDir.getAxis();
            boolean positive = newDir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            if(axis == Direction.Axis.Z) positive = !positive;
            return trunk.stemBlock.defaultBlockState().setValue(TreeStemBlock.TYPE, positive ? TreeStemBlock.Type.TOP : TreeStemBlock.Type.BOTTOM)
                    .setValue(TreeStemBlock.AXIS, axis);
        }
    }

    public TreeGenerator(int baseHeight, int randHeight, int baseBranchLength, int randBranchLength, int maxLeavesRadius) {
        this.baseHeight = baseHeight;
        this.randHeight = randHeight;
        this.baseBranchLength = baseBranchLength;
        this.randBranchLength = randBranchLength;
        this.maxLeavesRadius = maxLeavesRadius;
        this.maxPossibleHeight = getMaxPossibleHeight();
        this.averageHeight = baseHeight + Math.max(0, randHeight - 1) / 2;
        this.maxLength = maxPossibleHeight + baseBranchLength + Math.max(0, randBranchLength - 1);
        this.maxDistXZ = getMaxDistXZ();
        this.maxLeavesDistXZ = maxDistXZ + maxLeavesRadius;
    }

    protected int getMaxPossibleHeight() {
        return baseHeight + Math.max(0, randHeight - 1);
    }

    protected int getMaxDistXZ() {
        return baseBranchLength + Math.max(0, randBranchLength - 1);
    }

    public boolean canPlaceOnBlock(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(TagsNF.TILLABLE_SOIL);
    }

    public boolean canGrowAt(BlockAndTintGetter level, BlockPos pos) {
        return canPlaceOnBlock(level, pos.below()) && level.getRawBrightness(pos, 0) >= 7 &&
                level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.above(2)).isAir();
    }

    public Data grow(ServerLevel level, TreeTrunkBlockEntity entity, boolean forceGrowth) {
        return grow(level, entity, 1, forceGrowth, false);
    }

    /**
     * Grows tree based on its current state
     * @param ticks number of stages to grow, must be >= 1
     */
    public Data grow(WorldGenLevel level, TreeTrunkBlockEntity entity, int ticks, boolean forceGrowth, boolean generating) {
        ticks = Math.max(1, ticks);
        return tick(level, entity, ticks, LevelData.isPresent(level.getLevel()) ? LevelData.get(level.getLevel()).getSeasonTime() : 0, false, false, forceGrowth, generating);
    }

    /**
     * Grows tree based on its current state
     * @param ticks number of stages to grow, must be >= 1
     */
    public Data grow(WorldGenLevel level, TreeTrunkBlockEntity entity, int ticks, long seasonTime, boolean forceGrowth) {
        ticks = Math.max(1, ticks);
        return tick(level, entity, ticks, seasonTime, false, false, forceGrowth, false);
    }

    /**
     * @param simulateDetection treat any space where a tree block is expected as valid, regardless of actual state
     */
    public Data getTree(WorldGenLevel level, TreeTrunkBlockEntity entity, boolean simulateDetection) {
        return tick(level, entity, 0, 0, simulateDetection, false, false, false);
    }

    public Data getWood(WorldGenLevel level, TreeTrunkBlockEntity entity, boolean simulateDetection) {
        return tick(level, entity, 0, 0, simulateDetection, true, false, false);
    }

    protected Data tick(WorldGenLevel level, TreeTrunkBlockEntity entity, int ticks, long seasonTime, boolean simulateDetection, boolean woodOnly, boolean forceGrowth, boolean generating) {
        TreeTrunkBlock trunkBlock = (TreeTrunkBlock) entity.getBlockState().getBlock();
        boolean decaying;
        if(ticks > 0 && trunkBlock.type.isDeciduous()) {
            Season season = Season.get(seasonTime);
            if(season == Season.FALL) decaying = season.getProgress(seasonTime) > 0.5F;
            else decaying = season == Season.WINTER;
        }
        else decaying = false;
        Data d = new Data(level, trunkBlock, entity.getBlockPos(), ticks, decaying, 0, 0, 0, simulateDetection, woodOnly, forceGrowth, generating);
        Random random = new Random(entity.getSeed());
        d.maxHeight = baseHeight + ((random.nextInt() & Integer.MAX_VALUE) % randHeight);
        setupData(d, new Random(random.nextLong()));
        //Trunk
        tickTrunk(d, new Random(random.nextLong()), entity.maxHeight);
        d.height = Math.max(entity.maxHeight, d.height);
        //Branches & branch leaves
        tickBranches(d, new Random(random.nextLong()));
        //Trunk leaves
        if(!d.woodOnly) {
            //Mark trunk leaves from this stage before generating the new ones
            if(!d.generating && d.height > 1 && d.stemsPlaced > 0) {
                d.height -= d.stemsPlaced;
                tickTrunkLeaves(d, true);
                d.height += d.stemsPlaced;
            }
            if(d.height >= entity.maxHeight) tickTrunkLeaves(d, false);
            else if(d.decaying) {
                boolean noPlacement = d.noPlacement;
                d.noPlacement = true;
                tickTrunkLeaves(d, false);
                d.noPlacement = noPlacement;
            }
        }
        if(!d.oldTrunkLeaves.isEmpty()) {
            List<TreeTrunkBlockEntity> nearbyTrunks = TreeTrunkBlockEntity.getNearbyTrunks(level.getLevel(), trunkBlock.type, entity.getBlockPos(), d.oldTrunkLeaves);
            ObjectSet<BlockPos> nearbyTrees = new ObjectOpenHashSet<>(60 * nearbyTrunks.size());
            for(TreeTrunkBlockEntity nearbyTrunk : nearbyTrunks) {
                if(nearbyTrunk == entity) continue;
                nearbyTrees.addAll(nearbyTrunk.getTree());
            }
            for(BlockPos pos : d.oldTrunkLeaves) {
                if(!d.trunkLeaves.contains(pos)) {
                    if(!nearbyTrees.contains(pos)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), BLOCK_SET_FLAG);
                }
                else updateLeaves(d, d.level.getBlockState(pos), pos);
            }
        }
        if(ticks > 0 && !d.generating && !d.changingLeaves.isEmpty()) {
            if(d.decaying) for(BlockPos pos : d.changingLeaves) {
                BlockState state = level.getBlockState(pos);
                if(d.isTreeLeaves(state)) LevelUtil.uncheckedDropDestroyBlockNoSound((Level) level, pos, state, d.createLeaves(isAltLeaves(d, pos)), null, BLOCK_SET_FLAG);
            }
            else for(BlockPos pos : d.changingLeaves) {
                BlockState state = level.getBlockState(pos);
                if(d.isTreeLeaves(state)) level.setBlock(pos, d.createLeaves(isAltLeaves(d, pos)), BLOCK_SET_FLAG);
            }
        }
        if(!d.noPlacement) {
            if(!d.generating) {
                ObjectSet<SectionPos> accessedSections = new ObjectOpenHashSet<>(8);
                //Update leaves tint on client (this could theoretically be done on the client, but access to server logic already done allows for more precise targeting)
                for(BlockPos leavesPos : d.branchLeaves) updateClientSection(accessedSections, level.getLevel(), leavesPos);
                for(BlockPos leavesPos : d.trunkLeaves) updateClientSection(accessedSections, level.getLevel(), leavesPos);
            }
            if(d.stemsPlaced > 0) entity.maxHeight = d.height;
        }
        entity.lastTick = level.getLevelData().getGameTime();
        entity.setChanged();
        return d;
    }

    private static void updateClientSection(ObjectSet<SectionPos> accessedSections, ServerLevel level, BlockPos pos) {
        if(accessedSections.add(SectionPos.of(pos))) NetworkHandler.toAllTrackingChunk(level.getChunkAt(pos), new UpdateBlockToClient(pos));
    }

    protected void setupData(Data d, Random random) {

    }

    public void tryFruit(WorldGenLevel level, Data d, TreeTrunkBlockEntity entity) {

    }

    protected void tryFruitBranchLeaves(WorldGenLevel level, Data d, TreeTrunkBlockEntity entity, int maxFruit, List<BlockPos> leaves) {
        int limit = Math.min(leaves.size(), 1 + level.getRandom().nextInt(maxFruit));
        for(int i = 0; i < limit; i++) {
            BlockPos pos = leaves.remove(level.getRandom().nextInt(leaves.size()));
            level.setBlock(pos, d.trunk.fruitBlock.defaultBlockState().setValue(TreeBranchesBlock.ALTERNATE, isAltLeaves(d, pos)), BLOCK_SET_FLAG);
        }
        entity.hasFruited = true;
        entity.setChanged();
    }

    //Logs must always be collected in a vertical line if using default branch implementation
    protected void tickTrunk(Data d, Random random, int maxHeightReached) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(d.trunkPos.getX(), d.trunkPos.getY(), d.trunkPos.getZ());
        BlockState lastState = null;
        for(int i = 0; i < (d.simulateDetection ? maxHeightReached : d.maxHeight); i++) {
            pos.setY(pos.getY() + 1);
            BlockState centerState = d.level.getBlockState(pos);
            if(d.isTreeWood(centerState)) {
                d.height++;
                d.trunkWood.get(0).add(pos.immutable());
            }
            else if(d.canPlaceWood(centerState, lastState)) {
                d.level.setBlock(pos.immutable(), d.createStem(TreeStemBlock.Type.END), BLOCK_SET_FLAG);
                if(d.height != 0) d.level.setBlock(pos.below(), d.trunk.stemBlock.defaultBlockState(), BLOCK_SET_FLAG);
                d.height++;
                d.stemsPlaced++;
                d.trunkWood.get(0).add(pos.immutable());
                if(d.stemsPlaced >= d.ticks) break;
            }
            else break; //Missing log
            lastState = centerState;
        }
    }

    protected void tickBranches(Data d, Random random) {
        if(baseBranchLength == 0) return;
        int minBranchHeight = getMinBranchHeight(d.maxHeight, random);
        int maxBranchHeight = getMaxBranchHeight(d.height, minBranchHeight);
        int maxRadius = getLeavesRadius(d.maxHeight);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for(int j = d.trunkWood.size() > 1 ? 1 : 0; j < d.trunkWood.size(); j++) {
            List<BlockPos> trunkWood = d.trunkWood.get(j);
            Random trunkRandom = new Random(random.nextLong());
            if(trunkWood.isEmpty()) continue;
            List<Direction> lastDirections = null, lastLastDirections = null;
            int minBranchHeightOffset = minBranchHeight + d.trunkPos.getY();
            BlockPos centerPos = trunkWood.get(0);
            int index = 1;
            while(centerPos.getY() < minBranchHeightOffset && index < trunkWood.size()) {
                centerPos = trunkWood.get(index);
                index++;
            }
            for(int i = minBranchHeight; i < maxBranchHeight; i++) {
                Random branchRandom = new Random(trunkRandom.nextLong());
                if(index >= trunkWood.size()) break;
                centerPos = trunkWood.get(index);
                index++;
                List<Direction> branchDirections = getBranchStartDirections(d, centerPos, branchRandom, lastDirections, lastLastDirections);
                for(Direction direction : branchDirections) {
                    Random dirRandom = new Random(branchRandom.nextLong());
                    pos.set(centerPos);
                    //Select branch direction
                    pos.move(direction);
                    BlockState branchState = d.level.getBlockState(pos);
                    //Create new branch or tick existing one
                    if(d.stemsPlaced > 0 && d.canPlaceWood(branchState)) {
                        d.otherWood.add(pos.immutable());
                        d.level.setBlock(pos.immutable(), d.createStem(TreeStemBlock.Type.END, direction.getAxis()), BLOCK_SET_FLAG);
                        if(d.ticks > 1) tickBranch(d, dirRandom, pos, centerPos, maxRadius, direction);
                        else tickBranchLeaves(d, pos, centerPos, maxRadius, d.ticks * d.ticks);
                    }
                    else if(d.isTreeWood(branchState)) {
                        d.otherWood.add(pos.immutable());
                        tickBranch(d, dirRandom, pos, centerPos, maxRadius, direction);
                    }
                }
                lastLastDirections = lastDirections;
                lastDirections = branchDirections;
            }
        }
    }

    protected List<Direction> getBranchStartDirections(Data d, BlockPos centerPos, Random random, @Nullable List<Direction> lastDirections, @Nullable List<Direction> lastLastDirections) {
        List<Direction> directions = Lists.newArrayList(Direction.Plane.HORIZONTAL.iterator());
        Direction direction = directions.remove((random.nextInt() & Integer.MAX_VALUE) % directions.size());
        if(lastDirections != null) {
            while(lastDirections.contains(direction)) direction = directions.remove((random.nextInt() & Integer.MAX_VALUE) % directions.size());
        }
        return List.of(direction);
    }

    protected int getMinBranchHeight(int maxHeight, Random random) {
        return (baseHeight / 2) + random.nextInt(2);
    }

    protected int getMaxBranchHeight(int height, int minBranchHeight) {
        return Math.max(0, height - 1);
    }

    protected int getBranchLength(Data d, BlockPos stemPos, Random random) {
        int length = baseBranchLength + (randBranchLength > 0 ? ((random.nextInt() & Integer.MAX_VALUE) % randBranchLength) : 0);
        if(d.height < averageHeight / 2 && length > 2) length--;
        return length;
    }

    protected void tickBranch(Data d, Random random, BlockPos startPos, BlockPos stemPos, int radius, Direction startDirection) {
        if(baseBranchLength == 0) return;
        int minShortestPlacedSqr = d.ticks * d.ticks; //Layers of leaves grown should equal number of ticks
        BlockState lastState = d.level.getBlockState(startPos);
        Direction direction;
        Direction lastDirection = startDirection;
        BlockPos pos = startPos;
        int placed = 0;
        int branchLength = getBranchLength(d, stemPos, random);
        for(int i = 2; i <= branchLength; i++) {
            direction = selectBranchDirection(d, new Random(random.nextLong()), pos, i, startDirection, lastDirection);
            lastDirection = direction;
            BlockPos lastPos = pos.immutable();
            pos = pos.relative(direction);
            BlockState state = d.level.getBlockState(pos);
            if(d.canPlaceWood(state, lastState)) {
                d.otherWood.add(pos);
                d.level.setBlock(pos.immutable(), d.createStem(TreeStemBlock.Type.END, direction.getAxis()), BLOCK_SET_FLAG);
                d.level.setBlock(lastPos, d.createBranch(direction), BLOCK_SET_FLAG);
                d.branchLeaves.remove(pos);
                placed++;
            }
            else if(!d.isTreeWood(state)) break;
            d.otherWood.add(pos);
            tickBranchLeaves(d, pos, stemPos, radius, minShortestPlacedSqr);
            if(placed > 0 && placed >= d.ticks) break;
            lastState = state;
        }
        tickBranchLeaves(d, startPos, stemPos, radius, minShortestPlacedSqr);
    }

    protected Direction selectBranchDirection(Data d, Random random, BlockPos pos, int length, Direction startDirection, Direction lastDirection) {
        if(length != 2) {
            if(lastDirection == startDirection || random.nextFloat() < 0.2F) {
                Direction dir = random.nextBoolean() ? lastDirection.getClockWise() : lastDirection.getCounterClockWise();
                if(dir == startDirection.getOpposite()) return startDirection;
                else return dir;
            }
            else return startDirection;
        }
        else return lastDirection;
    }

    protected float squareBranchLeavesRadius(int radius) {
        return radius > 1 ? (radius - 0.5F) * (radius - 0.5F) : 1; //This results in a less square shape at low values
    }

    protected void tickBranchLeaves(Data d, BlockPos branchPos, BlockPos stemPos, int radius, int minShortestPlacedSqr) {
        tickBranchLeaves(d, branchPos, stemPos, radius, minShortestPlacedSqr, false, getBranchLeavesDirections(d));
    }

    protected void tickBranchLeaves(Data d, BlockPos branchPos, BlockPos stemPos, int radius, int minShortestPlacedSqr, boolean placeOriginLeaves, OctalDirection[] directions) {
        if(d.woodOnly) return;
        float radiusSqr = squareBranchLeavesRadius(radius);
        WrappedInt shortestPlaced = new WrappedInt(Integer.MAX_VALUE);
        if(!placeOriginLeaves || setBranchLeavesBlock(d, branchPos, branchPos, radiusSqr, shortestPlaced, minShortestPlacedSqr, OctalDirection.CENTER, OctalDirection.CENTER, 1)) {
            if(radius == 0) return;
            for(OctalDirection dir : directions) {
                tickBranchLeaves(d, branchPos, branchPos.offset(dir.xStepInt, dir.yStepInt, dir.zStepInt), radiusSqr, shortestPlaced, minShortestPlacedSqr, dir.getOpposite(), dir.getOpposite(), 1);
            }
        }
    }

    protected void tickBranchLeaves(Data d, BlockPos branchPos, BlockPos pos, float radiusSqr, WrappedInt shortestPlaced, int minShortestPlacedSqr, OctalDirection backDir, OctalDirection originDir, int dist) {
        if(!setBranchLeavesBlock(d, branchPos, pos, radiusSqr, shortestPlaced, minShortestPlacedSqr, backDir, originDir, dist)) return;
        for(OctalDirection newDir : getBranchLeavesDirections(d)) {
            if(cancelBranchLeavesDirection(newDir, backDir, originDir)) continue;
            tickBranchLeaves(d, branchPos, pos.offset(newDir.xStepInt, newDir.yStepInt, newDir.zStepInt), radiusSqr, shortestPlaced, minShortestPlacedSqr, newDir.getOpposite(), originDir, dist + 1);
        }
    }

    protected boolean checkBranchLeaves(Data d, BlockPos branchPos, BlockPos pos, float radiusSqr, WrappedInt shortestPlaced, int minShortestPlacedSqr, OctalDirection backDir, OctalDirection originDir, int dist) {
        if(dist * dist > radiusSqr) return false;
        double distSqr = branchPos.distSqr(pos);
        return distSqr <= radiusSqr;
    }

    protected boolean setBranchLeavesBlock(Data d, BlockPos branchPos, BlockPos pos, float radiusSqr, WrappedInt shortestPlaced, int minShortestPlacedSqr, OctalDirection backDir, OctalDirection originDir, int dist) {
        if(!checkBranchLeaves(d, branchPos, pos, radiusSqr, shortestPlaced, minShortestPlacedSqr, backDir, originDir, dist)) return false;
        if(d.simulateDetection) d.branchLeaves.add(pos);
        else {
            BlockState state = d.level.getBlockState(pos);
            int roundedDistSqr = Mth.ceil(branchPos.distSqr(pos));
            if(!d.generating && roundedDistSqr > shortestPlaced.val) {
                if(d.isTreeLeaves(state)) {
                    d.collectChangingLeaves(pos, state);
                }
                return false; //Only grow one layer of leaves per tick
            }
            if(d.isTreeLeaves(state)) {
                d.branchLeaves.add(pos);
                d.collectChangingLeaves(pos, state);
            }
            else if(state.isAir() && !d.noPlacement) {
                d.branchLeaves.add(pos);
                d.level.setBlock(pos, d.createLeaves(isAltLeaves(d, pos)), BLOCK_SET_FLAG);
                if(!d.generating && roundedDistSqr >= minShortestPlacedSqr) shortestPlaced.val = roundedDistSqr;
            }
            else return false;
        }
        return true;
    }

    protected boolean cancelBranchLeavesDirection(OctalDirection newDir, OctalDirection backDir, OctalDirection originDir) {
        return newDir == originDir || newDir == backDir.getOpposite();
    }

    protected void tickTrunkLeaves(Data d, boolean old) {
        int cutoff = getTrunkLeavesCutoff(d.height);
        WrappedInt shortestPlaced = new WrappedInt(Integer.MAX_VALUE);
        int minShortestPlaced = !d.oldTrunkLeaves.isEmpty() ? maxLeavesRadius : d.ticks; //If old leaves are shifting up, let new leaves grow fully to next stage
        for(int j = 0; j < d.trunkWood.size(); j++) {
            List<BlockPos> trunkWood = d.trunkWood.get(j);
            for(int i = 0; i <= Math.min(trunkWood.size() - 1, d.height); i++) {
                BlockPos pos = trunkWood.get(i);
                int y = pos.getY() - d.trunkPos.getY();
                if((j > 0 ? d.trunkWood.get(0).size() + i : i) < d.height - cutoff) continue;
                int radius = getTrunkLeavesRadius(y, d.height, d.maxHeight, cutoff);
                WrappedInt diagonalRadius = new WrappedInt(-1);
                for(OctalDirection dir : getInitialTrunkLeavesDirections(y, d.height)) {
                    tickTrunkLeaves(d, pos, dir.move(pos),
                            dir.isDiagonal() ? (diagonalRadius.val == -1 ? diagonalRadius.setAndGet((radius + 1) / 2) : diagonalRadius.val) : radius,
                            old, 1, minShortestPlaced, shortestPlaced, dir.getOpposite());
                }
            }
        }
    }

    protected void tickTrunkLeaves(Data d, BlockPos centerPos, BlockPos pos, int radius, boolean old, int dist, int minShortestPlaced, WrappedInt shortestPlaced, OctalDirection originDir) {
        if(dist > radius) return;
        if(!setTrunkLeavesBlock(d, pos, old, dist, minShortestPlaced, shortestPlaced)) return;
        for(OctalDirection dir : getTrunkLeavesDirections(pos.getY() - d.trunkPos.getY(), d.height)) {
            if(dir == originDir) continue;
            tickTrunkLeaves(d, centerPos, dir.move(pos), radius, old, dist + 1, minShortestPlaced, shortestPlaced, originDir);
        }
    }

    /**
     * @param old will mark leaves to be removed later
     * @return false if block is impassable
     */
    protected boolean setTrunkLeavesBlock(Data d, BlockPos pos, boolean old, int dist, int minShortestPlaced, WrappedInt shortestPlaced) {
        if(old) {
            if(!d.branchLeaves.contains(pos)) {
                BlockState state = d.level.getBlockState(pos);
                if(d.isTreeLeaves(state)) d.oldTrunkLeaves.add(pos);
            }
            return true;
        }
        else {
            if(d.simulateDetection) {
                d.trunkLeaves.add(pos);
                return true;
            }
            BlockState state = d.level.getBlockState(pos);
            if(!d.generating && !d.noPlacement && dist > shortestPlaced.val) {
                if(d.isTreeLeaves(state)) {
                    d.collectChangingLeaves(pos, state);
                }
                return false; //Only grow one layer of leaves per tick
            }
            if(state.isAir()) {
                if(!d.noPlacement) {
                    d.level.setBlock(pos, d.createLeaves(isAltLeaves(d, pos)), BLOCK_SET_FLAG);
                    d.trunkLeaves.add(pos);
                    if(!d.generating && dist >= minShortestPlaced) {
                        shortestPlaced.val = dist;
                    }
                    return true;
                }
                else return false;
            }
            else if(d.isTreeLeaves(state)) {
                d.trunkLeaves.add(pos);
                d.collectChangingLeaves(pos, state);
                return true;
            }
            else return false;
        }
    }

    protected void updateLeaves(Data d, BlockState state, BlockPos pos) {
        if(!d.noPlacement) {
            boolean alt = isAltLeaves(d, pos);
            if(state.getValue(TreeBranchesBlock.ALTERNATE) != alt) {
                d.level.setBlock(pos, state.setValue(TreeBranchesBlock.ALTERNATE, alt), BLOCK_SET_FLAG);
            }
        }
    }

    protected boolean isAltLeaves(Data d, BlockPos pos) {
        return pos.getY() % 2 == 0;
    }

    protected OctalDirection[] getInitialTrunkLeavesDirections(int y, int height) {
        return y == height ? OctalDirection.CARDINALS_UP : OctalDirection.CARDINALS;
    }

    protected OctalDirection[] getTrunkLeavesDirections(int y, int height) {
        return OctalDirection.CARDINALS;
    }

    protected OctalDirection[] getBranchLeavesDirections(Data d) {
        return OctalDirection.STRAIGHTS;
    }

    protected int getTrunkLeavesCutoff(int height) {
        return height / 2 + 1;
    }

    protected int getTrunkLeavesRadius(int y, int height, int maxHeight, int cutoff) {
        return ((height < baseHeight && y == height) || y == height - cutoff + 1) ? Math.max(1, maxLeavesRadius - 1) : maxLeavesRadius;
    }

    protected int getLeavesRadius(int height) {
        if(height < averageHeight) return Math.max(1, maxLeavesRadius - 1);
        return maxLeavesRadius;
    }
}
