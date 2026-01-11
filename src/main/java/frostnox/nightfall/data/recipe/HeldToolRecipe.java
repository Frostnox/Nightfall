package frostnox.nightfall.data.recipe;

import frostnox.nightfall.Nightfall;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class HeldToolRecipe extends ToolIngredientRecipe {
    public static final RecipeType<HeldToolRecipe> TYPE = RecipeType.register(Nightfall.MODID + ":held_tool");
    public static final Serializer<HeldToolRecipe> SERIALIZER = new Serializer<>(HeldToolRecipe::new, "held_tool");

    public HeldToolRecipe(ResourceLocation id, ResourceLocation requirement, Ingredient input, Ingredient tool, ItemStack output, int menuOrder) {
        super(id, requirement, input, tool, output, menuOrder);
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
        return new TranslatableComponent(Nightfall.MODID + ".held_tool");
    }
}
