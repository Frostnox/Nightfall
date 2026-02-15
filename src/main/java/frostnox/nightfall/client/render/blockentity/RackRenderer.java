package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.block.rack.RackBlock;
import frostnox.nightfall.block.block.rack.RackBlockEntity;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.extensible.TransformTypeNF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class RackRenderer<T extends RackBlockEntity> implements BlockEntityRenderer<T> {
    public RackRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public boolean shouldRender(RackBlockEntity rack, Vec3 camPos) {
        return !rack.items.isEmpty() && Vec3.atCenterOf(rack.getBlockPos()).closerThan(camPos, getViewDistance());
    }

    @Override
    public void render(T rack, float pPartialTick, PoseStack stack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        stack.pushPose();
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(Vector3f.YP.rotationDegrees(-rack.getBlockState().getValue(RackBlock.FACING).toYRot()));
        stack.translate(-0.5, -0.5, -0.5);
        for(int i = 0; i < rack.items.size(); i++) {
            ItemStack item = rack.items.get(i);
            if(!item.isEmpty()) {
                stack.pushPose();
                float scale = 15F/16F;
                if(item.is(TagsNF.ARMAMENT) || item.is(TagsNF.SHIELD)) {
                    stack.translate(8D / 16D - 5D / 16D * scale, 4.01D / 16D + 5D / 16D * i, 1.5D / 16D);
                    stack.scale(scale, scale, scale);
                    if(item.is(TagsNF.SHIELD)) {
                        if(i % 2 != 0) stack.translate(0, 0, -0.5D / 16D);
                        else stack.translate(0, 0, 0.5D / 16D);
                    }
                    else {
                        stack.mulPose(Vector3f.ZP.rotationDegrees(-90));
                        if(i % 2 != 0) {
                            stack.mulPose(Vector3f.XP.rotationDegrees(180));
                            stack.translate(0, -10D / 16D, 0.5D / 16D);
                        }
                        else stack.translate(0, 0, 0.5D / 16D);
                    }
                }
                else {
                    stack.translate(16.6D/16D, 5D/16D - 5D/16D * (2 - i), -5D/16D);
                    stack.scale(scale, scale, scale);
                    stack.mulPose(Vector3f.ZP.rotationDegrees(45));
                    stack.translate(0, 0, i % 2 != 0 ? -(0.5D/16D) : (0.5D/16D));
                }
                Minecraft.getInstance().getItemRenderer().renderStatic(item, TransformTypeNF.RACK, pPackedLight,
                        pPackedOverlay, stack, pBufferSource, (int) rack.getBlockPos().asLong());
                stack.popPose();
            }
        }
        stack.popPose();
    }
}
