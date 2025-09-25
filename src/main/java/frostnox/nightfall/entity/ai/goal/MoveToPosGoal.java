package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public abstract class MoveToPosGoal extends Goal {
    protected final ActionableEntity mob;
    protected final double speedModifier, requiredDist;
    protected @Nullable ReversePath path;
    protected @Nullable Vec3 pos;

    public MoveToPosGoal(ActionableEntity mob, double speedModifier, double requiredDist) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.requiredDist = requiredDist;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    protected abstract @Nullable Vec3 getPos();

    protected abstract void onReachGoal();

    @Override
    public boolean canUse() {
        pos = getPos();
        if(pos == null) return false;
        else if(mob.distanceToSqr(pos) <= requiredDist * requiredDist) {
            onReachGoal();
            return false;
        }
        else {
            path = mob.getNavigator().findPath(pos.x, pos.y, pos.z, (float) (requiredDist - 1));
            return path != null && (path.reachesGoal() || path.getSize() > 1);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigator().isDone();
    }

    @Override
    public void start() {
        mob.getNavigator().moveTo(path, speedModifier);
    }

    @Override
    public void stop() {
        if(mob.distanceToSqr(pos) <= requiredDist * requiredDist) onReachGoal();
        path = null;
        pos = null;
    }
}
