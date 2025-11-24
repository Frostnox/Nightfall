package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class FleeTargetGoal extends Goal {
    protected final ActionableEntity mob;
    protected final double walkSpeedModifier, sprintSpeedModifier;
    protected @Nullable Vec3 avoidPos;
    protected @Nullable ReversePath path;

    public FleeTargetGoal(ActionableEntity mob, double walkSpeedModifier, double sprintSpeedModifier) {
        this.mob = mob;
        this.walkSpeedModifier = walkSpeedModifier;
        this.sprintSpeedModifier = sprintSpeedModifier;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    protected boolean updatePath() {
        if(avoidPos == null) return false;
        Vec3 randPos = getRandomPos();
        if(randPos == null) return false;
        else {
            onFindPath();
            path = mob.getNavigator().findPath(randPos.x, randPos.y, randPos.z, 0);
            return path != null;
        }
    }

    protected void onFindPath() {

    }

    protected Vec3 getRandomPos() {
        return DefaultRandomPos.getPosAway(mob, 32, 8, avoidPos);
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if(target != null && mob.shouldFleeFrom(target)) avoidPos = target.position();
        return updatePath();
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigator().isDone();
    }

    @Override
    public void start() {
        mob.getNavigator().moveTo(path, sprintSpeedModifier);
    }

    @Override
    public void stop() {
        avoidPos = null;
        path = null;
        mob.lastTargetPos = null;
        mob.setTarget(null);
    }

    @Override
    public void tick() {
        if(mob.distanceToSqr(avoidPos) < 16D * 16D) mob.getNavigator().setSpeedModifier(sprintSpeedModifier);
        else mob.getNavigator().setSpeedModifier(walkSpeedModifier);
    }
}
