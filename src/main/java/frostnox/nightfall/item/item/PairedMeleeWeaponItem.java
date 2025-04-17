package frostnox.nightfall.item.item;

import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.player.PlayerActionSet;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.item.ITieredItemMaterial;
import frostnox.nightfall.item.IWeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolAction;

import javax.annotation.Nullable;
import java.util.List;

public class PairedMeleeWeaponItem extends MeleeWeaponItem {
    public final PlayerActionSet matchingSet, pairedSet;

    public PairedMeleeWeaponItem(ITieredItemMaterial material, PlayerActionSet actionSet, PlayerActionSet matchingSet, PlayerActionSet pairedSet, HurtSphere spheres, HurtSphere npcSpheres, boolean canDig, float durabilityMultiplier, Properties builder, List<ToolAction> toolActions, DamageType... defaultType) {
        super(material, actionSet, spheres, npcSpheres, canDig, durabilityMultiplier, builder, toolActions, defaultType);
        this.matchingSet = matchingSet;
        this.pairedSet = pairedSet;
    }

    public PairedMeleeWeaponItem(ITieredItemMaterial material, PlayerActionSet actionSet, PlayerActionSet matchingSet, PlayerActionSet pairedSet, HurtSphere spheres, HurtSphere npcSpheres, boolean canDig, Properties builder, List<ToolAction> toolActions, DamageType... defaultType) {
        this(material, actionSet, matchingSet, pairedSet, spheres, npcSpheres, canDig, 1, builder, toolActions, defaultType);
    }

    @Override
    public PlayerActionSet getActionSet(Player player) {
        InteractionHand hand = PlayerData.get(player).getOppositeActiveHand();
        if(player.getItemInHand(hand).getItem() instanceof IWeaponItem weapon && weapon.getBaseActionSet().equals(matchingSet)) {
            return pairedSet;
        }
        else return getBaseActionSet();
    }

    @Override
    protected void appendExtraHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        tooltips.add(new TranslatableComponent("item.pair").append(new TranslatableComponent("action_set." + matchingSet.toString()).withStyle(ChatFormatting.BLUE)));
        List<Component> basic = pairedSet.basic.get().getTooltips(pStack, pLevel, pIsAdvanced);
        for(int i = 0; i < basic.size(); i++) {
            MutableComponent text = new TranslatableComponent("action.basic_paired");
            if(ClientEngine.get().isShiftHeld()) text.append(new TranslatableComponent("action.control", ClientEngine.get().getAttackKeyName()).withStyle(ChatFormatting.AQUA));
            text.append(": ");
            if(i == 0) tooltips.add(text.append(basic.get(i)));
            else tooltips.add(basic.get(i));
        }
        List<Component> alternate = pairedSet.alternate.get().getTooltips(pStack, pLevel, pIsAdvanced);
        for(int i = 0; i < alternate.size(); i++) {
            MutableComponent text = new TranslatableComponent("action.alternate_paired");
            if(ClientEngine.get().isShiftHeld()) text.append(new TranslatableComponent("action.control_held", ClientEngine.get().getAttackKeyName()).withStyle(ChatFormatting.AQUA));
            text.append(": ");
            if(i == 0) tooltips.add(text.append(alternate.get(i)));
            else tooltips.add(alternate.get(i));
        }
    }
}
