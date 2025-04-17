package frostnox.nightfall.item.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class ChangeOnUseFinishItem extends Item {
    public final Supplier<? extends Item> changeItem;

    public ChangeOnUseFinishItem(Supplier<? extends Item> changeItem, Properties pProperties) {
        super(pProperties);
        this.changeItem = changeItem;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level level, LivingEntity pEntityLiving) {
        ItemStack item = super.finishUsingItem(pStack, level, pEntityLiving);
        return pEntityLiving instanceof Player && ((Player)pEntityLiving).getAbilities().instabuild ? item : new ItemStack(changeItem.get());
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        return new ItemStack(changeItem.get());
    }
}
