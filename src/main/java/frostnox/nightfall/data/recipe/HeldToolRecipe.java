package frostnox.nightfall.data.recipe;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.Optional;

public class HeldToolRecipe extends ToolIngredientRecipe {
    public static final RecipeType<HeldToolRecipe> TYPE = RecipeType.register(Nightfall.MODID + ":held_tool");
    public static final Serializer<HeldToolRecipe> SERIALIZER = new Serializer<>(HeldToolRecipe::new, "held_tool");

    public static Optional<HeldToolRecipe> getRecipe(Player player) {
        IPlayerData capP = PlayerData.get(player);
        capP.setHeldItemForRecipe(player.getItemInHand(capP.getActiveHand()));
        RecipeWrapper container = new RecipeWrapper(new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, player.getItemInHand(capP.getOppositeActiveHand()))));
        ForgeHooks.setCraftingPlayer(player);
        Optional<HeldToolRecipe> recipe = player.level.getRecipeManager().getRecipeFor(TYPE, container, player.level);
        ForgeHooks.setCraftingPlayer(null);
        capP.setHeldItemForRecipe(ItemStack.EMPTY);
        return recipe;
    }

    public HeldToolRecipe(ResourceLocation id, ResourceLocation requirement, Ingredient input, Ingredient tool, ItemStack output) {
        super(id, requirement, input, tool, output);
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
    public TranslatableComponent getTitle() {
        return new TranslatableComponent(Nightfall.MODID + ".held_tool");
    }
}
