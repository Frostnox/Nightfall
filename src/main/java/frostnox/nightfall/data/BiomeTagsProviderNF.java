package frostnox.nightfall.data;

import frostnox.nightfall.registry.forge.BiomesNF;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class BiomeTagsProviderNF extends BiomeTagsProvider {
    public BiomeTagsProviderNF(DataGenerator generator, String id, @Nullable ExistingFileHelper helper) {
        super(generator, id, helper);
    }

    @Override
    protected void addTags() {
        tag(TagsNF.LAND).add(BiomesNF.TUNDRA.get(), BiomesNF.TAIGA.get(), BiomesNF.OLDWOODS.get(), BiomesNF.GRASSLANDS.get(), BiomesNF.FOREST.get(),
                BiomesNF.JUNGLE.get(), BiomesNF.DESERT.get(), BiomesNF.BADLANDS.get(), BiomesNF.SWAMP.get(), BiomesNF.ISLAND.get());
        tag(TagsNF.CAVE).add(BiomesNF.TUNNELS.get(), BiomesNF.CAVERNS.get(), BiomesNF.DEPTHS.get());
        tag(TagsNF.GEN_RUINS_SURFACE).addTag(TagsNF.LAND);
        tag(TagsNF.GEN_COTTAGE_RUINS).add(BiomesNF.TAIGA.get(), BiomesNF.OLDWOODS.get(), BiomesNF.GRASSLANDS.get(), BiomesNF.FOREST.get(),
                BiomesNF.JUNGLE.get(), BiomesNF.SWAMP.get());
        tag(TagsNF.GEN_SLAYER_RUINS).add(BiomesNF.TUNDRA.get(), BiomesNF.TAIGA.get(), BiomesNF.OLDWOODS.get(), BiomesNF.GRASSLANDS.get(), BiomesNF.FOREST.get(),
                BiomesNF.JUNGLE.get(), BiomesNF.DESERT.get(), BiomesNF.BADLANDS.get(), BiomesNF.SWAMP.get());
        tag(TagsNF.GEN_EXPLORER_RUINS).add(BiomesNF.TUNDRA.get(), BiomesNF.TAIGA.get(), BiomesNF.OLDWOODS.get(), BiomesNF.GRASSLANDS.get(), BiomesNF.FOREST.get(),
                BiomesNF.JUNGLE.get(), BiomesNF.DESERT.get(), BiomesNF.BADLANDS.get(), BiomesNF.SWAMP.get());
        tag(TagsNF.GEN_DESERTED_CAMP).add(BiomesNF.TAIGA.get(), BiomesNF.OLDWOODS.get(), BiomesNF.GRASSLANDS.get(), BiomesNF.FOREST.get(),
                BiomesNF.JUNGLE.get(), BiomesNF.SWAMP.get(), BiomesNF.ISLAND.get());
    }
}
