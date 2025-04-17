package frostnox.nightfall.data.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;

public interface IEncyclopediaRecipe {
    /**
     * @return id for an entry or knowledge
     */
    @Nullable ResourceLocation getRequirementId();

    boolean isUnlocked(@Nullable Player player);

    NonNullList<Ingredient> getUnlockIngredients();
}
