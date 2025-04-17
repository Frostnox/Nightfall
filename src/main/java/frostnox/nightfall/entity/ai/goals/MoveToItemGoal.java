package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public abstract class MoveToItemGoal extends Goal {
    protected final ActionableEntity mob;
    protected final Predicate<ItemEntity> itemPredicate;
    protected final double speedModifier, minDistSqr;
    protected final int horizontalRange, verticalRange, tickDelay;
    protected int nextStartTick;
    protected @Nullable ItemEntity target;
    protected @Nullable ReversePath path;

    public MoveToItemGoal(ActionableEntity pMob, double pSpeedModifier, int horizontalRange, int verticalRange, Predicate<ItemEntity> itemPredicate) {
        this(pMob, pSpeedModifier, horizontalRange, verticalRange, itemPredicate, 1, 200);
    }

    public MoveToItemGoal(ActionableEntity pMob, double pSpeedModifier, int horizontalRange, int verticalRange, Predicate<ItemEntity> itemPredicate, double minDist, int tickDelay) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.horizontalRange = horizontalRange;
        this.verticalRange = verticalRange;
        this.itemPredicate = itemPredicate;
        this.minDistSqr = minDist * minDist;
        this.tickDelay = tickDelay;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    protected abstract void onReachItem();

    protected @Nullable ItemEntity findNearestItem() {
        List<ItemEntity> items = mob.level.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(horizontalRange, verticalRange, horizontalRange), itemPredicate);
        if(items.isEmpty()) return null;
        return Collections.min(items, (item1, item2) -> Double.compare(item1.distanceToSqr(mob), item2.distanceToSqr(mob)));
    }

    @Override
    public boolean canUse() {
        if(nextStartTick > 0) {
            nextStartTick--;
            return false;
        }
        else {
            nextStartTick = reducedTickDelay(tickDelay + mob.getRandom().nextInt(tickDelay));
            ItemEntity nearestItem = findNearestItem();
            if(nearestItem == null) return false;
            ReversePath path = mob.getNavigator().findPath(nearestItem, 0);
            if(path != null && path.reachesGoal()) {
                this.path = path;
                target = nearestItem;
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
            if(mob.tickCount % 20 == 0) {
                path = mob.getNavigator().findPath(target, 0);
                mob.getNavigator().moveTo(path, speedModifier);
            }
        }
        else onReachItem();
    }
}
