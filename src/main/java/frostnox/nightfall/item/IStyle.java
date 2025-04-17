package frostnox.nightfall.item;

import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.List;
import java.util.Locale;

/**
 * Make sure to register a wrapped entry during the associated registry event for full compatibility
 */
public interface IStyle {
    List<String> getMaterials();

    boolean hasMaterial(String name);

    default String getName() {
        return toString().toLowerCase(Locale.ROOT);
    }

    class Entry extends ForgeRegistryEntry<IStyle.Entry> {
        public final IStyle value;

        public Entry(IStyle value) {
            this.value = value;
        }
    }
}
