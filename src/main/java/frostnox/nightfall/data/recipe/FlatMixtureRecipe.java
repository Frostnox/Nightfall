package frostnox.nightfall.data.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

/**
 * Ingredients are specified in integer ranges (inclusive).
 * Output size is exactly unitsPerOutput.
 */
public abstract class FlatMixtureRecipe extends MixtureRecipe {
    protected FlatMixtureRecipe(ResourceLocation id, ResourceLocation requirement, NonNullList<Pair<Ingredient, Vec2>> input, ItemStack itemOutput, FluidStack fluidOutput, int unitsPerOutput, int cookTime) {
        super(id, requirement, input, itemOutput, fluidOutput, unitsPerOutput, cookTime);
    }

    @Override
    public boolean matches(RecipeWrapper inventory, Level level) {
        if(!super.matches(inventory, level)) return false;
        HashMap<Ingredient, Integer> amounts = new HashMap<>();
        //Build map of ingredients and their input counts
        for(int j = 0; j < inventory.getContainerSize(); j++) {
            ItemStack stack = inventory.getItem(j);
            if(stack.isEmpty()) continue;
            boolean found = false;
            for(int i = 0; i < input.size(); i++) {
                Ingredient ingredient = input.get(i).getA();
                if(ingredient.test(stack)) {
                    found = true;
                    if(amounts.containsKey(ingredient)) amounts.put(ingredient, amounts.get(ingredient) + getUnitsOf(stack));
                    else amounts.put(ingredient, getUnitsOf(stack));
                }
            }
            if(!found) return false;
        }
        //Check ingredients and amounts
        for(int i = 0; i < input.size(); i++) {
            Ingredient ingredient = input.get(i).getA();
            if(amounts.containsKey(ingredient)) {
                float amount = amounts.get(ingredient);
                Vec2 range = input.get(i).getB();
                if(amount < range.x || amount > range.y) return false;
            }
            else if(input.get(i).getB().x > 0.0F) return false;
        }
        return true;
    }

    @Override
    public ItemStack assembleItem(@Nullable RecipeWrapper inventory, @Nullable List<FluidStack> fluids) {
        ItemStack item = itemOutput.copy();
        if(!item.isEmpty()) item.setCount(unitsPerOutput);
        return item;
    }

    @Override
    public FluidStack assembleFluid(@Nullable RecipeWrapper inventory, @Nullable List<FluidStack> fluids) {
        FluidStack fluid = fluidOutput.copy();
        if(!fluid.isEmpty()) fluid.setAmount(unitsPerOutput);
        return fluid;
    }
}
