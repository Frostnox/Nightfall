package frostnox.nightfall.client.gui.screen.encyclopedia;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.encyclopedia.EntryStage;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.EntryNotificationToServer;
import frostnox.nightfall.network.message.capability.PuzzleContainerRequestToServer;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import javax.annotation.Nullable;

public class EncyclopediaScreen extends Screen {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/encyclopedia/encyclopedia.png");
    public static final TranslatableComponent LOCKED_ENTRY = new TranslatableComponent("encyclopedia.locked_entry");
    public static final int WIDTH = 396;
    public static final int HEIGHT = 276;
    private final EncyclopediaTab[] tabs;
    private boolean showTabs;
    public static EncyclopediaTab selectedTab;
    private @Nullable Screen entryScreen = null;
    private final Minecraft mc;
    public boolean dirtyTabs = false;

    public EncyclopediaScreen() {
        super(NarratorChatListener.NO_TITLE);
        mc = Minecraft.getInstance();
        tabs = new EncyclopediaTab[ClientEngine.get().getCategories().size()];
        refreshTabs();
    }

    protected EncyclopediaTab getTab(EncyclopediaCategory category) {
        for(EncyclopediaTab tab : tabs) {
            if(tab == null) continue;
            if(category == tab.category) return tab;
        }
        return null;
    }

    public void setEntryScreen(@Nullable Screen screen) {
        entryScreen = screen;
        if(screen != null) entryScreen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        setFocused(entryScreen);
    }

    public void refreshTabs() {
        int i = 0;
        IPlayerData capP = PlayerData.get(mc.player);
        int unlockedTabs = 0;
        for(EncyclopediaCategory category : ClientEngine.get().getCategories()) {
            if(capP.hasEntry(category.unlockEntryId())) {
                tabs[i] = new EncyclopediaTab(category);
                unlockedTabs++;
            }
            i++;
        }
        showTabs = unlockedTabs > 1;
        if(selectedTab == null) selectedTab = getTab(ClientEngine.get().WYLDERY);
        else selectedTab = getTab(selectedTab.category);
    }

    @Override
    protected void init() {
        if(entryScreen != null) entryScreen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    }

    @Override
    public void tick() {
        if(dirtyTabs) {
            dirtyTabs = false;
            refreshTabs();
        }
        selectedTab.tickCount++;
        for(EncyclopediaTab tab : tabs) {
            if(tab == null) continue;
            boolean hasNotifications = false;
            for(ResourceLocation id : PlayerData.get(mc.player).getEntryNotifications()) {
                EntryClient entry = ClientEngine.get().getEntry(id);
                if(entry == null) continue;
                if(entry.category == tab.category) {
                    hasNotifications = true;
                    break;
                }
            }
            tab.hasNotifications = hasNotifications;
        }
        if(entryScreen != null) entryScreen.tick();
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, float partial) {
        if(entryScreen == null) renderBackground(poseStack);
        super.render(poseStack, x, y, partial);
        int xOffset = (this.width - WIDTH) / 2;
        int yOffset = (this.height - HEIGHT) / 2;
        int mouseX = x - xOffset;
        int mouseY = y - yOffset;
        //Inside
        if(selectedTab != null) {
            PoseStack viewStack = RenderSystem.getModelViewStack();
            viewStack.pushPose();
            viewStack.translate(xOffset + 15, yOffset + 15, 0.0D);
            RenderSystem.applyModelViewMatrix();
            selectedTab.render(poseStack);
            viewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
        }
        //Border
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, xOffset, yOffset, getBlitOffset(), 0, 0, WIDTH, HEIGHT, 512, 512);
        //Category tabs
        if(showTabs) {
            for(int i = 0; i < tabs.length; i++) {
                if(tabs[i] == null) continue;
                int tabX = 7 + 24 * i;
                int tabY = -20;
                int yOff = 5;
                if(tabs[i] == selectedTab) yOff = 0;
                else if(entryScreen == null && mouseX >= tabX && mouseX < tabX + 20 && mouseY >= tabY && mouseY < tabY + 20) yOff = 0;
                RenderSystem.setShaderTexture(0, TEXTURE);
                blit(poseStack, tabX + xOffset, tabY + yOff + yOffset, 0, 385, 20, 20 - yOff,  512, 512);
                RenderSystem.setShaderTexture(0, tabs[i].category.icon());
                blit(poseStack, tabX + xOffset + 2, tabY + yOff + yOffset + 2, 0, 0, 16, 16 - Math.max(0, yOff - 2), 16, 16);
                if(tabs[i] == selectedTab) {
                    RenderUtil.renderGradient(poseStack, tabX + xOffset, tabY + yOff + yOffset, 20, 20 - yOff, getBlitOffset(), 0x4f402000);
                }
                if(tabs[i].hasNotifications) {
                    RenderSystem.setShaderTexture(0, TEXTURE);
                    blit(poseStack, tabX + xOffset, tabY + yOff + yOffset, 0, 364, 20, 20 - yOff,  512, 512);
                }
            }
        }
        //Opened entry
        if(entryScreen != null) {
            entryScreen.render(poseStack, x, y, partial);
        }
        else {
            //Category tooltips
            if(showTabs) {
                for(int i = 0; i < tabs.length; i++) {
                    if(tabs[i] == null) continue;
                    int tabX = 7 + 24 * i;
                    int tabY = -20;
                    if(mouseX >= tabX && mouseX < tabX + 20 && mouseY >= tabY && mouseY < tabY + 20) {
                        renderTooltip(poseStack, new TranslatableComponent(tabs[i].category.name()), x, y);
                        break;
                    }
                }
            }
            //Icon tooltips
            if(selectedTab != null && mouseX >= 15 && mouseX < 15 + EncyclopediaTab.BACKGROUND_WIDTH && mouseY >= 15 && mouseY < 15 + EncyclopediaTab.BACKGROUND_HEIGHT) {
                EntryClient entry = selectedTab.hoveredEntry(mouseX, mouseY);
                if(entry != null) {
                    if(PlayerData.get(mc.player).hasEntryStage(entry.entry.getId(), EntryStage.LOCKED)) {
                        renderTooltip(poseStack, LOCKED_ENTRY, x, y);
                    }
                    else renderTooltip(poseStack, new TranslatableComponent(entry.entry.get().getDescriptionId()), x, y);
                }
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if(pKeyCode == ClientEngine.get().keyEncyclopedia.getKey().getValue()) {
            if(entryScreen != null) {
                mc.player.connection.send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                mc.player.containerMenu = mc.player.inventoryMenu;
                entryScreen = null;
                setFocused(null);
            }
            else this.onClose();
            return true;
        }
        else if(pKeyCode == mc.options.keyInventory.getKey().getValue()) {
            this.onClose();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
        if(entryScreen != null) return entryScreen.mouseDragged(x, y, button, dragX, dragY);
        int xOffset = (this.width - WIDTH) / 2 + 15;
        int yOffset = (this.height - HEIGHT) / 2 + 15;
        if(button == 0 && selectedTab != null && x >= xOffset && x < xOffset + EncyclopediaTab.BACKGROUND_WIDTH && y >= yOffset && y < yOffset + EncyclopediaTab.BACKGROUND_HEIGHT) {
            selectedTab.scroll(dragX, dragY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if(entryScreen != null) return entryScreen.mouseClicked(x, y, button);
        int xOffset = (this.width - WIDTH) / 2 + 15;
        int yOffset = (this.height - HEIGHT) / 2 + 15;
        double mouseX = x - (this.width - WIDTH) / 2;
        double mouseY = y - (this.height - HEIGHT) / 2;
        if(showTabs) {
            for(int i = 0; i < tabs.length; i++) {
                if(tabs[i] == null) continue;
                int tabX = 7 + 24 * i;
                int tabY = -20;
                if(mouseX >= tabX && mouseX < tabX + 20 && mouseY >= tabY && mouseY < tabY + 20) {
                    if(tabs[i] != selectedTab) {
                        selectedTab = tabs[i];
                        selectedTab.tickCount = 0;
                        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    }
                }
            }
        }
        if(button == 0 && selectedTab != null && x >= xOffset && x < xOffset + EncyclopediaTab.BACKGROUND_WIDTH && y >= yOffset && y < yOffset + EncyclopediaTab.BACKGROUND_HEIGHT) {
            EntryClient entry = selectedTab.hoveredEntry(mouseX, mouseY);
            if(entry != null) {
                IPlayerData capP = PlayerData.get(mc.player);
                ResourceLocation id = entry.entry.getId();
                EntryStage stage = capP.getStage(id);
                if(stage == EntryStage.HIDDEN || stage == EntryStage.LOCKED) return false;
                else {
                    if(capP.hasEntryNotification(id)) {
                        capP.removeEntryNotification(id);
                        NetworkHandler.toServer(new EntryNotificationToServer(id, true));
                    }
                    if(stage == EntryStage.PUZZLE) NetworkHandler.toServer(new PuzzleContainerRequestToServer(id));
                    else if(stage == EntryStage.COMPLETED) setEntryScreen(new EntryCompletedScreen(entry));
                }
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 0.85F));
                return true;
            }
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if(entryScreen != null) return entryScreen.mouseReleased(x, y, button);
        else return super.mouseReleased(x, y, button);
    }

    @Override
    public void onClose() {
        if(entryScreen != null) entryScreen.onClose();
        super.onClose();
    }
}
