package frostnox.nightfall.data;

import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.registry.SpawnGroupsNF;
import frostnox.nightfall.world.spawngroup.SpawnGroup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;

import javax.annotation.Nullable;

public class SpawnGroupTagsProvider extends ForgeRegistryTagsProvider<SpawnGroup> {
    public SpawnGroupTagsProvider(DataGenerator generator, String id, @Nullable ExistingFileHelper helper) {
        super(generator, RegistriesNF.getSpawnGroups(), id, helper);
    }

    @Override
    protected void addTags() {
        tag(TagsNF.SURFACE_GROUPS).add(SpawnGroupsNF.COCKATRICE.get(), SpawnGroupsNF.DEER.get(), SpawnGroupsNF.RABBIT.get(),
                SpawnGroupsNF.WOLF.get(),
                SpawnGroupsNF.DRAKEFOWL.get(), SpawnGroupsNF.MERBOR.get(), SpawnGroupsNF.CREEPER.get(),
                SpawnGroupsNF.PIT_DEVIL_SURFACE.get());
        tag(TagsNF.FRESHWATER_GROUPS);
        tag(TagsNF.OCEAN_GROUPS).add(SpawnGroupsNF.JELLYFISH.get());
        tag(TagsNF.RANDOM_GROUPS).add(SpawnGroupsNF.PIT_DEVIL_CAVES.get(), SpawnGroupsNF.ECTOPLASM_LARGE.get(), SpawnGroupsNF.ECTOPLASM_MEDIUM.get());
    }

    @Override
    public String getName() {
        return "Spawn Groups";
    }
}
