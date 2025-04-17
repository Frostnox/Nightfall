package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.block.bowl.BowlBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class BowlRenderer<T extends BowlBlockEntity> implements BlockEntityRenderer<T> {
    public BowlRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(T pBlockEntity, float pPartialTick, PoseStack stack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if(!pBlockEntity.item.isEmpty()) {
            stack.pushPose();
            stack.translate(0.5D, 3.99D/16D, 0.5D);
            stack.mulPose(Vector3f.YP.rotationDegrees(pBlockEntity.itemAngle));
            stack.scale(0.5F, 0.5F, 0.5F);
            Minecraft.getInstance().getItemRenderer().renderStatic(pBlockEntity.item, ItemTransforms.TransformType.FIXED, pPackedLight, pPackedOverlay, stack, pBufferSource,
                    (int) pBlockEntity.getBlockPos().asLong());
            stack.popPose();
        }
    }
}
