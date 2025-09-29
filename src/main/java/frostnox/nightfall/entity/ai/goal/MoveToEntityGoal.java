package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public abstract class MoveToEntityGoal extends Goal {
    protected final ActionableEntity mob;
    protected final Predicate<Entity> entityPredicate;
    protected final double speedModifier, minDistSqr;
    protected final int horizontalRange, verticalRange, tickDelay;
    protected int nextStartTick;
    protected @Nullable Entity target;
    protected @Nullable ReversePath path;

    public MoveToEntityGoal(ActionableEntity pMob, double pSpeedModifier, int horizontalRange, int verticalRange, Predicate<Entity> entityPredicate) {
        this(pMob, pSpeedModifier, horizontalRange, verticalRange, entityPredicate, 1, 200);
    }

    public MoveToEntityGoal(ActionableEntity pMob, double pSpeedModifier, int horizontalRange, int verticalRange, Predicate<Entity> entityPredicate, double minDist, int tickDelay) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.horizontalRange = horizontalRange;
        this.verticalRange = verticalRange;
        this.entityPredicate = entityPredicate;
        this.minDistSqr = minDist * minDist;
        this.tickDelay = tickDelay;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    protected abstract void onReachEntity();

    protected @Nullable Entity findNearestEntity() {
        List<Entity> entities = mob.level.getEntities(mob, mob.getBoundingBox().inflate(horizontalRange, verticalRange, horizontalRange), entityPredicate);
        if(entities.isEmpty()) return null;
        return Collections.min(entities, (entity1, entity2) -> Double.compare(entity1.distanceToSqr(mob), entity2.distanceToSqr(mob)));
    }

    @Override
    public boolean canUse() {
        if(nextStartTick > 0) {
            nextStartTick--;
            return false;
        }
        else {
            nextStartTick = reducedTickDelay(tickDelay + mob.getRandom().nextInt(tickDelay));
            Entity nearestEntity = findNearestEntity();
            if(nearestEntity == null) return false;
            ReversePath path = mob.getNavigator().findPath(nearestEntity, 0);
            if(path != null && path.reachesGoal()) {
                this.path = path;
                target = nearestEntity;
                return true;
            }
            else return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return path != null && path.reachesGoal() && target != null && !target.isRemoved();
    }

    @Override
    public void start() {
        mob.getNavigator().moveTo(path, speedModifier);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if(mob.distanceToSqr(target) > minDistSqr) {
            if(mob.randTickCount % 20 == 0) {
                path = mob.getNavigator().findPath(target, 0);
                mob.getNavigator().moveTo(path, speedModifier);
            }
        }
        else onReachEntity();
    }
}
