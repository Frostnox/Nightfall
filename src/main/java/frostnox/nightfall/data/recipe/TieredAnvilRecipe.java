package frostnox.nightfall.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlockEntity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class TieredAnvilRecipe extends EncyclopediaRecipe<RecipeWrapper> implements IRenderableRecipe {
    public static final RecipeType<TieredAnvilRecipe> TYPE = RecipeType.register(Nightfall.MODID + ":anvil");
    public static final Serializer SERIALIZER = new Serializer();
    public static final ResourceLocation RECIPE_VIEWER_LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/recipe_tiered_anvil.png");
    private final int[][][] startShape;
    private final boolean[][][] finishShape;
    private final NonNullList<Ingredient> input;
    private final ItemStack output;
    private final int tier, randMin, randMax;
    private final float slagChance;

    public TieredAnvilRecipe(ResourceLocation id, ResourceLocation requirement, int[][][] startShape, boolean[][][] finishShape, NonNullList<Ingredient> input, ItemStack output, int tier, int randMin, int randMax, float slagChance) {
        super(id, requirement);
        this.startShape = startShape;
        this.finishShape = finishShape;
        this.input = input;
        this.output = output;
        this.tier = tier;
        this.randMin = randMin;
        this.randMax = randMax;
        this.slagChance = slagChance;
    }

    public int getTier() {
        return tier;
    }

    public int getRandMin() {
        return randMin;
    }

    public int getRandMax() {
        return randMax;
    }

    public float getSlagChance() {
        return slagChance;
    }

    public int[][][] getStartShape() {
        return startShape;
    }

    public boolean[][][] getFinishShape() {
        return finishShape;
    }

    @Override
    public boolean matches(RecipeWrapper inventory, Level level) {
        if(!super.matches(inventory, level)) return false;
        ItemStack stack1 = inventory.getItem(0);
        ItemStack stack2 = inventory.getItem(1);
        ItemStack stack3 = inventory.getItem(2);
        int empty = 0;
        if(stack1.isEmpty()) empty++;
        if(stack2.isEmpty()) empty++;
        if(stack3.isEmpty()) empty++;
        Ingredient ingredient1 = input.get(0);
        if(input.size() == 3) {
            if(empty != 0) return false;
            else return ingredient1.test(stack1) && input.get(1).test(stack2) && input.get(2).test(stack3);
        }
        else if(input.size() == 2) {
            if(empty != 1) return false;
            Ingredient ingredient2 = input.get(1);
            if(ingredient1.test(stack1)) return ingredient2.test(stack2);
            else if(ingredient1.test(stack2)) return ingredient2.test(stack3);
            else return false;
        }
        else {
            if(empty != 2) return false;
            else return ingredient1.test(stack1) || ingredient1.test(stack2) || ingredient1.test(stack3);
        }
    }

    @Override
    public ItemStack assemble(RecipeWrapper inventory) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width == 1 && height <= this.input.size();
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
    public void render(PoseStack poseStack, Screen screen, int mouseX, int mouseY, float partial, int xOffset, int yOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, RECIPE_VIEWER_LOCATION);
        Screen.blit(poseStack, 0, 0, 0, 0, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT);
        int xPos = xOffset + 10;
        int yPos = yOffset + 16;
        int y;
        for(int i = 0; i < input.size(); i++) {
            if(!input.get(i).isEmpty()) {
                y = yPos + i * 18;
                ItemStack item = LevelUtil.chooseUnlockedIngredient(input.get(i), ClientEngine.get().getPlayer());
                Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(item, xPos, y);
                if(onItem(xPos, y, mouseX, mouseY)) {
                    screen.renderTooltip(poseStack, screen.getTooltipFromItem(item), item.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
                }
            }
        }
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(output, xOffset + 50, yOffset + 34);
        if(onItem(xOffset + 50, yOffset + 34, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(output), output.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
    }

    @Override
    public ItemStack clickItem(Screen screen, int mouseX, int mouseY) {
        int xPos = 10;
        int yPos = 16;
        int y;
        for(int i = 0; i < input.size(); i++) {
            if(!input.get(i).isEmpty()) {
                y = yPos + i * 18;
                if(onItem(xPos, y, mouseX, mouseY)) {
                    return LevelUtil.chooseUnlockedIngredient(input.get(i), ClientEngine.get().getPlayer());
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public TranslatableComponent getTitle() {
        return new TranslatableComponent(Nightfall.MODID + ".anvil");
    }

    @Override
    public boolean showInRecipeViewer() {
        return getRequirementId() != null;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<TieredAnvilRecipe> {
        Serializer() {
            this.setRegistryName(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "anvil"));
        }

        @Override
        public TieredAnvilRecipe fromJson(ResourceLocation id, JsonObject json) {
            int[][][] gridStart = buildIntGrid(json, "gridStart");
            boolean[][][] gridFinish = buildBooleanGrid(json, "gridFinish");

            NonNullList<Ingredient> ingredients = NonNullList.create();
            JsonArray input = GsonHelper.getAsJsonArray(json, "input");
            for(int i = 0; i < input.size(); i++) {
                Ingredient ingredient = Ingredient.fromJson(input.get(i));
                if(!ingredient.isEmpty()) ingredients.add(ingredient);
            }
            if(ingredients.isEmpty()) throw new JsonSyntaxException("No ingredients defined for anvil recipe.");
            else if(ingredients.size() > 3) throw new JsonSyntaxException("Amount of ingredients exceeds maximum of 3 for anvil recipe.");

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int tier = GsonHelper.getAsInt(json, "tier");
            if(tier < 0) throw new JsonSyntaxException("Invalid tier: must be at least 0");
            ResourceLocation requirement = null;
            if(json.has("requirement")) requirement = ResourceLocation.parse(json.get("requirement").getAsString());
            int randMin = GsonHelper.getAsInt(json, "randMin", 0);
            int randMax = GsonHelper.getAsInt(json, "randMax", 1024);
            float slagChance = GsonHelper.getAsFloat(json, "slagChance", 0);
            if(slagChance < 0F || slagChance > 1F) throw new JsonSyntaxException("Slag chance " + slagChance + " must be between 0 and 1.");
            return new TieredAnvilRecipe(id, requirement, gridStart, gridFinish, ingredients, result, tier, randMin, randMax, slagChance);
        }

        private int[][][] buildIntGrid(JsonObject json, String name) {
            int[][][] grid = new int[TieredAnvilBlockEntity.GRID_X][TieredAnvilBlockEntity.GRID_Y][TieredAnvilBlockEntity.GRID_Z];
            JsonArray levels = GsonHelper.getAsJsonArray(json, name);
            if(levels.size() > TieredAnvilBlockEntity.GRID_Y) throw new JsonSyntaxException("Amount of grid levels exceeds maximum of " + TieredAnvilBlockEntity.GRID_Y + " for anvil recipe.");
            for(int y = 0; y < levels.size(); y++) {
                String level = GsonHelper.convertToString(levels.get(y), name + "[" + y + "]");
                if(level.length() != TieredAnvilBlockEntity.GRID_X * TieredAnvilBlockEntity.GRID_Z) throw new JsonSyntaxException("Grid shape for level " + y + " in anvil recipe is not fully defined.");
                for(int i = 0; i < level.length(); i++) {
                    if(level.charAt(i) != 'o') {
                        int x = i % TieredAnvilBlockEntity.GRID_X;
                        int z = i / TieredAnvilBlockEntity.GRID_X;
                        grid[x][y][z] = level.charAt(i) == 'X' ? 1 : 2;
                    }
                }
            }
            return grid;
        }

        private boolean[][][] buildBooleanGrid(JsonObject json, String name) {
            boolean[][][] grid = new boolean[TieredAnvilBlockEntity.GRID_X][TieredAnvilBlockEntity.GRID_Y][TieredAnvilBlockEntity.GRID_Z];
            JsonArray levels = GsonHelper.getAsJsonArray(json, name);
            if(levels.size() > TieredAnvilBlockEntity.GRID_Y) throw new JsonSyntaxException("Amount of grid levels exceeds maximum of " + TieredAnvilBlockEntity.GRID_Y + " for anvil recipe.");
            for(int y = 0; y < levels.size(); y++) {
                String level = GsonHelper.convertToString(levels.get(y), name + "[" + y + "]");
                if(level.length() != TieredAnvilBlockEntity.GRID_X * TieredAnvilBlockEntity.GRID_Z) throw new JsonSyntaxException("Grid shape for level " + y + " in anvil recipe is not fully defined.");
                for(int i = 0; i < level.length(); i++) {
                    if(level.charAt(i) == 'X') {
                        int x = i % TieredAnvilBlockEntity.GRID_X;
                        int z = i / TieredAnvilBlockEntity.GRID_X;
                        grid[x][y][z] = true;
                    }
                }
            }
            return grid;
        }

        @Override
        public TieredAnvilRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ResourceLocation requirement = buf.readResourceLocation();
            int[][][] startShape = new int[TieredAnvilBlockEntity.GRID_X][TieredAnvilBlockEntity.GRID_Y][TieredAnvilBlockEntity.GRID_Z];
            boolean[][][] finishShape = new boolean[TieredAnvilBlockEntity.GRID_X][TieredAnvilBlockEntity.GRID_Y][TieredAnvilBlockEntity.GRID_Z];
            for(int x = 0; x < TieredAnvilBlockEntity.GRID_X; x++) {
                for(int y = 0; y < TieredAnvilBlockEntity.GRID_Y; y++) {
                    for(int z = 0; z < TieredAnvilBlockEntity.GRID_Z; z++) {
                        startShape[x][y][z] = buf.readVarInt();
                        finishShape[x][y][z] = buf.readBoolean();
                    }
                }
            }
            int ingredientsAmount = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientsAmount, Ingredient.EMPTY);
            for(int k = 0; k < ingredients.size(); ++k) ingredients.set(k, Ingredient.fromNetwork(buf));
            ItemStack result = buf.readItem();
            int tier = buf.readVarInt();
            return new TieredAnvilRecipe(id, requirement.getPath().equals("empty") ? null : requirement, startShape, finishShape, ingredients, result, tier, buf.readVarInt(), buf.readVarInt(), buf.readFloat());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, TieredAnvilRecipe recipe) {
            buf.writeResourceLocation(recipe.getRequirementId() == null ? ResourceLocation.parse("empty") : recipe.getRequirementId());
            for(int x = 0; x < TieredAnvilBlockEntity.GRID_X; x++) {
                for(int y = 0; y < TieredAnvilBlockEntity.GRID_Y; y++) {
                    for(int z = 0; z < TieredAnvilBlockEntity.GRID_Z; z++) {
                        buf.writeVarInt(recipe.startShape[x][y][z]);
                        buf.writeBoolean(recipe.finishShape[x][y][z]);
                    }
                }
            }
            buf.writeVarInt(recipe.input.size());
            for(Ingredient ingredient : recipe.getIngredients()) ingredient.toNetwork(buf);
            buf.writeItem(recipe.getResultItem());
            buf.writeVarInt(recipe.tier);
            buf.writeVarInt(recipe.randMin);
            buf.writeVarInt(recipe.randMax);
            buf.writeFloat(recipe.slagChance);
        }
    }
}
