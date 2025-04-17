package frostnox.nightfall.action;

import frostnox.nightfall.util.data.Vec3f;
import net.minecraft.world.entity.Entity;

public class HitData {
    public final Entity hitEntity;
    public int boxIndex = -1; //-1 is entity's AABB
    public float x = Float.MAX_VALUE, y = Float.MAX_VALUE, z = Float.MAX_VALUE;
    public Vec3f force = Vec3f.ZERO;

    public HitData(Entity hitEntity) {
        this.hitEntity = hitEntity;
    }

    public HitData(Entity hitEntity, float x, float y, float z) {
        this.hitEntity = hitEntity;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public HitData(Entity hitEntity, float x, float y, float z, Vec3f force) {
        this.hitEntity = hitEntity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.force = force;
    }

    public HitData(Entity hitEntity, float x, float y, float z, Vec3f force, int boxIndex) {
        this.hitEntity = hitEntity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.force = force;
        this.boxIndex = boxIndex;
    }
}