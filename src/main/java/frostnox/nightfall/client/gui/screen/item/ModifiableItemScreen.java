package frostnox.nightfall.client.gui.screen.item;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.OverlayNF;
import frostnox.nightfall.item.client.IModifiable;
import frostnox.nightfall.item.client.IScreenCache;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;

public class ModifiableItemScreen extends Screen {
    public static final int DEFAULT_BACKGROUND = 0, BUILDING_BACKGROUND = 22;
    protected static final float[] ANIMATION = new float[] {1F, 1.2F, 1.45F, 1.4F, 1.25F, 1.1F, 1F};
    public final int radius, diameter;
    public final boolean mainHand;
    protected int slices;
    protected float sliceSize;
    protected final Minecraft mc;
    protected static int activeIndex = -1, page = 1, pageCount = 1;
    protected final IModifiable modifiableItem;
    protected List<ItemStack> items;
    protected int[] itemAnimateTimes = new int[0];
    protected boolean shouldAnimateSameItem = false;

    public ModifiableItemScreen(int radius, int slices, boolean mainHand, IModifiable modifiableItem, List<ItemStack> items) {
        super(NarratorChatListener.NO_TITLE);
        this.radius = radius;
        this.diameter = radius * 2;
        setSlices(slices);
        this.mainHand = mainHand;
        mc = Minecraft.getInstance();
        this.passEvents = true;
        this.modifiableItem = modifiableItem;
        setItems(items);
        updatePages(modifiableItem, items.size());
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
        if(itemAnimateTimes.length != items.size()) {
            this.itemAnimateTimes = new int[items.size()];
            Arrays.fill(itemAnimateTimes, -1);
        }
    }

    public boolean allowMovementInputs() {
        return true;
    }

    public void setSlices(int slices) {
        this.slices = Math.max(1, slices);
        sliceSize = MathUtil.toRadians(360F / this.slices);
    }

    protected Pair<Integer, Integer> getItemPosition(int index) {
        float angle = index * sliceSize;
        return new Pair<>(Math.round(-radius * Mth.sin(angle)), Math.round(radius * Mth.cos(angle)));
    }

    protected void drawHoverTooltip(PoseStack stack, ItemStack item, int mouseX, int mouseY, int x, int y) {
        if(!item.isEmpty() && mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16) {
            renderTooltip(stack, item, mouseX, mouseY);
        }
    }

    public static void initSelection(Minecraft mc, List<ItemStack> items, IModifiable modifiableItem, boolean mainHand) {
        if(mc.screen instanceof ModifiableItemScreen screen) screen.setItems(items);
        activeIndex = updateSelection(modifiableItem, items, mainHand);
        page = modifiableItem.getLastUsedPage();
    }

    public static int updateSelection(IScreenCache modifiableItem, List<ItemStack> items, boolean mainHand) {
        if(items.isEmpty()) {
            ClientEngine.get().setModifiableIndex(mainHand, ItemStack.EMPTY, 0);
            return -1;
        }
        int index = 0;
        //Try to set to last used item
        Item lastItem = modifiableItem.getLastUsedItem();
        if(lastItem != null) {
            for(int i = 0; i < items.size(); i++) {
                if(items.get(i).getItem() == lastItem) {
                    index = i;
                    break;
                }
            }
        }
        ItemStack item = items.get(index);
        ClientEngine.get().setModifiableIndex(mainHand, item, index);
        return index;
    }

    protected static void movePage(IScreenCache modifiableItem, int pages) {
        page = Mth.clamp(page + pages, 1, pageCount);
        modifiableItem.setLastUsedPage(page);
    }

    protected void updatePages(IScreenCache modifiableItem, int itemsSize) {
        pageCount = 1 + (itemsSize - 1) / slices;
        if(page > pageCount) {
            page = 1;
            modifiableItem.setLastUsedPage(page);
        }
    }

    protected void updateSlice(double mouseX, double mouseY) {
        double centeredX = mouseX - width / 2D;
        double centeredY = mouseY - height / 2D;
        int minRadius = radius - 16, maxRadius = radius + 16;
        double distSqr = centeredX * centeredX + centeredY * centeredY;
        if(distSqr > minRadius * minRadius && distSqr < maxRadius * maxRadius) {
            double angle = (Math.atan2(-centeredX, centeredY) + sliceSize / 2F + Math.PI + Math.PI * 2) % (Math.PI * 2);
            boolean noSlices = true;
            for(int i = (page - 1) * slices; i < items.size(); i++) {
                if(angle < sliceSize * (i % slices + 1)) {
                    modifiableItem.setLastUsedItem(items.get(i).getItem());
                    int lastIndex = activeIndex;
                    activeIndex = updateSelection(modifiableItem, items, mainHand);
                    if(itemAnimateTimes[i] == -1 && (shouldAnimateSameItem || activeIndex != lastIndex)) {
                        itemAnimateTimes[i] = 0;
                        shouldAnimateSameItem = false;
                    }
                    noSlices = false;
                    break;
                }
            }
            if(noSlices) shouldAnimateSameItem = true;
        }
        else shouldAnimateSameItem = true;
    }

    @Override
    public void tick() {
        for(int i = 0; i < itemAnimateTimes.length; i++) {
            if(itemAnimateTimes[i] >= 0) itemAnimateTimes[i]++;
            if(itemAnimateTimes[i] >= 7) itemAnimateTimes[i] = -1;
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
        if(mc.player.getItemInHand(mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND).getItem() != modifiableItem) {
            onClose();
            return;
        }
        super.render(poseStack, mouseX, mouseY, partial);
        int widthHalf = this.width / 2;
        int heightHalf = this.height / 2;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int startIndex = (page - 1) * slices;
        for(int i = startIndex; i < items.size() && (i == startIndex || i % slices != 0); i++) {
            ItemStack item = items.get(i);
            Pair<Integer, Integer> pos = getItemPosition(i);
            int xPos = widthHalf - pos.getA() - 8, yPos = heightHalf - pos.getB() - 8;
            RenderSystem.setShaderTexture(0, OverlayNF.TEXTURE);
            blit(poseStack, xPos - 3, yPos - 3, modifiableItem.getBackgroundUOffset(), 54, 22, 22, 256, 256);
            if(itemAnimateTimes[i] > 0) {
                PoseStack modelStack = RenderSystem.getModelViewStack();
                modelStack.pushPose();
                int animationTime = itemAnimateTimes[i];
                float scale = Mth.lerp(ClientEngine.get().getPartialTick(), ANIMATION[animationTime - 1], ANIMATION[animationTime]);
                int itemX = xPos + 8, itemY = yPos + 8;
                modelStack.translate(itemX, itemY, 0);
                modelStack.scale(scale, scale, 1F);
                modelStack.translate(-itemX, -itemY, 0);
                RenderSystem.applyModelViewMatrix();
                mc.getItemRenderer().renderAndDecorateItem(item, xPos, yPos);
                mc.getItemRenderer().renderGuiItemDecorations(mc.font, item, xPos, yPos);
                modelStack.popPose();
                RenderSystem.applyModelViewMatrix();
            }
            else {
                mc.getItemRenderer().renderAndDecorateItem(item, xPos, yPos);
                mc.getItemRenderer().renderGuiItemDecorations(mc.font, item, xPos, yPos);
            }
        }
        for(int i = startIndex; i < items.size() && (i == startIndex || i % slices != 0); i++) {
            ItemStack item = items.get(i);
            Pair<Integer, Integer> pos = getItemPosition(i);
            int xPos = widthHalf - pos.getA() - 8, yPos = heightHalf - pos.getB() - 8;
            if(mouseX >= xPos - 3 && mouseX < xPos + 16 + 3 && mouseY >= yPos - 3 && mouseY < yPos + 16 + 3) {
                renderTooltip(poseStack, item, mouseX, mouseY);
            }
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        updateSlice(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        movePage(modifiableItem, (int) scrollAmount);
        updateSlice(mouseX, mouseY);
        return super.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        if(!result) {
            for(KeyMapping key : mc.options.keyMappings) {
                if(key.getKey().getValue() == keyCode) {
                    if(key.getKeyConflictContext() != ClientEngine.get().movementKeyConflict) return true;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        if(allowMovementInputs()) {
            for(KeyMapping mapping : ClientEngine.get().movementConflictKeys) {
                InputConstants.Key key = mapping.getKey();
                if(key.getType() == InputConstants.Type.KEYSYM && key.getValue() != InputConstants.UNKNOWN.getValue()) {
                    mapping.setDown(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.getValue()));
                }
            }
        }
    }
}
