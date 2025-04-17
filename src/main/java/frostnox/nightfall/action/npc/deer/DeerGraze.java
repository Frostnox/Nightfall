package frostnox.nightfall.action.npc.deer;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.animal.DeerEntity;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

public class DeerGraze extends Action {
    public DeerGraze(int[] duration) {
        super(duration);
    }

    public DeerGraze(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public void onTick(LivingEntity user) {
        if(!user.level.isClientSide) ((DeerEntity) user).addSatiety(1);
    }

    @Override
    public int getChargeTimeout() {
        return 20 * 40;
    }

    @Override
    public int getRequiredCharge(LivingEntity user) {
        return 0;
    }

    @Override
    public boolean canContinueCharging(LivingEntity user) {
        return ((DeerEntity) user).sprintTime == 0;
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData neck = data.get(EntityPart.NECK);
        AnimationData head = data.get(EntityPart.HEAD);
        switch(state) {
            case 0 -> {
                head.rCalc.add(-35, 0, 0);
                neck.rCalc.extend(140, 0, 0);
            }
            case 1 -> {
                float f = (charge > 0 ? (ActionTracker.get(user).getCharge() - 2 + ActionTracker.get(user).getChargePartial()) :
                        (frame - 1 + head.rCalc.partialTicks)) * 0.4F;
                head.rCalc.add(Mth.cos(f) * 2, 0 ,0);
                neck.rCalc.add(Mth.sin(f) * 2F, 0 ,0);
            }
            case 2 -> {
                head.toDefaultRotation();
                neck.toDefaultRotation();
            }
        }
    }
}
