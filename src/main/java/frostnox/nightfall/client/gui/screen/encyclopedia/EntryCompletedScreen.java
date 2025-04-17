package frostnox.nightfall.client.gui.screen.encyclopedia;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.PartialInventoryScreen;
import frostnox.nightfall.client.gui.screen.inventory.RecipeViewerComponent;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.recipe.IEncyclopediaRecipe;
import frostnox.nightfall.util.RenderUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static frostnox.nightfall.client.gui.screen.encyclopedia.EntryPuzzleScreen.BACKGROUND_HEIGHT;
import static frostnox.nightfall.client.gui.screen.encyclopedia.EntryPuzzleScreen.BACKGROUND_WIDTH;

public class EntryCompletedScreen extends Screen {
    private final Minecraft mc;
    private final EntryClient cEntry;
    private final List<List<ItemStack>> groupedItems;
    private final RecipeViewerComponent recipeViewer;
    private int tickCount;

    public EntryCompletedScreen(EntryClient cEntry) {
        super(NarratorChatListener.NO_TITLE);
        mc = Minecraft.getInstance();
        this.cEntry = cEntry;
        List<Item> unlockedItems = new ObjectArrayList<>(15), allItems = new ObjectArrayList<>(15);
        for(Recipe<?> recipe : RenderUtil.getUnlockedRecipes(mc.player)) {
            if(recipe instanceof IEncyclopediaRecipe encyclopediaRecipe && cEntry.containsEntry(encyclopediaRecipe.getRequirementId())
                    && !unlockedItems.contains(recipe.getResultItem().getItem())) {
                unlockedItems.add(recipe.getResultItem().getItem());
            }
        }
        for(Recipe<?> recipe : RenderUtil.getSearchableRecipes(mc.player)) {
            if(recipe instanceof IEncyclopediaRecipe encyclopediaRecipe && cEntry.containsEntry(encyclopediaRecipe.getRequirementId())
                    && !allItems.contains(recipe.getResultItem().getItem())) {
                allItems.add(recipe.getResultItem().getItem());
            }
        }
        recipeViewer = new RecipeViewerComponent();
        groupedItems = new ObjectArrayList<>(8);
        if(!unlockedItems.isEmpty()) {
            recipeViewer.setVisible(true);
            recipeViewer.setRecipeItem(unlockedItems.get(0));
            Map<TagKey<Item>, List<ItemStack>> taggedItems = new Object2ObjectArrayMap<>(8);
            for(Item item : unlockedItems) {
                if(skipItem(item, taggedItems.values())) continue;
                if(item.builtInRegistryHolder().is(TagsNF.RECIPE_GROUP)) {
                    boolean added = false;
                    for(TagKey<Item> tag : item.builtInRegistryHolder().tags().toList()) {
                        if(taggedItems.containsKey(tag) || tag.location().getPath().contains("no_recipe_grouping/")) continue;
                        List<Item> tagItems = ForgeRegistries.ITEMS.tags().getTag(tag).stream().toList();
                        int match = 0;
                        for(Item tagItem : tagItems) {
                            if(!allItems.contains(tagItem)) {
                                match = 1;
                                break;
                            }
                            if(!tagItem.builtInRegistryHolder().is(TagsNF.RECIPE_GROUP)) {
                                match = 2;
                                break;
                            }
                        }
                        if(match == 0) {
                            added = true;
                            taggedItems.put(tag, tagItems.stream().filter(unlockedItems::contains).map(ItemStack::new).toList());
                            break;
                        }
                        else if(match == 2) {
                            added = true;
                            taggedItems.put(tag, List.of());
                        }
                    }
                    if(!added) groupedItems.add(List.of(new ItemStack(item)));
                }
                else groupedItems.add(List.of(new ItemStack(item)));
            }
            groupedItems.addAll(taggedItems.values().stream().filter((list ) -> !list.isEmpty()).toList());
        }
        ClientEngine.get().openEntry = cEntry;
    }

    private static boolean skipItem(Item item, Collection<List<ItemStack>> taggedItems) {
        for(List<ItemStack> taggedItemStacks : taggedItems) {
            for(ItemStack itemStack : taggedItemStacks) if(itemStack.getItem() == item) return true;
        }
        return false;
    }

    @Override
    public void tick() {
        tickCount++;
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientEngine.get().openEntry = null;
    }

    @Override
    public void render(PoseStack stack, int x, int y, float partial) {
        RenderSystem.disableDepthTest();
        renderBackground(stack);
        super.render(stack, x, y, partial);
        if(cEntry != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, EncyclopediaScreen.TEXTURE);
            stack.pushPose();
            int xOff = width/2 - BACKGROUND_WIDTH/2;
            int yOff = height/2 - BACKGROUND_HEIGHT/2 - PartialInventoryScreen.HEIGHT/2;
            stack.translate(xOff, yOff, 0);
            blit(stack, 0, 0, 0, 512 - BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 512, 512);
            EntryPuzzleScreen.renderImage(stack, cEntry.completedImage);
            /*for(int i = 0; i < groupedItems.size(); i++) {
                int xPos = 5 + (i % 5) * 21 - 1, yPos = 5 + (i / 5) * 21 - 1;
                blit(stack, xPos, yPos, 19, 406, 18, 18, 512, 512);
            }*/
            for(int i = 0; i < groupedItems.size(); i++) {
                int xPos = 5 + (i % 5) * 21 + xOff, yPos = 5 + (i / 5) * 21 + yOff;
                mc.getItemRenderer().renderAndDecorateItem(groupedItems.get(i).get((tickCount / 24) % groupedItems.get(i).size()), xPos, yPos);
            }
            stack.popPose();
            //Recipe viewer
            if(recipeViewer.isVisible()) {
                recipeViewer.setOffset(width/2 - RecipeViewerComponent.WIDTH/2, yOff + BACKGROUND_HEIGHT + 5);
                recipeViewer.render(stack, x, y, partial);
            }
            for(int i = 0; i < groupedItems.size(); i++) {
                int xPos = 5 + (i % 5) * 21 + xOff, yPos = 5 + yOff + (i / 5) * 21;
                if(x >= xPos && x < xPos + 16 && y >= yPos && y < yPos + 16) {
                    renderTooltip(stack, groupedItems.get(i).get((tickCount / 24) % groupedItems.get(i).size()), x, y);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if(recipeViewer.mouseClicked(x, y, button)) return true;
        int xOff = width/2 - BACKGROUND_WIDTH/2;
        int yOff = height/2 - BACKGROUND_HEIGHT/2 - PartialInventoryScreen.HEIGHT/2;
        for(int i = 0; i < groupedItems.size(); i++) {
            int xPos = 5 + (i % 5) * 21 + xOff, yPos = 5 + yOff + (i / 5) * 21;
            if(x >= xPos && x < xPos + 16 && y >= yPos && y < yPos + 16) {
                if(recipeViewer.setRecipeItem(groupedItems.get(i).get((tickCount / 24) % groupedItems.get(i).size()).getItem())) {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                }
            }
        }
        return super.mouseClicked(x, y, button);
    }
}
