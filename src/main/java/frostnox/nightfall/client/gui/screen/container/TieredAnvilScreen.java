package frostnox.nightfall.client.gui.screen.container;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.block.anvil.TieredAnvilContainer;
import frostnox.nightfall.client.gui.screen.PartialInventoryScreen;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.blockentity.StartSmithingToServer;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class TieredAnvilScreen extends PartialInventoryScreen<TieredAnvilContainer> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/tiered_anvil.png");
    private int startIndex;
    private boolean displayRecipes;
    private float scrollOffs;
    private boolean grabbedScrollBar = false;

    public TieredAnvilScreen(TieredAnvilContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected ResourceLocation getInventoryTexture() {
        return INVENTORY_METAL_TEXTURE;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partial) {
        if(menu.getSelectedRecipeIndex() == -1) {
            displayRecipes = menu.hasValidInput();
            if(!displayRecipes) startIndex = 0;
        }
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, partial);
        renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack stack, float partial, int mouseX, int mouseY) {
        super.renderBg(stack, partial, mouseX, mouseY);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        //Start button
        int buttonX = leftPos + 143;
        int buttonY = topPos + 35;
        int heatTier = menu.entity.getBestHeatSource().getTier();
        if(menu.getSelectedRecipeIndex() != -1 && heatTier >= menu.recipes.get(menu.getSelectedRecipeIndex()).getTier()) {
            double xPos = mouseX - buttonX;
            double yPos = mouseY - buttonY;
            if(xPos >= -1D && xPos < 17D && yPos >= -1D && yPos < 17D) {
                RenderUtil.renderGradient(stack, buttonX, buttonY, 16, 16, getBlitOffset(), RenderUtil.COLOR_SLOT_HIGHLIGHT);
            }
        }
        else RenderUtil.renderGradient(stack, buttonX, buttonY, 16, 16, getBlitOffset(), RenderUtil.COLOR_SLOT_DARKEN);
        //Water/heat indicators
        if(menu.entity.hasWaterSource()) blit(stack, leftPos + 143, topPos + 14, 0, 184, 16, 15);
        if(heatTier > 0) blit(stack, leftPos + 143, topPos + 58, 17 * heatTier, 184, 16, 15);
        //Scrollbar
        if(canScroll()) {
            blit(stack, leftPos + 115, topPos + 16 + (int) ((54 - 3) * scrollOffs), 18, 166, 3, 3);
        }
        int listStart = startIndex + 9;
        int listX = leftPos + 58;
        int listY = topPos + 16;
        //Recipe buttons
        for(int i = startIndex; i < listStart && i < menu.recipes.size(); ++i) {
            int offset = i - startIndex;
            int x = listX + offset % 3 * 18;
            int row = offset / 3;
            int y = listY + row * 18;
            blit(stack, x, y, 0, imageHeight, 18, 18);
        }
        //Items
        for(int i = startIndex; i < listStart && i < menu.recipes.size(); ++i) {
            int offset = i - startIndex;
            int x = listX + offset % 3 * 18 + 1;
            int row = offset / 3;
            int y = listY + row * 18 + 1;
            minecraft.getItemRenderer().renderAndDecorateItem(menu.recipes.get(i).getResultItem(), x, y);
        }
        //Recipe button overlay
        for(int i = startIndex; i < listStart && i < menu.recipes.size(); ++i) {
            int offset = i - startIndex;
            int x = listX + offset % 3 * 18;
            int row = offset / 3;
            int y = listY + row * 18;
            if(i == menu.getSelectedRecipeIndex()) {
                RenderUtil.renderGradient(stack, x + 1, y + 1, 16, 16, getBlitOffset(), RenderUtil.COLOR_SLOT_DARKEN);
            }
            else if(mouseX >= x && mouseY >= y && mouseX < x + 18 && mouseY < y + 18) {
                RenderUtil.renderGradient(stack, x + 1, y + 1, 16, 16, getBlitOffset(), RenderUtil.COLOR_SLOT_HIGHLIGHT);
            }
        }
    }

    @Override
    public void onClose() {
        grabbedScrollBar = false;
        super.onClose();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(button == 0) grabbedScrollBar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(button == 0) updateScroll(mouseX, mouseY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button == 0 && updateScroll(mouseX, mouseY)) {
            grabbedScrollBar = true;
            return true;
        }
        if(menu.getSelectedRecipeIndex() != -1) {
            int x = leftPos + 143;
            int y = topPos + 35;
            double xPos = mouseX - x;
            double yPos = mouseY - y;
            if(xPos >= -1D && xPos < 17D && yPos >= -1D && yPos < 17D) {
                ResourceLocation id = menu.recipes.get(menu.getSelectedRecipeIndex()).getId();
                if(menu.entity.canStartSmithing(minecraft.player, id)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    NetworkHandler.toServer(new StartSmithingToServer(id, menu.containerId));
                }
            }
        }
        if(displayRecipes) {
            int listX = leftPos + 58;
            int listY = topPos + 16;
            int listStart = startIndex + 9;
            for(int i = startIndex; i < listStart; ++i) {
                int i1 = i - startIndex;
                double d0 = mouseX - (double)(listX + i1 % 3 * 18);
                double d1 = mouseY - (double)(listY + i1 / 3 * 18);
                if(d0 >= 0.0D && d1 >= 0.0D && d0 < 18.0D && d1 < 18.0D && menu.clickMenuButton(minecraft.player, i)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    return true;
                }
            }
        }
        if(menu.recipes.size() > 9 && (canScrollUp() || canScrollDown())) {
            int arrowX = leftPos + 112;
            int upY = topPos + 19;
            int downY = topPos + 55;
            double xPos = mouseX - arrowX;
            double yPosUp = mouseY - upY;
            double yPosDown = mouseY - downY;
            if(xPos >= 0D && xPos < 13D) {
                if(yPosUp >= 0D && yPosUp < 12D && canScrollUp()) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    startIndex -= 3;
                    scrollOffs = Mth.clamp(scrollOffs - (1F / getOffScreenRows()), 0F, 1F);
                    return true;
                }
                else if(yPosDown >= 0D && yPosDown < 12D && canScrollDown()) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    startIndex += 3;
                    scrollOffs = Mth.clamp(scrollOffs + (1F / getOffScreenRows()), 0F, 1F);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollAmount) {
        if(scrollAmount < 0D ? canScrollDown() : canScrollUp()) {
            int offScreenRows = getOffScreenRows();
            scrollOffs = (float)((double)scrollOffs - scrollAmount / (double)offScreenRows);
            scrollOffs = Mth.clamp(scrollOffs, 0.0F, 1.0F);
            startIndex = (int)((double)(scrollOffs * (float)offScreenRows) + 0.5D) * 3;
        }
        return true;
    }

    private boolean updateScroll(double mouseX, double mouseY) {
        if(!canScroll()) return false;
        int barX = leftPos + 115;
        int barYMin = topPos + 16;
        int barYMax = barYMin + 54;
        if(grabbedScrollBar || (mouseX >= barX - 1 && mouseX <= barX + 3 && mouseY >= barYMin && mouseY <= barYMax)) {
            scrollOffs = (float) ((mouseY - barYMin - 2) / (54 - 3));
            scrollOffs = Mth.clamp(scrollOffs, 0, 1);
            startIndex = (int) ((scrollOffs * getOffScreenRows()) + 0.5) * 3;
            return true;
        }
        return false;
    }

    private int getOffScreenRows() {
        return (Math.max(0, menu.recipes.size() - 9) + 3 - 1) / 3;
    }

    private boolean canScrollUp() {
        return startIndex != 0;
    }

    private boolean canScrollDown() {
        return menu.recipes.size() - startIndex > 9;
    }

    private boolean canScroll() {
        return menu.recipes.size() > 9;
    }

    @Override
    protected void renderTooltip(PoseStack stack, int mouseX, int mouseY) {
        super.renderTooltip(stack, mouseX, mouseY);
        if(displayRecipes) {
            int listX = leftPos + 58;
            int listY = topPos + 16;
            int listStart = startIndex + 9;
            for(int i = startIndex; i < listStart && i < menu.recipes.size(); ++i) {
                int i1 = i - startIndex;
                int j1 = listX + i1 % 3 * 18;
                int k1 = listY + i1 / 3 * 18;
                if(mouseX >= j1 && mouseX < j1 + 18 && mouseY >= k1 && mouseY < k1 + 18) {
                    renderTooltip(stack, menu.recipes.get(i).getResultItem(), mouseX, mouseY);
                }
            }
        }
    }
}
