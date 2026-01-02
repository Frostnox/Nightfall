package frostnox.nightfall.action.npc.wolf;

import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.animal.WolfEntity;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class WolfBiteChain extends WolfBite {
    public WolfBiteChain(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public WolfBiteChain(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData body = data.get(EntityPart.BODY);
        AnimationData head = data.get(EntityPart.HEAD);
        switch(state) {
            case 0 -> {
                body.rCalc.add(5, 0, 0);
                body.tCalc.add(0, 1, 1);
                head.rCalc.extend(-10 + pitch, 0, 0);
                head.tCalc.add(0, 0F, 2F);
            }
            case 1 -> {
                body.rCalc.add(-10, 0, 0, Easing.outQuart);
                body.tCalc.add(0, -1, -1, Easing.outQuart);
                head.rCalc.add(15 - pitch / 4, 0, 25, Easing.outQuart);
                head.tCalc.add(0, 0.5F, -2F, Easing.outQuart);
            }
            case 2 -> {
                body.toDefault();
                head.toDefault();
            }
        }
        if(data.size() > 3) {
            AnimationData tail = data.get(EntityPart.TAIL);
            AnimationData legLeft = data.get(EntityPart.LEG_LEFT);
            AnimationData legRight = data.get(EntityPart.LEG_RIGHT);
            AnimationData hindLegLeft = data.get(EntityPart.LEG_2_LEFT);
            AnimationData hindLegRight = data.get(EntityPart.LEG_2_RIGHT);
            float y = pitch > 0 ? pitch/50 : 0;
            switch(state) {
                case 0 -> {
                    tail.rCalc.add(-15, 0, 0);
                    legLeft.rCalc.add(-10, 0, 0);
                    legRight.rCalc.add(-10, 0, 0);
                    legLeft.tCalc.add(0, -y + 1, 1.5F);
                    legRight.tCalc.add(0, -y + 1, 1.5F);
                    hindLegLeft.tCalc.add(0, y, 0);
                    hindLegRight.tCalc.add(0, y, 0);
                }
                case 1 -> {
                    tail.rCalc.add(30, 0, 0, Easing.outQuart);
                    legLeft.rCalc.add(15, 0, 0, Easing.outCubic);
                    legRight.rCalc.add(15, 0, 0, Easing.outCubic);
                    legLeft.tCalc.add(0, y + -1.5F, -1.5F, Easing.outQuart);
                    legRight.tCalc.add(0, y + -1.5F, -1.5F, Easing.outQuart);
                    hindLegLeft.tCalc.add(0, -y, 0, Easing.outQuart);
                    hindLegRight.tCalc.add(0, -y, 0, Easing.outQuart);
                }
                case 2 -> {
                    tail.toDefaultRotation();
                    legLeft.toDefault();
                    legRight.toDefault();
                    hindLegLeft.toDefaultTranslation();
                    hindLegRight.toDefaultTranslation();
                }
            }
        }
    }

    @Override
    public void onTick(LivingEntity user) {
        WolfEntity wolf = (WolfEntity) user;
        if(wolf.getActionTracker().getState() > 1 && wolf.fleeTicks == 0) wolf.fleeTicks = 10;
    }
}
