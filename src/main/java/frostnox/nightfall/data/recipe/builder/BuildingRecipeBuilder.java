package frostnox.nightfall.data.recipe.builder;

import com.google.gson.JsonObject;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.data.recipe.BuildingRecipe;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
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
import java.util.function.Consumer;

public class BuildingRecipeBuilder {
    private Item baseItem;
    private Ingredient extraIngredient;
    private int baseAmount, extraAmount, menuOrder = -1;
    private final Item output;
    private @Nullable ResourceLocation requirement;

    public BuildingRecipeBuilder(Item baseItem, int baseAmount, Item output) {
        this.baseItem = baseItem;
        this.baseAmount = baseAmount;
        this.output = output;
    }

    public static BuildingRecipeBuilder base(Item baseItem, Item output) {
        return new BuildingRecipeBuilder(baseItem, 1, output);
    }

    public static BuildingRecipeBuilder base(Item baseItem, int baseAmount, Item output) {
        return new BuildingRecipeBuilder(baseItem, baseAmount, output);
    }

    public BuildingRecipeBuilder order(int menuOrder) {
        this.menuOrder = menuOrder;
        return this;
    }

    public BuildingRecipeBuilder addExtra(TagKey<Item> itemTag) {
        return this.addExtra(Ingredient.of(itemTag));
    }

    public BuildingRecipeBuilder addExtra(ItemLike item) {
        return this.addExtra(Ingredient.of(item));
    }

    public BuildingRecipeBuilder addExtra(Ingredient ingredient) {
        extraIngredient = ingredient;
        extraAmount = 1;
        return this;
    }

    public BuildingRecipeBuilder addExtra(TagKey<Item> itemTag, int amount) {
        return this.addExtra(Ingredient.of(itemTag), amount);
    }

    public BuildingRecipeBuilder addExtra(ItemLike item, int amount) {
        return this.addExtra(Ingredient.of(item), amount);
    }

    public BuildingRecipeBuilder addExtra(Ingredient ingredient, int amount) {
        extraIngredient = ingredient;
        extraAmount = amount;
        return this;
    }

    public BuildingRecipeBuilder requirement(RegistryObject<?> entryOrKnowledge) {
        requirement = entryOrKnowledge.getId();
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        save(consumer, Nightfall.MODID);
    }

    public void save(Consumer<FinishedRecipe> consumer, String namespace) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, "building_" + ForgeRegistries.ITEMS.getKey(baseItem).getPath() + "_" + ForgeRegistries.ITEMS.getKey(output).getPath());
        if(baseAmount < 1) throw new IllegalStateException("Base ingredient amount is less than 1 for building recipe " + id);
        else if(extraIngredient != null && extraAmount < 1) throw new IllegalStateException("Extra ingredient is defined but amount is less than 1 for building recipe " + id);
        else if(extraAmount > 0 && extraIngredient == null) throw new IllegalStateException("Extra ingredient amount is greater than 0 but ingredient is undefined for building recipe " + id);
        consumer.accept(new BuildingRecipeBuilder.Result(id, requirement, baseItem, extraIngredient, baseAmount, extraAmount, menuOrder, output));
    }
    
    public static class Result implements FinishedRecipe {
        private final ResourceLocation id, requirementId;
        private final Item baseItem;
        private final Ingredient extraIngredient;
        private final int baseAmount, extraAmount, menuOrder;
        private final Item output;
        
        public Result(ResourceLocation id, ResourceLocation requirementId, Item baseItem, Ingredient extraIngredient, int baseAmount, int extraAmount, int menuOrder, Item output) {
            this.id = id;
            this.requirementId = requirementId;
            this.baseItem = baseItem;
            this.extraIngredient = extraIngredient;
            this.baseAmount = baseAmount;
            this.extraAmount = extraAmount;
            this.menuOrder = menuOrder;
            this.output = output;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if(requirementId != null) json.addProperty("requirement", requirementId.toString());

            json.addProperty("outputItem", ForgeRegistries.ITEMS.getKey(this.output).toString());

            json.addProperty("baseAmount", baseAmount);
            json.addProperty("item", ForgeRegistries.ITEMS.getKey(this.baseItem).toString());

            if(extraAmount > 0) {
                json.addProperty("extraAmount", extraAmount);
                json.add("extraIngredient", extraIngredient.toJson());
            }

            if(menuOrder >= 0) json.addProperty("menuOrder", menuOrder);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return BuildingRecipe.SERIALIZER;
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
