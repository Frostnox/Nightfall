package frostnox.nightfall.action.player.action;

import net.minecraft.world.entity.LivingEntity;

public abstract class ReloadAction extends MoveSpeedPlayerAction {
    public ReloadAction(float speedMultiplier, int... duration) {
        super(speedMultiplier, duration);
    }

    public ReloadAction(Properties properties, float speedMultiplier, int... duration) {
        super(properties, speedMultiplier, duration);
    }

    @Override
    public void onTick(LivingEntity user) {
        super.onTick(user);

    }
}