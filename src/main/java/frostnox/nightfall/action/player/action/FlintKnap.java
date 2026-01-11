package frostnox.nightfall.action.player.action;

import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;

public class FlintKnap extends CarveAction{
    public FlintKnap(int[] duration, Properties properties) {
        super(duration, properties);
    }

    public FlintKnap(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public void onTick(LivingEntity user) {
        super.onTick(user);
        IActionTracker capA = ActionTracker.get(user);
        if(capA.getState() == getChargeState() && capA.getFrame() % 10 == 7) {
            Player player = (Player) user;
            player.playSound(SoundsNF.CARVE_STONE.get(), 0.6F, 0.97F + player.getRandom().nextFloat() * 0.06F);
        }
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
                tCalc.add(-8.75F/16F, 6F/16F, -1F/16F);
                rCalc.extend(0, 0, 100);
            }
            case 1 -> {
                data.tCalc.addWithCharge(6F/16F, 1F/16F, 0, Math.min(1F, (ActionTracker.get(user).getCharge() % 10F) / 3F), Easing.outSine);
                if(charge == 0F) data.tCalc.frame %= 10;
                if((charge > 0F ? ActionTracker.get(user).getCharge() : frame) % 10 > 3) {
                    if(charge == 0F) data.tCalc.frame -= 3;
                    data.tCalc.addWithCharge(-6F/16F, -1F/16F, 0, Math.min(1F, ((ActionTracker.get(user).getCharge() % 10F) - 3) / 4F), Easing.outCubic);
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
                    rightHand.rCalc.add(-100, -40, 0);
                    leftHand.rCalc.add(-35, 25, 0);
                }
                case 1 -> {
                    rightArm.rCalc.freeze();
                    leftArm.rCalc.freeze();
                    rightHand.rCalc.addWithCharge(-30, 0, 0, Math.min(1F, (ActionTracker.get(user).getCharge() % 10F) / 3F), Easing.outSine);
                    if(charge == 0F) rightHand.rCalc.frame %= 10;
                    if((charge > 0F ? ActionTracker.get(user).getCharge() : frame) % 10 > 3) {
                        if(charge == 0F) rightHand.rCalc.frame -= 3;
                        rightHand.rCalc.addWithCharge(30, 0, -0, Math.min(1F, ((ActionTracker.get(user).getCharge() % 10F) - 3) / 4F), Easing.outCubic);
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
                data.tCalc.add(0, 2F/16F, 0);
                data.rCalc.add(-160, 15, 0);
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
