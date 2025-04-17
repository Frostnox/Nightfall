package frostnox.nightfall.item.item;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class DyeableAttributeEquipmentItem extends AttributeEquipmentItem implements DyeableLeatherItem {
    public final int defaultColor;

    public DyeableAttributeEquipmentItem(EquipmentSlot slot, int defaultColor, ImmutableMultimap<Supplier<Attribute>, AttributeModifier> modifiers, Properties properties) {
        super(slot, modifiers, properties);
        this.defaultColor = defaultColor;
    }

    @Override
    public int getColor(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTagElement("display");
        return compoundtag != null && compoundtag.contains("color", 99) ? compoundtag.getInt("color") : defaultColor;
    }
}
