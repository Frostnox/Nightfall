package frostnox.nightfall.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.gui.screen.ScreenGuiComponent;
import frostnox.nightfall.data.recipe.IEncyclopediaRecipe;
import frostnox.nightfall.item.item.FilledBucketItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericToServer;
import frostnox.nightfall.registry.forge.FluidsNF;
import frostnox.nightfall.util.RenderUtil;
import frostnox.nightfall.world.inventory.PlayerInventoryContainer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.*;

public class RecipeSearchComponent extends ScreenGuiComponent {
    private static final int ROWS = 8, COLUMNS = 5, SIZE = ROWS * COLUMNS;
    private final Minecraft mc;
    private final PlayerInventoryScreen screen;
    private EditBox searchBox;
    private int startIndex = 0;
    private float scrollOffs;
    private boolean grabbedScrollBar = false;
    private int selectedIndex = Integer.MIN_VALUE;
    private final List<Recipe<?>> searchableRecipes;
    private final List<Item> allItems, searchedItems;
    private final List<Item> visibleItems = new ObjectArrayList<>(SIZE);
    private boolean ignoreTextInput;
    private String lastSearch = "";
    private final RecipeViewerComponent recipeViewer;

    public RecipeSearchComponent(PlayerInventoryScreen screen) {
        mc = Minecraft.getInstance();
        this.screen = screen;
        searchableRecipes = RenderUtil.getSearchableRecipes(mc.player, false);
        allItems = new ObjectArrayList<>(searchableRecipes.size());
        searchedItems = new ObjectArrayList<>(searchableRecipes.size());
        recipeViewer = new RecipeViewerComponent(searchableRecipes);
    }

    public void updateItems() {
        scrollOffs = 0;
        startIndex = 0;
        allItems.clear();
        ItemStack searchItem = ((PlayerInventoryContainer) screen.getMenu()).getSearchItem();
        ItemStack altSearchItem = searchItem.getItem() instanceof FilledBucketItem bucket ? new ItemStack(FluidsNF.getAsItem(bucket.getFluid())) : ItemStack.EMPTY;
        boolean checkIngredient = !searchItem.isEmpty();
        for(Iterator<Recipe<?>> recipes = searchableRecipes.stream().iterator(); recipes.hasNext();) {
            Recipe<?> recipe = recipes.next();
            if(allItems.contains(recipe.getResultItem().getItem())) continue;
            if(checkIngredient) {
                boolean skip = true;
                for(Ingredient ingredient : recipe.getIngredients()) {
                    if(ingredient.test(searchItem) || (!altSearchItem.isEmpty() && ingredient.test(altSearchItem))) {
                        skip = false;
                        break;
                    }
                }
                if(skip) continue;
            }
            if(!(recipe instanceof IEncyclopediaRecipe encyclopediaRecipe) || encyclopediaRecipe.isUnlocked(mc.player)) {
                allItems.add(recipe.getResultItem().getItem());
            }
        }
        updateVisibleItems();
        updateSearchedItems();
    }

    protected void updateVisibleItems() {
        visibleItems.clear();
        String search = searchBox.getValue().toLowerCase(Locale.ROOT);
        int i = 0;
        int scrollSkips = 0;
        for(int j = 0; j < allItems.size() && i < SIZE; j++) {
            Item item = allItems.get(j);
            if(search.isEmpty() || item.getDescription().getString().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
                if(scrollSkips < startIndex) scrollSkips++;
                else {
                    visibleItems.add(item);
                    i++;
                }
            }
        }
    }

    protected void updateSearchedItems() {
        selectedIndex = Integer.MIN_VALUE;
        searchedItems.clear();
        String search = searchBox.getValue().toLowerCase(Locale.ROOT);
        for(Item item : allItems) {
            if(search.isEmpty() || item.getDescription().getString().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
                searchedItems.add(item);
            }
        }
    }

    @Override
    public void init() {
        int x = screen.getLeftPos() - 112 - 2;
        int y = screen.getTopPos();
        String s = searchBox != null ? searchBox.getValue() : "";
        searchBox = new EditBox(mc.font, x + 6, y + 5, 85, 9, new TranslatableComponent("itemGroup.search"));
        searchBox.setMaxLength(50);
        searchBox.setBordered(false);
        searchBox.setVisible(true);
        searchBox.setTextColor(0xffffff);
        searchBox.setValue(s);
        updateItems();
    }

    @Override
    public void containerTick() {
        searchBox.tick();
    }

    @Override
    public void onClose() {
        searchBox.setVisible(false);
        recipeViewer.setVisible(false);
        selectedIndex = Integer.MIN_VALUE;
        grabbedScrollBar = false;
        NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.CLOSE_RECIPE_SEARCH_SERVER));
    }

    @Override
    public void onOpen() {
        init();
    }

    public boolean isRecipeSelected() {
        return recipeViewer.isVisible();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, PlayerInventoryScreen.TEXTURE);
        searchBox.x = screen.getLeftPos() - 112 - 2 + 6;
        searchBox.y = screen.getTopPos() + 5;
        blit(poseStack, screen.getLeftPos() - 112 - 1 + 8, screen.getTopPos() - 17, 288, PlayerInventoryScreen.IMAGE_HEIGHT + 54, 18, 18, 512, 256);
        blit(poseStack, screen.getLeftPos() - 112 - 1, screen.getTopPos(), 288, 0, 112, PlayerInventoryScreen.IMAGE_HEIGHT, 512, 256);
        if(!searchBox.isFocused() && searchBox.getValue().isEmpty()) mc.font.draw(poseStack, new TranslatableComponent("container." + Nightfall.MODID + ".search").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.DARK_GRAY), screen.getLeftPos() - 112 - 2 + 8, screen.getTopPos() + 5, -1);
        else searchBox.render(poseStack, mouseX, mouseY, partial);
        RenderSystem.enableDepthTest(); //Drawing font disables this
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, PlayerInventoryScreen.TEXTURE);
        //Scrollbar
        if(canScroll()) {
            blit(poseStack, screen.getLeftPos() - 112 + 95, screen.getTopPos() + 16 + (int) (134 * scrollOffs), 306, 166, 3, 5, 512, 256);
        }
        int listX = screen.getLeftPos() - 112 - 2 + 6;
        int listY = screen.getTopPos() + 16;
        //Recipe buttons
        for(int i = 0; i < SIZE && i < visibleItems.size(); i++) {
            int x = listX + i % COLUMNS * 18;
            int row = i / COLUMNS;
            int y = listY + row * 18;
            int yOff = PlayerInventoryScreen.IMAGE_HEIGHT;
            if(mouseX >= x && mouseY >= y && mouseX < x + 18 && mouseY < y + 18) yOff += 36;
            else if(i == selectedIndex) yOff += 18;
            blit(poseStack, x, y, 288, yOff, 18, row == 7 ? 17 : 18, 512, 256);
        }
        //Items
        for(int i = 0; i < SIZE && i < visibleItems.size(); i++) {
            int x = listX + i % COLUMNS * 18 + 1;
            int row = i / COLUMNS;
            int y = listY + row * 18 + 1;
            mc.getItemRenderer().renderAndDecorateItem(new ItemStack((Item) visibleItems.toArray()[i]), x, y);
        }
        //Tooltips
        for(int i = 0; i < SIZE && i < visibleItems.size(); i++) {
            int x = listX + i % COLUMNS * 18;
            int y = listY + i / COLUMNS * 18;
            if(mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                screen.renderTooltip(poseStack, new ItemStack((Item) visibleItems.toArray()[i]), mouseX, mouseY);
            }
        }
        //Recipe viewer
        if(recipeViewer.isVisible()) {
            recipeViewer.setOffset(screen.getLeftPos() + 176 + 2, screen.getTopPos());
            recipeViewer.render(poseStack, mouseX, mouseY, partial);
        }
    }

    private boolean updateScroll(double mouseX, double mouseY) {
        if(!canScroll()) return false;
        int barX = screen.getLeftPos() - 112 + 95;
        int barYMin = screen.getTopPos() + 16;
        int barYMax = barYMin + 134 + 5;
        if(grabbedScrollBar || (mouseX >= barX - 1 && mouseX <= barX + 3 && mouseY >= barYMin && mouseY <= barYMax)) {
            scrollOffs = (float) ((mouseY - barYMin - 2) / 134);
            scrollOffs = Mth.clamp(scrollOffs, 0, 1);
            startIndex = (int) ((scrollOffs * getOffScreenRows()) + 0.5) * COLUMNS;
            updateVisibleItems();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(searchBox.mouseClicked(mouseX, mouseY, button)) return true;
        if(recipeViewer.mouseClicked(mouseX, mouseY, button)) return true;
        if(button == 0 && updateScroll(mouseX, mouseY)) {
            grabbedScrollBar = true;
            return true;
        }
        int listX = screen.getLeftPos() - 112 - 2 + 6;
        int listY = screen.getTopPos() + 17;
        for(int i = 0; i < SIZE && i < visibleItems.size(); i++) {
            int x = listX + i % COLUMNS * 18;
            int y = listY + i / COLUMNS * 18;
            if(mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                selectedIndex = i;
                recipeViewer.setVisible(true);
                recipeViewer.setRecipeItem((Item) visibleItems.toArray()[i]);
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(button != 0) return false;
        boolean ret = grabbedScrollBar;
        grabbedScrollBar = false;
        return ret;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(button == 0) return updateScroll(mouseX, mouseY);
        return false;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        ignoreTextInput = false;
        if(!mc.player.isSpectator()) {
            if(searchBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
                checkSearchStringUpdate();
                return true;
            }
            else if(searchBox.isFocused() && searchBox.isVisible() && pKeyCode != 256) return true;
            else if(mc.options.keyChat.matches(pKeyCode, pScanCode) && !searchBox.isFocused()) {
                ignoreTextInput = true;
                searchBox.setFocus(true);
                return true;
            }
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        ignoreTextInput = false;
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if(ignoreTextInput) return false;
        else if(!mc.player.isSpectator()) {
            if(searchBox.charTyped(pCodePoint, pModifiers)) {
                checkSearchStringUpdate();
                return true;
            }
        }
        return super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollAmount) {
        if(scrollAmount < 0 ? canScrollDown() : canScrollUp()) {
            int offScreenRows = getOffScreenRows();
            scrollOffs = (float) (scrollOffs - scrollAmount / offScreenRows);
            scrollOffs = Mth.clamp(scrollOffs, 0, 1);
            startIndex = (int) ((scrollOffs * offScreenRows) + 0.5) * COLUMNS;
            if(selectedIndex != Integer.MIN_VALUE) selectedIndex += scrollAmount * COLUMNS;
            updateVisibleItems();
        }
        return true;
    }

    private int getOffScreenRows() {
        return (Math.max(0, searchedItems.size() - SIZE) + COLUMNS - 1) / COLUMNS;
    }

    private boolean canScrollUp() {
        return startIndex != 0;
    }

    private boolean canScrollDown() {
        return searchedItems.size() - startIndex > SIZE;
    }

    private boolean canScroll() {
        return searchedItems.size() > SIZE;
    }

    private void checkSearchStringUpdate() {
        String search = searchBox.getValue().toLowerCase(Locale.ROOT);
        if(!search.equals(lastSearch)) {
            scrollOffs = 0;
            startIndex = 0;
            updateVisibleItems();
            updateSearchedItems();
            lastSearch = search;
        }
    }

    public void removed() {
        mc.keyboardHandler.setSendRepeatsToGui(false);
    }
}
