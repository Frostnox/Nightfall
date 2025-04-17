package frostnox.nightfall.action.player.action;

import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;

public abstract class WrapAction extends MoveSpeedPlayerAction {
    public WrapAction(Properties properties, int... duration) {
        super(properties, -0.6F, duration);
    }

    @Override
    public void onChargeRelease(LivingEntity user) {
        if(!user.level.isClientSide && getExtraSound() != null && ActionTracker.get(user).isFullyCharged()) {
            user.level.playSound(null, user, getExtraSound().get(), SoundSource.PLAYERS, 1F, 0.96F + user.level.random.nextFloat() * 0.08F);
        }
    }

    @Override
    public void onStart(LivingEntity user) {
        if(!user.level.isClientSide && getSound() != null) {
            user.level.playSound(null, user, getSound().get(), SoundSource.PLAYERS, 1F, 0.96F + user.level.random.nextFloat() * 0.08F);
        }
    }

    @Override
    public boolean hasChargeZoom() {
        return false;
    }

    @Override
    public int getMaxCharge() {
        return super.getMaxCharge() * 3;
    }

    @Override
    public int getChargeTimeout() {
        return getMaxCharge();
    }

    @Override
    public void transformModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        super.transformModelFP(state, frame, duration, charge, user, data);
        AnimationCalculator tCalc = data.tCalc;
        AnimationCalculator rCalc = data.rCalc;
        Vector3f dTranslation = data.dTranslation;
        Vector3f dRotation = data.dRotation;
        switch(state) {
            case 0 -> {
                float chargeFrame = ActionTracker.get(user).getCharge() - 2 + ActionTracker.get(user).getChargePartial();
                float f = (charge > 0 ? (chargeFrame) : (frame - 1 + tCalc.partialTicks)) * 0.6F;
                tCalc.add(-3F/16F + Mth.sin(f) * 0.12F, -Mth.cos(f) * 0.12F, 0);
            }
            case 1 -> {
                tCalc.extend(dTranslation);
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
                    rightArm.rCalc.extend(0, 0, 0);
                    float chargeFrame = ActionTracker.get(user).getCharge() - 2 + ActionTracker.get(user).getChargePartial();
                    float f = (charge > 0 ? (chargeFrame) : (frame - 1 + rightHand.rCalc.partialTicks)) * 0.6F;
                    rightHand.rCalc.extend(-45 + Mth.sin(f) * 12F, -45 - Mth.cos(f) * 12F, 0);
                }
                case 1 -> {
                    rightArm.rCalc.extend(rightArm.dRotation);
                    rightHand.rCalc.extend(rightHand.dRotation);
                }
            }
        }
    }
}
