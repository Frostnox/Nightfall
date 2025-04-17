package frostnox.nightfall.data.recipe.builder;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.MicroGridShape;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlockEntity;
import frostnox.nightfall.data.recipe.TieredAnvilRecipe;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class TieredAnvilRecipeBuilder {
    private static final HashMap<String, Integer> IDS = new HashMap<>();
    private final Item result;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final List<String> levelsStart = Lists.newArrayList();
    private final List<String> levelsFinish = Lists.newArrayList();
    private final int count, tier;
    private @Nullable ResourceLocation requirement;
    private int randMin, randMax = 1024;
    private float slagChance;

    public TieredAnvilRecipeBuilder(ItemLike result, int count, int tier) {
        this.result = result.asItem();
        this.count = count;
        this.tier = tier;
    }

    public static TieredAnvilRecipeBuilder base(ItemLike result, int tier) {
        return base(result, 1, tier);
    }

    public static TieredAnvilRecipeBuilder base(ItemLike result, int count, int tier) {
        return new TieredAnvilRecipeBuilder(result, count, tier);
    }

    public TieredAnvilRecipeBuilder addIngredient(TagKey<Item> itemTag) {
        return this.addIngredient(Ingredient.of(itemTag));
    }

    public TieredAnvilRecipeBuilder addIngredient(ItemLike item) {
        return this.addIngredient(Ingredient.of(item));
    }

    public TieredAnvilRecipeBuilder addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    /**
     * @param string representation of grid shape, 'o' for empty, 'X' for filled, and '?' for random (control output with range)
     */
    private TieredAnvilRecipeBuilder addGridLevel(List<String> levels, String string) {
        if(string.length() != TieredAnvilBlockEntity.GRID_X * TieredAnvilBlockEntity.GRID_Z) throw new IllegalArgumentException("Grid level must fully define all positions.");
        for(int i = 0; i < string.length(); i++) if(string.charAt(i) != 'o' && string.charAt(i) != 'X' && string.charAt(i) != '?') throw new IllegalArgumentException("Grid level must be defined using only 'o', 'X', and '?' characters.");
        levels.add(string);
        return this;
    }

    public TieredAnvilRecipeBuilder addFinishShape(String... strings) {
        for(String string : strings) addGridLevel(this.levelsFinish, string);
        return this;
    }

    public TieredAnvilRecipeBuilder addStartShape(String... strings) {
        for(String string : strings) addGridLevel(this.levelsStart, string);
        return this;
    }

    public TieredAnvilRecipeBuilder addStartShape(MicroGridShape shape) {
        return addStartShape(shape.getGridAsString());
    }

    public TieredAnvilRecipeBuilder addFinishShape(MicroGridShape shape) {
        return addFinishShape(shape.getGridAsString());
    }

    public TieredAnvilRecipeBuilder requirement(RegistryObject<?> entryOrKnowledge) {
        this.requirement = entryOrKnowledge.getId();
        return this;
    }

    public TieredAnvilRecipeBuilder requirement(ResourceLocation id) {
        this.requirement = id;
        return this;
    }

    public TieredAnvilRecipeBuilder randRange(int min, int max) {
        this.randMin = min;
        this.randMax = max;
        return this;
    }

    public TieredAnvilRecipeBuilder slagChance(float slagChance) {
        this.slagChance = slagChance;
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        save(consumer, Nightfall.MODID);
    }

    public void save(Consumer<FinishedRecipe> consumer, String namespace) {
        String name = "anvil_" + ForgeRegistries.ITEMS.getKey(this.result).getPath();
        int number = 0;
        if(IDS.containsKey(name)) {
            number = IDS.get(name);
            number++;
        }
        IDS.put(name, number);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, name + "_" + number);
        if(this.ingredients.isEmpty()) throw new IllegalStateException("No ingredients defined for anvil recipe " + id);
        else if(this.ingredients.size() > 3) throw new IllegalStateException("More than 3 ingredients defined for anvil recipe " + id);
        else if(levelsStart.isEmpty()) throw new IllegalStateException("No starting grid shape defined for anvil recipe " + id);
        else if(levelsStart.size() > TieredAnvilBlockEntity.GRID_Y) throw new IllegalStateException("Starting grid shape exceeds the maximum height of " + TieredAnvilBlockEntity.GRID_Y + "for anvil recipe " + id);
        else if(levelsFinish.isEmpty()) throw new IllegalStateException("No finished grid shape defined for anvil recipe " + id);
        else if(levelsFinish.size() > TieredAnvilBlockEntity.GRID_Y) throw new IllegalStateException("Finished grid shape exceeds the maximum height of " + TieredAnvilBlockEntity.GRID_Y + "for anvil recipe " + id);
        else if(slagChance < 0F || slagChance > 1F) throw new IllegalStateException("Slag chance " + slagChance + " must be between 0 and 1.");
        consumer.accept(new TieredAnvilRecipeBuilder.Result(id, requirement, result, count, tier, levelsStart, levelsFinish, ingredients, randMin, randMax, slagChance));
    }
    
    public static class Result implements FinishedRecipe {
        private final ResourceLocation id, requirement;
        private final Item result;
        private final int count, tier;
        private final List<String> levelsStart;
        private final List<String> levelsFinish;
        private final List<Ingredient> ingredients;
        private final int randMin, randMax;
        private final float slagChance;
        
        public Result(ResourceLocation id, ResourceLocation requirement, Item result, int count, int tier, List<String> levelsStart, List<String> levelsFinish, List<Ingredient> ingredients, int randMin, int randMax, float slagChance) {
            this.id = id;
            this.requirement = requirement;
            this.result = result;
            this.count = count;
            this.tier = tier;
            this.levelsStart = levelsStart;
            this.levelsFinish = levelsFinish;
            this.ingredients = ingredients;
            this.randMin = randMin;
            this.randMax = randMax;
            this.slagChance = slagChance;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("tier", this.tier);

            JsonObject result = new JsonObject();
            result.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result).toString());
            if(this.count > 1) result.addProperty("count", this.count);
            json.add("result", result);

            JsonArray input = new JsonArray();
            for(Ingredient ingredient : this.ingredients) input.add(ingredient.toJson());
            json.add("input", input);

            JsonArray gridStart = new JsonArray();
            for(String s : this.levelsStart) gridStart.add(s);
            json.add("gridStart", gridStart);

            JsonArray gridFinish = new JsonArray();
            for(String s : this.levelsFinish) gridFinish.add(s);
            json.add("gridFinish", gridFinish);

            if(requirement != null) json.addProperty("requirement", requirement.toString());

            if(randMin > 0) json.addProperty("randMin", randMin);
            if(randMax < 1024) json.addProperty("randMax", randMax);
            if(slagChance > 0F) json.addProperty("slagChance", slagChance);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return TieredAnvilRecipe.SERIALIZER;
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
