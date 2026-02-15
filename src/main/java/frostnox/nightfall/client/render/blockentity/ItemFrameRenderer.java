package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.block.itemframe.ItemFrameBlock;
import frostnox.nightfall.block.block.itemframe.ItemFrameBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ItemFrameRenderer<T extends ItemFrameBlockEntity> implements BlockEntityRenderer<T> {
    public ItemFrameRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public boolean shouldRender(ItemFrameBlockEntity frame, Vec3 camPos) {
        return !frame.items.isEmpty() && Vec3.atCenterOf(frame.getBlockPos()).closerThan(camPos, getViewDistance());
    }

    @Override
    public void render(T frame, float pPartialTick, PoseStack stack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        stack.pushPose();
        stack.translate(0.5, 0.5, 0.5);
        Direction dir = frame.getBlockState().getValue(ItemFrameBlock.FACING).getOpposite();
        stack.translate(dir.getStepX() * 0.46875D, dir.getStepY() * 0.46875D, dir.getStepZ() * 0.46875D);
        stack.mulPose(Vector3f.XP.rotationDegrees(dir == Direction.UP ? 90 : (dir == Direction.DOWN ? -90 : 0)));
        if(dir.getAxis().isHorizontal()) stack.mulPose(Vector3f.YP.rotationDegrees(-dir.toYRot()));
        else stack.mulPose(Vector3f.YP.rotationDegrees(-180));
        ItemStack item = frame.items.get(0);
        if(!item.isEmpty()) {
            stack.mulPose(Vector3f.ZP.rotationDegrees(frame.getRotation() * 360.0F / 8.0F));
            stack.scale(0.5F, 0.5F, 0.5F);
            Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemTransforms.TransformType.FIXED, pPackedLight, OverlayTexture.NO_OVERLAY, stack,
                    pBufferSource, (int) frame.getBlockPos().asLong());
        }
        stack.popPose();
    }
}
