package frostnox.nightfall.action.npc.dreg;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

public class DregCower extends Action {
    public DregCower(int[] duration) {
        super(duration);
    }

    public DregCower(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public int getChargeTimeout() {
        return Action.CHARGE_MAX;
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData leftHand = data.get(EntityPart.HAND_LEFT);
        AnimationData leftArm = data.get(EntityPart.ARM_LEFT);
        AnimationData rightHand = data.get(EntityPart.HAND_RIGHT);
        AnimationData rightArm = data.get(EntityPart.ARM_RIGHT);
        AnimationData head = data.get(EntityPart.HEAD);
        switch(state) {
            case 0 -> {
                rightArm.rCalc.extend(0, 0, 0);
                leftArm.rCalc.extend(0, 0, 0);
                rightHand.rCalc.extend(-140, -30, 0, Easing.outCubic);
                leftHand.rCalc.extend(-140, 30, 0, Easing.outCubic);
                head.rCalc.extend(20, 0, 0, Easing.outCubic);
            }
            case 1 -> {
                rightArm.rCalc.freeze();
                leftArm.rCalc.freeze();
                rightHand.rCalc.freeze();
                leftHand.rCalc.freeze();
                head.rCalc.freeze();
            }
            case 2 -> {
                rightArm.toDefaultRotation();
                leftArm.toDefaultRotation();
                rightHand.toDefaultRotation();
                leftHand.toDefaultRotation();
                head.toDefaultRotation();
            }
        }
    }
}
