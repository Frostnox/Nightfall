package frostnox.nightfall.block;

import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.util.math.OctalDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.List;

public interface ICustomPathfindable {
    OctalDirection[] GAP_DIRECTIONS = new OctalDirection[] {OctalDirection.CENTER, OctalDirection.NORTH, OctalDirection.WEST, OctalDirection.SOUTH, OctalDirection.EAST};
    List<AABB> NO_BOXES = List.of();
    List<AABB> FULL_BOXES = Shapes.block().toAabbs();

    /**
     * Should also set custom path position here if necessary
     * @return direction independent type to hold in cache
     */
    NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos);

    /**
     * @return type when using as a centered floor
     */
    NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos);

    List<AABB> getTopFaceShape(BlockState state);

    List<AABB> getBottomFaceShape(BlockState state);

    /**
     * @return most prominent direction of shape
     */
    default OctalDirection getDirection(BlockState state) {
        return OctalDirection.CENTER;
    }

    /**
     * @return node type for a block that has an open center and completely occludes the opposite face of a block in a given direction (ex. doors)
     */
    default NodeType getTypeForThinSideClosedShape(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos, OctalDirection dir, float centerOffset) {
        //Check center then opposite edge
        float x = pos.getX() + 0.5F + dir.xStepInt * centerOffset, y = pos.getY(), z = pos.getZ() + 0.5F + dir.zStepInt * centerOffset;
        if(nodeManager.collidesWith(nodeManager.getEntityBox(x, y, z, 1D))) {
            x = pos.getX() + 0.5F + dir.xStepHalf;
            z = pos.getZ() + 0.5F + dir.zStepHalf;
            if(nodeManager.collidesWith(nodeManager.getEntityBox(x, y, z, 1D))) {
                nodeManager.getNode(pos).partial = true;
                return NodeType.CLOSED;
            }
        }
        nodeManager.getNode(pos).setPartialPath(x, y, z);
        return hasAnyFloorAt(state, pos, level) ? NodeType.WALKABLE : NodeType.OPEN;
    }

    /**
     * @return node type for a block that has a closed center and completely occludes the opposite face of a block in a given direction (ex. sidings)
     */
    default NodeType getTypeForSideClosedShape(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos, OctalDirection dir) {
        //Check opposite edge
        float y = pos.getY();
        float x = pos.getX() + 0.5F + dir.xStepHalf;
        float z = pos.getZ() + 0.5F + dir.zStepHalf;
        if(nodeManager.collidesWith(nodeManager.getEntityBox(x, y, z, 1D))) {
            nodeManager.getNode(pos).partial = true;
            return NodeType.CLOSED;
        }
        nodeManager.getNode(pos).setPartialPath(x, y, z);
        return hasAnyFloorAt(state, pos, level) ? NodeType.WALKABLE : NodeType.OPEN;
    }

    /**
     * @return node type for a block that completely occludes the bottom face of the block above (ex. top slabs)
     */
    default NodeType getTypeForTopClosedShape(NodeManager nodeManager, BlockPos pos, float ySpace) {
        nodeManager.getNode(pos).partial = true;
        return nodeManager.getEntityHeight() <= ySpace ? NodeType.OPEN_OR_WALKABLE : NodeType.CLOSED; //All collision checks failed
    }

    /**
     * @return node type for a block against the top face of the block below (ex. chests)
     */
    default NodeType getTypeForCenteredBottomShape(NodeManager nodeManager, BlockPos pos, float yOffset) {
        float x = pos.getX() + 0.5F, y = pos.getY() + yOffset, z = pos.getZ() + 0.5F;
        if(!nodeManager.collidesWith(nodeManager.getEntityBox(x, y, z))) {
            nodeManager.getNode(pos).setPartialPath(x, y, z);
            return NodeType.WALKABLE;
        }
        else {
            nodeManager.getNode(pos).partial = true;
            return NodeType.CLOSED;
        }
    }

    /**
     * @return node type for a block that completely occludes the top face of the block below (ex. bottom slabs)
     */
    default NodeType getTypeForBottomClosedShape(NodeManager nodeManager, BlockGetter level, BlockPos pos, float yOffset) {
        BlockPos.MutableBlockPos abovePos = pos.mutable();
        //Exit early if a full block is in the way
        for(int i = 1; i < nodeManager.getScanHeight(); i++) {
            abovePos.setY(abovePos.getY() + 1);
            if(abovePos.getY() >= nodeManager.getMaxBuildHeight()) break;
            if(!level.getBlockState(abovePos).isPathfindable(level, abovePos, nodeManager.pathType)) {
                nodeManager.getNode(pos).partial = true;
                return NodeType.CLOSED; //Closed full block, no more space
            }
        }
        //Check openings
        float y = pos.getY() + yOffset;
        for(OctalDirection gapDirection : GAP_DIRECTIONS) {
            float x = pos.getX() + 0.5F + gapDirection.xStepHalf;
            float z = pos.getZ() + 0.5F + gapDirection.zStepHalf;
            if(!nodeManager.collidesWith(nodeManager.getEntityBox(x, y, z))) {
                nodeManager.getNode(pos).setPartialPath(x, y, z);
                return NodeType.WALKABLE;
            }
        }
        nodeManager.getNode(pos).partial = true;
        return NodeType.CLOSED; //All collision checks failed
    }

    default boolean hasFloorAt(float x, float z, BlockPos pos, BlockGetter level) {
        //Convert global coords to block local
        x = x % 1F;
        if(x < 0F) x += 1F;
        z = z % 1F;
        if(z < 0F) z += 1F;
        BlockState belowState = level.getBlockState(pos.below());
        if(belowState.getBlock() instanceof ICustomPathfindable pathfindable) {
            List<AABB> bBoxes = pathfindable.getTopFaceShape(belowState);
            if(bBoxes.isEmpty()) return false;
            for(AABB bBox : bBoxes) {
                if(x >= bBox.minX && x <= bBox.maxX && z >= bBox.minZ && z <= bBox.maxZ) return true;
            }
            return false;
        }
        else return belowState.getMaterial().blocksMotion();
    }

    /**
     * @return whether any floor exists regardless of entity positioning
     */
    default boolean hasAnyFloorAt(BlockState state, BlockPos pos, BlockGetter level) {
        BlockState belowState = level.getBlockState(pos.below());
        if(belowState.getBlock() instanceof ICustomPathfindable pathfindable) {
            List<AABB> bBoxes = pathfindable.getTopFaceShape(belowState);
            if(bBoxes.isEmpty()) return false;
            List<AABB> boxes = getBottomFaceShape(state);
            if(boxes.isEmpty()) return true;
            for(AABB box : boxes) {
                for(AABB bBox : bBoxes) {
                    if(!(bBox.minX >= box.minX && bBox.maxX <= box.maxX && bBox.minZ >= box.minZ && bBox.maxZ <= box.maxZ)) return true;
                }
            }
            return false;
        }
        else return belowState.getMaterial().blocksMotion();
    }
}
