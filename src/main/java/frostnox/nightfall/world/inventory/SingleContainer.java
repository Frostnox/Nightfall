package frostnox.nightfall.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SingleContainer implements Container {
    private ItemStack item = ItemStack.EMPTY;

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return item.isEmpty();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return item;
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        ItemStack oldItem = item;
        item = ItemStack.EMPTY;
        return oldItem;
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return removeItem(pSlot, 1);
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        item = pStack;
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        item = ItemStack.EMPTY;
    }
}
