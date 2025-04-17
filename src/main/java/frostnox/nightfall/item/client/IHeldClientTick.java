package frostnox.nightfall.item.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IHeldClientTick {
    void onHeldTickClient(Minecraft mc, ItemStack item, Player player, boolean mainHand);
}
