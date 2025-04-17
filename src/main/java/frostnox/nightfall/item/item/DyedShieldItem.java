package frostnox.nightfall.item.item;

import frostnox.nightfall.action.Action;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

public class DyedShieldItem extends ShieldItemNF implements DyeableLeatherItem {
    public DyedShieldItem(float[] defense, float[] absorption, RegistryObject<? extends Action> useAction, Properties properties) {
        super(defense, absorption, useAction, properties);
    }

    @Override
    public int getColor(ItemStack pStack) {
        CompoundTag tag = pStack.getTagElement("display");
        return tag != null && tag.contains("color", 99) ? tag.getInt("color") : 0xffffff;
    }
}
