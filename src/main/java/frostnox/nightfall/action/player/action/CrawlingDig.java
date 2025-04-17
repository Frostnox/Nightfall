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

public class CrawlingDig extends PlayerAction {
    public CrawlingDig(int[] duration, Properties properties) {
        super(properties, duration);
    }

    public CrawlingDig(Properties properties, int... duration) {
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
                tCalc.extend(3.5F/16F, 2.25F/16F, 12F/16F);
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
        int side = AnimationUtil.getActiveSideModifier((Player) user);
        if(data.size() == 6) {
            AnimationData rightHand = data.get(EntityPart.getSidedHand(side));
            AnimationData rightArm = data.get(EntityPart.getSidedArm(side));
            AnimationData leftArm = data.get(EntityPart.getSidedArm(-side));
            AnimationData leftHand = data.get(EntityPart.getSidedHand(-side));
            AnimationData rightLeg = data.get(EntityPart.LEG_RIGHT);
            AnimationData leftLeg = data.get(EntityPart.LEG_LEFT);
            switch (state) {
                case 0 -> {
                    mCalc.setVectors(0, 0, 0, -1, 2, 0);
                    rightHand.rCalc.add(0, 20, 90);
                    rightArm.rCalc.extend(-180 + pitch, 0, 0);
                }
                case 1 -> {
                    mCalc.extend(0, -2, 0, Easing.outCubic);
                    rightHand.rCalc.add(0, -20, -95, Easing.outCubic);
                    rightArm.rCalc.freeze();
                }
                case 2 -> {
                    mCalc.freeze();
                    rightHand.rCalc.add(-30, 0, 0, Easing.outCubic);
                    rightArm.rCalc.freeze();
                }
                case 3 -> {
                    mCalc.freeze();
                    rightArm.rCalc.freeze();
                    rightHand.rCalc.freeze();
                }
                case 4 -> {
                    mCalc.extend(0, 0, 0);
                    rightHand.toDefaultRotation();
                    rightArm.toDefaultRotation();
                }
            }
        }
    }

    @Override
    public void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        int side = AnimationUtil.getActiveSideModifier((Player) user);
        switch (state) {
            case 0 -> {
                data.rCalc.extend(data.dRotation.x(), data.dRotation.y() + 90, data.dRotation.z() - 90);
            }
            case 1 -> {
                data.rCalc.add(0, -90, 0, Easing.outQuart);
            }
            case 2 -> {
                data.rCalc.extend(data.dRotation, Easing.outCubic);
            }
            case 3 -> {
                data.rCalc.freeze();
            }
            case 4 -> {
                data.toDefaultRotation();
            }
        }
    }

    @Override
    public float getPitch(LivingEntity user, float partial) {
        return Mth.clamp(user.getViewXRot(partial) - 14, -70, 70);
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
