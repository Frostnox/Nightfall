package frostnox.nightfall.action.npc.skeleton;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.npc.NPCAttack;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.SkeletonEntity;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class SkeletonThrust extends NPCAttack {
    public SkeletonThrust(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public SkeletonThrust(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 2.5;
    }

    @Override
    protected EnumMap<EntityPart, AnimationData> getDefaultAnimationData() {
        return SkeletonEntity.getRightArmAnimMap();
    }

    @Override
    public void onTick(LivingEntity user) {
        if(!user.level.isClientSide && user instanceof Mob mob) {
            IActionTracker capA = ActionTracker.get(user);
            if(capA.getState() == 0 && capA.getFrame() == capA.getDuration() - 2) {
                CombatUtil.addMovementTowardsTarget(0.3, 1, mob);
            }
        }
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData body = data.get(EntityPart.BODY);
        AnimationData rightArm = data.get(EntityPart.ARM_RIGHT);
        AnimationData rightHand = data.get(EntityPart.HAND_RIGHT);
        switch(state) {
            case 0 -> {
                body.rCalc.add(-15, 0, 0);
                mCalc.extend(0, 45, 0);
                rightArm.rCalc.extend(pitch, 0, 0);
                rightHand.rCalc.extend(30, -5, 0);
            }
            case 1 -> {
                body.rCalc.add(20, 0, 0, Easing.outCubic);
                mCalc.extend(0, -30, 0, Easing.outCubic);
                rightArm.rCalc.freeze();
                rightHand.rCalc.extend(-75, 20, 0, Easing.outSine);
            }
            case 2 -> {
                body.rCalc.freeze();
                mCalc.freeze();
                rightArm.rCalc.freeze();
                rightHand.rCalc.freeze();
            }
            case 3 -> {
                body.toDefaultRotation();
                mCalc.extend(0, 0, 0);
                rightArm.rCalc.extend(rightArm.dRotation);
                rightHand.rCalc.extend(rightHand.dRotation);
            }
        }
        if(data.size() != 3) {
            AnimationData leftArm = data.get(EntityPart.ARM_LEFT);
            AnimationData leftHand = data.get(EntityPart.HAND_LEFT);
            switch(state) {
                case 0 -> {
                    leftArm.rCalc.extend(pitch/2F, 0, 0);
                    leftHand.rCalc.extend(-25, 0, -10);
                }
                case 1 -> {
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.extend(25, 0, -15, Easing.outCubic);
                }
                case 2 -> {
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                }
                case 3 -> {
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
                data.rCalc.addFrom(data.dRotation, 30, 0, 0);
            }
            case 1 -> {
                data.rCalc.add(-90, 0, 0, Easing.outSine);
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
    public Vector3f getTranslation(LivingEntity user) {
        return new Vector3f(-6F/16F, 14F/16F, 0F/16F);
    }

    @Override
    public Vector3f getOffset(LivingEntity user) {
        return new Vector3f(0F/16F, 0F/16F, hasWeapon(user) ? 2F/16F : 0F/16F);
    }

    @Override
    public float getPitch(LivingEntity user, float partialTicks) {
        return Mth.clamp(user.getViewXRot(partialTicks), -45F, 45F);
    }

    @Override
    public String getName(LivingEntity user) {
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
