package frostnox.nightfall.data.recipe.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.data.recipe.CraftingRecipeNF;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class CraftingRecipeBuilder {
    private final Item result;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final int count;
    private final @Nullable ResourceLocation requirement;

    public CraftingRecipeBuilder(ItemLike result, int count, @Nullable ResourceLocation requirement) {
        this.result = result.asItem();
        this.count = count;
        this.requirement = requirement;
    }

    public static CraftingRecipeBuilder base(ItemLike result) {
        return new CraftingRecipeBuilder(result, 1, null);
    }

    public static CraftingRecipeBuilder base(ItemLike result, int count) {
        return new CraftingRecipeBuilder(result, count, null);
    }

    public static CraftingRecipeBuilder base(ItemLike result, RegistryObject<?> requirement) {
        return base(result, 1, requirement);
    }

    public static CraftingRecipeBuilder base(ItemLike result, int count, RegistryObject<?> requirement) {
        return new CraftingRecipeBuilder(result, count, requirement.getId());
    }

    public CraftingRecipeBuilder define(Character symbol, TagKey<Item> itemTag) {
        return this.define(symbol, Ingredient.of(itemTag));
    }

    public CraftingRecipeBuilder define(Character symbol, ItemLike item) {
        return this.define(symbol, Ingredient.of(item));
    }

    public CraftingRecipeBuilder define(Character symbol, Ingredient ingredient) {
        if(this.key.containsKey(symbol)) throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
        else if (symbol == ' ') throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        else {
            this.key.put(symbol, ingredient);
            return this;
        }
    }

    public CraftingRecipeBuilder pattern(String pattern) {
        if(!this.rows.isEmpty() && pattern.length() != this.rows.get(0).length()) throw new IllegalArgumentException("Pattern must be the same width on every line!");
        else {
            this.rows.add(pattern);
            return this;
        }
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        save(consumer, Nightfall.MODID);
    }

    public void save(Consumer<FinishedRecipe> consumer, String namespace) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, "crafting_" + ForgeRegistries.ITEMS.getKey(this.result).getPath());
        if(this.rows.isEmpty()) throw new IllegalStateException("No pattern is defined for shaped recipe " + id + "!");
        else {
            Set<Character> set = Sets.newHashSet(this.key.keySet());
            set.remove(' ');
            for(String s : this.rows) {
                for(int i = 0; i < s.length(); ++i) {
                    char c0 = s.charAt(i);
                    if(!this.key.containsKey(c0) && c0 != ' ') throw new IllegalStateException("Pattern in recipe " + id + " uses undefined symbol '" + c0 + "'");
                    set.remove(c0);
                }
            }
            if(!set.isEmpty()) throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + id);
        }
        consumer.accept(new CraftingRecipeBuilder.Result(id, this.result, this.count, this.rows, this.key, requirement));
    }
    
    public static class Result implements FinishedRecipe {
        private final ResourceLocation id, requirement;
        private final Item result;
        private final int count;
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;
        
        public Result(ResourceLocation id, Item result, int count, List<String> pattern, Map<Character, Ingredient> key, ResourceLocation requirement) {
            this.id = id;
            this.result = result;
            this.count = count;
            this.pattern = pattern;
            this.key = key;
            this.requirement = requirement;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray patterns = new JsonArray();
            for(String s : this.pattern) patterns.add(s);
            json.add("pattern", patterns);

            JsonObject keys = new JsonObject();
            for(Map.Entry<Character, Ingredient> entry : this.key.entrySet()) keys.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            json.add("key", keys);

            JsonObject result = new JsonObject();
            result.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result).toString());
            if(this.count > 1) result.addProperty("count", this.count);
            json.add("result", result);

            if(requirement != null) json.addProperty("requirement", requirement.toString());
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return CraftingRecipeNF.SERIALIZER;
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
