package frostnox.nightfall.world;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class OrientedEntityHitResult extends EntityHitResult {
    public final int boxIndex; //-1 is bounding box

    public OrientedEntityHitResult(Entity pEntity) {
        super(pEntity);
        boxIndex = -1;
    }

    public OrientedEntityHitResult(Entity pEntity, Vec3 pLocation) {
        super(pEntity, pLocation);
        boxIndex = -1;
    }

    public OrientedEntityHitResult(Entity pEntity, Vec3 pLocation, int boxIndex) {
        super(pEntity, pLocation);
        this.boxIndex = boxIndex;
    }
}
