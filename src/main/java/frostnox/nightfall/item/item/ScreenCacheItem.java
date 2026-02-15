package frostnox.nightfall.item.item;

import frostnox.nightfall.item.client.IScreenCache;
import net.minecraft.world.item.Item;

public class ScreenCacheItem extends ItemNF implements IScreenCache {
    private Object lastUsedItem;
    private int lastUsedPage = 1;

    public ScreenCacheItem(Properties properties) {
        super(properties);
    }

    @Override
    public Object getLastUsedObject() {
        return lastUsedItem;
    }

    @Override
    public int getLastUsedPage() {
        return lastUsedPage;
    }

    @Override
    public void setLastUsedObject(Object object) {
        lastUsedItem = object;
    }

    @Override
    public void setLastUsedPage(int page) {
        lastUsedPage = page;
    }
}
