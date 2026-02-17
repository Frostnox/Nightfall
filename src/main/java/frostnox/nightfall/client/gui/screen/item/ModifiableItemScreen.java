package frostnox.nightfall.client.gui.screen.item;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.item.client.IModifiable;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ModifiableItemScreen extends ModifiableScreen<ItemStack> {
    public ModifiableItemScreen(int radius, int slices, boolean mainHand, IModifiable modifiableItem, List<ItemStack> items) {
        super(radius, slices, mainHand, modifiableItem, items);
    }

    @Override
    protected void renderObject(ItemStack object, PoseStack poseStack, int xPos, int yPos) {
        mc.getItemRenderer().renderAndDecorateItem(object, xPos, yPos);
        mc.getItemRenderer().renderGuiItemDecorations(mc.font, object, xPos, yPos);
    }

    @Override
    protected void renderObjectTooltip(ItemStack object, PoseStack poseStack, int mouseX, int mouseY) {
        renderTooltip(poseStack, object, mouseX, mouseY);
    }

    @Override
    protected Object getObjectForLastUse(int i) {
        return objects.get(i).getItem();
    }

    protected void drawHoverTooltip(PoseStack stack, ItemStack item, int mouseX, int mouseY, int x, int y) {
        if(!item.isEmpty() && mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16) {
            renderTooltip(stack, item, mouseX, mouseY);
        }
    }
}
