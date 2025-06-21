package frostnox.nightfall.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.inventory.RecipeViewerComponent;
import frostnox.nightfall.util.LevelUtil;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Map;

public class BarrelRecipe extends EncyclopediaRecipe<RecipeWrapper> implements IRenderableRecipe {
    public static final RecipeType<BarrelRecipe> TYPE = RecipeType.register(Nightfall.MODID + ":barrel");
    public static final Serializer SERIALIZER = new Serializer();
    public static final ResourceLocation RECIPE_VIEWER_LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/recipe_barrel.png");
    private final NonNullList<Ingredient> input;
    private final ItemStack output;
    private final int soakTime;
    private final boolean fixedSoakTime;

    public BarrelRecipe(ResourceLocation id, ResourceLocation requirementId, NonNullList<Ingredient> input, ItemStack output, int soakTime, boolean fixedSoakTime) {
        super(id, requirementId);
        this.input = input;
        this.output = output;
        this.soakTime = soakTime;
        this.fixedSoakTime = fixedSoakTime;
    }

    public boolean hasFixedSoakTime() {
        return fixedSoakTime;
    }

    public int getSoakTime() {
        return soakTime;
    }

    @Override
    public boolean matches(RecipeWrapper container, Level level) {
        if(!super.matches(container, level) || container.isEmpty()) return false;
        IntSet matchingIngredients = new IntArraySet(input.size());
        IntSet[] sets = new IntArraySet[container.getContainerSize()];
        for(int i = 0; i < sets.length; i++) sets[i] = new IntArraySet(input.size());
        int inputSize = 0;
        for(int j = 0; j < container.getContainerSize(); j++) {
            ItemStack item = container.getItem(j);
            if(item.isEmpty()) continue;
            if(inputSize == 0) inputSize = item.getCount();
            else if(item.getCount() != inputSize) return false;
            for(int i = 0; i < input.size(); i++) {
                Ingredient ingredient = input.get(i);
                if(ingredient.test(item)) {
                    sets[j].add(i);
                    matchingIngredients.add(i);
                }
            }
            if(sets[j].isEmpty()) return false;
        }
        //All ingredients must be present
        if(matchingIngredients.size() != input.size()) return false;
        //Check if each ingredient can be mapped to a single item in the container
        //If same set of ingredients appears in more items than the set's size, then mapping is impossible
        Map<IntSet, Integer> setCounts = new Object2IntArrayMap<>(sets.length);
        for(int j = 0; j < sets.length; j++) {
            if(!sets[j].isEmpty()) setCounts.merge(sets[j], 1, Integer::sum);
        }
        for(var entry : setCounts.entrySet()) {
            if(entry.getValue() > entry.getKey().size()) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(RecipeWrapper container) {
        int inputSize = 0;
        for(int i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            if(item.isEmpty()) continue;
            inputSize = item.getCount();
            break;
        }
        return new ItemStack(output.getItem(), output.getCount() * inputSize);
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= input.size();
    }

    @Override
    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return input;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public void render(PoseStack poseStack, Screen screen, int mouseX, int mouseY, float partial, int xOffset, int yOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, RECIPE_VIEWER_LOCATION);
        int x = 9;
        int y = 6;
        int u = 38;
        int height = 18;
        switch(input.size()) {
            case 1 -> y += 27;
            case 2 -> {
                y += 18;
                u += 18;
                height += 18;
            }
            case 3 -> {
                y += 9;
                u += 36;
                height += 36;
            }
            default -> {
                u += 54;
                height += 54;
            }
        }
        Screen.blit(poseStack, x, y, u, 0, 18, height, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT);
        Screen.blit(poseStack, 30, 32, 0, 0, 37, 20, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT);
        x += 1 + xOffset;
        y += 1 + yOffset;
        for(Ingredient ingredient : input) {
            ItemStack item = LevelUtil.chooseUnlockedIngredient(ingredient, ClientEngine.get().getPlayer());
            Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(item, x, y);
            if(onItem(x, y, mouseX, mouseY)) {
                screen.renderTooltip(poseStack, screen.getTooltipFromItem(item), item.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
            }
            y += 18;
        }
        x = xOffset + 50;
        y = yOffset + 34;
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(output, x, y);
        Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, output, x, y);
        if(onItem(x, y, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(output), output.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
    }

    @Override
    public ItemStack clickItem(Screen screen, int mouseX, int mouseY) {
        int x = 9 + 1;
        int y = 6 + 1;
        switch(input.size()) {
            case 1 -> y += 27;
            case 2 -> y += 18;
            case 3 -> y += 9;
        }
        for(Ingredient ingredient : input) {
            if(onItem(x, y, mouseX, mouseY)) {
                return LevelUtil.chooseUnlockedIngredient(ingredient, ClientEngine.get().getPlayer());
            }
            y += 18;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public TranslatableComponent getTitle() {
        return new TranslatableComponent(Nightfall.MODID + ".barrel");
    }

    @Override
    public boolean showInRecipeViewer() {
        return getRequirementId() != null;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<BarrelRecipe> {
        Serializer() {
            this.setRegistryName(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "barrel"));
        }

        @Override
        public BarrelRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray inputJson = GsonHelper.getAsJsonArray(json, "input");
            Ingredient[] ingredients = new Ingredient[inputJson.size()];
            for(int i = 0; i < inputJson.size(); i++) {
                ingredients[i] = Ingredient.fromJson(inputJson.getAsJsonArray().get(i));
            }
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            int soakTime = GsonHelper.getAsInt(json, "soakTime", 0);
            boolean fixedSoakTime = GsonHelper.getAsBoolean(json, "fixedSoakTime", false);
            ResourceLocation requirement = json.has("requirement") ? ResourceLocation.parse(json.get("requirement").getAsString()) : null;
            return new BarrelRecipe(id, requirement, NonNullList.of(Ingredient.EMPTY, ingredients), output, soakTime, fixedSoakTime);
        }

        @Override
        public BarrelRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            int inputSize = buf.readVarInt();
            Ingredient[] ingredients = new Ingredient[inputSize];
            for(int i = 0; i < inputSize; i++) {
                ingredients[i] = Ingredient.fromNetwork(buf);
            }
            ResourceLocation requirement = buf.readResourceLocation();
            return new BarrelRecipe(id, requirement.getPath().equals("empty") ? null : requirement, NonNullList.of(Ingredient.EMPTY, ingredients), buf.readItem(), buf.readVarInt(), buf.readBoolean());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, BarrelRecipe recipe) {
            buf.writeVarInt(recipe.input.size());
            for(Ingredient ingredient : recipe.input) ingredient.toNetwork(buf);
            buf.writeResourceLocation(recipe.getRequirementId() == null ? ResourceLocation.parse("empty") : recipe.getRequirementId());
            buf.writeItem(recipe.output);
            buf.writeVarInt(recipe.soakTime);
            buf.writeBoolean(recipe.fixedSoakTime);
        }
    }
}
