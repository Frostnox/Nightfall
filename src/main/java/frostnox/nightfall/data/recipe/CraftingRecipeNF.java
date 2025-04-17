package frostnox.nightfall.data.recipe;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.inventory.RecipeViewerComponent;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Map;
import java.util.Set;

public class CraftingRecipeNF extends EncyclopediaRecipe<CraftingContainer> implements CraftingRecipe, IShapedRecipe<CraftingContainer>, IRenderableRecipe {
    public static RecipeType<CraftingRecipeNF> TYPE = RecipeType.register(Nightfall.MODID + ":crafting");
    public static final Serializer SERIALIZER = new Serializer();
    public static final ResourceLocation RECIPE_VIEWER_LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/recipe_crafting.png");
    private final NonNullList<Ingredient> input;
    private final ItemStack output;
    private final int width, height;

    public CraftingRecipeNF(ResourceLocation id, int width, int height, NonNullList<Ingredient> input, ItemStack output, ResourceLocation requirement) {
        super(id, requirement);
        this.width = width;
        this.height = height;
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level level) {
        if(super.matches(inventory, level)) {
            for(int i = 0; i <= 3 - this.width; ++i) {
                for(int j = 0; j <= 3 - this.height; ++j) {
                    if(this.matches(inventory, i, j, true)) return true;
                    if(this.matches(inventory, i, j, false)) return true;
                }
            }
        }
        return false;
    }

    private boolean matches(CraftingContainer inventory, int p_44172_, int p_44173_, boolean p_44174_) {
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 3; ++j) {
                int k = i - p_44172_;
                int l = j - p_44173_;
                Ingredient ingredient = Ingredient.EMPTY;
                if (k >= 0 && l >= 0 && k < this.width && l < this.height) {
                    if(p_44174_) ingredient = this.input.get(this.width - k - 1 + l * this.width);
                    else ingredient = this.input.get(k + l * this.width);
                }
                if(!ingredient.test(inventory.getItem(i + j * 3))) return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.width && height >= this.height;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return input;
    }

    @Override
    public ItemStack getResultItem() {
        return output;
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
    public int getRecipeWidth() {
        return width;
    }

    @Override
    public int getRecipeHeight() {
        return height;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer pContainer) {
        return NonNullList.withSize(pContainer.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public void render(PoseStack poseStack, Screen screen, int mouseX, int mouseY, float partial, int xOffset, int yOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, RECIPE_VIEWER_LOCATION);
        Screen.blit(poseStack, 0, 0, 0, 0, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT);
        int xPos = xOffset + 10;
        int yPos = yOffset + 16;
        int x, y;
        for(int i = 0; i < input.size(); i++) {
            if(!input.get(i).isEmpty()) {
                x = xPos + (i % width) * 18;
                y = yPos + (i / width) * 18;
                ItemStack item = LevelUtil.chooseUnlockedIngredient(input.get(i), ClientEngine.get().getPlayer());
                Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(item, x, y);
                if(onItem(x, y, mouseX, mouseY)) {
                    screen.renderTooltip(poseStack, screen.getTooltipFromItem(item), item.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
                }
            }
        }
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(output, xOffset + 86, yOffset + 34);
        if(onItem(xOffset + 86, yOffset + 34, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(output), output.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
    }

    @Override
    public ItemStack clickItem(Screen screen, int mouseX, int mouseY) {
        int xPos = 10;
        int yPos = 16;
        int x, y;
        for(int i = 0; i < input.size(); i++) {
            if(!input.get(i).isEmpty()) {
                x = xPos + (i % width) * 18;
                y = yPos + (i / width) * 18;
                if(onItem(x, y, mouseX, mouseY)) {
                    return LevelUtil.chooseUnlockedIngredient(input.get(i), ClientEngine.get().getPlayer());
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public TranslatableComponent getTitle() {
        return new TranslatableComponent("container.crafting");
    }

    @Override
    public boolean showInRecipeViewer() {
        return getRequirementId() != null;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<CraftingRecipeNF> {
        Serializer() {
            this.setRegistryName(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "crafting"));
        }

        @Override
        public CraftingRecipeNF fromJson(ResourceLocation id, JsonObject json) {
            //Map symbols to ingredients
            Map<String, Ingredient> map = Maps.newHashMap();
            for(Map.Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(json, "key").entrySet()) {
                if(entry.getKey().length() != 1) throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
                if(" ".equals(entry.getKey())) throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
                map.put(entry.getKey(), Ingredient.fromJson(entry.getValue()));
            }
            map.put(" ", Ingredient.EMPTY);
            //Get patterns
            JsonArray patterns = GsonHelper.getAsJsonArray(json, "pattern");
            String[] patternsString = new String[patterns.size()];
            if(patternsString.length > 3) throw new JsonSyntaxException("Invalid pattern: too many rows, " + 3 + " is maximum");
            else if(patternsString.length == 0) throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
            else {
                for(int i = 0; i < patternsString.length; ++i) {
                    String s = GsonHelper.convertToString(patterns.get(i), "pattern[" + i + "]");
                    if(s.length() > 3) throw new JsonSyntaxException("Invalid pattern: too many columns, " + 3 + " is maximum");
                    if(i > 0 && patternsString[0].length() != s.length()) throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
                    patternsString[i] = s;
                }
            }
            //Shrink patterns
            /*int i = Integer.MAX_VALUE;
            int j = -1;
            int k = 0;
            int l = 0;
            for(int i1 = 0; i1 < patternsString.length; ++i1) {
                String s = patternsString[i1];
                for(int m = 0; m < s.length() && m < i; m++) if(s.charAt(m) != ' ') i = m;
                for(int n = s.length() - 1; n >= 0; n--) if(s.charAt(n) != ' ') j = n;
                if(j == -1) {
                    if(k == i1) ++k;
                    ++l;
                    j = 0;
                }
                else l = 0;
            }
            if(patternsString.length == l) patternsString = new String[0];
            else {
                String[] temp = new String[patternsString.length - l - k];
                for(int k1 = 0; k1 < temp.length; ++k1) temp[k1] = patternsString[k1 + k].substring(i, j + 1);
                patternsString = temp;
            }*/
            //Construct params
            int width = patternsString[0].length();
            int height = patternsString.length;
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            Set<String> keys = Sets.newHashSet(map.keySet());
            keys.remove(" ");
            for(int m = 0; m < patternsString.length; ++m) {
                for(int n = 0; n < patternsString[m].length(); ++n) {
                    String s = patternsString[m].substring(n, n + 1);
                    Ingredient ingredient = map.get(s);
                    if(ingredient == null) throw new JsonSyntaxException("Pattern references symbol '" + s + "' but it's not defined in the key");
                    keys.remove(s);
                    ingredients.set(n + width * m, ingredient);
                }
            }
            if(!keys.isEmpty()) throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            ResourceLocation requirement = null;
            if(json.has("requirement")) requirement = ResourceLocation.parse(json.get("requirement").getAsString());
            return new CraftingRecipeNF(id, width, height, ingredients, result, requirement);
        }

        @Override
        public CraftingRecipeNF fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            int width = buf.readVarInt();
            int height = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for(int k = 0; k < ingredients.size(); ++k) ingredients.set(k, Ingredient.fromNetwork(buf));
            ItemStack result = buf.readItem();
            ResourceLocation requirementId = buf.readResourceLocation();
            return new CraftingRecipeNF(id, width, height, ingredients, result, requirementId.getPath().equals("empty") ? null : requirementId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CraftingRecipeNF recipe) {
            buf.writeVarInt(recipe.getRecipeWidth());
            buf.writeVarInt(recipe.getRecipeHeight());
            for(Ingredient ingredient : recipe.getIngredients()) ingredient.toNetwork(buf);
            buf.writeItem(recipe.getResultItem());
            buf.writeResourceLocation(recipe.getRequirementId() == null ? ResourceLocation.parse("empty") : recipe.getRequirementId());
        }
    }
}
