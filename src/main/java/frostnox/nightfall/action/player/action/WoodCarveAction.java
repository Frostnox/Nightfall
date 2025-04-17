package frostnox.nightfall.action.player.action;

import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class WoodCarveAction extends CarveAction{
    public WoodCarveAction(int[] duration, Properties properties) {
        super(duration, properties);
    }

    public WoodCarveAction(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public void onTick(LivingEntity user) {
        super.onTick(user);
        IActionTracker capA = ActionTracker.get(user);
        if(capA.getState() == getChargeState() && capA.getFrame() % 10 == 1) {
            Player player = (Player) user;
            player.playSound(SoundsNF.CARVE_WOOD.get(), 0.6F, 0.97F + player.getRandom().nextFloat() * 0.06F);
        }
    }
}
