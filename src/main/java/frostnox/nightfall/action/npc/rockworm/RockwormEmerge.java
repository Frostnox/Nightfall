package frostnox.nightfall.action.npc.rockworm;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

public class RockwormEmerge extends Action {
    public RockwormEmerge(int[] duration) {
        super(duration);
    }

    public RockwormEmerge(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        ActionsNF.ROCKWORM_RETREAT.get().transformModelSingle(state, frame, duration, charge, pitch, user, data, mCalc);
        AnimationData head = data.get(EntityPart.HEAD);
        AnimationData lowerBody = data.get(EntityPart.BODY);
        AnimationData upperBody = data.get(EntityPart.BODY_2);
        lowerBody.tCalc.add(0, -36, -3, Easing.outSine);
        lowerBody.rCalc.extend(lowerBody.dRotation, Easing.outSine);
        upperBody.rCalc.extend(upperBody.dRotation, Easing.outSine);
        head.rCalc.extend(head.dRotation, Easing.outSine);
    }
}
