package frostnox.nightfall.block;

import frostnox.nightfall.registry.forge.BiomesNF;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MaterialColor;

import javax.annotation.Nullable;

public enum SoilCover {
    GRASS("grassy", MaterialColor.GRASS, SoundType.GRASS) {
        @Override
        public boolean canGrow(int aboveLight) {
            return aboveLight > 5;
        }
    },
    MOSS("mossy", MaterialColor.COLOR_GREEN, SoundType.GRASS) {
        @Override
        public boolean canGrow(int aboveLight) {
            return aboveLight > 0;
        }
    },
    FOREST("forested", MaterialColor.PODZOL, SoundType.ROOTED_DIRT) {
        @Override
        public boolean canGrow(int aboveLight) {
            return aboveLight > 0;
        }
    },
    LICHEN("lichenous", MaterialColor.TERRACOTTA_RED, SoundType.GRAVEL) {
        @Override
        public boolean canGrow(int aboveLight) {
            return aboveLight > 0;
        }
    };

    public final String prefix;
    public final MaterialColor color;
    public final SoundType sound;

    SoilCover(String prefix, MaterialColor color, SoundType sound) {
        this.prefix = prefix;
        this.color = color;
        this.sound = sound;
    }

    public abstract boolean canGrow(int aboveLight);

    public static @Nullable SoilCover getForBiome(Holder<Biome> biome) {
        if(biome.is(BiomesNF.TUNDRA.getKey())) return LICHEN;
        else if(biome.is(BiomesNF.FOREST.getKey())) return FOREST;
        else if(biome.is(BiomesNF.OLDWOODS.getKey()) || biome.is(BiomesNF.JUNGLE.getKey())) return MOSS;
        else if(biome.is(BiomesNF.DESERT.getKey()) || biome.is(BiomesNF.BADLANDS.getKey())) return null;
        else return GRASS; //Grasslands, Taiga, Swamp, Island
    }
}
