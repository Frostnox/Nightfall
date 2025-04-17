package frostnox.nightfall.data.recipe;

import frostnox.nightfall.Nightfall;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class BowlCrushingRecipe extends ToolIngredientRecipe {
    public static final RecipeType<BowlCrushingRecipe> TYPE = RecipeType.register(Nightfall.MODID + ":bowl_crushing");
    public static final Serializer<BowlCrushingRecipe> SERIALIZER = new Serializer<>(BowlCrushingRecipe::new, "bowl_crushing");

    public BowlCrushingRecipe(ResourceLocation id, ResourceLocation requirement, Ingredient input, Ingredient tool, ItemStack output) {
        super(id, requirement, input, tool, output);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public TranslatableComponent getTitle() {
        return new TranslatableComponent(Nightfall.MODID + ".bowl_crushing");
    }
}
