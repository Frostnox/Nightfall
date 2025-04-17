package frostnox.nightfall.item;

import frostnox.nightfall.world.ILightSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;

public interface IItemLightSource extends ILightSource {
    /**
     * @return item to replace with when in water
     */
    Item getExtinguishedItem();

    double getEquippedHeight(Pose pose);
}
