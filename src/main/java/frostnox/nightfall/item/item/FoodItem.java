package frostnox.nightfall.item.item;

import net.minecraft.world.item.ItemStack;

public class FoodItem extends ItemNF {
    public final int eatTicks;

    public FoodItem(int eatTicks, Properties properties) {
        super(properties);
        this.eatTicks = eatTicks;
    }

    @Override
    public int getUseDuration(ItemStack item) {
        if(item.getItem().isEdible()) return item.getFoodProperties(null).isFastFood() ? eatTicks / 2 : eatTicks;
        else return 0;
    }
}
