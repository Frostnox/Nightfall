package frostnox.nightfall.encyclopedia.knowledge;

import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemTagKnowledge extends ItemKnowledge {
    private final TagKey<Item> tag;

    public ItemTagKnowledge(Item item, TagKey<Item> tag) {
        super(item);
        this.tag = tag;
    }

    @Override
    public void onPickedUpItem(Player player, ItemStack item) {
        IPlayerData encyclopedia = PlayerData.get(player);
        if(!encyclopedia.hasKnowledge(this.getRegistryName()) && item.is(tag)) encyclopedia.addKnowledge(this.getRegistryName());
    }
}
