package frostnox.nightfall.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.item.client.IModifiable;
import frostnox.nightfall.registry.forge.AttributesNF;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;

import java.util.Random;

public abstract class OverlayNF extends GuiComponent {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/overlay.png");
    public static final ResourceLocation HOT_VIGNETTE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/hot_vignette.png");
    public static final ResourceLocation COLD_VIGNETTE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/cold_vignette.png");
    private static boolean registered = false;
    protected final static Random random = new Random();
    private static int lastHealth, displayHealth;
    private static long healthBlinkTime, lastHealthTime;

    public static void register() {
        Minecraft mc = Minecraft.getInstance();
        if(!registered) {
            IIngameOverlay healthOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.PLAYER_HEALTH_ELEMENT, "Health",
                    (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
                        if(!mc.options.hideGui && gui.shouldDrawSurvivalElements()) {
                            if(mc.player != null) {
                                gui.setupOverlayRenderState(true, false);
                                renderHealth(mStack);
                            }
                        }
                    });
            IIngameOverlay foodOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.FOOD_LEVEL_ELEMENT, "Food",
                    (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
                        if(!mc.options.hideGui && gui.shouldDrawSurvivalElements()) {
                            if(mc.player != null) {
                                gui.setupOverlayRenderState(true, false);
                                renderFood(mStack);
                            }
                        }
                    });
            IIngameOverlay staminaOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.FOOD_LEVEL_ELEMENT, "Stamina",
                    (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
                        if(!mc.options.hideGui && gui.shouldDrawSurvivalElements()) {
                            if(mc.player != null && !mc.player.isRidingJumpable()) {
                                gui.setupOverlayRenderState(true, false);
                                renderStamina(mStack);
                            }
                        }
                    });
            IIngameOverlay airOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.AIR_LEVEL_ELEMENT, "Air",
                    (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
                        if(!mc.options.hideGui && gui.shouldDrawSurvivalElements()) {
                            gui.setupOverlayRenderState(true, false);
                            renderAir(mStack);
                        }
                    });
            IIngameOverlay crosshairOverlay = OverlayRegistry.registerOverlayTop("Crosshair", (gui, poseStack, partialTick, screenWidth, screenHeight) -> {
                if(!mc.options.hideGui) {
                    gui.setupOverlayRenderState(true, false);
                    gui.setBlitOffset(-90);
                    renderCrosshair(poseStack);
                }
            });
            IIngameOverlay optionalItemsOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "OptionalItems",
                    (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
                        if(!mc.options.hideGui) {
                            if(mc.player != null && !mc.player.isSpectator()) {
                                gui.setupOverlayRenderState(true, false);
                                renderOptionalItems(mStack);
                            }
                        }
                    });
            OverlayRegistry.registerOverlayAbove(ForgeIngameGui.FROSTBITE_ELEMENT, "TemperatureVignette", (gui, mStack, partialTicks, screenWidth, screenHeight) -> {
                if(mc.player != null && !mc.player.isSpectator() && PlayerData.isPresent(mc.player)) {
                    gui.setupOverlayRenderState(true, false);
                    float temperature = PlayerData.get(mc.player).getTemperature();
                    if(temperature < 0.25) {
                        renderTextureOverlay(COLD_VIGNETTE, temperature < 0 ? 1 : (1F - (temperature / 0.25F)));
                    }
                    else if(temperature > 1) {
                        renderTextureOverlay(HOT_VIGNETTE, Math.min((temperature - 1) / 0.25F, 1));
                    }
                }
            });
            OverlayRegistry.enableOverlay(ForgeIngameGui.AIR_LEVEL_ELEMENT, false);
            OverlayRegistry.enableOverlay(ForgeIngameGui.ARMOR_LEVEL_ELEMENT, false);
            OverlayRegistry.enableOverlay(ForgeIngameGui.PLAYER_HEALTH_ELEMENT, false);
            OverlayRegistry.enableOverlay(ForgeIngameGui.FOOD_LEVEL_ELEMENT, false);
            OverlayRegistry.enableOverlay(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, false);
            OverlayRegistry.enableOverlay(ForgeIngameGui.JUMP_BAR_ELEMENT, false);
            OverlayRegistry.enableOverlay(ForgeIngameGui.MOUNT_HEALTH_ELEMENT, false);
            OverlayRegistry.enableOverlay(ForgeIngameGui.CROSSHAIR_ELEMENT, false);
        }
        registered = true;
    }

    protected static void draw(PoseStack pPoseStack, int x, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
        draw(pPoseStack, x, pY, 0, pUOffset, pVOffset, pUWidth, pVHeight);
    }

    protected static void draw(PoseStack pPoseStack, int x, int pY, int blitOffset, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
        blit(pPoseStack, x, pY, blitOffset, (float)pUOffset, (float)pVOffset, pUWidth, pVHeight, 256, 256);
    }

    protected static void renderAttributeBar(PoseStack stack, int posX, int posY, int vOff, int size, int maxSize, float value, float displayValue, boolean highlight) {
        int length = size * 9 + 1;
        //Background
        draw(stack, posX, posY, 0, vOff, length, 5);
        length -= 1;
        if(size != maxSize) draw(stack, posX + length, posY, maxSize * 9, vOff, 1, 5);
        //Fill
        draw(stack, posX, posY, 0, vOff + 5, Math.round(value * length), 5);
        //Flashing fill
        if(highlight && value < displayValue) draw(stack, posX, posY, 0, vOff + 5, Math.round(displayValue * length), 5);
    }

    protected static void renderAttributeBarDynamic(PoseStack stack, int posX, int posY, int vOff, int size, int value, int displayValue, boolean highlight) {
        //Draw first section
        draw(stack, posX, posY, 0, vOff, 10, 5);
        posX += 1;
        if(10 <= value || (highlight && 10 < displayValue)) draw(stack, posX, posY, 28, vOff, 9, 5);
        else {
            int u = Math.round(9 * (value / 10F));
            if(u > 0) {
                draw(stack, posX, posY, 28, vOff, u, 5);
            }
        }
        posX += 9;

        for(int i = 1; i < size; i++) {
            int v = i * 10;

            //Draw section background
            draw(stack, posX, posY, i == size - 1 ? 19 : 10, vOff, 9, 5);
            int uWidth = i == (size - 1) ? 8 : 9;

            //Draw section fill
            if(v + 10 <= value || (highlight && v + 10 < displayValue)) draw(stack, posX, posY, 28, vOff, uWidth, 5);
            else if(v < value) {
                int u = Math.round(uWidth * ((value - v) / 10F));
                if(u > 0) {
                    draw(stack, posX, posY, 28, vOff, u, 5);
                }
            }
            else if(highlight && v < displayValue) draw(stack, posX, posY, 28, vOff, Math.round(uWidth * ((displayValue - v) / 10F)), 5);
            posX += 9;
        }
    }

    protected static void renderHealth(PoseStack stack) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        Minecraft mc = Minecraft.getInstance();
        Player p = (Player) mc.getCameraEntity();
        if(p == null) return;
        int health = Mth.ceil(p.getHealth());
        int tickCount = ClientEngine.get().getTickCount();
        boolean highlight = healthBlinkTime > (long)tickCount && (healthBlinkTime - (long)tickCount) / 3L %2L == 1L;
        if(health < lastHealth && p.invulnerableTime > 0) {
            lastHealthTime = Util.getMillis();
            healthBlinkTime = (long)(tickCount + 20);
        }
        if(Util.getMillis() - lastHealthTime > 1000L) {
            lastHealth = health;
            displayHealth = health;
            lastHealthTime = Util.getMillis();
        }
        lastHealth = health;
        int healthLast = displayHealth;

        AttributeInstance attrMaxHealth = p.getAttribute(Attributes.MAX_HEALTH);
        float healthMax = Math.max((float)attrMaxHealth.getValue(), Math.max(healthLast, health));
        int vitality = Mth.floor(AttributesNF.getVitality(p));
        int absorb = Mth.ceil(p.getAbsorptionAmount());

        random.setSeed(tickCount * 312871L);

        int posX = mc.getWindow().getGuiScaledWidth() / 2 - 89;
        int posY = mc.getWindow().getGuiScaledHeight() - 29;

        renderAttributeBar(stack, posX, posY, 0, vitality, 20, p.getHealth() / healthMax, displayHealth / healthMax, highlight);
        draw(stack, posX - 3, posY - 1, 0, 40, 7, 7);

        RenderSystem.disableBlend();
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
    }

    protected static void renderFood(PoseStack stack) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if(p == null) return;
        FoodData stats = mc.player.getFoodData();
        int food = stats.getFoodLevel() * 5; //Scale up to 100 for rendering purposes
        int posX = mc.getWindow().getGuiScaledWidth() / 2 - 89;
        int posY = mc.getWindow().getGuiScaledHeight() - 43;

        renderAttributeBar(stack, posX, posY, 20, 10, 10, food / 100F, food, false);
        draw(stack, posX - 3, posY - 1, 14, 40, 7, 7);

        RenderSystem.disableBlend();
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
    }

    protected static void renderStamina(PoseStack stack) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if(p == null) return;
        double lastStamina = ClientEngine.get().getLastStamina();
        double stamina = p.isAlive() ? PlayerData.get(p).getStamina() : lastStamina;
        if(Math.abs(stamina - lastStamina) > 0.001D) {
            stamina = Mth.lerp(ClientEngine.get().getPartialTick(), lastStamina, stamina);
        }
        int endurance = Math.round((float) p.getAttribute(AttributesNF.ENDURANCE.get()).getValue());
        double maxStamina = AttributesNF.getMaxStamina(p);
        int posX = mc.getWindow().getGuiScaledWidth() / 2 - 89;
        int posY = mc.getWindow().getGuiScaledHeight() - 36;

        renderAttributeBar(stack, posX, posY, 10, endurance, 20, (float) (stamina / maxStamina), (float) stamina, false);
        draw(stack, posX - 3, posY - 1, 7, 40, 7, 7);

        RenderSystem.disableBlend();
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
    }

    protected static void renderAir(PoseStack stack) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if(p == null || !p.isAlive()) return;
        int posX = mc.getWindow().getGuiScaledWidth() / 2 - 89;
        int posY = mc.getWindow().getGuiScaledHeight() - 50;
        int air = p.getAirSupply();

        if(air < 300) {
            renderAttributeBar(stack, posX, posY, 30, 10, 10, air / 300F, air, false);
            draw(stack, posX - 3, posY - 1, 21, 40, 7, 7);
        }

        RenderSystem.disableBlend();
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
    }

    protected static void renderTextureOverlay(ResourceLocation texture, float alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.setShaderTexture(0, texture);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        bufferbuilder.vertex(0.0D, height, -90.0D).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(width, height, -90.0D).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(width, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    //Simpler crosshair rendering, removes the debug axes & attack cooldown logic
    protected static void renderCrosshair(PoseStack stack) {
        Minecraft mc = Minecraft.getInstance();
        Options options = mc.options;
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        if(options.getCameraType().isFirstPerson()) {
            if(mc.gameMode.getPlayerMode() != GameType.SPECTATOR || canRenderCrosshairForSpectator(mc.hitResult)) {
                RenderSystem.enableBlend();
                RenderSystem.setShaderTexture(0, TEXTURE);
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                draw(stack, (width - 7) / 2, (height - 7) / 2, 0, 47, 7, 7);
                RenderSystem.disableBlend();
            }
        }
    }

    protected static boolean canRenderCrosshairForSpectator(HitResult pRayTrace) {
        if(pRayTrace == null) return false;
        else if(pRayTrace.getType() == HitResult.Type.ENTITY) return ((EntityHitResult)pRayTrace).getEntity() instanceof MenuProvider;
        else if(pRayTrace.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult)pRayTrace).getBlockPos();
            Level level = Minecraft.getInstance().level;
            return level.getBlockState(blockpos).getMenuProvider(level, blockpos) != null;
        }
        else return false;
    }

    //Renders optional items present in ClientEngine (used for items like building materials or bows)
    protected static void renderOptionalItems(PoseStack stack) {
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth() / 2;
        int height = mc.getWindow().getGuiScaledHeight();
        ItemStack mainItem = ClientEngine.get().getOptionalMainItem(), offItem = ClientEngine.get().getOptionalOffItem();
        boolean mainPresent = !mainItem.isEmpty(), offPresent = !offItem.isEmpty();
        if(mainPresent || offPresent) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            if(mainPresent) draw(stack, width + 91 + 7, height - 22,
                    mc.player.getMainHandItem().getItem() instanceof IModifiable modifiable ? modifiable.getBackgroundUOffset() : 0, 54, 22, 22);
            if(offPresent) draw(stack, width - 91 - 48, height - 22,
                    mc.player.getOffhandItem().getItem() instanceof IModifiable modifiable ? modifiable.getBackgroundUOffset() : 0, 54, 20, 22);
        }
        if(mainPresent) {
            mc.getItemRenderer().renderAndDecorateItem(mainItem, width + 91 + 10, height - 16 - 3);
            mc.getItemRenderer().renderGuiItemDecorations(mc.font, mainItem, width + 91 + 10, height - 16 - 3);
        }
        if(offPresent) {
            mc.getItemRenderer().renderAndDecorateItem(offItem, width - 91 - 45, height - 16 - 3);
            mc.getItemRenderer().renderGuiItemDecorations(mc.font, offItem, width - 91 - 45, height - 16 - 3);
        }
    }
}