package frostnox.nightfall.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

public abstract class SoundToast implements ISoundToast {
    protected abstract Component getText();

    protected abstract void renderBg(PoseStack stack, ToastComponent toastComponent, long timeSinceLastVisible);

    @Override
    public int width() {
        return Math.min(200, 22 + Minecraft.getInstance().font.width(getText().getVisualOrderText()));
    }

    @Override
    public int height() {
        return 22;
    }

    /**
     * @return time in milliseconds to remain visible
     */
    public long visibleTime() {
        return 3000L;
    }

    @Override
    public Visibility render(PoseStack stack, ToastComponent toastComponent, long timeSinceLastVisible) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, OverlayNF.TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        toastComponent.blit(stack, 0, 0, 0, 76, width(), 22 - 2);
        renderBg(stack, toastComponent, timeSinceLastVisible);
        toastComponent.getMinecraft().font.draw(stack, getText(), 20.0F, 6.0F, RenderUtil.COLOR_BLACK);
        return timeSinceLastVisible >= visibleTime() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}
