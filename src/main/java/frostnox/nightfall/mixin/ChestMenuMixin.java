package frostnox.nightfall.mixin;

import frostnox.nightfall.registry.forge.ContainersNF;
import frostnox.nightfall.world.inventory.CapacitySlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestMenu.class)
public abstract class ChestMenuMixin extends AbstractContainerMenu {
    protected ChestMenuMixin(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;I)V", at = @At("TAIL"))
    private void nightfall$alterInventorySlots(MenuType<?> pType, int pContainerId, Inventory pPlayerInventory, Container pContainer, int pRows, CallbackInfo callbackInfo) {
        int yOff = (pRows - 4) * 18;
        int chestSize = pRows * 9;
        for(int i = 0; i < chestSize; i++) {
            Slot slot = slots.get(i);
            Slot newSlot = new Slot(pContainer, i, slot.x, slot.y - 10);
            newSlot.index = slot.index;
            slots.set(i, newSlot);
        }
        for(int r = 0; r < 3; r++) {
            for(int c = 0; c < 9; c++) {
                int position = c + r * 9;
                int index = chestSize + position;
                Slot slot;
                if(c >= 5) slot = new CapacitySlot(r * 4 + c - 5, pPlayerInventory, position + 9, 8 + c * 18, 84 + r * 18 + yOff);
                else slot = new Slot(pPlayerInventory, position + 9, 8 + c * 18, 84 + r * 18 + yOff);
                slot.index = index;
                slots.set(index, slot);
            }
        }
        for(int i = 0; i < 9; ++i) {
            Slot newSlot = new Slot(pPlayerInventory, i, 8 + i * 18, 142 + yOff);
            newSlot.index = chestSize + 27 + i;
            slots.set(newSlot.index, newSlot);
        }
    }

    /**
     * @author Frostnox
     * @reason Replace vanilla screen to support inventory changes
     */
    @Overwrite
    public static ChestMenu threeRows(int pContainerId, Inventory pPlayerInventory, Container pContainer) {
        return new ChestMenu(ContainersNF.CHEST_9x3.get(), pContainerId, pPlayerInventory, pContainer, 3);
    }

    /**
     * @author Frostnox
     * @reason Replace vanilla screen to support inventory changes
     */
    @Overwrite
    public static ChestMenu sixRows(int pContainerId, Inventory pPlayerInventory, Container pContainer) {
        return new ChestMenu(ContainersNF.CHEST_9x6.get(), pContainerId, pPlayerInventory, pContainer, 6);
    }
}
