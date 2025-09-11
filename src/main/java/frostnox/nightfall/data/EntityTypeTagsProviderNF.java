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
        tag(TagsNF.IMPACT_TYPE_STONE).add(EntitiesNF.ROCKWORM.get());
        tag(TagsNF.IMPACT_TYPE_GASEOUS);
        tag(EntityTypeTags.IMPACT_PROJECTILES).add(EntitiesNF.THROWN_ROCK.get(), EntitiesNF.ARROW.get(), EntitiesNF.THROWN_WEAPON.get());
        tag(TagsNF.RABBIT_PREDATOR).add(EntitiesNF.COCKATRICE.get(), EntitiesNF.SPIDER.get(), EntitiesNF.ROCKWORM.get(), EntitiesNF.PIT_DEVIL.get(),
                EntitiesNF.SCORPION.get(), EntitiesNF.OLMUR.get(), EntitiesNF.SKARA_SWARM.get());
        tag(TagsNF.DEER_PREDATOR).add(EntitiesNF.COCKATRICE.get(), EntitiesNF.ROCKWORM.get(), EntitiesNF.PIT_DEVIL.get(),
                EntitiesNF.SCORPION.get(), EntitiesNF.TROLL.get(), EntitiesNF.OLMUR.get(), EntitiesNF.SPIDER.get(), EntitiesNF.SKARA_SWARM.get());
        tag(TagsNF.COCKATRICE_PREDATOR).add(EntitiesNF.ROCKWORM.get(), EntitiesNF.TROLL.get(), EntitiesNF.OLMUR.get(), EntitiesNF.SKARA_SWARM.get());
        tag(TagsNF.COCKATRICE_PREY).add(EntitiesNF.RABBIT.get(), EntitiesNF.DEER.get(), EntitiesNF.SPIDER.get());
        tag(TagsNF.SPIDER_PREDATOR).add(EntitiesNF.ROCKWORM.get(), EntitiesNF.PIT_DEVIL.get(), EntitiesNF.SKARA_SWARM.get());
        tag(TagsNF.SPIDER_PREY).add(EntitiesNF.RABBIT.get(), EntitiesNF.DEER.get());
        tag(TagsNF.PIT_DEVIL_PREDATOR).add(EntitiesNF.ROCKWORM.get(), EntitiesNF.TROLL.get(), EntitiesNF.OLMUR.get(), EntitiesNF.SKARA_SWARM.get());
        tag(TagsNF.PIT_DEVIL_PREY).add(EntitiesNF.RABBIT.get(), EntitiesNF.DEER.get(), EntitiesNF.SPIDER.get());
        tag(TagsNF.SKARA_SWARM_PREY).add(EntitiesNF.RABBIT.get(), EntitiesNF.DEER.get(), EntitiesNF.SPIDER.get(), EntitiesNF.PIT_DEVIL.get(), EntitiesNF.COCKATRICE.get(),
                EntitiesNF.HUSK.get(), EntitiesNF.DREG.get());
        tag(TagsNF.JELLYFISH_IMMUNE).add(EntitiesNF.JELLYFISH.get());
        tag(TagsNF.EDIBLE_CORPSE).add(EntitiesNF.RABBIT.get(), EntitiesNF.SPIDER.get(), EntitiesNF.DEER.get(),
                EntitiesNF.COCKATRICE.get(), EntitiesNF.ROCKWORM.get(), EntitiesNF.PIT_DEVIL.get());
    }
}
