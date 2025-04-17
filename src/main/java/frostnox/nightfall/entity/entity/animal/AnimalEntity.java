package frostnox.nightfall.entity.entity.animal;

import frostnox.nightfall.entity.entity.HungryEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class AnimalEntity extends HungryEntity {
    public AnimalEntity(EntityType<? extends AnimalEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public double getReducedAIThresholdSqr() {
        return 250 * 250;
    }

    @Override
    public boolean dropLootFromSkinning() {
        return true;
    }
}
