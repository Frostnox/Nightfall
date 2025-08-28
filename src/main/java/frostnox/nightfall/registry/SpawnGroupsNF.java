package frostnox.nightfall.registry;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.BiomesNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.world.spawngroup.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static frostnox.nightfall.world.generation.ContinentalChunkGenerator.SEA_LEVEL;

public class SpawnGroupsNF {
    public static final DeferredRegister<SpawnGroup> GROUPS = DeferredRegister.create(RegistriesNF.SPAWN_GROUPS_KEY, Nightfall.MODID);

    public static final RegistryObject<SpawnGroup> COCKATRICE = GROUPS.register("cockatrice", () -> new LandSpawnGroup(
            2, false, EntitiesNF.COCKATRICE.get(), SEA_LEVEL, Integer.MAX_VALUE, 1, 15, 1, 1,
            0.45F, 1F, 0F, 1F, TagsNF.COCKATRICE_SPAWN_BLOCK));
    public static final RegistryObject<SpawnGroup> DEER = GROUPS.register("deer", () -> new LandSpawnGroup(
            7, true, EntitiesNF.DEER.get(), SEA_LEVEL, SEA_LEVEL + 208, 1, 15, 1, 4,
            0.05F, 1F, 0.0F, 1.0F, TagsNF.DEER_SPAWN_BLOCK));
    public static final RegistryObject<SpawnGroup> RABBIT = GROUPS.register("rabbit", () -> new LandSpawnGroup(
            2, true, EntitiesNF.RABBIT.get(), SEA_LEVEL, SEA_LEVEL + 312, 1, 15, 1, 1,
            0.05F, 1F, 0.0F, 1.0F, TagsNF.RABBIT_SPAWN_BLOCK));
    public static final RegistryObject<SpawnGroup> CREEPER = GROUPS.register("creeper", () -> new LandSpawnGroup(
            1, false, EntitiesNF.CREEPER.get(), SEA_LEVEL, Integer.MAX_VALUE, 0, 15, 1, 1,
            0.1F, 1F, 0.4F, 1F, TagsNF.CREEPER_SPAWN_BLOCK));
    public static final RegistryObject<SpawnGroup> JELLYFISH = GROUPS.register("jellyfish", () -> new JellyfishSpawnGroup(
            2, true, EntitiesNF.JELLYFISH.get(), SEA_LEVEL - 64, Integer.MAX_VALUE, 0, 15, 5, 13,
            0.35F, 1F, 0F, 1F));
    public static final RegistryObject<SpawnGroup> PIT_DEVIL_SURFACE = GROUPS.register("pit_devil_surface", () -> new LandSpawnGroup(
            1, false, EntitiesNF.PIT_DEVIL.get(), SEA_LEVEL, Integer.MAX_VALUE, 0, 15, 1, 1,
            0.7F, 0.9F, 0.35F, 0.65F, TagsNF.NATURAL_TERRAIN));
    public static final RegistryObject<SpawnGroup> PIT_DEVIL_CAVES = GROUPS.register("pit_devil_caves", () -> new LandBiomeSpawnGroup(
            5, false, EntitiesNF.PIT_DEVIL.get(), 300, Integer.MAX_VALUE, 0, 0, 1, 1,
            BiomesNF.TUNNELS, TagsNF.NATURAL_TERRAIN));
    public static final RegistryObject<SpawnGroup> ECTOPLASM_LARGE = GROUPS.register("ectoplasm_large", () -> new LandBiomeTagSpawnGroup(
            2, false, EntitiesNF.ECTOPLASM_LARGE.get(), Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0, 1, 1,
            TagsNF.CAVE, TagsNF.NATURAL_TERRAIN));
    public static final RegistryObject<SpawnGroup> ECTOPLASM_MEDIUM = GROUPS.register("ectoplasm_medium", () -> new LandBiomeTagSpawnGroup(
            1, false, EntitiesNF.ECTOPLASM_MEDIUM.get(), Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0, 1, 1,
            TagsNF.CAVE, TagsNF.NATURAL_TERRAIN));

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
