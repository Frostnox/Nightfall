package frostnox.nightfall.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Gui.class)
public abstract class GuiMixin extends GuiComponent {
    /**
     * Fix for MC-67532: Bottom 2 rows of pixels in selected hotbar slot texture don't appear in game
     * Note that this only fixes the upper row; adjusting the position along with the UV would completely fix it
     */
    @ModifyArg(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V", ordinal = 1), index = 6)
    private int nightfall$fixVHeight(int v) {
        return 23;
    }
}
