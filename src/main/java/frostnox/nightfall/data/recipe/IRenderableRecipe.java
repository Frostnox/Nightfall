package frostnox.nightfall.data.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

public interface IRenderableRecipe {
    /**
     * Draws the layout of the recipe. This should include the input(s), the output(s), and a background.
     */
    void render(PoseStack poseStack, Screen screen, int mouseX, int mouseY, float partial, int xOffset, int yOffset);

    ItemStack clickItem(Screen screen, int mouseX, int mouseY);

    TranslatableComponent getTitle();

    boolean showInRecipeViewer();

    default boolean showInEntry() {
        return true;
    }

    default boolean onItem(int x, int y, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }
}
