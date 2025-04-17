package frostnox.nightfall.action.player.action;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.player.PlayerAction;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;

public class DigCharged extends PlayerAction {
    public DigCharged(int[] duration, Properties properties) {
        super(properties, duration);
    }

    public DigCharged(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public void transformModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        super.transformModelFP(state, frame, duration, charge, user, data);
        AnimationCalculator tCalc = data.tCalc;
        AnimationCalculator rCalc = data.rCalc;
        AnimationCalculator sCalc = data.sCalc;
        Vector3f dTranslation = data.dTranslation;
        Vector3f dRotation = data.dRotation;
        Vector3f dScale = data.dScale;
        switch(state) {
            case 0 -> {
                tCalc.extendWithCharge(3.5F/16F, 2.25F/16F, 14F/16F, charge, Easing.outSine);
                rCalc.length = rCalc.length/2;
                rCalc.extend(-120, 80, 20);
            }
            case 1 -> {
                tCalc.extend(-5.5F/16F, 6F/16F, -8F/16F, Easing.outSine);
                rCalc.extend(-120, 80, 20, Easing.outSine);
            }
            case 2 -> {
                tCalc.extend(-1F/16F, 6F/16F, -2F/16F, Easing.inSine);
                rCalc.extend(-25, 110, 0, Easing.outSine);
            }
            case 3 -> {
                tCalc.freeze();
                rCalc.freeze();
            }
            case 4 -> {
                tCalc.extend(dTranslation);
                rCalc.extend(dRotation);
            }
        }
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
            switch (state) {
                case 0 -> {
                    mCalc.extendWithCharge(0, 30, 0, charge, Easing.outSine);
                    rightArm.rCalc.extendWithCharge(0, 0, 0, charge, Easing.outQuart);
                    rightHand.rCalc.extendWithCharge(30 + pitch / 2F, 0, 10, charge, Easing.outQuart);
                    leftArm.rCalc.extendWithCharge(-Math.abs(pitch/2F), 0, 0, charge, Easing.outQuart);
                    leftHand.rCalc.extendWithCharge(-20, 0, -10, charge, Easing.outQuart);
                }
                case 1 -> {
                    mCalc.extend(0, -25, 0, Easing.outCubic);
                    rightArm.rCalc.freeze();
                    rightHand.rCalc.extend(-45 + pitch, 8, -pitch / 8F, Easing.outCubic);
                    leftHand.rCalc.extend(15, 0, -15);
                    leftArm.rCalc.extend(0, 0, 0);
                }
                case 2 -> {
                    mCalc.freeze();
                    rightArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                    leftArm.rCalc.freeze();
                    rightHand.rCalc.extend(-85 + pitch, 8, -pitch / 8F, Easing.outCubic);
                }
                case 3 -> {
                    mCalc.freeze();
                    rightArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                    leftArm.rCalc.freeze();
                    rightHand.rCalc.freeze();
                }
                case 4 -> {
                    mCalc.extend(0, 0, 0);
                    mCalc.setEasing(Easing.inOutSine);
                    rightArm.rCalc.extend(rightArm.dRotation);
                    leftArm.rCalc.extend(leftArm.dRotation);
                    leftHand.rCalc.extend(leftHand.dRotation);
                    rightHand.rCalc.extend(rightHand.dRotation);
                }
            }
        }
    }

    @Override
    public void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        int offset;
        int side = AnimationUtil.getActiveSideModifier((Player) user);
        switch (state) {
            case 0 -> {
                data.rCalc.freeze();
            }
            case 1 -> {
                data.rCalc.add(-45, 0, 0, Easing.outCubic);
            }
            case 2 -> {
                data.rCalc.extend(data.dRotation, Easing.outCubic);
            }
            case 3 -> {
                data.rCalc.freeze();
            }
            case 4 -> {
                data.rCalc.extend(data.dRotation);
            }
        }
    }

    @Override
    public float getPitch(LivingEntity user, float partial) {
        return Mth.clamp(user.getViewXRot(partial) - (user.isCrouching() ? 11 : 14), -80, 75);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public int getBlockHitFrame(int state, LivingEntity user) {
        return getDuration(1, user);
    }
}
