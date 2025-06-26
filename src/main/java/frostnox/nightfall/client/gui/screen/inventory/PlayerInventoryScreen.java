package frostnox.nightfall.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.gui.screen.PartialInventoryScreen;
import frostnox.nightfall.client.gui.screen.ScreenGuiComponent;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class PlayerInventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/player_inventory.png");
    public static final int IMAGE_HEIGHT = 166;
    public final RecipeSearchComponent recipeSearch;
    private boolean recipeSearchInit = false;
    public final PlayerStatsComponent stats;
    public ScreenGuiComponent selectedComponent;
    private int selectedIndex = -1;
    private float xOld, yOld;

    public PlayerInventoryScreen(Player player) {
        super(player.inventoryMenu, player.getInventory(), new TranslatableComponent("container.crafting"));
        passEvents = true;
        titleLabelX = 98;
        titleLabelY = 13;
        recipeSearch = new RecipeSearchComponent(this);
        stats = new PlayerStatsComponent(this);
    }

    @Override
    public void containerTick() {
        if(minecraft.gameMode.hasInfiniteItems()) {
            minecraft.setScreen(new CreativePlayerInventoryScreen(minecraft.player));
        }
        else if(selectedComponent != null) selectedComponent.containerTick();
    }

    @Override
    protected void init() {
        if(minecraft.gameMode.hasInfiniteItems()) {
            minecraft.setScreen(new CreativePlayerInventoryScreen(minecraft.player));
        }
        else {
            super.init();
            if(!recipeSearchInit) {
                recipeSearch.init();
                recipeSearchInit = true;
            }
            if(selectedComponent != null) selectedComponent.onOpen();
            stats.init();
            minecraft.keyboardHandler.setSendRepeatsToGui(true);
        }
    }

    public int getLeftPos() {
        return leftPos;
    }

    public int getTopPos() {
        return topPos;
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int x, int pY) {
        font.draw(pPoseStack, title, (float) titleLabelX, (float) titleLabelY, RenderUtil.COLOR_BLACK);
    }

    @Override
    protected void renderEffects(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        //Cancel effects in super render call so we can do them later
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
        renderBackground(poseStack);
        int x = leftPos;
        int y = topPos;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        //Tabs
        for(int i = 0; i < 2; i++) {
            int tabX = x + 7 + 24 * i;
            int tabY = y - 19;
            int v = IMAGE_HEIGHT;
            int yOff = 5;
            if(i == selectedIndex) {
                v += 20;
                yOff = 0;
            }
            else if(mouseX >= tabX && mouseX < tabX + 20 && mouseY >= tabY && mouseY < tabY + 20) yOff = 0;
            blit(poseStack, tabX, tabY + yOff, 0, v, 20, 20 - yOff,  512, 256);
            int u = 20 + 20 * i;
            v = IMAGE_HEIGHT;
            blit(poseStack, tabX, tabY + yOff, u, v, 20, 20 - yOff, 512, 256);
        }
        super.render(poseStack, mouseX, mouseY, partial);
        //Component
        if(selectedComponent != null) selectedComponent.render(poseStack, mouseX, mouseY, partial);
        //Effects
        if(selectedComponent != recipeSearch || !recipeSearch.isRecipeSelected()) super.renderEffects(poseStack, mouseX, mouseY);
        //Inventory tooltips
        renderTooltip(poseStack, mouseX, mouseY);
        xOld = (float) mouseX;
        yOld = (float) mouseY;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partial, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, PartialInventoryScreen.INVENTORY_WOOD_TEXTURE);
        int x = leftPos;
        int y = topPos;
        blit(poseStack, x + 7, y + 83, 7, 7, 162, 76, PartialInventoryScreen.WIDTH, PartialInventoryScreen.HEIGHT);
        //Expandable inventory
        for(int i = 0; i < AttributesNF.getInventoryCapacity(minecraft.player); i++) {
            int offX = 7 + (i % 4) * 18;
            int offY = 7 + (i / 4) * 18;
            int slotX = x + 90 + offX;
            int slotY = y + 83 + (i / 4) * 18;
            blit(poseStack, slotX, slotY, 18 + offX, offY, 18, 18, PartialInventoryScreen.WIDTH, PartialInventoryScreen.HEIGHT);
        }
        //Frame
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight, 512, 256);
        //Player
        PoseStack modelStack = RenderSystem.getModelViewStack();
        modelStack.pushPose();
        modelStack.translate(0.5D, 0, 0); //Center is at 49.5
        InventoryScreen.renderEntityInInventory(x + 49, y + 73, 30, (x + 49.5F) - xOld, (y + 73F - 50F) - yOld, minecraft.player);
        modelStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = leftPos;
        int y = topPos;
        for(int i = 0; i < 2; i++) {
            int tabX = x + 7 + 24 * i;
            int tabY = y - 19;
            if(mouseX >= tabX && mouseX < tabX + 20 && mouseY >= tabY && mouseY < tabY + 20) {
                if(selectedIndex == i) {
                    selectedIndex = -1;
                    if(selectedComponent != null) selectedComponent.onClose();
                    selectedComponent = null;
                }
                else {
                    selectedIndex = i;
                    if(selectedComponent != null) selectedComponent.onClose();
                    if(i == 0) selectedComponent = recipeSearch;
                    else if(i == 1) selectedComponent = stats;
                    selectedComponent.onOpen();
                }
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
            }
        }
        if(selectedComponent != null && selectedComponent.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(selectedComponent != null && selectedComponent.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollAmount) {
        if(selectedComponent != null && selectedComponent.mouseScrolled(x, y, scrollAmount)) return true;
        return super.mouseScrolled(x, y, scrollAmount);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
        if(selectedComponent != null && selectedComponent.mouseDragged(x, y, button, dragX, dragY)) return true;
        return super.mouseDragged(x, y, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if(selectedComponent != null && selectedComponent.keyPressed(pKeyCode, pScanCode, pModifiers)) return true;
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if(selectedComponent != null && selectedComponent.keyReleased(pKeyCode, pScanCode, pModifiers)) return true;
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if(selectedComponent != null && selectedComponent.charTyped(pCodePoint, pModifiers)) return true;
        return super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public void renderTooltip(PoseStack poseStack, ItemStack itemStack, int x, int y) {
        super.renderTooltip(poseStack, itemStack, x, y);
    }

    @Override
    public void removed() {
        if(recipeSearchInit) recipeSearch.removed();
        minecraft.keyboardHandler.setSendRepeatsToGui(false);
        super.removed();
    }
}
