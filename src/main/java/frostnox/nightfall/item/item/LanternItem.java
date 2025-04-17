package frostnox.nightfall.item.item;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.client.model.entity.AttachedEntityModel;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.item.IItemLightSource;
import frostnox.nightfall.item.client.IAnimatedEquipment;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class LanternItem extends BlockItemNF implements IItemLightSource, IAnimatedEquipment {
    public final Supplier<? extends Item> extinguishedItem;

    public LanternItem(Block block, Supplier<? extends Item> extinguishedItem, Properties properties) {
        super(block, properties);
        this.extinguishedItem = extinguishedItem;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if(entity.isInWater()) LevelUtil.extinguishItemEntity(entity, extinguishedItem.get(), false);
        return false;
    }

    @Override
    public int getBrightness() {
        return 14;
    }

    @Override
    public double getLightRadiusSqr() {
        return 14D * 14D;
    }

    @Override
    public Item getExtinguishedItem() {
        return extinguishedItem.get();
    }

    @Override
    public double getEquippedHeight(Pose pose) {
        if(pose == Pose.STANDING) return 0.55F;
        else if(pose == Pose.CROUCHING) return 0.4F;
        else return 0.1F;
    }

    @Override
    public void animate(AttachedEntityModel model, ItemStack itemStack, PoseStack poseStack, MultiBufferSource buffer, int light, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        model.child.zRot = -0.2618F - Math.abs(Mth.cos(limbSwing * 0.3331F + MathUtil.PI/6F) * limbSwingAmount * 0.3F);
        ItemStack leggings = entity.getItemBySlot(EquipmentSlot.LEGS);
        if(!leggings.isEmpty() && !leggings.is(TagsNF.WAIST_OFFSET_NONE)) model.child.x = leggings.is(TagsNF.WAIST_OFFSET_EXTRA) ? 5.5F : 5F;
        else model.child.x = 4.5F;
    }
}
