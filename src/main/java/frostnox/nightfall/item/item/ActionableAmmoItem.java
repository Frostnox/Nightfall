package frostnox.nightfall.item.item;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.item.IActionableItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.ActionToServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;

public class ActionableAmmoItem extends ItemNF implements IActionableItem {
    public final RegistryObject<? extends Action> useAction, reloadAction;
    public final TagKey<Item> ammo;
    public final int maxAmmo;

    public ActionableAmmoItem(RegistryObject<? extends Action> useAction, RegistryObject<? extends Action> reloadAction, TagKey<Item> ammo, int maxAmmo, Properties properties) {
        super(properties);
        this.useAction = useAction;
        this.reloadAction = reloadAction;
        this.ammo = ammo;
        this.maxAmmo = maxAmmo;
    }

    public int getAmmo(ItemStack item) {
        return item.getOrCreateTag().contains("ammo") ? item.getTag().getInt("ammo") : 0;
    }

    public boolean shouldReload(ItemStack item, ItemStack otherItem) {
        return otherItem.is(ammo) && getAmmo(item) < maxAmmo;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        List<Component> actionComponents = useAction.get().getTooltips(pStack, pLevel, pIsAdvanced);
        for(int i = 0; i < actionComponents.size(); i++) {
            if(i == 0) pTooltipComponents.add(new TranslatableComponent("item.on_use").append(actionComponents.get(i)));
            else pTooltipComponents.add(actionComponents.get(i));
        }
    }

    @Override
    public boolean hasAction(ResourceLocation id, Player player) {
        if(useAction.getId().equals(id) || reloadAction.getId().equals(id)) return true;
        for(ResourceLocation linkedId : useAction.get().linkedActions) {
            if(linkedId.equals(id)) return true;
        }
        for(ResourceLocation linkedId : reloadAction.get().linkedActions) {
            if(linkedId.equals(id)) return true;
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player pPlayer, InteractionHand pHand) {
        var action = shouldReload(pPlayer.getItemInHand(pHand), pPlayer.getItemInHand(pHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND)) ? reloadAction : useAction;
        if(action.get().canStart(pPlayer)) {
            if(level.isClientSide()) {
                IPlayerData capP = PlayerData.get(pPlayer);
                if(capP.getActiveHand() == pHand) {
                    ActionTracker.get(pPlayer).startAction(action.getId());
                    NetworkHandler.toServer(new ActionToServer(capP.isMainhandActive(), action.getId()));
                }
            }
            return InteractionResultHolder.fail(pPlayer.getItemInHand(pHand));
        }
        return super.use(level, pPlayer, pHand);
    }
}
