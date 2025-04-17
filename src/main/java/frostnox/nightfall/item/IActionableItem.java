package frostnox.nightfall.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public interface IActionableItem {
    boolean hasAction(ResourceLocation id, Player player);
}