package frostnox.nightfall.action.npc.wolf;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.animal.WolfEntity;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

public class WolfGrowl extends Action {
    public WolfGrowl(int[] duration) {
        super(duration);
    }

    public WolfGrowl(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public void onTick(LivingEntity user) {
        IActionTracker capA = ActionTracker.get(user);
        if(capA.getState() == 1) {
            WolfEntity wolf = (WolfEntity) user;
            if(!user.level.isClientSide) {
                if(capA.getFrame() % 60 == 1) user.playSound(getSound().get(), 1.25F, 0.97F + user.getRandom().nextFloat() * 0.06F);
                wolf.growlTicks++;
            }
            wolf.setYBodyRot(wolf.getYHeadRot());
        }
    }

    @Override
    public int getChargeTimeout() {
        return WolfEntity.GROWL_DURATION;
    }

    @Override
    public int getRequiredCharge(LivingEntity user) {
        return 0;
    }

    @Override
    public boolean canContinueCharging(LivingEntity user) {
        if(user.level.isClientSide) return true;
        else {
            WolfEntity wolf = (WolfEntity) user;
            return wolf.growlTicks < WolfEntity.GROWL_DURATION && wolf.getTarget() != null && user.getEyePosition().distanceToSqr(wolf.getTarget().getEyePosition()) > 4 * 4;
        }
    }

    @Override
    public void onChargeRelease(LivingEntity user) {
        if(!user.level.isClientSide) {
            NetworkHandler.toAllTracking(user, new GenericEntityToClient(NetworkHandler.Type.QUEUE_ACTION_TRACKER, user.getId()));
            int frame = ActionTracker.get(user).getFrame() % 60;
            if(frame == 1 || frame > 18) user.playSound(getSound().get(), 1.25F, 0.97F + user.getRandom().nextFloat() * 0.06F);
        }
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData body = data.get(EntityPart.BODY);
        AnimationData neck = data.get(EntityPart.NECK);
        AnimationData head = data.get(EntityPart.HEAD);
        AnimationData tail = data.get(EntityPart.TAIL);
        AnimationData legLeft = data.get(EntityPart.LEG_LEFT);
        AnimationData legRight = data.get(EntityPart.LEG_RIGHT);
        AnimationData earLeft = data.get(EntityPart.EAR_LEFT);
        AnimationData earRight = data.get(EntityPart.EAR_RIGHT);
        switch(state) {
            case 0 -> {
                body.rCalc.add(10, 0, 0);
                body.tCalc.add(0, 1, 0);
                head.tCalc.extend(0, 1, 0);
                head.rCalc.extend(pitch, 0, 0);
                earLeft.rCalc.add(-25, 0, 0);
                earRight.rCalc.add(-25, 0, 0);
                tail.rCalc.extend(20, 0, 0);
            }
            case 1 -> {
                float f = (charge > 0 ? (ActionTracker.get(user).getCharge() - 2 + ActionTracker.get(user).getChargePartial()) :
                        (frame - 1 + head.rCalc.partialTicks)) * 0.55F;
                body.rCalc.freeze();
                body.tCalc.freeze();
                head.tCalc.freeze();
                head.rCalc.freeze();
                earLeft.rCalc.freeze();
                earRight.rCalc.freeze();
                tail.rCalc.freeze();
                float scale = 0.35F;
                legLeft.rCalc.add(Mth.sin(f + MathUtil.PI / 6) * 10, 0, 0);
                legLeft.tCalc.add(0, -Mth.sin(f) * scale, Mth.cos(f) * scale);
                legRight.rCalc.add(-Mth.sin(f + MathUtil.PI / 6) * 10, 0, 0);
                legRight.tCalc.add(0, Mth.sin(f) * scale, -Mth.cos(f) * scale);
            }
            case 2 -> {
                body.toDefault();
                head.toDefaultRotation();
                neck.toDefaultRotation();
                earLeft.toDefaultRotation();
                earRight.toDefaultRotation();
                tail.toDefaultRotation();
                legLeft.toDefault();
                legRight.toDefault();
            }
        }
    }
}
