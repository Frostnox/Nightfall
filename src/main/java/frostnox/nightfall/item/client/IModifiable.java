package frostnox.nightfall.item.client;

import frostnox.nightfall.client.gui.screen.item.ModifiableItemScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface IModifiable extends IScreenCache {
    /**
     * Called on the client when the modify key is initially pressed
     * @return screen to open (has to be wrapped in optional or server will try to load the class for unknown reasons)
     */
    Optional<Screen> modifyStartClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand);

    /**
     * Called on the client while the modify key is pressed
     * @param heldTime how long key has been held in ticks
     * @return screen to open
     */
    Optional<Screen> modifyContinueClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand, int heldTime);

    /**
     * Called on the client when the modify key is released
     * @param heldTime how long key has been held in ticks
     */
    void modifyReleaseClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand, int heldTime);

    default int getBackgroundUOffset() {
        return ModifiableItemScreen.DEFAULT_BACKGROUND;
    }
}
