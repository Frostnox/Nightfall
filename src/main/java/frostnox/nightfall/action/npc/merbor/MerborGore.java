package frostnox.nightfall.action.npc.merbor;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.npc.NPCAttack;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.animal.MerborEntity;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class MerborGore extends NPCAttack {
    public MerborGore(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public MerborGore(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 2;
    }

    @Override
    protected EnumMap<EntityPart, AnimationData> getDefaultAnimationData() {
        return MerborEntity.getHeadAnimMap();
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
        AnimationData body = data.get(EntityPart.BODY);
        AnimationData neck = data.get(EntityPart.NECK);
        AnimationData head = data.get(EntityPart.HEAD);
        switch(state) {
            case 0 -> {
                body.tCalc.add(0, 0.5F, 1F);
                body.rCalc.extend(0, 0, 0);
                neck.rCalc.extend(25, 0, 0);
                neck.tCalc.add(0, 0, 1.5F);
                head.rCalc.extend(pitch, 0, 0);
            }
            case 1 -> {
                body.tCalc.add(0, -0.5F, -2F, Easing.outQuart);
                body.rCalc.freeze();
                neck.rCalc.add(-50, 0, 0, Easing.outQuart);
                neck.tCalc.add(0, 0, -1.5F, Easing.outQuart);
                head.rCalc.freeze();
            }
            case 2 -> {
                body.toDefault();
                neck.toDefault();
                head.toDefaultRotation();
            }
        }
        if(data.size() > 3) {
            AnimationData legRight = data.get(EntityPart.LEG_RIGHT);
            AnimationData legLeft = data.get(EntityPart.LEG_LEFT);
            AnimationData tail = data.get(EntityPart.TAIL);
            switch(state) {
                case 0 -> {
                    legRight.tCalc.add(0, 0, 0.75F);
                    legLeft.tCalc.add(0, 0, 0.75F);
                    tail.rCalc.add(6, 0, 0);
                }
                case 1 -> {
                    legRight.tCalc.add(0, 0, -1.5F, Easing.outQuart);
                    legLeft.tCalc.add(0, 0, -1.5F, Easing.outQuart);
                    tail.rCalc.add(20, 0, 0, Easing.outSine);
                }
                case 2 -> {
                    legRight.toDefault();
                    legLeft.toDefault();
                    tail.toDefaultRotation();
                }
            }
        }
    }

    @Override
    public Vector3f getTranslation(LivingEntity user) {
        return new Vector3f(0F/16F, 10.5F/16F * 17F/16F, 8F/16F * 17F/16F);
    }

    @Override
    public Vector3f getOffset(LivingEntity user) {
        return new Vector3f(0F/16F, -1F/16F * 17F/16F, 5.5F/16F * 17F/16F);
    }

    @Override
    public float getPitch(LivingEntity user, float partialTicks) {
        return Mth.clamp(user.getViewXRot(partialTicks), -60F, 60F);
    }

    @Override
    public float getMaxXRot(int state) {
        if(state == 1) return 10;
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
