package frostnox.nightfall.action.npc.ectoplasm;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.npc.NPCAttack;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.EctoplasmEntity;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class EctoplasmClubLarge extends NPCAttack {
    public EctoplasmClubLarge(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public EctoplasmClubLarge(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
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
        return EctoplasmEntity.getClubAnimMap();
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData club = data.get(EntityPart.HAND_RIGHT);
        switch(state) {
            case 0 -> {
                club.rCalc.add(-20 + (pitch < 0 ? pitch : 0), 0, 0);
                club.tCalc.extend(club.dTranslation.x(), -1 + (pitch < 0 ? pitch * 0.2F : 0), club.dTranslation.z());
            }
            case 1 -> {
                club.rCalc.add(160, 0, 0, Easing.outQuart);
                club.tCalc.add(0, 3, -5, Easing.outQuart);
            }
            case 2 -> {
                club.toDefault();
            }
        }
    }

    @Override
    public Vector3f getTranslation(LivingEntity user) {
        return new Vector3f(1F/16F, 11F/16F, 8.5F/16F);
    }

    @Override
    public Vector3f getOffset(LivingEntity user) {
        return new Vector3f(0F/16F, 13F/16F, 0F/16F);
    }

    @Override
    public float getPitch(LivingEntity user, float partialTicks) {
        return Mth.clamp(user.getViewXRot(partialTicks), -90F, 90F);
    }

    @Override
    public float getMaxYRot(int state) {
        return switch(state) {
            case 0 -> 45;
            case 1 -> 10;
            default -> 45;
        };
    }
}
