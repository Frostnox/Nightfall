package frostnox.nightfall.client.gui.screen.container;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.client.gui.screen.PartialInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class SimpleContainerScreen<T extends AbstractContainerMenu> extends PartialInventoryScreen<T> {
    public SimpleContainerScreen(T container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    public SimpleContainerScreen(T container, Inventory inventory, Component title, int xOff, int yOff) {
        super(container, inventory, title, xOff, yOff);
    }

    public abstract ResourceLocation getTexture();

    public void innerRender(PoseStack stack, int mouseX, int mouseY, float partial) {

    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partial) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, partial);
        innerRender(stack, mouseX, mouseY, partial);
        renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack stack, float partial, int mouseX, int mouseY) {
        super.renderBg(stack, partial, mouseX, mouseY);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, getTexture());
        blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
