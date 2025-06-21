package frostnox.nightfall.data.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.data.recipe.BarrelRecipe;
import frostnox.nightfall.util.DataUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class BarrelRecipeBuilder {
    private static final HashMap<String, Integer> IDS = new HashMap<>();
    private @Nullable ResourceLocation requirement;
    private final List<Ingredient> input = new ObjectArrayList<>(4);
    private ItemStack output;
    private int soakTime;
    private boolean fixedSoakTime = false;

    private BarrelRecipeBuilder(ItemStack output) {
        this.output = output;
    }

    public static BarrelRecipeBuilder base(ItemLike itemResult, int count) {
        return new BarrelRecipeBuilder(new ItemStack(itemResult, count));
    }

    public BarrelRecipeBuilder input(TagKey<Item> itemTag) {
        input.add(Ingredient.of(itemTag));
        return this;
    }

    public BarrelRecipeBuilder input(ItemLike item) {
        input.add(Ingredient.of(item));
        return this;
    }

    public BarrelRecipeBuilder soakTime(int soakTime) {
        if(soakTime < 0) throw new IllegalArgumentException("Soak time for barrel recipe must be greater than 0");
        this.soakTime = soakTime;
        return this;
    }

    public BarrelRecipeBuilder fixedSoakTime() {
        fixedSoakTime = true;
        return this;
    }

    public BarrelRecipeBuilder requirement(ResourceLocation requirement) {
        this.requirement = requirement;
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        save(consumer, Nightfall.MODID);
    }

    public void save(Consumer<FinishedRecipe> consumer, String namespace) {
        save(consumer, BarrelRecipe.SERIALIZER, namespace);
    }

    public void save(Consumer<FinishedRecipe> consumer, BarrelRecipe.Serializer serializer, String namespace) {
        String name = serializer.getRegistryName().getPath() + "_" + ForgeRegistries.ITEMS.getKey(output.getItem()).getPath();
        int number = 0;
        if(IDS.containsKey(name)) {
            number = IDS.get(name);
            number++;
        }
        IDS.put(name, number);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, name + "_" + number);
        if(input.isEmpty()) throw new IllegalStateException("No input defined for barrel recipe " + id);
        consumer.accept(new Result(serializer, id, requirement, input, output, soakTime, fixedSoakTime));
    }

    public static class Result implements FinishedRecipe {
        private final BarrelRecipe.Serializer serializer;
        private final ResourceLocation id, requirement;
        private final List<Ingredient> input;
        private final ItemStack output;
        private final int soakTime;
        private final boolean fixedSoakTime;

        public Result(BarrelRecipe.Serializer serializer, ResourceLocation id, ResourceLocation requirement, List<Ingredient> input, ItemStack output, int soakTime, boolean fixedSoakTime) {
            this.serializer = serializer;
            this.id = id;
            this.requirement = requirement;
            this.input = input;
            this.output = output;
            this.soakTime = soakTime;
            this.fixedSoakTime = fixedSoakTime;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray ingredients = new JsonArray();
            for(Ingredient ingredient : input) ingredients.add(ingredient.toJson());
            json.add("input", ingredients);
            json.add("output", DataUtil.itemStackToJson(output));
            if(soakTime > 0) json.addProperty("soakTime", soakTime);
            json.addProperty("fixedSoakTime", fixedSoakTime);
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
