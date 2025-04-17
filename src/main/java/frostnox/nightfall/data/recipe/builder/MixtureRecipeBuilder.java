package frostnox.nightfall.data.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.data.recipe.CauldronRecipe;
import frostnox.nightfall.data.recipe.CrucibleRecipe;
import frostnox.nightfall.data.recipe.FurnaceRecipe;
import frostnox.nightfall.data.recipe.MixtureRecipe;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.compress.utils.Lists;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class MixtureRecipeBuilder {
    private static final HashMap<String, Integer> IDS = new HashMap<>();
    private final Item itemResult;
    private final Fluid fluidResult;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final List<Vec2> ranges = Lists.newArrayList();
    private @Nullable ResourceLocation requirement;
    private int unitsPerOutput = 1, cookTime = 1;

    public MixtureRecipeBuilder(ItemLike itemResult, Fluid fluidResult) {
        this.itemResult = itemResult.asItem();
        this.fluidResult = fluidResult;
    }

    public MixtureRecipeBuilder(ItemLike itemResult) {
        this.itemResult = itemResult.asItem();
        fluidResult = null;
    }

    public MixtureRecipeBuilder(Fluid fluidResult) {
        this.itemResult = null;
        this.fluidResult = fluidResult;
    }

    public static MixtureRecipeBuilder base(ItemLike itemResult, Fluid fluidResult) {
        return new MixtureRecipeBuilder(itemResult, fluidResult);
    }

    public static MixtureRecipeBuilder base(ItemLike itemResult) {
        return new MixtureRecipeBuilder(itemResult);
    }

    public static MixtureRecipeBuilder base(Fluid fluidResult) {
        return new MixtureRecipeBuilder(fluidResult);
    }

    public MixtureRecipeBuilder addIngredient(TagKey<Item> itemTag, float min, float max) {
        return this.addIngredient(Ingredient.of(itemTag), min, max);
    }

    public MixtureRecipeBuilder addIngredient(ItemLike item, float min, float max) {
        return this.addIngredient(Ingredient.of(item), min, max);
    }

    public MixtureRecipeBuilder addIngredient(Ingredient ingredient, float min, float max) {
        this.ingredients.add(ingredient);
        this.ranges.add(new Vec2(min, max));
        return this;
    }

    public MixtureRecipeBuilder unitsPerOutput(int units) {
        if(units < 0) throw new IllegalArgumentException("Units per output for mixture recipe must be greater than 0");
        this.unitsPerOutput = units;
        return this;
    }

    public MixtureRecipeBuilder cookTime(int cookTime) {
        if(cookTime < 0) throw new IllegalArgumentException("Cook time for mixture recipe must be greater than 0");
        this.cookTime = cookTime;
        return this;
    }

    public MixtureRecipeBuilder requirement(ResourceLocation requirement) {
        this.requirement = requirement;
        return this;
    }

    public void saveCauldron(Consumer<FinishedRecipe> consumer) {
        saveCauldron(consumer, Nightfall.MODID);
    }

    public void saveCauldron(Consumer<FinishedRecipe> consumer, String namespace) {
        save(consumer, CauldronRecipe.SERIALIZER, namespace);
    }

    public void saveFurnace(Consumer<FinishedRecipe> consumer) {
        saveFurnace(consumer, Nightfall.MODID);
    }

    public void saveFurnace(Consumer<FinishedRecipe> consumer, String namespace) {
        save(consumer, FurnaceRecipe.SERIALIZER, namespace);
    }

    public void save(Consumer<FinishedRecipe> consumer, MixtureRecipe.Serializer<?> serializer, String namespace) {
        String name = serializer.getRegistryName().getPath() + "_" + (fluidResult != null ? ForgeRegistries.FLUIDS.getKey(fluidResult).getPath() : ForgeRegistries.ITEMS.getKey(this.itemResult).getPath());
        int number = 0;
        if(IDS.containsKey(name)) {
            number = IDS.get(name);
            number++;
        }
        IDS.put(name, number);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, name + "_" + number);
        if(this.ingredients.isEmpty()) throw new IllegalStateException("No ingredients defined for mixture recipe " + id);
        else if(itemResult == null && fluidResult == null) throw new IllegalStateException("No result specified for mixture recipe " + id);
        consumer.accept(new Result(serializer, id, requirement, itemResult, fluidResult, ingredients, ranges, unitsPerOutput, cookTime));
    }

    public static class Result implements FinishedRecipe {
        private final MixtureRecipe.Serializer<?> serializer;
        private final ResourceLocation id, requirement;
        private final Item itemResult;
        private final Fluid fluidResult;
        private final List<Ingredient> ingredients;
        private final List<Vec2> ranges;
        private final int unitsPerOutput, cookTime;

        public Result(MixtureRecipe.Serializer<?> serializer, ResourceLocation id, ResourceLocation requirement, Item itemResult, Fluid fluidResult, List<Ingredient> ingredients, List<Vec2> ranges, int unitsPerOutput, int cookTime) {
            this.serializer = serializer;
            this.id = id;
            this.requirement = requirement;
            this.itemResult = itemResult;
            this.fluidResult = fluidResult;
            this.ingredients = ingredients;
            this.ranges = ranges;
            this.unitsPerOutput = unitsPerOutput;
            this.cookTime = cookTime;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if(itemResult != null) {
                JsonObject result = new JsonObject();
                result.addProperty("item", ForgeRegistries.ITEMS.getKey(this.itemResult).toString());
                json.add("itemResult", result);
            }
            if(fluidResult != null) {
                JsonObject result = new JsonObject();
                result.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(this.fluidResult).toString());
                json.add("fluidResult", result);
            }

            JsonArray input = new JsonArray();
            for(int i = 0; i < ingredients.size(); i++) {
                input.add(ingredients.get(i).toJson());
                input.add(ranges.get(i).x);
                input.add(ranges.get(i).y);
            }
            json.add("input", input);
            if(unitsPerOutput > 0) json.addProperty("units", unitsPerOutput);
            json.addProperty("cookTime", cookTime);

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
