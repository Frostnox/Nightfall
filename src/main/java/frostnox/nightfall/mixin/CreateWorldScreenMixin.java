package frostnox.nightfall.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    @Unique private static final Object[] DIFFICULTIES = new Object[] {Difficulty.PEACEFUL, Difficulty.NORMAL};

    private CreateWorldScreenMixin(Component pTitle) {
        super(pTitle);
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/CycleButton$Builder;withValues([Ljava/lang/Object;)Lnet/minecraft/client/gui/components/CycleButton$Builder;", ordinal = 1))
    private Object[] nightfall$adjustDifficultyButton(Object[] values) {
        return DIFFICULTIES;
    }

    @ModifyArg(method = "setGameMode", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/CycleButton;setValue(Ljava/lang/Object;)V", ordinal = 2))
    private Object nightfall$adjustHardcoreDifficulty(Object value) {
        return Difficulty.NORMAL;
    }

    @Inject(method = "getEffectiveDifficulty", at = @At("TAIL"), cancellable = true)
    private void nightfall$adjustEffectiveDifficulty(CallbackInfoReturnable<Difficulty> callbackInfo) {
        if(callbackInfo.getReturnValue() == Difficulty.HARD) callbackInfo.setReturnValue(Difficulty.NORMAL);
    }
}
