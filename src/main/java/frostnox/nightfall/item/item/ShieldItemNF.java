package frostnox.nightfall.item.item;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.item.IGuardingItem;
import it.unimi.dsi.fastutil.floats.FloatImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;

public class ShieldItemNF extends ActionableItem implements IGuardingItem {
    public final List<Float> defense;

    public ShieldItemNF(float[] defense, RegistryObject<? extends Action> useAction, Properties properties) {
        super(useAction, properties);
        this.defense = FloatImmutableList.of(defense);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !newStack.is(oldStack.getItem());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, tooltips, pIsAdvanced);
        if(!ClientEngine.get().isShiftHeld()) {
            tooltips.add(new TranslatableComponent("tooltip.expand_prompt").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true)));
        }
    }

    @Override
    public float getDefense(DamageTypeSource source) {
        float total = 0;
        for(DamageType type : source.types) {
            if(type.isDefensible()) total += defense.get(type.ordinal());
        }
        return total / source.types.length;
    }
}
