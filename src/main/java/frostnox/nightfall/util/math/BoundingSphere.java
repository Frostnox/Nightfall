package frostnox.nightfall.util.math;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.HitData;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.animation.AnimationData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashSet;

public class BoundingSphere {
    public double xPos, yPos, zPos;
    public double radius;

    public BoundingSphere(double radius) {
        this.radius = radius;
    }

    public BoundingSphere(double xPos, double yPos, double zPos, double radius) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.radius = radius;
    }

    public BoundingSphere(BoundingSphere sphere) {
        this.xPos = sphere.xPos;
        this.yPos = sphere.yPos;
        this.zPos = sphere.zPos;
        this.radius = sphere.radius;
    }

    public void translate(Vec3 vec) {
        translate(vec.x, vec.y, vec.z);
    }

    public void translate(double x, double y, double z) {
        xPos += x;
        yPos += y;
        zPos += z;
    }

    public void scalePos(double x, double y, double z) {
        xPos *= x;
        yPos *= y;
        zPos *= z;
    }

    public void scale(double scalar) {
        xPos *= scalar;
        yPos *= scalar;
        zPos *= scalar;
        radius *= scalar;
    }

    public AABB toAABB() {
        return new AABB(xPos - radius,yPos - radius,zPos - radius, xPos + radius, yPos + radius, zPos + radius);
    }

    public void transform(Mat4f matrix) {
        Vec3 center = new Vec3(xPos, yPos, zPos);
        center = matrix.transformVector3d(center);
        xPos = center.x;
        yPos = center.y;
        zPos = center.z;
    }

    public Vec3 getXZRotation(Mat4f matrix, double yOff) {
        Vec3 center = new Vec3(xPos, yOff, zPos);
        Vec3 rotCenter = matrix.transformVector3d(center);
        return rotCenter.subtract(center);
    }

    public void rotateAtOffset(Mat4f matrix, double offX, double offY, double offZ) {
        xPos += offX;
        yPos += offY;
        zPos += offZ;
        transform(matrix);
        xPos -= offX;
        yPos -= offY;
        zPos -= offZ;
    }

    /**
     * Transform based on FP animation data only; FP has a different format from third person
     * @param transforms rotations from FP item model
     * @param userMatrix any rotations applied to PoseStack, done last
     * @param translation extra translation applied after transforms (still affected by userMatrix)
     * @param offset extra translation applied before transforms
     * @param leftHand true if item is in left hand
     */
    public void transformFP(AnimationData transforms, Mat4f userMatrix, Vector3f translation, Vector3f offset, boolean leftHand) {
        Vector3f center = new Vector3f((float) xPos, (float) yPos, (float) zPos);
        center.add(offset);
        Mat4f mat = new Mat4f();

        Vector3f rVec = transforms.rCalc.getTransformations();
        if(rVec.z() != 0F) mat.multiply(new Quat(rVec.z(), Vector3f.ZP, true));
        if(rVec.y() != 0F) mat.multiply(new Quat(rVec.y(), Vector3f.YN, true));
        if(rVec.x() != 0F) mat.multiply(new Quat(rVec.x(), Vector3f.XP, true));
        mat.transformVector3f(center);
        Vector3f tVec = transforms.tCalc.getTransformations();
        tVec.mul(-1, 1, -1);

        center.add(tVec);
        center.add(translation);
        if(leftHand) center.setX(-center.x());
        userMatrix.transformVector3f(center);
        xPos = center.x();
        yPos = center.y();
        zPos = center.z();
    }

    /**
     * @param transforms main rotations (in degrees) from first to last; translation is applied after each rotation
     * @param userMatrix any rotations applied to PoseStack, done last
     * @param localMatrix any rotations around the origin, done first
     * @param translation extra translation applied after transforms (still affected by userMatrix)
     * @param offset extra translation applied before any transforms
     */
    public void transform(AnimationData[] transforms, Mat4f userMatrix, Mat4f localMatrix, Vector3f translation, Vector3f offset) {
        Vector3f center = MathUtil.transformPoint(new Vector3f((float) xPos, (float) yPos, (float) zPos), transforms, userMatrix, localMatrix, translation, offset);
        xPos = center.x();
        yPos = center.y();
        zPos = center.z();
    }

    public void transform(AnimationData[] transforms, Mat4f userMatrix, Mat4f localMatrix) {
        transform(transforms, userMatrix, localMatrix, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));
    }

    public void transform(AnimationData[] transforms, Mat4f userMatrix, Vector3f translation, Vector3f offset) {
        transform(transforms, userMatrix, new Mat4f(), translation, offset);
    }

    public void transform(AnimationData[] transforms, Mat4f userMatrix, Vector3f offset) {
        transform(transforms, userMatrix, new Mat4f(), new Vector3f(0, 0, 0), offset);
    }

    public void transform(AnimationData[] transforms, Mat4f userMatrix) {
        transform(transforms, userMatrix, new Mat4f(), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));
    }

    public boolean intersectsAABB(AABB box) {
        //Point in box closest to center
        return MathUtil.getShortestDistanceSqrPointToBox(xPos, yPos, zPos, box) <= radius * radius;
    }

    public boolean intersectsAndSeesEntity(LivingEntity user, Entity entity, HitData hitData) {
        if(entity instanceof IOrientedHitBoxes hitBoxesEntity) {
            if(hitBoxesEntity.includeAABB() && intersectsAndSeesAABB(user, entity.getBoundingBox(), hitData)) return true;
            OBB[] obbs = hitBoxesEntity.getOBBs(1F);
            for(int i = 0; i < obbs.length; i++) {
                if(intersectsAndSeesOBB(user, obbs[i], entity.position(), hitData)) {
                    hitData.boxIndex = i;
                    return true;
                }
            }
        }
        else return intersectsAndSeesAABB(user, entity.getBoundingBox(), hitData);
        return false;
    }

    public boolean intersectsAndSeesOBB(LivingEntity user, OBB box, Vec3 entityPos, HitData hitData) {
        box.rotate();
        Vec3 dir = new Vec3(xPos - entityPos.x, yPos - entityPos.y, zPos - entityPos.z).subtract(box.center.x(), box.center.y(), box.center.z()).subtract(box.translation);
        double rotCenterX = dir.dot(box.localX);
        double rotCenterY = dir.dot(box.localY);
        double rotCenterZ = dir.dot(box.localZ);
        double xC = Mth.clamp(rotCenterX, -box.extents.x + 0.0001D, box.extents.x - 0.0001D);
        double yC = Mth.clamp(rotCenterY, -box.extents.y + 0.0001D, box.extents.y - 0.0001D);
        double zC = Mth.clamp(rotCenterZ, -box.extents.z + 0.0001D, box.extents.z - 0.0001D);
        //Point in box closest to center
        double x = xC * box.localX.x + yC * box.localY.x + zC * box.localZ.x;
        double y = xC * box.localX.y + yC * box.localY.y + zC * box.localZ.y;
        double z = xC * box.localX.z + yC * box.localY.z + zC * box.localZ.z;
        Vec3 hitPos = entityPos.add(box.center.x(), box.center.y(), box.center.z()).add(x, y, z).add(box.translation);
        //If point is in sphere, do LoS check
        if(hitPos.distanceToSqr(xPos, yPos, zPos) <= radius * radius) {
            if(user.level.clip(new ClipContext(user.getEyePosition(1), hitPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user)).getType() == HitResult.Type.MISS) {
                hitData.x = (float) hitPos.x;
                hitData.y = (float) hitPos.y;
                hitData.z = (float) hitPos.z;
                return true;
            }
        }
        return false;
    }

    public boolean intersectsAndSeesAABB(LivingEntity user, AABB box, HitData hitData) {
        //Point in box closest to center
        double x = Mth.clamp(xPos, box.minX + 0.0001D, box.maxX - 0.0001D);
        double y = Mth.clamp(yPos, box.minY + 0.0001D, box.maxY - 0.0001D);
        double z = Mth.clamp(zPos, box.minZ + 0.0001D, box.maxZ - 0.0001D);
        double xDist = xPos - x;
        double yDist = yPos - y;
        double zDist = zPos - z;
        //If point is in sphere, do LoS check
        if(xDist * xDist + yDist * yDist + zDist * zDist <= radius * radius) {
            if(user.level.clip(new ClipContext(user.getEyePosition(1), new Vec3(x, y, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user)).getType() == HitResult.Type.MISS) {
                hitData.x = (float) x;
                hitData.y = (float) y;
                hitData.z = (float) z;
                return true;
            }
        }
        return false;
    }

    public boolean isSpaceObstructed(LivingEntity user, BlockPos climbPos) {
        VoxelShape shape = user.level.getBlockState(climbPos).getCollisionShape(user.level, climbPos, CollisionContext.of(user));
        if(shape.isEmpty()) return false;
        BoundingSphere sphere = new BoundingSphere(this);
        sphere.radius /= 2;
        AABB box = sphere.toAABB();
        for(AABB climbBox : shape.move(climbPos.getX(), climbPos.getY(), climbPos.getZ()).toAabbs()) {
            if(climbBox.intersects(box)) return true;
        }
        return false;
    }

    public boolean isSpaceClimbable(LivingEntity user) {
        return isSpaceClimbable(user, new Vector3d(0, 0, 0));
    }

    /**
     * This only works consistently with spheres of radius <= 1
     */
    public boolean isSpaceClimbable(LivingEntity user, Vector3d hitCoords) {
        AABB box = this.toAABB();
        //Collect possible positions
        HashSet<BlockPos> positions = new HashSet<>();
        positions.add(new BlockPos(box.maxX, box.maxY, box.maxZ));
        positions.add(new BlockPos(box.maxX, box.maxY, box.minZ));
        positions.add(new BlockPos(box.minX, box.maxY, box.maxZ));
        positions.add(new BlockPos(box.minX, box.maxY, box.minZ));
        positions.add(new BlockPos(box.maxX, box.minY, box.maxZ));
        positions.add(new BlockPos(box.maxX, box.minY, box.minZ));
        positions.add(new BlockPos(box.minX, box.minY, box.maxZ));
        positions.add(new BlockPos(box.minX, box.minY, box.minZ));
        for(BlockPos pos : positions) {
            VoxelShape shape = LevelUtil.getBlockClimbingShape(user, pos);
            if(shape.isEmpty() || !intersectsAABB(shape.bounds())) continue;
            for(AABB shapeBox : shape.toAabbs()) {
                if(isAABBClimbable(user, shapeBox, hitCoords)) return true;
            }
        }
        return false;
    }

    public boolean isAABBClimbable(LivingEntity user, AABB box) {
        return isAABBClimbable(user, box, new Vector3d(0, 0, 0));
    }

    public boolean isAABBClimbable(LivingEntity user, AABB box, Vector3d hitCoords) {
        //Closest point to center
        double x = Mth.clamp(xPos, box.minX + 0.00001D, box.maxX - 0.00001D);
        double y = box.maxY + (box.maxY % 1 == 0 ? 0 : 0.00001D);
        double z = Mth.clamp(zPos, box.minZ + 0.00001D, box.maxZ - 0.00001D);

        //Check that user is facing the point
        float lookAngle = CombatUtil.getRelativeHorizontalAngle(user.getEyePosition(), new Vec3(x, y, z), user.getYHeadRot());

        //Check if point is in sphere and above user, user is facing block, and user can see block
        if((x - xPos) * (x - xPos) + (y - yPos) * (y - yPos) + (z - zPos) * (z - zPos) <= radius * radius && y > user.getY() && lookAngle <= 25F && lookAngle >= -25F
                && user.level.clip(new ClipContext(new Vec3(user.getX(), y, user.getZ()), new Vec3(x, y, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user)).getType() != HitResult.Type.BLOCK) {
            hitCoords.set(x, y, z);
            //Round to the closest edge
            if(x - Mth.floor(x) > z - Mth.floor(z)) z = Mth.clamp(Math.round(z), box.minZ + 0.00001D, box.maxZ - 0.00001D);
            else x = Mth.clamp(Math.round(x), box.minX + 0.00001D, box.maxX - 0.00001D);
            //Check if point above box is obstructed by block
            BlockPos posA = new BlockPos(x, y + 1D/16D, z);
            BlockState stateA = user.level.getBlockState(posA);
            AABB boxA = AABB.ofSize(new Vec3(x, y + 1D/16D, z), 0.00001D, 0.00001D, 0.00001D);
            boolean emptyFlag = true;
            if(!stateA.isAir() && !stateA.getCollisionShape(user.level, posA).isEmpty()) {
                emptyFlag = !Shapes.joinIsNotEmpty(stateA.getCollisionShape(user.level, posA).move(posA.getX(), posA.getY(), posA.getZ()), Shapes.create(boxA), BooleanOp.AND);
            }
            //Check for space above user's head
            if(y + 1D/16D > user.getY() + user.getBbHeight() && emptyFlag) {
                BlockPos posB = new BlockPos(user.getX(), y + 1D / 16D, user.getZ());
                BlockState stateB = user.level.getBlockState(posB);
                AABB boxB = AABB.ofSize(new Vec3(user.getX(), y + 1D/16D, user.getZ()), 0.00001D, 0.00001D, 0.00001D);
                if(!stateB.isAir() && !stateB.getCollisionShape(user.level, posB).isEmpty()) {
                    if(Shapes.joinIsNotEmpty(stateB.getCollisionShape(user.level, posB).move(posB.getX(), posB.getY(), posB.getZ()), Shapes.create(boxB), BooleanOp.AND)) {
                        return false;
                    }
                }
            }
            //Check that position isn't too far
            if(emptyFlag && user instanceof Player player) {
                IPlayerData capP = PlayerData.get(player);
                return capP.getClimbPosition().y == -1 || Math.abs(capP.getClimbPosition().y - hitCoords.y) < 0.5001
                        || LevelUtil.isPositionFullyClimbable(user, hitCoords) || LevelUtil.isPositionFullyClimbable(user, capP.getClimbPosition());
            }
            return emptyFlag;
        }
        return false;
    }
}
