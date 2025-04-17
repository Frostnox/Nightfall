package frostnox.nightfall.world.inventory;

import frostnox.nightfall.registry.forge.AttributesNF;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CapacitySlot extends Slot {
    private final Player player;
    private final int rank;

    public CapacitySlot(int rank, Inventory pContainer, int pSlot, int x, int pY) {
        super(pContainer, pSlot, x, pY);
        player = pContainer.player;
        this.rank = rank;
    }

    @Override
    public boolean mayPlace(ItemStack item) {
        return isActive();
    }

    @Override
    public boolean isActive() {
        return rank < AttributesNF.getInventoryCapacity(player);
    }
}
