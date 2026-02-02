package frostnox.nightfall.mixin;

import frostnox.nightfall.item.IContainerChanger;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Shadow @Final private NonNullList<Slot> slots;

    @Inject(method = "doClick", at = @At("HEAD"))
    private void nightfall$doClick(int pSlotId, int pButton, ClickType pClickType, Player pPlayer, CallbackInfo callbackInfo) {
        if(!pPlayer.level.isClientSide && pSlotId > 0) {
            Slot slot = slots.get(pSlotId);
            ItemStack stack = slot.getItem();
            if(stack.getItem() instanceof IContainerChanger changer) {
                changer.containerChanged(stack);
                slot.setChanged();
            }
        }
    }
}
