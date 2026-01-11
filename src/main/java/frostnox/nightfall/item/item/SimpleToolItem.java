package frostnox.nightfall.item.item;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.data.recipe.ToolIngredientRecipe;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.ActionToServer;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleToolItem extends ToolItem {
    public final @Nullable RegistryObject<? extends Action> recipeAction;

    public SimpleToolItem(@Nullable RegistryObject<? extends Action> recipeAction, Properties properties) {
        super(properties);
        this.recipeAction = recipeAction;
    }

    @Override
    public @Nullable RegistryObject<? extends Action> getRecipeAction() {
        return recipeAction;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if(getRecipeAction() == null || !getRecipeAction().get().canStart(player) || !MeleeWeaponItem.canExecuteAttack(player, false)) return InteractionResultHolder.pass(player.getItemInHand(hand));
        ItemStack item = player.getItemInHand(hand);
        List<ToolIngredientRecipe> recipes = getRecipes(level, player, player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
        int index = LevelUtil.getModifiableItemIndex(level, player, hand);
        if(index < 0 || index >= recipes.size()) return InteractionResultHolder.pass(item);
        if(level.isClientSide()) {
            IPlayerData capP = PlayerData.get(player);
            if(capP.getActiveHand() == hand) {
                ActionTracker.get(player).startAction(getRecipeAction().getId());
                NetworkHandler.toServer(new ActionToServer(capP.isMainhandActive(), getRecipeAction().getId()));
            }
        }
        return InteractionResultHolder.fail(item);
    }
}
