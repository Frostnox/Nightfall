package frostnox.nightfall.action;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.function.Supplier;

public class AttackEffect {
    public final Supplier<MobEffect> effect;
    public final int duration, amplifier;
    public final float chance;

    public AttackEffect(Supplier<MobEffect> effect, int duration, int amplifier, float chance) {
        this.effect = effect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.chance = chance;
    }

    public MobEffectInstance getEffect() {
        return new MobEffectInstance(effect.get(), duration, amplifier);
    }
}