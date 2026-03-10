package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.block.furnacechannel.FurnaceChannelBlock;
import frostnox.nightfall.block.block.furnacechannel.FurnaceChannelBlockEntity;
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
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class FurnaceChannelRenderer<T extends FurnaceChannelBlockEntity> implements BlockEntityRenderer<T> {
    public FurnaceChannelRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(FurnaceChannelBlockEntity furnaceChannel, Vec3 camPos) {
        return furnaceChannel.visualDist > 0 && Vec3.atCenterOf(furnaceChannel.getBlockPos()).closerThan(camPos, getViewDistance());
    }

    @Override
    public void render(T entity, float pPartialTick, PoseStack stack, MultiBufferSource buffers, int pPackedLight, int pPackedOverlay) {
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(Vector3f.YP.rotationDegrees(180 - entity.getBlockState().getValue(FurnaceChannelBlock.FACING).toYRot()));
        stack.translate(-0.5, -0.5, -0.5);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FluidsNF.METAL_FLOW);
        VertexConsumer builder = buffers.getBuffer(RenderType.entitySolid(sprite.atlas().location()));
        Matrix4f matrix = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        Color color = RenderUtil.getHeatedMetalColor(entity.visualTemp, 0xffcb623a); //Metal color doesn't matter
        Vec2 UV = new Vec2(sprite.getU0(), sprite.getV0());
        float xSize = 2F/16F, zSize = 9F/16F;
        RenderUtil.drawFace(Direction.UP, matrix, normal, builder, color, new Vec3(8D/16D, 8D/16D, 4.5D/16D), xSize, zSize, UV,
                16F * xSize / ClientEngine.get().atlasWidth, 16F * zSize / ClientEngine.get().atlasHeight, LightTexture.FULL_BLOCK);
        zSize = 8F/16F;
        for(Direction dir : Direction.Plane.HORIZONTAL) {
            RenderUtil.drawFace(dir, matrix, normal, builder, color, new Vec3(8D/16D + dir.getStepX()/16D, 8D/16D - zSize/2, 8D/16D + dir.getStepZ()/16D), xSize, zSize, UV,
                    16F * xSize / ClientEngine.get().atlasWidth, 16F * zSize / ClientEngine.get().atlasHeight, LightTexture.FULL_BLOCK, false, true);
            for(int i = 0; i < entity.visualDist; i++) {
                RenderUtil.drawFace(dir, matrix, normal, builder, color, new Vec3(8D/16D + dir.getStepX()/16D, 8D/16D - (i + 1) * 1F, 8D/16D + dir.getStepZ()/16D), xSize, 1, UV,
                        16F * xSize / ClientEngine.get().atlasWidth, 16F / ClientEngine.get().atlasHeight, LightTexture.FULL_BLOCK, false, true);
            }
        }
    }
}
