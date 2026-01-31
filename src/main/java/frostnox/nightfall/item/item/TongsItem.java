package frostnox.nightfall.item.item;

import frostnox.nightfall.item.ITieredItemMaterial;

public class TongsItem extends ItemNF {
    public final ITieredItemMaterial material;

    public TongsItem(ITieredItemMaterial material, Properties properties) {
        super(properties);
        this.material = material;
    }
}
