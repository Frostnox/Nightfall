package frostnox.nightfall.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.registry.forge.AttributesNF;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class PartialInventoryScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public static final ResourceLocation INVENTORY_WOOD_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/partial_inventory_wood.png");
    public static final ResourceLocation INVENTORY_TERRACOTTA_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/partial_inventory_terracotta.png");
    public static final ResourceLocation INVENTORY_METAL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/partial_inventory_metal.png");
    public static final ResourceLocation INVENTORY_PAPER_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/partial_inventory_paper.png");
    public static final int WIDTH = 176, HEIGHT = 90;

    public PartialInventoryScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    protected ResourceLocation getInventoryTexture() {
        return INVENTORY_WOOD_TEXTURE;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partial, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, getInventoryTexture());
        int x = leftPos + imageWidth / 2 - WIDTH / 2;
        int y = topPos + imageHeight / 2 - 7;
        blit(poseStack, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
        //Expandable inventory
        for(int i = 0; i < AttributesNF.getInventoryCapacity(minecraft.player); i++) {
            int offX = 7 + (i % 4) * 18;
            int offY = 7 + (i / 4) * 18;
            int slotX = x + 18 * 5 + offX;
            int slotY = y + 7 + (i / 4) * 18;
            blit(poseStack, slotX, slotY, 18 + offX, offY, 18, 18, WIDTH, HEIGHT);
        }
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {

    }
}
