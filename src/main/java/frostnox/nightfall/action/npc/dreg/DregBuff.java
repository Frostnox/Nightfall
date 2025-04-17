package frostnox.nightfall.action.npc.dreg;

import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.entity.monster.DregEntity;
import frostnox.nightfall.registry.forge.EffectsNF;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class DregBuff extends DregChannel {
    public DregBuff(int[] duration) {
        super(duration);
    }

    public DregBuff(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public void onTick(LivingEntity user) {
        super.onTick(user);
        DregEntity dreg = (DregEntity) user;
        IActionTracker capA = dreg.getActionTracker();
        if(capA.getState() == 1 && dreg.ally != null) {
            dreg.ally.addEffect(new MobEffectInstance(EffectsNF.MOON_BLESSING.get(), 2));
        }
    }
}
