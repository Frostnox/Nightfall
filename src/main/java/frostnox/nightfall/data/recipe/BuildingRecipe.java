package frostnox.nightfall.data.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.inventory.RecipeViewerComponent;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class BuildingRecipe extends EncyclopediaRecipe<RecipeWrapper> implements IRenderableRecipe {
    public static final RecipeType<BuildingRecipe> TYPE = RecipeType.register(Nightfall.MODID + ":building");
    public static final Serializer SERIALIZER = new Serializer();
    public static final ResourceLocation RECIPE_VIEWER_LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/recipe_building.png");
    private final String group;
    public final Item baseItem;
    public final Ingredient extraIngredient;
    public final int baseAmount, extraAmount, menuOrder;
    public final Item output;
    private final ItemStack baseItemStack, outputStack;

    public BuildingRecipe(ResourceLocation id, ResourceLocation requirementId, String group, Item baseItem, Ingredient extraIngredient, int baseAmount, int extraAmount, int menuOrder, Item output) {
        super(id, requirementId);
        this.group = group;
        this.baseItem = baseItem;
        this.extraIngredient = extraIngredient;
        this.baseAmount = baseAmount;
        this.extraAmount = extraAmount;
        this.menuOrder = menuOrder;
        this.output = output;
        this.baseItemStack = new ItemStack(baseItem, baseAmount);
        this.outputStack = new ItemStack(output);
    }

    public boolean hasExtraIngredient() {
        return extraAmount > 0;
    }

    @Override
    public boolean matches(RecipeWrapper inventory, Level level) {
        if(super.matches(inventory, level)) {
            Player player = ForgeHooks.getCraftingPlayer();
            if(player != null) {
                IPlayerData capP = PlayerData.get(player);
                ItemStack heldItem = player.getItemInHand(capP.getActiveHand());
                if(heldItem.is(baseItem) && heldItem.getCount() >= baseAmount) {
                    if(hasExtraIngredient()) {
                        for(int i = 0; i < inventory.getContainerSize(); i++) {
                            ItemStack item = inventory.getItem(i);
                            if(extraIngredient.test(item) && item.getCount() >= extraAmount) return true;
                        }
                    }
                    else return true;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(RecipeWrapper inventory) {
        return new ItemStack(output);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.of(baseItem));
        if(hasExtraIngredient()) list.add(extraIngredient);
        return list;
    }

    @Override
    public ItemStack getResultItem() {
        return new ItemStack(output);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    public static Item itemFromJson(JsonObject pItemObject, String name) {
        String s = GsonHelper.getAsString(pItemObject, name);
        Item item = Registry.ITEM.getOptional(ResourceLocation.parse(s)).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown item '" + s + "'");
        });
        if (item == Items.AIR) {
            throw new JsonSyntaxException("Invalid item: " + s);
        } else {
            return item;
        }
    }

    @Override
    public void render(PoseStack poseStack, Screen screen, int mouseX, int mouseY, float partial, int xOffset, int yOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, RECIPE_VIEWER_LOCATION);
        Screen.blit(poseStack, 0, 0, 0, 0, 26, RecipeViewerComponent.HEIGHT, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT);
        if(hasExtraIngredient()) {
            Screen.blit(poseStack, 26, 34, 8, 34, 18, 18, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT);
            Screen.blit(poseStack, 44, 0, 26, 0, RecipeViewerComponent.WIDTH - 44, RecipeViewerComponent.HEIGHT, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT);
        }
        else Screen.blit(poseStack, 26, 0, 26, 0, RecipeViewerComponent.WIDTH - 26, RecipeViewerComponent.HEIGHT, RecipeViewerComponent.WIDTH, RecipeViewerComponent.HEIGHT);
        int x = xOffset + 10;
        int y = yOffset + 34;
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(baseItemStack, x, y);
        Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, baseItemStack, x, y);
        if(onItem(x, y, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(baseItemStack), baseItemStack.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
        if(hasExtraIngredient()) {
            x += 18;
            ItemStack extraItem = LevelUtil.chooseUnlockedIngredient(extraIngredient, ClientEngine.get().getPlayer());
            extraItem.setCount(extraAmount);
            Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(extraItem, x, y);
            Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, extraItem, x, y);
            if(onItem(x, y, mouseX, mouseY)) {
                screen.renderTooltip(poseStack, screen.getTooltipFromItem(extraItem), extraItem.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
            }
        }
        x += 40;
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(outputStack, x, y);
        if(onItem(x, y, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(outputStack), outputStack.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
    }

    @Override
    public ItemStack clickItem(Screen screen, int mouseX, int mouseY) {
        int x = 10;
        int y = 34;
        if(onItem(x, y, mouseX, mouseY)) return baseItemStack;
        if(hasExtraIngredient()) {
            x += 18;
            if(onItem(x, y, mouseX, mouseY)) {
                return LevelUtil.chooseUnlockedIngredient(extraIngredient, ClientEngine.get().getPlayer());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public TranslatableComponent getTitle() {
        return new TranslatableComponent(Nightfall.MODID + ".building");
    }

    @Override
    public boolean showInRecipeViewer() {
        return getRequirementId() != null;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<BuildingRecipe> {
        Serializer() {
            this.setRegistryName(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "building"));
        }

        @Override
        public BuildingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "type", "");

            ResourceLocation requirement = null;
            if(json.has("requirement")) requirement = ResourceLocation.parse(json.get("requirement").getAsString());
            Item baseItem = ShapedRecipe.itemFromJson(json);
            int baseAmount = GsonHelper.getAsInt(json, "baseAmount");
            int extraAmount = GsonHelper.getAsInt(json, "extraAmount", 0);
            Ingredient extraIngredient = extraAmount > 0 ? Ingredient.fromJson(json.get("extraIngredient")) : null;
            Item output = itemFromJson(json, "outputItem");
            int menuOrder = GsonHelper.getAsInt(json, "menuOrder", -1);

            if(baseAmount < 1) throw new IllegalStateException("Base ingredient amount is less than 1 for building recipe " + id);

            return new BuildingRecipe(id, requirement, group, baseItem, extraIngredient, baseAmount, extraAmount, menuOrder, output);
        }

        @Override
        public BuildingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            ResourceLocation requirement = buf.readResourceLocation();
            int baseAmount = buf.readVarInt();
            ResourceLocation itemLoc = buf.readResourceLocation();
            Item baseItem =  Registry.ITEM.getOptional(itemLoc).orElseThrow(() -> {
                return new JsonSyntaxException("Unknown item '" + itemLoc + "'");
            });
            int extraAmount = buf.readVarInt();
            Ingredient extraIngredient = extraAmount > 0 ? Ingredient.fromNetwork(buf) : null;
            ResourceLocation outputLoc = buf.readResourceLocation();
            Item output =  Registry.ITEM.getOptional(outputLoc).orElseThrow(() -> {
                return new JsonSyntaxException("Unknown item '" + outputLoc + "'");
            });
            int menuOrder = buf.readVarInt();
            return new BuildingRecipe(id, requirement.getPath().equals("empty") ? null : requirement, group, baseItem, extraIngredient, baseAmount, extraAmount, menuOrder, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, BuildingRecipe recipe) {
            buf.writeUtf(recipe.getGroup());
            buf.writeResourceLocation(recipe.getRequirementId() == null ? ResourceLocation.parse("empty") : recipe.getRequirementId());
            buf.writeVarInt(recipe.baseAmount);
            buf.writeResourceLocation(recipe.baseItem.getRegistryName());
            buf.writeVarInt(recipe.extraAmount);
            if(recipe.extraAmount > 0) recipe.extraIngredient.toNetwork(buf);
            buf.writeResourceLocation(recipe.output.getRegistryName());
            buf.writeVarInt(recipe.menuOrder);
        }
    }
}
