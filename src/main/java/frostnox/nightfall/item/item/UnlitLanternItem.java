package frostnox.nightfall.item.item;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.client.model.entity.AttachedEntityModel;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.item.client.IAnimatedEquipment;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class UnlitLanternItem extends IgnitableBlockItem implements IAnimatedEquipment {
    public UnlitLanternItem(Supplier<? extends Item> ignitedItem, Block block, Properties properties) {
        super(ignitedItem, block, properties);
    }

    @Override
    public void animate(AttachedEntityModel model, ItemStack itemStack, PoseStack poseStack, MultiBufferSource buffer, int light, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        model.child.zRot = -0.2618F - Math.abs(Mth.cos(limbSwing * 0.3331F + MathUtil.PI/6F) * limbSwingAmount * 0.3F);
        ItemStack leggings = entity.getItemBySlot(EquipmentSlot.LEGS);
        if(!leggings.isEmpty() && !leggings.is(TagsNF.WAIST_OFFSET_NONE)) model.child.x = leggings.is(TagsNF.WAIST_OFFSET_EXTRA) ? 5.5F : 5F;
        else model.child.x = 4.5F;
    }
}
