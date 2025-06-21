package frostnox.nightfall.world.inventory;

import frostnox.nightfall.block.block.MenuContainerBlockEntity;
import frostnox.nightfall.block.block.barrel.BarrelBlockEntityNF;
import frostnox.nightfall.block.block.cauldron.CauldronBlockEntity;
import frostnox.nightfall.block.block.pot.PotBlockEntity;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.ContainersNF;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public class StorageContainer extends PartialInventoryContainer {
    public final MenuContainerBlockEntity entity;
    public interface ISlotFactory {
        Slot create(ItemStackHandlerNF inventory, StorageContainer menu, Player player, int index, int xPosition, int yPosition);
    }

    protected StorageContainer(@Nullable MenuType<?> pMenuType, Inventory playerInv, int pContainerId, MenuContainerBlockEntity entity,
                               int rows, int columns, int xOff, int yOff) {
        this(pMenuType, playerInv, pContainerId, entity,
                (inventory, menu, player, index, xPosition, yPosition) -> new SlotItemHandler(inventory, index, xPosition, yPosition),
                rows, columns, xOff, yOff, 8, 84, false);
    }

    protected StorageContainer(@Nullable MenuType<?> pMenuType, Inventory playerInv, int pContainerId, MenuContainerBlockEntity entity, ISlotFactory slotFactory,
                               int rows, int columns, int xOff, int yOff) {
        this(pMenuType, playerInv, pContainerId, entity, slotFactory, rows, columns, xOff, yOff, 8, 84, false);
    }

    protected StorageContainer(@Nullable MenuType<?> pMenuType, Inventory playerInv, int pContainerId, MenuContainerBlockEntity entity, ISlotFactory slotFactory,
                               int rows, int columns, int xOff, int yOff, int invXOff, int invYOff, boolean quickMoveSingleItem) {
        super(pMenuType, playerInv, pContainerId, invXOff, invYOff, quickMoveSingleItem);
        this.entity = entity;
        ItemStackHandlerNF inv = entity.getInventory();
        for(int r = 0; r < rows; ++r) {
            for(int c = 0; c < columns; ++c) {
                addSlot(slotFactory.create(inv, this, playerInv.player, c + r * columns, xOff + c * 18, yOff + r * 18));
            }
        }
        entity.startOpen(playerInv.player);
        if(entity.getData() != null) addDataSlots(entity.getData());
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        entity.stopOpen(pPlayer);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return entity.stillValid(pPlayer);
    }

    public static StorageContainer createBarrelContainer(int windowID, Inventory playerInv, FriendlyByteBuf extraData) {
        return createBarrelContainer(windowID, playerInv, playerInv.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public static StorageContainer createBarrelContainer(int windowID, Inventory playerInv, BlockEntity entity) {
        if(entity instanceof BarrelBlockEntityNF containerEntity) return new StorageContainer(ContainersNF.BARREL.get(), playerInv, windowID, containerEntity,
                (inventory, menu, player, index, xPosition, yPosition) -> new FluidSlot(inventory, menu, player, index, xPosition, yPosition, false),
                BarrelBlockEntityNF.ROWS, BarrelBlockEntityNF.COLUMNS, 62, 28, 8, 84 + 20, false);
        else return createContainerException(entity);
    }

    public static StorageContainer createCauldronContainer(int windowID, Inventory playerInv, FriendlyByteBuf extraData) {
        return createCauldronContainer(windowID, playerInv, playerInv.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public static StorageContainer createCauldronContainer(int windowID, Inventory playerInv, BlockEntity entity) {
        if(entity instanceof CauldronBlockEntity containerEntity) return new StorageContainer(ContainersNF.CAULDRON.get(), playerInv, windowID, containerEntity,
                ((inventory, menu, player, index, xPosition, yPosition) -> new FluidSlot(inventory, menu, player, index, xPosition, yPosition, true) {
                    @Override
                    public boolean mayPlace(ItemStack pStack) {
                        return pStack.is(TagsNF.FOOD_INGREDIENT) || super.mayPlace(pStack);
                    }

                    @Override
                    public int getMaxStackSize(ItemStack pStack) {
                        return 1;
                    }

                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }

                    @Override
                    public boolean isActive() {
                        return !containerEntity.hasMeal();
                    }
                }), 2, 2, 71, 19);
        else return createContainerException(entity);
    }

    public static StorageContainer createPotContainer(int windowID, Inventory playerInv, FriendlyByteBuf extraData) {
        return createPotContainer(windowID, playerInv, playerInv.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public static StorageContainer createPotContainer(int windowID, Inventory playerInv, BlockEntity entity) {
        if(entity instanceof PotBlockEntity containerEntity) return new StorageContainer(ContainersNF.POT.get(), playerInv, windowID, containerEntity,
                PotBlockEntity.ROWS, PotBlockEntity.COLUMNS, 53, 8);
        else return createContainerException(entity);
    }

    private static StorageContainer createContainerException(@Nullable BlockEntity entity) {
        if(entity == null) throw new IllegalStateException("Null storage container entity");
        else throw new IllegalStateException("Invalid storage container entity " + entity.getType() + " at " + entity.getBlockPos());
    }
}
