package frostnox.nightfall.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.render.RenderTypeNF;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.recipe.IEncyclopediaRecipe;
import frostnox.nightfall.data.recipe.IRenderableRecipe;
import frostnox.nightfall.encyclopedia.knowledge.IItemKnowledge;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.entity.PlayerAttribute;
import frostnox.nightfall.registry.KnowledgeNF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class RenderUtil {
    public static final int COLOR_WHITE = 0xffffff;
    public static final int COLOR_BLACK = 0x34312e;
    public static final int COLOR_LEATHER = 0xcb814f;
    public static final int COLOR_LINEN = 0xffdfaf;
    public static final int COLOR_UNDEAD_CLOTH = 0xb4c1bd;
    public static final int COLOR_SLOT_HIGHLIGHT = 0x7fffffff;
    public static final int COLOR_SLOT_DARKEN = 0x7f000000;
    public static final TranslatableComponent ATTRIBUTES_TEXT = new TranslatableComponent("screen.attributes");
    public static final TranslatableComponent RESISTANCES_TEXT = new TranslatableComponent("screen.resistances");
    public static final TranslatableComponent EFFECT_DEFENSE_TEXT = new TranslatableComponent("screen.defense.effect");
    public static final TranslatableComponent ARMOR_DEFENSE_TEXT = new TranslatableComponent("screen.defense.armor");
    public static final TranslatableComponent FREE_POINTS_TEXT = new TranslatableComponent("screen.free_points");
    public static final List<TranslatableComponent> FOOD_GROUPS_TEXT = TagsNF.FOOD_GROUPS.stream().map((tag) -> new TranslatableComponent(
            tag.location().toString().replace(":", ".").replace("/", "."))).toList();

    public static TranslatableComponent getAttributeText(PlayerAttribute attribute) {
        return new TranslatableComponent("screen." + attribute.toString());
    }

    public static TranslatableComponent getDamageTypeText(DamageType type) {
        return new TranslatableComponent("screen." + type.toString());
    }
    
    public static int getItemBarColor(ItemStack stack) {
        float stackMaxDamage = stack.getItem().getMaxDamage(stack);
        float hue = Math.max(0.0F, (stackMaxDamage - (float)stack.getDamageValue()) / stackMaxDamage);
        return Mth.hsvToRgb(hue / 3.0F - 0.08F, 0.8F, 0.8F);
    }

    public static Color getFireColor(float temperature) {
        if(temperature < 500) return new Color(128, 16, 1, 255);
        else if(temperature < 1000) {
            float p = (temperature - 500F) / 500F;
            return new Color(128 + (int) (122 * p), 16 + (int) (60 * p), 1, 255);
        }
        else if(temperature < 1200) {
            float p = (temperature - 1000F) / 200F;
            return new Color(250, 76 + (int) (55 * p), 1, 255);
        }
        else if(temperature < 1350) {
            float p = (temperature - 1200F) / 150F;
            return new Color(250 + (int) (5 * p), 131 + (int) (85 * p), 1 + (int) (122 * p), 255);
        }
        else if(temperature < 1550) {
            float p = (temperature - 1350F) / 200F;
            return new Color(255, 216 + (int) (34 * p), 123 + (int) (117 * p), 255);
        }
        else if(temperature < 1700) {
            float p = (temperature - 1500F) / 200F;
            return new Color(255 - (int) (69 * p), 250 - (int) (21 * p), 240 + (int) (3 * p), 255);
        }
        else return new Color(186, 229, 243, 255);
    }

    public static Color getHeatedMetalColor(float temperature, int metalColor) {
        int r, g, b;
        TieredHeat heat = TieredHeat.fromTemp(temperature);
        if(temperature >= TieredHeat.ORANGE.getBaseTemp()) {
            TieredHeat fromHeat = TieredHeat.fromTier(heat.getTier() + 1);
            Color toColor = heat.color;
            Color fromColor = fromHeat.color;
            float progress = 1F - (temperature - heat.getBaseTemp()) / (fromHeat.getBaseTemp() - heat.getBaseTemp());
            r = (int) Mth.lerp(progress, fromColor.getRed(), toColor.getRed());
            g = (int) Mth.lerp(progress, fromColor.getGreen(), toColor.getGreen());
            b = (int) Mth.lerp(progress, fromColor.getBlue(), toColor.getBlue());
        }
        else if(temperature >= 500) {
            TieredHeat fromHeat = TieredHeat.fromTier(heat.getTier() + 1);
            Color fromColor = fromHeat.color;
            float progress = 1F - (temperature - 500) / (fromHeat.getBaseTemp() - 500);
            r = (int) Mth.lerp(progress, fromColor.getRed(), heat.color.getRed());
            g = (int) Mth.lerp(progress, fromColor.getGreen(), heat.color.getGreen());
            b = (int) Mth.lerp(progress, fromColor.getBlue(), heat.color.getBlue());
        }
        else {
            r = (metalColor & 0x00FF0000) >> 16;
            g = (metalColor & 0x0000FF00) >> 8;
            b = metalColor & 0x000000FF;
            if(heat == TieredHeat.RED) {
                float progress = 1F - (temperature - 100) / (500 - 100);
                r = (int) Mth.lerp(progress, heat.color.getRed(), r);
                g = (int) Mth.lerp(progress, heat.color.getGreen(), g);
                b = (int) Mth.lerp(progress, heat.color.getBlue(), b);
            }
        }
        return new Color(r, g, b);
    }

    public static void renderGradient(PoseStack pPoseStack, int x, int pY, int xSize, int ySize, int pBlitOffset, int slotColor) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        GuiComponent.fillGradient(pPoseStack, x, pY, x + xSize, pY + ySize, slotColor, slotColor, pBlitOffset);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    public static void renderFluidScreen(PoseStack poseStack, FluidStack fluidStack, AbstractContainerScreen<?> screen, int x, int y, double mouseX, double mouseY) {
        if(fluidStack.isEmpty()) return;
        int color = fluidStack.getFluid().getAttributes().getColor();
        if(color != -1) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(((color & 0x00FF0000) >> 16) / 255F, ((color & 0x0000FF00) >> 8) / 255F, (color & 0x000000FF) / 255F, ((color & 0xFF000000) >> 24) / 255F);
        }
        else RenderSystem.setShader(GameRenderer::getPositionTexShader);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStack.getFluid().getAttributes().getStillTexture());
        RenderSystem.setShaderTexture(0, sprite.atlas().location());
        int width = ClientEngine.get().atlasWidth;
        int height = ClientEngine.get().atlasHeight;
        AbstractContainerScreen.blit(poseStack, x, y, sprite.getU0() * width, sprite.getV0() * height, 16, 16, width, height);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if(mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
            screen.renderTooltip(poseStack, new TextComponent(fluidStack.getAmount() + " ").append(fluidStack.getDisplayName()), Mth.floor(mouseX), Mth.floor(mouseY));
        }
    }

    public static void drawCenteredFont(PoseStack poseStack, Font font, Component text, int x, int y, int color, boolean shadow) {
        font.draw(poseStack, text, x - Math.round(font.width(text.getVisualOrderText()) / 2F), y, color);
    }

    public static void drawCenteredText(PoseStack poseStack, Font font, String text, int x, int y, int color, boolean dropShadow, int light) {
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(text, x - font.width(text) / 2F, y, color, dropShadow, poseStack.last().pose(), buffer, false, 0, light);
        buffer.endBatch();
    }

    public static void drawRightFont(PoseStack poseStack, Font font, Component text, int x, int y, int color) {
        font.draw(poseStack, text, x - font.width(text.getVisualOrderText()), y, color);
    }

    public static void drawRightText(PoseStack poseStack, Font font, String text, int x, int y, int color, boolean dropShadow, int light) {
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(text, x - font.width(text), y, color, dropShadow, poseStack.last().pose(), buffer, false, 0, light);
        buffer.endBatch();
    }

    public static void drawCubeOutlineWorld(PoseStack stack, MultiBufferSource.BufferSource buffer, Color color, VoxelShape cube) {
        VertexConsumer builder = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        cube.forAllEdges((xMin, yMin, zMin, xMax, yMax, zMax) -> {
            float xSize = (float)(xMax - xMin);
            float ySize = (float)(yMax - yMin);
            float zSize = (float)(zMax - zMin);
            float dist = Mth.sqrt(xSize * xSize + ySize * ySize + zSize * zSize);
            xSize /= dist;
            ySize /= dist;
            zSize /= dist;
            builder.vertex(matrix, (float)(xMin), (float)(yMin), (float)(zMin)).color(color.getRed(), color.getBlue(), color.getGreen(), color.getAlpha()).normal(normal, xSize, ySize, zSize).endVertex();
            builder.vertex(matrix, (float)(xMax), (float)(yMax), (float)(zMax)).color(color.getRed(), color.getBlue(), color.getGreen(), color.getAlpha()).normal(normal, xSize, ySize, zSize).endVertex();
        });
        buffer.endBatch(RenderType.lines());
    }

    //This could be further optimized to not draw lines that aren't visible
    public static void drawMicroGridCubeOutline(PoseStack stack, MultiBufferSource buffer, Color color, VoxelShape cube, boolean skipHitBox) {
        VertexConsumer builder = buffer.getBuffer(RenderTypeNF.LINES_DEPTH);
        Matrix4f matrix = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        cube.forAllEdges((xMin, yMin, zMin, xMax, yMax, zMax) -> {
            float xSize = (float)(xMax - xMin);
            float ySize = (float)(yMax - yMin);
            float zSize = (float)(zMax - zMin);
            float dist = Mth.sqrt(xSize * xSize + ySize * ySize + zSize * zSize);
            xSize /= dist;
            ySize /= dist;
            zSize /= dist;
            if(skipHitBox || !ClientEngine.get().microHitBox.contains(xMin, yMin, zMin) || !ClientEngine.get().microHitBox.contains(xMax, yMax, zMax)) {
                builder.vertex(matrix, (float) (xMin), (float) (yMin), (float) (zMin)).color(color.getRed(), color.getBlue(), color.getGreen(), color.getAlpha()).normal(normal, xSize, ySize, zSize).endVertex();
                builder.vertex(matrix, (float) (xMax), (float) (yMax), (float) (zMax)).color(color.getRed(), color.getBlue(), color.getGreen(), color.getAlpha()).normal(normal, xSize, ySize, zSize).endVertex();
            }
        });
    }

    public static void drawMicroGridCube(PoseStack stack, MultiBufferSource buffer, Color color, int combinedLight, float size, boolean[][][] grid, int x, int y, int z, TextureAtlasSprite sprite) {
        VertexConsumer builder = buffer.getBuffer(RenderType.entitySolid(sprite.atlas().location()));
        Vec2 UV = new Vec2(sprite.getU0(), sprite.getV0());
        Vec3 translation = new Vec3(x/16D, y/16D, z/16D);
        int maxX = grid.length - 1;
        int maxY = grid[0].length - 1;
        int maxZ = grid[0][0].length - 1;
        Matrix4f matrix = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        if(z == 0 || !grid[x][y][z-1]) drawFace(Direction.NORTH, matrix, normal, builder, color, translation.add(size/2D, size/2D, 0), size, size, UV, 1F/ClientEngine.get().atlasWidth, 1F/ClientEngine.get().atlasHeight, combinedLight);
        if(z == maxZ || !grid[x][y][z+1]) drawFace(Direction.SOUTH, matrix, normal, builder, color, translation.add(size/2D, size/2D, size), size, size, UV, 1F/ClientEngine.get().atlasWidth, 1F/ClientEngine.get().atlasHeight, combinedLight);
        if(x == 0 || !grid[x-1][y][z]) drawFace(Direction.WEST, matrix, normal, builder, color, translation.add(0, size/2D, size/2D), size, size, UV, 1F/ClientEngine.get().atlasWidth, 1F/ClientEngine.get().atlasHeight, combinedLight);
        if(x == maxX || !grid[x+1][y][z]) drawFace(Direction.EAST, matrix, normal, builder, color, translation.add(size, size/2D, size/2D), size, size, UV, 1F/ClientEngine.get().atlasWidth, 1F/ClientEngine.get().atlasHeight, combinedLight);
        if(y == maxY || !grid[x][y+1][z]) drawFace(Direction.UP, matrix, normal, builder, color, translation.add(size/2D, size, size/2D), size, size, UV, 1F/ClientEngine.get().atlasWidth, 1F/ClientEngine.get().atlasHeight, combinedLight);
        if(y != 0 && !grid[x][y-1][z]) drawFace(Direction.DOWN, matrix, normal, builder, color, translation.add(size/2D, 0, size/2D), size, size, UV, 1F/ClientEngine.get().atlasWidth, 1F/ClientEngine.get().atlasHeight, combinedLight);
    }

    public static void drawBox(PoseStack stack, MultiBufferSource buffer, Color color, int combinedLight, float width, float height, float depth, double x, double y, double z, TextureAtlasSprite sprite, int uOff, int vOff) {
        VertexConsumer builder = buffer.getBuffer(RenderType.entitySolid(sprite.atlas().location()));
        Vec2 UV = new Vec2(sprite.getU0() + (float) uOff/ClientEngine.get().atlasWidth, sprite.getV0() + (float) vOff/ClientEngine.get().atlasHeight);
        Vec3 translation = new Vec3(x, y, z);
        Matrix4f matrix = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        float iWidth = Math.round(width * 16), iHeight = Math.round(height * 16), iDepth = Math.round(depth * 16);
        drawFace(Direction.NORTH, matrix, normal, builder, color, translation.add(width/2D, height/2D, 0), width, height, UV, iWidth/ClientEngine.get().atlasWidth, iHeight/ClientEngine.get().atlasHeight, combinedLight);
        drawFace(Direction.SOUTH, matrix, normal, builder, color, translation.add(width/2D, height/2D, depth), width, height, UV, iWidth/ClientEngine.get().atlasWidth, iHeight/ClientEngine.get().atlasHeight, combinedLight);
        drawFace(Direction.WEST, matrix, normal, builder, color, translation.add(0, height/2D, depth/2D), depth, height, UV, iDepth/ClientEngine.get().atlasWidth, iHeight/ClientEngine.get().atlasHeight, combinedLight);
        drawFace(Direction.EAST, matrix, normal, builder, color, translation.add(width, height/2D, depth/2D), depth, height, UV, iDepth/ClientEngine.get().atlasWidth, iHeight/ClientEngine.get().atlasHeight, combinedLight);
        drawFace(Direction.UP, matrix, normal, builder, color, translation.add(width/2D, height, depth/2D), width, depth, UV, iWidth/ClientEngine.get().atlasWidth, iDepth/ClientEngine.get().atlasHeight, combinedLight);
        drawFace(Direction.DOWN, matrix, normal, builder, color, translation.add(width/2D, 0, depth/2D), width, depth, UV, iWidth/ClientEngine.get().atlasWidth, iDepth/ClientEngine.get().atlasHeight, combinedLight);
    }

    public static void drawFace(Direction face, Matrix4f matrix, Matrix3f normal, VertexConsumer builder, Color color, Vec3 center, float width, float height, Vec2 UV, float UVwidth, float UVheight, int combinedLight) {
        //Adjusted directions
        Vector3f rightDirection = new Vector3f(0, 0, 1);
        Vector3f topDirection = new Vector3f(0, 1, 0);
        switch(face) {
            case NORTH -> { // bottom left is east
                rightDirection = new Vector3f(-1, 0, 0);  // or alternatively Vector3f.XN
                topDirection = new Vector3f(0, 1, 0);  // or alternatively Vector3f.YP
            }
            case SOUTH -> {  // bottom left is west
                rightDirection = new Vector3f(1, 0, 0);
                topDirection = new Vector3f(0, 1, 0);
            }
            case EAST -> {  // bottom left is south
                rightDirection = new Vector3f(0, 0, -1);
                topDirection = new Vector3f(0, 1, 0);
            }
            case WEST -> { // bottom left is north
                rightDirection = new Vector3f(0, 0, 1);
                topDirection = new Vector3f(0, 1, 0);
            }
            case UP -> { // bottom left is southwest by minecraft block convention
                rightDirection = new Vector3f(-1, 0, 0);
                topDirection = new Vector3f(0, 0, 1);
            }
            case DOWN -> { // bottom left is northwest by minecraft block convention
                rightDirection = new Vector3f(1, 0, 0);
                topDirection = new Vector3f(0, 0, 1);
            }
        }
        rightDirection.mul(0.5F * width);
        topDirection.mul(0.5F * height);
        //Vertices
        Vector3f BLPos = new Vector3f(center);
        BLPos.sub(rightDirection);
        BLPos.sub(topDirection);
        Vector3f BRPos = new Vector3f(center);
        BRPos.add(rightDirection);
        BRPos.sub(topDirection);
        Vector3f TLPos = new Vector3f(center);
        TLPos.sub(rightDirection);
        TLPos.add(topDirection);
        Vector3f TRPos = new Vector3f(center);
        TRPos.add(rightDirection);
        TRPos.add(topDirection);
        //Adjusted UV
        Vec2 BRUV = new Vec2(UV.x + UVwidth, UV.y);
        Vec2 TLUV = new Vec2(UV.x, UV.y + UVheight);
        Vec2 TRUV = new Vec2(UV.x + UVwidth, UV.y + UVheight);
        Vector3f faceNormal = face.step();
        drawVertex(matrix, normal, builder, BLPos, UV, faceNormal, color, combinedLight);
        drawVertex(matrix, normal, builder, BRPos, BRUV, faceNormal, color, combinedLight);
        drawVertex(matrix, normal, builder, TRPos, TRUV, faceNormal, color, combinedLight);
        drawVertex(matrix, normal, builder, TLPos, TLUV, faceNormal, color, combinedLight);
    }

    private static void drawVertex(Matrix4f matrix, Matrix3f normal, VertexConsumer builder, Vector3f pos, Vec2 UV, Vector3f faceNormal, Color color, int combinedLight) {
        builder.vertex(matrix, pos.x(), pos.y(), pos.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).uv(UV.x, UV.y).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight).normal(normal, faceNormal.x(), faceNormal.y(), faceNormal.z()).endVertex();
    }

    public static void renderSprite(PoseStack stack, int x, int y, ResourceLocation tex) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(tex);
        GuiComponent.blit(stack, x, y, sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight(), ClientEngine.get().atlasWidth, ClientEngine.get().atlasHeight);
    }

    public static void renderKnowledge(PoseStack stack, int xPos, int yPos, int xOff, int yOff, Knowledge knowledge) {
        if(knowledge instanceof IItemKnowledge itemKnowledge) {
            Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(new ItemStack(itemKnowledge.getItem()), xPos + xOff, yPos + yOff);
        }
        else RenderUtil.renderSprite(stack, xPos, yPos, KnowledgeNF.KNOWLEDGE_TEXTURE);
    }

    /**
     * Renders items like in ItemRenderer but with the option to render dark silhouettes
     */
    public static void renderItem(PoseStack poseStack, ItemStack item, int xOff, int yOff, int blitOff, boolean darken) {
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        PoseStack viewStack = RenderSystem.getModelViewStack();
        viewStack.pushPose();
        viewStack.translate(xOff, yOff, blitOff + 16);
        viewStack.translate(8.0D, 8.0D, 0.0D);
        viewStack.scale(1.0F, -1.0F, 1.0F);
        viewStack.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        BakedModel model = mc.getItemRenderer().getModel(item, null, null, 0);
        Lighting.setupForFlatItems();

        poseStack.pushPose();
        model = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(poseStack, model, ItemTransforms.TransformType.GUI, false);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        RenderType rendertype = ItemBlockRenderTypes.getRenderType(item, true);
        Random random = new Random();
        random.setSeed(42L);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer builder = ItemRenderer.getFoilBufferDirect(buffer, rendertype, true, item.hasFoil());
        for(Direction dir : Direction.values()) {
            random.setSeed(42L);
            for(BakedQuad quad : model.getQuads(null, dir, random)) {
                if(darken) builder.putBulkData(pose, quad, 0, 0, 0, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
                else {
                    int rgb = -1;
                    if(quad.isTinted()) rgb = mc.getItemColors().getColor(item, quad.getTintIndex());
                    float r = (float) (rgb >> 16 & 255) / 255.0F;
                    float g = (float) (rgb >> 8 & 255) / 255.0F;
                    float b = (float) (rgb & 255) / 255.0F;
                    builder.putBulkData(pose, quad, r, g, b, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
                }
            }
        }
        for(BakedQuad quad : model.getQuads(null, null, random)) {
            if(darken) builder.putBulkData(pose, quad, 0, 0, 0, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
            else {
                int rgb = -1;
                if(quad.isTinted()) rgb = mc.getItemColors().getColor(item, quad.getTintIndex());
                float r = (float) (rgb >> 16 & 255) / 255.0F;
                float g = (float) (rgb >> 8 & 255) / 255.0F;
                float b = (float) (rgb & 255) / 255.0F;
                builder.putBulkData(pose, quad, r, g, b, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
            }
        }
        poseStack.popPose();

        buffer.endBatch();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        viewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static List<Recipe<?>> getSearchableRecipes(Player player) {
        return player.level.getRecipeManager().getRecipes().stream().sorted((r1, r2) -> {
            String r1Name = r1.getResultItem().getItem().getDescription().getString();
            String r2Name = r2.getResultItem().getItem().getDescription().getString();
            return r1Name.compareTo(r2Name);
        }).filter((recipe -> {
            return !recipe.getResultItem().isEmpty() && recipe instanceof IRenderableRecipe renderableRecipe && renderableRecipe.showInRecipeViewer();
        })).toList();
    }

    public static List<Recipe<?>> getUnlockedRecipes(Player player) {
        return player.level.getRecipeManager().getRecipes().stream().sorted((r1, r2) -> {
            String r1Name = r1.getResultItem().getItem().getDescription().getString();
            String r2Name = r2.getResultItem().getItem().getDescription().getString();
            return r1Name.compareTo(r2Name);
        }).filter((recipe -> {
            if(!recipe.getResultItem().isEmpty() && recipe instanceof IRenderableRecipe renderableRecipe && renderableRecipe.showInRecipeViewer()) {
                if(recipe instanceof IEncyclopediaRecipe encyclopediaRecipe) return encyclopediaRecipe.isUnlocked(player);
                else return true;
            }
            return false;
        })).toList();
    }
}
