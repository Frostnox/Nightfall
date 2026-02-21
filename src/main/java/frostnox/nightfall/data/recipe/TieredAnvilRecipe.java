package frostnox.nightfall.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.inventory.RecipeViewerComponent;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class TieredAnvilRecipe extends EncyclopediaRecipe<RecipeWrapper> implements IRenderableRecipe {
    public static final RecipeType<TieredAnvilRecipe> TYPE = RecipeType.register(Nightfall.MODID + ":anvil");
    public static final Serializer SERIALIZER = new Serializer();
    public static final ResourceLocation RECIPE_VIEWER_LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/recipe_anvil.png");
    public final int menuOrder;
    public final Ingredient input;
    public final int[] work;
    public final TagKey<Fluid> quenchFluid;
    public final boolean showInEntry;
    private final ItemStack output;

    public TieredAnvilRecipe(ResourceLocation id, ResourceLocation requirement, Ingredient input, int[] work, TagKey<Fluid> quenchFluid, ItemStack output, int menuOrder, boolean showInEntry) {
        super(id, requirement);
        this.input = input;
        this.work = work;
        this.quenchFluid = quenchFluid;
        this.output = output;
        this.menuOrder = menuOrder;
        this.showInEntry = showInEntry;
    }

    public boolean matchesWorkAndFluid(int[] testWork, Fluid testFluid) {
        if(testFluid.is(quenchFluid) && testWork.length == 11) {
            if(checkWork(testWork)) return true;
            int[] workFlip = testWork.clone();
            workFlip[3] = testWork[7];
            workFlip[4] = testWork[8];
            workFlip[5] = testWork[9];
            workFlip[6] = testWork[10];
            workFlip[7] = testWork[3];
            workFlip[8] = testWork[4];
            workFlip[9] = testWork[5];
            workFlip[10] = testWork[6];
            if(checkWork(workFlip)) return true;
        }
        return false;
    }

    protected boolean checkWork(int[] testWork) {
        for(int i = 0; i < testWork.length; i++) {
            if(work[i] != testWork[i]) return false;
        }
        return true;
    }

    @Override
    public boolean matches(RecipeWrapper inventory, Level level) {
        if(!super.matches(inventory, level)) return false;
        return input.test(inventory.getItem(0));
    }

    @Override
    public ItemStack assemble(RecipeWrapper inventory) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input);
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
        int yPos = yOffset + 34;
        ItemStack item = LevelUtil.chooseUnlockedIngredient(input, ClientEngine.get().getPlayer());
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(item, xPos, yPos);
        if(onItem(xPos, yPos, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(item), item.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
        ItemStack fluid = LevelUtil.chooseUnlockedFluid(quenchFluid, null);
        xPos = xOffset + 50;
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(fluid, xPos, yPos);
        if(onItem(xPos, yPos, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(fluid), fluid.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
        xPos = xOffset + 90;
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(output, xPos, yPos);
        if(onItem(xPos, yPos, mouseX, mouseY)) {
            screen.renderTooltip(poseStack, screen.getTooltipFromItem(output), output.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
        }
    }

    @Override
    public ItemStack clickItem(Screen screen, int mouseX, int mouseY) {
        if(onItem(10, 34, mouseX, mouseY)) {
            return LevelUtil.chooseUnlockedIngredient(input, ClientEngine.get().getPlayer());
        }
        if(onItem(50, 34, mouseX, mouseY)) {
            return LevelUtil.chooseUnlockedFluid(quenchFluid, null);
        }
        if(onItem(90, 34, mouseX, mouseY)) {
            return output;
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

    @Override
    public boolean showInEntry() {
        return showInEntry;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<TieredAnvilRecipe> {
        Serializer() {
            this.setRegistryName(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "anvil"));
        }

        @Override
        public TieredAnvilRecipe fromJson(ResourceLocation id, JsonObject json) {
            ResourceLocation requirement = null;
            if(json.has("requirement")) requirement = ResourceLocation.parse(json.get("requirement").getAsString());
            Ingredient input = Ingredient.fromJson(json.get("input"));
            JsonArray workArray = GsonHelper.getAsJsonArray(json, "work");
            int[] work = new int[11];
            if(workArray.size() != work.length) throw new JsonSyntaxException("Work array must have exactly 11 values");
            for(int i = 0; i < work.length; i++) work[i] = workArray.get(i).getAsInt();
            TagKey<Fluid> quenchFluid = json.has("quenchFluid") ? TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), ResourceLocation.parse(json.get("quenchFluid").getAsString())) : TagsNF.FRESHWATER;
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int menuOrder = GsonHelper.getAsInt(json, "menuOrder", -1);
            boolean showInEntry = GsonHelper.getAsBoolean(json, "showInEntry", true);
            return new TieredAnvilRecipe(id, requirement, input, work, quenchFluid, result, menuOrder, showInEntry);
        }

        @Override
        public TieredAnvilRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ResourceLocation requirement = buf.readResourceLocation();
            Ingredient input = Ingredient.fromNetwork(buf);
            int[] work = new int[11];
            for(int i = 0; i < work.length; i++) work[i] = buf.readVarInt();
            TagKey<Fluid> quenchFluid = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), buf.readResourceLocation());
            ItemStack result = buf.readItem();
            int menuOrder = buf.readVarInt();
            boolean showInEntry = buf.readBoolean();
            return new TieredAnvilRecipe(id, requirement.getPath().equals("empty") ? null : requirement, input, work, quenchFluid, result, menuOrder, showInEntry);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, TieredAnvilRecipe recipe) {
            buf.writeResourceLocation(recipe.getRequirementId() == null ? ResourceLocation.parse("empty") : recipe.getRequirementId());
            recipe.input.toNetwork(buf);
            for(int i : recipe.work) buf.writeVarInt(i);
            buf.writeResourceLocation(recipe.quenchFluid.location());
            buf.writeItem(recipe.getResultItem());
            buf.writeVarInt(recipe.menuOrder);
            buf.writeBoolean(recipe.showInEntry);
        }
    }
}
