package frostnox.nightfall.item.item;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.item.IActionableItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.ActionToServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;

public class ActionableItem extends ItemNF implements IActionableItem {
    public final RegistryObject<? extends Action> useAction;

    public ActionableItem(RegistryObject<? extends Action> useAction, Properties properties) {
        super(properties);
        this.useAction = useAction;
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
        if(useAction.getId().equals(id)) return true;
        for(ResourceLocation linkedId : useAction.get().linkedActions) {
            if(linkedId.equals(id)) return true;
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if(useAction.get().canStart(player)) {
            if(level.isClientSide()) {
                IPlayerData capP = PlayerData.get(player);
                if(capP.getActiveHand() == hand) {
                    ActionTracker.get(player).startAction(useAction.getId());
                    NetworkHandler.toServer(new ActionToServer(capP.isMainhandActive(), useAction.getId()));
                }
            }
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }
}
