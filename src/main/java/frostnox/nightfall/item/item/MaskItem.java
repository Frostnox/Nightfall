package frostnox.nightfall.item.item;

import com.google.common.collect.ImmutableMultimap;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class MaskItem extends AttributeAccessoryItem implements DyeableLeatherItem {
    public MaskItem(ImmutableMultimap<Supplier<Attribute>, AttributeModifier> modifiers, Properties properties) {
        super(modifiers, properties);
    }

    @Override
    public int getColor(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTagElement("display");
        return compoundtag != null && compoundtag.contains("color", 99) ? compoundtag.getInt("color") : RenderUtil.COLOR_LINEN;
    }
}
