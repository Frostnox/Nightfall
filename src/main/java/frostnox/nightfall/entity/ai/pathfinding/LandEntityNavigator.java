package frostnox.nightfall.entity.ai.pathfinding;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LandEntityNavigator extends EntityNavigator {
    public LandEntityNavigator(ActionableEntity entity, Level level) {
        this(new LandNodeManager(entity), level);
    }

    public LandEntityNavigator(NodeManager nodeManager, Level level) {
        super(nodeManager, level);
    }

    @Override
    protected float heuristic(Node from, Node to) {
        return from.distOctile(to);
    }

    @Override
    protected Vec3 getTempMobPos() {
        if(entity.isInWater() && canFloat()) {
            double y = entity.getY();
            BlockState block = level.getBlockState(new BlockPos(entity.getX(), y, entity.getZ()));
            int water = 0;

            while(!block.getMaterial().blocksMotion() && block.getFluidState().is(FluidTags.WATER)) {
                y++;
                if(water == 0) y = (int) (y);
                water++;
                block = level.getBlockState(new BlockPos(entity.getX(), y, entity.getZ()));
                if(water > 16) return new Vec3(entity.getX(), entity.getBlockY(), entity.getZ());
            }

            return new Vec3(entity.getX(), y, entity.getZ());
        }
        else return new Vec3(entity.getX(), Mth.floor(entity.getY() + 0.5D), entity.getZ());
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }
}
