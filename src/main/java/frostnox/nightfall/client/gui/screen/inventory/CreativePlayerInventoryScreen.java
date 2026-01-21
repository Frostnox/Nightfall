package frostnox.nightfall.client.gui.screen.inventory;

import frostnox.nightfall.client.ClientEngine;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;

public class CreativePlayerInventoryScreen extends CreativeModeInventoryScreen {
    public CreativePlayerInventoryScreen(Player pPlayer) {
        super(pPlayer);
    }

    @Override
    protected void refreshSearchResults() {
        super.refreshSearchResults();
        //Remove all vanilla items
        menu.items.removeIf(itemStack -> itemStack.getItem().getRegistryName().getNamespace().equals("minecraft"));
        //Update container slots
        scrollOffs = 0.0F;
        menu.scrollTo(0.0F);
    }

    @Override
    public void selectTab(CreativeModeTab tab) {
        super.selectTab(tab); //This touches a lot of private stuff so just let it run and clear out the menu afterwards
        if(tab == CreativeModeTab.TAB_INVENTORY) {
            AbstractContainerMenu inventoryMenu = minecraft.player.inventoryMenu;
            menu.slots.clear();
            //Adjust the survival inventory to match with the new layout
            for(int index = 0; index < inventoryMenu.slots.size(); ++index) {
                int x;
                int y;
                if(index >= 5 && index <= 8) {
                    int i = index - 5;
                    x = i % 2 == 0 ? 37 : 55;
                    y = i < 2 ? 9 : 31;
                }
                else if(index < 5 || index >= 49) { //Exclude crafting slots
                    x = -2000;
                    y = -2000;
                }
                else if(index == 45) {
                    x = 125;
                    y = 31;
                }
                else if(index >= 46) {
                    int i = index - 46;
                    x = i % 2 == 0 ? 107 : 125;
                    y = i < 2 ? 9 : 31;
                }
                else {
                    int k1 = index - 9;
                    int i2 = k1 % 9;
                    int k2 = k1 / 9;
                    x = 9 + i2 * 18;
                    if(index >= 36) y = 112;
                    else y = 54 + k2 * 18;
                }
                Slot slot = new CreativeModeInventoryScreen.SlotWrapper(inventoryMenu.slots.get(index), index, x, y);
                menu.slots.add(slot);
            }
            menu.slots.add(destroyItemSlot);
        }
    }
}
