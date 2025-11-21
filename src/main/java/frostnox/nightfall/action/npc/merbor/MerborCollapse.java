package frostnox.nightfall.action.npc.merbor;

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

public class MerborCollapse extends CollapseAction {
    public MerborCollapse(int[] duration) {
        super(duration);
    }

    public MerborCollapse(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData body = data.get(EntityPart.BODY);
        AnimationData neck = data.get(EntityPart.NECK);
        AnimationData head = data.get(EntityPart.HEAD);
        AnimationData legRightF = data.get(EntityPart.LEG_RIGHT);
        AnimationData legLeftF = data.get(EntityPart.LEG_LEFT);
        AnimationData legRightH = data.get(EntityPart.LEG_2_RIGHT);
        AnimationData legLeftH = data.get(EntityPart.LEG_2_LEFT);
        switch(state) {
            case 0 -> {
                body.tCalc.extend(0, 17, 0, Easing.outCubic);
                legRightF.rCalc.extend(-90, 30, 0, Easing.outCubic);
                legRightF.tCalc.add(0, 4, 1, Easing.outCubic);
                legLeftF.rCalc.extend(-90, -30, 0, Easing.outCubic);
                legLeftF.tCalc.add(0, 4, 1, Easing.outCubic);
                legRightH.rCalc.extend(-90, 40, 0, Easing.outCubic);
                legRightH.tCalc.add(-0.5F, 4.5F, 0.5F, Easing.outCubic);
                legLeftH.rCalc.extend(-90, -40, 0, Easing.outCubic);
                legLeftH.tCalc.add(0.5F, 4.5F, 0.5F, Easing.outCubic);
                head.rCalc.extend(-110, 25 * (((ActionableEntity) user).getSynchedRandom() % 2 == 0 ? 1 : -1), 0, Easing.outCubic);
                neck.tCalc.extend(0, 0, -7F, Easing.outCubic);
                neck.rCalc.extend(120, 0, 0, Easing.outCubic);
            }
            case 1 -> {
                legRightF.rCalc.freeze();
                legRightF.tCalc.freeze();
                legLeftF.rCalc.freeze();
                legLeftF.tCalc.freeze();
                legRightH.rCalc.freeze();
                legRightH.tCalc.freeze();
                legLeftH.rCalc.freeze();
                legLeftH.tCalc.freeze();
                float f = (charge > 0 ? (ActionTracker.get(user).getCharge() - 2 + ActionTracker.get(user).getChargePartial()) :
                        (frame - 1 + head.rCalc.partialTicks)) * 0.2F;
                neck.rCalc.freeze();
                head.rCalc.freeze();
                body.tCalc.add(0, Mth.cos(f) * 0.1F,0);
                neck.tCalc.add(0, -Mth.cos(f) * 0.05F,0);
            }
            case 2 -> {
                body.toDefaultTranslation();
                legRightF.toDefault();
                legLeftF.toDefault();
                legRightH.toDefault();
                legLeftH.toDefault();
                head.toDefault();
                neck.toDefault();
            }
        }
    }
}
