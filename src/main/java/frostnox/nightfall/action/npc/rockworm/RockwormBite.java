package frostnox.nightfall.action.npc.rockworm;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.npc.NPCAttack;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.RockwormEntity;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class RockwormBite extends NPCAttack {
    public RockwormBite(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public RockwormBite(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 0;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 2.7;
    }

    @Override
    protected EnumMap<EntityPart, AnimationData> getDefaultAnimationData() {
        return RockwormEntity.getHeadAnimMap();
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData head = data.get(EntityPart.HEAD);
        AnimationData lowerBody = data.get(EntityPart.BODY);
        AnimationData upperBody = data.get(EntityPart.BODY_2);
        switch(state) {
            case 0 -> {
                lowerBody.rCalc.add(35 + pitch * 0.5F, 0, 0, Easing.outCubic);
                upperBody.rCalc.add(-10 + pitch * 0.25F, 0, 0, Easing.outCubic);
                head.rCalc.add(-pitch * 0.25F, 0, 0, Easing.outCubic);
                head.tCalc.add(0, 0, -1, Easing.outCubic);
            }
            case 1 -> {
                head.toDefault();
                lowerBody.toDefault();
                upperBody.toDefault();
            }
        }
    }

    @Override
    public float getPitch(LivingEntity user, float partialTicks) {
        return Mth.clamp(user.getViewXRot(partialTicks) + 30, 0F, 75F);
    }

    @Override
    public Vector3f getTranslation(LivingEntity user) {
        return new Vector3f(0F/16F, 26F/16F, 0F/16F);
    }

    @Override
    public Vector3f getOffset(LivingEntity user) {
        return new Vector3f(0F/16F, 2F/16F, 0F/16F);
    }

    @Override
    public String getName(LivingEntity user) {
        return "bite";
    }
}
