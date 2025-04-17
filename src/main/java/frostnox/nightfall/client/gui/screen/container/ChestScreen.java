package frostnox.nightfall.client.gui.screen.container;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.gui.screen.PartialInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

public class ChestScreen extends PartialInventoryScreen<ChestMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/generic_54.png");
    private final int containerRows;

    public ChestScreen(ChestMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        passEvents = false;
        containerRows = pMenu.getRowCount();
        imageHeight = 114 + containerRows * 18;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int mouseX, int mouseY) {
        pPoseStack.pushPose();
        if(containerRows > 4) pPoseStack.translate(0, (containerRows - 4) * 18 - 9 - 19, 0);
        else pPoseStack.translate(0, -19, 0);
        super.renderBg(pPoseStack, pPartialTick, mouseX, mouseY);
        pPoseStack.popPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(pPoseStack, x, y, 0, 0, imageWidth, containerRows * 18 + 7);
        blit(pPoseStack, x, y + containerRows * 18 + 7, 0, 115, imageWidth, 87);
    }
}
