package frostnox.nightfall.entity.ai.pathfinding;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.util.DataUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.data.Vec2d;
import frostnox.nightfall.util.data.Vec2f;
import frostnox.nightfall.util.math.OctalDirection;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public abstract class NodeManager {
    private static final float CHECK_DIST = (float) Math.sqrt(Math.pow(1D/16D, 2) + Math.pow(1D/16D, 2));
    public final ActionableEntity entity;
    public final PathComputationType pathType;
    protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<>();
    protected final Int2ObjectMap<NodeType> types = new Int2ObjectOpenHashMap<>();
    protected final Object2BooleanMap<AABB> collisions = new Object2BooleanOpenHashMap<>();
    protected int scanWidth, scanHeight; //Determines size of surrounding area that each node takes into account
    protected int maxFallDistance;
    protected float entityWidthHalf, entityWidthSqr, entityHeight, entityStepHeight, entityWidthDiagonalSqr;
    public boolean canSwim, canMine, canBuild;
    protected PathNavigationRegion region;

    public NodeManager(ActionableEntity entity, PathComputationType pathType) {
        this.entity = entity;
        this.pathType = pathType;
        canSwim = true;
    }

    protected void setupPath(PathNavigationRegion region) {
        this.region = region;
        nodes.clear();
        this.entityWidthHalf = entity.getBbWidth() / 2F;
        this.entityHeight = entity.getBbHeight();
        this.entityWidthSqr = entity.getBbWidth() * entity.getBbWidth();
        this.entityWidthDiagonalSqr = entityWidthSqr + entityWidthSqr;
        this.entityStepHeight = entity.getStepHeight();
        this.scanWidth = Mth.floor(entity.getBbWidth() + 1F);
        this.scanHeight = Mth.floor(entityHeight + 1F);
        this.maxFallDistance = entity.getMaxFallDistance();
        this.canMine = entity.canMineAnything();
        this.canBuild = entity.canBuild();
    }

    protected void cleanupPath() {
        region = null;
        types.clear();
        collisions.clear();
    }

    public boolean collidesWith(AABB box) {
        return collisions.computeIfAbsent(box, (o) -> !region.noCollision(entity, box));
    }

    public AABB getEntityBox(double x, double y, double z) {
        return new AABB(x - entityWidthHalf, y + 0.001D, z - entityWidthHalf, x + entityWidthHalf, y + entityHeight - 0.002D, z + entityWidthHalf);
    }

    public AABB getEntityBox(double x, double y, double z, double ySize) {
        return new AABB(x - entityWidthHalf, y + 0.001D, z - entityWidthHalf, x + entityWidthHalf, y + ySize - 0.002D, z + entityWidthHalf);
    }

    public int getScanHeight() {
        return scanHeight;
    }

    public int getScanWidth() {
        return scanWidth;
    }

    public float getEntityHeight() {
        return entityHeight;
    }

    public float getEntityWidthHalf() {
        return entityWidthHalf;
    }

    public int getMaxBuildHeight() {
        return region.getMaxBuildHeight();
    }

    public int getMinBuildHeight() {
        return region.getMinBuildHeight();
    }

    public Node getNode(BlockPos pos) {
        return getNode(pos.getX(), pos.getY(), pos.getZ());
    }

    public Node getNode(int x, int y, int z) {
        int hash = DataUtil.hashPos(x, y, z);
        return nodes.computeIfAbsent(hash, (i) -> new Node(x, y, z, hash, region));
    }

    protected Node typeAndGetNode(BlockPos pos) {
        return typeAndGetNode(pos.getX(), pos.getY(), pos.getZ());
    }

    protected Node typeAndGetNode(int x, int y, int z) {
        NodeType type = getCachedNodeType(x, y, z);
        float cost = type.cost;
        Node node = getNode(x, y, z);
        if(cost >= 0.0F) node.setTypeMaxCost(type);
        return node;
    }

    protected boolean willEntityFall(Node cur, Node prev, OctalDirection dir) {
        if(cur.hasFullFloor() && prev.hasFullFloor()) return false;

        float xStep = cur.pathX - prev.pathX, xStepBack = -xStep;
        float zStep = cur.pathZ - prev.pathZ, zStepBack = -zStep;
        double size = dir.isDiagonal() ? entityWidthDiagonalSqr : entityWidthSqr;
        for(AABB prevBox : prev.floorShape) {
            //Line equation from previous to current
            Double m;
            double b;
            if(cur.pathX == prev.pathX) {
                m = null;
                b = prev.pathX;
            }
            else {
                m = (double) zStep / xStep;
                b = prev.pathZ - m * prev.pathX;
            }
            Vec2d prevPoint = findIntersectionPoint(m, b, prevBox, xStep, zStep);
            for(AABB curBox : cur.floorShape) {
                Vec2d curPoint = findIntersectionPoint(m, b, curBox, xStepBack, zStepBack);
                if(prevPoint.distSqr(curPoint) < size) return false;
            }
        }
        return true;
    }

    private static Vec2d findIntersectionPoint(Double m, double b, AABB box, float xStep, float zStep) {
        if(m == null) {
            //Must be north or south since x coords are equal
            if(zStep > 0) return new Vec2d(b, box.maxZ);
            else return new Vec2d(b, box.minZ);
        }
        Double xEdge = null, zEdge = null;
        //Can only intersect two possible edges
        if(xStep > 0) xEdge = box.maxX;
        else if(xStep < 0) xEdge = box.minX;
        if(xEdge != null) {
            double z = m * xEdge + b;
            if(z >= box.minZ && z <= box.maxZ) return new Vec2d(xEdge, z);
        }
        if(zStep > 0) zEdge = box.maxZ;
        else if(zStep < 0) zEdge = box.minZ;
        if(zEdge != null) {
            double x = (zEdge - b) / m;
            if(x >= box.minX && x <= box.maxX) return new Vec2d(x, zEdge);
        }
        Nightfall.LOGGER.error("NodeManager failed to find intersection point in box " + box.toString());
        return Vec2d.ZERO; //Should never reach here
    }

    protected boolean canEntityMoveDiagonally(BlockPos.MutableBlockPos pos1, BlockPos.MutableBlockPos pos2) {
        double minDistSqr = Double.POSITIVE_INFINITY;
        float baseY1 = pos1.getY(), baseY2 = pos2.getY();
        for(int i = 0; i < scanHeight; i++) {
            for(AABB box1 : region.getBlockState(pos1).getCollisionShape(region, pos1).move(pos1.getX(), pos1.getY(), pos1.getZ()).toAabbs()) {
                if(box1.minY - baseY1 > entityHeight) continue;
                for(AABB box2 : region.getBlockState(pos2).getCollisionShape(region, pos2).move(pos2.getX(), pos2.getY(), pos2.getZ()).toAabbs()) {
                    if(box2.minY - baseY2 > entityHeight) continue;
                    double dist = MathUtil.getShortestDistanceSqrBoxToBox(box1, box2);
                    if(dist < minDistSqr) minDistSqr = dist;
                }
            }
            pos1.setY(pos1.getY() + 1);
            pos2.setY(pos2.getY() + 1);
        }
        if(minDistSqr == Double.POSITIVE_INFINITY) return true;
        else return minDistSqr >= entityWidthDiagonalSqr;
    }

    protected boolean isNeighborValid(Node startNode, @Nullable Node node) {
        return node != null && !node.closed && (node.terrainCost >= 0.0F || startNode.terrainCost < 0.0F)
                && (node.y <= startNode.y || (Math.abs(node.pathX - startNode.pathX) < 1.5F && Math.abs(node.pathZ - startNode.pathZ) < 1.5F));
    }

    protected boolean isDiagonalValid(Node startNode, Node node) {
        if(node.closed || node.terrainCost < 0F || (node.y > startNode.y &&
                (Math.abs(node.pathX - startNode.pathX) >= 1.5F || Math.abs(node.pathZ - startNode.pathZ) >= 1.5F))) return false;
        int diagonalY = Math.max(startNode.y, node.y);
        Node xNode = typeAndGetNode(node.x, diagonalY, startNode.z);
        Node zNode = typeAndGetNode(startNode.x, diagonalY, node.z);
        if((xNode.partial && zNode.partial) || (!xNode.type.blocksMovement && !zNode.type.blocksMovement)) {
            if(zNode.partial || xNode.partial) {
                if(entityWidthDiagonalSqr >= 1D) return false;
                return canEntityMoveDiagonally(xNode.blockPos.mutable(), zNode.blockPos.mutable());
            }
            else return !xNode.mineable || !zNode.mineable;
        }
        return false;
    }

    /**
     * Note that this function makes many collision checks and is best used with short distances
     */
    protected boolean canReachWithoutCollision(Entity entity, Vec2f pos) {
        float x = (float) entity.getX(), z = (float) entity.getZ();
        Vec2f dist = pos.subtract(x, z);
        dist = dist.scale((1F / dist.length()) * CHECK_DIST);

        AABB tempBox = entity.getBoundingBox();
        AABB box = new AABB(tempBox.minX, tempBox.minY + 0.001D, tempBox.minZ, tempBox.maxX, tempBox.maxY - 0.002D, tempBox.maxZ);
        //AABB box = getEntityBox(entity.getX(), entity.getY(), entity.getZ());
        while(pos.distSqr(x, z) > CHECK_DIST * CHECK_DIST) {
            box = box.move(dist.x(), 0, dist.y());
            if(collidesWith(box)) return false;
            x += dist.x();
            z += dist.y();
        }

        return true;
    }

    protected NodeType adjustNodeByNeighbors(NodeType type, BlockPos.MutableBlockPos pos) {
        int xCenter = pos.getX(), yCenter = pos.getY(), zCenter = pos.getZ();
        for(OctalDirection direction : OctalDirection.CARDINALS) {
            if(entity.adjustPathType(type, getNode(pos.set(xCenter + direction.xStepInt, yCenter, zCenter + direction.zStepInt)).state).inDanger) {
                return NodeType.NEAR_DANGER;
            }
        }
        return type;
    }

    protected NodeType adjustAndSetNodeByEntity(NodeType type, BlockState state, BlockPos pos) {
        type = entity.adjustPathType(type, state);
        if(type == NodeType.PASSABLE_FLUID) {
            Node node = getNode(pos);
            if(node.partial) node.pathY = Math.max(node.pathY, node.y + state.getFluidState().getOwnHeight());
            else node.pathY = node.y + state.getFluidState().getOwnHeight();
        }
        return type;
    }

    protected double getFloorLevel(BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = region.getBlockState(belowPos);
        if(!belowState.getFluidState().isEmpty()) return pos.getY();
        VoxelShape shape = region.getBlockState(belowPos).getCollisionShape(region, belowPos);
        return (double)belowPos.getY() + (shape.isEmpty() ? 0.0D : shape.max(Direction.Axis.Y));
    }

    protected boolean isWalkable(BlockPos pos) {
        NodeType type = getCachedNodeType(pos);
        return type.walkable;
    }

    protected boolean isEnterable(BlockPos invalidPos, BlockPos pos) {
        return (invalidPos.getX() != pos.getX() || invalidPos.getZ() != pos.getZ()) && getCachedNodeType(pos).cost >= 0F;
    }

    public boolean isOpenType(BlockState state) {
        return state.isAir();
    }

    protected abstract NodeType getRawNodeType(BlockGetter level, BlockPos pos, Node node, OctalDirection direction);

    public abstract NodeType getFullNodeType(BlockGetter level, int x, int y, int z, ActionableEntity entity);

    /**
     * @return node type independent of entity
     */
    public abstract NodeType getSingleNodeType(BlockGetter level, int x, int y, int z);

    /**
     * Be careful not to use this inside any of the node type logic since recursive calls cause inconsistent crashes
     */
    protected NodeType getCachedNodeType(int x, int y, int z) {
        return types.computeIfAbsent(DataUtil.hashPos(x, y, z), (i) -> getFullNodeType(region, x, y, z, entity));
    }

    protected NodeType getCachedNodeType(BlockPos pos) {
        return getCachedNodeType(pos.getX(), pos.getY(), pos.getZ());
    }

    public abstract Node getStartNode();

    public abstract Node getGoalNode(BlockPos goalPos, @Nullable Entity target);

    /**
     * @return amount of neighbors found
     */
    public abstract int findNeighbors(Node startNode, Node[] neighbors);
}
