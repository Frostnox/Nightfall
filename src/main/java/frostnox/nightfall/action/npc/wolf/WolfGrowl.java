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
                if(capA.getFrame() % 36 == 1) user.playSound(getSound().get(), 1.25F, 0.96F + user.getRandom().nextFloat() * 0.08F);
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
            int frame = ActionTracker.get(user).getFrame() % 36;
            if(frame == 1 || frame > 18) user.playSound(getSound().get(), 1.25F, 0.97F + user.getRandom().nextFloat() * 0.06F);
        }
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData body = data.get(EntityPart.BODY);
        AnimationData head = data.get(EntityPart.HEAD);
        AnimationData neck = data.get(EntityPart.NECK);
        AnimationData tail = data.get(EntityPart.TAIL);
        AnimationData earLeft = data.get(EntityPart.EAR_LEFT);
        AnimationData earRight = data.get(EntityPart.EAR_RIGHT);
        switch(state) {
            case 0 -> {
                body.rCalc.add(5, 0, 0);
                body.tCalc.add(0, 0.5F, 0);
                head.tCalc.add(0, 1F, -0.5F);
                head.rCalc.extend(-5 + pitch, 0, 0);
                neck.tCalc.add(0, -0.5F, 0);
                earLeft.rCalc.extend(-45, 0, 0);
                earRight.rCalc.extend(-45, 0, 0);
                tail.rCalc.extend(-3, 0, 0);
            }
            case 1 -> {
                body.rCalc.freeze();
                body.tCalc.freeze();
                head.tCalc.freeze();
                head.rCalc.freeze();
                neck.tCalc.freeze();
                earLeft.rCalc.freeze();
                earRight.rCalc.freeze();
                tail.rCalc.freeze();
            }
            case 2 -> {
                body.toDefault();
                head.toDefault();
                neck.toDefaultTranslation();
                earLeft.toDefaultRotation();
                earRight.toDefaultRotation();
                tail.toDefaultRotation();
            }
        }
    }
}
