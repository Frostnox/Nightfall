package frostnox.nightfall.data.recipe;

import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.encyclopedia.EntryStage;
import frostnox.nightfall.registry.RegistriesNF;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;

public abstract class EncyclopediaRecipe<T extends Container> implements Recipe<T>, IEncyclopediaRecipe {
    private final ResourceLocation id, requirementId;

    public EncyclopediaRecipe(ResourceLocation id, ResourceLocation requirementId) {
        this.id = id;
        this.requirementId = requirementId;
    }

    @Override
    public boolean matches(T pContainer, Level level) {
        return isUnlocked(ForgeHooks.getCraftingPlayer());
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public ResourceLocation getRequirementId() {
        return requirementId;
    }

    @Override
    public NonNullList<Ingredient> getUnlockIngredients() {
        return getIngredients();
    }

    @Override
    public boolean isUnlocked(@Nullable Player player) {
        if(player == null) return true;
        else {
            IPlayerData capP = PlayerData.get(player);
            //Assume entry over knowledge even if both exist with same id (entry names should ideally not conflict with knowledge names)
            if(requirementId == null || (RegistriesNF.getEntries().containsKey(requirementId) ? capP.hasEntryStage(requirementId, EntryStage.COMPLETED) : capP.hasKnowledge(requirementId))) {
                for(Ingredient ingredient : getUnlockIngredients()) {
                    if(ingredient.isEmpty()) continue;
                    boolean hasAny = false;
                    for(ItemStack item : ingredient.getItems()) {
                        ResourceLocation location = ResourceLocation.parse(item.getItem().getRegistryName().toString() + "_item");
                        if(!RegistriesNF.getKnowledge().containsKey(location) || capP.hasKnowledge(location)) {
                            hasAny = true;
                            break;
                        }
                    }
                    if(!hasAny) return false;
                }
                return true;
            }
            return false;
        }
    }
}
