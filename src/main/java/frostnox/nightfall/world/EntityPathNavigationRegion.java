package frostnox.nightfall.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class EntityPathNavigationRegion extends PathNavigationRegion {
    public EntityPathNavigationRegion(Level pLevel, BlockPos pCenterPos, BlockPos pOffsetPos) {
        super(pLevel, pCenterPos, pOffsetPos);
    }

    @Override
    public boolean noCollision(@Nullable Entity pEntity, AABB pCollisionBox) {
        for(VoxelShape shape : this.getBlockCollisions(pEntity, pCollisionBox)) {
            if(!shape.isEmpty()) return false;
        }
        return true;
    }
}
