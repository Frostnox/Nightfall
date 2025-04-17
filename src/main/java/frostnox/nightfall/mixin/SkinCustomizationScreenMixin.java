package frostnox.nightfall.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SkinCustomizationScreen.class)
public abstract class SkinCustomizationScreenMixin extends OptionsSubScreen {
    private SkinCustomizationScreenMixin(Screen pLastScreen, Options pOptions, Component pTitle) {
        super(pLastScreen, pOptions, pTitle);
    }

    /**
     * Allowing players to change the main arm would cause significant functional changes during attacks, so the feature is removed
     */
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/SkinCustomizationScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 1))
    private <T extends GuiEventListener & Widget & NarratableEntry> T nightfall$removeMainHandWidget(SkinCustomizationScreen screen, T widget) {
        return (T) new Dummy();
    }

    private static class Dummy implements GuiEventListener, Widget, NarratableEntry {
        @Override
        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {}

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}
    }
}
