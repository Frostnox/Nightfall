package frostnox.nightfall.client.gui.screen.encyclopedia;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.gui.screen.ScreenGuiComponent;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.registry.EntriesNF;
import frostnox.nightfall.registry.KnowledgeNF;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Deprecated
public class OldEntryComponent extends ScreenGuiComponent {
    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {

    }
    /*public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/encyclopedia/entry.png");
    public static final int BACKGROUND_WIDTH = 300;
    public static final int BACKGROUND_HEIGHT = 200;
    public static final int TEXT_WIDTH = 127;
    public static final int TEXT_HEIGHT = 180;
    private final static int ARROW_WIDTH = 16;
    private final static int ARROW_HEIGHT = 11;
    private final static int TEXT_OFFSET_WIDTH_0 = 16;
    private final static int TEXT_OFFSET_WIDTH_1 = TEXT_OFFSET_WIDTH_0 + TEXT_WIDTH + 14;
    private final static int TEXT_OFFSET_HEIGHT = 10;
    private final Minecraft mc;
    private final EncyclopediaScreen screen;
    private EntryClient cEntry;
    private Entry entry;
    private int page = 0;
    private int pages = 2;
    private int stage = 0;
    private List<EntryClient.Image> images;
    private List<FormattedCharSequence> text;

    public OldEntryComponent(EncyclopediaScreen screen) {
        mc = Minecraft.getInstance();
        this.screen = screen;
    }

    public void setEntry(EntryClient entry) {
        this.cEntry = entry;
        this.entry = EntriesNF.get(cEntry.entryId);
        int currentLine = 0, prevLine = -1, pageCount = 0;
        stage = PlayerData.get(mc.player).getStage(entry.entryId);
        images = entry.getImages(stage);
        text = mc.font.split(entry.getDescription(stage), TEXT_WIDTH);
        while(prevLine != currentLine) { //This won't count correctly if a page is entirely taken up by an image
            prevLine = currentLine;
            currentLine = renderPage(null, pageCount++, currentLine, true);
        }
        pages = pageCount - 1;
        page = 0;
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, float partial) {
        if(cEntry != null) {
            poseStack.pushPose();
            poseStack.translate(EncyclopediaTab.BACKGROUND_WIDTH/2 - BACKGROUND_WIDTH/2, EncyclopediaTab.BACKGROUND_HEIGHT/2 - BACKGROUND_HEIGHT/2, 0);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            //Background
            blit(poseStack, 0, 0, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 300, 256);
            //Left arrow
            if(page != 0) {
                int uOff = onLeftArrow(x, y) ? ARROW_WIDTH : 0;
                blit(poseStack, TEXT_OFFSET_WIDTH_0, TEXT_OFFSET_HEIGHT + TEXT_HEIGHT - ARROW_HEIGHT/2 - 2, uOff, 200 + ARROW_HEIGHT, ARROW_WIDTH, ARROW_HEIGHT, 300, 256);
            }
            //Right arrow
            if(pages - page > 2) {
                int uOff = onRightArrow(x, y) ? ARROW_WIDTH : 0;
                blit(poseStack, TEXT_OFFSET_WIDTH_1 + TEXT_WIDTH - ARROW_WIDTH/2 - 8, TEXT_OFFSET_HEIGHT + TEXT_HEIGHT - ARROW_HEIGHT/2 - 2, uOff, 200, ARROW_WIDTH, ARROW_HEIGHT, 300, 256);
            }
            //Title
            if(page == 0) {
                blit(poseStack, TEXT_OFFSET_WIDTH_0 + TEXT_WIDTH/2 - 50, TEXT_OFFSET_HEIGHT + 9, 0, 222, 100, 7, 300, 256);
                RenderUtil.drawCenteredFont(poseStack, mc.font, cEntry.getTitle().withStyle(ChatFormatting.BOLD), TEXT_OFFSET_WIDTH_0 + TEXT_WIDTH/2, TEXT_OFFSET_HEIGHT, 0x000000);
            }
            //Pages
            int currentLine = 0;
            int pageCount = 0;
            while(pageCount < page + 2) {
                currentLine = renderPage(poseStack, pageCount, currentLine, pageCount < page);
                pageCount++;
            }
            //Knowledge
            List<RegistryObject<? extends Knowledge>> knowledgeIds = entry.getStage(stage).getKnowledge();
            if(stage == 0 && !knowledgeIds.isEmpty()) {
                for(int i = 0; i < knowledgeIds.size(); i++) {
                    Knowledge knowledge = KnowledgeNF.get(knowledgeIds.get(i).getId());
                    if(knowledge.getIcon() != null) {
                        RenderSystem.setShaderTexture(0, knowledge.getIcon());
                        blit(poseStack, TEXT_OFFSET_WIDTH_1 + i * 18, TEXT_OFFSET_HEIGHT, 0, 0, 16, 16, 16, 16);
                    }
                    else if(!knowledge.getItemIcon()[0].isEmpty()) {
                        int xPos = EncyclopediaTab.BACKGROUND_WIDTH/2 - BACKGROUND_WIDTH/2 + TEXT_OFFSET_WIDTH_1 + i * 18;
                        int yPos = EncyclopediaTab.BACKGROUND_HEIGHT/2 - BACKGROUND_HEIGHT/2 + TEXT_OFFSET_HEIGHT;
                        mc.getItemRenderer().renderAndDecorateItem(knowledge.getItemIcon()[mc.player.tickCount / 20 % knowledge.getItemIcon().length], xPos, yPos);
                    }
                }
                for(int i = 0; i < knowledgeIds.size(); i++) {
                    Knowledge knowledge = KnowledgeNF.get(knowledgeIds.get(i).getId());
                    if(onKnowledge(x, y) == i) screen.renderTooltip(poseStack, knowledge.getTooltip(mc.player.tickCount), x, y);
                }
            }
            poseStack.popPose();
        }
    }

    private int onKnowledge(double x, double y) {
        if(stage != 0) return -1;
        if(x + 1 >= TEXT_OFFSET_WIDTH_1 && x + 1 < TEXT_OFFSET_WIDTH_1 + 8 * 18 && y + 1 >= TEXT_OFFSET_HEIGHT && y + 1 < TEXT_OFFSET_HEIGHT + 18) return Mth.floor(x + 1 - TEXT_OFFSET_WIDTH_1) / 18;
        return -1;
    }

    private boolean onLeftArrow(double x, double y) {
        int arrowX = TEXT_OFFSET_WIDTH_0;
        int arrowY = TEXT_OFFSET_HEIGHT + TEXT_HEIGHT - ARROW_HEIGHT/2 - 2;
        return x >= arrowX-2 && x < arrowX+2 + ARROW_WIDTH && y >= arrowY-2 && y < arrowY+2 + ARROW_HEIGHT;
    }

    private boolean onRightArrow(double x, double y) {
        int arrowX = TEXT_OFFSET_WIDTH_1 + TEXT_WIDTH - ARROW_WIDTH/2 - 8;
        int arrowY = TEXT_OFFSET_HEIGHT + TEXT_HEIGHT - ARROW_HEIGHT/2 - 2;
        return x >= arrowX-2 && x < arrowX+2 + ARROW_WIDTH && y >= arrowY-2 && y < arrowY+2 + ARROW_HEIGHT;
    }

    public int renderPage(PoseStack poseStack, int page, int currentLine, boolean simulate) {
        int lineCount = page == 0 ? 17 : 19;
        boolean[] lines = new boolean[lineCount];
        for(int i = 0; i < lineCount; i++) lines[i] = true;
        int widthOffset = page % 2 == 0 ? TEXT_OFFSET_WIDTH_0 : TEXT_OFFSET_WIDTH_1;
        int titleOffset = page == 0 ? 18 : 0;
        //Images
        if(!images.isEmpty()) {
            for(EntryClient.Image image : images) {
                if(image.page() == page) {
                    int yStart = image.y();
                    int yEnd = yStart + image.height();
                    for(int i = 0; i < lineCount; i++) {
                        int lineTop = titleOffset + i * 9;
                        int lineBottom = lineTop + 9;
                        if((lineTop >= yStart && lineTop < yEnd) || (lineBottom >= yStart && lineBottom < yEnd)) lines[i] = false; //Skip text where image is rendered
                    }
                    if(!simulate) {
                        RenderSystem.setShaderTexture(0, image.texture());
                        blit(poseStack, widthOffset + TEXT_WIDTH / 2 - image.width() / 2, TEXT_OFFSET_HEIGHT + image.y(), 0, 0, image.width(), image.height(), image.width(), image.height());
                    }
                }
            }
        }
        //Text
        for(int i = 0; i < lineCount; i++) {
            if(currentLine >= text.size()) break;
            if(!lines[i]) continue;
            if(!simulate) mc.font.draw(poseStack, text.get(currentLine), widthOffset, TEXT_OFFSET_HEIGHT + titleOffset + 9 * i, 0x000000);
            currentLine++;
        }
        return currentLine;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if(onRightArrow(x, y) && pages - page > 2) {
            page += 2;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
        else if(onLeftArrow(x, y) && page != 0) {
            page -= 2;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
        return false;
    }*/
}
