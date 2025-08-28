package frostnox.nightfall.action.npc.ectoplasm;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class EctoplasmClubMedium extends EctoplasmClubLarge {
    public EctoplasmClubMedium(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public EctoplasmClubMedium(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData club = data.get(EntityPart.HAND_RIGHT);
        switch(state) {
            case 0 -> {
                club.rCalc.add(-20 + (pitch < 0 ? pitch : 0), 0, 0);
                club.tCalc.extend(club.dTranslation.x(), -1 + (pitch < 0 ? pitch * 0.1F : 0), club.dTranslation.z());
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
        return new Vector3f(1F/16F, 10F/16F, 0F/16F);
    }
}
