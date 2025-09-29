package frostnox.nightfall.item;

import frostnox.nightfall.block.IMetal;
import net.minecraft.world.item.Tier;

import javax.annotation.Nullable;
import java.util.Locale;

public interface ITieredItemMaterial extends Tier {
    default String getName() {
        return toString().toLowerCase(Locale.ROOT);
    }

    int getTier();

    int getDurability();

    float getDamageMultiplier();

    Weight getWeight();

    @Nullable IMetal getMetal();
}
