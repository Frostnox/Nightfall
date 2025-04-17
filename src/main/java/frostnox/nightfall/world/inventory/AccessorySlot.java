package frostnox.nightfall.world.inventory;

import frostnox.nightfall.data.TagsNF;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Locale;

public enum AccessorySlot {
    FACE(TagsNF.ACCESSORY_FACE),
    NECK(TagsNF.ACCESSORY_NECK),
    WAIST(TagsNF.ACCESSORY_WAIST);

    public final TagKey<Item> acceptedItems;

    AccessorySlot(TagKey<Item> acceptedItems) {
        this.acceptedItems = acceptedItems;
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean acceptsItem(ItemStack item) {
        return item.is(acceptedItems);
    }

    public static @Nullable AccessorySlot getSlotFor(ItemStack item) {
        if(FACE.acceptsItem(item)) return FACE;
        else if(NECK.acceptsItem(item)) return NECK;
        else if(WAIST.acceptsItem(item)) return WAIST;
        else return null;
    }
}
