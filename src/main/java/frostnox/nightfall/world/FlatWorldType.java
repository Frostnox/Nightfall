package frostnox.nightfall.world;

import com.mojang.serialization.Lifecycle;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.registry.forge.BiomesNF;
import frostnox.nightfall.world.generation.FlatChunkGenerator;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.common.world.ForgeWorldPreset;

public class FlatWorldType extends ForgeWorldPreset {
    public FlatWorldType() {
        super(null);
        setRegistryName(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "flat"));
    }

    @Override
    public ChunkGenerator createChunkGenerator(RegistryAccess registryAccess, long seed, String generatorSettings) {
        return new FlatChunkGenerator(registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY),
                new FixedBiomeSource(registryAccess.registryOrThrow(Registry.BIOME_REGISTRY).getHolderOrThrow(BiomesNF.GRASSLANDS.getKey())));
    }

    @Override
    public WorldGenSettings createSettings(RegistryAccess registryAccess, long seed, boolean generateStructures, boolean generateLoot, String generatorSettings) {
        WritableRegistry<LevelStem> registry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), null);
        registry.register(LevelStem.OVERWORLD, new LevelStem(ContinentalWorldType.HOLDER, createChunkGenerator(registryAccess, seed, generatorSettings)), Lifecycle.stable());
        return new WorldGenSettings(seed, generateStructures, false, registry);
    }
}
