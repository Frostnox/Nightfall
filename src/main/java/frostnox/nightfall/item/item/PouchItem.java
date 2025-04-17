package frostnox.nightfall.item.item;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.client.model.entity.AttachedEntityModel;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.item.client.IAnimatedEquipment;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class PouchItem extends AttributeAccessoryItem implements DyeableLeatherItem, IAnimatedEquipment {
    public PouchItem(ImmutableMultimap<Supplier<Attribute>, AttributeModifier> modifiers, Properties properties) {
        super(modifiers, properties);
    }

    @Override
    public int getColor(ItemStack pStack) {
        CompoundTag tag = pStack.getTagElement("display");
        return tag != null && tag.contains("color", 99) ? tag.getInt("color") : RenderUtil.COLOR_LINEN;
    }

    @Override
    public void animate(AttachedEntityModel model, ItemStack itemStack, PoseStack poseStack, MultiBufferSource buffer, int light, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        model.child.zRot = -0.2618F - Math.abs(Mth.cos(limbSwing * 0.3331F + MathUtil.PI/6F) * limbSwingAmount * 0.2F);
        ItemStack leggings = entity.getItemBySlot(EquipmentSlot.LEGS);
        if(!leggings.isEmpty() && !leggings.is(TagsNF.WAIST_OFFSET_NONE)) model.child.x = leggings.is(TagsNF.WAIST_OFFSET_EXTRA) ? 5.5F : 5F;
        else model.child.x = 4.5F;
    }
}
