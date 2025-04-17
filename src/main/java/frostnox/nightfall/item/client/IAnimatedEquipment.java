package frostnox.nightfall.item.client;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.client.model.entity.AttachedEntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IAnimatedEquipment {
    void animate(AttachedEntityModel model, ItemStack itemStack, PoseStack poseStack, MultiBufferSource buffer, int light, LivingEntity entity,
                 float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch);
}
