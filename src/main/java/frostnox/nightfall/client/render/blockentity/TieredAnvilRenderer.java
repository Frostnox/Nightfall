package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlockEntity;
import frostnox.nightfall.registry.forge.FluidsNF;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;

import java.awt.*;

public class TieredAnvilRenderer implements BlockEntityRenderer<TieredAnvilBlockEntity> {
    public TieredAnvilRenderer(BlockEntityRendererProvider.Context renderer) {

    }

    public static void renderWorkpiece(PoseStack stack, MultiBufferSource buffers, Color color, int light, double center, float temperature, int[] work) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FluidsNF.METAL_SOLID);
        if(temperature > 500) light = LightTexture.FULL_BRIGHT;
        else if(temperature > 100) {
            int blockLight = 15 - Math.round((500 - temperature) / (400F / 14F));
            light = LightTexture.pack(Math.max(blockLight, LightTexture.block(light)), LightTexture.sky(light));
        }
        float draw = work[1] * 0.5F/2F;
        float centerY = draw/2/16F;
        float rightPunch = work[7] * 0.5F / 16F;
        float rightDraw = work[6] * 0.5F/2F;
        float rightY = rightPunch + rightDraw/2/16F;
        float leftDraw = work[3] * 0.5F/2F;
        float leftPunch = work[4] * 0.5F / 16F;
        float leftY = leftPunch + leftDraw/2/16F;
        float yOff = center == 0.25 ? -Math.min(rightY, centerY) : -Math.min(Math.min(centerY, leftY), rightY);
        float baseWidth = 2F/16F, baseHeight = 2F/16F, baseDepth = 3F/16F;
        //Center
        float spread = work[0] * 0.5F/2F;
        float centerWidth = baseWidth * (1F + draw);
        float height = baseHeight * (1F - spread - draw/2);
        float depth = baseDepth * (1F + spread - draw/2);
        if(height > 0) RenderUtil.drawBox(stack, buffers, color, light, centerWidth, height, depth, -centerWidth/2, centerY + yOff, -depth/2, sprite, 5, 0);
        //Left end
        spread = work[2] * 0.5F/2F;
        float width = baseWidth * (1F + leftDraw);
        height = baseHeight * (1F - spread - leftDraw/2);
        depth = baseDepth * (1F + spread - leftDraw/2);
        if(height > 0) RenderUtil.drawBox(stack, buffers, color, light, width, height, depth, -width - centerWidth/2, leftY + yOff, -depth/2, sprite, 0, 0);
        //Right end
        spread = work[5] * 0.5F/2F;
        width = baseWidth * (1F + rightDraw);
        height = baseHeight * (1F - spread - rightDraw/2);
        depth = baseDepth * (1F + spread - rightDraw/2);
        if(height > 0) RenderUtil.drawBox(stack, buffers, color, light, width, height, depth, centerWidth/2, rightY + yOff, -depth/2, sprite, 10, 0);
    }

    @Override
    public void render(TieredAnvilBlockEntity entity, float partialTicks, PoseStack stack, MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
        stack.pushPose();
        if(entity.getRotationDegrees() != 0) {
            stack.translate(0.5, 0.5, 0.5);
            stack.mulPose(Vector3f.YP.rotationDegrees(entity.getRotationDegrees()));
            stack.translate(-0.5, -0.5, -0.5);
        }
        if(entity.hasWorkpiece()) {
            Color color = RenderUtil.getHeatedMetalColor(entity.getTemperature(), entity.getColor().getRGB());
            double center = entity.getSection().center;
            stack.translate(center, 1, 0.5);
            renderWorkpiece(stack, buffers, color, entity.hasLevel() ? LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above()) : LightTexture.FULL_BRIGHT, center, entity.getTemperature(), entity.work);
        }
        stack.popPose();
    }
}
