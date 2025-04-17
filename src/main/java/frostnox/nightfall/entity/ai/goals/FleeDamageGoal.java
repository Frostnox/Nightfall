package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class FleeDamageGoal extends Goal {
    protected final ActionableEntity mob;
    protected final double speedModifier;
    protected @Nullable ReversePath path;

    public FleeDamageGoal(ActionableEntity mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    protected void onFindPath() {

    }

    protected boolean updatePath() {
        Vec3 randPos = getRandomPos();
        if(randPos == null) return false;
        else {
            onFindPath();
            path = mob.getNavigator().findPath(randPos.x, randPos.y, randPos.z, 0);
            return path != null;
        }
    }

    protected Vec3 getRandomPos() {
        return DefaultRandomPos.getPos(mob, 24, 8);
    }

    @Override
    public boolean canUse() {
        if(mob.reactToDamage) {
            if(updatePath()) {
                mob.reactToDamage = false;
                return true;
            }
        }
        return false;
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
        path = null;
    }

    @Override
    public void tick() {
        if(mob.reactToDamage) {
            if(updatePath()) {
                mob.reactToDamage = false;
                mob.getNavigator().moveTo(path, speedModifier);
            }
        }
    }
}
