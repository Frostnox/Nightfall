package frostnox.nightfall.registry;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.block.*;
import frostnox.nightfall.data.recipe.IEncyclopediaRecipe;
import frostnox.nightfall.data.recipe.ToolIngredientRecipe;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.item.*;
import frostnox.nightfall.world.condition.WorldCondition;
import frostnox.nightfall.world.spawngroup.SpawnGroup;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class RegistriesNF {
    public static final ResourceKey<Registry<Action>> ACTIONS_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "actions"));
    private static IForgeRegistryModifiable<Action> ACTIONS;
    public static final ResourceKey<Registry<Entry>> ENTRIES_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "entries"));
    private static IForgeRegistryModifiable<Entry> ENTRIES;
    public static final ResourceKey<Registry<Knowledge>> KNOWLEDGE_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "knowledge"));
    private static IForgeRegistryModifiable<Knowledge> KNOWLEDGE;
    public static final ResourceKey<Registry<WorldCondition>> WORLD_CONDITIONS_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "world_conditions"));
    private static IForgeRegistryModifiable<WorldCondition> WORLD_CONDITIONS;
    public static final ResourceKey<Registry<SpawnGroup>> SPAWN_GROUPS_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "spawn_groups"));
    private static IForgeRegistryModifiable<SpawnGroup> SPAWN_GROUPS;
    //Wrapped registries to allow use of Enum
    //These registries should only be used to iterate over all objects of the interface (single objects may be accessed directly)
    public static final ResourceKey<Registry<IArmament.Entry>> ARMAMENTS_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "armaments"));
    private static IForgeRegistryModifiable<IArmament.Entry> ARMAMENTS;
    public static final ResourceKey<Registry<IDye.Entry>> DYES_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "dyes"));
    private static IForgeRegistryModifiable<IDye.Entry> DYES;
    public static final ResourceKey<Registry<IMetal.Entry>> METALS_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "metals"));
    private static IForgeRegistryModifiable<IMetal.Entry> METALS;
    public static final ResourceKey<Registry<ISoil.Entry>> SOILS_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "soils"));
    private static IForgeRegistryModifiable<ISoil.Entry> SOILS;
    public static final ResourceKey<Registry<IStone.Entry>> STONES_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "stones"));
    private static IForgeRegistryModifiable<IStone.Entry> STONES;
    public static final ResourceKey<Registry<IStyle.Entry>> STYLES_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "styles"));
    private static IForgeRegistryModifiable<IStyle.Entry> STYLES;
    public static final ResourceKey<Registry<ITieredArmorMaterial.Entry>> TIERED_ARMOR_MATERIALS_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "tiered_armor_materials"));
    private static IForgeRegistryModifiable<ITieredArmorMaterial.Entry> TIERED_ARMOR_MATERIALS;
    public static final ResourceKey<Registry<ITree.Entry>> TREES_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "trees"));
    private static IForgeRegistryModifiable<ITree.Entry> TREES;

    private static ObjectSet<Knowledge> ACTIVE_KNOWLEDGE;
    private static ObjectSet<INaturalVegetation> NATURAL_VEGETATION;

    public static IForgeRegistryModifiable<Action> getActions() {
        if(ACTIONS == null) ACTIONS = RegistryManager.ACTIVE.getRegistry(ACTIONS_KEY);
        return ACTIONS;
    }

    public static IForgeRegistryModifiable<Entry> getEntries() {
        if(ENTRIES == null) ENTRIES = RegistryManager.ACTIVE.getRegistry(ENTRIES_KEY);
        return ENTRIES;
    }

    public static IForgeRegistryModifiable<Knowledge> getKnowledge() {
        if(KNOWLEDGE == null) KNOWLEDGE = RegistryManager.ACTIVE.getRegistry(KNOWLEDGE_KEY);
        return KNOWLEDGE;
    }

    public static void buildActiveKnowledge(ServerLevel level) {
        ObjectSet<Knowledge> activeKnowledge = new ObjectOpenHashSet<>(getKnowledge().getEntries().size());
        for(Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
            if(recipe instanceof IEncyclopediaRecipe encyclopediaRecipe) {
                for(Ingredient ingredient : encyclopediaRecipe.getUnlockIngredients()) {
                    for(ItemStack item : ingredient.getItems()) {
                        Knowledge knowledge = getKnowledge().getValue(ResourceLocation.parse(item.getItem().getRegistryName().toString() + "_item"));
                        if(knowledge != null) activeKnowledge.add(knowledge);
                    }
                }
                Knowledge requiredKnowledge = getKnowledge().getValue(encyclopediaRecipe.getRequirementId());
                if(requiredKnowledge != null) activeKnowledge.add(requiredKnowledge);
            }
        }
        for(Entry entry : RegistriesNF.getEntries()) {
            for(RegistryObject<? extends Knowledge> knowledge : entry.getAssociatedKnowledge()) {
                activeKnowledge.add(knowledge.get());
            }
        }
        ACTIVE_KNOWLEDGE = ObjectSets.unmodifiable(activeKnowledge);
    }

    /**
     * @return knowledge set used for currently loaded world; does not exist on client!
     */
    public static ObjectSet<Knowledge> getActiveServerKnowledge() {
        return ACTIVE_KNOWLEDGE;
    }

    public static void buildNaturalVegetation() {
        ObjectSet<INaturalVegetation> set = new ObjectOpenHashSet<>();
        for(Block block : ForgeRegistries.BLOCKS.getValues()) {
            if(block instanceof INaturalVegetation vegetation) set.add(vegetation);
        }
        NATURAL_VEGETATION = ObjectSets.unmodifiable(set);
    }

    public static ObjectSet<INaturalVegetation> getNaturalVegetation() {
        return NATURAL_VEGETATION;
    }

    public static IForgeRegistryModifiable<WorldCondition> getWorldConditions() {
        if(WORLD_CONDITIONS == null) WORLD_CONDITIONS = RegistryManager.ACTIVE.getRegistry(WORLD_CONDITIONS_KEY);
        return WORLD_CONDITIONS;
    }

    public static IForgeRegistryModifiable<SpawnGroup> getSpawnGroups() {
        if(SPAWN_GROUPS == null) SPAWN_GROUPS = RegistryManager.ACTIVE.getRegistry(SPAWN_GROUPS_KEY);
        return SPAWN_GROUPS;
    }

    public static IForgeRegistryModifiable<IArmament.Entry> getArmaments() {
        if(ARMAMENTS == null) ARMAMENTS = RegistryManager.ACTIVE.getRegistry(ARMAMENTS_KEY);
        return ARMAMENTS;
    }

    public static IForgeRegistryModifiable<IDye.Entry> getDyes() {
        if(DYES == null) DYES = RegistryManager.ACTIVE.getRegistry(DYES_KEY);
        return DYES;
    }

    public static IForgeRegistryModifiable<IMetal.Entry> getMetals() {
        if(METALS == null) METALS = RegistryManager.ACTIVE.getRegistry(METALS_KEY);
        return METALS;
    }

    public static IForgeRegistryModifiable<ISoil.Entry> getSoils() {
        if(SOILS == null) SOILS = RegistryManager.ACTIVE.getRegistry(SOILS_KEY);
        return SOILS;
    }

    public static IForgeRegistryModifiable<IStone.Entry> getStones() {
        if(STONES == null) STONES = RegistryManager.ACTIVE.getRegistry(STONES_KEY);
        return STONES;
    }

    public static IForgeRegistryModifiable<IStyle.Entry> getStyles() {
        if(STYLES == null) STYLES = RegistryManager.ACTIVE.getRegistry(STYLES_KEY);
        return STYLES;
    }

    public static IForgeRegistryModifiable<ITieredArmorMaterial.Entry> getTieredArmorMaterials() {
        if(TIERED_ARMOR_MATERIALS == null) TIERED_ARMOR_MATERIALS = RegistryManager.ACTIVE.getRegistry(TIERED_ARMOR_MATERIALS_KEY);
        return TIERED_ARMOR_MATERIALS;
    }

    public static IForgeRegistryModifiable<ITree.Entry> getTrees() {
        if(TREES == null) TREES = RegistryManager.ACTIVE.getRegistry(TREES_KEY);
        return TREES;
    }

    @SubscribeEvent
    public static void onRegisterArmamentsEvent(RegistryEvent.Register<IArmament.Entry> event) {
        for(Armament value : Armament.values()) event.getRegistry().register(new IArmament.Entry(value).setRegistryName(Nightfall.MODID, value.getName()));
    }

    @SubscribeEvent
    public static void onRegisterDyesEvent(RegistryEvent.Register<IDye.Entry> event) {
        for(Dye value : Dye.values()) event.getRegistry().register(new IDye.Entry(value).setRegistryName(Nightfall.MODID, value.getName()));
    }

    @SubscribeEvent
    public static void onRegisterMetalsEvent(RegistryEvent.Register<IMetal.Entry> event) {
        for(Metal value : Metal.values()) event.getRegistry().register(new IMetal.Entry(value).setRegistryName(Nightfall.MODID, value.getName()));
    }

    @SubscribeEvent
    public static void onRegisterSoilsEvent(RegistryEvent.Register<ISoil.Entry> event) {
        for(Soil value : Soil.values()) event.getRegistry().register(new ISoil.Entry(value).setRegistryName(Nightfall.MODID, value.getName()));
    }

    @SubscribeEvent
    public static void onRegisterStonesEvent(RegistryEvent.Register<IStone.Entry> event) {
        for(Stone value : Stone.values()) event.getRegistry().register(new IStone.Entry(value).setRegistryName(Nightfall.MODID, value.getName()));
    }

    @SubscribeEvent
    public static void onRegisterStylesEvent(RegistryEvent.Register<IStyle.Entry> event) {
        for(Style value : Style.values()) event.getRegistry().register(new IStyle.Entry(value).setRegistryName(Nightfall.MODID, value.getName()));
    }

    @SubscribeEvent
    public static void onRegisterTieredArmorMaterialsEvent(RegistryEvent.Register<ITieredArmorMaterial.Entry> event) {
        for(TieredArmorMaterial value : TieredArmorMaterial.values()) event.getRegistry().register(new ITieredArmorMaterial.Entry(value).setRegistryName(Nightfall.MODID, value.getName()));
    }

    @SubscribeEvent
    public static void onRegisterTreesEvent(RegistryEvent.Register<ITree.Entry> event) {
        for(Tree value : Tree.values()) event.getRegistry().register(new ITree.Entry(value).setRegistryName(Nightfall.MODID, value.getName()));
    }
}
