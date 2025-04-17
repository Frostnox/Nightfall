package frostnox.nightfall.action;

import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class GenericAttack extends Attack {
    public GenericAttack(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public GenericAttack(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {

    }

    @Override
    public void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {

    }

    @Override
    public float getPitch(LivingEntity user, float partialTicks) {
        return 0;
    }

    @Override
    public boolean isStateDamaging(int state) {
        return false;
    }

    @Override
    protected EnumMap<EntityPart, AnimationData> getDefaultAnimationData() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.HAND_RIGHT, new AnimationData());
        return map;
    }

    @Override
    public float getMaxXRot(int state) {
        return 360;
    }

    @Override
    public float getMaxYRot(int state) {
        return 360;
    }
}
