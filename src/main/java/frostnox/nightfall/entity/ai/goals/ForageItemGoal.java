package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.IHungerEntity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.function.Predicate;

public class ForageItemGoal extends MoveToItemGoal {
    private final IHungerEntity hungerEntity;

    public ForageItemGoal(IHungerEntity hungerEntity, double pSpeedModifier, int horizontalRange, int verticalRange) {
        this(hungerEntity, pSpeedModifier, horizontalRange, verticalRange, (entity) -> hungerEntity.canEat(entity.getItem()), 1D, 200);
    }

    public ForageItemGoal(IHungerEntity hungerEntity, double pSpeedModifier, int horizontalRange, int verticalRange, Predicate<ItemEntity> itemPredicate, double minDist, int tickDelay) {
        super(hungerEntity.getEntity(), pSpeedModifier, horizontalRange, verticalRange, itemPredicate, minDist, tickDelay);
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
    protected void onReachItem() {
        hungerEntity.eatItem(target);
    }
}
