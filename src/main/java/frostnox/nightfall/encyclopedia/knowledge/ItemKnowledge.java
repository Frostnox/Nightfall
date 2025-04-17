package frostnox.nightfall.encyclopedia.knowledge;

import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemKnowledge extends Knowledge implements IItemKnowledge {
    private final Item item;

    public ItemKnowledge(Item item) {
        this.item = item;
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public void onPickedUpItem(Player player, ItemStack item) {
        IPlayerData encyclopedia = PlayerData.get(player);
        if(!encyclopedia.hasKnowledge(this.getRegistryName()) && this.item.equals(item.getItem())) encyclopedia.addKnowledge(this.getRegistryName());
    }
}
