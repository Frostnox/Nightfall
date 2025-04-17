package frostnox.nightfall.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Make sure to register a wrapped entry during the associated registry event for full compatibility
 */
public interface ISoil extends IBlock {
    Supplier<SoundEvent> getSlideSound();

    Fertility getFertility();

    default String getName() {
        return toString().toLowerCase(Locale.ROOT);
    }

    class Entry extends ForgeRegistryEntry<ISoil.Entry> {
        public final ISoil value;

        public Entry(ISoil value) {
            this.value = value;
        }
    }
}
