package frostnox.nightfall.client.gui.screen.encyclopedia;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.PartialInventoryScreen;
import frostnox.nightfall.encyclopedia.EntryStage;
import frostnox.nightfall.encyclopedia.Puzzle;
import frostnox.nightfall.encyclopedia.PuzzleContainer;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericToServer;
import frostnox.nightfall.registry.KnowledgeNF;
import frostnox.nightfall.registry.WorldConditionsNF;
import frostnox.nightfall.util.RenderUtil;
import frostnox.nightfall.world.condition.WorldCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EntryPuzzleScreen extends PartialInventoryScreen<PuzzleContainer> {
    public static final int BACKGROUND_WIDTH = 110, BACKGROUND_HEIGHT = 68;
    public static final TranslatableComponent MISSING_KNOWLEDGE = new TranslatableComponent("encyclopedia.missing_knowledge");
    public static final TranslatableComponent HIDDEN_KNOWLEDGE = new TranslatableComponent("encyclopedia.hidden_knowledge");
    private final static int MAX_IMAGE_SIZE = 150;
    private final Minecraft mc;
    private final EntryClient cEntry;
    private final String tipPrefix;

    public EntryPuzzleScreen(PuzzleContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        mc = Minecraft.getInstance();
        cEntry = ClientEngine.get().getEntry(pMenu.entry.getRegistryName());
        tipPrefix = cEntry.entry.get().getDescriptionId();
        ClientEngine.get().openEntry = cEntry;
    }

    @Override
    protected ResourceLocation getInventoryTexture() {
        return INVENTORY_PAPER_TEXTURE;
    }

    @Override
    protected void containerTick() {
        EntryStage stage = PlayerData.get(mc.player).getStage(cEntry.entry.getId());
        if(stage != EntryStage.PUZZLE) {
            mc.player.connection.send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
            mc.player.containerMenu = mc.player.inventoryMenu;
            if(mc.screen instanceof EncyclopediaScreen encyclopediaScreen) {
                if(stage == EntryStage.COMPLETED) {
                    encyclopediaScreen.setEntryScreen(new EntryCompletedScreen(cEntry));
                }
                else encyclopediaScreen.setEntryScreen(null);
            }
            else onClose();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        mc.player.connection.send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
        mc.player.containerMenu = mc.player.inventoryMenu;
        if(mc.screen instanceof EncyclopediaScreen encyclopediaScreen) encyclopediaScreen.setEntryScreen(null);
        ClientEngine.get().openEntry = null;
    }

    @Override
    protected void renderBg(PoseStack stack, float partial, int mouseX, int mouseY) {
        RenderSystem.disableDepthTest();
        renderBackground(stack);
        super.renderBg(stack, partial, mouseX, mouseY);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, EncyclopediaScreen.TEXTURE);
        stack.pushPose();
        int xOff = width/2 - BACKGROUND_WIDTH/2;
        int yOff = height/2 - BACKGROUND_HEIGHT/2 - HEIGHT/2;
        stack.translate(xOff, yOff, 0);
        Puzzle puzzle = cEntry.entry.get().puzzle;
        if(puzzle != null) {
            //Background
            int rows = 1;
            if(!puzzle.knowledge().isEmpty()) rows++;
            if(!puzzle.conditions().isEmpty()) rows++;
            if(rows == 1) {
                blit(stack, 0, BACKGROUND_HEIGHT - 26, 0, 512 - BACKGROUND_HEIGHT, BACKGROUND_WIDTH, 4 + 18, 512, 512);
                blit(stack, 0, BACKGROUND_HEIGHT - 26 + 22, 0, 512 - 4, BACKGROUND_WIDTH, 4, 512, 512);
            }
            else if(rows == 2) {
                blit(stack, 0, BACKGROUND_HEIGHT - 26 - 21, 0, 512 - BACKGROUND_HEIGHT, BACKGROUND_WIDTH, 4 + 18 + 3 + 18, 512, 512);
                blit(stack, 0, BACKGROUND_HEIGHT - 26 + 22, 0, 512 - 4, BACKGROUND_WIDTH, 4, 512, 512);
            }
            else blit(stack, 0, 0, 0, 512 - BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 512, 512);
            //Item slots
            for(int i = 0; i < puzzle.ingredients().size(); i++) {
                blit(stack, 4 + i * 21, 46, 0, 406, 18, 18, 512, 512);
            }
            //Knowledge/condition slots
            for(int x = 1; x <= rows - 1; x++) {
                int slots = rows == 2 ? (puzzle.knowledge().isEmpty() ? puzzle.conditions().size() : puzzle.knowledge().size()) :
                        (x == 1 ? puzzle.knowledge().size() : puzzle.conditions().size());
                for(int i = 0; i < slots; i++) {
                    blit(stack, 4 + i * 21, 46 - 21 * x, 19, 406, 18, 18, 512, 512);
                }
            }
            //Experiment button
            blit(stack, 4 + puzzle.ingredients().size() * 21, 46, 0, 425, 18, 18, 512, 512);
        }
        renderImage(stack, cEntry.puzzleImage);
        stack.popPose();
    }

    @Override
    public void render(PoseStack stack, int x, int y, float partial) {
        super.render(stack, x, y, partial);
        if(cEntry != null) {
            Puzzle puzzle = cEntry.entry.get().puzzle;
            if(puzzle != null) {
                stack.pushPose();
                int xOff = width/2 - BACKGROUND_WIDTH/2;
                int yOff = height/2 - BACKGROUND_HEIGHT/2 - HEIGHT/2;
                IPlayerData capP = PlayerData.get(mc.player);
                stack.translate(xOff, yOff, 200);
                RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
                for(int i = 0; i < puzzle.conditions().size(); i++) {
                    WorldCondition condition = puzzle.conditions().get(i).get();
                    int xPos = 5 + i * 21, yPos = puzzle.knowledge().isEmpty() ? 26 : 5;
                    if(condition.test(mc.player)) RenderUtil.renderSprite(stack, xPos, yPos, WorldConditionsNF.SATISFIED_CONDITION_TEXTURE);
                    else RenderUtil.renderSprite(stack, xPos, yPos, WorldConditionsNF.UNSATISFIED_CONDITION_TEXTURE);
                }
                for(int i = 0; i < puzzle.knowledge().size(); i++) {
                    int xPos = 5 + i * 21, yPos = 26;
                    Knowledge knowledge = puzzle.knowledge().get(i).get();
                    if(!capP.hasKnowledge(knowledge.getRegistryName())) RenderUtil.renderSprite(stack, xPos, yPos, KnowledgeNF.UNKNOWN_KNOWLEDGE_TEXTURE);
                    else RenderUtil.renderSprite(stack, xPos, yPos, KnowledgeNF.KNOWLEDGE_TEXTURE);
                }
                if(!canExperiment(puzzle)) {
                    RenderUtil.renderGradient(stack, 4 + puzzle.ingredients().size() * 21, 46, 18, 18, getBlitOffset(), RenderUtil.COLOR_SLOT_DARKEN);
                    if(isHoveringExperimentButton(x, y, puzzle) && !puzzle.hasAllKnowledge(PlayerData.get(mc.player))) {
                        renderTooltip(stack, MISSING_KNOWLEDGE, x - xOff, y - yOff);
                    }
                }
                else if(isHoveringExperimentButton(x, y, puzzle)) {
                    RenderUtil.renderGradient(stack, 4 + puzzle.ingredients().size() * 21, 46, 18, 18, getBlitOffset(), RenderUtil.COLOR_SLOT_HIGHLIGHT);
                }
                //Tooltips
                for(int i = 0; i < puzzle.conditions().size(); i++) {
                    int xPos = xOff + 4 + i * 21, yPos = yOff + (puzzle.knowledge().isEmpty() ? 25 : 4);
                    if(x >= xPos && x < xPos + 18 && y >= yPos && y < yPos + 18) {
                        Component tip = new TranslatableComponent(tipPrefix + ".condition_" + i);
                        renderTooltip(stack, tip, x - xOff, y - yOff);
                    }
                }
                for(int i = 0; i < puzzle.knowledge().size(); i++) {
                    int xPos = xOff + 4 + i * 21, yPos = yOff + 25;
                    if(x >= xPos && x < xPos + 18 && y >= yPos && y < yPos + 18) {
                        String key = tipPrefix + ".knowledge_" + i;
                        if(Language.getInstance().has(key)) renderTooltip(stack, new TranslatableComponent(key), x - xOff, y - yOff);
                        else renderTooltip(stack, HIDDEN_KNOWLEDGE, x - xOff, y - yOff);
                    }
                }
                stack.popPose();
            }
        }
        renderTooltip(stack, x, y);
    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int x, int pY) {
        if(hoveredSlot != null) {
            Component tip = new TranslatableComponent(tipPrefix + ".item_" + hoveredSlot.getContainerSlot());
            if(hoveredSlot.index >= 36 && (!menu.getCarried().isEmpty() || !hoveredSlot.hasItem())) {
                renderTooltip(pPoseStack, tip, x, pY);
            }
            else {
                ItemStack item = hoveredSlot.getItem();
                if(!item.isEmpty()) {
                    List<Component> components = getTooltipFromItem(item);
                    if(hoveredSlot.index >= 36) {
                        components.add(TextComponent.EMPTY);
                        components.add(tip);
                    }
                    renderTooltip(pPoseStack, components, item.getTooltipImage(), x, pY, null, item);
                }
            }
        }
    }

    private boolean isHoveringExperimentButton(double x, double y, Puzzle puzzle) {
        int buttonX = width/2 - BACKGROUND_WIDTH/2 + 4 + puzzle.ingredients().size() * 21;
        int buttonY = height/2 - BACKGROUND_HEIGHT/2 - HEIGHT/2 + 46;
        return x >= buttonX && x < buttonX + 18 && y >= buttonY && y < buttonY + 18;
    }

    private boolean canExperiment(Puzzle puzzle) {
        if(menu.inventory.isFull()) return puzzle.hasAllKnowledge(PlayerData.get(mc.player));
        else return false;
    }

    static void renderImage(PoseStack poseStack, EntryClient.Image image) {
        if(image != null) {
            int x = 100 + BACKGROUND_WIDTH/2;
            int y = BACKGROUND_HEIGHT/2 - image.height()/2;
            int margin = 8;
            int width = Math.min(image.width() + margin * 2, MAX_IMAGE_SIZE), height = Math.min(image.height() + margin * 2, MAX_IMAGE_SIZE);
            blit(poseStack, x, y, 111, 362, width, height, 512, 512);
            blit(poseStack, x, y + height - margin, 111, 512 - margin, width, margin, 512, 512);
            blit(poseStack, x + width - margin, y, 261 - margin, 362, margin, height, 512, 512);
            blit(poseStack, x + width - margin, y + height - margin, 261 - margin, 512 - margin, margin, margin, 512, 512);
            RenderSystem.setShaderTexture(0, image.texture());
            blit(poseStack, x + margin, y + margin, 0, 0, image.width(), image.height(), image.width(), image.height());
        }
    }

    @Override
    protected boolean hasClickedOutside(double pMouseX, double pMouseY, int pGuiLeft, int pGuiTop, int pMouseButton) {
        return false;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        Puzzle puzzle = cEntry.entry.get().puzzle;
        if(puzzle != null) {
            if(isHoveringExperimentButton(x, y, puzzle) && canExperiment(puzzle)) {
                NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.PUZZLE_EXPERIMENT_SERVER));
                return true;
            }
        }
        return super.mouseClicked(x, y, button);
    }
}
