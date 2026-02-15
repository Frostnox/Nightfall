package frostnox.nightfall.item.client;

import javax.annotation.Nullable;

public interface IScreenCache {
    @Nullable
    Object getLastUsedObject();

    int getLastUsedPage();

    void setLastUsedObject(Object object);

    void setLastUsedPage(int page);
}
