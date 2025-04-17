package frostnox.nightfall.block.block.crucible;

import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.ContainersNF;
import frostnox.nightfall.world.inventory.FluidSlot;
import frostnox.nightfall.world.inventory.PartialInventoryContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;

public class CrucibleContainer extends PartialInventoryContainer {
    public final CrucibleBlockEntity entity;
    private final ContainerLevelAccess access;
    private final ItemStackHandler inventory;

    public CrucibleContainer(int windowID, Inventory playerInv, CrucibleBlockEntity entity) {
        super(ContainersNF.CRUCIBLE.get(), playerInv, windowID, false);
        this.entity = entity;
        this.access = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());
        this.inventory = entity.getInventory();
        addSlot(new FluidSlot(inventory, this, playerInv.player, 0, 61, 26, false));
        addSlot(new FluidSlot(inventory, this, playerInv.player, 1, 79, 26, false));
        addSlot(new FluidSlot(inventory, this, playerInv.player, 2, 61, 44, false));
        addSlot(new FluidSlot(inventory, this, playerInv.player, 3, 79, 44, false));

        addDataSlots(entity.data);
        entity.startOpen(playerInv.player);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public static CrucibleContainer createClientContainer(int windowID, Inventory playerInv, FriendlyByteBuf extraData) {
        BlockEntity entity = playerInv.player.level.getBlockEntity(extraData.readBlockPos());
        if(entity instanceof CrucibleBlockEntity crucibleEntity) return new CrucibleContainer(windowID, playerInv, crucibleEntity);
        else throw new IllegalStateException("Crucible block entity does not exist at " + extraData.readBlockPos());
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, BlocksNF.CRUCIBLE.get());
    }
}
