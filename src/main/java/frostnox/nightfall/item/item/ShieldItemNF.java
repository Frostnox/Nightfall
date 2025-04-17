package frostnox.nightfall.item.item;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.item.IGuardingItem;
import it.unimi.dsi.fastutil.floats.FloatImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ShieldItemNF extends ActionableItem implements IGuardingItem {
    public final List<Float> defense, absorption;

    public ShieldItemNF(float[] defense, float[] absorption, RegistryObject<? extends Action> useAction, Properties properties) {
        super(useAction, properties);
        this.defense = FloatImmutableList.of(defense);
        this.absorption = FloatImmutableList.of(absorption);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !newStack.is(oldStack.getItem());
    }

    @Override
    public float getDefense(DamageTypeSource source) {
        float total = 0;
        for(DamageType type : source.types) {
            if(type.isDefensible()) total += defense.get(type.ordinal());
        }
        return total / source.types.length;
    }

    @Override
    public float getAbsorption(DamageTypeSource source) {
        float total = 0;
        for(DamageType type : source.types) {
            if(type.isDefensible()) total += absorption.get(type.ordinal());
        }
        return total / source.types.length;
    }
}
