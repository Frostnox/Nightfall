package frostnox.nightfall.block;

import frostnox.nightfall.world.generation.tree.TreeGenerator;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Make sure to register a wrapped entry during the associated registry event for full compatibility
 */
public interface ITree extends IBlock {
    float BASE_STRENGTH = 3F;

    MaterialColor getBarkColor();

    TreeGenerator getGenerator();

    int getGrowthIntervalTicks();

    boolean isDeciduous();

    /**
     * @return decorative style, should match a base texture
     */
    String getStyle();

    float getHardness();

    int getLifespan();

    TagKey<Block> getTag();

    @Nullable RegistryObject<? extends ParticleType<BlockParticleOption>> getParticle();

    /**
     * @param temperature height adjusted (not season adjusted) noise value, 0 to 1
     * @param humidity noise value, 0 to 1
     */
    boolean canSurvive(float temperature, float humidity);

    default String getName() {
        return toString().toLowerCase(Locale.ROOT);
    }

    class Entry extends ForgeRegistryEntry<ITree.Entry> {
        public final ITree value;

        public Entry(ITree value) {
            this.value = value;
        }
    }
}
