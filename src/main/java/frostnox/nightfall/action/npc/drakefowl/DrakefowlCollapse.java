package frostnox.nightfall.action.npc.drakefowl;

import frostnox.nightfall.action.npc.CollapseAction;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

public class DrakefowlCollapse extends CollapseAction {
    public DrakefowlCollapse(int[] duration) {
        super(duration);
    }

    public DrakefowlCollapse(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData body = data.get(EntityPart.BODY);
        AnimationData neck = data.get(EntityPart.NECK);
        AnimationData head = data.get(EntityPart.HEAD);
        AnimationData legRight = data.get(EntityPart.LEG_RIGHT);
        AnimationData legLeft = data.get(EntityPart.LEG_LEFT);
        switch(state) {
            case 0 -> {
                body.tCalc.extend(0, 22, 0, Easing.outCubic);
                legRight.rCalc.extend(-90, 35, 0, Easing.outCubic);
                legRight.tCalc.add(0, 4, 0, Easing.outCubic);
                legLeft.rCalc.extend(-90, -35, 0, Easing.outCubic);
                legLeft.tCalc.add(0, 4, 0, Easing.outCubic);
                head.rCalc.extend(-110, 30 * (((ActionableEntity) user).getSynchedRandom() % 2 == 0 ? 1 : -1), 0, Easing.outCubic);
                neck.tCalc.extend(0, -1, -3F, Easing.outCubic);
                neck.rCalc.extend(120, 0, 0, Easing.outCubic);
            }
            case 1 -> {
                legRight.rCalc.freeze();
                legRight.tCalc.freeze();
                legLeft.rCalc.freeze();
                legLeft.tCalc.freeze();
                float f = (charge > 0 ? (ActionTracker.get(user).getCharge() - 2 + ActionTracker.get(user).getChargePartial()) :
                        (frame - 1 + head.rCalc.partialTicks)) * 0.3F;
                neck.rCalc.freeze();
                head.rCalc.freeze();
                body.tCalc.add(0, Mth.cos(f) * 0.08F,0);
                neck.tCalc.add(0, -Mth.cos(f) * 0.04F,0);
            }
            case 2 -> {
                body.toDefaultTranslation();
                legRight.toDefault();
                legLeft.toDefault();
                head.toDefault();
                neck.toDefault();
            }
        }
    }
}
