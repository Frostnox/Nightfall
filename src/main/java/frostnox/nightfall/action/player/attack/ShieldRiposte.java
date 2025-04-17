package frostnox.nightfall.action.player.attack;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.player.IClientAction;
import frostnox.nightfall.action.player.PlayerAttack;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.item.ImpactSoundType;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class ShieldRiposte extends PlayerAttack {
    public ShieldRiposte(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public ShieldRiposte(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    public ShieldRiposte(DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(0, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public ImpactSoundType getImpactSoundType(LivingEntity user) {
        return ImpactSoundType.STRIKE;
    }

    @Override
    public void onTick(LivingEntity user) {
        if(user instanceof Player player) {
            IActionTracker capA = ActionTracker.get(player);
            if(!capA.isStunned() && capA.getState() == 1 && capA.getFrame() == 1) {
                if(user.level.isClientSide()) {
                    ClientEngine.get().lastDashTick = player.tickCount;
                    Vec3 velocity = user.getDeltaMovement();
                    float modifier = (player.isOnGround() ? 0.7F : 0.2F) * player.level.getBlockState(player.getOnPos()).getBlock().getSpeedFactor();
                    Vec3 lookAngle = user.getLookAngle();
                    user.setDeltaMovement(velocity.x + lookAngle.x * modifier, velocity.y, velocity.z + lookAngle.z * modifier);
                }
            }
        }
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
                tCalc.extend(-10F/16F, 8F/16F, 1F/16F, Easing.inOutSine);
                data.toDefaultRotation();
            }
            case 1 -> {
                tCalc.add(2F / 16F, 1F / 16F, -7F / 16F, Easing.outQuart);
                rCalc.add(10, -12, 0, Easing.outQuart);
            }
            case 2 -> {
                tCalc.freeze();
                rCalc.freeze();
            }
            case 3 -> {
                data.toDefault();
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
                    mCalc.extend(0, -25, 0);
                    rightHand.rCalc.extend(-70, -45, 35);
                    rightArm.rCalc.extend(pitch, 0, 0);
                    leftHand.rCalc.freeze();
                    leftArm.rCalc.freeze();
                }
                case 1 -> {
                    mCalc.extend(5, 15, 0, Easing.outQuart);
                    rightHand.rCalc.add(-12, 40, 0, Easing.outQuart);
                    rightArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
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
                data.rCalc.extend(data.dRotation.x() + 70, data.dRotation.y() + 45, data.dRotation.z() - 35);
                data.tCalc.extend(data.dTranslation.x(), data.dTranslation.y(), data.dTranslation.z() - 3F/16F);
            }
            case 1 -> {
                data.rCalc.freeze();
                data.tCalc.freeze();
            }
            case 2 -> {
                data.rCalc.freeze();
                data.tCalc.freeze();
            }
            case 3 -> {
                data.toDefault();
            }
        }
    }

    @Override
    public float getPitch(LivingEntity user, float partial) {
        return Mth.clamp(user.getViewXRot(partial) - 20, -90 - 20, 30);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public int getBlockHitFrame(int state, LivingEntity user) {
        return Math.max(1, super.getBlockHitFrame(state, user) - 2);
    }
}
