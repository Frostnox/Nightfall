package frostnox.nightfall.action.npc;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.entity.animal.TamableAnimalEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public abstract class CollapseAction extends Action {
    public CollapseAction(int[] duration) {
        super(duration);
    }

    public CollapseAction(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public int getChargeTimeout() {
        return 20 * 60;
    }

    @Override
    public int getRequiredCharge(LivingEntity user) {
        return 0;
    }

    @Override
    public boolean allowFlag(int state, Goal.Flag flag) {
        return false;
    }

    @Override
    public void onTick(LivingEntity user) {
        IActionTracker capA = ActionTracker.get(user);
        if(capA.getState() == getChargeState() && capA.getFrame() == 1) {
            if(user instanceof TamableAnimalEntity animal) animal.tamable = true;
        }
    }

    @Override
    public void onStart(LivingEntity user) {
        ((ActionableEntity) user).getNavigator().stop();
    }

    @Override
    public void onEnd(LivingEntity user) {
        if(user instanceof TamableAnimalEntity animal) animal.tamable = false;
        user.heal(user.getMaxHealth() / 10);
    }
}