package frostnox.nightfall.action.player.action.guard;

import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.item.item.MeleeWeaponItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.ActionToServer;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.CombatUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public abstract class CounterGuardAction extends GuardAction {
    public CounterGuardAction(int... duration) {
        super(duration);
    }

    public CounterGuardAction(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public void onAttackInput(Player player) {
        IActionTracker capA = ActionTracker.get(player);
        if((capA.getState() == 0 && !ActionsNF.isEmpty(getChain(player).getId()) && MeleeWeaponItem.canExecuteAttack(player, false) && player.tickCount - PlayerData.get(player).getLastBlockTick() <= 8)) {
            CombatUtil.removeTransientMultiplier(player, player.getAttribute(Attributes.MOVEMENT_SPEED), CombatUtil.BLOCK_SLOW_ID);
            capA.startAction(getChain(player).getId());
            NetworkHandler.toServer(new ActionToServer(PlayerData.get(player).getActiveHand() == InteractionHand.MAIN_HAND, capA.getActionID()));
        }
    }

    @Override
    public List<Component> getTooltips(ItemStack stack, @Nullable Level level, TooltipFlag isAdvanced) {
        List<Component> tooltips = super.getTooltips(stack, level, isAdvanced);
        if(ClientEngine.get().isShiftHeld()) {
            tooltips.add(new TextComponent(" ").append(new TranslatableComponent("action.guard.riposte_1")));
            tooltips.add(new TextComponent(" ").append(new TranslatableComponent("action.guard.riposte_2")));
            List<Component> riposteTips = chainsTo().get().getTooltips(stack, level, isAdvanced);
            for(int i = 0; i < riposteTips.size(); i++) {
                if(i == 0) tooltips.add(new TextComponent(" ").append(new TranslatableComponent("action.riposte")).append(": ").append(riposteTips.get(i)));
                else tooltips.add(new TextComponent(" ").append(riposteTips.get(i)));
            }
        }
        return tooltips;
    }
}
