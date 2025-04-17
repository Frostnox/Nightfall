package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
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

    @Override
    public void render(TieredAnvilBlockEntity entity, float partialTicks, PoseStack stack, MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
        stack.pushPose();
        if(entity.getRotationDegrees() != 0) {
            stack.translate(0.5, 0.5, 0.5);
            stack.mulPose(Vector3f.YP.rotationDegrees(entity.getRotationDegrees()));
            stack.translate(-0.5, -0.5, -0.5);
        }
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
                                    Color color = entity.grid[x][y][z] ? Color.RED : Color.WHITE;
                                    RenderUtil.drawMicroGridCubeOutline(stack, buffers, color, cube, hitNull);
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
