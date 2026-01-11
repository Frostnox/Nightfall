package frostnox.nightfall.item.item;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.item.SimpleModifiableItemScreen;
import frostnox.nightfall.client.gui.screen.item.ModifiableItemScreen;
import frostnox.nightfall.data.recipe.HeldToolRecipe;
import frostnox.nightfall.data.recipe.ToolIngredientRecipe;
import frostnox.nightfall.item.IActionableItem;
import frostnox.nightfall.item.client.IModifiable;
import frostnox.nightfall.item.client.ISwapBehavior;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ToolItem extends ScreenCacheItem implements IModifiable, ISwapBehavior, IActionableItem {
    public ToolItem(Properties properties) {
        super(properties);
    }

    public abstract @Nullable RegistryObject<? extends Action> getRecipeAction();

    public List<ToolIngredientRecipe> getRecipes(Level level, @Nullable Player player, ItemStack otherItem) {
        return level.getRecipeManager().getAllRecipesFor(HeldToolRecipe.TYPE).stream()
                .filter((toolIngredientRecipe -> toolIngredientRecipe.getTool().test(new ItemStack(this)) && toolIngredientRecipe.getInput().test(otherItem) && toolIngredientRecipe.isUnlocked(player)))
                .sorted((r1, r2) -> {
                    if(r1.menuOrder < 0 && r2.menuOrder >= 0) return 1;
                    else if(r2.menuOrder < 0 && r1.menuOrder >= 0) return -1;
                    else if(r1.menuOrder == r2.menuOrder || r1.menuOrder < 0) return -r1.output.getDescriptionId().compareTo(r2.output.getDescriptionId());
                    else return r1.menuOrder > r2.menuOrder ? 1 : -1;
                })
                .collect(Collectors.toList());
    }

    protected boolean canUseSelectedItem(int index, Level level, Player player, InteractionHand hand) {
        List<ToolIngredientRecipe> recipes = getRecipes(level, player, player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
        return index >= 0 && index < recipes.size();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if(worldIn.isClientSide) {
            if(isSelected) {
                if(entityIn instanceof Player player && PlayerData.isPresent(player)) {
                    boolean oldCanUse = ClientEngine.get().canUseModifiableMain;
                    ClientEngine.get().canUseModifiableMain = canUseSelectedItem(ClientEngine.get().getModifiableIndexMain(), worldIn, player, InteractionHand.MAIN_HAND);
                    if(oldCanUse != ClientEngine.get().canUseModifiableMain) ClientEngine.get().updateToolItemRecipeSelection(this, true);
                }
            }
            else if(itemSlot == 0) {
                if(entityIn instanceof Player player && PlayerData.isPresent(player) && player.getOffhandItem() == stack) {
                    boolean oldCanUse = ClientEngine.get().canUseModifiableOff;
                    ClientEngine.get().canUseModifiableOff = canUseSelectedItem(ClientEngine.get().getModifiableIndexOff(), worldIn, player, InteractionHand.OFF_HAND);
                    if(oldCanUse != ClientEngine.get().canUseModifiableOff) ClientEngine.get().updateToolItemRecipeSelection(this, false);
                }
            }
        }
    }

    @Override
    public Optional<Screen> modifyStartClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand) {
        if(mc.screen == null) {
            //Pass common level and not client level since this function is known elsewhere by the server and will cause compilation errors
            List<ToolIngredientRecipe> recipes = getRecipes(player.level, player, player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
            if(recipes.isEmpty()) return Optional.empty();
            List<ItemStack> items = new ObjectArrayList<>(recipes.size());
            for(int i = 0; i < recipes.size(); i++) items.add(i, recipes.get(i).getResultItem());
            return Optional.of(new SimpleModifiableItemScreen(PlayerData.get(player).isMainhandActive(), this, items));
        }
        else return Optional.empty();
    }

    @Override
    public Optional<Screen> modifyContinueClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand, int heldTime) {
        return Optional.empty();
    }

    @Override
    public void modifyReleaseClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand, int heldTime) {
        if(mc.screen instanceof SimpleModifiableItemScreen) mc.screen.onClose();
    }

    @Override
    public int getBackgroundUOffset() {
        return ModifiableItemScreen.BUILDING_BACKGROUND;
    }

    @Override
    public void swapClient(Minecraft mc, ItemStack item, Player player, boolean mainHand) {
        ClientEngine.get().updateToolItemRecipeSelection(this, mainHand);
    }

    @Override
    public boolean hasAction(ResourceLocation id, Player player) {
        if(getRecipeAction() == null) return false;
        else return id.equals(getRecipeAction().getId());
    }
}
