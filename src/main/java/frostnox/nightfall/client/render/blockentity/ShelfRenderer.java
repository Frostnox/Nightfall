package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.block.shelf.ShelfBlock;
import frostnox.nightfall.block.block.shelf.ShelfBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ShelfRenderer<T extends ShelfBlockEntity> implements BlockEntityRenderer<T> {
    public ShelfRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public boolean shouldRender(ShelfBlockEntity shelf, Vec3 camPos) {
        return !shelf.items.isEmpty();
    }

    @Override
    public void render(T shelf, float pPartialTick, PoseStack stack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        stack.pushPose();
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(Vector3f.YP.rotationDegrees(-shelf.getBlockState().getValue(ShelfBlock.FACING).toYRot()));
        stack.translate(-0.5, -0.5, -0.5);
        for(int i = 0; i < shelf.items.size(); i++) {
            ItemStack item = shelf.items.get(i);
            if(!item.isEmpty()) {
                BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(item, shelf.getLevel(), null, (int) shelf.getBlockPos().asLong());
                boolean is3d = model.isGui3d();
                stack.pushPose();
                float scale = is3d ? 0.5F : 7F/16F;
                stack.translate(5.5D/16D + 5D/16D * (i % 2), is3d ? (11D/16D - 7D/16D * (i / 2)) : (9.25D/16D - 7D/16D * (i / 2) + 0.001D * (i % 2)), 4D/16D);
                if(!is3d) stack.mulPose(Vector3f.XP.rotationDegrees(-90));
                stack.scale(scale, scale, 0.5F);
                for(int j = 0; j < (is3d ? 1 : Math.ceil((double) item.getCount() / item.getMaxStackSize() * 4D)); j++) {
                    stack.pushPose();
                    if(j > 0) {
                        stack.translate(0, 0, 1D/16D * j);
                        if(j % 2 == 1) stack.mulPose(Vector3f.ZP.rotationDegrees(-90));
                    }
                    Minecraft.getInstance().getItemRenderer().render(item, ItemTransforms.TransformType.FIXED, false, stack, pBufferSource, pPackedLight, pPackedOverlay, model);
                    stack.popPose();
                }
                stack.popPose();
            }
        }
        stack.popPose();
    }
}
