package frostnox.nightfall.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.gui.OverlayNF;
import frostnox.nightfall.entity.PlayerAttribute;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.AttributeSelectionToServer;
import frostnox.nightfall.util.RenderUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.Optional;

public class AttributeSelectionScreen extends Screen {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/attribute_selection.png");
    public static final int WIDTH = 112;
    public static final int HEIGHT = 101;
    private static final int MARGIN = 8;
    private final Minecraft mc;
    protected final Component pauseText = new TranslatableComponent("menu.paused");

    public AttributeSelectionScreen() {
        super(NarratorChatListener.NO_TITLE);
        mc = Minecraft.getInstance();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partial) {
        renderBackground(stack);
        if(isPauseScreen() && mc.hasSingleplayerServer() && !mc.getSingleplayerServer().isPublished()) {
            drawCenteredString(stack, font, pauseText, width / 2, 40, 16777215);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = width / 2 - WIDTH / 2;
        int y = height / 2 - HEIGHT / 2;
        blit(stack, x, y, 0, 0, WIDTH, HEIGHT, 128, 128);
        y += MARGIN;
        LocalPlayer player = mc.player;
        RenderUtil.drawCenteredFont(stack, mc.font, RenderUtil.ATTRIBUTES_TEXT.plainCopy().withStyle(ChatFormatting.UNDERLINE), x + WIDTH/2, y, RenderUtil.COLOR_WHITE, false);
        y += 11;
        int freePoints = 0;
        IPlayerData capP = PlayerData.get(player);
        for(PlayerAttribute attribute : PlayerAttribute.values()) {
            freePoints -= capP.getAttributePoints(attribute);
        }
        //Arrows
        RenderSystem.setShaderTexture(0, TEXTURE);
        for(int i = 0; i < 6; i++) {
            if(i == 2) continue; //TODO:
            int arrowY = y - 1 + i * 11;
            int arrowX = x + WIDTH - MARGIN - 5 - 11;
            int upOff = 0, downOff = 0;
            int points = capP.getAttributePoints(PlayerAttribute.values()[i]);
            if(points >= 3 || freePoints <= 0) upOff = 10;
            if(points <= -3) downOff = 10;
            if(mouseY >= arrowY - 1 && mouseY < arrowY + 9) {
                if(mouseX >= arrowX - 7 - 1 && mouseX < arrowX - 7 + 6) {
                    if(upOff != 10) upOff = 5;
                }
                else if(mouseX >= arrowX - 1 && mouseX < arrowX + 6) {
                    if(downOff != 10) downOff = 5;
                }
            }
            blit(stack, arrowX - 7, arrowY, upOff, 101, 5, 9, 128, 128);
            blit(stack, arrowX, arrowY, 15 + downOff, 101, 5, 9, 128, 128);
        }
        //Icons
        RenderSystem.setShaderTexture(0, OverlayNF.TEXTURE);
        int iconYStart = y;
        for(int i = 0; i < 6; i++) {
            int iconY = y - 1 + i * 11;
            blit(stack, x + MARGIN, iconY, i * 9, 96, 9, 9, 256, 256);
        }
        //Values
        for(PlayerAttribute attribute : PlayerAttribute.values()) {
            mc.font.draw(stack, RenderUtil.getAttributeText(attribute), x + MARGIN + 10, y, RenderUtil.COLOR_WHITE);
            if(attribute == PlayerAttribute.WILLPOWER) RenderUtil.drawRightText(stack, mc.font, "?", x + WIDTH - MARGIN, y, RenderUtil.COLOR_WHITE, false, LightTexture.FULL_BRIGHT);
            else RenderUtil.drawRightText(stack, mc.font, "" + (10 + capP.getAttributePoints(attribute)), x + WIDTH - MARGIN, y, RenderUtil.COLOR_WHITE, false, LightTexture.FULL_BRIGHT);
            y += 11;
        }
        mc.font.draw(stack, RenderUtil.FREE_POINTS_TEXT.copy().append("" + freePoints), x + MARGIN + 1, y, RenderUtil.COLOR_WHITE);
        RenderSystem.setShaderTexture(0, TEXTURE);
        y--;
        int finX = x + WIDTH - MARGIN - 9;
        if(mouseX >= finX && mouseX < finX + 9 && mouseY >= y && mouseY < y + 9) {
            blit(stack, x + WIDTH - MARGIN - 9, y, 39, 101, 9, 9, 128, 128);
            renderTooltip(stack, new TranslatableComponent("screen.finalize"), mouseX, mouseY);
        }
        else blit(stack, x + WIDTH - MARGIN - 9, y, 30, 101, 9, 9, 128, 128);
        //Tooltips
        if(mouseX >= x + MARGIN && mouseX < x + WIDTH - MARGIN - 5 - 11 - 7 - 7) {
            for(int i = 0; i < 6; i++) {
                int iconY = iconYStart - 1 + i * 11;
                if(mouseY >= iconY - 1 && mouseY < iconY + 10) {
                    PlayerAttribute attribute = PlayerAttribute.values()[i];
                    List<Component> components = new ObjectArrayList<>(2);
                    String plus = capP.getAttributePoints(attribute) >= 0 ? "+" : "";
                    switch(attribute) {
                        case VITALITY -> {
                            components.add(new TranslatableComponent("screen." + attribute + ".info", 100 + 10 * capP.getAttributePoints(attribute)));
                        }
                        case ENDURANCE -> {
                            components.add(new TranslatableComponent("screen." + attribute + ".info", 100 + 10 * capP.getAttributePoints(attribute), plus + 5 * capP.getAttributePoints(attribute)));
                        }
                        case WILLPOWER -> {
                            components.add(new TranslatableComponent("screen." + attribute + ".info", 100 + 10 * capP.getAttributePoints(attribute)));
                        }
                        case STRENGTH -> {
                            components.add(new TranslatableComponent("screen." + attribute + ".info", plus + 5 * capP.getAttributePoints(attribute)));
                        }
                        case AGILITY -> {
                            components.add(new TranslatableComponent("screen." + attribute + ".info", plus + 3 * capP.getAttributePoints(attribute)));
                        }
                        case PERCEPTION -> {
                            components.add(new TranslatableComponent("screen." + attribute + ".info"));
                        }
                    }
                    renderTooltip(stack, components, Optional.empty(), mouseX, mouseY);
                    break;
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = width / 2 - WIDTH / 2;
        int y = height / 2 - HEIGHT / 2 + MARGIN + 11;
        int freePoints = 0;
        IPlayerData capP = PlayerData.get(mc.player);
        for(PlayerAttribute attribute : PlayerAttribute.values()) {
            freePoints -= capP.getAttributePoints(attribute);
        }
        int finishX = x + WIDTH - MARGIN - 9, finishY = y + 11 * 6 - 1;
        if(mouseX >= finishX && mouseX < finishX + 9 && mouseY >= finishY && mouseY < finishY + 9) {
            NetworkHandler.toServer(new AttributeSelectionToServer(capP.copyAttributePoints()));
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
            onClose();
            return true;
        }
        else for(int i = 0; i < 6; i++) {
            if(i == 2) continue; //TODO:
            int arrowY = y - 1 + i * 11;
            int arrowX = x + WIDTH - MARGIN - 5 - 11;
            PlayerAttribute attribute = PlayerAttribute.values()[i];
            int points = capP.getAttributePoints(attribute);
            if(mouseY >= arrowY - 1 && mouseY < arrowY + 9) {
                if(mouseX >= arrowX - 7 - 1 && mouseX < arrowX - 7 + 6) {
                    if(points < 3 && freePoints > 0) {
                        capP.setAttributePoints(attribute, points + 1);
                        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                        break;
                    }
                }
                else if(mouseX >= arrowX - 1 && mouseX < arrowX + 6) {
                    if(points > -3) {
                        capP.setAttributePoints(attribute, points - 1);
                        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                        break;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return mc.player == null || mc.player.tickCount > 100; //Let world load in SP
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
