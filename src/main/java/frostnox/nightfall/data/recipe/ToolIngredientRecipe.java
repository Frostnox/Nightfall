package frostnox.nightfall.data.recipe;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.inventory.RecipeViewerComponent;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class ToolIngredientRecipe extends EncyclopediaRecipe<RecipeWrapper> implements IRenderableRecipe {
    public static final ResourceLocation RECIPE_VIEWER_LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/recipe_tool.png");
    private final Ingredient input, tool;
    private final ItemStack output;

    protected ToolIngredientRecipe(ResourceLocation id, ResourceLocation requirement, Ingredient input, Ingredient tool, ItemStack output) {
        super(id, requirement);
        this.input = input;
        this.tool = tool;
        this.output = output;
    }

    @Override
    public boolean matches(RecipeWrapper inventory, Level level) {
        if(tool.test(PlayerData.get(ForgeHooks.getCraftingPlayer()).getHeldItemForRecipe())) {
            for(int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if(input.test(item)) return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(RecipeWrapper pContainer) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth >= 1 && pHeight >= 1;
    }

    public Ingredient getInput() {
        return input;
    }

    public Ingredient getTool() {
        return tool;
    }

    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input);
    }

    @Override
    public NonNullList<Ingredient> getUnlockIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input, tool);
    }

    @Override
    public boolean showInRecipeViewer() {
        return getRequirementId() != null;
    }

    @Override
    public void render(PoseStack poseStack, Screen screen, int mouseX, int mouseY, float partial, int xOffset, int yOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, RECIPE_VIEWER_LOCATION);
        Screen.blit(poseStack, 0, 0, 0, 0, 26, RecipeViewerComponent.HEIGHT, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT);
        Screen.blit(poseStack, 26, 0, 26, 0, RecipeViewerComponent.WIDTH - 26, RecipeViewerComponent.HEIGHT, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT);
        int x = xOffset + 10;
        int y = yOffset + 34;
        ItemStack toolStack = LevelUtil.chooseUnlockedIngredient(tool, ClientEngine.get().getPlayer());
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(toolStack, x, y);
        if(onItem(x, y, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(toolStack), toolStack.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
        x += 36;
        ItemStack inputStack = LevelUtil.chooseUnlockedIngredient(input, ClientEngine.get().getPlayer());
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(inputStack, x, y);
        if(onItem(x, y, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(inputStack), inputStack.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
        x += 40;
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(output, x, y);
        if(onItem(x, y, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(output), output.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
    }

    @Override
    public ItemStack clickItem(Screen screen, int mouseX, int mouseY) {
        int x = 10;
        int y = 34;
        if(onItem(x, y, mouseX, mouseY)) return LevelUtil.chooseUnlockedIngredient(tool, ClientEngine.get().getPlayer());
        x += 36;
        if(onItem(x, y, mouseX, mouseY)) return LevelUtil.chooseUnlockedIngredient(input, ClientEngine.get().getPlayer());
        return ItemStack.EMPTY;
    }

    public static class Serializer<T extends ToolIngredientRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {
        interface Factory<T extends ToolIngredientRecipe> {
            T create(ResourceLocation id, ResourceLocation requirement, Ingredient input, Ingredient tool, ItemStack output);
        }
        private final ToolIngredientRecipe.Serializer.Factory<T> factory;

        Serializer(ToolIngredientRecipe.Serializer.Factory<T> factory, String name) {
            this.factory = factory;
            this.setRegistryName(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, name));
        }

        @Override
        public T fromJson(ResourceLocation id, JsonObject json) {
            ResourceLocation requirement = null;
            if(json.has("requirement")) requirement = ResourceLocation.parse(json.get("requirement").getAsString());
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            Ingredient tool = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "tool"));
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            return factory.create(id, requirement, input, tool, output);
        }

        @Override
        public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ResourceLocation requirement = buf.readResourceLocation();
            return factory.create(id, requirement.getPath().equals("empty") ? null : requirement,
                    Ingredient.fromNetwork(buf), Ingredient.fromNetwork(buf), buf.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ToolIngredientRecipe recipe) {
            buf.writeResourceLocation(recipe.getRequirementId() == null ? ResourceLocation.parse("empty") : recipe.getRequirementId());
            recipe.getInput().toNetwork(buf);
            recipe.getTool().toNetwork(buf);
            buf.writeItem(recipe.getResultItem());
        }
    }

    public static void saveBowl(Ingredient input, Ingredient tool, ItemLike output, @Nullable ResourceLocation requirement, Consumer<FinishedRecipe> consumer) {
        saveBowl(input, tool, output, requirement, consumer, Nightfall.MODID);
    }

    public static void saveBowl(Ingredient input, Ingredient tool, ItemLike output, @Nullable ResourceLocation requirement, Consumer<FinishedRecipe> consumer, String namespace) {
        save(BowlCrushingRecipe.SERIALIZER, input, tool, output, requirement, consumer, namespace);
    }

    public static void saveHeldTool(Ingredient input, Ingredient tool, ItemLike output, @Nullable ResourceLocation requirement, Consumer<FinishedRecipe> consumer) {
        saveHeldTool(input, tool, output, requirement, consumer, Nightfall.MODID);
    }

    public static void saveHeldTool(Ingredient input, Ingredient tool, ItemLike output, @Nullable ResourceLocation requirement, Consumer<FinishedRecipe> consumer, String namespace) {
        save(HeldToolRecipe.SERIALIZER, input, tool, output, requirement, consumer, namespace);
    }

    public static void save(Serializer<?> serializer, Ingredient input, Ingredient tool, ItemLike output, @Nullable ResourceLocation requirement, Consumer<FinishedRecipe> consumer, String namespace) {
        Item outputItem = output.asItem();
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath (namespace, serializer.getRegistryName().getPath() + "_" +
                ForgeRegistries.ITEMS.getKey(outputItem).getPath());
        if(input.isEmpty()) throw new IllegalStateException("No ingredient defined for held tool recipe " + id);
        if(tool.isEmpty()) throw new IllegalStateException("No tool defined for held tool recipe " + id);
        consumer.accept(new Result(serializer, id, requirement, input, tool, outputItem));
    }

    public static class Result implements FinishedRecipe {
        private final Serializer<?> serializer;
        private final ResourceLocation id, requirement;
        private final Ingredient input, tool;
        private final Item output;

        public Result(Serializer<?> serializer, ResourceLocation id, ResourceLocation requirement, Ingredient input, Ingredient tool, Item output) {
            this.serializer = serializer;
            this.id = id;
            this.requirement = requirement;
            this.input = input;
            this.tool = tool;
            this.output = output;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("input", input.toJson());
            json.add("tool", tool.toJson());
            JsonObject result = new JsonObject();
            result.addProperty("item", ForgeRegistries.ITEMS.getKey(output).toString());
            json.add("output", result);
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
