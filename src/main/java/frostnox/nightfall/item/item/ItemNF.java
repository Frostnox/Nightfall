package frostnox.nightfall.item.item;

import frostnox.nightfall.util.RenderUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class ItemNF extends Item {
    public ItemNF(Properties properties) {
        super(properties);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return RenderUtil.getItemBarColor(stack);
    }
}
