package frostnox.nightfall.action.player.action;

import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;

public class AxeCarveAction extends WoodCarveAction {
    public AxeCarveAction(int[] duration, Properties properties) {
        super(properties, duration);
    }

    public AxeCarveAction(Properties properties, int... duration) {
        super(properties, duration);
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
                tCalc.add(-13F/16F, 10F/16F, -10F/16F);
                rCalc.extend(-10, 245, -90);
            }
            case 1 -> {
                data.tCalc.addWithCharge(1F/16F, -5F/16F, 0, Math.min(1F, (ActionTracker.get(user).getCharge() % 10F) / 3F), Easing.outSine);
                if(charge == 0F) data.tCalc.frame %= 10;
                if((charge > 0F ? ActionTracker.get(user).getCharge() : frame) % 10 > 3) {
                    if(charge == 0F) data.tCalc.frame -= 3;
                    data.tCalc.addWithCharge(-1F/16F, 5F/16F, 0, Math.min(1F, ((ActionTracker.get(user).getCharge() % 10F) - 3) / 4F));
                    data.tCalc.length = 4;
                }
                else data.tCalc.length = 3;
                rCalc.freeze();
            }
            case 2 -> {
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
            switch(state) {
                case 0 -> {
                    rightArm.rCalc.extend(0, 0, 0);
                    leftArm.rCalc.extend(0, 0, 0);
                    rightHand.rCalc.add(-60, -40, 0);
                    leftHand.rCalc.add(-35, 20, 0);
                }
                case 1 -> {
                    rightArm.rCalc.freeze();
                    leftArm.rCalc.freeze();
                    rightHand.rCalc.addWithCharge(30, 0, 0, Math.min(1F, (ActionTracker.get(user).getCharge() % 10F) / 3F), Easing.outSine);
                    if(charge == 0F) rightHand.rCalc.frame %= 10;
                    if((charge > 0F ? ActionTracker.get(user).getCharge() : frame) % 10 > 3) {
                        if(charge == 0F) rightHand.rCalc.frame -= 3;
                        rightHand.rCalc.addWithCharge(-30, 0, -0, Math.min(1F, ((ActionTracker.get(user).getCharge() % 10F) - 3) / 4F));
                        rightHand.rCalc.length = 4;
                    }
                    else rightHand.rCalc.length = 3;
                    leftHand.rCalc.freeze();
                }
                case 2 -> {
                    rightArm.toDefaultRotation();
                    leftArm.toDefaultRotation();
                    rightHand.toDefaultRotation();
                    leftHand.toDefaultRotation();
                }
            }
        }
    }

    @Override
    public void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        switch(state) {
            case 0 -> {
                data.tCalc.add(0, -5F/16F, 0);
                data.rCalc.add(-10, 0, 0);
            }
            case 1 -> {
                data.tCalc.freeze();
                data.rCalc.freeze();
            }
            case 2 -> {
                data.toDefault();
            }
        }
    }
}
