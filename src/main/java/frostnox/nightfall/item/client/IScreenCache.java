package frostnox.nightfall.item.client;

import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public interface IScreenCache {
    @Nullable Item getLastUsedItem();

    int getLastUsedPage();

    void setLastUsedItem(Item item);

    void setLastUsedPage(int page);
}
