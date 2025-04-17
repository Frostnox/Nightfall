package frostnox.nightfall.action.npc.husk;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.npc.NPCAttack;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.HuskEntity;
import frostnox.nightfall.item.IWeaponItem;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class HuskOverhead extends NPCAttack {
    public HuskOverhead(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public HuskOverhead(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 2;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 2.1;
    }

    @Override
    protected EnumMap<EntityPart, AnimationData> getDefaultAnimationData() {
        return HuskEntity.getRightArmAnimMap();
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData body = data.get(EntityPart.BODY);
        AnimationData rightArm = data.get(EntityPart.ARM_RIGHT);
        AnimationData rightHand = data.get(EntityPart.HAND_RIGHT);
        switch(state) {
            case 0 -> {
                body.rCalc.extend(-22.5F, 0, 0);
                rightArm.rCalc.extend(pitch, 0, 0);
                rightHand.rCalc.extend(-235, 5, 0);
                rightHand.tCalc.add(0, 1.5F, 0);
            }
            case 1 -> {
                body.rCalc.freeze();
                rightArm.rCalc.freeze();
                rightHand.rCalc.freeze();
                rightHand.tCalc.freeze();
            }
            case 2 -> {
                body.rCalc.extend(40, 0, 0);
                body.rCalc.setEasing(Easing.outCubic);
                rightArm.rCalc.freeze();
                rightHand.rCalc.extend(-45, -30, 0);
                rightHand.rCalc.setEasing(Easing.outCubic);
                rightHand.tCalc.add(0, -1.5F, 0);
                rightHand.tCalc.setEasing(Easing.outCubic);
            }
            case 3 -> {
                body.rCalc.extend(body.dRotation);
                rightArm.rCalc.extend(rightArm.dRotation);
                rightHand.rCalc.extend(rightHand.dRotation);
                rightHand.tCalc.extend(rightHand.dTranslation);
            }
        }
        if(data.size() != 3) {
            AnimationData leftArm = data.get(EntityPart.ARM_LEFT);
            AnimationData leftHand = data.get(EntityPart.HAND_LEFT);
            switch(state) {
                case 0 -> {
                    leftArm.rCalc.extend(pitch, 0, 0);
                    leftHand.rCalc.extend(-235, -5, 0);
                    leftHand.tCalc.add(0, 1.5F, 0);
                }
                case 1 -> {
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                    leftHand.tCalc.freeze();
                }
                case 2 -> {
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.extend(-45, 30, 0);
                    leftHand.rCalc.setEasing(Easing.outCubic);
                    leftHand.tCalc.add(0, -1.5F, 0);
                    leftHand.tCalc.setEasing(Easing.outCubic);
                }
                case 3 -> {
                    leftArm.rCalc.extend(leftArm.dRotation);
                    leftHand.rCalc.extend(leftHand.dRotation);
                    leftHand.tCalc.extend(leftHand.dTranslation);
                }
            }
        }
    }

    @Override
    public void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
    }

    @Override
    public Vector3f getTranslation(LivingEntity user) {
        return new Vector3f(-6F/16F, 14F/16F, -2F/16F);
    }

    @Override
    public Vector3f getOffset(LivingEntity user) {
        return new Vector3f(0F/16F, 0F/16F, hasWeapon(user) ? 2F/16F : 0F/16F);
    }

    @Override
    public float getPitch(LivingEntity user, float partialTicks) {
        return Mth.clamp(user.getViewXRot(partialTicks), -45F, 30F);
    }

    @Override
    public String getName(LivingEntity user) {
        if(!(user.getMainHandItem().getItem() instanceof IWeaponItem)) return "swipe";
        return super.getName(user);
    }

    @Override
    public float getMaxXRot(int state) {
        return switch(state) {
            case 1 -> 0;
            case 2 -> 4;
            default -> 15;
        };
    }

    @Override
    public float getMaxYRot(int state) {
        return switch(state) {
            case 0 -> 20;
            case 1 -> 0;
            case 2 -> 10;
            default -> 30;
        };
    }
}
