package frostnox.nightfall.action.npc.pit_devil;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.npc.NPCAttack;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.PitDevilEntity;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class PitDevilBite extends NPCAttack {
    public PitDevilBite(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public PitDevilBite(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 2.2;
    }

    @Override
    protected EnumMap<EntityPart, AnimationData> getDefaultAnimationData() {
        return PitDevilEntity.getHeadAnimMap();
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData body = data.get(EntityPart.BODY);
        AnimationData neck = data.get(EntityPart.NECK);
        AnimationData head = data.get(EntityPart.HEAD);
        switch(state) {
            case 0 -> {
                body.rCalc.add(10, 0, 0);
                body.tCalc.add(0, 1, 1);
                neck.rCalc.add(-10, 0, 0);
                head.rCalc.extend(pitch, 0, 0);
                head.tCalc.add(0, 0F, 1F);
            }
            case 1 -> {
                body.rCalc.add(-20, 0, 0, Easing.outQuart);
                body.tCalc.add(0, -1, -1, Easing.outQuart);
                neck.rCalc.add(10, 0, 0, Easing.outQuart);
                head.rCalc.freeze();
                head.tCalc.add(0, 0F, -2F, Easing.outQuart);
            }
            case 2 -> {
                body.toDefault();
                head.toDefault();
                neck.toDefaultRotation();
            }
        }
        if(data.size() > 3) {
            AnimationData tail = data.get(EntityPart.TAIL);
            AnimationData legLeft = data.get(EntityPart.LEG_LEFT);
            AnimationData legRight = data.get(EntityPart.LEG_RIGHT);
            switch(state) {
                case 0 -> {
                    tail.rCalc.extend(20, 0, 0);
                    legLeft.tCalc.add(0, 0, 1.5F);
                    legRight.tCalc.add(0, 0, 1.5F);
                }
                case 1 -> {
                    tail.rCalc.extend(-5, 0, 0, Easing.outQuart);
                    legLeft.tCalc.add(0, 0, -1.5F, Easing.outQuart);
                    legRight.tCalc.add(0, 0, -1.5F, Easing.outQuart);
                }
                case 2 -> {
                    tail.toDefaultRotation();
                    legLeft.toDefaultTranslation();
                    legRight.toDefaultTranslation();
                }
            }
        }
    }

    @Override
    public Vector3f getTranslation(LivingEntity user) {
        return new Vector3f(0F/16F, 12F/16F, 4F/16F);
    }

    @Override
    public Vector3f getOffset(LivingEntity user) {
        return new Vector3f(0F/16F, 0F/16F, 4.5F/16F);
    }

    @Override
    public float getPitch(LivingEntity user, float partialTicks) {
        return Mth.clamp(user.getViewXRot(partialTicks), -90F, 90F);
    }

    @Override
    public void onTick(LivingEntity user) {
        if(!user.level.isClientSide && user instanceof Mob mob) {
            IActionTracker capA = ActionTracker.get(user);
            if(capA.getState() == 0 && capA.getFrame() == capA.getDuration() - 1) {
                CombatUtil.addMovementTowardsTarget(0.6, 1, mob);
            }
        }
    }

    @Override
    public String getName(LivingEntity user) {
        return "bite";
    }

    @Override
    public float getMaxXRot(int state) {
        if(state == 1) return 30;
        else return super.getMaxXRot(state);
    }

    @Override
    public float getMaxYRot(int state) {
        return switch(state) {
            case 0 -> 30;
            case 1 -> 0;
            default -> 45;
        };
    }
}
