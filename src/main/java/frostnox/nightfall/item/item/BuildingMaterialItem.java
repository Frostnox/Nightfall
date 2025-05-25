package frostnox.nightfall.item.item;

import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.item.BuildingMaterialItemScreen;
import frostnox.nightfall.client.gui.screen.item.ModifiableItemScreen;
import frostnox.nightfall.data.recipe.BuildingRecipe;
import frostnox.nightfall.item.client.IModifiable;
import frostnox.nightfall.item.client.ISwapBehavior;
import frostnox.nightfall.util.LevelUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BuildingMaterialItem extends ScreenCacheItem implements IModifiable, ISwapBehavior {
    public BuildingMaterialItem(Properties pProperties) {
        super(pProperties);
    }

    public List<BuildingRecipe> getRecipes(Level level, @Nullable Player player) {
        return level.getRecipeManager().getAllRecipesFor(BuildingRecipe.TYPE).stream()
                .filter((buildingRecipe -> buildingRecipe.baseItem == this && buildingRecipe.isUnlocked(player)))
                .sorted((r1, r2) -> {
                    if(r1.menuOrder < 0 && r2.menuOrder >= 0) return 1;
                    else if(r2.menuOrder < 0 && r1.menuOrder >= 0) return -1;
                    else if(r1.menuOrder == r2.menuOrder || r1.menuOrder < 0) return -r1.output.getDescriptionId().compareTo(r2.output.getDescriptionId());
                    else return r1.menuOrder > r2.menuOrder ? 1 : -1;
                })
                .collect(Collectors.toList());
    }

    protected boolean canUseSelectedItem(int index, Level level, Player player, InteractionHand hand) {
        if(player.getAbilities().instabuild) return true;
        List<BuildingRecipe> recipes = getRecipes(level, player);
        if(index < 0 || index >= recipes.size()) return false;
        ItemStack item = player.getItemInHand(hand);
        BuildingRecipe recipe = recipes.get(index);
        ItemStack extraItem = getExtraItem(item, player, hand, recipe.extraIngredient, recipe.extraAmount);
        return item.getCount() >= recipe.baseAmount && extraItem.getCount() >= recipe.extraAmount;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if(worldIn.isClientSide) {
            if(isSelected) {
                if(entityIn instanceof Player player && PlayerData.isPresent(player)) {
                    ClientEngine.get().canUseModifiableMain =
                            canUseSelectedItem(ClientEngine.get().getModifiableIndexMain(), worldIn, player, InteractionHand.MAIN_HAND);
                }
            }
            else if(itemSlot == 0) {
                if(entityIn instanceof Player player && PlayerData.isPresent(player)) {
                    ClientEngine.get().canUseModifiableOff =
                            canUseSelectedItem(ClientEngine.get().getModifiableIndexOff(), worldIn, player, InteractionHand.OFF_HAND);
                }
            }
        }
    }

    @Override
    public Optional<Screen> modifyStartClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand) {
        if(mc.screen == null) {
            //Pass common level and not client level since this function is known elsewhere by the server and will cause compilation errors
            List<BuildingRecipe> recipes = getRecipes(player.level, player);
            if(recipes.isEmpty()) return Optional.empty();
            List<ItemStack> items = new ObjectArrayList<>(recipes.size());
            for(int i = 0; i < recipes.size(); i++) items.add(i, recipes.get(i).getResultItem());
            return Optional.of(new BuildingMaterialItemScreen(PlayerData.get(player).isMainhandActive(), this, recipes, items));
        }
        else return Optional.empty();
    }

    @Override
    public Optional<Screen> modifyContinueClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand, int heldTime) {
        return Optional.empty();
    }

    @Override
    public void modifyReleaseClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand, int heldTime) {
        if(mc.screen instanceof BuildingMaterialItemScreen) mc.screen.onClose();
    }

    @Override
    public int getBackgroundUOffset() {
        return ModifiableItemScreen.BUILDING_BACKGROUND;
    }

    @Override
    public void swapClient(Minecraft mc, ItemStack item, Player player, boolean mainHand) {
        BuildingMaterialItemScreen.initSelection(mc, this, mainHand);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        List<BuildingRecipe> recipes = getRecipes(level, player);
        int index = LevelUtil.getModifiableItemIndex(level, player, hand);
        if(index < 0 || index >= recipes.size()) return InteractionResultHolder.pass(item);
        BuildingRecipe recipe = recipes.get(index);
        ItemStack extraItem = getExtraItem(item, player, hand, recipe.extraIngredient, recipe.extraAmount);
        if(!player.getAbilities().instabuild && (item.getCount() < recipe.baseAmount || extraItem.getCount() < recipe.extraAmount)) {
            return InteractionResultHolder.pass(item);
        }
        ItemStack placeItem = recipe.getResultItem();
        InteractionResultHolder<ItemStack> result = placeItem.use(level, player, hand);
        if(!result.getResult().consumesAction() && isEdible()) {
            if(player.canEat(result.getObject().getFoodProperties(player).canAlwaysEat())) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(result.getObject());
            }
            else return InteractionResultHolder.fail(result.getObject());
        }
        else {
            if(result.getResult().consumesAction() && !player.getAbilities().instabuild) {
                item.shrink(recipe.baseAmount);
                extraItem.shrink(recipe.extraAmount);
            }
            return result;
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if(player == null || !player.isAlive()) return InteractionResult.FAIL;
        List<BuildingRecipe> recipes = getRecipes(context.getLevel(), player);
        int index = LevelUtil.getModifiableItemIndex(context.getLevel(), player, context.getHand());
        if(index < 0 || index >= recipes.size()) return InteractionResult.FAIL;
        BuildingRecipe recipe = recipes.get(index);
        ItemStack item = context.getItemInHand();
        ItemStack extraItem = getExtraItem(item, player, context.getHand(), recipe.extraIngredient, recipe.extraAmount);
        if((!player.getAbilities().instabuild) && (item.getCount() < recipe.baseAmount || extraItem.getCount() < recipe.extraAmount)) {
            return InteractionResult.FAIL;
        }
        ItemStack placeItem = recipe.getResultItem();
        InteractionResult result = placeItem.useOn(new UseOnContext(context.getLevel(), player, context.getHand(), placeItem, context.getHitResult()));

        if(!result.consumesAction() && this.isEdible()) {
            InteractionResult eatResult = this.use(context.getLevel(), player, context.getHand()).getResult();
            return eatResult == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : eatResult;
        }
        else {
            if(result.consumesAction() && !player.getAbilities().instabuild) {
                item.shrink(recipe.baseAmount);
                extraItem.shrink(recipe.extraAmount);
            }
            return result;
        }
    }

    private static ItemStack getExtraItem(ItemStack heldItem, Player player, InteractionHand hand, Ingredient target, int amount) {
        if(amount == 0) return ItemStack.EMPTY;
        if(hand == InteractionHand.MAIN_HAND) {
            ItemStack offItem = player.getOffhandItem();
            if(target.test(offItem) && offItem.getCount() >= amount) return offItem;
        }
        for(ItemStack item : player.getInventory().items) {
            if(item == heldItem) continue;
            if(target.test(item) && item.getCount() >= amount) return item;
        }
        return ItemStack.EMPTY;
    }
}
