package frostnox.nightfall.action.npc.cockatrice;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.npc.NPCAttack;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.CockatriceEntity;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class CockatriceBite extends NPCAttack {
    public CockatriceBite(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public CockatriceBite(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 2.25;
    }

    @Override
    protected EnumMap<EntityPart, AnimationData> getDefaultAnimationData() {
        return CockatriceEntity.getHeadAnimMap();
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData neck = data.get(EntityPart.NECK);
        AnimationData head = data.get(EntityPart.HEAD);
        switch(state) {
            case 0 -> {
                neck.rCalc.extend(70 + pitch, 0, 0);
                head.rCalc.extend(-70 + pitch, 0, 0);
                head.tCalc.add(0, 1F, 0F);
            }
            case 1 -> {
                neck.rCalc.add(20, 0, 0, Easing.outQuart);
                head.rCalc.add(-pitch/2F, 10, 0, Easing.outQuart);
                head.tCalc.add(0, -1.5F, 0F, Easing.outQuart);
            }
            case 2 -> {
                head.toDefault();
                neck.toDefaultRotation();
            }
        }
        if(data.size() != 3) {
            AnimationData tail = data.get(EntityPart.TAIL);
            AnimationData wingRight = data.get(EntityPart.WING_RIGHT);
            AnimationData wingLeft = data.get(EntityPart.WING_LEFT);
            switch(state) {
                case 0 -> {
                    tail.rCalc.add(10, 0, 0);
                    wingRight.rCalc.add(8, 0, 8);
                    wingLeft.rCalc.add(8, 0, -8);
                }
                case 1 -> {
                    tail.rCalc.freeze();
                    wingRight.rCalc.add(-8, 0, -12.5F, Easing.outQuart);
                    wingLeft.rCalc.add(-8, 0, 12.5F, Easing.outQuart);
                }
                case 2 -> {
                    tail.toDefaultRotation();
                    wingRight.toDefaultRotation();
                    wingLeft.toDefaultRotation();
                }
            }
        }
    }

    @Override
    public Vector3f getTranslation(LivingEntity user) {
        return new Vector3f(0F/16F, 19.5F/16F, 5F/16F);
    }

    @Override
    public Vector3f getOffset(LivingEntity user) {
        return new Vector3f(0F/16F, 1.5F/16F, 3F/16F);
    }

    @Override
    public float getPitch(LivingEntity user, float partialTicks) {
        return Mth.clamp(user.getViewXRot(partialTicks) + 10F, -90F, 90F);
    }

    @Override
    public void onTick(LivingEntity user) {
        if(!user.level.isClientSide && user instanceof Mob mob) {
            IActionTracker capA = ActionTracker.get(user);
            if(capA.getState() == 0 && capA.getFrame() == capA.getDuration() - 1) {
                CombatUtil.addMovementTowardsTarget(0.5, 1, mob);
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
            case 0 -> 20;
            case 1 -> 0;
            default -> 30;
        };
    }
}
