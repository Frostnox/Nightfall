package frostnox.nightfall.action.player.action;

import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;

public class BowShoot extends LaunchProjectileAction {
    public BowShoot(Properties properties, float speedMultiplier, float velocity, int... duration) {
        super(properties, speedMultiplier, velocity, duration);
    }

    @Override
    public void onStart(LivingEntity user) {
        if(!user.level.isClientSide) {
            user.level.playSound(null, user, SoundsNF.BOW_PULL.get(), SoundSource.PLAYERS, 1F, 0.975F + user.level.random.nextFloat() * 0.05F);
        }
    }

    @Override
    public boolean allowDodging(int state) {
        return state > getChargeState();
    }

    @Override
    public void transformModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        super.transformModelFP(state, frame, duration, charge, user, data);
        switch(state) {
            case 0 -> {
                data.rCalc.add(-10F, 0F, 0F);
                data.tCalc.add(-4F/16F, 3F/16F, 2F/16F);
            }
            case 1 -> {
                data.rCalc.freeze();
                data.tCalc.add(0F, charge > 0F ? 0F : (Mth.sin((frame - 1 + data.tCalc.partialTicks) * 1.2F) * 0.005F), 0F);
                data.sCalc.add(0.3F, 0F, 0F, Easing.outCubic);
            }
            case 2 -> {
                data.rCalc.freeze();
                data.tCalc.add(0F, 0F, -2F/16F, Easing.outQuart);
                data.sCalc.extend(data.dScale, Easing.outCubic);
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
                    mCalc.extend(0, -70, 0);
                    rightHand.rCalc.extend(-90 + pitch, 70, 0);
                    rightArm.rCalc.extend(0, 0, 0);
                    leftArm.rCalc.extend(pitch / 4F, 0, 0);
                    leftHand.rCalc.extend(-93 + pitch/4F, 75, 0);
                    leftHand.tCalc.add(-1.5F, 0, -0.5F);
                }
                case 1 -> {
                    mCalc.freeze();
                    rightHand.rCalc.freeze();
                    rightArm.rCalc.freeze();
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                    leftHand.tCalc.addWithCharge(1.5F, 0, 0F, charge, Easing.outSine);
                }
                case 2 -> {
                    mCalc.freeze();
                    rightHand.rCalc.freeze();
                    rightArm.rCalc.freeze();
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                    leftHand.tCalc.freeze();
                }
                case 3 -> {
                    mCalc.extend(0, 0, 0);
                    rightHand.toDefaultRotation();
                    rightArm.toDefaultRotation();
                    leftArm.toDefaultRotation();
                    leftHand.toDefault();
                }
            }
        }
    }

    @Override
    public void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        switch(state) {
            case 0 -> {
                data.rCalc.add(0, 10, 0);
            }
            case 1 -> {
                data.rCalc.freeze();
            }
            case 2 -> {
                data.rCalc.freeze();
            }
            case 3 -> {
                data.toDefaultRotation();
            }
        }
    }

    @Override
    public float getPitch(LivingEntity user, float partialTicks) {
        return Mth.clamp(user.getViewXRot(partialTicks), -80F, 70F);
    }
}
