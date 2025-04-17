package frostnox.nightfall.item;

import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Locale;

/**
 * Make sure to register a wrapped entry during the associated registry event for full compatibility
 */
public interface IDye {
    int getColor();

    default String getName() {
        return toString().toLowerCase(Locale.ROOT);
    }

    class Entry extends ForgeRegistryEntry<IDye.Entry> {
        public final IDye value;

        public Entry(IDye value) {
            this.value = value;
        }
    }
}
