package frostnox.nightfall.item.item;

import frostnox.nightfall.item.client.IScreenCache;
import net.minecraft.world.item.Item;

public class ScreenCacheItem extends ItemNF implements IScreenCache {
    private Item lastUsedItem;
    private int lastUsedPage = 1;

    public ScreenCacheItem(Properties properties) {
        super(properties);
    }

    @Override
    public Item getLastUsedItem() {
        return lastUsedItem;
    }

    @Override
    public int getLastUsedPage() {
        return lastUsedPage;
    }

    @Override
    public void setLastUsedItem(Item item) {
        lastUsedItem = item;
    }

    @Override
    public void setLastUsedPage(int page) {
        lastUsedPage = page;
    }
}
