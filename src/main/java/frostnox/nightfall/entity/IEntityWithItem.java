package frostnox.nightfall.entity;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public interface IEntityWithItem {
    Item getItemForm();

    ItemStack getPickedResult(HitResult target);
}
