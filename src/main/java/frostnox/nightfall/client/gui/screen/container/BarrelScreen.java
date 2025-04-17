package frostnox.nightfall.client.gui.screen.container;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.block.barrel.BarrelBlockEntityNF;
import frostnox.nightfall.world.inventory.StorageContainer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BarrelScreen extends SimpleContainerScreen<StorageContainer> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/barrel.png");

    public BarrelScreen(StorageContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    public void innerRender(PoseStack stack, int mouseX, int mouseY, float partial) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = leftPos + 62;
        int y = topPos + 8;
        stack.pushPose();
        RenderSystem.disableDepthTest();
        BarrelBlockEntityNF barrel = (BarrelBlockEntityNF) menu.entity;
        for(int i = 0; i < BarrelBlockEntityNF.MAX_RECIPES; i++) {
            int soakTicks = barrel.soakTicks.getInt(i);
            if(soakTicks > 0) {
                for(int p = 0; p <= (int) ((float) soakTicks / barrel.soakDurations.getInt(i) * 4); p++) {
                    blit(stack, x, y + 54 - (18 * p), 0, 166, 16, 16);
                }
            }
            x += 18;
        }
        RenderSystem.enableDepthTest();
        stack.popPose();
    }
}
