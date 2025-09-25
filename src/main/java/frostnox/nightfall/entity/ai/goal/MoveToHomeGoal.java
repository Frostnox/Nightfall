package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.IHomeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class MoveToHomeGoal extends MoveToPosGoal {
    protected final IHomeEntity homeEntity;

    public MoveToHomeGoal(IHomeEntity mob, double speedModifier, double requiredDist) {
        super(mob.getEntity(), speedModifier, requiredDist);
        homeEntity = mob;
    }

    @Nullable
    @Override
    protected Vec3 getPos() {
        BlockPos pos = homeEntity.getHomePos();
        return pos == null ? null : Vec3.atCenterOf(pos);
    }
}
