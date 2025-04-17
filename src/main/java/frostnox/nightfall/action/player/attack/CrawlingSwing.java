package frostnox.nightfall.action.player.attack;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.player.IClientAction;
import frostnox.nightfall.action.player.PlayerAttack;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class CrawlingSwing extends PlayerAttack implements IClientAction {
    public CrawlingSwing(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public CrawlingSwing(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    public CrawlingSwing(DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
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
        switch (state) {
            case 0 -> {
                tCalc.extend(-3.5F/16F, 10F/16F, 2F/16F);
                rCalc.extend(45, 100, 0);
            }
            case 1 -> {
                tCalc.extendWithHitStop(user, getDuration(1, user), -6F/16F, -4F/16F, -2F/16F, Easing.outQuart);
                rCalc.extendWithHitStop(user, getDuration(1, user), 0, 100, -60, Easing.outCubic);
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
            AnimationData rightLeg = data.get(EntityPart.LEG_RIGHT);
            AnimationData leftLeg = data.get(EntityPart.LEG_LEFT);
            switch (state) {
                case 0 -> {
                    mCalc.setVectors(0, 0, 0, -1, 5, 0);
                    rightHand.rCalc.add(-90, 20, 0);
                    rightArm.rCalc.extend(-180 + pitch, 0, 0);
                }
                case 1 -> {
                    mCalc.extend(0, -5, 0, Easing.outQuart);
                    rightHand.rCalc.add(145, -20, -15);
                    rightHand.rCalc.setEasing(Easing.outQuart);
                    rightArm.rCalc.freeze();
                }
                case 2 -> {
                    mCalc.freeze();
                    rightArm.rCalc.freeze();
                    rightHand.rCalc.freeze();
                }
                case 3 -> {
                    mCalc.extend(0, 0, 0);
                    rightHand.toDefaultRotation();
                    rightArm.toDefaultRotation();
                }
            }
        }
    }

    @Override
    public void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
    }

    @Override
    public float getPitch(LivingEntity user, float partial) {
        return Mth.clamp(user.getViewXRot(partial), -30, 70);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }
}
