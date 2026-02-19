package frostnox.nightfall.data.recipe;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.inventory.RecipeViewerComponent;
import frostnox.nightfall.item.TieredItemMaterial;
import frostnox.nightfall.item.item.TongsItem;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Stream;

public class TongsEmptyRecipe extends EncyclopediaRecipe<CraftingContainer> implements CraftingRecipe, IRenderableRecipe {
    public static final SimpleRecipeSerializer<TongsEmptyRecipe> SERIALIZER = (SimpleRecipeSerializer<TongsEmptyRecipe>) new SimpleRecipeSerializer<>(TongsEmptyRecipe::new).setRegistryName(Nightfall.MODID, "tongs_empty");
    private static final Lazy<Ingredient> INGREDIENT = Lazy.of(() -> {
        Stream.Builder<ItemStack> stream = Stream.builder();
        for(Item item : ForgeRegistries.ITEMS.getValues()) {
            if(item instanceof TongsItem) {
                ItemStack tongs = new ItemStack(item);
                tongs.getTag().putInt("color", Metal.IRON.getColor().getRGB());
                stream.add(tongs);
            }
        }
        return Ingredient.of(stream.build());
    });

    public TongsEmptyRecipe(ResourceLocation id) {
        super(id, null);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, INGREDIENT.get());
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level level) {
        for(int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if(item.getItem() instanceof TongsItem tongs) return tongs.hasWorkpiece(item);
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer pContainer) {
        return NonNullList.withSize(pContainer.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory) {
        for(int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if(item.getItem() instanceof TongsItem tongs && tongs.hasWorkpiece(item)) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("Damage", item.getDamageValue());
                ItemStack newItem = item.copy();
                newItem.setTag(tag);
                return newItem;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem() {
        return new ItemStack(ItemsNF.WOODEN_TONGS.get());
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth >= 1 && pHeight >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return CraftingRecipeNF.TYPE;
    }

    @Override
    public void render(PoseStack poseStack, Screen screen, int mouseX, int mouseY, float partial, int xOffset, int yOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CraftingRecipeNF.RECIPE_VIEWER_LOCATION);
        Screen.blit(poseStack, 0, 0, 0, 0, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT);
        int xPos = xOffset + 10;
        int yPos = yOffset + 16;
        ItemStack item = LevelUtil.chooseUnlockedIngredient(INGREDIENT.get(), ClientEngine.get().getPlayer());
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(item, xPos, yPos);
        if(onItem(xPos, yPos, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(item), item.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
        ItemStack output = new ItemStack(item.getItem());
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(output, xOffset + 86, yOffset + 34);
        if(onItem(xOffset + 86, yOffset + 34, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(output), output.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
    }

    @Override
    public ItemStack clickItem(Screen screen, int mouseX, int mouseY) {
        if(onItem(10, 16, mouseX, mouseY)) return LevelUtil.chooseUnlockedIngredient(INGREDIENT.get(), ClientEngine.get().getPlayer());
        else return ItemStack.EMPTY;
    }

    @Override
    public TranslatableComponent getTitle() {
        return new TranslatableComponent("container.crafting");
    }

    @Override
    public boolean showInRecipeViewer() {
        return true;
    }

    @Override
    public boolean showInEntry() {
        return false;
    }
}
