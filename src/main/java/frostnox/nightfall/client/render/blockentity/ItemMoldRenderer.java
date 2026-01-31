package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import frostnox.nightfall.block.block.mold.ItemMoldBlockEntity;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.registry.forge.FluidsNF;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class ItemMoldRenderer implements BlockEntityRenderer<ItemMoldBlockEntity> {
    public ItemMoldRenderer(BlockEntityRendererProvider.Context renderer) {

    }

    @Override
    public void render(ItemMoldBlockEntity entity, float partialTicks, PoseStack stack, MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
        if(entity.getInputFluid().isEmpty()) return;
        stack.pushPose();
        boolean isFluid = !entity.isCool();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(entity.getTemperature() > 500 ? entity.getInputFluid().getFluid().getAttributes().getStillTexture() : FluidsNF.METAL_SOLID);
        VertexConsumer builder = buffers.getBuffer(RenderType.entitySolid(sprite.atlas().location()));
        Matrix4f matrix = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        Color color = RenderUtil.getHeatedMetalColor(entity.getTemperature(), entity.getInputFluid().getFluid().getAttributes().getColor());
        float height = (float) entity.getInputFluid().getAmount() / entity.getMaxUnits();
        AABB shape = entity.getBlockState().getShape(entity.getLevel(), entity.getBlockPos()).bounds();
        float xSize = (float) shape.getXsize() - 2F/16F, zSize = (float) shape.getZsize() - 2F/16F;
        Vec2 UV = new Vec2(sprite.getU0(), sprite.getV0());
        RenderUtil.drawFace(Direction.UP, matrix, normal, builder, color,
                new Vec3(8D/16D, 1D/16D + (shape.getYsize() - 1.25D/16D) * height, 8D/16D), xSize, zSize, UV,
                16F * xSize / ClientEngine.get().atlasWidth, 16F * zSize / ClientEngine.get().atlasHeight, isFluid ? LightTexture.FULL_BLOCK : combinedLight);
        stack.popPose();
    }
}
