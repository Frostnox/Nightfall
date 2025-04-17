package frostnox.nightfall.action.player;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.registry.ActionsNF;

import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class PlayerAction extends Action implements IClientAction {
    public PlayerAction(int... duration) {
        super(duration);
    }

    public PlayerAction(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public boolean canStart(LivingEntity user) {
        if(super.canStart(user)) return PlayerData.get((Player) user).hasNoSwapDelay();
        else return false;
    }

    @Override
    public Action getAction() {
        return this;
    }

    @Override
    public float getPitch(LivingEntity user, float partial) {
        return user.getViewXRot(partial);
    }
}
