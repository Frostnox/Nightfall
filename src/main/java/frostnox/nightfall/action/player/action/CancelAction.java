package frostnox.nightfall.action.player.action;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.player.PlayerAction;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;

public class CancelAction extends PlayerAction {
    public CancelAction(int[] duration, Properties properties) {
        super(properties, duration);
    }

    public CancelAction(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public void transformModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        super.transformModelFP(state, frame, duration, charge, user, data);
        AnimationCalculator tCalc = data.tCalc;
        AnimationCalculator rCalc = data.rCalc;
        Vector3f dTranslation = data.dTranslation;
        Vector3f dRotation = data.dRotation;
        tCalc.extend(dTranslation);
        rCalc.extend(dRotation);
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        if(data.size() == 6) {
            int side = AnimationUtil.getActiveSideModifier((Player) user);
            AnimationData rightHand = data.get(EntityPart.getSidedHand(side));
            AnimationData rightArm = data.get(EntityPart.getSidedArm(side));
            AnimationData leftArm = data.get(EntityPart.getSidedArm(-side));
            AnimationData leftHand = data.get(EntityPart.getSidedHand(-side));
            AnimationData rightLeg = data.get(EntityPart.LEG_RIGHT);
            AnimationData leftLeg = data.get(EntityPart.LEG_LEFT);
            mCalc.extend(0, 0, 0, Easing.inOutSine);
            rightArm.rCalc.extend(rightArm.dRotation);
            leftArm.rCalc.extend(leftArm.dRotation);
            leftHand.rCalc.extend(leftHand.dRotation);
            rightHand.rCalc.extend(rightHand.dRotation);
            rightArm.tCalc.extend(rightArm.dTranslation);
            leftArm.tCalc.extend(leftArm.dTranslation);
            leftHand.tCalc.extend(leftHand.dTranslation);
            rightHand.tCalc.extend(rightHand.dTranslation);
        }
    }

    @Override
    public void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        data.rCalc.extend(data.dRotation);
        data.tCalc.extend(data.dTranslation);
    }
}
