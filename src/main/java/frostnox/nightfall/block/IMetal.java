package frostnox.nightfall.block;

import frostnox.nightfall.block.fluid.MetalFluid;
import frostnox.nightfall.util.data.Vec2f;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Make sure to register a wrapped entry during the associated registry event for full compatibility
 */
public interface IMetal extends IBlock {
    enum Category {
        HARD, MYSTIC, NOBLE
    }

    int getTier();

    int getWorkTier();

    Color getColor();

    Category getCategory();

    TagKey<Item> getTag();

    /**
     * @return map of metals with percentage ranges (0 to 1) necessary to create this metal
     */
    Map<IMetal, Vec2f> getBaseMetals();

    List<Float> getBaseDefenses();

    default String getName() {
        return toString().toLowerCase(Locale.ROOT);
    }

    default float getMeltTemp() {
        return TieredHeat.fromTier(getWorkTier() + 1).getBaseTemp();
    }

    default Item getMatchingItem(TagKey<Item> tag) {
        for(Item item : ForgeRegistries.ITEMS) {
            if(item.builtInRegistryHolder().is(tag) && item.builtInRegistryHolder().is(getTag())) return item;
        }
        return Items.AIR;
    }

    default boolean canCreateFromFluids(Collection<FluidStack> fluids, float temperature) {
        if(getBaseMetals().isEmpty() || fluids.size() < getBaseMetals().size()) return false;
        Object2IntMap<IMetal> units = new Object2IntArrayMap<>(fluids.size());
        int total = 0;
        for(FluidStack fluid : fluids) {
            if(fluid.getFluid() instanceof MetalFluid metalFluid) {
                if(metalFluid.metal == this) continue;
                if(temperature < metalFluid.metal.getMeltTemp() || !getBaseMetals().containsKey(metalFluid.metal)) return false;
                units.put(metalFluid.metal, fluid.getAmount());
                total += fluid.getAmount();
            }
            else return false;
        }
        if(units.size() != getBaseMetals().size()) return false;
        for(FluidStack fluid : fluids) {
            MetalFluid metalFluid = (MetalFluid) fluid.getFluid();
            if(metalFluid.metal == this) continue;
            float part = ((float) units.getInt(metalFluid.metal)) / total;
            Vec2f range = getBaseMetals().get(metalFluid.metal);
            if(part < range.x() || part > range.y()) return false;
        }
        return true;
    }

    class Entry extends ForgeRegistryEntry<IMetal.Entry> {
        public final IMetal value;

        public Entry(IMetal value) {
            this.value = value;
        }
    }
}
