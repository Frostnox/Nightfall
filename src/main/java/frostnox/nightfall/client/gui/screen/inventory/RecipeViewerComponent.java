package frostnox.nightfall.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.gui.screen.ScreenGuiComponent;
import frostnox.nightfall.data.recipe.IRenderableRecipe;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class RecipeViewerComponent extends ScreenGuiComponent {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/recipe_viewer.png");
    public static final int WIDTH = 122;
    public static final int HEIGHT = 81;
    public static final int ARROW_WIDTH = 16;
    public static final int ARROW_HEIGHT = 11;
    private final Minecraft mc;
    private final List<Recipe<?>> allRecipes;
    private List<Recipe<?>> recipes = Lists.newArrayList();
    private Item item;
    private int pages;
    private int page;
    private boolean visible;
    private int xOffset, yOffset;

    public RecipeViewerComponent(List<Recipe<?>> allRecipes) {
        mc = Minecraft.getInstance();
        this.allRecipes = allRecipes;
    }

    public boolean setRecipeItem(Item item) {
        if(item == this.item) return false;
        List<Recipe<?>> recipes = Lists.newArrayList();
        recipes.clear();
        allRecipes.forEach((recipe -> {
            if(recipe.getResultItem().getItem().equals(item)) recipes.add(recipe);
        }));
        if(recipes.isEmpty()) return false;
        this.recipes = recipes;
        this.item = item;
        pages = recipes.size();
        page = 0;
        return true;
    }

    public void setOffset(int x, int y) {
        xOffset = x;
        yOffset = y;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
        poseStack.translate(xOffset, yOffset, 0);
        mouseX -= xOffset;
        mouseY -= yOffset;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.disableDepthTest();
        blit(poseStack, 0, 0, 0, 0, WIDTH, HEIGHT, 128, 128);
        //Left arrow
        if(page != 0) {
            int uOff = onLeftArrow(mouseX, mouseY) ? ARROW_WIDTH : 0;
            blit(poseStack, WIDTH - ARROW_WIDTH - 3 - ARROW_WIDTH - 3 - 1, HEIGHT - ARROW_HEIGHT - 3, uOff, HEIGHT + ARROW_HEIGHT, ARROW_WIDTH, ARROW_HEIGHT, 128, 128);
        }
        //Right arrow
        if(page < pages - 1) {
            int uOff = onRightArrow(mouseX, mouseY) ? ARROW_WIDTH : 0;
            blit(poseStack, WIDTH - ARROW_WIDTH - 3, HEIGHT - ARROW_HEIGHT - 3, uOff, HEIGHT, ARROW_WIDTH, ARROW_HEIGHT, 128, 128);
        }
        Recipe<?> recipe = recipes.get(page);
        if(recipe instanceof IRenderableRecipe renderableRecipe) {
            poseStack.pushPose();
            renderableRecipe.render(poseStack, mc.screen, mouseX + xOffset, mouseY + yOffset, partial, xOffset, yOffset);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(0, 0, 200); //Make sure font is in front of items
            RenderUtil.drawCenteredFont(poseStack, mc.font, renderableRecipe.getTitle(), WIDTH/2, 3, RenderUtil.COLOR_BLACK, false);
            poseStack.popPose();
        }
        poseStack.translate(-xOffset, -yOffset, 0);
    }

    private boolean onLeftArrow(double x, double y) {
        int arrowX = WIDTH - ARROW_WIDTH - 3 - ARROW_WIDTH - 3 - 1;
        int arrowY = HEIGHT - ARROW_HEIGHT - 3;
        return x >= arrowX-2 && x < arrowX+2 + ARROW_WIDTH && y >= arrowY-2 && y < arrowY+2 + ARROW_HEIGHT;
    }

    private boolean onRightArrow(double x, double y) {
        int arrowX = WIDTH - ARROW_WIDTH - 3;
        int arrowY = HEIGHT - ARROW_HEIGHT - 3;
        return x >= arrowX-2 && x < arrowX+2 + ARROW_WIDTH && y >= arrowY-2 && y < arrowY+2 + ARROW_HEIGHT;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if(!recipes.isEmpty() && recipes.get(page) instanceof IRenderableRecipe renderableRecipe) {
            ItemStack item = renderableRecipe.clickItem(mc.screen, (int) x - xOffset, (int) y - yOffset);
            if(!item.isEmpty() && !item.getItem().equals(this.item)) {
                if(setRecipeItem(item.getItem())) {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                }
                return true;
            }
        }
        x -= xOffset;
        y -= yOffset;
        if(onRightArrow(x, y) && page < pages - 1) {
            page++;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
        else if(onLeftArrow(x, y) && page != 0) {
            page--;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
        return false;
    }
}
