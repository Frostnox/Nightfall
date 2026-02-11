package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.block.anvil.AnvilSection;
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
    public static final Color SLAG_COLOR = new Color(48, 43, 50);

    public TieredAnvilRenderer(BlockEntityRendererProvider.Context renderer) {

    }

    public static void renderWorkpiece(PoseStack stack, MultiBufferSource buffers, Color color, int light, AnvilSection section, float temperature, int[] work, boolean flipUvX, boolean flipUvY, boolean slagCenter, boolean slagLeft, boolean slagRight) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FluidsNF.METAL_SOLID);
        if(temperature > 500) light = LightTexture.FULL_BRIGHT;
        else if(temperature > 100) {
            int blockLight = 15 - Math.round((500 - temperature) / (400F / 14F));
            light = LightTexture.pack(Math.max(blockLight, LightTexture.block(light)), LightTexture.sky(light));
        }
        float baseWidth = 2F/16F, baseHeight = 2F/16F, baseDepth = 3F/16F;

        float spread = work[0] * 0.5F/2F;
        float draw = work[1] * 0.5F/2F;
        float centerY = draw/2/16F;
        float height = Math.max(0, baseHeight * (1F - spread - draw/2));

        float leftDraw = work[3] * 0.5F/2F;
        float leftPunch = work[4] * 0.5F / 16F;
        float leftY = leftPunch + leftDraw/2/16F;
        float leftSpread = work[2] * 0.5F/2F;
        float leftHeight = Math.max(0, baseHeight * (1F - leftSpread - leftDraw/2));

        float rightDraw = work[6] * 0.5F/2F;
        float rightPunch = work[7] * 0.5F / 16F;
        float rightY = rightPunch + rightDraw/2/16F;
        float rightSpread = work[5] * 0.5F/2F;
        float rightHeight = Math.max(0, baseHeight * (1F - rightSpread - rightDraw/2));

        float yOff = -Math.min(Math.min(height > 0 ? centerY : 1, leftHeight > 0 ? (leftY + (section == AnvilSection.HORN ? 1F/16F : 0)) : 1), rightHeight > 0 ? rightY : 1);
        //Center
        float centerWidth = baseWidth * (1F + draw);
        float depth = baseDepth * (1F + spread - draw/2);
        if(height > 0) RenderUtil.drawBox(stack, buffers, slagCenter ? SLAG_COLOR : color, light, centerWidth, height, depth, -centerWidth/2, centerY + yOff, -depth/2, sprite, 5, 0, flipUvX, flipUvY);
        //Left end
        float width = baseWidth * (1F + leftDraw);
        depth = baseDepth * (1F + leftSpread - leftDraw/2);
        if(leftHeight > 0) RenderUtil.drawBox(stack, buffers, slagLeft ? SLAG_COLOR : color, light, width, leftHeight, depth, -width - centerWidth/2, leftY + yOff, -depth/2, sprite, flipUvX ? 10 : 0, 0, flipUvX, flipUvY);
        //Right end
        width = baseWidth * (1F + rightDraw);
        depth = baseDepth * (1F + rightSpread - rightDraw/2);
        if(rightHeight > 0) RenderUtil.drawBox(stack, buffers, slagRight ? SLAG_COLOR : color, light, width, rightHeight, depth, centerWidth/2, rightY + yOff, -depth/2, sprite, flipUvX ? 0 : 10, 0, flipUvX, flipUvY);
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
            stack.translate(entity.getRawSection().center, 1, 0.5);
            renderWorkpiece(stack, buffers, color, entity.hasLevel() ? LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above()) : LightTexture.FULL_BRIGHT, entity.getSection(), entity.getTemperature(), entity.work,
                    entity.hasFlipXZ(), entity.hasFlipY(), entity.hasSlagCenter(), entity.hasSlagLeft(), entity.hasSlagRight());
        }
        stack.popPose();
    }
}
