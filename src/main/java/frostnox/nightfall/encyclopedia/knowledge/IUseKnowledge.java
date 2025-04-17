package frostnox.nightfall.encyclopedia.knowledge;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IUseKnowledge {
    void onUseItem(Player player, ItemStack item);
}
