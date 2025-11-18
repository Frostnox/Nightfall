package frostnox.nightfall.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.util.DataUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Mixtures are unordered recipes that can take in various amounts of ingredients.
 * Mixtures must be unique such that any valid combination of ingredients results only in that mixture and no other.
 */
public abstract class MixtureRecipe extends EncyclopediaRecipe<RecipeWrapper> implements Recipe<RecipeWrapper>, IRenderableRecipe {
    protected final NonNullList<Pair<Ingredient, Vec2>> input;
    protected final ItemStack itemOutput;
    protected final FluidStack fluidOutput;
    protected final int unitsPerOutput, cookTime, priority;

    protected MixtureRecipe(ResourceLocation id, ResourceLocation requirement, NonNullList<Pair<Ingredient, Vec2>> input, ItemStack itemOutput, FluidStack fluidOutput, int unitsPerOutput, int cookTime, int priority) {
        super(id, requirement);
        this.input = input;
        this.itemOutput = itemOutput;
        this.fluidOutput = fluidOutput;
        this.unitsPerOutput = unitsPerOutput;
        this.cookTime = cookTime;
        this.priority = priority;
    }

    public abstract ItemStack assembleItem(@Nullable RecipeWrapper inventory, @Nullable List<FluidStack> fluids);

    public abstract FluidStack assembleFluid(@Nullable RecipeWrapper inventory, @Nullable List<FluidStack> fluids);

    public FluidStack getResultFluid() {
        return fluidOutput.copy();
    }

    public NonNullList<Pair<Ingredient, Vec2>> getInput() {
        return input;
    }

    public int getUnitsPerOutput() {
        return unitsPerOutput;
    }

    public int getCookTime() {
        return cookTime;
    }

    public int getPriority() {
        return priority;
    }

    public float getTemperature() {
        if(!fluidOutput.isEmpty() && !itemOutput.isEmpty()) return Math.max(fluidOutput.getRawFluid().getAttributes().getTemperature(), TieredHeat.getMinimumTemp(itemOutput));
        else if(!itemOutput.isEmpty()) return TieredHeat.getMinimumTemp(itemOutput);
        else return fluidOutput.getRawFluid().getAttributes().getTemperature();
    }

    @Override
    public ItemStack assemble(RecipeWrapper inventory) {
        return itemOutput.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height <= this.input.size();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for(int i = 0; i < input.size(); i++) ingredients.add(input.get(i).getA());
        return ingredients;
    }

    @Override
    public ItemStack getResultItem() {
        return itemOutput.copy();
    }

    @Override
    public boolean showInRecipeViewer() {
        return getRequirementId() != null;
    }

    public static int getUnitsOf(ItemStack stack) {
        if(stack.is(TagsNF.MIXTURE_INGREDIENT)) {
            int sum = 0;
            if(stack.is(TagsNF.MIXTURE_1)) sum += 1;
            if(stack.is(TagsNF.MIXTURE_2)) sum += 2;
            if(stack.is(TagsNF.MIXTURE_3)) sum += 3;
            if(stack.is(TagsNF.MIXTURE_4)) sum += 4;
            if(stack.is(TagsNF.MIXTURE_5)) sum += 5;
            if(stack.is(TagsNF.MIXTURE_10)) sum += 10;
            if(stack.is(TagsNF.MIXTURE_20)) sum += 20;
            if(stack.is(TagsNF.MIXTURE_30)) sum += 30;
            if(stack.is(TagsNF.MIXTURE_40)) sum += 40;
            if(stack.is(TagsNF.MIXTURE_50)) sum += 50;
            if(stack.is(TagsNF.MIXTURE_100)) sum += 100;
            return Math.max(1, sum) * stack.getCount();
        }
        else return stack.getCount();
    }

    public static class Serializer<T extends MixtureRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {
        interface Factory<T extends MixtureRecipe> {
            T create(ResourceLocation id, ResourceLocation requirement, NonNullList<Pair<Ingredient, Vec2>> input, ItemStack itemOutput, FluidStack fluidOutput, int unitsPerOutput, int cookTime, int priority);
        }
        private final Factory<T> factory;

        Serializer(Factory<T> factory, String name) {
            this.factory = factory;
            this.setRegistryName(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, name));
        }

        @Override
        public T fromJson(ResourceLocation id, JsonObject json) {
            NonNullList<Pair<Ingredient, Vec2>> inputs = NonNullList.create();
            JsonArray input = GsonHelper.getAsJsonArray(json, "input");
            for(int i = 0; i < input.size(); i += 3) {
                Ingredient ingredient = Ingredient.fromJson(input.getAsJsonArray().get(i));
                float min = input.get(i+1).getAsFloat();
                float max = input.get(i+2).getAsFloat();
                if(!ingredient.isEmpty()) inputs.add(new Pair<>(ingredient, new Vec2(min, max)));
            }
            if(inputs.isEmpty()) throw new JsonSyntaxException("No ingredients defined for mixture recipe.");

            ItemStack itemResult = json.has("itemResult") ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "itemResult")) : ItemStack.EMPTY;
            FluidStack fluidResult = json.has("fluidResult") ? DataUtil.fluidStackFromJson(GsonHelper.getAsJsonObject(json, "fluidResult")) : FluidStack.EMPTY;
            int unitsPerOutput = GsonHelper.getAsInt(json, "units", 1);
            int cookTime = GsonHelper.getAsInt(json, "cookTime", 1);
            int priority = GsonHelper.getAsInt(json, "priority", 0);
            ResourceLocation requirement = null;
            if(json.has("requirement")) requirement = ResourceLocation.parse(json.get("requirement").getAsString());

            return factory.create(id, requirement, inputs, itemResult, fluidResult, unitsPerOutput, cookTime, priority);
        }

        @Override
        public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ResourceLocation requirement = buf.readResourceLocation();
            int ingredientsAmount = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientsAmount, Ingredient.EMPTY);
            for(int i = 0; i < ingredients.size(); ++i) ingredients.set(i, Ingredient.fromNetwork(buf));
            NonNullList<Vec2> ranges = NonNullList.withSize(ingredientsAmount, Vec2.ZERO);
            for(int i = 0; i < ranges.size(); i++) ranges.set(i, new Vec2(buf.readFloat(), buf.readFloat()));
            NonNullList<Pair<Ingredient, Vec2>> inputs = NonNullList.createWithCapacity(ingredientsAmount);
            for(int i = 0; i < inputs.size(); i++) inputs.add(i, new Pair<>(ingredients.get(i), ranges.get(i)));
            ItemStack item = buf.readItem();
            FluidStack fluid = buf.readFluidStack();
            int unitsPerOutput = buf.readVarInt();
            int cookTime = buf.readVarInt();
            int priority = buf.readVarInt();
            return factory.create(id, requirement.getPath().equals("empty") ? null : requirement, inputs, item, fluid, unitsPerOutput, cookTime, priority);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, T recipe) {
            buf.writeResourceLocation(recipe.getRequirementId() == null ? ResourceLocation.parse("empty") : recipe.getRequirementId());
            buf.writeVarInt(recipe.input.size());
            for(Ingredient ingredient : recipe.getIngredients()) ingredient.toNetwork(buf);
            for(int i = 0; i < recipe.input.size(); i++) {
                Vec2 range = recipe.input.get(i).getB();
                buf.writeFloat(range.x);
                buf.writeFloat(range.y);
            }
            buf.writeItem(recipe.getResultItem());
            buf.writeFluidStack(recipe.getResultFluid());
            buf.writeVarInt(recipe.getUnitsPerOutput());
            buf.writeVarInt(recipe.getCookTime());
            buf.writeVarInt(recipe.getPriority());
        }
    }
}
