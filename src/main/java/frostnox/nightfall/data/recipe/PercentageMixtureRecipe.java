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
 * Ingredients are specified in percentage ranges (0 to 1, inclusive).
 * Outputs are in the form of an ItemStack and/or a FluidStack with amount equal to the summation of the ingredients' amounts.
 * Inputs are always consumed in their entirety, regardless of whether it will increase the output.
 */
public abstract class PercentageMixtureRecipe extends MixtureRecipe {
    protected PercentageMixtureRecipe(ResourceLocation id, ResourceLocation requirement, NonNullList<Pair<Ingredient, Vec2>> input, ItemStack itemOutput, FluidStack fluidOutput, int unitsPerOutput, int cookTime, int priority) {
        super(id, requirement, input, itemOutput, fluidOutput, unitsPerOutput, cookTime, priority);
    }

    @Override
    public boolean matches(RecipeWrapper inventory, Level level) {
        if(!super.matches(inventory, level)) return false;
        HashMap<Ingredient, Integer> amounts = new HashMap<>();
        int totalAmount = 0;
        //Build map of ingredients and their input counts
        for(int j = 0; j < inventory.getContainerSize(); j++) {
            ItemStack stack = inventory.getItem(j);
            if(stack.isEmpty()) continue;
            boolean found = false;
            for(int i = 0; i < input.size(); i++) {
                Ingredient ingredient = input.get(i).getA();
                if(ingredient.test(stack)) {
                    found = true;
                    totalAmount += getUnitsOf(stack);
                    if(amounts.containsKey(ingredient)) amounts.put(ingredient, amounts.get(ingredient) + stack.getCount());
                    else amounts.put(ingredient, stack.getCount());
                }
            }
            if(!found) return false;
        }
        //Check ingredients and percentages
        for(int i = 0; i < input.size(); i++) {
            Ingredient ingredient = input.get(i).getA();
            if(amounts.containsKey(ingredient)) {
                float percentage = (float) amounts.get(ingredient) / totalAmount;
                Vec2 range = input.get(i).getB();
                if(percentage < range.x || percentage > range.y) return false;
            }
            else if(input.get(i).getB().x > 0.0F) return false;
        }
        return true;
    }

    @Override
    public ItemStack assembleItem(@Nullable RecipeWrapper inventory, @Nullable List<FluidStack> fluids) {
        int units = 0;
        if(inventory != null) for(int i = 0; i < inventory.getContainerSize(); i++) units += getUnitsOf(inventory.getItem(i));
        if(fluids != null) for(FluidStack fluid : fluids) units += fluid.getAmount();
        ItemStack item = itemOutput.copy();
        if(!item.isEmpty()) item.setCount(units/unitsPerOutput);
        return item;
    }

    @Override
    public FluidStack assembleFluid(@Nullable RecipeWrapper inventory, @Nullable List<FluidStack> fluids) {
        int units = 0;
        if(inventory != null) for(int i = 0; i < inventory.getContainerSize(); i++) units += getUnitsOf(inventory.getItem(i));
        if(fluids != null) for(FluidStack fluid : fluids) units += fluid.getAmount();
        FluidStack fluid = fluidOutput.copy();
        if(!fluid.isEmpty()) fluid.setAmount(units/unitsPerOutput);
        return fluid;
    }
}
