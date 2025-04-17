package frostnox.nightfall.entity.ai.pathfinding;

import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.EntityPathNavigationRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class ActionableMoveControl extends MoveControl {
    protected final ActionableEntity entity;
    protected long lastJumpTime = -100;
    protected @Nullable PathNavigationRegion region;
    protected int regionMinX, regionMaxX, regionMinZ, regionMaxZ;

    public ActionableMoveControl(ActionableEntity entity) {
        super(entity);
        this.entity = entity;
    }

    protected void setSpeed(float lookAngle) {
        //Make sure entity is facing the correct position before walking
        if(Math.abs(lookAngle - mob.getYRot()) < 20F) mob.setSpeed((float) (speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
        else mob.setSpeed((float) (speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED)) * 0.2F);
    }

    @Override
    public void tick() {
        if(operation == MoveControl.Operation.MOVE_TO) {
            operation = MoveControl.Operation.WAIT;
            Node node = entity.getNavigator().getCurrentNode();
            if(node == null) return;
            double dX = wantedX - mob.getX();
            double dZ = wantedZ - mob.getZ();
            double dY = wantedY - mob.getY();
            //Reset strafe values to avoid extra movement in unintended directions
            //This behavior can be seen in the vanilla skeleton when it is trying to shoot a player and the player moves to the edge of its attack range
            mob.setZza(0);
            mob.setXxa(0);
            if(dX * dX + dY * dY + dZ * dZ < 0.0015) return;

            float lookAngle = MathUtil.getAngleDegrees(dZ, dX);
            mob.setYRot(rotlerp(mob.getYRot(), lookAngle, 90.0F));
            setSpeed(lookAngle);

            BlockPos pos = mob.blockPosition();
            BlockState inState = mob.level.getBlockState(pos);
            VoxelShape inShape = inState.getCollisionShape(mob.level, pos);
            if(dY > mob.getStepHeight() && Math.abs(lookAngle - mob.getYRot()) < 20F) {
                double bestDistSqr = Double.POSITIVE_INFINITY;
                AABB entityBox = entity.getBoundingBox();
                for(AABB box : node.floorShape) {
                    double x1 = Math.max(0, entityBox.minX - box.maxX);
                    double z1 = Math.max(0, entityBox.minZ - box.maxZ);
                    double x2 = Math.max(0, box.minX - entityBox.maxX);
                    double z2 = Math.max(0, box.minZ - entityBox.maxZ);
                    double distSqr = x1 * x1 + z1 * z1 + x2 * x2 + z2 * z2;
                    if(distSqr < bestDistSqr) bestDistSqr = distSqr;
                }
                if(bestDistSqr <= mob.getSpeed() * mob.getSpeed() || !inShape.isEmpty() && mob.getY() < inShape.max(Direction.Axis.Y) + pos.getY()
                        && !inState.is(BlockTags.DOORS) && !inState.collisionExtendsVertically(mob.level, pos, mob)
                        && Shapes.joinIsNotEmpty(inShape, Shapes.create(entityBox), BooleanOp.AND)
                        && (!node.mineable || !inState.getMaterial().blocksMotion())) {
                    //Give entity a chance to realign in case it missed its jump
                    if(entity.level.getGameTime() > lastJumpTime + 14) {
                        mob.getJumpControl().jump();
                        operation = Operation.JUMPING;
                        lastJumpTime = entity.level.getGameTime();
                    }
                }
            }
        }
        else if(operation == MoveControl.Operation.JUMPING) {
            setSpeed(MathUtil.getAngleDegrees(wantedZ - mob.getZ(), wantedX - mob.getX()));
            if(mob.isOnGround() || mob.isInWater() || mob.isInLava()) operation = MoveControl.Operation.WAIT;
        }
        else super.tick();
    }

    @Override
    protected boolean isWalkable(float pRelativeX, float pRelativeZ) {
        EntityNavigator navigator = entity.getNavigator();
        if(navigator != null) {
            NodeManager nodeManager = navigator.nodeManager;
            if(nodeManager != null) {
                int y = mob.getBlockY();
                if(entity.level.isOutsideBuildHeight(y)) return false;
                int x = Mth.floor(mob.getX() + pRelativeX);
                int z = Mth.floor(mob.getZ() + pRelativeZ);
                int chunkX = SectionPos.blockToSectionCoord(x);
                int chunkZ = SectionPos.blockToSectionCoord(z);
                if(region == null || chunkX < regionMinX || chunkX > regionMaxX || chunkZ < regionMinZ || chunkZ > regionMaxZ) {
                    regionMinX = SectionPos.blockToSectionCoord(x - 8);
                    regionMaxX = SectionPos.blockToSectionCoord(x + 8);
                    regionMinZ = SectionPos.blockToSectionCoord(z - 8);
                    regionMaxZ = SectionPos.blockToSectionCoord(z + 8);
                    region = new EntityPathNavigationRegion(entity.level, new BlockPos(x - 8, y - 8, z - 8), new BlockPos(x + 8, y + 8, z + 8));
                }
                nodeManager.setupPath(region);
                nodeManager.canMine = false;
                nodeManager.canBuild = false;
                if(nodeManager.getSingleNodeType(mob.level, x, y, z) != NodeType.WALKABLE) {
                    nodeManager.cleanupPath();
                    return false;
                }
                else nodeManager.cleanupPath();
            }
        }
        return true;
    }
}
