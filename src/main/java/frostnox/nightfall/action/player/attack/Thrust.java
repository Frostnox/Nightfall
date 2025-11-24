package frostnox.nightfall.action.player.attack;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.player.IClientAction;
import frostnox.nightfall.action.player.PlayerAttack;
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

public class Thrust extends PlayerAttack implements IClientAction {
    public Thrust(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public Thrust(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    public Thrust(DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(0, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
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
        switch (state) {
            case 0 -> {
                tCalc.extend(3.5F/16F, 2.25F/16F, 16F/16F);
                rCalc.extend(-120, 80, 35);
            }
            case 1 -> {
                tCalc.extend(-6F/16F, 6F/16F, -7F/16F, Easing.outQuart);
                rCalc.freeze();
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
                    mCalc.extend(0, 40, 0);
                    rightArm.rCalc.extend(0, 0, 0);
                    rightHand.rCalc.extend(30 + pitch / 2F, -15, 10);
                    rightHand.tCalc.extend(0, 0, 1F);
                    leftArm.rCalc.extend(pitch/4F, 0, 0);
                    leftHand.rCalc.extend(-20, 0, -10);
                }
                case 1 -> {
                    mCalc.extend(0, -40, 0);
                    mCalc.setEasing(Easing.outCubic);
                    rightArm.rCalc.freeze();
                    rightHand.rCalc.extend(-90 + pitch, 32F, -pitch / 8F);
                    rightHand.rCalc.setEasing(Easing.outCubic);
                    rightHand.tCalc.extend(0, 0, -1);
                    rightHand.tCalc.setEasing(Easing.outQuart);
                    leftHand.rCalc.extend(20, 0, -15);
                    leftArm.rCalc.extend(0, 0, 0);
                }
                case 2 -> {
                    mCalc.freeze();
                    rightArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                    leftArm.rCalc.freeze();
                    rightHand.rCalc.freeze();
                    rightHand.tCalc.freeze();
                }
                case 3 -> {
                    mCalc.extend(0, 0, 0);
                    mCalc.setEasing(Easing.inOutSine);
                    rightArm.rCalc.extend(rightArm.dRotation);
                    leftArm.rCalc.extend(leftArm.dRotation);
                    leftHand.rCalc.extend(leftHand.dRotation);
                    rightHand.rCalc.extend(rightHand.dRotation);
                    rightHand.tCalc.extend(rightHand.dTranslation);
                }
            }
        }
    }

    @Override
    public void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        switch(state) {
            case 0 -> {
                data.rCalc.extend(data.dRotation.x() + 30, data.dRotation.y(), data.dRotation.z());
            }
            case 1 -> {
                data.rCalc.setEasing(Easing.outCubic);
                data.rCalc.extend(-90, 90, 0);
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
        return Mth.clamp(user.getViewXRot(partial) - (user.isCrouching() ? 11 : 14), -80, 70);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public int getDamageStartFrame(int state) {
        return 1;
    }
}
