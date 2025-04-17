package frostnox.nightfall.action.player.attack;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
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

public class LongVerticalSwingCharged extends PlayerAttack {
    public LongVerticalSwingCharged(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public LongVerticalSwingCharged(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    public LongVerticalSwingCharged(DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(0, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
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
                tCalc.extendWithCharge(-3.5F/16F, 10.5F/16F, 3F/16F, charge, Easing.outQuart);
                rCalc.extendWithCharge(60, 100, 0, charge, Easing.outQuart);
            }
            case 1 -> {
                tCalc.extendWithHitStop(user, getDuration(1, user), -6F / 16F, -9F / 16F, -3F / 16F, Easing.outQuart);
                rCalc.extendWithHitStop(user, getDuration(1, user), 0, 100, -60, Easing.outQuart);
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
            switch(state) {
                case 0 -> {
                    mCalc.extendWithCharge(-3, 30, 0, charge, Easing.outQuart);
                    rightHand.rCalc.extendWithCharge(-190 + pitch, -15, -15, charge, Easing.outQuart);
                    rightArm.rCalc.extendWithCharge(0, 0, 0, charge, Easing.outQuart);
                    leftArm.rCalc.extendWithCharge(pitch / 4F, 0, 0, charge, Easing.outQuart);
                    leftHand.rCalc.extendWithCharge(-35, 0, -11, charge, Easing.outQuart);
                }
                case 1 -> {
                    mCalc.extendWithHitStop(user, getDuration(1, user), 3, -22, 0, Easing.outQuart);
                    rightHand.rCalc.extendWithHitStop(user, getDuration(1, user), -12 + pitch, (-10 + (pitch > 0 ? pitch/3 : 0)), 0, Easing.outQuart);
                    rightArm.rCalc.freeze();
                    leftHand.rCalc.extendWithHitStop(user, getDuration(1, user), 8 - Math.abs(pitch) * 0.25F, 0, -15, Easing.outQuart);
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
                data.rCalc.extendWithCharge(data.dRotation, charge, Easing.outQuart);
            }
            case 1 -> {
                data.rCalc.freeze();
            }
            case 2 -> {
                data.rCalc.freeze();
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
        return Mth.clamp(user.getViewXRot(partial), -70, 45);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }
}
