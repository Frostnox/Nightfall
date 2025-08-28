package frostnox.nightfall.entity.entity.ambient;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public abstract class AquaticAmbientEntity extends AmbientEntity {
    public AquaticAmbientEntity(EntityType<? extends ActionableEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public MobType getMobType() {
        return MobType.WATER;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader pLevel) {
        return pLevel.noCollision(this);
    }

    @Override
    public void baseTick() {
        int air = getAirSupply();
        super.baseTick();
        if(isAlive() && !isInWaterOrBubble()) {
            setAirSupply(air - 1);
            if(getAirSupply() == -20) {
                setAirSupply(0);
                hurt(DamageSource.DROWN, 2.0F);
            }
        }
        else setAirSupply(300);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }
}
