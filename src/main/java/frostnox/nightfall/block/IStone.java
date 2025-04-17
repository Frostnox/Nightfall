package frostnox.nightfall.block;

import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Make sure to register a wrapped entry during the associated registry event for full compatibility
 */
public interface IStone extends IBlock {
    @Nullable ISoil getSoil();

    StoneType getType();

    default String getName() {
        return toString().toLowerCase(Locale.ROOT);
    }

    class Entry extends ForgeRegistryEntry<Entry> {
        public final IStone value;

        public Entry(IStone value) {
            this.value = value;
        }
    }
}
