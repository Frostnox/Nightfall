package frostnox.nightfall.entity.entity.ambient;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class AmbientEntity extends ActionableEntity {
    public AmbientEntity(EntityType<? extends ActionableEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public float getPushResistance() {
        return PUSH_ZERO;
    }

    @Override
    public float getPushForce() {
        return PUSH_ZERO;
    }

    @Override
    public boolean dropLootFromSkinning() {
        return true;
    }
}
