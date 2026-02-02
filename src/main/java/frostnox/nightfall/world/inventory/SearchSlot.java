package frostnox.nightfall.world.inventory;

import frostnox.nightfall.client.ClientEngine;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SearchSlot extends Slot {
    private final boolean onClient;

    public SearchSlot(Container pContainer, boolean onClient, int index, int xPosition, int yPosition) {
        super(pContainer, index, xPosition, yPosition);
        this.onClient = onClient;
    }

    @Override
    public void onTake(Player player, ItemStack pStack) {
        super.onTake(player, pStack);
        if(onClient) ClientEngine.get().updateRecipeSearchItems();
    }

    @Override
    public void set(ItemStack pStack) {
        super.set(pStack);
        if(onClient) ClientEngine.get().updateRecipeSearchItems();
    }

    @Override
    public boolean mayPlace(ItemStack item) {
        return isActive();
    }

    @Override
    public boolean isActive() {
        return onClient ? ClientEngine.get().isRecipeSearchOpen() : true;
    }
}
