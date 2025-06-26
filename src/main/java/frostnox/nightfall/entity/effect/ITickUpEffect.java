package frostnox.nightfall.entity.effect;

import net.minecraft.world.entity.LivingEntity;

public interface ITickUpEffect {
    boolean shouldTickUp(LivingEntity entity, int amplifier, int duration);

    default void onTickUp(LivingEntity entity, int amplifier, int duration) {}
}
