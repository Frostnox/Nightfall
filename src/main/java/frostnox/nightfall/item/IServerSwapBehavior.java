package frostnox.nightfall.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IServerSwapBehavior {
    void swapToServer(ItemStack item, Player player, boolean mainHand);

    void swapFromServer(ItemStack item, Player player, boolean mainHand);
}