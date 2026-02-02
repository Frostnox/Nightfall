package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlockEntity;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.recipe.TieredAnvilRecipe;
import frostnox.nightfall.registry.forge.FluidsNF;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.*;

public class TieredAnvilRenderer implements BlockEntityRenderer<TieredAnvilBlockEntity> {
    private static final Color SLAG_COLOR = new Color(48, 43, 50);

    public TieredAnvilRenderer(BlockEntityRendererProvider.Context renderer) {

    }

    public static void renderWorkpiece(PoseStack stack, MultiBufferSource buffers, Color color, int light, TextureAtlasSprite sprite, double center, float temperature, int[] work) {
        if(temperature > 500) light = LightTexture.FULL_BRIGHT;
        else if(temperature > 100) {
            int blockLight = 15 - Math.round((500 - temperature) / (400F / 14F));
            light = LightTexture.pack(Math.max(blockLight, LightTexture.block(light)), LightTexture.sky(light));
        }
        float draw = work[1] * 1F/2F;
        float centerY = draw/2/16F;
        float rightPunch = work[7] * 0.5F / 16F;
        float rightDraw = work[6] * 1F/2F;
        float rightY = rightPunch + rightDraw/2/16F;
        float leftDraw = work[3] * 1F/2F;
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
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FluidsNF.METAL_SOLID);
        Color color = RenderUtil.getHeatedMetalColor(0, Metal.COPPER.getColor().getRGB());
        int light = LightTexture.FULL_BRIGHT;
        double center = 0.5; //0.25 0.5 0.75
        stack.translate(center, 1, 0.5);
        renderWorkpiece(stack, buffers, color, light, sprite, center, 0, entity.work);

        if(!entity.getResult().isEmpty()) {
            if(entity.getResult().getItem() instanceof BlockItem) {
                stack.translate(0.5, 1 + 8D/64D, 0.5);
            }
            else {
                stack.translate(0.5, 1 + 1D / 64D, 0.5);
                stack.mulPose(Vector3f.XP.rotationDegrees(-90));
            }
            stack.scale(0.5F, 0.5F, 0.5F);
            Minecraft.getInstance().getItemRenderer().renderStatic(entity.getResult(), ItemTransforms.TransformType.FIXED,
                    entity.hasLevel() ? LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above()) : LightTexture.FULL_BRIGHT,
                    combinedOverlay, stack, buffers, (int) entity.getBlockPos().asLong());
        }
        else {
            stack.translate(entity.getWorldGridOffset().x(), entity.getWorldGridOffset().y(), entity.getWorldGridOffset().z());
            //Cubes
            if(entity.gridColors[0][0][0] != null) {
                for(int x = 0; x < TieredAnvilBlockEntity.GRID_X; x++) {
                    for(int y = 0; y < TieredAnvilBlockEntity.GRID_Y; y++) {
                        for(int z = 0; z < TieredAnvilBlockEntity.GRID_Z; z++) {
                            boolean slag = entity.slag[x][y][z];
                            if(entity.grid[x][y][z] || slag) {
                                RenderUtil.drawMicroGridCube(stack, buffers, slag ? SLAG_COLOR : entity.gridColors[x][y][z], LightTexture.FULL_BRIGHT, 1F/16F, entity.grid,
                                        x, y, z, Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FluidsNF.METAL_SOLID));
                            }
                        }
                    }
                }
            }
            Player player = Minecraft.getInstance().player;
            if(entity.inProgress && entity.getLevel() != null && entity.getRecipeID() != null &&
                    entity.getLevel().getRecipeManager().byKey(entity.getRecipeID()).isPresent() && player != null && player.isAlive()
                    && entity.getBlockPos().distToCenterSqr(player.getPosition(partialTicks)) <= 9
                    && (player.getMainHandItem().is(TagsNF.HAMMER) || player.getOffhandItem().is(TagsNF.HAMMER))) {
                Vec3i hitPos = ClientEngine.get().microHitResult;
                boolean hitNull = hitPos == null || !ClientEngine.get().microBlockEntityPos.equals(entity.getBlockPos());
                int hitX = 0, hitY = 0, hitZ = 0;
                if(!hitNull) {
                    hitX = hitPos.getX();
                    hitY = hitPos.getY();
                    hitZ = hitPos.getZ();
                }
                TieredAnvilRecipe recipe = (TieredAnvilRecipe) entity.getLevel().getRecipeManager().byKey(entity.getRecipeID()).get();
                boolean[][][] finishShape = recipe.getFinishShape();
                //Outlines
                for(int x = 0; x < TieredAnvilBlockEntity.GRID_X; x++) {
                    for(int y = 0; y < TieredAnvilBlockEntity.GRID_Y; y++) {
                        for(int z = 0; z < TieredAnvilBlockEntity.GRID_Z; z++) {
                            //Make sure this cube isn't selected
                            if(hitNull || x != hitX || y != hitY || z != hitZ) {
                                if(entity.grid[x][y][z] != finishShape[x][y][z]) {
                                    double cubeX = x/16D;
                                    double cubeY = y/16D;
                                    double cubeZ = z/16D;
                                    VoxelShape cube = Shapes.create(cubeX, cubeY, cubeZ, cubeX + 1/16F, cubeY + 1/16F, cubeZ + 1/16F);
                                    Color outlineColor = entity.grid[x][y][z] ? Color.RED : Color.WHITE;
                                    RenderUtil.drawMicroGridCubeOutline(stack, buffers, outlineColor, cube, hitNull);
                                }
                            }
                        }
                    }
                }
            }
        }
        stack.popPose();
    }
}
