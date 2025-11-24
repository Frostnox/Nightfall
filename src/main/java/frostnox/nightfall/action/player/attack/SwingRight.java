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

public class SwingRight extends PlayerAttack implements IClientAction {
    public SwingRight(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public SwingRight(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    public SwingRight(DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(0F, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
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
                tCalc.extend(10F/16F, 4F/16F, 0F/16F);
                rCalc.extend(-69, 175, 60);
            }
            case 1 -> {
                tCalc.extend(-20 / 16F, 4F / 16F, 1 / 16F, Easing.outCubic);
                rCalc.add(0, 0, -90, Easing.outQuart);
            }
            case 2 -> {
                tCalc.freeze();
                rCalc.freeze();
            }
            case 3 -> {
                rCalc.extend(dRotation);
                tCalc.extend(dTranslation);
            }
        }
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        int offset;
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
                    mCalc.extend(0, 20, 0);
                    rightHand.rCalc.extend(0, 90, 70);
                    rightArm.rCalc.extend(pitch - 90, 0, -pitch);
                    leftArm.rCalc.extend(pitch / 4F, 0, 0);
                    leftHand.rCalc.extend(-35, 0, -10);
                }
                case 1 -> {
                    mCalc.extend(0, -45, 0, Easing.outQuart);
                    rightHand.rCalc.extend(20, 90, 0, Easing.outCubic);
                    rightArm.rCalc.add(0, 0, pitch, Easing.outCubic);
                    leftHand.rCalc.add(70, 0, -15);
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
                    offset = duration / 2;
                    mCalc.extend(0, 0, 0);
                    rightHand.rCalc.extend(rightHand.dRotation.x(), 90, rightHand.dRotation.z());
                    rightHand.rCalc.length = offset;
                    rightHand.rCalc.setEasing(Easing.inSine);
                    rightArm.rCalc.extend(rightArm.dRotation);
                    leftArm.rCalc.extend(leftArm.dRotation);
                    leftHand.rCalc.extend(leftHand.dRotation);
                    if (frame > offset) {
                        rightHand.rCalc.extend(rightHand.dRotation);
                        rightHand.rCalc.length = duration;
                        rightHand.rCalc.offset = offset;
                        rightHand.rCalc.setEasing(Easing.outSine);
                    }
                }
            }
        }
    }

    @Override
    public void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        switch(state) {
            case 0 -> {
                data.toDefaultRotation();
            }
            case 1 -> {
                data.rCalc.length = duration / 2;
                data.rCalc.add(-90, 0, 0);
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
        return Mth.clamp(user.getViewXRot(partial) - (user.isCrouching() ? 5 : 10), -50, 50);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public int getDamageStartFrame(int state) {
        return 1;
    }

    @Override
    public int getDamageEndFrame(int state, LivingEntity user) {
        return getDuration(state, user) - 2;
    }

    @Override
    public int getBlockHitFrame(int state, LivingEntity user) {
        return Math.max(1, super.getBlockHitFrame(state, user) - 1);
    }
}
