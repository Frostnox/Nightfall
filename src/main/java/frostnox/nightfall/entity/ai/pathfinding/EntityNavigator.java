package frostnox.nightfall.entity.ai.pathfinding;

import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.world.EntityPathNavigationRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Stream;

public abstract class EntityNavigator extends PathNavigation {
    public final ActionableEntity entity;
    public final NodeManager nodeManager;
    protected @Nullable ReversePath activePath;
    protected float pathAccuracy, maxNodeCost;
    protected @Nullable BlockPos activeGoalPos;
    protected @Nullable Entity cachedTarget; //Only present when a path is being calculated
    protected double lastActiveX, lastActiveZ;
    protected int stuckTicks = 0;
    protected int ticksOnCurrentNode = 0;

    public EntityNavigator(NodeManager nodeManager, Level level) {
        super(nodeManager.entity, level);
        this.entity = nodeManager.entity;
        this.nodeManager = nodeManager;
        lastActiveX = entity.getX();
        lastActiveZ = entity.getZ();
    }

    protected abstract float heuristic(Node from, Node to);

    protected boolean isGoalInvalid(Node goal) {
        return false;
    }

    public @Nullable ReversePath getActivePath() {
        return activePath;
    }

    public float getAdjustedTicksOnCurrentNode() {
        if(activePath == null || !activePath.isActive()) return ticksOnCurrentNode;
        else return ticksOnCurrentNode / (1 + Math.max(0, activePath.getCurrentNode().terrainCost));
    }

    @Override
    public void tick() {
        tick++;
        if(hasDelayedRecomputation) recomputePath();

        if(!isDone()) {
            ticksOnCurrentNode++;
            if(canUpdatePath()) updatePath();
            if(!isDone()) {
                Vec3 pathPos = activePath.getCurrentNode().getPathPos();
                mob.getMoveControl().setWantedPosition(pathPos.x, pathPos.y, pathPos.z, speedModifier);
                lastActiveX = mob.getX();
                lastActiveZ = mob.getZ();
            }
        }
        else {
            ticksOnCurrentNode = 0;
            stuckTicks = 0;
        }
    }

    @Override
    public boolean isDone() {
        return activePath == null || !activePath.isActive();
    }

    @Override
    public void stop() {
        activePath = null;
        activeGoalPos = null;
    }

    @Override
    public boolean shouldRecomputePath(BlockPos pos) {
        if(hasDelayedRecomputation) return false;
        else if(activePath != null && activePath.isActive()) {
            Node node = activePath.getEndNode();
            Vec3 averagePos = new Vec3((node.pathX + mob.getX()) / 2D, (node.pathY + mob.getY()) / 2D, (node.pathZ + mob.getZ()) / 2D);
            return pos.closerToCenterThan(averagePos, activePath.getIndex());
        }
        else return false;
    }

    @Override
    public void recomputePath() {
        if(level.getGameTime() - timeLastRecompute > 15L) {
            if(activePath != null) {
                activePath = findPath(activePath.getEndNode().blockPos, pathAccuracy);
                timeLastRecompute = level.getGameTime();
                hasDelayedRecomputation = false;
            }
        }
        else hasDelayedRecomputation = true;
    }

    protected void updatePath() {
        maxDistanceToWaypoint = entity.getNavigatorWaypointDist() * (float) speedModifier;
        Node node = activePath.getCurrentNode();
        if(Math.abs(lastActiveX - mob.getX()) < 0.001 && Math.abs(lastActiveZ - mob.getZ()) < 0.001) {
            stuckTicks++;
            if(stuckTicks > 40) {
                recomputePath();
                stuckTicks = 0;
                return;
            }
        }
        Vec3 mobPos = getTempMobPos();
        if(mobPos.y > node.pathY || Math.abs(mobPos.y - node.pathY) < mob.getStepHeight()) {
            double dX = Math.abs(mobPos.x - node.pathX);
            double dZ = Math.abs(mobPos.z - node.pathZ);
            if(dX <= maxDistanceToWaypoint && dZ <= maxDistanceToWaypoint) {
                activePath.advanceIndex();
                ticksOnCurrentNode = 0;
            }
        }
    }

    @Override
    protected void trimPath() {

    }

    @Override
    public boolean canFloat() {
        return nodeManager.canSwim;
    }

    @Override
    public boolean isStableDestination(BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState state = level.getBlockState(belowPos);
        return state.isSolidRender(level, belowPos) || !state.getFluidState().isEmpty();
    }

    @Override
    public boolean moveTo(double x, double pY, double z, double pSpeed) {
        return moveTo(x, pY, z, pSpeed, 1);
    }

    @Override
    public boolean moveTo(Entity pEntity, double pSpeed) {
        return moveTo(pEntity, pSpeed, 1);
    }

    public boolean moveTo(double x, double pY, double z, double pSpeed, int accuracy) {
        return moveTo(findPath(x, pY, z, accuracy), pSpeed);
    }

    public boolean moveTo(Entity pEntity, double pSpeed, int accuracy) {
        ReversePath path = findPath(pEntity, accuracy);
        return path != null && moveTo(path, pSpeed);
    }

    public boolean moveTo(@Nullable ReversePath path, double pSpeed) {
        if(path == null) {
            activePath = null;
            return false;
        }
        else {
            activePath = path;
            if(isDone()) return false;
            else {
                trimPath();
                if(activePath.getSize() <= 0) return false;
                else {
                    speedModifier = pSpeed;
                    return true;
                }
            }
        }
    }

    public @Nullable Node getCurrentNode() {
        if(isDone()) return null;
        else return activePath.getCurrentNode();
    }

    public @Nullable Node getNextNode() {
        if(isDone() || activePath.getIndex() < 1) return null;
        else return activePath.getNode(activePath.getIndex() - 1);
    }

    public @Nullable ReversePath findPath(double x, double y, double z, float accuracy) {
        return findPath(new BlockPos(x, y, z), accuracy);
    }

    public @Nullable ReversePath findPath(BlockPos goalPos, float accuracy) {
        return findPath(goalPos, null, 8, (float)mob.getAttributeValue(Attributes.FOLLOW_RANGE), false, accuracy);
    }

    public @Nullable ReversePath findPath(Entity goal, float accuracy) {
        return findPath(goal.blockPosition(), goal, 16, (float)mob.getAttributeValue(Attributes.FOLLOW_RANGE), true, accuracy);
    }

    public @Nullable ReversePath findPath(BlockPos goalPos, @Nullable Entity target, int regionOffset, float regionRange, boolean offsetUp, float accuracy) {
        if(mob.getY() < (double)level.getMinBuildHeight() || !canUpdatePath()) return null;
        //else if(activePath != null && activePath.isActive() && goalPos.equals(activeGoalPos)) return activePath;
        else {
            level.getProfiler().push("pathfind");
            BlockPos pos = offsetUp ? mob.blockPosition().above() : mob.blockPosition();
            int i = (int) (regionRange + regionOffset);
            EntityPathNavigationRegion region = new EntityPathNavigationRegion(level, pos.offset(-i, -i, -i), pos.offset(i, i, i));
            nodeManager.setupPath(region);
            cachedTarget = target;
            maxNodeCost = entity.getMaxNodeCost();
            ReversePath path = findPath(region.getProfiler(), goalPos, accuracy);
            cachedTarget = null;
            nodeManager.cleanupPath();
            level.getProfiler().pop();
            if(path != null && path.reachesGoal()) {
                Node endNode = path.getEndNode();
                activeGoalPos = new BlockPos(endNode.x, endNode.y, endNode.z);
                pathAccuracy = accuracy;
                resetStuckTimeout();
            }
            return path;
        }
    }

    /**
     * Implementation of A* search algorithm with nodes using floats instead of integers for their positions.
     * @param accuracy minimum distance from the gaol for a node to succeed (0 is perfect match)
     * @return best path to goal
     */
    protected ReversePath findPath(ProfilerFiller profiler, BlockPos goalPos, float accuracy) {
        profiler.push("find_path");
        profiler.markForCharting(MetricCategory.PATH_FINDING);

        Node start = nodeManager.getStartNode(), goal = nodeManager.getGoalNode(goalPos, cachedTarget);

        BinaryHeap openSet = new BinaryHeap(128);
        start.goalCost = heuristic(start, goal);
        start.totalCost =  start.goalCost;
        if(isGoalInvalid(goal)) return new ReversePath(start, false);
        Node bestNode = start;
        openSet.push(start);
        int visits = 0;
        Node[] neighbors = new Node[32];

        while(!openSet.isEmpty()) {
            visits++;
            if(visits >= 512) break;

            Node current = openSet.pop();
            current.closed = true;
            if(current.distOctile(goal) <= accuracy) {
                profiler.pop();
                return new ReversePath(current, true);
            }
            else if(current.totalCost > maxNodeCost) continue;

            int neighborsSize = nodeManager.findNeighbors(current, neighbors);
            for(int i = 0; i < neighborsSize; i++) {
                Node neighbor = neighbors[i];
                float newAccumulatedCost = current.accumulatedCost + heuristic(neighbor, current) + neighbor.terrainCost;
                if(!neighbor.inOpenSet() || newAccumulatedCost < neighbor.accumulatedCost) {
                    neighbor.prev = current;
                    neighbor.accumulatedCost = newAccumulatedCost;
                    neighbor.goalCost = heuristic(neighbor, goal);
                    if(neighbor.inOpenSet()) openSet.modifyValue(neighbor, newAccumulatedCost + neighbor.goalCost);
                    else {
                        neighbor.totalCost = newAccumulatedCost + neighbor.goalCost;
                        openSet.push(neighbor);
                    }
                    if(neighbor.totalCost <= maxNodeCost && neighbor.goalCost < bestNode.goalCost) bestNode = neighbor;
                }
            }
        }

        profiler.pop();
        return new ReversePath(bestNode, false);
    }

    private static class BinaryHeap {
        private Node[] heap;
        private int size;

        BinaryHeap(int capacity) {
            heap = new Node[capacity];
            size = 0;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public void push(Node data) {
            if(size == heap.length) {
                Node[] temp = new Node[size << 1];
                System.arraycopy(heap, 0, temp, 0, size);
                heap = temp;
            }
            heap[size] = data;
            data.heapIndex = size;
            upHeapify(size);
            size++;
        }

        public Node pop() {
            Node minNode = heap[0];
            minNode.heapIndex = -1;
            size--;
            heap[0] = heap[size];
            heap[0].heapIndex = 0;
            heap[size] = null;
            if(size > 0) downHeapify(0);
            return minNode;
        }

        public void modifyValue(Node node, float totalCost) {
            float oldTotalCost = node.totalCost;
            node.totalCost = totalCost;
            if(oldTotalCost > totalCost) upHeapify(node.heapIndex);
            else downHeapify(node.heapIndex);
        }

        private void downHeapify(int pIndex) {
            int lIndex = (pIndex << 1) + 1, rIndex = lIndex + 1;
            if(lIndex >= size) return;
            Node lChild = heap[lIndex], rChild;
            float lf = lChild.totalCost, rf;
            if(rIndex >= size) {
                rChild = null;
                rf = Float.POSITIVE_INFINITY;
            }
            else {
                rChild = heap[rIndex];
                rf = rChild.totalCost;
            }
            Node parent = heap[pIndex];
            if(lf < rf) {
                if(parent.totalCost < lf) return;
                parent.heapIndex = lIndex;
                lChild.heapIndex = pIndex;
                heap[pIndex] = lChild;
                heap[lIndex] = parent;
                downHeapify(lIndex);
            }
            else {
                if(parent.totalCost < rf) return;
                parent.heapIndex = rIndex;
                rChild.heapIndex = pIndex;
                heap[pIndex] = rChild;
                heap[rIndex] = parent;
                downHeapify(rIndex);
            }
        }

        private void upHeapify(int cIndex) {
            int pIndex = (cIndex - 1) >> 1;
            while(cIndex != 0) {
                Node child = heap[cIndex], parent = heap[pIndex];
                if(child.totalCost < parent.totalCost) {
                    parent.heapIndex = cIndex;
                    child.heapIndex = pIndex;
                    heap[cIndex] = parent;
                    heap[pIndex] = child;
                }
                else break;
                cIndex = pIndex;
                pIndex = (pIndex - 1) >> 1;
            }
        }
    }

    @Override @Nullable @Deprecated
    protected PathFinder createPathFinder(int p_26531_) {
        return null;
    }

    @Override @Nullable @Deprecated
    protected Path createPath(Set<BlockPos> p_148223_, int p_148224_, boolean p_148225_, int p_148226_, float p_148227_) {
        return null;
    }

    @Override @Nullable @Deprecated
    public Path createPath(Stream<BlockPos> pTargets, int pAccuracy) {
        return null;
    }

    @Override @Nullable @Deprecated
    public Path createPath(Set<BlockPos> pPositions, int pDistance) {
        return null;
    }

    @Override @Nullable @Deprecated
    public Path createPath(BlockPos pos, int pAccuracy) {
        return null;
    }

    @Override @Nullable @Deprecated
    public Path createPath(BlockPos p_148219_, int p_148220_, int p_148221_) {
        return null;
    }

    @Override @Nullable @Deprecated
    public Path createPath(Entity p_26534_, int p_26535_) {
        return null;
    }

    @Override @Nullable @Deprecated
    public Path getPath() {
        return null;
    }

    @Override @Nullable @Deprecated
    public NodeEvaluator getNodeEvaluator() {
        return null;
    }

    @Override @Deprecated
    public void setCanFloat(boolean pCanSwim) {

    }
}