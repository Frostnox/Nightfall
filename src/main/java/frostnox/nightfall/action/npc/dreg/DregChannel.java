package frostnox.nightfall.action.npc.dreg;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.npc.MoveSpeedAction;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.DregEntity;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

public class DregChannel extends MoveSpeedAction {
    public DregChannel(int[] duration) {
        super(-0.15F, duration);
    }

    public DregChannel(Properties properties, int... duration) {
        super(properties, -0.15F, duration);
    }

    @Override
    public void onTick(LivingEntity user) {
        if(user.level.isClientSide) {
            DregEntity dreg = (DregEntity) user;
            IActionTracker capA = dreg.getActionTracker();
            if(capA.getState() == 1 && dreg.ally != null) {
                user.level.addParticle(ParticleTypesNF.ESSENCE_MOON.get(), dreg.ally.getRandomX(0.5D), dreg.ally.getRandomY(), dreg.ally.getRandomZ(0.5D),
                        (dreg.getRandom().nextDouble() - 0.5D) * 2.0D, -dreg.getRandom().nextDouble(), (dreg.getRandom().nextDouble() - 0.5D) * 2.0D);
            }
        }
    }

    @Override
    public int getChargeTimeout() {
        return Action.CHARGE_MAX;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 10;
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData leftHand = data.get(EntityPart.HAND_LEFT);
        AnimationData leftArm = data.get(EntityPart.ARM_LEFT);
        AnimationData rightHand = data.get(EntityPart.HAND_RIGHT);
        AnimationData rightArm = data.get(EntityPart.ARM_RIGHT);
        AnimationData head = data.get(EntityPart.HEAD);
        switch(state) {
            case 0 -> {
                rightArm.rCalc.extend(0, 0, 0);
                leftArm.rCalc.extend(0, 0, 0);
                rightHand.rCalc.extend(-135, 20, 0, Easing.outSine);
                leftHand.rCalc.extend(-135, -20, 0, Easing.outSine);
                head.rCalc.extend(-55, 0, 0, Easing.outSine);
            }
            case 1 -> {
                rightArm.rCalc.freeze();
                leftArm.rCalc.freeze();
                rightHand.rCalc.freeze();
                leftHand.rCalc.freeze();
                head.rCalc.freeze();
            }
            case 2 -> {
                rightArm.toDefaultRotation();
                leftArm.toDefaultRotation();
                rightHand.toDefaultRotation();
                leftHand.toDefaultRotation();
                head.toDefaultRotation();
            }
        }
    }
}
