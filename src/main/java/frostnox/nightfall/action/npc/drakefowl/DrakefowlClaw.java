package frostnox.nightfall.action.npc.drakefowl;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.npc.NPCAttack;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class DrakefowlClaw extends NPCAttack {
    public DrakefowlClaw(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public DrakefowlClaw(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 1;
    }

    @Override
    protected EnumMap<EntityPart, AnimationData> getDefaultAnimationData() {
        return EMPTY_MAP;
    }

    @Override
    public void onTick(LivingEntity user) {
        if(!user.level.isClientSide && user instanceof Mob mob) {
            IActionTracker capA = ActionTracker.get(user);
            if(!capA.isStunned()) {
                if(capA.getFrame() == getDuration(0, user) - 2 && capA.getState() == 0) {
                    CombatUtil.addMovementTowardsTarget(1, 1, mob);
                }
            }
        }
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        if(!data.isEmpty()) {
            AnimationData body = data.get(EntityPart.BODY);
            AnimationData neck = data.get(EntityPart.NECK);
            AnimationData head = data.get(EntityPart.HEAD);
            AnimationData legRight = data.get(EntityPart.LEG_RIGHT);
            AnimationData legLeft = data.get(EntityPart.LEG_LEFT);
            switch(state) {
                case 0 -> {
                    body.tCalc.add(0, -2F, 0);
                    body.rCalc.extend(-45, 0, 0);
                    neck.rCalc.extend(70, 0, 0);
                    head.rCalc.extend(-20, 0, 0);
                    legRight.tCalc.add(0, -3.5F, -3);
                    legRight.rCalc.extend(-115, 0, 0, Easing.outCubic);
                    legLeft.tCalc.add(0, -3.5F, -3);
                    legLeft.rCalc.extend(-115, 0, 0, Easing.outCubic);
                }
                case 1 -> {
                    body.tCalc.freeze();
                    body.rCalc.add(10F, 0, 0, Easing.outQuart);
                    neck.rCalc.add(-10F, 0, 0, Easing.outQuart);
                    head.rCalc.freeze();
                    legRight.tCalc.add(0, 1.5F, 1.5F, Easing.outQuart);
                    legRight.rCalc.add(95, 0, 0, Easing.outQuart);
                    legLeft.tCalc.add(0, 1.5F, 1.5F, Easing.outCubic);
                    legLeft.rCalc.add(95, 0, 0, Easing.outCubic);
                }
                case 2 -> {
                    body.toDefault();
                    head.toDefaultRotation();
                    neck.toDefaultRotation();
                    legRight.toDefault();
                    legLeft.toDefault();
                }
            }
        }
    }

    @Override
    public Vector3f getTranslation(LivingEntity user) {
        return new Vector3f(0F/16F, 6/16F, 4F/16F);
    }

    @Override
    public String getName(LivingEntity user) {
        return "claw";
    }

    @Override
    public float getMaxXRot(int state) {
        if(state == 1) return 10;
        else return super.getMaxXRot(state);
    }

    @Override
    public float getMaxYRot(int state) {
        return switch(state) {
            case 0 -> 20;
            case 1 -> 0;
            default -> 30;
        };
    }
}
