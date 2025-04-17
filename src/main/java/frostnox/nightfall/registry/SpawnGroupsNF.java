package frostnox.nightfall.registry;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.world.spawngroup.SimpleSpawnGroup;
import frostnox.nightfall.world.spawngroup.SpawnGroup;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static frostnox.nightfall.world.generation.ContinentalChunkGenerator.SEA_LEVEL;

public class SpawnGroupsNF {
    public static final DeferredRegister<SpawnGroup> GROUPS = DeferredRegister.create(RegistriesNF.SPAWN_GROUPS_KEY, Nightfall.MODID);

    public static final RegistryObject<SpawnGroup> COCKATRICE = GROUPS.register("cockatrice", () -> new SimpleSpawnGroup(
            2, false, EntitiesNF.COCKATRICE.get(), SEA_LEVEL, Integer.MAX_VALUE, 1, 15, 1, 1,
            0.45F, 1F, 0F, 1F, TagsNF.COCKATRICE_SPAWN_BLOCK));
    public static final RegistryObject<SpawnGroup> DEER = GROUPS.register("deer", () -> new SimpleSpawnGroup(
            7, true, EntitiesNF.DEER.get(), SEA_LEVEL, SEA_LEVEL + 208, 1, 15, 1, 4,
            0.05F, 1F, 0.0F, 1.0F, TagsNF.DEER_SPAWN_BLOCK));
    public static final RegistryObject<SpawnGroup> RABBIT = GROUPS.register("rabbit", () -> new SimpleSpawnGroup(
            2, true, EntitiesNF.RABBIT.get(), SEA_LEVEL, SEA_LEVEL + 312, 1, 15, 1, 1,
            0.05F, 1F, 0.0F, 1.0F, TagsNF.RABBIT_SPAWN_BLOCK));
    public static final RegistryObject<SpawnGroup> CREEPER = GROUPS.register("creeper", () -> new SimpleSpawnGroup(
            1, false, EntitiesNF.CREEPER.get(), SEA_LEVEL, Integer.MAX_VALUE, 0, 15, 1, 1,
            0.1F, 1F, 0.4F, 1F, TagsNF.CREEPER_SPAWN_BLOCK));

    public static void register() {
        GROUPS.register(Nightfall.MOD_EVENT_BUS);
    }

    public static SpawnGroup get(ResourceLocation id) {
        return RegistriesNF.getSpawnGroups().getValue(id);
    }

    public static boolean contains(ResourceLocation id) {
        return RegistriesNF.getSpawnGroups().containsKey(id);
    }
}
