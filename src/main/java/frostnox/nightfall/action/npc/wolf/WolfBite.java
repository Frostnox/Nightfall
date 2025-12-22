package frostnox.nightfall.action.npc.wolf;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.npc.NPCAttack;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.animal.WolfEntity;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class WolfBite extends NPCAttack {
    public WolfBite(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public WolfBite(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 1.75;
    }

    @Override
    protected EnumMap<EntityPart, AnimationData> getDefaultAnimationData() {
        return WolfEntity.getHeadAnimMap();
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData body = data.get(EntityPart.BODY);
        AnimationData head = data.get(EntityPart.HEAD);
        switch(state) {
            case 0 -> {
                body.rCalc.add(10, 0, 0);
                body.tCalc.add(0, 1, 1);
                head.rCalc.extend(-10 + pitch, 0, 0);
                head.tCalc.add(0, 0F, 1F);
            }
            case 1 -> {
                body.rCalc.add(-15 + pitch / 4, 0, 0, Easing.outQuart);
                body.tCalc.add(0, -1, -1, Easing.outQuart);
                head.rCalc.add(15 - pitch / 4, 0, -25, Easing.outQuart);
                head.tCalc.add(0, 0.5F, -2F, Easing.outQuart);
            }
            case 2 -> {
                body.tCalc.freeze();
                head.tCalc.freeze();
                body.rCalc.freeze();
                head.rCalc.freeze();
            }
            case 3 -> {
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
            switch(state) {
                case 0 -> {
                    tail.rCalc.add(20 + pitch / 2, 0, 0);
                    legLeft.tCalc.add(0, 0, 1.5F);
                    legRight.tCalc.add(0, 0, 1.5F);
                }
                case 1 -> {
                    tail.rCalc.add(10, 0, 0, Easing.outQuart);
                    legLeft.rCalc.add(5, 0, 0, Easing.outCubic);
                    legRight.rCalc.add(5, 0, 0, Easing.outCubic);
                    float y = pitch > 0 ? pitch/50 : 0;
                    legLeft.tCalc.add(0, y + -1, -1.5F, Easing.outQuart);
                    legRight.tCalc.add(0, y + -1, -1.5F, Easing.outQuart);
                    hindLegLeft.tCalc.add(0, -y, 0, Easing.outQuart);
                    hindLegRight.tCalc.add(0, -y, 0, Easing.outQuart);
                }
                case 2 -> {
                    tail.rCalc.freeze();
                    legLeft.rCalc.freeze();
                    legRight.rCalc.freeze();
                    legLeft.tCalc.freeze();
                    legRight.tCalc.freeze();
                    hindLegLeft.tCalc.freeze();
                    hindLegRight.tCalc.freeze();
                }
                case 3 -> {
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
    public Vector3f getTranslation(LivingEntity user) {
        float scale = ((WolfEntity) user).getModelScale();
        return new Vector3f(0F/16F, 12.5F/16F * scale, 7.5F/16F * scale);
    }

    @Override
    public Vector3f getOffset(LivingEntity user) {
        float scale = ((WolfEntity) user).getModelScale();
        return new Vector3f(0F/16F, 0F/16F, 3F/16F * scale);
    }

    @Override
    public float getPitch(LivingEntity user, float partialTicks) {
        return Mth.clamp(user.getViewXRot(partialTicks), -85F, 80F);
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
            case 1 -> 5;
            default -> 45;
        };
    }
}
