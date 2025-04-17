package frostnox.nightfall.entity;

import frostnox.nightfall.util.math.OBB;
import net.minecraft.world.phys.AABB;

public interface IOrientedHitBoxes {
    double MAX_DIST_FROM_AABB = 1D; //OBBs exceeding this distance from the entity's AABB will not be detected consistently

    OBB[] getOBBs(float partial);

    AABB getEnclosingAABB();
}
