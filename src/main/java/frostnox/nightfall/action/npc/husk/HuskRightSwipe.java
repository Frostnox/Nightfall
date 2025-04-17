package frostnox.nightfall.action.npc.husk;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.npc.NPCAttack;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.HuskEntity;
import frostnox.nightfall.item.IWeaponItem;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class HuskRightSwipe extends NPCAttack {
    public HuskRightSwipe(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public HuskRightSwipe(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Action.Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 2;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return ActionsNF.isEmpty(chainsFrom().getId()) ? 2.1 : 3.0;
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
                mCalc.extend(0, 25, 0);
                body.rCalc.extend(-10, 0, 0);
                rightArm.rCalc.extend(pitch, 0, -pitch);
                rightHand.rCalc.extend(0, 90, 90);
            }
            case 1, 3 -> {
                mCalc.freeze();
                body.rCalc.freeze();
                rightArm.rCalc.freeze();
                rightHand.rCalc.freeze();
            }
            case 2 -> {
                mCalc.extend(0, -50, 0);
                mCalc.setEasing(Easing.outCubic);
                body.rCalc.extend(25, 0, 0);
                body.rCalc.setEasing(Easing.outCubic);
                rightArm.rCalc.freeze();
                rightHand.rCalc.extend(-60, -45, 0);
            }
            case 4 -> {
                mCalc.extend(0, 0, 0);
                body.rCalc.extend(body.dRotation);
                rightArm.rCalc.extend(rightArm.dRotation);
                rightHand.rCalc.extend(rightHand.dRotation);
            }
        }
        if(data.size() != 3) {
            AnimationData head = data.get(EntityPart.HEAD);
            AnimationData leftHand = data.get(EntityPart.HAND_LEFT);
            switch(state) {
                case 0 -> {
                    head.rCalc.add(0, 20, 0);
                    leftHand.rCalc.extend(-30, 0, -10);
                }
                case 1, 3 -> {
                    head.rCalc.freeze();
                    leftHand.rCalc.freeze();
                }
                case 2 -> {
                    head.rCalc.add(0, -45, 0);
                    leftHand.rCalc.extend(25, 0, -15);
                }
                case 4 -> {
                    head.toDefaultRotation();
                    leftHand.toDefaultRotation();
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
        return Mth.clamp(user.getViewXRot(partialTicks), -45F, 25F);
    }

    @Override
    public String getName(LivingEntity user) {
        if(!(user.getMainHandItem().getItem() instanceof IWeaponItem)) return "swipe";
        return super.getName(user);
    }

    @Override
    public float getMaxXRot(int state) {
        return switch(state) {
            case 1, 3 -> 0;
            case 2 -> 5;
            default -> 15;
        };
    }

    @Override
    public float getMaxYRot(int state) {
        return switch(state) {
            case 0 -> 22.5F;
            case 1, 3 -> 0;
            case 2 -> 14;
            default -> 30;
        };
    }
}
