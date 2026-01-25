package frostnox.nightfall.action.player.action;

import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;

public class FirestarterReload extends ReloadAction {
    public FirestarterReload(float speedMultiplier, int... duration) {
        super(speedMultiplier, duration);
    }

    public FirestarterReload(Properties properties, float speedMultiplier, int... duration) {
        super(properties, speedMultiplier, duration);
    }

    @Override
    public void transformModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        super.transformModelFP(state, frame, duration, charge, user, data);
        switch(state) {
            case 0 -> {
                data.tCalc.add(-12F/16F, 0F/16F, 0.5F/16F);
                data.rCalc.extend(85, 0, 90);
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

    @Override
    public void transformOppositeHandFP(AnimationCalculator tCalc, int xSide, int side, IActionTracker capA) {
        tCalc.add(-7F/16F * xSide * side, 5F/16F, -3F/16F * side);
        if(capA.getState() >= 1) {
            tCalc.add(0, -1F/16F, 0, Easing.inSine);
            if(capA.getState() == 1) tCalc.length = capA.getDuration();
        }
        if(capA.getState() >= 2) tCalc.extend(Vector3f.ZERO);
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
                    rightHand.rCalc.add(-50, -40, 0);
                    leftHand.rCalc.add(-35, 25, 0);
                }
                case 1 -> {
                    rightArm.rCalc.freeze();
                    leftArm.rCalc.freeze();
                    rightHand.rCalc.addWithCharge(-35, 0, 0, Math.min(1F, (ActionTracker.get(user).getCharge() % 10F) / 3F), Easing.outSine);
                    if(charge == 0F) rightHand.rCalc.frame %= 10;
                    if((charge > 0F ? ActionTracker.get(user).getCharge() : frame) % 10 > 3) {
                        if(charge == 0F) rightHand.rCalc.frame -= 3;
                        rightHand.rCalc.addWithCharge(35, 0, -0, Math.min(1F, ((ActionTracker.get(user).getCharge() % 10F) - 3) / 4F), Easing.outCubic);
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
                data.tCalc.add(0, -6F/16F, 1.1F/16F);
            }
            case 1 -> {
                data.tCalc.freeze();
            }
            case 2 -> {
                data.toDefaultTranslation();
            }
        }
    }
}