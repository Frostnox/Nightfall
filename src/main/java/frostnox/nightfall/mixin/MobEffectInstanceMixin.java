package frostnox.nightfall.mixin;

import frostnox.nightfall.entity.effect.ITickUpEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements Comparable<MobEffectInstance>, net.minecraftforge.common.extensions.IForgeMobEffectInstance {
    private @Shadow @Final MobEffect effect;
    private @Shadow int duration;
    private @Shadow int amplifier;
    private @Shadow @Nullable MobEffectInstance hiddenEffect;

    /**
     * Allow ticking effects up instead of always down
     */
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;tickDownDuration()I", shift = At.Shift.AFTER))
    private void tickUpDuration(LivingEntity pEntity, Runnable pOnExpirationRunnable, CallbackInfoReturnable<Boolean> callbackInfo) {
        if(effect instanceof ITickUpEffect tickUpEffect && tickUpEffect.shouldTickUp(pEntity, amplifier, duration)) {
            duration += 2;
            tickUpEffect.onTickUp(pEntity, amplifier, duration);
        }
        if(hiddenEffect instanceof ITickUpEffect tickUpEffect && tickUpEffect.shouldTickUp(pEntity, amplifier, duration)) {
            duration += 2;
            tickUpEffect.onTickUp(pEntity, amplifier, duration);
        }
    }
}
