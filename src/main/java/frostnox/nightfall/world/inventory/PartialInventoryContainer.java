package frostnox.nightfall.world.inventory;

import frostnox.nightfall.item.item.FilledBucketItem;
import frostnox.nightfall.registry.forge.FluidsNF;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public abstract class PartialInventoryContainer extends AbstractContainerMenu {
    protected final boolean quickMoveSingleItem;

    protected PartialInventoryContainer(@Nullable MenuType<?> pMenuType, Inventory playerInv, int pContainerId, boolean quickMoveSingleItem) {
        this(pMenuType, playerInv, pContainerId, 8, 84, quickMoveSingleItem);
    }

    protected PartialInventoryContainer(@Nullable MenuType<?> pMenuType, Inventory playerInv, int pContainerId, int xOff, int yOff, boolean quickMoveSingleItem) {
        super(pMenuType, pContainerId);
        //Player inventory
        for(int r = 0; r < 3; ++r) {
            for(int c = 0; c < 9; ++c) {
                if(c < 5) addSlot(new Slot(playerInv, c + r * 9 + 9, xOff + c * 18, yOff + r * 18));
                else addSlot(new CapacitySlot(r * 4 + c - 5, playerInv, c + r * 9 + 9, xOff + c * 18, yOff + r * 18));
            }
        }
        //Hotbar
        for(int c = 0; c < 9; ++c) {
            addSlot(new Slot(playerInv, c, xOff + c * 18, yOff + 58));
        }
        this.quickMoveSingleItem = quickMoveSingleItem;
    }

    protected boolean moveBucketTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
        boolean flag = false;
        int i = pStartIndex;
        if (pReverseDirection) {
            i = pEndIndex - 1;
        }

        //Try stacking into a fluid slot first
        while(!pStack.isEmpty()) {
            if (pReverseDirection) {
                if (i < pStartIndex) {
                    break;
                }
            } else if (i >= pEndIndex) {
                break;
            }

            Slot slot = this.slots.get(i);
            if(slot instanceof FluidSlot && slot.mayPlace(pStack)) slot.set(pStack);

            if (pReverseDirection) {
                --i;
            } else {
                ++i;
            }
        }

        if (!pStack.isEmpty()) {
            if (pReverseDirection) {
                i = pEndIndex - 1;
            } else {
                i = pStartIndex;
            }

            while(true) {
                if (pReverseDirection) {
                    if (i < pStartIndex) {
                        break;
                    }
                } else if (i >= pEndIndex) {
                    break;
                }

                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(pStack)) {
                    if (pStack.getCount() > slot1.getMaxStackSize()) {
                        slot1.set(pStack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.set(pStack.split(pStack.getCount()));
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (pReverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemCopy = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if(slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemCopy = slotItem.copy();
            slot.onTake(player, slotItem);
            if(slotItem.isEmpty()) return ItemStack.EMPTY;
            if(index < 36) {
                if(slotItem.getItem() instanceof FilledBucketItem) {
                    if(!moveBucketTo(slotItem, 36, slots.size(), false)) return ItemStack.EMPTY;
                    else if(quickMoveSingleItem) return ItemStack.EMPTY;
                }
                else {
                    if(!moveItemStackTo(slotItem, 36, slots.size(), false)) return ItemStack.EMPTY;
                    else if(quickMoveSingleItem) return ItemStack.EMPTY;
                }
            }
            else if(!moveItemStackTo(slotItem, 0, 36, false)) return ItemStack.EMPTY;
            slot.setChanged();
        }
        return itemCopy;
    }

    @Override
    public void setCarried(ItemStack pStack) {
        //Prevent carrying of fluid items through clone action
        if(FluidsNF.getAsFluid(pStack.getItem()) == Fluids.EMPTY) super.setCarried(pStack);
    }
}
