package frostnox.nightfall.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.model.entity.AnimatedHumanoidModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class HeldItemLayer<T extends LivingEntity, M extends AnimatedHumanoidModel<T>> extends ItemInHandLayer<T, M> {
    public HeldItemLayer(RenderLayerParent<T, M> p_i50934_1_) {
        super(p_i50934_1_);
    }

    @Override
    public void render(PoseStack p_225628_1_, MultiBufferSource p_225628_2_, int p_225628_3_, T p_225628_4_, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        boolean flag = p_225628_4_.getMainArm() == HumanoidArm.RIGHT;
        ItemStack itemstack = flag ? p_225628_4_.getOffhandItem() : p_225628_4_.getMainHandItem();
        ItemStack itemstack1 = flag ? p_225628_4_.getMainHandItem() : p_225628_4_.getOffhandItem();
        if (!itemstack.isEmpty() || !itemstack1.isEmpty()) {
            p_225628_1_.pushPose();
            this.renderArmWithItem(p_225628_4_, itemstack1, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, p_225628_1_, p_225628_2_, p_225628_3_);
            this.renderArmWithItem(p_225628_4_, itemstack, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, p_225628_1_, p_225628_2_, p_225628_3_);
            p_225628_1_.popPose();
        }
    }

    @Override
    protected void renderArmWithItem(LivingEntity user, ItemStack itemStack, ItemTransforms.TransformType p_229135_3_, HumanoidArm handSide, PoseStack stack, MultiBufferSource p_229135_6_, int p_229135_7_) {
        if(!itemStack.isEmpty()) {
            stack.pushPose();
            this.getParentModel().translateToHand(handSide, stack);
            stack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            stack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            boolean flag = handSide == HumanoidArm.LEFT;
            Minecraft.getInstance().getItemInHandRenderer().renderItem(user, itemStack, p_229135_3_, flag, stack, p_229135_6_, p_229135_7_);
            stack.popPose();
        }
    }
}
