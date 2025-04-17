package frostnox.nightfall.entity.ai.pathfinding;

import frostnox.nightfall.block.ICustomPathfindable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class Node {
    private static final float SQRT2 = 1.4142F, SQRT2_MINUS_1 = SQRT2 - 1F, SQRT2_MINUS_2 = SQRT2 - 2F, SQRT3_MINUS_SQRT2 = 1.73205F - SQRT2;
    private static final List<AABB> defaultFloor = Shapes.block().toAabbs();

    public final int x, y, z; //World block position
    public float pathX, pathY, pathZ; //Pathfinding position
    public float accumulatedCost; //g-score, actual cost from start to this node
    public float goalCost; //h-score, estimated cost to goal determined by heuristic function (typically distance)
    public float terrainCost; //Additional cost based on block used to compute f-score
    public float totalCost; //f-score, total estimated cost of path from start to goal that includes this node (summation of other costs)
    public NodeType type = NodeType.CLOSED;
    public boolean partial = false; //True if block is not a full shape
    public boolean mineable = false; //True if block should be destroyed by path entity
    public BlockState state;
    public @Nullable ICustomPathfindable pathfindable; //Block of ICustomPathfindable for this node
    public List<AABB> floorShape;
    protected boolean fullFloor = true;
    protected final BlockPos blockPos;
    protected boolean closed = false; //Included in closed set if true
    protected int heapIndex = -1; //Included in open set if >= 0
    protected @Nullable Node prev;
    private final int hash;

    public Node(int x, int y, int z, int hash, PathNavigationRegion region) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pathX = x + 0.5F;
        this.pathY = y;
        this.pathZ = z + 0.5F;
        this.hash = hash;
        this.blockPos = new BlockPos(x, y, z);
        this.floorShape = defaultFloor.stream().map(box -> box.move(blockPos)).collect(Collectors.toList());
        this.state = region.getBlockState(blockPos);
        if(state.getBlock() instanceof ICustomPathfindable pathfindable) this.pathfindable = pathfindable;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof Node node) return hash == node.hash;
        else return false;
    }

    @Override
    public String toString() {
        if(state != null) return type.toString() + " " + state.toString() + " at " + pathX + ", " + pathY + ", " + pathZ;
        else return type.toString() + " at " + pathX + ", " + pathY + ", " + pathZ;
    }

    public float getXPartial() {
        return pathX - x;
    }

    public float getYPartial() {
        return pathY - y;
    }

    public float getZPartial() {
        return pathZ - z;
    }

    public boolean inOpenSet() {
        return heapIndex >= 0;
    }

    public void setType(NodeType type) {
        this.type = type;
        terrainCost = type.cost;
    }

    public void setTypeMaxCost(NodeType type) {
        this.type = type;
        if(type.cost > terrainCost) terrainCost = type.cost;
    }

    public Vec3 getPathPos() {
        return new Vec3(pathX, pathY, pathZ);
    }

    /**
     * Set path and mark node as partial block
     */
    public void setPartialPath(float x, float y, float z) {
        this.pathX = x;
        this.pathY = y;
        this.pathZ = z;
        partial = true;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public @Nullable Node getPrevious() {
        return prev;
    }

    public void setFloor(List<AABB> shape) {
        this.floorShape = shape.stream().map(box -> box.move(blockPos)).collect(Collectors.toList());
        fullFloor = false;
    }

    public boolean hasFullFloor() {
        return fullFloor;
    }

    public float dist(Node node) {
        float dX = this.pathX - node.pathX;
        float dY = this.pathY - node.pathY;
        float dZ = this.pathZ - node.pathZ;
        return (float) Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public float distManhattan(Node node) {
        float dX = this.pathX - node.pathX;
        float dY = this.pathY - node.pathY;
        float dZ = this.pathZ - node.pathZ;
        return Math.abs(dX) + Math.abs(dY) + Math.abs(dZ);
    }

    public float distChebyshev(Node node) {
        float dX = Math.abs(this.pathX - node.pathX);
        float dY = Math.abs(this.pathY - node.pathY);
        float dZ = Math.abs(this.pathZ - node.pathZ);
        return dX > dZ ? (dX > dY ? dX : dY) : (dZ > dY ? dZ : dY);
    }

    /**
     * As a heuristic, this function most closely represents the movement of most mobs since they only move in 8 directions when following a path.
     */
    public float distOctile(Node node) {
        float[] minToMax = new float[] {Math.abs(this.pathY - node.pathY), Math.abs(this.pathX - node.pathX), Math.abs(this.pathZ - node.pathZ)};
        if(minToMax[0] < minToMax[1]) {
            if(minToMax[0] > minToMax[2]) {
                float temp = minToMax[2];
                minToMax[2] = minToMax[1];
                minToMax[1] = temp;
            }
        }
        else {
            if(minToMax[1] < minToMax[2]) {
                float temp = minToMax[0];
                minToMax[0] = minToMax[1];
                minToMax[1] = temp;
            }
            else {
                float temp = minToMax[2];
                minToMax[2] = minToMax[0];
                minToMax[0] = temp;
            }
        }
        if(minToMax[2] < minToMax[1]) {
            float temp = minToMax[2];
            minToMax[2] = minToMax[1];
            minToMax[1] = temp;
        }
        return (SQRT3_MINUS_SQRT2) * minToMax[0] + (SQRT2_MINUS_1) * minToMax[1] + minToMax[2];
    }

    public float horizontalDistOctile(Node node) {
        float dX = Math.abs(this.pathX - node.pathX);
        float dZ = Math.abs(this.pathZ - node.pathZ);
        return (dX + dZ) + SQRT2_MINUS_2 * Math.min(dX, dZ);
    }

    /**
     * Octile distance function that excludes diagonals for the y value, giving a bias to the x/z values.
     * Best suited for entities that move faster horizontally than vertically.
     */
    public float distDiagonalXZ(Node node) {
        float dX = Math.abs(this.x - node.x);
        float dY = Math.abs(this.y - node.y);
        float dZ = Math.abs(this.z - node.z);
        return dX > dZ ? ((dX - dZ + SQRT2 * dZ) + dY) : ((dZ - dX + SQRT2 * dX) + dY);
    }
}
