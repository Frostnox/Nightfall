package frostnox.nightfall.mixin;

import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    /**
     * Change splash text color from yellow (0xFFFF00) to gold
     */
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;drawCenteredString(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"), index = 5)
    private int nightfall$changeSplashColor(int color) {
        return 0xebca63 | ((color >>> 24) << 24); //Preserve alpha present in original
    }
}
