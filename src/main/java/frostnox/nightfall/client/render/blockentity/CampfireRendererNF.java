package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.block.campfire.CampfireBlockEntityNF;
import frostnox.nightfall.block.block.campfire.CampfireBlockNF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class CampfireRendererNF<T extends CampfireBlockEntityNF> implements BlockEntityRenderer<T> {
    public CampfireRendererNF(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(T pBlockEntity, float pPartialTick, PoseStack stack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Direction.Axis axis = pBlockEntity.getBlockState().getValue(CampfireBlockNF.AXIS);
        int packedPos = (int)pBlockEntity.getBlockPos().asLong();
        if(axis == Direction.Axis.Z) {
            for(int i = 0; i < pBlockEntity.items.size(); i++) {
                ItemStack item = pBlockEntity.items.get(i);
                if(item != ItemStack.EMPTY) {
                    stack.pushPose();
                    if(i % 2 == 1) stack.translate(0.5D + 0.25D * ((i + 1) / 2), 1D - (0.25F * 0.375F), 0.5D);
                    else if(i > 0) stack.translate(0.5D - 0.25D * (i / 2), 1D - (0.25F * 0.375F), 0.5D);
                    else stack.translate(0.5D, 1D - (0.25F * 0.375F), 0.5D);
                    stack.mulPose(Vector3f.YP.rotationDegrees(90));
                    stack.scale(0.375F, 0.375F, 0.375F);
                    Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemTransforms.TransformType.FIXED, pPackedLight, pPackedOverlay, stack, pBufferSource, packedPos + i);
                    stack.popPose();
                }
            }
        }
        else {
            for(int i = 0; i < pBlockEntity.items.size(); i++) {
                ItemStack item = pBlockEntity.items.get(i);
                if(item != ItemStack.EMPTY) {
                    stack.pushPose();
                    if(i % 2 == 1) stack.translate(0.5D, 1D - (0.25F * 0.375F), 0.5D + 0.25D * ((i + 1) / 2));
                    else if(i > 0) stack.translate(0.5D, 1D - (0.25F * 0.375F), 0.5D - 0.25D * (i / 2));
                    else stack.translate(0.5D, 1D - (0.25F * 0.375F), 0.5D);
                    stack.scale(0.375F, 0.375F, 0.375F);
                    Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemTransforms.TransformType.FIXED, pPackedLight, pPackedOverlay, stack, pBufferSource, packedPos + i);
                    stack.popPose();
                }
            }
        }
    }
}
