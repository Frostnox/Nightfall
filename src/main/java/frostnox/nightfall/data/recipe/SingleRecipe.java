package frostnox.nightfall.data.recipe;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.inventory.RecipeViewerComponent;
import frostnox.nightfall.util.DataUtil;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class SingleRecipe extends EncyclopediaRecipe<RecipeWrapper> implements IRenderableRecipe {
    public static final ResourceLocation RECIPE_VIEWER_LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/recipe_single.png");
    private final Ingredient input;
    private final ItemStack output;
    private final FluidStack fluidOutput;
    private final int cookTime;

    protected SingleRecipe(ResourceLocation id, ResourceLocation requirement, Ingredient input, ItemStack output, FluidStack fluidOutput, int cookTime) {
        super(id, requirement);
        this.input = input;
        this.output = output;
        this.fluidOutput = fluidOutput;
        this.cookTime = cookTime;
    }

    @Override
    public boolean matches(RecipeWrapper inventory, Level level) {
        if(super.matches(inventory, level)) {
            for(int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if(input.test(item)) return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(RecipeWrapper container) {
        int count = 0;
        for(int i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            count += item.getCount();
        }
        return new ItemStack(output.getItem(), output.getCount() * count);
    }

    public FluidStack assembleFluid(RecipeWrapper container) {
        int count = 0;
        for(int i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            count += item.getCount();
        }
        return new FluidStack(fluidOutput.getFluid(), fluidOutput.getAmount() * count);
    }

    public float getTemperature() {
        if(!fluidOutput.isEmpty() && !output.isEmpty()) return Math.max(fluidOutput.getRawFluid().getAttributes().getTemperature(), TieredHeat.getMinimumTemp(output));
        else if(!output.isEmpty()) return TieredHeat.getMinimumTemp(output);
        else return fluidOutput.getRawFluid().getAttributes().getTemperature();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth >= 1 && pHeight >= 1;
    }

    public Ingredient getInput() {
        return input;
    }

    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    public FluidStack getResultFluid() {
        return fluidOutput.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input);
    }

    public int getCookTime() {
        return cookTime;
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
        if(onItem(x, y, mouseX, mouseY)) return LevelUtil.chooseUnlockedIngredient(input, ClientEngine.get().getPlayer());
        return ItemStack.EMPTY;
    }

    public static class Serializer<T extends SingleRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {
        interface Factory<T extends SingleRecipe> {
            T create(ResourceLocation id, ResourceLocation requirement, Ingredient input, ItemStack output, FluidStack fluidOutput, int cookTime);
        }
        private final SingleRecipe.Serializer.Factory<T> factory;

        Serializer(SingleRecipe.Serializer.Factory<T> factory, String name) {
            this.factory = factory;
            this.setRegistryName(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, name));
        }

        @Override
        public T fromJson(ResourceLocation id, JsonObject json) {
            ResourceLocation requirement = null;
            if(json.has("requirement")) requirement = ResourceLocation.parse(json.get("requirement").getAsString());
            Ingredient input = Ingredient.fromJson(json.get("input"));
            ItemStack output = json.has("output") ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output")) : ItemStack.EMPTY;
            FluidStack fluidOutput = json.has("fluidOutput") ? DataUtil.fluidStackFromJson(GsonHelper.getAsJsonObject(json, "fluidOutput")) : FluidStack.EMPTY;
            int cookTime = GsonHelper.getAsInt(json, "cookTime", 0);
            return factory.create(id, requirement, input, output, fluidOutput, cookTime);
        }

        @Override
        public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ResourceLocation requirement = buf.readResourceLocation();
            return factory.create(id, requirement.getPath().equals("empty") ? null : requirement, Ingredient.fromNetwork(buf), buf.readItem(), buf.readFluidStack(), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, T recipe) {
            buf.writeResourceLocation(recipe.getRequirementId() == null ? ResourceLocation.parse("empty") : recipe.getRequirementId());
            recipe.getInput().toNetwork(buf);
            buf.writeItem(recipe.getResultItem());
            buf.writeFluidStack(recipe.getResultFluid());
            buf.writeVarInt(recipe.getCookTime());
        }
    }
}
