package frostnox.nightfall.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.ItemStackHandler;

/**
 * More utility functions
 */
public class ItemStackHandlerNF extends ItemStackHandler {
    public ItemStackHandlerNF() {
        super();
    }

    public ItemStackHandlerNF(int size) {
        super(size);
    }

    public ItemStackHandlerNF(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    public ItemStackHandlerNF(ItemStack item) {
        this(NonNullList.of(ItemStack.EMPTY, item));
    }

    public boolean contains(Ingredient ingredient) {
        for(ItemStack stack : stacks) if(ingredient.test(stack)) return true;
        return false;
    }

    public void clear() {
        stacks.clear();
        for(int slot = 0; slot < getSlots(); slot++) {
            stacks.set(slot, ItemStack.EMPTY);
            onContentsChanged(slot);
        }
    }

    public void transferToPlayer(Player player) {
        if(player instanceof ServerPlayer serverPlayer) {
            if(!player.isAlive() || serverPlayer.hasDisconnected()) {
                for(int i = 0; i < stacks.size(); i++) {
                    player.drop(extractItemNoUpdate(i), false);
                }
            }
            else {
                Inventory inventory = player.getInventory();
                for(int i = 0; i < stacks.size(); i++) {
                    inventory.placeItemBackInInventory(extractItemNoUpdate(i));
                }
                serverPlayer.inventoryMenu.broadcastChanges();
            }
        }
    }

    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    public boolean isFull() {
        return hasItemsUpTo(stacks.size() - 1);
    }

    public boolean hasItemsUpTo(int slot) {
        for(int i = 0; i <= slot; i++) {
            if(stacks.get(i).isEmpty()) return false;
        }
        return true;
    }

    public ItemStack extractItemNoUpdate(int slot) {
        validateSlotIndex(slot);
        ItemStack item = stacks.get(slot);
        stacks.set(slot, ItemStack.EMPTY);
        return item;
    }

    public NonNullList<ItemStack> copyItems() {
        return NonNullList.of(ItemStack.EMPTY, stacks.toArray(new ItemStack[0]));
    }
}
