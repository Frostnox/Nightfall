package frostnox.nightfall.world.inventory;

import frostnox.nightfall.item.item.EmptyBucketItem;
import frostnox.nightfall.item.item.FilledBucketItem;
import frostnox.nightfall.registry.forge.FluidsNF;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FluidSlot extends SlotItemHandler {
    protected final AbstractContainerMenu menu;
    protected final Player player;
    protected final int index;
    protected final boolean fluidOnly;
    private static boolean inInsert;

    public FluidSlot(IItemHandler itemHandler, AbstractContainerMenu menu, Player player, int index, int xPosition, int yPosition, boolean fluidOnly) {
        super(itemHandler, index, xPosition, yPosition);
        this.menu = menu;
        this.player = player;
        this.index = index;
        this.fluidOnly = fluidOnly;
    }

    public Fluid getFluid() {
        return FluidsNF.getAsFluid(getItem().getItem());
    }

    public boolean addFluid(Fluid fluid) {
        if(!hasItem()) {
            Item item = FluidsNF.getAsItem(fluid);
            if(item != null) {
                set(new ItemStack(item));
                return true;
            }
        }
        else {
            ItemStack stack = getItem();
            if(stack.getCount() < getMaxStackSize(stack)) {
                stack.grow(1);
                set(stack);
                return true;
            }
        }
        return false;
    }

    public boolean removeFluid(Fluid fluid) {
        if(fluid != FluidsNF.getAsFluid(getItem().getItem())) return false;
        else {
            remove(1);
            return true;
        }
    }

    @Override
    public void onTake(Player pPlayer, ItemStack pStack) {
        super.onTake(pPlayer, pStack);
        //Remove fluid when moved out indirectly using hotkeys or quick move
        if(FluidsNF.getAsFluid(pStack.getItem()) != Fluids.EMPTY) pStack.setCount(0);
    }

    @Override
    public boolean mayPlace(ItemStack pStack) {
        Fluid fluid = getFluid();
        if(pStack.getItem() instanceof EmptyBucketItem bucket) return fluid != Fluids.EMPTY && FluidsNF.getFilledBucket(bucket, fluid) != null;
        else if(pStack.getItem() instanceof FilledBucketItem bucket) {
            ItemStack item = getItem();
            return item.isEmpty() || (bucket.getFluid() == fluid && item.getCount() < Math.min(getMaxStackSize(), item.getMaxStackSize()));
        }
        else return !fluidOnly;
    }

    @Override
    public void set(ItemStack pStack) {
        Fluid fluid = getFluid();
        if(pStack.getItem() instanceof EmptyBucketItem emptyBucket) {
            ItemStack filledBucket = FluidsNF.getFilledBucket(emptyBucket, fluid);
            if(!filledBucket.isEmpty() && removeFluid(fluid)) {
                if(pStack.getCount() > 1) {
                    pStack.shrink(1);
                    player.getInventory().placeItemBackInInventory(filledBucket);
                    menu.setCarried(pStack);
                }
                else {
                    pStack = filledBucket;
                    menu.setCarried(pStack);
                }
                return;
            }
        }
        else if(pStack.getItem() instanceof FilledBucketItem filledBucket) {
            if(addFluid(filledBucket.getFluid())) {
                pStack.shrink(1);
                //Add bucket back to inventory if it wasn't carried
                pStack = filledBucket.getContainerItem(pStack);
                if(!inInsert && menu.getCarried().isEmpty()) {
                    player.getInventory().placeItemBackInInventory(pStack);
                }
                else menu.setCarried(filledBucket.getContainerItem(pStack));
                return;
            }
        }
        super.set(pStack);
    }

    @Override
    public ItemStack safeInsert(ItemStack pStack, int pIncrement) {
        inInsert = true;
        ItemStack originalCarry = menu.getCarried().copy();
        ItemStack newCarry = super.safeInsert(pStack, pIncrement);
        inInsert = false;
        if(originalCarry.getItem() instanceof FilledBucketItem filledBucket && !originalCarry.equals(newCarry, false)) {
            return filledBucket.getContainerItem(originalCarry);
        }
        else return newCarry;
    }
}
