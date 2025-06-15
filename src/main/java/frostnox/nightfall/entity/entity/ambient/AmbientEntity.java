package frostnox.nightfall.entity.entity.ambient;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.List;

public abstract class AmbientEntity extends ActionableEntity {
    public AmbientEntity(EntityType<? extends ActionableEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void pushEntities() {
        if(level.isClientSide) return;
        List<Entity> list = this.level.getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this).and(entity -> entity instanceof AmbientEntity));
        if (!list.isEmpty()) {
            int i = this.level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
                int j = 0;

                for(int k = 0; k < list.size(); ++k) {
                    if (!list.get(k).isPassenger()) {
                        ++j;
                    }
                }

                if (j > i - 1) {
                    this.hurt(DamageSource.CRAMMING, 6.0F);
                }
            }

            for(int l = 0; l < list.size(); ++l) {
                Entity entity = list.get(l);
                this.doPush(entity);
            }
        }

    }

    @Override
    public boolean dropLootFromSkinning() {
        return false;
    }
}
