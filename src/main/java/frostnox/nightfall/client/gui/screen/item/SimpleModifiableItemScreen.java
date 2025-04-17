package frostnox.nightfall.client.gui.screen.item;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.item.client.IModifiable;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SimpleModifiableItemScreen extends ModifiableItemScreen {
    public SimpleModifiableItemScreen(boolean mainHand, IModifiable modifiableItem, List<ItemStack> items) {
        super(64, 12, mainHand, modifiableItem, items);
        updatePages(modifiableItem, items.size());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
        super.render(poseStack, mouseX, mouseY, partial);
        if(pageCount > 1) {
            String pageText = page + "/" + pageCount;
            RenderUtil.drawCenteredText(poseStack, mc.font, pageText, width/2, height/2, RenderUtil.COLOR_WHITE, true, LightTexture.FULL_BRIGHT);
        }
    }
}
