package frostnox.nightfall.action.npc.rockworm;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.RockwormEntity;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

public class RockwormRetreat extends Action {
    public RockwormRetreat(int[] duration) {
        super(duration);
    }

    public RockwormRetreat(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData head = data.get(EntityPart.HEAD);
        AnimationData lowerBody = data.get(EntityPart.BODY);
        AnimationData upperBody = data.get(EntityPart.BODY_2);
        lowerBody.tCalc.add(0, 36, 3, Easing.inSine);
        lowerBody.rCalc.extend(0, 0, 0, Easing.inSine);
        upperBody.rCalc.extend(0, 0, 0, Easing.inSine);
        head.rCalc.extend(0, 0, 0, Easing.inSine);
    }

    @Override
    public void onTick(LivingEntity user) {
        if(user instanceof RockwormEntity rockworm) {
            IActionTracker capA = ActionTracker.get(user);
            if(capA.getFrame() == capA.getDuration()) {
                if(!user.level.isClientSide) rockworm.enterNest(false);
                else if(rockworm.enterNest(true)) rockworm.discard();
            }
        }
    }
}
