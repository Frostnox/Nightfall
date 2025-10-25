package frostnox.nightfall.data;

import frostnox.nightfall.registry.forge.EntitiesNF;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class EntityTypeTagsProviderNF extends EntityTypeTagsProvider {
    public EntityTypeTagsProviderNF(DataGenerator pGenerator, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        List<RegistryObject<?>> drakefowl = List.of(EntitiesNF.DRAKEFOWL_ROOSTER, EntitiesNF.DRAKEFOWL_HEN);
        List<RegistryObject<?>> merbor = List.of(EntitiesNF.MERBOR_TUSKER);
        List<RegistryObject<?>> babies = List.of(EntitiesNF.DRAKEFOWL_CHICK);

        tag(TagsNF.BOAT_PASSENGER).add(merge(merge(drakefowl, merbor, babies)));
        tag(TagsNF.IMPACT_TYPE_BONE).add(EntitiesNF.SKELETON.get());
        tag(TagsNF.IMPACT_TYPE_STONE).add(EntitiesNF.ROCKWORM.get());
        tag(TagsNF.IMPACT_TYPE_GASEOUS);
        tag(EntityTypeTags.IMPACT_PROJECTILES).add(EntitiesNF.THROWN_ROCK.get(), EntitiesNF.ARROW.get(), EntitiesNF.THROWN_WEAPON.get());

        tag(TagsNF.RABBIT_PREDATOR).add(merge(EntitiesNF.COCKATRICE, EntitiesNF.SPIDER, EntitiesNF.ROCKWORM, EntitiesNF.PIT_DEVIL,
                EntitiesNF.SCORPION, EntitiesNF.OLMUR, EntitiesNF.SKARA_SWARM, EntitiesNF.DRAKEFOWL_ROOSTER));
        tag(TagsNF.DEER_PREDATOR).add(merge(EntitiesNF.COCKATRICE, EntitiesNF.ROCKWORM, EntitiesNF.PIT_DEVIL,
                EntitiesNF.SCORPION, EntitiesNF.TROLL, EntitiesNF.OLMUR, EntitiesNF.SPIDER, EntitiesNF.SKARA_SWARM));
        tag(TagsNF.DRAKEFOWL_PREDATOR).add(merge(EntitiesNF.ROCKWORM, EntitiesNF.TROLL, EntitiesNF.OLMUR, EntitiesNF.SKARA_SWARM, EntitiesNF.SPIDER,
                EntitiesNF.PIT_DEVIL, EntitiesNF.SCORPION));
        tag(TagsNF.DRAKEFOWL_PREY).add(merge(EntitiesNF.RABBIT));
        tag(TagsNF.COCKATRICE_PREDATOR).add(merge(EntitiesNF.ROCKWORM, EntitiesNF.TROLL, EntitiesNF.OLMUR, EntitiesNF.SKARA_SWARM));
        tag(TagsNF.COCKATRICE_PREY).add(merge(merge(merbor), EntitiesNF.RABBIT, EntitiesNF.DEER, EntitiesNF.SPIDER));
        tag(TagsNF.SPIDER_PREDATOR).add(merge(EntitiesNF.ROCKWORM, EntitiesNF.PIT_DEVIL, EntitiesNF.SKARA_SWARM));
        tag(TagsNF.SPIDER_PREY).add(merge(merge(drakefowl, babies), EntitiesNF.RABBIT, EntitiesNF.DEER));
        tag(TagsNF.PIT_DEVIL_PREDATOR).add(merge(EntitiesNF.ROCKWORM, EntitiesNF.TROLL, EntitiesNF.OLMUR, EntitiesNF.SKARA_SWARM));
        tag(TagsNF.PIT_DEVIL_PREY).add(merge(merge(drakefowl, babies), EntitiesNF.RABBIT, EntitiesNF.DEER, EntitiesNF.SPIDER));
        tag(TagsNF.SKARA_SWARM_PREY).add(merge(merge(drakefowl, merbor, babies), EntitiesNF.RABBIT, EntitiesNF.DEER, EntitiesNF.SPIDER, EntitiesNF.PIT_DEVIL, EntitiesNF.COCKATRICE,
                EntitiesNF.HUSK, EntitiesNF.DREG));
        tag(TagsNF.JELLYFISH_IMMUNE).add(EntitiesNF.JELLYFISH.get());
        tag(TagsNF.EDIBLE_CORPSE).add(merge(merge(drakefowl, merbor, babies), EntitiesNF.RABBIT, EntitiesNF.SPIDER, EntitiesNF.DEER,
                EntitiesNF.COCKATRICE, EntitiesNF.ROCKWORM, EntitiesNF.PIT_DEVIL));
    }

    private static EntityType<?>[] single(List<RegistryObject<?>> list) {
        return list.stream().map((obj) -> (EntityType<?>) obj.get()).toArray(EntityType<?>[]::new);
    }

    private static List<List<RegistryObject<?>>> merge(List<RegistryObject<?>>... lists) {
        List<List<RegistryObject<?>>> mergedList = new ObjectArrayList<>();
        mergedList.addAll(Arrays.asList(lists));
        return mergedList;
    }

    private static EntityType<?>[] merge(List<List<RegistryObject<?>>> arrays, RegistryObject<?>... types) {
        Stream<EntityType<?>> stream = Arrays.stream(types).map((obj) -> (EntityType<?>) obj.get());
        for(var array : arrays) stream = Stream.concat(stream, array.stream().map((obj) -> (EntityType<?>) obj.get()));
        return stream.toArray(EntityType<?>[]::new);
    }

    private static EntityType<?>[] merge(RegistryObject<?>... types) {
        return Arrays.stream(types).<EntityType<?>>map((obj) -> (EntityType<?>) obj.get()).toArray(EntityType<?>[]::new);
    }
}
