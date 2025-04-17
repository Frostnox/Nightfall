package frostnox.nightfall.item.item;

import frostnox.nightfall.block.TieredHeat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class FuelItem extends BuildingMaterialItem {
    public final int burnTicks;
    public final float burnTemp;

    public FuelItem(int burnTicks, float burnTemp, Properties pProperties) {
        super(pProperties);
        this.burnTicks = burnTicks;
        this.burnTemp = burnTemp;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        TieredHeat heat = TieredHeat.fromTemp(burnTemp);
        pTooltipComponents.add(new TranslatableComponent("heat.tier." + heat.getTier()).withStyle(Style.EMPTY.withColor(heat.color.getRGB())));
        //TODO: List burn time in seconds once applicable?
    }
}
