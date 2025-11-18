package frostnox.nightfall.data.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;
import oshi.util.tuples.Pair;

public class FurnaceRecipe extends PercentageMixtureRecipe {
    public static final RecipeType<FurnaceRecipe> TYPE = RecipeType.register(Nightfall.MODID + ":furnace");
    public static final Serializer<FurnaceRecipe> SERIALIZER = new Serializer<>(FurnaceRecipe::new, "furnace");

    public FurnaceRecipe(ResourceLocation id, ResourceLocation requirement, NonNullList<Pair<Ingredient, Vec2>> input, ItemStack itemOutput, FluidStack fluidOutput, int unitsPerOutput, int cookTime, int priority) {
        super(id, requirement, input, itemOutput, fluidOutput, unitsPerOutput, cookTime, priority);
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

    }

    @Override
    public ItemStack clickItem(Screen screen, int mouseX, int mouseY) {
        return ItemStack.EMPTY;
    }

    @Override
    public TranslatableComponent getTitle() {
        return new TranslatableComponent(Nightfall.MODID + ".furnace");
    }
}
