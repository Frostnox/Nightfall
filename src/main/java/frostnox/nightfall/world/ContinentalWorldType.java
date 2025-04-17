package frostnox.nightfall.world;

import com.mojang.serialization.Lifecycle;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.registry.forge.BiomesNF;
import frostnox.nightfall.world.biome.ContinentalBiomeSource;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.common.world.ForgeWorldPreset;

import java.util.OptionalLong;

public class ContinentalWorldType extends ForgeWorldPreset {
    //public static final ResourceKey<LevelStem> CONTINENTAL = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "continental"));
    //public static final ResourceKey<DimensionType> CONTINENTAL_TYPE_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "continental"));
    public static final long DAY_LENGTH = 48000; //24000 ticks = 20 minutes
    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "continental");
    public static final DimensionType CONTINENTAL_TYPE = DimensionType.create(OptionalLong.empty(), true, false, false,
            true, 1.0D, false, false, true, false, false,
            0, 832, 832, BlockTags.INFINIBURN_OVERWORLD, LOCATION, 0.0F);
    public static final Holder<DimensionType> HOLDER = Holder.direct(CONTINENTAL_TYPE); //This should be retrieved from the registry if possible

    public ContinentalWorldType() {
        super(null);
        setRegistryName(LOCATION);
    }

    @Override
    public ChunkGenerator createChunkGenerator(RegistryAccess registryAccess, long seed, String generatorSettings) {
        return new ContinentalChunkGenerator(registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY),
                new ContinentalBiomeSource(BiomesNF.getDefaultBiomes(registryAccess)), seed);
    }

    @Override
    public WorldGenSettings createSettings(RegistryAccess registryAccess, long seed, boolean generateStructures, boolean generateLoot, String generatorSettings) {
        WritableRegistry<LevelStem> registry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), null);
        registry.register(LevelStem.OVERWORLD, new LevelStem(HOLDER, createChunkGenerator(registryAccess, seed, generatorSettings)), Lifecycle.stable());
        return new WorldGenSettings(seed, generateStructures, false, registry);
    }
}
