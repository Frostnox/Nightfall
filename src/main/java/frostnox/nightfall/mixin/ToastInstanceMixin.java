package frostnox.nightfall.mixin;

import frostnox.nightfall.client.gui.ISoundToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.gui.components.toasts.ToastComponent$ToastInstance")
public class ToastInstanceMixin<T extends Toast> {
    @Shadow @Final private T toast;

    /**
     * Allow Toast to control sound
     */
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/Toast$Visibility;playSound(Lnet/minecraft/client/sounds/SoundManager;)V"))
    private void nightfall$redirectSound(Toast.Visibility visibility, SoundManager soundManager) {
        if(toast instanceof ISoundToast soundToast) {
            soundToast.playSound(visibility, soundManager);
        }
        else visibility.playSound(soundManager);
    }
}
