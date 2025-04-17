package frostnox.nightfall.world.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SingleFluidSlot extends FluidSlot {
    public SingleFluidSlot(IItemHandler itemHandler, AbstractContainerMenu menu, Player player, int index, int xPosition, int yPosition, boolean fluidOnly) {
        super(itemHandler, menu, player, index, xPosition, yPosition, fluidOnly);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack pStack) {
        return 1;
    }
}
