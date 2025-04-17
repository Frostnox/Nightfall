package frostnox.nightfall.action.npc.dreg;

import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.entity.monster.DregEntity;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.world.entity.LivingEntity;

public class DregResurrect extends DregChannel {
    public DregResurrect(int[] duration) {
        super(duration);
    }

    public DregResurrect(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public void onTick(LivingEntity user) {
        super.onTick(user);
        if(!user.level.isClientSide) {
            DregEntity dreg = (DregEntity) user;
            IActionTracker capA = dreg.getActionTracker();
            if(capA.getState() == 1 && capA.getFrame() == capA.getDuration() && dreg.ally != null && !dreg.ally.isAlive() && !dreg.ally.isResurrecting()) {
                dreg.ally.resurrect();
                dreg.ally.playSound(SoundsNF.DREG_RESURRECT.get(), 1F, 1F);
                dreg.addEssence(-10F);
            }
        }
    }
}
