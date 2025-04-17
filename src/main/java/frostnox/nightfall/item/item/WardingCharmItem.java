package frostnox.nightfall.item.item;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.client.model.entity.AttachedEntityModel;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class WardingCharmItem extends NecklaceItem {
    public WardingCharmItem(Properties properties) {
        super(ImmutableMultimap.of(), properties);
    }

    @Override
    public void animate(AttachedEntityModel model, ItemStack itemStack, PoseStack poseStack, MultiBufferSource buffer, int light, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        float tick = entity.tickCount % 200 + partialTick;
        if(tick < 9) {
            float rot = (entity.tickCount / 200) % 2 == 0 ? 0.08F : -0.08F;
            if(tick < 5) model.child.zRot = Easing.outQuart.apply(tick / 4F) * rot;
            else model.child.zRot = rot * Easing.outSine.apply(1F - (tick - 4) / 5F);
        }
        else model.child.zRot = 0;
        super.animate(model, itemStack, poseStack, buffer, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
    }
}
