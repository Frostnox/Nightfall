package frostnox.nightfall.data.recipe;

import frostnox.nightfall.Nightfall;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

public class CrucibleRecipe extends SingleRecipe {
    public static final RecipeType<CrucibleRecipe> TYPE = RecipeType.register(Nightfall.MODID + ":crucible");
    public static final Serializer<CrucibleRecipe> SERIALIZER = new Serializer<>(CrucibleRecipe::new, "crucible");

    public CrucibleRecipe(ResourceLocation id, ResourceLocation requirement, Ingredient input, ItemStack output, FluidStack fluidOutput, int cookTime) {
        super(id, requirement, input, output, fluidOutput, cookTime);
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
        return new TranslatableComponent(Nightfall.MODID + ".crucible");
    }
}