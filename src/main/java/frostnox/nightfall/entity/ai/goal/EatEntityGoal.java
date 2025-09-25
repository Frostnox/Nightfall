package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.IHungerEntity;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public class EatEntityGoal extends MoveToEntityGoal {
    private final IHungerEntity hungerEntity;

    public EatEntityGoal(IHungerEntity hungerEntity, double pSpeedModifier, int horizontalRange, int verticalRange) {
        this(hungerEntity, pSpeedModifier, horizontalRange, verticalRange, hungerEntity::canEat, 1D, 200);
    }

    public EatEntityGoal(IHungerEntity hungerEntity, double pSpeedModifier, int horizontalRange, int verticalRange, Predicate<Entity> entityPredicate, double minDist, int tickDelay) {
        super(hungerEntity.getEntity(), pSpeedModifier, horizontalRange, verticalRange, entityPredicate, minDist, tickDelay);
        this.hungerEntity = hungerEntity;
    }

    @Override
    public boolean canUse() {
        if(!hungerEntity.isHungry()) return false;
        else return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return hungerEntity.isHungry() && super.canContinueToUse();
    }

    @Override
    protected void onReachEntity() {
        hungerEntity.eatEntity(target);
    }
}
