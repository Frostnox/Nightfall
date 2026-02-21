package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.block.anvil.AnvilSection;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlockEntity;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.data.recipe.TieredAnvilRecipe;
import frostnox.nightfall.registry.forge.FluidsNF;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import java.awt.*;

public class TieredAnvilRenderer implements BlockEntityRenderer<TieredAnvilBlockEntity> {
    public static final Color SLAG_COLOR = new Color(48, 43, 50);

    public TieredAnvilRenderer(BlockEntityRendererProvider.Context renderer) {

    }

    public static void renderWorkpiece(PoseStack stack, MultiBufferSource buffers, Color color, int light, AnvilSection section, float temperature, int[] work, boolean flipUvX, boolean slagCenter, boolean slagLeft, boolean slagRight) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FluidsNF.METAL_SOLID);
        if(temperature > 500) light = LightTexture.FULL_BRIGHT;
        else if(temperature > 100) {
            int blockLight = 15 - Math.round((500 - temperature) / (400F / 14F));
            light = LightTexture.pack(Math.max(blockLight, LightTexture.block(light)), LightTexture.sky(light));
        }
        float baseWidth = 2F/16F, baseHeight = 2F/16F, baseDepth = 3F/16F;
        //work = new int[]{0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0};

        float spread = work[0] * 0.5F;
        float draw = work[1] * (work[1] < 0 ? 0.25F : 0.5F);
        float cut = work[2] == 0 ? 1 : (work[2] == 1 ? 0.5F : 0.25F);
        float height = work[2] > 2 ? 0 : Math.max(0, baseHeight * (1F - spread/2 - draw/2));

        float leftDraw = work[4] * (work[4] < 0 ? 0.25F : 0.5F);
        float leftPunch = work[5] * -0.5F/16F;
        float leftSpread = work[3] * 0.5F;
        float leftCut = work[6] == 0 ? 1 : (work[6] == 1 ? 0.5F : 0.25F);
        float leftHeight = work[6] > 2 ? 0 : Math.max(0, baseHeight * (1F - leftSpread/2 - leftDraw/2));

        float rightDraw = work[8] * (work[8] < 0 ? 0.25F : 0.5F);
        float rightPunch = work[9] * -0.5F/16F;
        float rightSpread = work[7] * 0.5F;
        float rightCut = work[10] == 0 ? 1 : (work[10] == 1 ? 0.5F : 0.25F);
        float rightHeight = work[10] > 2 ? 0 : Math.max(0, baseHeight * (1F - rightSpread/2 - rightDraw/2));

        float yOff = -Math.min(Math.min(height > 0 ? 0 : 1, leftHeight > 0 ? (leftPunch + (section == AnvilSection.HORN ? 1F/16F : 0)) : 1), rightHeight > 0 ? rightPunch : 1);
        //Center
        float centerWidth = baseWidth * (1F + draw);
        float depth = baseDepth * (1F + spread - draw/2) * cut;
        if(height > 0) RenderUtil.drawBox(stack, buffers, slagCenter ? SLAG_COLOR : color, light, centerWidth, height, depth, -centerWidth/2, yOff, -depth/2, sprite, 5, 0, flipUvX, false);
        float spacing = height > 0 ? centerWidth/2 : 1F/32F;
        //Left end
        float width = baseWidth * (1F + leftDraw) * leftCut;
        depth = baseDepth * (1F + leftSpread - leftDraw/2);
        if(leftHeight > 0) RenderUtil.drawBox(stack, buffers, slagLeft ? SLAG_COLOR : color, light, width, leftHeight, depth, -width - spacing, leftPunch + yOff, -depth/2, sprite, flipUvX ? 10 : 0, 0, flipUvX, false);
        //Right end
        width = baseWidth * (1F + rightDraw) * rightCut;
        depth = baseDepth * (1F + rightSpread - rightDraw/2);
        if(rightHeight > 0) RenderUtil.drawBox(stack, buffers, slagRight ? SLAG_COLOR : color, light, width, rightHeight, depth, spacing, rightPunch + yOff, -depth/2, sprite, flipUvX ? 0 : 10, 0, flipUvX, false);
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
            stack.translate(entity.getRawSection().center, 1, 0.5);
            AnvilSection section = entity.getSection();
            renderWorkpiece(stack, buffers, RenderUtil.getHeatedMetalColor(entity.getTemperature(), entity.getColor().getRGB()),
                    entity.hasLevel() ? LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above()) : LightTexture.FULL_BRIGHT, section, entity.getTemperature(), entity.work,
                    entity.hasFlip(), entity.hasSlagCenter(), entity.hasSlagLeft(), entity.hasSlagRight());
            if(ClientEngine.get().getLastVisualizedRecipe() instanceof TieredAnvilRecipe recipe && recipe.input.test(entity.getWorkpiece())) {
                Player player = Minecraft.getInstance().player;
                if(player != null && player.isAlive() && entity.getBlockPos().distToCenterSqr(player.getPosition(partialTicks)) < 2 * 2) {
                    stack.translate(0, 4F/16F, 0);
                    if(entity.hasFlip()) stack.mulPose(Vector3f.YP.rotationDegrees(180));
                    float baseWidth = 2F/16F, baseHeight = 2F/16F, baseDepth = 3F/16F;
                    int[] work = recipe.work;

                    float spread = work[0] * 0.5F;
                    float draw = work[1] * (work[1] < 0 ? 0.25F : 0.5F);
                    float cut = work[2] == 0 ? 1 : (work[2] == 1 ? 0.5F : 0.25F);
                    float height = work[2] > 2 ? 0 : Math.max(0, baseHeight * (1F - spread/2 - draw/2));

                    float leftDraw = work[4] * (work[4] < 0 ? 0.25F : 0.5F);
                    float leftPunch = work[5] * -0.5F/16F;
                    float leftSpread = work[3] * 0.5F;
                    float leftCut = work[6] == 0 ? 1 : (work[6] == 1 ? 0.5F : 0.25F);
                    float leftHeight = work[6] > 2 ? 0 : Math.max(0, baseHeight * (1F - leftSpread/2 - leftDraw/2));

                    float rightDraw = work[8] * (work[7] < 0 ? 0.25F : 0.5F);
                    float rightPunch = work[9] * -0.5F/16F;
                    float rightSpread = work[7] * 0.5F;
                    float rightCut = work[10] == 0 ? 1 : (work[10] == 1 ? 0.5F : 0.25F);
                    float rightHeight = work[10] > 2 ? 0 : Math.max(0, baseHeight * (1F - rightSpread/2 - rightDraw/2));

                    float yOff = -Math.min(Math.min(height > 0 ? 0 : 1, leftHeight > 0 ? (leftPunch + (section == AnvilSection.HORN ? 1F/16F : 0)) : 1), rightHeight > 0 ? rightPunch : 1);
                    //Center
                    float centerWidth = baseWidth * (1F + draw);
                    float depth = baseDepth * (1F + spread - draw/2) * cut;
                    if(height > 0) {
                        double x = -centerWidth/2, y = yOff, z = -depth/2;
                        RenderUtil.drawShapeOutline(stack, buffers, new Color(1, 1, 1, 0.75F), Shapes.create(x, y, z, x + centerWidth, y + height, z + depth));
                    }
                    float spacing = height > 0 ? centerWidth/2 : 1F/32F;
                    //Left end
                    float width = baseWidth * (1F + leftDraw) * leftCut;
                    depth = baseDepth * (1F + leftSpread - leftDraw/2);
                    if(leftHeight > 0) {
                        double x = -width - spacing, y = leftPunch + yOff, z = -depth/2;
                        RenderUtil.drawShapeOutline(stack, buffers, new Color(1, 1, 1, 0.75F), Shapes.create(x, y, z, x + width, y + leftHeight, z + depth));
                    }
                    //Right end
                    width = baseWidth * (1F + rightDraw) * rightCut;
                    depth = baseDepth * (1F + rightSpread - rightDraw/2);
                    if(rightHeight > 0) {
                        double x = spacing, y = rightPunch + yOff, z = -depth/2;
                        RenderUtil.drawShapeOutline(stack, buffers, new Color(1, 1, 1, 0.75F), Shapes.create(x, y, z, x + width, y + leftHeight, z + depth));
                    }
                }
            }
        }
        stack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(TieredAnvilBlockEntity anvil, Vec3 camPos) {
        return anvil.hasWorkpiece() && Vec3.atCenterOf(anvil.getBlockPos()).closerThan(camPos, getViewDistance());
    }
}
