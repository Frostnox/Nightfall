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
        if(level.isClientSide || isDeadOrDying()) return;
        List<Entity> entities = this.level.getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this).and(entity -> entity instanceof AmbientEntity));
        if(!entities.isEmpty()) {
            int maxGroup = level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            if(maxGroup > 0 && entities.size() > maxGroup - 1 && random.nextInt(4) == 0) {
                int count = 0;
                for(Entity entity : entities) {
                    if(!entity.isPassenger()) ++count;
                }
                if(count > maxGroup - 1) hurt(DamageSource.CRAMMING, 6.0F);
            }
            for(Entity entity : entities) doPush(entity);
        }
    }

    @Override
    public boolean dropLootFromSkinning() {
        return true;
    }
}
