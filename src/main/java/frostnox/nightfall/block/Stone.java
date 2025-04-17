package frostnox.nightfall.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MaterialColor;

import javax.annotation.Nullable;

public enum Stone implements IStone {
    SHALE(4.0F, 5.0F, MaterialColor.STONE, SoundType.TUFF, Soil.GRAVEL, StoneType.SEDIMENTARY),
    SLATE(4.75F, 5.0F, MaterialColor.TERRACOTTA_LIGHT_BLUE, SoundType.DEEPSLATE_TILES, Soil.BLUE_GRAVEL, StoneType.METAMORPHIC),
    DEEPSLATE(5.5F, 15.0F, MaterialColor.COLOR_GRAY, SoundType.DEEPSLATE, Soil.GRAVEL, StoneType.IGNEOUS),
    SANDSTONE(4.0F, 5.0F, MaterialColor.SAND, SoundType.STONE, Soil.SAND, StoneType.SEDIMENTARY),
    SUNSCHIST(5.0F, 5.0F, MaterialColor.COLOR_ORANGE, SoundType.STONE, Soil.RED_SAND, StoneType.METAMORPHIC),
    GRANITE(6.0F, 15.0F, MaterialColor.DIRT, SoundType.BASALT, Soil.RED_SAND, StoneType.IGNEOUS),
    LIMESTONE(4.0F, 5.0F, MaterialColor.TERRACOTTA_WHITE, SoundType.DRIPSTONE_BLOCK, Soil.SAND, StoneType.SEDIMENTARY),
    MARBLE(4.5F, 5.0F, MaterialColor.SNOW, SoundType.STONE, Soil.WHITE_SAND, StoneType.METAMORPHIC),
    BASALT(5.0F, 15.0F, MaterialColor.COLOR_BLACK, SoundType.BASALT, Soil.BLACK_GRAVEL, StoneType.IGNEOUS),
    NIXWACKE(4.5F, 5.0F, MaterialColor.COLOR_CYAN, SoundType.CALCITE, Soil.WHITE_SAND, StoneType.SEDIMENTARY),
    AURGROT(5.5F, 5.0F, MaterialColor.TERRACOTTA_LIGHT_BLUE, SoundType.DEEPSLATE_TILES, Soil.BLUE_GRAVEL, StoneType.METAMORPHIC),
    STYGFEL(6.0F, 15.0F, MaterialColor.TERRACOTTA_BLUE, SoundType.DEEPSLATE, Soil.BLACK_GRAVEL, StoneType.IGNEOUS),
    PUMICE(2.5F, 4.0F, MaterialColor.TERRACOTTA_LIGHT_GRAY, SoundType.TUFF, Soil.ASH, StoneType.IGNEOUS),
    MOONSTONE(10F, 15F, MaterialColor.WARPED_STEM, SoundType.ANCIENT_DEBRIS, null, StoneType.METAMORPHIC);

    private final float strength, explosionResistance;
    private final MaterialColor color;
    private final SoundType sound;
    private final ISoil soil;
    private final StoneType stoneType;

    Stone(float strength, float explosionResistance, MaterialColor color, SoundType sound, ISoil soil, StoneType stoneType) {
        this.strength = strength;
        this.explosionResistance = explosionResistance;
        this.color = color;
        this.sound = sound;
        this.soil = soil;
        this.stoneType = stoneType;
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
    public @Nullable ISoil getSoil() {
        return soil;
    }

    @Override
    public StoneType getType() {
        return stoneType;
    }
}
