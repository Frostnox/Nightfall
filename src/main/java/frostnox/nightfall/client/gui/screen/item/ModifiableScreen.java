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
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;

public abstract class ModifiableScreen<T> extends Screen {
    public static final int DEFAULT_BACKGROUND = 0, BUILDING_BACKGROUND = 22;
    protected static final float[] ANIMATION = new float[] {1F, 1.2F, 1.45F, 1.4F, 1.25F, 1.1F, 1F};
    public final int radius, diameter;
    public final boolean mainHand;
    protected int slices;
    protected float sliceSize;
    protected final Minecraft mc;
    protected static int activeIndex = -1, page = 1, pageCount = 1;
    protected final IModifiable modifiableItem;
    protected List<T> objects;
    protected int[] animateTimes = new int[0];
    protected boolean shouldAnimateSameObject = false;

    public ModifiableScreen(int radius, int slices, boolean mainHand, IModifiable modifiableItem, List<T> objects) {
        super(NarratorChatListener.NO_TITLE);
        this.radius = radius;
        this.diameter = radius * 2;
        setSlices(slices);
        this.mainHand = mainHand;
        mc = Minecraft.getInstance();
        this.passEvents = true;
        this.modifiableItem = modifiableItem;
        setObjects(objects);
        updatePages(modifiableItem, objects.size());
    }

    public static void initSelection(Minecraft mc, List<?> objects, IModifiable modifiableItem, boolean mainHand) {
        if(mc.screen instanceof ModifiableScreen screen) {
            if(screen.mainHand != mainHand) return;
            screen.setObjects(objects);
        }
        activeIndex = updateSelection(modifiableItem, objects, mainHand);
        page = modifiableItem.getLastUsedPage();
    }

    public static int updateSelection(IScreenCache modifiableItem, List<?> objects, boolean mainHand) {
        if(objects.isEmpty()) {
            ClientEngine.get().setModifiableIndex(mainHand, null, -1);
            return -1;
        }
        int index = 0;
        //Try to set to last used item
        Object lastObject = modifiableItem.getLastUsedObject();
        if(lastObject != null) {
            for(int i = 0; i < objects.size(); i++) {
                if(objects.get(i) == lastObject || (objects.get(i) instanceof ItemStack item && item.getItem() == lastObject)) {
                    index = i;
                    break;
                }
            }
        }
        ClientEngine.get().setModifiableIndex(mainHand, objects.get(index), index);
        return index;
    }

    protected abstract void renderObject(T object, PoseStack poseStack, int xPos, int yPos);

    protected abstract void renderObjectTooltip(T object, PoseStack poseStack, int mouseX, int mouseY);

    protected Object getObjectForLastUse(int i) {
        return objects.get(i);
    }

    public void setObjects(List<T> objects) {
        this.objects = objects;
        if(animateTimes.length != objects.size()) {
            this.animateTimes = new int[objects.size()];
            Arrays.fill(animateTimes, -1);
        }
    }

    public boolean allowMovementInputs() {
        return true;
    }

    public void setSlices(int slices) {
        this.slices = Math.max(1, slices);
        sliceSize = MathUtil.toRadians(360F / this.slices);
    }

    protected Pair<Integer, Integer> getPosition(int index) {
        float angle = index * sliceSize;
        return new Pair<>(Math.round(-radius * Mth.sin(angle)), Math.round(radius * Mth.cos(angle)));
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
            for(int i = (page - 1) * slices; i < objects.size(); i++) {
                if(angle < sliceSize * (i % slices + 1)) {
                    modifiableItem.setLastUsedObject(getObjectForLastUse(i));
                    int lastIndex = activeIndex;
                    activeIndex = updateSelection(modifiableItem, objects, mainHand);
                    if(animateTimes[i] == -1 && (shouldAnimateSameObject || activeIndex != lastIndex)) {
                        animateTimes[i] = 0;
                        shouldAnimateSameObject = false;
                    }
                    noSlices = false;
                    break;
                }
            }
            if(noSlices) shouldAnimateSameObject = true;
        }
        else shouldAnimateSameObject = true;
    }

    @Override
    public void tick() {
        for(int i = 0; i < animateTimes.length; i++) {
            if(animateTimes[i] >= 0) animateTimes[i]++;
            if(animateTimes[i] >= 7) animateTimes[i] = -1;
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
        for(int i = startIndex; i < objects.size() && (i == startIndex || i % slices != 0); i++) {
            Pair<Integer, Integer> pos = getPosition(i);
            int xPos = widthHalf - pos.getA() - 8, yPos = heightHalf - pos.getB() - 8;
            RenderSystem.setShaderTexture(0, OverlayNF.TEXTURE);
            blit(poseStack, xPos - 3, yPos - 3, modifiableItem.getBackgroundUOffset(), 54, 22, 22, 256, 256);
            if(animateTimes[i] > 0) {
                PoseStack modelStack = RenderSystem.getModelViewStack();
                modelStack.pushPose();
                int animationTime = animateTimes[i];
                float scale = Mth.lerp(ClientEngine.get().getPartialTick(), ANIMATION[animationTime - 1], ANIMATION[animationTime]);
                int itemX = xPos + 8, itemY = yPos + 8;
                modelStack.translate(itemX, itemY, 0);
                modelStack.scale(scale, scale, 1F);
                modelStack.translate(-itemX, -itemY, 0);
                RenderSystem.applyModelViewMatrix();
                renderObject(objects.get(i), poseStack, xPos, yPos);
                modelStack.popPose();
                RenderSystem.applyModelViewMatrix();
            }
            else renderObject(objects.get(i), poseStack, xPos, yPos);
        }
        for(int i = startIndex; i < objects.size() && (i == startIndex || i % slices != 0); i++) {
            Pair<Integer, Integer> pos = getPosition(i);
            int xPos = widthHalf - pos.getA() - 8, yPos = heightHalf - pos.getB() - 8;
            if(mouseX >= xPos - 3 && mouseX < xPos + 16 + 3 && mouseY >= yPos - 3 && mouseY < yPos + 16 + 3) {
                renderObjectTooltip(objects.get(i), poseStack, mouseX, mouseY);
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
