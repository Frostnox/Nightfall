package frostnox.nightfall.item.item;

import frostnox.nightfall.item.ITieredArmorMaterial;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public class DyeableTieredArmorItem extends TieredArmorItem implements DyeableLeatherItem {
    public DyeableTieredArmorItem(ITieredArmorMaterial material, EquipmentSlot slot, Properties builder) {
        super(material, slot, builder);
    }

    @Override
    public int getColor(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTagElement("display");
        return compoundtag != null && compoundtag.contains("color", 99) ? compoundtag.getInt("color") : material.getDefaultColor();
    }
}
