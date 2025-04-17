package frostnox.nightfall.action.player.action;

import com.mojang.math.Vector3f;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;

public class SlingThrow extends LaunchProjectileAction {
    public SlingThrow(Properties properties, float speedMultiplier, float velocity, int... duration) {
        super(properties, speedMultiplier, velocity, duration);
    }

    @Override
    public void transformModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        super.transformModelFP(state, frame, duration, charge, user, data);
        AnimationCalculator tCalc = data.tCalc;
        AnimationCalculator rCalc = data.rCalc;
        Vector3f dTranslation = data.dTranslation;
        Vector3f dRotation = data.dRotation;
        switch(state) {
            case 0 -> {
                tCalc.add(0, 0.35F, 0);
                rCalc.add(-240, 0, 0, Easing.inSine);
            }
            case 1 -> {
                rCalc.setLooping(true);
                if(frame <= 10) rCalc.length = 10;
                else if(frame <= 10 + 9) {
                    rCalc.offset = 10;
                    rCalc.length = 9 + 10;
                }
                else if(frame <= 10 + 9 + 8) {
                    rCalc.offset = 19;
                    rCalc.length = 8 + 19;
                }
                else {
                    rCalc.offset = 27;
                    rCalc.length = 7 + 27;
                }
                rCalc.add(-360, 0, 0, Easing.none);
                tCalc.freeze();
            }
            case 2 -> {
                tCalc.extend(-6F / 16F, -2F / 16F, -2F / 16F, Easing.outCubic);
                rCalc.add(-240, 30, 0, Easing.outCubic);
            }
            case 3 -> {
                tCalc.freeze();
                rCalc.freeze();
            }
            case 4 -> {
                tCalc.extend(dTranslation);
                rCalc.wrapRotation();
                rCalc.extend(dRotation);
            }
        }
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        int side = AnimationUtil.getActiveSideModifier((Player) user);
        if(data.size() == 6) {
            AnimationData rightHand = data.get(EntityPart.getSidedHand(side));
            AnimationData rightArm = data.get(EntityPart.getSidedArm(side));
            AnimationData leftArm = data.get(EntityPart.getSidedArm(-side));
            AnimationData leftHand = data.get(EntityPart.getSidedHand(-side));
            switch(state) {
                case 0 -> {
                    mCalc.extend(0, 75, 0);
                    rightHand.rCalc.extend(-170, -35, -70 + pitch / 2);
                    rightArm.rCalc.extend(0, 0, 0);
                    leftArm.rCalc.extend(pitch / 4F, 0, 0);
                    leftHand.rCalc.extend(-35, 0, -11);
                }
                case 1 -> {
                    mCalc.freeze();
                    rightHand.rCalc.length /= 2;
                    rightHand.rCalc.add(charge > 0F ? 0F : (Mth.sin((frame + rightHand.rCalc.partialTicks) * 1.1F) * 2.5F), 0, 0, Easing.none);
                    rightArm.rCalc.freeze();
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                }
                case 2 -> {
                    mCalc.extend(2, -45, 0, Easing.outQuart);
                    rightHand.rCalc.extend(-35, -10, -30, Easing.outQuart);
                    rightArm.rCalc.freeze();
                    leftHand.rCalc.extend(8 - Math.abs(pitch) * 0.25F, 0, -15, Easing.outQuart);
                    leftArm.rCalc.freeze();
                }
                case 3 -> {
                    mCalc.freeze();
                    rightHand.rCalc.freeze();
                    rightArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                    leftArm.rCalc.freeze();
                }
                case 4 -> {
                    mCalc.extend(0, 0, 0);
                    rightHand.toDefaultRotation();
                    rightArm.rCalc.extend(rightArm.dRotation);
                    leftArm.rCalc.extend(leftArm.dRotation);
                    leftHand.rCalc.extend(leftHand.dRotation);
                }
            }
        }
    }

    @Override
    public void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        switch(state) {
            case 0 -> {
                data.rCalc.extend(data.dRotation.x(), 0, data.dRotation.z(), Easing.inSine);
            }
            case 1 -> {
                data.rCalc.setLooping(true);
                if(frame <= 10) data.rCalc.length = 10;
                else if(frame <= 10 + 9) {
                    data.rCalc.offset = 10;
                    data.rCalc.length = 9 + 10;
                }
                else if(frame <= 10 + 9 + 8) {
                    data.rCalc.offset = 19;
                    data.rCalc.length = 8 + 19;
                }
                else {
                    data.rCalc.offset = 27;
                    data.rCalc.length = 7 + 27;
                }
                data.rCalc.add(0, 0, 360, Easing.none);
            }
            case 2 -> {
                data.rCalc.add(-85, 90, 0, Easing.outCubic);
            }
            case 3 -> {
                data.rCalc.freeze();
            }
            case 4 -> {
                data.rCalc.wrapRotation();
                data.rCalc.extend(data.dRotation);
                data.tCalc.add(0, -6F/16F, 0);
            }
        }
    }
}
