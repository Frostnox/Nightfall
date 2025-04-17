package frostnox.nightfall.block;

import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public enum Soil implements ISoil {
    SILT(0.85F, MaterialColor.DIRT, SoundType.GRAVEL, SoundsNF.DIRT_FALL, Fertility.POOR),
    DIRT(0.9F, MaterialColor.PODZOL, SoundType.GRAVEL, SoundsNF.DIRT_FALL, Fertility.FAIR),
    LOAM(0.95F, MaterialColor.TERRACOTTA_BROWN, SoundType.GRAVEL, SoundsNF.DIRT_FALL, Fertility.RICH),
    ASH(0.6F, MaterialColor.COLOR_LIGHT_GRAY, SoundType.SAND, () -> null, Fertility.NONE),
    GRAVEL(1F, MaterialColor.STONE, SoundType.GRAVEL, SoundsNF.GRAVEL_FALL, Fertility.GRAVEL),
    BLUE_GRAVEL(1F, MaterialColor.TERRACOTTA_LIGHT_BLUE, SoundType.GRAVEL, SoundsNF.GRAVEL_FALL, Fertility.GRAVEL),
    BLACK_GRAVEL(1F, MaterialColor.COLOR_BLACK, SoundType.GRAVEL, SoundsNF.GRAVEL_FALL, Fertility.GRAVEL),
    SAND(0.8F, MaterialColor.SAND, SoundType.SAND, SoundsNF.SAND_FALL, Fertility.SAND),
    RED_SAND(0.8F, MaterialColor.COLOR_ORANGE, SoundType.SAND, SoundsNF.SAND_FALL, Fertility.SAND),
    WHITE_SAND(0.8F, MaterialColor.SNOW, SoundType.SAND, SoundsNF.SAND_FALL, Fertility.SAND);

    private final float strength, explosionResistance;
    private final MaterialColor color;
    private final SoundType sound;
    private final Supplier<SoundEvent> slideSound;
    private final Fertility fertility;

    Soil(float strength, MaterialColor color, SoundType sound, Supplier<SoundEvent> slideSound, Fertility fertility) {
        this(strength, 0F, color, sound, slideSound, fertility);
    }

    Soil(float strength, float explosionResistance, MaterialColor color, SoundType sound, Supplier<SoundEvent> slideSound, Fertility fertility) {
        this.strength = strength;
        this.explosionResistance = explosionResistance;
        this.color = color;
        this.sound = sound;
        this.slideSound = slideSound;
        this.fertility = fertility;
    }

    @Override
    public MaterialColor getBaseColor() {
        return color;
    }

    @Override
    public SoundType getSound() {
        return sound;
    }

    @Override
    public float getStrength() {
        return strength;
    }

    @Override
    public float getExplosionResistance() {
        return explosionResistance;
    }

    @Override
    public Supplier<SoundEvent> getSlideSound() {
        return slideSound;
    }

    @Override
    public Fertility getFertility() {
        return fertility;
    }

    public static RegistryObject<? extends Block> getBlock(Soil type) {
        return switch(type) {
            case SILT -> BlocksNF.SILT;
            case DIRT -> BlocksNF.DIRT;
            case LOAM -> BlocksNF.LOAM;
            case ASH -> BlocksNF.ASH;
            case GRAVEL -> BlocksNF.GRAVEL;
            case BLUE_GRAVEL -> BlocksNF.BLUE_GRAVEL;
            case BLACK_GRAVEL -> BlocksNF.BLACK_GRAVEL;
            case SAND -> BlocksNF.SAND;
            case RED_SAND -> BlocksNF.RED_SAND;
            case WHITE_SAND -> BlocksNF.WHITE_SAND;
        };
    }
}