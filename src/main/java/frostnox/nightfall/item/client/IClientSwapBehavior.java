package frostnox.nightfall.item.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IClientSwapBehavior {
    /**
     * Called on the client when the item is first held in hand
     */
    void swapClient(Minecraft mc, ItemStack item, Player player, boolean mainHand);
}
