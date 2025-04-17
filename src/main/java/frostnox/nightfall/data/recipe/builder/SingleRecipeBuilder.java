package frostnox.nightfall.data.recipe.builder;

import com.google.gson.JsonObject;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.data.recipe.*;
import frostnox.nightfall.util.DataUtil;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Consumer;

public class SingleRecipeBuilder {
    private static final HashMap<String, Integer> IDS = new HashMap<>();
    private @Nullable ResourceLocation requirement;
    private Ingredient input;
    private ItemStack output;
    private FluidStack fluidOutput;
    private int cookTime;

    private SingleRecipeBuilder(ItemStack output, FluidStack fluidOutput) {
        this.output = output;
        this.fluidOutput = fluidOutput;
    }

    public static SingleRecipeBuilder base(ItemLike itemResult, int itemCount, Fluid fluidResult, int fluidCount) {
        return new SingleRecipeBuilder(new ItemStack(itemResult, itemCount), new FluidStack(fluidResult, fluidCount));
    }

    public static SingleRecipeBuilder base(ItemLike itemResult, int count) {
        return new SingleRecipeBuilder(new ItemStack(itemResult, count), FluidStack.EMPTY);
    }

    public static SingleRecipeBuilder base(Fluid fluidResult, int count) {
        return new SingleRecipeBuilder(ItemStack.EMPTY, new FluidStack(fluidResult, count));
    }

    public SingleRecipeBuilder input(TagKey<Item> itemTag) {
        input = Ingredient.of(itemTag);
        return this;
    }

    public SingleRecipeBuilder input(ItemLike item) {
        input = Ingredient.of(item);
        return this;
    }

    public SingleRecipeBuilder cookTime(int cookTime) {
        if(cookTime < 0) throw new IllegalArgumentException("Cook time for single recipe must be greater than 0");
        this.cookTime = cookTime;
        return this;
    }

    public SingleRecipeBuilder requirement(ResourceLocation requirement) {
        this.requirement = requirement;
        return this;
    }

    public void saveCampfire(Consumer<FinishedRecipe> consumer) {
        saveCampfire(consumer, Nightfall.MODID);
    }

    public void saveCampfire(Consumer<FinishedRecipe> consumer, String namespace) {
        save(consumer, CampfireRecipe.SERIALIZER, namespace);
    }

    public void saveCrucible(Consumer<FinishedRecipe> consumer) {
        saveCrucible(consumer, Nightfall.MODID);
    }

    public void saveCrucible(Consumer<FinishedRecipe> consumer, String namespace) {
        save(consumer, CrucibleRecipe.SERIALIZER, namespace);
    }

    public void save(Consumer<FinishedRecipe> consumer, SingleRecipe.Serializer<?> serializer, String namespace) {
        String name = serializer.getRegistryName().getPath() + "_" + (!fluidOutput.isEmpty() ? ForgeRegistries.FLUIDS.getKey(fluidOutput.getFluid()).getPath()
                : ForgeRegistries.ITEMS.getKey(output.getItem()).getPath());
        int number = 0;
        if(IDS.containsKey(name)) {
            number = IDS.get(name);
            number++;
        }
        IDS.put(name, number);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, name + "_" + number);
        if(input == null) throw new IllegalStateException("No input defined for single recipe " + id);
        else if(output.isEmpty() && fluidOutput.isEmpty()) throw new IllegalStateException("No result specified for single recipe " + id);
        consumer.accept(new Result(serializer, id, requirement, input, output, fluidOutput, cookTime));
    }

    public static class Result implements FinishedRecipe {
        private final SingleRecipe.Serializer<?> serializer;
        private final ResourceLocation id, requirement;
        private final Ingredient input;
        private final ItemStack output;
        private final FluidStack fluidOutput;
        private final int cookTime;

        public Result(SingleRecipe.Serializer<?> serializer, ResourceLocation id, ResourceLocation requirement, Ingredient input, ItemStack output, FluidStack fluidOutput, int cookTime) {
            this.serializer = serializer;
            this.id = id;
            this.requirement = requirement;
            this.input = input;
            this.output = output;
            this.fluidOutput = fluidOutput;
            this.cookTime = cookTime;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("input", input.toJson());
            if(!output.isEmpty()) json.add("output", DataUtil.itemStackToJson(output));
            if(!fluidOutput.isEmpty()) json.add("fluidOutput", DataUtil.fluidStackToJson(fluidOutput));
            if(cookTime > 0) json.addProperty("cookTime", cookTime);
            if(requirement != null) json.addProperty("requirement", requirement.toString());
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return serializer;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
