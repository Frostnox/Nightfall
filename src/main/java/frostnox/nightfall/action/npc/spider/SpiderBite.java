package frostnox.nightfall.action.npc.spider;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.npc.NPCAttack;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.SpiderEntity;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class SpiderBite extends NPCAttack {
    public SpiderBite(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public SpiderBite(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 2.5;
    }

    @Override
    protected EnumMap<EntityPart, AnimationData> getDefaultAnimationData() {
        return SpiderEntity.getHeadAnimMap();
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData head = data.get(EntityPart.HEAD);
        switch(state) {
            case 0 -> {
                head.rCalc.add(-10, 0, 0);
                head.tCalc.add(0, 0, 1F);
            }
            case 1 -> {
                head.rCalc.add(15, 0, 0, Easing.outQuart);
                head.tCalc.add(0, 0, -1.5F, Easing.outQuart);
            }
            case 2 -> {
                head.toDefault();
            }
        }
        if(data.size() != 1) {
            AnimationData body = data.get(EntityPart.BODY);
            AnimationData legRight = data.get(EntityPart.LEG_RIGHT);
            AnimationData legLeft = data.get(EntityPart.LEG_LEFT);
            switch(state) {
                case 0 -> {
                    body.tCalc.add(0, 0, 0.5F);
                    legRight.rCalc.extend(-75, -25, 0, Easing.outCubic);
                    legLeft.rCalc.extend(-75, 25, 0, Easing.outCubic);
                }
                case 1 -> {
                    body.tCalc.add(0, 0, -1F, Easing.outQuart);
                    legRight.rCalc.add(75, 10, 0, Easing.outCubic);
                    legLeft.rCalc.add(75, -10, 0, Easing.outCubic);
                }
                case 2 -> {
                    body.toDefaultTranslation();
                    legRight.toDefaultRotation();
                    legLeft.toDefaultRotation();
                }
            }
        }
    }

    @Override
    public Vector3f getTranslation(LivingEntity user) {
        return new Vector3f(0F/16F, 2.5F/16F, 2F/16F);
    }

    @Override
    public Vector3f getOffset(LivingEntity user) {
        return new Vector3f(0F/16F, 0F/16F, 4F/16F);
    }

    @Override
    public void onTick(LivingEntity user) {
        if(!user.level.isClientSide && user instanceof Mob mob) {
            IActionTracker capA = ActionTracker.get(user);
            if(capA.getState() == 0 && capA.getFrame() == capA.getDuration() - 1) {
                CombatUtil.addMovementTowardsTarget(0.8, 1, mob);
            }
        }
    }

    @Override
    public String getName(LivingEntity user) {
        return "bite";
    }

    @Override
    public float getMaxXRot(int state) {
        if(state == 1) return 20;
        else return super.getMaxXRot(state);
    }

    @Override
    public float getMaxYRot(int state) {
        return switch(state) {
            case 0 -> 45;
            case 1 -> 0;
            default -> 60;
        };
    }
}
