package frostnox.nightfall.client.gui.screen.encyclopedia;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.encyclopedia.EntryStage;
import frostnox.nightfall.registry.EntriesNF;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.registries.RegistryObject;

import java.nio.ByteBuffer;
import java.util.Random;

public class EncyclopediaTab extends GuiComponent {
    public static final int BACKGROUND_WIDTH = 366;
    public static final int BACKGROUND_HEIGHT = 246;
    public static final int IMAGE_WIDTH = BACKGROUND_WIDTH;
    public static final int IMAGE_HEIGHT = 314;
    public static final int GRID_SIZE = 25;
    public static final int GRID_SLOT_SIZE = 24;
    public static final int BUTTON_SIZE = 20;
    public static final int ICON_SIZE = 16;
    private static final int MIN_POS = -GRID_SIZE / 2;
    private static final int MAX_POS = GRID_SIZE / 2;
    private static final int OFFSET = 2 + (GRID_SIZE / 2 * GRID_SLOT_SIZE);
    private static final int GRID_MIN = MIN_POS * GRID_SLOT_SIZE;
    private static final int X_CENTER = BACKGROUND_WIDTH / 2 - OFFSET - GRID_SLOT_SIZE / 2;
    private static final int Y_CENTER = BACKGROUND_HEIGHT / 2 - OFFSET - GRID_SLOT_SIZE / 2;
    public final EncyclopediaCategory category;
    private final Minecraft mc;
    private double scrollX, scrollY;
    protected int tickCount = 0;
    public boolean hasNotifications;

    public EncyclopediaTab(EncyclopediaCategory category) {
        this.category = category;
        mc = Minecraft.getInstance();
    }

    public void render(PoseStack poseStack) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, category.background());
        blit(poseStack, 0, 0, 0.0F, 0.0F, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
        int xScroll = Mth.floor(this.scrollX);
        int yScroll = Mth.floor(this.scrollY);
        IPlayerData encyclopedia = PlayerData.get(mc.player);
        for(EntryClient cEntry : ClientEngine.get().getEntries(this.category)) {
            EntryStage stage = encyclopedia.getStage(cEntry.entry.getId());
            if(stage == EntryStage.HIDDEN) continue;
            int x = cEntry.x * GRID_SLOT_SIZE + xScroll + X_CENTER;
            int y = cEntry.y * GRID_SLOT_SIZE + yScroll + Y_CENTER;
            int sizeX = 0, sizeY = 0;
            int uOff = 0, vOff = 0;
            boolean renderEntry = x > GRID_MIN - GRID_SLOT_SIZE && x < GRID_MIN + BACKGROUND_WIDTH - 4 && y > GRID_MIN - GRID_SLOT_SIZE && y < GRID_MIN + BACKGROUND_HEIGHT - 4;
            //Adjust for edges
            if(x > GRID_MIN + BACKGROUND_WIDTH - GRID_SLOT_SIZE) sizeX = x - (GRID_MIN + BACKGROUND_WIDTH - GRID_SLOT_SIZE);
            else if(x < GRID_MIN - 4) {
                uOff = -(x - GRID_MIN + 4);
                sizeX = uOff;
            }
            if(y > GRID_MIN + BACKGROUND_HEIGHT - GRID_SLOT_SIZE) sizeY = y - (GRID_MIN + BACKGROUND_HEIGHT - GRID_SLOT_SIZE);
            else if(y < GRID_MIN - 4) {
                vOff = -(y - GRID_MIN + 4);
                sizeY = vOff;
            }
            int iconX = Mth.clamp(x, GRID_MIN - 6, GRID_MIN + BACKGROUND_WIDTH);
            int iconY = Mth.clamp(y, GRID_MIN - 6, GRID_MIN + BACKGROUND_HEIGHT);
            x = Mth.clamp(x, GRID_MIN - 4, GRID_MIN + BACKGROUND_WIDTH);
            y = Mth.clamp(y, GRID_MIN - 4, GRID_MIN + BACKGROUND_HEIGHT);
            Entry entry = EntriesNF.get(cEntry.entry.getId());
            //Connectors
            if(!cEntry.separated) {
                for(RegistryObject<? extends Entry> prerequisite : entry.parents) {
                    EntryClient pEntry = ClientEngine.get().getEntry(cEntry.category, prerequisite.getId());
                    if(pEntry == null) continue;
                    int xDist = cEntry.x - pEntry.x;
                    int yDist = cEntry.y - pEntry.y;
                    while(xDist != 0 || yDist != 0) {
                        int connectorX = (pEntry.x + xDist);
                        int connectorY = (pEntry.y + yDist);
                        int offX = connectorX * GRID_SLOT_SIZE + xScroll + X_CENTER;
                        int offY = connectorY * GRID_SLOT_SIZE + yScroll + Y_CENTER;
                        //Connector type
                        int u = 0;
                        if(connectorX == cEntry.x && connectorY == pEntry.y) {
                            if(cEntry.x > pEntry.x) {
                                if(cEntry.y < pEntry.y) u = GRID_SLOT_SIZE * 5;
                                else u = GRID_SLOT_SIZE * 3;
                            }
                            else {
                                if(cEntry.y < pEntry.y) u = GRID_SLOT_SIZE * 4;
                                else u = GRID_SLOT_SIZE * 2;
                            }
                        }
                        else if(connectorX == pEntry.x && connectorY == cEntry.y) {
                            if(cEntry.x < pEntry.x) {
                                if(cEntry.y > pEntry.y) u = GRID_SLOT_SIZE * 5;
                                else u = GRID_SLOT_SIZE * 3;
                            }
                            else {
                                if(cEntry.y > pEntry.y) u = GRID_SLOT_SIZE * 4;
                                else u = GRID_SLOT_SIZE * 2;
                            }
                        }
                        else if(connectorX != pEntry.x) u = GRID_SLOT_SIZE;

                        //Draw connector
                        if(connectorX != cEntry.x || connectorY != cEntry.y) {
                            RenderSystem.setShaderTexture(0, category.background());
                            blitInside(poseStack, offX, offY, u, 266);
                        }

                        if(xDist != 0 && (yDist == 0 || Math.abs(xDist) < Math.abs(yDist))) xDist += xDist < 0 ? 1 : -1;
                        else yDist += yDist < 0 ? 1 : -1;
                    }
                }
            }
            if(renderEntry) {
                //Button
                RenderSystem.setShaderTexture(0, category.background());
                blit(poseStack, OFFSET + x + 2, OFFSET + y + 2, uOff, 246 + vOff, BUTTON_SIZE - sizeX, BUTTON_SIZE - sizeY, IMAGE_WIDTH, IMAGE_HEIGHT);
                //Icon
                if(!cEntry.itemIcon.isEmpty()) {
                    int xi = x + 4 - uOff, yi = y + 4 - vOff;
                    if(xi > GRID_MIN - GRID_SLOT_SIZE + 6 && xi < GRID_MIN + BACKGROUND_WIDTH - 2 &&
                            yi > GRID_MIN - GRID_SLOT_SIZE + 6 && yi < GRID_MIN + BACKGROUND_HEIGHT - 2) {
                        RenderUtil.renderItem(poseStack, cEntry.itemIcon, OFFSET + xi, OFFSET + yi, getBlitOffset(), stage != EntryStage.COMPLETED);
                    }
                }
                else {
                    RenderSystem.setShaderTexture(0, cEntry.icon);
                    if(stage != EntryStage.COMPLETED) RenderSystem.setShaderColor(0F, 0F, 0F, 1F);
                    blit(poseStack, OFFSET + iconX + 4, OFFSET + iconY + 4, Math.max(0, uOff - 2), Math.max(0, vOff - 2), ICON_SIZE - Math.max(0, sizeX - 2), ICON_SIZE - Math.max(0, sizeY - 2), ICON_SIZE, ICON_SIZE);
                    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                }
                //Overlay
                if(stage == EntryStage.LOCKED) {
                    RenderUtil.renderGradient(poseStack, OFFSET + x + 2, OFFSET + y + 2, BUTTON_SIZE - sizeX, BUTTON_SIZE - sizeY, getBlitOffset(), RenderUtil.COLOR_SLOT_DARKEN);
                }
                else if(stage == EntryStage.PUZZLE) {
                    RenderSystem.setShaderColor(1F, 1F, 1F, Mth.lerp(ClientEngine.get().getPartialTick(),
                            Mth.sin((tickCount - 1) / 8F), Mth.sin(tickCount / 8F)));
                    RenderUtil.renderGradient(poseStack, OFFSET + x + 2, OFFSET + y + 2, BUTTON_SIZE - sizeX, BUTTON_SIZE - sizeY, getBlitOffset(), RenderUtil.COLOR_SLOT_HIGHLIGHT);
                    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                }
                //Notification
                if(encyclopedia.hasEntryNotification(cEntry.entry.getId())) {
                    RenderSystem.setShaderTexture(0, EncyclopediaScreen.TEXTURE);
                    blit(poseStack, OFFSET + x + 2, OFFSET + y + 2, uOff, 364 + vOff, BUTTON_SIZE - sizeX, BUTTON_SIZE - sizeY, 512, 512);
                }
            }
        }
        //Arrow connectors
        RenderSystem.setShaderTexture(0, category.background());
        for(EntryClient cEntry : ClientEngine.get().getEntries(this.category)) {
            if(cEntry.separated || !encyclopedia.hasEntry(cEntry.entry.getId())) continue;
            Entry entry = EntriesNF.get(cEntry.entry.getId());
            for(RegistryObject<? extends Entry> prerequisite : entry.parents) {
                EntryClient pEntry = ClientEngine.get().getEntry(cEntry.category, prerequisite.getId());
                if(pEntry == null) continue;
                int xDist = cEntry.x - pEntry.x;
                int yDist = cEntry.y - pEntry.y;
                int xMove = 0, yMove = 0;
                if (xDist != 0 && (yDist == 0 || Math.abs(xDist) < Math.abs(yDist))) xMove = xDist < 0 ? 1 : -1;
                else yMove = yDist < 0 ? 1 : -1;
                int x = (pEntry.x + xDist + xMove);
                int y = (pEntry.y + yDist + yMove);
                int entryX = x * GRID_SLOT_SIZE + xScroll + X_CENTER;
                int entryY = y * GRID_SLOT_SIZE + yScroll + Y_CENTER;

                //Arrow type
                int u;
                if (yMove == 1) u = 0;
                else if (xMove == 1) u = GRID_SLOT_SIZE;
                else if (xMove == -1) u = GRID_SLOT_SIZE * 2;
                else u = GRID_SLOT_SIZE * 3;
                blitInside(poseStack, entryX, entryY, u, 290);
            }
        }
    }

    protected void blitInside(PoseStack poseStack, int x, int y, int u, int v) {
        int sizeX = 0, sizeY = 0;
        int uOff = 0, vOff = 0;
        if(!(x < GRID_MIN - GRID_SLOT_SIZE - 2 || x >= GRID_MIN + BACKGROUND_WIDTH || y < GRID_MIN - GRID_SLOT_SIZE - 2 || y >= GRID_MIN + BACKGROUND_HEIGHT)) {
            if(x > GRID_MIN + BACKGROUND_WIDTH - GRID_SLOT_SIZE) sizeX = x - (GRID_MIN + BACKGROUND_WIDTH - GRID_SLOT_SIZE);
            else if(x < GRID_MIN - 2) {
                uOff = -(x - GRID_MIN + 2);
                sizeX = uOff;
            }
            if(y > GRID_MIN + BACKGROUND_HEIGHT - GRID_SLOT_SIZE) sizeY = y - (GRID_MIN + BACKGROUND_HEIGHT - GRID_SLOT_SIZE);
            else if(y < GRID_MIN - 2) {
                vOff = -(y - GRID_MIN + 2);
                sizeY = vOff;
            }
            x = Mth.clamp(x, GRID_MIN - 2, GRID_MIN + BACKGROUND_WIDTH);
            y = Mth.clamp(y, GRID_MIN - 2, GRID_MIN + BACKGROUND_HEIGHT);
            blit(poseStack, OFFSET + x, OFFSET + y, u + uOff, v + vOff, GRID_SLOT_SIZE - sizeX, GRID_SLOT_SIZE - sizeY, IMAGE_WIDTH, IMAGE_HEIGHT);
        }
    }

    public EntryClient hoveredEntry(double x, double y) {
        int xScroll = Mth.floor(this.scrollX);
        int yScroll = Mth.floor(this.scrollY);
        IPlayerData encyclopedia = PlayerData.get(mc.player);
        for(EntryClient entry : ClientEngine.get().getEntries(this.category)) {
            if(!encyclopedia.hasEntry(entry.entry.getId())) continue;
            int entryX = OFFSET + entry.x * GRID_SLOT_SIZE + xScroll + 17 + X_CENTER;
            int entryY = OFFSET + entry.y * GRID_SLOT_SIZE + yScroll + 17 + Y_CENTER;
            if(x >= entryX && x < entryX + BUTTON_SIZE && y >= entryY && y < entryY + BUTTON_SIZE) return entry;
        }
        return null;
    }

    public void scroll(double dragX, double dragY) {
        this.scrollX = Mth.clamp(this.scrollX + dragX, MIN_POS * GRID_SLOT_SIZE, MAX_POS * GRID_SLOT_SIZE);
        this.scrollY = Mth.clamp(this.scrollY + dragY, MIN_POS * GRID_SLOT_SIZE, MAX_POS * GRID_SLOT_SIZE);
    }
}
