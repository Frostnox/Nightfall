package frostnox.nightfall.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.gui.screen.inventory.RecipeViewerComponent;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.FluidsNF;
import frostnox.nightfall.util.DataUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
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
    public static final ResourceLocation RECIPE_VIEWER_LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/recipe_tiered_anvil.png");
    private final Ingredient input;
    private final int[] work;
    private final TagKey<Fluid> quenchFluid;
    private final ItemStack output;

    public TieredAnvilRecipe(ResourceLocation id, ResourceLocation requirement, Ingredient input, int[] work, TagKey<Fluid> quenchFluid, ItemStack output) {
        super(id, requirement);
        this.input = input;
        this.work = work;
        this.quenchFluid = quenchFluid;
        this.output = output;
    }

    public boolean matchesWorkAndFluid(int[] testWork, Fluid testFluid) {
        if(testFluid.is(quenchFluid) && testWork.length == 8) {
            for(int i = 0; i < testWork.length; i++) {
                if(work[i] != testWork[i]) return false;
            }
            return true;
        }
        return false;
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
        int yPos = yOffset + 16;
        int y;
//        for(int i = 0; i < input.size(); i++) {
//            if(!input.get(i).isEmpty()) {
//                y = yPos + i * 18;
//                ItemStack item = LevelUtil.chooseUnlockedIngredient(input.get(i), ClientEngine.get().getPlayer());
//                Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(item, xPos, y);
//                if(onItem(xPos, y, mouseX, mouseY)) {
//                    screen.renderTooltip(poseStack, screen.getTooltipFromItem(item), item.getTooltipImage(), mouseX - xOffset, mouseY - yOffset);
//                }
//            }
//        }
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
//        for(int i = 0; i < input.size(); i++) {
//            if(!input.get(i).isEmpty()) {
//                y = yPos + i * 18;
//                if(onItem(xPos, y, mouseX, mouseY)) {
//                    return LevelUtil.chooseUnlockedIngredient(input.get(i), ClientEngine.get().getPlayer());
//                }
//            }
//        }
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
            ResourceLocation requirement = null;
            if(json.has("requirement")) requirement = ResourceLocation.parse(json.get("requirement").getAsString());
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            JsonArray workArray = GsonHelper.getAsJsonArray(json, "work");
            int[] work = new int[8];
            if(workArray.size() != work.length) throw new JsonSyntaxException("Work array must have exactly 8 values");
            for(int i = 0; i < work.length; i++) work[i] = workArray.get(i).getAsInt();
            TagKey<Fluid> quenchFluid = json.has("quenchFluid") ? TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), ResourceLocation.parse(json.get("quenchFluid").getAsString())) : TagsNF.FRESHWATER;
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new TieredAnvilRecipe(id, requirement, input, work, quenchFluid, result);
        }

        @Override
        public TieredAnvilRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ResourceLocation requirement = buf.readResourceLocation();
            Ingredient input = Ingredient.fromNetwork(buf);
            int[] work = new int[8];
            for(int i = 0; i < work.length; i++) work[i] = buf.readVarInt();
            TagKey<Fluid> quenchFluid = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), buf.readResourceLocation());
            ItemStack result = buf.readItem();
            return new TieredAnvilRecipe(id, requirement.getPath().equals("empty") ? null : requirement, input, work, quenchFluid, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, TieredAnvilRecipe recipe) {
            buf.writeResourceLocation(recipe.getRequirementId() == null ? ResourceLocation.parse("empty") : recipe.getRequirementId());
            recipe.input.toNetwork(buf);
            for(int i : recipe.work) buf.writeVarInt(i);
            buf.writeResourceLocation(recipe.quenchFluid.location());
            buf.writeItem(recipe.getResultItem());
        }
    }
}
