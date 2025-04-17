package frostnox.nightfall.item;

import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.player.PlayerActionSet;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.List;
import java.util.Locale;

/**
 * Make sure to register a wrapped entry during the associated registry event for full compatibility
 */
public interface IArmament {
    PlayerActionSet getActionSet();

    HurtSphere getHurtSpheres();

    DamageType getDefaultDamageType();

    ImpactSoundType getImpactSoundType();

    List<ToolAction> getToolActions();

    boolean canDig();

    default String getName() {
        return toString().toLowerCase(Locale.ROOT);
    }

    class Entry extends ForgeRegistryEntry<IArmament.Entry> {
        public final IArmament value;

        public Entry(IArmament value) {
            this.value = value;
        }
    }
}
