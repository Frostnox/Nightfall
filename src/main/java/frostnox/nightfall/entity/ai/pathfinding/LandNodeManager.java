package frostnox.nightfall.entity.ai.pathfinding;

import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.data.Vec2f;
import frostnox.nightfall.util.math.OctalDirection;
import it.unimi.dsi.fastutil.floats.Float2FloatMap;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class LandNodeManager extends NodeManager {
    protected static final float DRAG = 0.02F;
    protected static final float JUMP_SCALE = 0.97F; //Scale jump height down a bit since AI won't be doing perfect jumps
    protected static final float ONE_MINUS_DRAG = 1F - DRAG;
    protected static final double LOG_ONE_MINUS_DRAG = Math.log(ONE_MINUS_DRAG);
    protected final Float2FloatMap jumpHeights = new Float2FloatOpenHashMap(4); //Maximum height reached from a jump given a block jump factor

    public LandNodeManager(ActionableEntity entity) {
        super(entity, PathComputationType.LAND);
    }

    //Jump height formula simplified from https://github.com/OrHy3/MinecraftMotionTools
    protected float calculateJumpHeight(float jumpFactor) {
        double v = entity.getBaseJumpPower() * jumpFactor + entity.getJumpBoostPower();
        double g = entity.getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
        g *= ONE_MINUS_DRAG;
        double t = v * DRAG + g;
        t = g / t;
        t = Math.ceil(Math.log(t) / LOG_ONE_MINUS_DRAG);
        return (float) (((v * DRAG + g) * (1 - Math.pow(ONE_MINUS_DRAG, t)) / DRAG - g * t) / DRAG) * JUMP_SCALE;
    }

    @Override
    protected void cleanupPath() {
        super.cleanupPath();
        jumpHeights.clear();
    }

    @Override
    public NodeType getFullNodeType(BlockGetter level, int x, int y, int z, ActionableEntity entity) {
        EnumSet<NodeType> typeSet = EnumSet.noneOf(NodeType.class);
        BlockPos.MutableBlockPos tempPos = new BlockPos.MutableBlockPos();
        tempPos.setY(y - 1);
        for(int xI = -(scanWidth - 1); xI < scanWidth; xI++) {
            int xC = x + xI;
            tempPos.setX(xC);
            for(int zI = -(scanWidth - 1); zI < scanWidth; zI++) {
                int zC = z + zI;
                tempPos.setZ(zC);
                BlockState lastState = level.getBlockState(tempPos);
                ICustomPathfindable lastPathfindable = lastState.getBlock() instanceof ICustomPathfindable pathfindable ? pathfindable : null;
                float partialX = 0.5F, partialZ = 0.5F;
                int alignmentIndex = 0, partialIndex = 0, mineableIndex = 0;
                for(int j = 0; j < scanHeight; j++) {
                    int yC = y + j;
                    Node columnNode = getNode(xC, yC, zC);
                    NodeType columnType = getSingleNodeType(level, xC, yC, zC);
                    //Align and retype walkable nodes with partial floors if necessary
                    boolean shift = false;
                    if(columnType.walkable && columnType != NodeType.BUILDABLE_WALKABLE) {
                        if(lastPathfindable != null) {
                            List<AABB> boxes = lastPathfindable.getTopFaceShape(lastState);
                            float xPartial = Math.abs(columnNode.getXPartial()), zPartial = Math.abs(columnNode.getZPartial());
                            shift = true;
                            for(AABB box : boxes) {
                                //If on any of the floors, can safely exit and not shift
                                if(xPartial >= box.minX && xPartial <= box.maxX && zPartial >= box.minZ && zPartial <= box.maxZ) {
                                    shift = false;
                                    break;
                                }
                            }
                            if(shift) {
                                OctalDirection moveDirection = lastPathfindable.getDirection(lastState);
                                columnNode.pathX = Mth.clamp(columnNode.pathX + moveDirection.xStepHalf, columnNode.x, columnNode.x + 1);
                                columnNode.pathZ = Mth.clamp(columnNode.pathZ + moveDirection.zStepHalf, columnNode.z, columnNode.z + 1);
                            }
                        }
                        else if(columnNode.getYPartial() == 0F && !lastState.getMaterial().blocksMotion()) columnType = NodeType.OPEN;
                        if(j > 0) columnType = NodeType.OPEN;
                    }
                    //Record position of highest unadjusted partial block
                    if(columnNode.partial && !shift) {
                        partialX = columnNode.pathX;
                        partialZ = columnNode.pathZ;
                        alignmentIndex = j;
                    }
                    typeSet.add(columnType);
                    if(columnNode.partial) partialIndex = j;
                    if(columnNode.mineable) mineableIndex = j;
                    lastState = columnNode.state;
                    lastPathfindable = columnNode.pathfindable;
                }
                for(int j = 0; j < scanHeight; j++) {
                    Node node = getNode(xC, y + j, zC);
                    if(j < partialIndex) node.partial = true;
                    if(j < mineableIndex) node.mineable = true;
                    //Align path coordinates vertically if necessary
                    if(j < alignmentIndex) {
                        node.pathX = partialX;
                        node.pathZ = partialZ;
                    }
                }

            }
        }
        NodeType worstType = NodeType.CLOSED;
        if(typeSet.contains(NodeType.BUILDABLE_WALKABLE) && typeSet.contains(NodeType.WALKABLE)) typeSet.remove(NodeType.BUILDABLE_WALKABLE);

        for(NodeType type : typeSet) {
            if(type.cost < 0F) return type;
            //This relies on the natural ordering of EnumSet, allowing equal costs that are declared later to override earlier types
            else if(type.cost >= worstType.cost) worstType = type;
        }
        return worstType;
    }

    @Override
    public NodeType getSingleNodeType(BlockGetter level, int x, int y, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        Node node = getNode(x, y, z);
        NodeType type = getRawNodeType(level, pos, node, OctalDirection.CENTER);

        if(canMine && type.blocksMovement) {
            float destroySpeed = node.state.getDestroySpeed(region, pos);
            if(destroySpeed >= 0F) {
                node.mineable = true;
                node.pathX = x + 0.5F;
                node.pathY = y;
                node.pathZ = z + 0.5F;
                node.terrainCost = Math.max(2F, destroySpeed);
                //type = NodeType.OPEN_OR_WALKABLE; TODO: This doesn't really work, messes up floor logic plus entity never seems to choose these paths
            }
        }

        Node belowNode = getNode(pos.set(x, y - 1, z));
        boolean openFloor;
        if(belowNode.state.collisionExtendsVertically(level, pos, entity)) {
            node.pathY = Math.max(node.pathY, y + 0.5F);
            openFloor = node.getYPartial() == 0.5F;
        }
        else openFloor = node.getYPartial() == 0F;

        if(!type.blocksMovement && y > level.getMinBuildHeight() && openFloor) {
            NodeType belowType = getRawNodeType(level, pos.set(x, y - 1, z), getNode(pos), OctalDirection.DOWN);
            if(belowType.inDanger) type = belowType;
            else if(type == NodeType.OPEN_OR_WALKABLE) {
                if(!node.partial && belowType.blocksMovement && level.getBlockState(pos.set(x, y + 1, z)).getMaterial().blocksMotion() &&
                        collidesWith(getEntityBox(node.pathX, node.pathY, node.pathZ))) type = NodeType.CLOSED;
                else type = belowType.blocksMovement ? NodeType.WALKABLE : (canBuild ? NodeType.BUILDABLE_WALKABLE : NodeType.OPEN);
            }
        }

        if(type.walkable) {
            if(!type.inDanger) type = adjustNodeByNeighbors(type, pos.set(x, y, z));
            if(openFloor && belowNode.pathfindable != null) {
                node.setFloor(belowNode.pathfindable.getTopFaceShape(belowNode.state));
            }
            if(type.walkable) {
                float speedFactor;
                if(Math.abs(node.getYPartial()) > 1D/16D) speedFactor = node.state.getBlock().getJumpFactor();
                else speedFactor = belowNode.state.getBlock().getJumpFactor();
                if(speedFactor != 1F) node.terrainCost = (node.terrainCost + 1F - speedFactor) * 4F;
            }
        }

        return type;
    }

    @Override
    public Node getStartNode() {
        int y = entity.getBlockY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(entity.getX(), y, entity.getZ());
        BlockState state = region.getBlockState(pos);

        boolean inFluid = entity.isInWater() || entity.isInLava();
        //In fluid (no way to easily check for other fluid types unfortunately), search up
        if(canSwim && inFluid) {
            while(true) {
                if(state.getFluidState().isEmpty() || collidesWith(getEntityBox(entity.getX(), y, entity.getZ()))) {
                    y--;
                    break;
                }
                y++;
                state = region.getBlockState(pos.setY(y));
            }
        }
        //In air, search down
        else if(!entity.isOnGround() && !inFluid) {
            FluidState fluid = region.getFluidState(pos.setY(y));
            while(true) {
                if(!fluid.isEmpty() || collidesWith(getEntityBox(entity.getX(), y, entity.getZ())) || y < region.getMinBuildHeight()) {
                    y++;
                    break;
                }
                y--;
                fluid = region.getFluidState(pos.setY(y));
            }
        }

        BlockPos blockPos = entity.blockPosition();
        NodeType startType = getCachedNodeType(pos.set(blockPos.getX(), y, blockPos.getZ()));
        Node node = getNode(pos);
        //Look for different node if this one is invalid
        if(!startType.walkable || !canReachWithoutCollision(entity, new Vec2f(node.pathX, node.pathZ))) {
            AABB box = entity.getBoundingBox();
            double boxWidth = Math.abs(box.maxX - box.minX);
            if(boxWidth < 0.5) box = box.inflate(0.5 - boxWidth, 0, 0.5 - boxWidth); //Prevent getting stuck inside fences
            BlockPos bestPos = LevelUtil.NULL_POS;
            ObjectArraySet<BlockPos.MutableBlockPos> positions = ObjectArraySet.of(pos.mutable());
            if(!positions.contains(pos.set(box.minX, y, box.minZ))) positions.add(pos.mutable());
            if(!positions.contains(pos.set(box.minX, y, box.maxZ))) positions.add(pos.mutable());
            if(!positions.contains(pos.set(box.maxX, y, box.minZ))) positions.add(pos.mutable());
            if(!positions.contains(pos.set(box.maxX, y, box.maxZ))) positions.add(pos.mutable());
            //Try to choose from the highest walkable position, if one exists
            for(BlockPos.MutableBlockPos cPos : positions) {
                if(isWalkable(cPos)) {
                    bestPos = cPos.immutable();
                    node = typeAndGetNode(bestPos);
                    if(canReachWithoutCollision(entity, new Vec2f(node.pathX, node.pathZ))) {
                        break;
                    }
                }
                for(int i = 1; i < 4; i++) {
                    cPos.setY(cPos.getY() - 1);
                    if(cPos.getY() <= bestPos.getY()) break;
                    else if(isWalkable(cPos)) {
                        bestPos = cPos.immutable();
                        node = typeAndGetNode(bestPos);
                        if(canReachWithoutCollision(entity, new Vec2f(node.pathX, node.pathZ))) {
                            break;
                        }
                    }
                }
            }
            if(bestPos != LevelUtil.NULL_POS) {
                node = typeAndGetNode(bestPos);
                node.setType(getCachedNodeType(bestPos));
                return node;
            }
        }

        node.setType(startType);
        return node;
    }

    @Override
    public Node getGoalNode(BlockPos goalPos, @Nullable Entity target) {
        if(target == null) return typeAndGetNode(goalPos);
        BlockPos blockPos = target.blockPosition();
        int y = blockPos.getY();
        NodeType type = getCachedNodeType(blockPos.getX(), y, blockPos.getZ());
        Node entityNode = getNode(blockPos.getX(), y, blockPos.getZ());
        //Look for different node if this one is invalid
        if(type.cost < 0F || !canReachWithoutCollision(target, new Vec2f(entityNode.pathX, entityNode.pathZ))) {
            AABB box = target.getBoundingBox();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            if(isEnterable(blockPos, pos.set(box.minX, y, box.minZ)) || isEnterable(blockPos, pos.set(box.minX, y, box.maxZ)) ||
                    isEnterable(blockPos, pos.set(box.maxX, y, box.minZ)) || isEnterable(blockPos, pos.set(box.maxX, y, box.maxZ))) {
                Node node = getNode(pos);
                node.setType(getCachedNodeType(node.getBlockPos()));
                return node;
            }
        }

        entityNode.setType(type);
        return entityNode;
    }

    @Override
    protected NodeType getRawNodeType(BlockGetter level, BlockPos pos, Node node, OctalDirection direction) {
        BlockState state = node.state;
        if(isOpenType(state)) return NodeType.OPEN_OR_WALKABLE;
        else if(state.isPathfindable(level, pos, PathComputationType.LAND)) {
            if(node.pathfindable != null) {
                NodeType type;
                if(direction == OctalDirection.DOWN) type = node.pathfindable.getFloorNodeType(this, state, level, pos);
                else type = node.pathfindable.getRawNodeType(this, state, level, pos);
                return adjustAndSetNodeByEntity(type, state, pos);
            }
            else return adjustAndSetNodeByEntity(NodeType.OPEN_OR_WALKABLE, state, pos);
        }
        else return adjustAndSetNodeByEntity(NodeType.CLOSED, state, pos);
    }

    @Override
    public int findNeighbors(Node startNode, Node[] neighbors) {
        int count = 0, jumpSteps = 0;
        float floor = startNode.pathY, jumpHeight = entityStepHeight;
        NodeType type = getCachedNodeType(startNode.x, startNode.y, startNode.z);
        NodeType typeAbove = getCachedNodeType(startNode.x, startNode.y + 1, startNode.z);
        if(typeAbove.cost >= 0.0F) {
            float jumpFactor;
            if(Math.abs(floor % 1D) > 1D/16D) jumpFactor = startNode.state.getBlock().getJumpFactor();
            else jumpFactor = getNode(startNode.x, startNode.y - 1, startNode.z).state.getBlock().getJumpFactor();
            if(!jumpHeights.containsKey(jumpFactor)) {
                jumpHeight = calculateJumpHeight(jumpFactor);
                jumpHeights.put(jumpFactor, jumpHeight);
            }
            else jumpHeight = jumpHeights.get(jumpFactor);
            jumpHeight = Math.max(jumpHeight, entityStepHeight);
            jumpSteps = Mth.floor(floor % 1F + jumpHeight);
        }

        //Octal directions
        Node nodeS = findValidNode(startNode.x, startNode.y, startNode.z + 1, jumpSteps, jumpHeight, floor, OctalDirection.SOUTH, startNode);
        if(isNeighborValid(startNode, nodeS)) neighbors[count++] = nodeS;

        Node nodeW = findValidNode(startNode.x - 1, startNode.y, startNode.z, jumpSteps, jumpHeight, floor, OctalDirection.WEST, startNode);
        if(isNeighborValid(startNode, nodeW)) neighbors[count++] = nodeW;

        Node nodeE = findValidNode(startNode.x + 1, startNode.y, startNode.z, jumpSteps, jumpHeight, floor, OctalDirection.EAST, startNode);
        if(isNeighborValid(startNode, nodeE)) neighbors[count++] = nodeE;

        Node nodeN = findValidNode(startNode.x, startNode.y, startNode.z - 1, jumpSteps, jumpHeight, floor, OctalDirection.NORTH, startNode);
        if(isNeighborValid(startNode, nodeN)) neighbors[count++] = nodeN;

        //Buildable nodes, encompasses cardinal directions above and below (straight across is covered already)
        if(canBuild) {
            Node[] validNodes = new Node[count];
            System.arraycopy(neighbors, 0, validNodes, 0, count);
            //Case 1: upwards staircase
            if(typeAbove == NodeType.BUILDABLE_WALKABLE) {
                int y = startNode.y + 1;
                for(Node neighbor : validNodes) {
                    if(neighbor.y <= startNode.y) {
                        Node node = typeAndGetNode(neighbor.x, y, neighbor.z);
                        if(node.type == NodeType.BUILDABLE_WALKABLE && !node.closed) neighbors[count++] = node;
                    }
                }
            }
            //Case 2: downwards staircase
            int y = startNode.y - 1;
            for(Node neighbor : validNodes) {
                if(neighbor.y <= startNode.y && getCachedNodeType(neighbor.x, startNode.y, neighbor.z) == NodeType.BUILDABLE_WALKABLE) {
                    Node node = typeAndGetNode(neighbor.x, y, neighbor.z);
                    if(node.type == NodeType.BUILDABLE_WALKABLE && !node.closed) neighbors[count++] = node;
                }
            }
        }

        Node nodeNW = findValidNode(startNode.x - 1, startNode.y, startNode.z - 1, jumpSteps, jumpHeight, floor, OctalDirection.NORTHWEST, startNode);
        if(nodeNW != null && isDiagonalValid(startNode, nodeNW)) neighbors[count++] = nodeNW;

        Node nodeNE = findValidNode(startNode.x + 1, startNode.y, startNode.z - 1, jumpSteps, jumpHeight, floor, OctalDirection.NORTHEAST, startNode);
        if(nodeNE != null && isDiagonalValid(startNode, nodeNE)) neighbors[count++] = nodeNE;

        Node nodeSW = findValidNode(startNode.x - 1, startNode.y, startNode.z + 1, jumpSteps, jumpHeight, floor, OctalDirection.SOUTHWEST, startNode);
        if(nodeSW != null && isDiagonalValid(startNode, nodeSW)) neighbors[count++] = nodeSW;

        Node nodeSE = findValidNode(startNode.x + 1, startNode.y, startNode.z + 1, jumpSteps, jumpHeight, floor, OctalDirection.SOUTHEAST, startNode);
        if(nodeSE != null && isDiagonalValid(startNode, nodeSE)) neighbors[count++] = nodeSE;

        //If inside a block, check above in case entity can stand on top
        if(type == NodeType.WALKABLE && startNode.partial && startNode.state.getMaterial().blocksMotion()) {
            Node nodeUp = findValidNode(startNode.x, startNode.y + 1, startNode.z, jumpSteps - 1, jumpHeight, floor, OctalDirection.UP, startNode);
            if(isNeighborValid(startNode, nodeUp)) neighbors[count++] = nodeUp;
        }

        //If inside a breakable block, consider the block above in case its path is better
        else if(startNode.mineable && startNode.prev != null && !startNode.prev.mineable) {
            Node nodeUp = findValidNode(startNode.x, startNode.y + 1, startNode.z, 0, jumpHeight, floor, OctalDirection.UP, startNode);
            if(isNeighborValid(startNode, nodeUp)) neighbors[count++] = nodeUp;
        }

        //Check below if on top of a partially open or mineable block
        if(type.walkable && (!startNode.fullFloor || canMine) && floor % 1D == 0D) {
            Node nodeDown = findValidNode(startNode.x, startNode.y - 1, startNode.z, 0, jumpHeight, floor, OctalDirection.DOWN, startNode);
            if(isNeighborValid(startNode, nodeDown)) neighbors[count++] = nodeDown;
        }

        return count;
    }
    
    protected @Nullable Node findValidNode(int x, int y, int z, int jumpSteps, float jumpHeight, float fromFloor, OctalDirection dir, Node prev) {
        Node node = null;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);

        NodeType type = getCachedNodeType(x, y, z);
        float cost = type.cost;
        if(cost >= 0.0F) {
            node = getNode(x, y, z);
            node.setTypeMaxCost(type);
            if(node.pathY - fromFloor > jumpHeight) return null;
        }

        //Make sure jump is unobstructed if moving up to adjacent node with distant partial floor
        if(type.walkable && prev.type.walkable && node.y > prev.y) {
            if((node.x == prev.x && Math.abs(node.pathZ - prev.pathZ) > 0.5F) || (node.z == prev.z && Math.abs(node.pathX - prev.pathX) > 0.5F)) {
                if(collidesWith(getEntityBox((prev.pathX + node.pathX) / 2F, node.pathY, (prev.pathZ + node.pathZ) / 2F))) {
                    return findValidNode(x, y - 1, z, jumpSteps - 1, jumpHeight, fromFloor, dir, prev);
                }
            }
        }

        //Mark as open if walkable gap is too large
        if(type.walkable && prev.type.walkable && !dir.isVertical() && willEntityFall(node, prev, dir)) {
            type = NodeType.OPEN;
            node.setTypeMaxCost(type);
        }

        //Check collision when moving in/out of partially walkable spaces
        if(node != null && node.terrainCost >= 0.0F) {
            boolean prevWalkable = prev.type.walkable && prev.partial;
            if(prevWalkable || (type.walkable && node.partial)) {
                float xC, zC, scale;
                if(prevWalkable) {
                    scale = Math.max(0.5F, Math.max(Math.abs(prev.getXPartial() - 0.5F) * 2F, Math.abs(prev.getZPartial() - 0.5F) * 2F));
                    if(dir.isVertical() || Math.abs(dir.xStep * scale) > Math.abs(node.pathX - prev.pathX)) xC = node.pathX;
                    else xC = prev.pathX + dir.xStep * scale;
                    if(dir.isVertical() || Math.abs(dir.zStep * scale) > Math.abs(node.pathZ - prev.pathZ)) zC = node.pathZ;
                    else zC = prev.pathZ + dir.zStep * scale;
                }
                else {
                    scale = Math.max(0.5F, Math.max(Math.abs(node.getXPartial() - 0.5F) * 2F, Math.abs(node.getZPartial() - 0.5F) * 2F));
                    xC = node.pathX - dir.xStep * scale;
                    zC = node.pathZ - dir.zStep * scale;
                }
                //If this space can't be reached, then try using this block as a floor before discarding it
                if(collidesWith(getEntityBox(xC, Math.max(prev.pathY, node.pathY), zC))) {
                    if(jumpSteps == 0) return null;
                    type = getCachedNodeType(x, y + 1, z);
                    if(!type.walkable || collidesWith(getEntityBox(xC, y + 1, zC))) return null;
                    if(type.cost >= 0.0F) {
                        node = getNode(x, y + 1, z);
                        if(node.pathY - fromFloor > jumpHeight) return null;
                        node.setTypeMaxCost(type);
                    }
                    return node;
                }
            }
        }

        //Choose node above mineable node if it can be walked on
        if(node != null && node.mineable && dir != OctalDirection.DOWN) {
            if(jumpSteps > 0) {
                Node nodeUp = findValidNode(x, y + 1, z, jumpSteps - 1, jumpHeight, fromFloor, dir, prev);
                if(nodeUp != null && nodeUp.type.walkable && !nodeUp.mineable) {
                    double backX = (x - dir.xStep) + 0.5D;
                    double backZ = (z - dir.zStep) + 0.5D;
                    AABB box = new AABB(backX - entityWidthHalf, getFloorLevel(pos.set(backX, (y + 1), backZ)) + 1.001D,
                            backZ - entityWidthHalf, backX + entityWidthHalf,
                            (double)entityHeight + getFloorLevel(pos.set(nodeUp.x, nodeUp.y, nodeUp.z)) - 0.002D, backZ + entityWidthHalf);
                    if(!collidesWith(box)) node = nodeUp;
                }
            }
        }
        else if(!type.walkable) {
            if((node == null || node.terrainCost < 0.0F || node.partial) && jumpSteps > 0) {
                node = findValidNode(x, y + 1, z, jumpSteps - 1, jumpHeight, fromFloor, dir, prev);
                //Vanilla doesn't check if entity width is greater than 2...
                if(node != null && (node.type == NodeType.OPEN || node.type.walkable)) {
                    type = node.type;
                    if(!prev.partial) {
                        double backX = (x - dir.xStep) + 0.5D;
                        double backZ = (z - dir.zStep) + 0.5D;
                        AABB box = new AABB(backX - entityWidthHalf, getFloorLevel(pos.set(backX, (y + 1), backZ)) + 1.001D, backZ - entityWidthHalf, backX + entityWidthHalf, (double) entityHeight + getFloorLevel(pos.set(node.x, node.y, node.z)) - 0.002D, backZ + entityWidthHalf);
                        if(collidesWith(box)) node = null;
                    }
                }
            }

            if(type == NodeType.PASSABLE_FLUID && !canSwim) {
                if(getCachedNodeType(x, y - 1, z) != NodeType.PASSABLE_FLUID) return node;

                while(y > entity.level.getMinBuildHeight()) {
                    --y;
                    type = getCachedNodeType(x, y, z);
                    if(type != NodeType.PASSABLE_FLUID) return node;

                    node = getNode(x, y, z);
                    node.setTypeMaxCost(type);
                }
            }

            if(type == NodeType.OPEN) {
                int fallDist = 0;
                int i = y;

                while(type == NodeType.OPEN) {
                    --y;
                    if(y < entity.level.getMinBuildHeight()) {
                        Node minHeightNode = getNode(x, i, z);
                        minHeightNode.setType(NodeType.CLOSED);
                        return minHeightNode;
                    }

                    if(fallDist++ >= maxFallDistance) {
                        Node maxFallNode = getNode(x, y, z);
                        maxFallNode.setType(NodeType.CLOSED);
                        return maxFallNode;
                    }

                    type = getCachedNodeType(x, y, z);
                    Node fallNode = getNode(x, y, z);
                    //If partially open, must continue falling along the same position
                    if(type == NodeType.OPEN && fallNode.partial) {
                        Node aboveNode = getNode(x, y + 1, z);
                        if(aboveNode.partial && (fallNode.getXPartial() != aboveNode.getXPartial() || fallNode.getZPartial() != aboveNode.getZPartial())) {
                            fallNode.setType(NodeType.CLOSED);
                            return fallNode;
                        }
                    }
                    cost = type.cost;

                    if(type != NodeType.OPEN && cost >= 0.0F) {
                        node = getNode(x, y, z);
                        node.setTypeMaxCost(type);
                        break;
                    }

                    if(cost < 0.0F) {
                        Node closedNode = getNode(x, y, z);
                        closedNode.setType(NodeType.CLOSED);
                        return closedNode;
                    }
                }
            }
        }

        if(type == NodeType.BUILDABLE_WALKABLE && node != null) {
            Node last = node.getPrevious();
            while(last != null) {
                //Avoid building on top of a position the entity is trying to walk over
                if(node.x == last.x && node.z == last.z && last.type == NodeType.BUILDABLE_WALKABLE && node.y - scanHeight == prev.y) {
                    node.setType(NodeType.OPEN);
                    return node;
                }
                last = last.getPrevious();
            }
        }

        return node;
    }
}
