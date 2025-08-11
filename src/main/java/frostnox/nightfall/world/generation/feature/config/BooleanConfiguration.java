package frostnox.nightfall.world.generation.feature.config;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record BooleanConfiguration(boolean val) implements FeatureConfiguration {
    public static final Codec<BooleanConfiguration> CODEC = Codec.BOOL.fieldOf("val").xmap(BooleanConfiguration::new, (config) -> config.val).codec();
}
