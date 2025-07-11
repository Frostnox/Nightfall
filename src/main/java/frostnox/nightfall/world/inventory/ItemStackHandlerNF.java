package frostnox.nightfall.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

/**
 * More utility functions and a bug fix
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

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if(amount == 0) return ItemStack.EMPTY;
        validateSlotIndex(slot);
        ItemStack existing = stacks.get(slot);
        if(existing.isEmpty()) return ItemStack.EMPTY;
        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if(existing.getCount() <= toExtract) {
            if(!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            }
            else {
                //Fake non-simulated update because quick move will never perform a non-simulated move for some reason
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                stacks.set(slot, existing);
                return existing.copy();
            }
        }
        else if(!simulate) {
            stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
            onContentsChanged(slot);
        }
        else {
            //Fake non-simulated update because quick move will never perform a non-simulated move for some reason
            stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
            onContentsChanged(slot);
            stacks.set(slot, existing);
        }
        return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
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
