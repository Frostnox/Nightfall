package frostnox.nightfall.client.gui.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.data.recipe.TieredAnvilRecipe;
import frostnox.nightfall.item.client.IModifiable;
import frostnox.nightfall.util.DataUtil;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class TongsVisualRecipeScreen extends ModifiableScreen<TieredAnvilRecipe> {
    public TongsVisualRecipeScreen(boolean mainHand, IModifiable modifiableItem, List<TieredAnvilRecipe> items) {
        super(64, 12, mainHand, modifiableItem, items);
    }

    @Override
    protected void renderObject(TieredAnvilRecipe object, PoseStack poseStack, int xPos, int yPos) {
        if(object != null) {
            mc.getItemRenderer().renderAndDecorateItem(object.getResultItem(), xPos, yPos);
            mc.getItemRenderer().renderGuiItemDecorations(mc.font, object.getResultItem(), xPos, yPos);
        }
        else {
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            RenderUtil.renderSprite(poseStack, xPos, yPos, DataUtil.NULL_ICON);
        }
    }

    @Override
    protected void renderObjectTooltip(TieredAnvilRecipe object, PoseStack poseStack, int mouseX, int mouseY) {
        if(object != null) renderTooltip(poseStack, object.getResultItem(), mouseX, mouseY);
        else renderTooltip(poseStack, new TranslatableComponent("gui.empty"), mouseX, mouseY);
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
