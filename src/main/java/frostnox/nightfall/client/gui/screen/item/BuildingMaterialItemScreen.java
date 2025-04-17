package frostnox.nightfall.client.gui.screen.item;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.data.recipe.BuildingRecipe;
import frostnox.nightfall.item.client.IModifiable;
import frostnox.nightfall.item.item.BuildingMaterialItem;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.RenderUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BuildingMaterialItemScreen extends ModifiableItemScreen {
    protected final List<BuildingRecipe> recipes;

    public BuildingMaterialItemScreen(boolean mainHand, IModifiable modifiableItem, List<BuildingRecipe> recipes, List<ItemStack> items) {
        super(64, 12, mainHand, modifiableItem, items);
        this.recipes = recipes;
    }

    public static void initSelection(Minecraft mc, BuildingMaterialItem material, boolean mainHand) {
        List<BuildingRecipe> recipes = material.getRecipes(mc.level, mc.player);
        List<ItemStack> items = new ObjectArrayList<>(recipes.size());
        for(int i = 0; i < recipes.size(); i++) items.add(i, recipes.get(i).getResultItem());
        initSelection(mc, items, material, mainHand);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
        super.render(poseStack, mouseX, mouseY, partial);
        int baseX = width / 2 - 8, baseY = height / 2 - 8;
        int extraX = baseX;
        ItemStack base = ItemStack.EMPTY, extra = ItemStack.EMPTY;
        BuildingRecipe activeRecipe = activeIndex < 0 ? null : recipes.get(activeIndex);
        if(activeRecipe != null) {
            if(activeRecipe.hasExtraIngredient()) baseX -= 12;
            base = new ItemStack(activeRecipe.baseItem, activeRecipe.baseAmount);
            mc.getItemRenderer().renderAndDecorateItem(base, baseX, baseY);
            mc.getItemRenderer().renderGuiItemDecorations(mc.font, base, baseX, baseY);
            if(activeRecipe.hasExtraIngredient()) {
                extraX += 8;
                List<ItemStack> items = LevelUtil.getUnlockedIngredients(activeRecipe.extraIngredient, mc.player);
                extra = new ItemStack(items.get(mc.player.tickCount / 20 % items.size()).getItem(), activeRecipe.extraAmount);
                mc.getItemRenderer().renderAndDecorateItem(extra, extraX, baseY);
                mc.getItemRenderer().renderGuiItemDecorations(mc.font, extra, extraX, baseY);
            }
        }
        if(pageCount > 1) {
            String pageText = page + "/" + pageCount;
            RenderUtil.drawCenteredText(poseStack, mc.font, pageText, width/2, height/2 - 24, RenderUtil.COLOR_WHITE, true, LightTexture.FULL_BRIGHT);
        }
        drawHoverTooltip(poseStack, base, mouseX, mouseY, baseX, baseY);
        drawHoverTooltip(poseStack, extra, mouseX, mouseY, extraX, baseY);
    }
}
