package frostnox.nightfall.action.player.action;

import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

public abstract class KnapAction extends CarveAction {
    public KnapAction(int[] duration, Properties properties) {
        super(properties, duration);
    }

    public KnapAction(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    protected SoundEvent getCarveSound(ItemStack oppItem) {
        return oppItem.is(TagsNF.STONE) ? SoundsNF.CARVE_STONE.get() : SoundsNF.HIT_WOOD.get();
    }

    @Override
    protected boolean isSoundFrame(int frame) {
        return frame % 10 == 7;
    }
}
