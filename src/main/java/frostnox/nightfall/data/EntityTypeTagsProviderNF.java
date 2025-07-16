package frostnox.nightfall.data;

import frostnox.nightfall.registry.forge.EntitiesNF;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class EntityTypeTagsProviderNF extends EntityTypeTagsProvider {
    public EntityTypeTagsProviderNF(DataGenerator pGenerator, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(TagsNF.BOAT_PASSENGER);
        tag(TagsNF.IMPACT_TYPE_BONE).add(EntitiesNF.SKELETON.get());
        tag(TagsNF.IMPACT_TYPE_STONE);
        tag(TagsNF.IMPACT_TYPE_GASEOUS);
        tag(EntityTypeTags.IMPACT_PROJECTILES).add(EntitiesNF.THROWN_ROCK.get(), EntitiesNF.ARROW.get(), EntitiesNF.THROWN_WEAPON.get());
        tag(TagsNF.RABBIT_PREDATOR).add(EntitiesNF.COCKATRICE.get(), EntitiesNF.SPIDER.get(), EntitiesNF.SKULLWORM.get(), EntitiesNF.PIT_DEVIL.get(),
                EntitiesNF.SCORPION.get(), EntitiesNF.OLMUR.get());
        tag(TagsNF.DEER_PREDATOR).add(EntitiesNF.COCKATRICE.get(), EntitiesNF.SKULLWORM.get(), EntitiesNF.PIT_DEVIL.get(),
                EntitiesNF.SCORPION.get(), EntitiesNF.TROLL.get(), EntitiesNF.OLMUR.get());
        tag(TagsNF.COCKATRICE_PREDATOR).add(EntitiesNF.SKULLWORM.get(), EntitiesNF.TROLL.get(), EntitiesNF.OLMUR.get());
        tag(TagsNF.COCKATRICE_PREY).add(EntitiesNF.RABBIT.get(), EntitiesNF.DEER.get(), EntitiesNF.SPIDER.get());
        tag(TagsNF.SPIDER_PREDATOR).add(EntitiesNF.SKULLWORM.get());
        tag(TagsNF.SPIDER_PREY).add(EntitiesNF.RABBIT.get());
        tag(TagsNF.JELLYFISH_IMMUNE).add(EntitiesNF.JELLYFISH.get());
    }
}
