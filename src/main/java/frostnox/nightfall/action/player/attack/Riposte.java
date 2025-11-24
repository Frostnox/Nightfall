package frostnox.nightfall.action.player.attack;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.player.IClientAction;
import frostnox.nightfall.action.player.PlayerAttack;
import frostnox.nightfall.capability.IActionTracker;
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

public class Riposte extends PlayerAttack {
    public Riposte(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public Riposte(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    public Riposte(DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(0, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public void transformOppositeHandFP(AnimationCalculator tCalc, int xSide, int side, IActionTracker capA) {
        if(getAction().getTotalStates() > 1) {
            if(capA.getState() == 0) tCalc.add(0.95F * xSide * side, -0.95F * side, 0);
            else tCalc.setStaticVector(0.95F * xSide * side, -0.95F * side, 0);
            if(capA.getState() == getAction().getTotalStates() - 1) tCalc.add(-0.95F * xSide * side, 0.95F * side, 0);
        }
    }

    @Override
    public void transformModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        super.transformModelFP(state, frame, duration, charge, user, data);
        int offset;
        AnimationCalculator tCalc = data.tCalc;
        AnimationCalculator rCalc = data.rCalc;
        AnimationCalculator sCalc = data.sCalc;
        Vector3f dTranslation = data.dTranslation;
        Vector3f dRotation = data.dRotation;
        Vector3f dScale = data.dScale;
        switch(state) {
            case 0 -> {
                tCalc.extend(-13.5F/16F, 8F/16F, 1F/16F, Easing.inOutSine);
                rCalc.extend(60, 80, 0, Easing.inOutSine);
            }
            case 1 -> {
                tCalc.extend(-2F / 16F, -9F / 16F, -3F / 16F, Easing.outQuart);
                rCalc.extend(-50, 50, -10, Easing.outQuart);
            }
            case 2 -> {
                tCalc.freeze();
                rCalc.freeze();
            }
            case 3 -> {
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
            switch (state) {
                case 0 -> {
                    mCalc.extend(0, -45, 0);
                    rightHand.rCalc.extend(-130 + pitch, -11, 45);
                    rightArm.rCalc.extend(0, 0, 0);
                    leftHand.rCalc.extend(20, 0, -5, Easing.outCubic);
                    leftArm.rCalc.extend(0, 0, 0, Easing.outCubic);
                }
                case 1 -> {
                    mCalc.extend(0, 20, 0, Easing.outQuart);
                    rightHand.rCalc.extend(-20 + pitch, -5, 25, Easing.outQuart);
                    rightArm.rCalc.freeze();
                    leftHand.rCalc.extend(-35, 0, -10, Easing.outQuart);
                    leftArm.rCalc.freeze();
                }
                case 2 -> {
                    mCalc.freeze();
                    rightHand.rCalc.freeze();
                    rightArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                    leftArm.rCalc.freeze();
                }
                case 3 -> {
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
        switch (state) {
            case 0 -> {
                data.rCalc.extend(data.dRotation.x(), data.dRotation.y() - 5, data.dRotation.z());
            }
            case 1 -> {
                data.rCalc.add(-30, 0, 0);
                data.rCalc.setEasing(Easing.outQuart);
            }
            case 2 -> {
                data.rCalc.freeze();
            }
            case 3 -> {
                data.rCalc.extend(data.dRotation);
            }
        }
    }

    @Override
    public float getPitch(LivingEntity user, float partial) {
        return Mth.clamp(user.getViewXRot(partial), -35, 5);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public int getBlockHitFrame(int state, LivingEntity user) {
        return Math.max(1, super.getBlockHitFrame(state, user) - 2);
    }

    @Override
    public int getDamageStartFrame(int state) {
        return 1;
    }

    @Override
    public int getDamageEndFrame(int state, LivingEntity user) {
        return getDuration(state, user) - 2;
    }
}
