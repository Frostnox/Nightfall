package frostnox.nightfall.block;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * For block entities that hold ItemStacks
 */
public interface IDropsItems {
    NonNullList<ItemStack> getContainerDrops();

    default boolean dropOnFall() {
        return false;
    }
}
