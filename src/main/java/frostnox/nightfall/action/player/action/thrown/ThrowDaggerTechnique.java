package frostnox.nightfall.action.player.action.thrown;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class ThrowDaggerTechnique extends ThrowTechnique {
    public ThrowDaggerTechnique(DamageType[] damageType, int stunDuration, int[] duration, Properties properties, float throwVelocity, float curve, boolean spinning, @Nullable AttackEffect... effects) {
        super(damageType, stunDuration, duration, properties, throwVelocity, curve, spinning, effects);
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
                tCalc.extend(3.5F/16F, 12F/16F, 6F/16F, Easing.inOutSine);
                rCalc.extend(270, 80, 0, Easing.inOutSine);
            }
            case 1 -> {
                tCalc.addWithCharge(-1F/16F, 0F/16F, 6F/16F, charge, Easing.inOutSine);
                rCalc.freeze();
            }
            case 2 -> {
                tCalc.extend(-8F/16F, 8F/16F, -16F/16F, Easing.outQuart);
                rCalc.extend(90, 100, 0, Easing.outQuart);
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
                    mCalc.extend(0, 20, 0);
                    rightHand.rCalc.extend(-190 + pitch, -35, -15);
                    rightArm.rCalc.extend(0, 0, 0);
                    leftArm.rCalc.extend(pitch / 4F, 0, 0);
                    leftHand.rCalc.extend(-45, 0, -10);
                }
                case 1 -> {
                    mCalc.addWithCharge(0, 15, 0, charge);
                    rightArm.rCalc.freeze();
                    rightHand.rCalc.freeze();
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                }
                case 2 -> {
                    mCalc.extend(0, -45, 0);
                    mCalc.setEasing(Easing.outQuart);
                    rightArm.rCalc.freeze();
                    rightHand.rCalc.extend(-95 + pitch, 30, -pitch / 8F, Easing.outQuart);
                    leftHand.rCalc.extend(20, 0, -15, Easing.outQuart);
                    leftArm.rCalc.extend(0, 0, 0, Easing.outQuart);
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
        switch(state) {
            case 0 -> {
                data.rCalc.extend(data.dRotation.x() + 180, data.dRotation.y(), data.dRotation.z());
                data.tCalc.add(0, 8.01F/16F, 0F);
            }
            case 1 -> {
                data.rCalc.freeze();
                data.tCalc.freeze();
            }
            case 2 -> {
                data.rCalc.add(-90, 0, 0, Easing.outQuart);
                data.tCalc.add(0, -8.01F/16F, -10.01F/16F, Easing.outQuart);
            }
            case 3 -> {
                data.rCalc.freeze();
                data.tCalc.freeze();
            }
            case 4 -> {
                data.rCalc.extend(data.dRotation);
                data.tCalc.extend(data.dTranslation);
            }
        }
    }

    @Override
    public float getPitch(LivingEntity user, float partial) {
        return Mth.clamp(user.getViewXRot(partial), -80, 75);
    }
}
