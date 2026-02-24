package frostnox.nightfall.data.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.recipe.SmithingRecipe;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Consumer;

public class SmithingRecipeBuilder {
    private static final HashMap<String, Integer> IDS = new HashMap<>();
    private final Item result;
    private final int count;
    private final Ingredient input;
    private final int[] work;
    private TagKey<Fluid> quenchFluid = TagsNF.FRESHWATER;
    private @Nullable ResourceLocation requirement;
    private int menuOrder = -1;
    private boolean showInEntry = true;

    public SmithingRecipeBuilder(Ingredient input, int[] work, ItemLike result, int count) {
        this.input = input;
        this.work = work;
        this.result = result.asItem();
        this.count = count;
    }

    public static SmithingRecipeBuilder base(Ingredient input, int[] work, ItemLike result) {
        return base(input, work, result, 1);
    }

    public static SmithingRecipeBuilder base(Ingredient input, int[] work, ItemLike result, int count) {
        return new SmithingRecipeBuilder(input, work, result, count);
    }

    public SmithingRecipeBuilder addQuenchFluid(TagKey<Fluid> fluid) {
        quenchFluid = fluid;
        return this;
    }

    public SmithingRecipeBuilder requirement(RegistryObject<?> entryOrKnowledge) {
        this.requirement = entryOrKnowledge.getId();
        return this;
    }

    public SmithingRecipeBuilder requirement(ResourceLocation id) {
        this.requirement = id;
        return this;
    }

    public SmithingRecipeBuilder order(int menuOrder) {
        this.menuOrder = menuOrder;
        return this;
    }

    public SmithingRecipeBuilder hideInEntry() {
        this.showInEntry = false;
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
        if(work.length != 11) throw new IllegalStateException("Work array is not 11 elements for anvil recipe " + id);
        consumer.accept(new SmithingRecipeBuilder.Result(id, requirement, input, work, quenchFluid, result, count, menuOrder, showInEntry));
    }
    
    public static class Result implements FinishedRecipe {
        private final ResourceLocation id, requirement;
        private final Ingredient input;
        private final int[] work;
        private final TagKey<Fluid> fluid;
        private final Item result;
        private final int count;
        private final int menuOrder;
        private final boolean showInEntry;
        
        public Result(ResourceLocation id, ResourceLocation requirement, Ingredient input, int[] work, TagKey<Fluid> fluid, Item result, int count, int menuOrder, boolean showInEntry) {
            this.id = id;
            this.requirement = requirement;
            this.input = input;
            this.work = work;
            this.fluid = fluid;
            this.result = result;
            this.count = count;
            this.menuOrder = menuOrder;
            this.showInEntry = showInEntry;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("input", input.toJson());

            JsonArray workArray = new JsonArray();
            for(int i : work) workArray.add(i);
            json.add("work", workArray);

            if(fluid != TagsNF.FRESHWATER) {
                json.addProperty("quenchFluid", fluid.location().toString());
            }

            JsonObject result = new JsonObject();
            result.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result).toString());
            if(this.count > 1) result.addProperty("count", this.count);
            json.add("result", result);

            if(menuOrder >= 0) json.addProperty("menuOrder", menuOrder);

            if(!showInEntry) json.addProperty("showInEntry", false);

            if(requirement != null) json.addProperty("requirement", requirement.toString());
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return SmithingRecipe.SERIALIZER;
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
