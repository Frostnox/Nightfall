package frostnox.nightfall.world.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.*;
import net.minecraftforge.registries.RegistryObject;

public class ContinentalBiomeSource extends BiomeSource {
    public static final Codec<ContinentalBiomeSource> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(Biome.LIST_CODEC.fieldOf("biomes").forGetter((source) -> source.biomes)).apply(instance, ContinentalBiomeSource::new));
    public ContinentalChunkGenerator generator;
    private final HolderSet<Biome> biomes;

    public ContinentalBiomeSource(HolderSet<Biome> biomes) {
        super(biomes.stream());
        this.biomes = biomes;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long seed) {
        return this;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        if(generator != null) {
            RegistryObject<Biome> biome = generator.calculateBiome(QuartPos.toBlock(x), QuartPos.toBlock(y), QuartPos.toBlock(z));
            for(int i = 0; i < biomes.size(); i++) {
                if(biomes.get(i).is(biome.getKey())) return biomes.get(i);
            }
        }
        return biomes.get(0);
    }
}
