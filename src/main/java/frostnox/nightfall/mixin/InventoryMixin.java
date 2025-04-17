package frostnox.nightfall.mixin;

import frostnox.nightfall.registry.forge.AttributesNF;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Support for expandable inventory slots.
 */
@Mixin(Inventory.class)
public abstract class InventoryMixin implements Container, Nameable {
    @Shadow @Final public NonNullList<ItemStack> items;
    @Shadow @Final public Player player;
    @Shadow public int selected;
    @Shadow protected abstract boolean hasRemainingSpaceForItem(ItemStack pDestination, ItemStack pOrigin);

    /**
     * @author Frostnox
     * @reason Avoid filling into slots that are no longer active but haven't had their items moved out yet
     */
    @Overwrite
    public int getSlotWithRemainingSpace(ItemStack pStack) {
        if(hasRemainingSpaceForItem(getItem(selected), pStack)) return selected;
        else if(hasRemainingSpaceForItem(getItem(40), pStack)) return 40;
        else {
            int capacity = AttributesNF.getInventoryCapacity(player);
            for(int i = 0; i < items.size(); i++) {
                if(i >= 9 && i <= 35) {
                    int mod = (i - 4) % 9;
                    if(mod <= 4 && mod > 0) {
                        int adjustedIndex = (i / 9 - 1) * 4 + mod;
                        if(adjustedIndex > capacity) {
                            i += 4 - mod;
                            continue;
                        }
                    }
                }
                if(hasRemainingSpaceForItem(items.get(i), pStack)) return i;
            }
            return -1;
        }
    }

    /**
     * @author Frostnox
     * @reason Account for expandable slots that might not be accepting items
     */
    @Overwrite
    public int getFreeSlot() {
        for(int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            //Check for creative to avoid touching client-only classes on server
            //Check that slot allows item with converted index (inventory to menu)
            if(item.isEmpty() && (player.isCreative() || player.inventoryMenu.getSlot(i < 9 ? i + 36 : i).mayPlace(item))) {
                return i;
            }
        }
        return -1;
    }
}
