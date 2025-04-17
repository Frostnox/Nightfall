package frostnox.nightfall.item.item;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.client.model.entity.AttachedEntityModel;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.item.client.IAnimatedEquipment;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class NecklaceItem extends AttributeAccessoryItem implements IAnimatedEquipment {
    public NecklaceItem(ImmutableMultimap<Supplier<Attribute>, AttributeModifier> modifiers, Properties properties) {
        super(modifiers, properties);
    }

    @Override
    public void animate(AttachedEntityModel model, ItemStack itemStack, PoseStack poseStack, MultiBufferSource buffer, int light, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if(entity instanceof Player player) {
            float crouch = AnimationUtil.getCrouchProgress(player, partialTick);
            if(crouch > 0F) {
                model.child.xRot = crouch * -MathUtil.PI / 8F;
                model.child.y = -12.5F + crouch;
            }
            else {
                model.child.z = -0.5F * player.getSwimAmount(partialTick);
                model.child.y = -12.5F;
                model.child.xRot = 0;
            }
        }
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        float scale = chest.isEmpty() || chest.is(TagsNF.NECK_OFFSET_NONE) ? 1F : (chest.is(TagsNF.NECK_OFFSET_EXTRA) ? 1.7F : 1.375F);
        poseStack.scale(1, 1, scale);
    }
}
