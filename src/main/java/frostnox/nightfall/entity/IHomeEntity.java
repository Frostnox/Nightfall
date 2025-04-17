package frostnox.nightfall.entity;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

public interface IHomeEntity extends IActionableEntity {
    @Nullable BlockPos getHomePos();

    void setHomePos(@Nullable BlockPos pos);

    default void onExitHome() {
        ActionableEntity entity = getEntity();
        entity.removeAllEffects();
        long timePassed = entity.level.getGameTime() - entity.getLastTickedGameTime();
        entity.heal(timePassed * 1F/20F/4F);
    }
}
