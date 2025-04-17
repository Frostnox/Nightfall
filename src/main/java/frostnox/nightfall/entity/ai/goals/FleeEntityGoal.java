package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class FleeEntityGoal<T extends LivingEntity> extends Goal {
    protected final ActionableEntity mob;
    protected final Class<T> fleeClass;
    protected final double walkSpeedModifier, sprintSpeedModifier;
    protected final TargetingConditions fleeConditions;
    protected final Predicate<LivingEntity> fleePredicate;
    protected @Nullable Vec3 avoidPos;
    protected @Nullable ReversePath path;

    public FleeEntityGoal(ActionableEntity mob, Class<T> fleeClass, double walkSpeedModifier, double sprintSpeedModifier) {
        this(mob, fleeClass, walkSpeedModifier, sprintSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
    }

    public FleeEntityGoal(ActionableEntity mob, Class<T> fleeClass, double walkSpeedModifier, double sprintSpeedModifier, Predicate<LivingEntity> fleePredicate) {
        this.mob = mob;
        this.fleeClass = fleeClass;
        this.walkSpeedModifier = walkSpeedModifier;
        this.sprintSpeedModifier = sprintSpeedModifier;
        this.fleeConditions = TargetingConditions.forCombat().selector(fleePredicate);
        this.fleePredicate = fleePredicate;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    protected boolean checkHeardEntities() {
        boolean update = false;
        double avoidDistSqr = avoidPos == null ? Double.MAX_VALUE : mob.distanceToSqr(avoidPos);
        for(Entity entity : mob.getAudioSensing().getHeardEntities()) {
            if(fleeClass.isAssignableFrom(entity.getClass()) && fleePredicate.test((LivingEntity) entity)) {
                double distSqr = mob.distanceToSqr(entity);
                if(distSqr == avoidDistSqr || (distSqr < avoidDistSqr && avoidDistSqr - distSqr > 4D)) {
                    update = true;
                    avoidDistSqr = distSqr;
                    avoidPos = entity.position();
                }
            }
        }
        return update;
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
        int range = (int) mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        fleeConditions.range(range);
        LivingEntity nearestEntity = mob.level.getNearestEntity(mob.level.getEntitiesOfClass(fleeClass,
                mob.getBoundingBox().inflate(range, range/2, range), (entity) -> true), fleeConditions, mob, mob.getX(), mob.getY(), mob.getZ());
        if(nearestEntity != null) avoidPos = nearestEntity.position();
        checkHeardEntities();
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
    }

    @Override
    public void tick() {
        if(checkHeardEntities()) {
            if(updatePath()) mob.getNavigator().moveTo(path, sprintSpeedModifier);
        }
        if(mob.distanceToSqr(avoidPos) < 16D * 16D) mob.getNavigator().setSpeedModifier(sprintSpeedModifier);
        else mob.getNavigator().setSpeedModifier(walkSpeedModifier);
    }
}
