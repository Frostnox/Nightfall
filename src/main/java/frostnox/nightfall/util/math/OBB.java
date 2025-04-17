package frostnox.nightfall.util.math;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class OBB {
    public Vector3f center;
    public Vec3 extents, translation;
    public Quaternion rotation;
    public Vec3 localX, localY, localZ;
    private boolean rotated;

    public OBB(double xSize, double ySize, double zSize, double xCenter, double yCenter, double zCenter) {
        this(xSize, ySize, zSize, xCenter, yCenter, zCenter, new Quaternion(0F, 0F, 0F, 1F));
    }

    public OBB(double xSize, double ySize, double zSize, double xCenter, double yCenter, double zCenter, Quaternion rotation) {
        this(xSize, ySize, zSize, xCenter, yCenter, zCenter, 0, 0, 0, rotation);
    }

    public OBB(double xSize, double ySize, double zSize, double xCenter, double yCenter, double zCenter, double x, double y, double z, Quaternion rotation) {
        center = new Vector3f((float) xCenter, (float) yCenter, (float) zCenter);
        extents = new Vec3(xSize / 2, ySize / 2, zSize / 2);
        translation = new Vec3(x, y, z);
        this.rotation = rotation;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
        rotated = false;
    }

    public void rotate() {
        if(!rotated) {
            center.transform(rotation);
            //Conjugate before calculating rotation matrix to align with transform function
            float i = -rotation.i();
            float j = -rotation.j();
            float k = -rotation.k();
            float r = rotation.r();
            float iSqr2 = 2.0F * i * i;
            float jSqr2 = 2.0F * j * j;
            float kSqr2 = 2.0F * k * k;
            float ij = i * j;
            float jk = j * k;
            float ki = k * i;
            float ir = i * r;
            float jr = j * r;
            float kr = k * r;
            localX = new Vec3(1.0F - jSqr2 - kSqr2, 2.0F * (ij - kr), 2.0F * (ki + jr));
            localY = new Vec3(2.0F * (ij + kr), 1.0F - kSqr2 - iSqr2, 2.0F * (jk - ir));
            localZ = new Vec3(2.0F * (ki - jr), 2.0F * (jk + ir), 1.0F - iSqr2 - jSqr2);
            rotated = true;
        }
    }

    public boolean contains(Vec3 point) {
        rotate();
        point = point.subtract(translation);
        double dist = point.dot(localX);
        if(dist < -extents.x || dist > extents.x) return false;
        dist = point.dot(localY);
        if(dist < -extents.y || dist > extents.y) return false;
        dist = point.dot(localZ);
        if(dist < -extents.z || dist > extents.z) return false;
        return true;
    }

    public Optional<Vec3> rayCast(Vec3 start, Vec3 end) {
        rotate();
        Vec3 rayDir = end.subtract(start).normalize();
        double rayDirX = localX.dot(rayDir), rayDirY = localY.dot(rayDir), rayDirZ = localZ.dot(rayDir);
        Vec3 boxDir = new Vec3(center.x() + translation.x - start.x, center.y() + translation.y - start.y, center.z() + translation.z - start.z);
        double boxDirX = localX.dot(boxDir), boxDirY = localY.dot(boxDir), boxDirZ = localZ.dot(boxDir);
        if(rayDirX == 0D) {
            if(-boxDirX - extents.x > 0D || -boxDirX + extents.x < 0D) return Optional.empty();
            rayDirX = 0.00001D;
        }
        else if(rayDirY == 0D) {
            if(-boxDirY - extents.y > 0D || -boxDirY + extents.y < 0D) return Optional.empty();
            rayDirY = 0.00001D;
        }
        else if(rayDirZ == 0D) {
            if(-boxDirZ - extents.z > 0D || -boxDirZ + extents.z < 0D) return Optional.empty();
            rayDirZ = 0.00001D;
        }
        double t1 = (boxDirX + extents.x) / rayDirX;
        double t2 = (boxDirX - extents.x) / rayDirX;
        double t3 = (boxDirY + extents.y) / rayDirY;
        double t4 = (boxDirY - extents.y) / rayDirY;
        double t5 = (boxDirZ + extents.z) / rayDirZ;
        double t6 = (boxDirZ - extents.z) / rayDirZ;
        double tMax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));
        if(tMax < 0D) return Optional.empty();
        double tMin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        if(tMin > tMax) return Optional.empty();
        double t = tMin < 0D ? tMax : tMin;
        return Optional.of(start.add(rayDir.scale(t)));
    }
}
