package frostnox.nightfall.encyclopedia.knowledge;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IItemKnowledge {
    Item getItem();

    void onPickedUpItem(Player player, ItemStack item);
}
