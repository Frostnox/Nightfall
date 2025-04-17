package frostnox.nightfall.data;

import frostnox.nightfall.registry.forge.FluidsNF;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class FluidTagsProviderNF extends FluidTagsProvider {
    public FluidTagsProviderNF(DataGenerator generator, String id, @Nullable ExistingFileHelper helper) {
        super(generator, id, helper);
    }

    @Override
    protected void addTags() {
        tag(TagsNF.FRESHWATER).add(FluidsNF.WATER.get(), FluidsNF.WATER_FLOWING.get(), Fluids.WATER, Fluids.FLOWING_WATER);
        tag(TagsNF.SEAWATER).add(FluidsNF.SEAWATER.get(), FluidsNF.SEAWATER_FLOWING.get());
        tag(FluidTags.WATER).addTags(TagsNF.FRESHWATER, TagsNF.SEAWATER);
        tag(FluidTags.LAVA).add(FluidsNF.LAVA.get(), FluidsNF.LAVA_FLOWING.get());
    }
}
