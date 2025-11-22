package frostnox.nightfall.action.npc.merbor;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

public class MerborBreed extends Action {
    public MerborBreed(int[] duration) {
        super(duration);
    }

    public MerborBreed(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData body = data.get(EntityPart.BODY);
        AnimationData neck = data.get(EntityPart.NECK);
        AnimationData head = data.get(EntityPart.HEAD);
        AnimationData legRight = data.get(EntityPart.LEG_RIGHT);
        AnimationData legLeft = data.get(EntityPart.LEG_LEFT);
        AnimationData tail = data.get(EntityPart.TAIL);
        switch(state) {
            case 0 -> {
                body.tCalc.extend(0, 11, 2.5F, Easing.outCubic);
                body.rCalc.extend(-45, 0, 0, Easing.outSine);
                legRight.rCalc.extend(-90, 0, 0, Easing.outCubic);
                legRight.tCalc.add(0, -7, 2.5F, Easing.outCubic);
                legLeft.rCalc.extend(-90, 0, 0, Easing.outCubic);
                legLeft.tCalc.add(0, -7, 2.5F, Easing.outCubic);
                neck.rCalc.add(45, 0, 0, Easing.outCubic);
                tail.rCalc.add(35, 0, 0, Easing.outSine);
            }
            case 1 -> {
                body.toDefaultTranslation();
                body.toDefaultRotation(Easing.inSine);
                legRight.toDefault();
                legLeft.toDefault();
                neck.toDefault();
                tail.toDefaultRotation();
            }
        }
    }
}
