package frostnox.nightfall.block;

import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.data.TagsNF;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Locale;

public enum TieredHeat {
    NONE(0F, null, null, Color.WHITE),
    RED(100F, ParticleTypesNF.FLAME_RED, ParticleTypesNF.SPARK_RED, new Color(1F, 0.378F, 0.312F)),
    ORANGE(1000F, ParticleTypesNF.FLAME_ORANGE, ParticleTypesNF.SPARK_ORANGE, new Color(1F, 0.595F, 0.359F)),
    YELLOW(1200F, ParticleTypesNF.FLAME_YELLOW, ParticleTypesNF.SPARK_YELLOW, new Color(1F, 0.792F, 0.480F)),
    WHITE(1400F, ParticleTypesNF.FLAME_WHITE, ParticleTypesNF.SPARK_WHITE, new Color(1F, 1F, 0.869F)),
    BLUE(1600F, ParticleTypesNF.FLAME_BLUE, ParticleTypesNF.SPARK_BLUE, new Color(0.788F, 0.983F, 1F));

    private final float temperature;
    private final @Nullable RegistryObject<SimpleParticleType> flameParticle, sparkParticle;
    public final Color color;

    TieredHeat(float temperature, @Nullable RegistryObject<SimpleParticleType> flameParticle, @Nullable RegistryObject<SimpleParticleType> sparkParticle, Color color) {
        this.temperature = temperature;
        this.flameParticle = flameParticle;
        this.sparkParticle = sparkParticle;
        this.color = color;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ROOT);
    }

    public int getTier() {
        return this.ordinal();
    }

    public float getBaseTemp() {
        return temperature;
    }

    public @Nullable RegistryObject<SimpleParticleType> getFlameParticle() {
        return flameParticle;
    }

    public @Nullable RegistryObject<SimpleParticleType> getSparkParticle() {
        return sparkParticle;
    }

    public static TieredHeat getMinimumHeat(ItemStack item) {
        if(item.is(TagsNF.SMELT_TIER_CUSTOM)) {
            if(item.is(TagsNF.SMELT_TIER_0)) return NONE;
            if(item.is(TagsNF.SMELT_TIER_2)) return ORANGE;
            if(item.is(TagsNF.SMELT_TIER_3)) return YELLOW;
            if(item.is(TagsNF.SMELT_TIER_4)) return WHITE;
            if(item.is(TagsNF.SMELT_TIER_5)) return BLUE;
        }
        return RED;
    }

    public static float getMinimumTemp(ItemStack item) {
        return getMinimumHeat(item).getBaseTemp();
    }

    public static TieredHeat fromTemp(float temperature) {
        if(temperature < 100F) return NONE;
        if(temperature < 1000F) return RED;
        if(temperature < 1200F) return ORANGE;
        if(temperature < 1400F) return YELLOW;
        if(temperature < 1600F) return WHITE;
        return BLUE;
    }

    public static TieredHeat fromTier(int tier) {
        if(tier < 0) return NONE;
        if(tier > 5) return BLUE;
        return values()[tier];
    }
}
