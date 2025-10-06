package frostnox.nightfall.registry;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.Tree;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.encyclopedia.Puzzle;
import frostnox.nightfall.encyclopedia.knowledge.ItemKnowledge;
import frostnox.nightfall.encyclopedia.knowledge.ItemTagKnowledge;
import frostnox.nightfall.registry.forge.ItemsNF;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Set;

public class EntriesNF {
    public static final DeferredRegister<Entry> ENTRIES = DeferredRegister.create(RegistriesNF.ENTRIES_KEY, Nightfall.MODID);

    //Survival
    public static final RegistryObject<Entry> TOOLS = ENTRIES.register("tools", () -> Entry.create(
            List.of(),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(ItemsNF.FLINT.get()), Ingredient.of(ItemsNF.STICK.get()), Ingredient.of(ItemsNF.PLANT_FIBERS.get())),
                    List.of())));
    public static final RegistryObject<Entry> SLING = ENTRIES.register("sling", () -> Entry.create(
            List.of(TOOLS),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(ItemsNF.ROPE.get()), Ingredient.of(TagsNF.ROCK)),
                    List.of())));
    public static final RegistryObject<Entry> REINFORCED_SLING = ENTRIES.register("reinforced_sling",
            () -> Entry.createAddendum(List.of(SLING), knowledge(ItemsNF.LEATHER)));
    public static final RegistryObject<Entry> TAMING = ENTRIES.register("taming", () -> Entry.create(
            List.of(SLING),
            Set.of(),
            new Puzzle(List.of(KnowledgeNF.TAMED_ANIMAL),
                    List.of(),
                    List.of())));
    public static final RegistryObject<Entry> TROUGH = ENTRIES.register("trough",
            () -> Entry.createAddendum(List.of(TAMING, EntriesNF.WOODWORKING)));
    public static final RegistryObject<Entry> WOODCARVING = ENTRIES.register("woodcarving", () -> Entry.create(
            List.of(TOOLS),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(ItemTags.LOGS)),
                    List.of())));
    public static final RegistryObject<Entry> WOODWORKING = ENTRIES.register("woodworking", () -> Entry.create(
            List.of(WOODCARVING),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.PLANK)),
                    List.of())));
    public static final RegistryObject<Entry> ADVANCED_WOODWORKING = ENTRIES.register("advanced_woodworking", () -> Entry.create(
            List.of(WOODWORKING),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.PLANK_SOFT), Ingredient.of(TagsNF.PLANK_HARD), Ingredient.of(TagsNF.PLANK_FAIR)),
                    List.of())));
    public static final RegistryObject<Entry> WOODEN_SHIELD = ENTRIES.register("wooden_shield", () -> Entry.create(
            List.of(WOODWORKING),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(ItemsNF.PLANKS.get(Tree.IRONWOOD).get())),
                    List.of())));
    public static final RegistryObject<Entry> TANNING = ENTRIES.register("tanning", () -> Entry.create(
            List.of(WOODWORKING),
            Set.of(knowledge(TagsNF.ANIMAL_HIDE)),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.ANIMAL_HIDE), Ingredient.of(ItemsNF.WATER.get()), Ingredient.of(ItemsNF.LIME.get()), Ingredient.of(TagsNF.TANNIN)),
                    List.of())));
    public static final RegistryObject<Entry> CURING = ENTRIES.register("curing",
            () -> Entry.createAddendum(List.of(TANNING), knowledge(ItemsNF.SALT), knowledge(TagsNF.CURABLE_FOOD)));
    public static final RegistryObject<Entry> CAMPFIRE = ENTRIES.register("campfire", () -> Entry.create(
            List.of(TOOLS),
            Set.of(KnowledgeNF.LOG_TAG),
            new Puzzle(List.of(KnowledgeNF.STARTED_FIRE),
                    List.of(Ingredient.of(ItemsNF.FIREWOOD.get())),
                    List.of())));
    public static final RegistryObject<Entry> POTTERY = ENTRIES.register("pottery", () -> Entry.create(
            List.of(CAMPFIRE),
            Set.of(),
            new Puzzle(List.of(knowledge(ItemsNF.CLAY)),
                    List.of(Ingredient.of(TagsNF.HEAT_RESISTANT_MATERIAL_1)),
                    List.of(WorldConditionsNF.HEAT_SOURCE))));
    public static final RegistryObject<Entry> COOKING = ENTRIES.register("cooking", () -> Entry.create(
            List.of(POTTERY),
            Set.of(),
            new Puzzle(List.of(KnowledgeNF.COLLECTED_WATER, knowledge(TagsNF.COOKED_MEAT), knowledge(TagsNF.COOKED_VEGETABLE)),
                    List.of(),
                    List.of())));
    public static final RegistryObject<Entry> WEAVING = ENTRIES.register("weaving", () -> Entry.create(
            List.of(TOOLS),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(ItemsNF.FLAX_FIBERS.get())),
                    List.of())));
    public static final RegistryObject<Entry> MEDICINAL_BANDAGE = ENTRIES.register("medicinal_bandage", () -> Entry.create(
            List.of(WEAVING),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(ItemsNF.YARROW_POWDER.get())),
                    List.of())));
    public static final RegistryObject<Entry> BOW_AND_ARROW = ENTRIES.register("bow_and_arrow", () -> Entry.create(
            List.of(WEAVING),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.PLANK_BOW), Ingredient.of(TagsNF.FLETCHING)),
                    List.of())));
    public static final RegistryObject<Entry> BONE_ARROW = ENTRIES.register("bone_arrow",
            () -> Entry.createAddendum(List.of(BOW_AND_ARROW), knowledge(ItemsNF.BONE_SHARD)));
    public static final RegistryObject<Entry> WARDING_CHARM = ENTRIES.register("warding_charm", () -> Entry.createHidden(
            List.of(TOOLS),
            Set.of(KnowledgeNF.UNDEAD_PRESENCE),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(ItemsNF.LIVING_BONE.get())),
                    List.of())));
    public static final RegistryObject<Entry> WARDING_EFFIGY = ENTRIES.register("warding_effigy", () -> Entry.create(
            List.of(WARDING_CHARM),
            Set.of(KnowledgeNF.ESSENCE),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(ItemsNF.LIVING_BONE.get()), Ingredient.of(ItemsNF.DREG_HEART.get()), Ingredient.of(ItemsNF.ROTTEN_FLESH.get())),
                    List.of())));
    //Metallurgy
    public static final RegistryObject<Entry> CASTING = ENTRIES.register("casting", () -> Entry.createHidden(
            List.of(POTTERY),
            Set.of(knowledge(TagsNF.CRUCIBLE_METAL)),
            new Puzzle(List.of(KnowledgeNF.MELTED_CASTABLE_METAL),
                    List.of(),
                    List.of())));
    public static final RegistryObject<Entry> CHISEL_MOLD = ENTRIES.register("chisel_mold",
            () -> Entry.createAddendum(List.of(CASTING, WOODCARVING)));
    public static final RegistryObject<Entry> ARROWHEAD_MOLD = ENTRIES.register("arrowhead_mold",
            () -> Entry.createAddendum(List.of(CASTING, BOW_AND_ARROW)));
    public static final RegistryObject<Entry> SMITHING = ENTRIES.register("smithing", () -> Entry.create(
            List.of(CASTING),
            Set.of(),
            new Puzzle(List.of(KnowledgeNF.MELTED_HARD_METAL, KnowledgeNF.IMPROVISED_ANVIL),
                    List.of(),
                    List.of())));
    public static final RegistryObject<Entry> SMELTING = ENTRIES.register("smelting", () -> Entry.create(
            List.of(CASTING),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.HEAT_RESISTANT_MATERIAL_2)),
                    List.of())));
    public static final RegistryObject<Entry> IRONWORKING = ENTRIES.register("ironworking", () -> Entry.create(
            List.of(SMITHING, SMELTING),
            Set.of(knowledge(TagsNF.IRON_ORE)),
            new Puzzle(List.of(KnowledgeNF.WORKED_IRON_BLOOM),
                    List.of(),
                    List.of())));
    public static final RegistryObject<Entry> SABRE = ENTRIES.register("sabre", () -> Entry.createHidden(
            List.of(SMITHING),
            Set.of(knowledge(TagsNF.SABRE_OR_HEAD)),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.SABRE_OR_HEAD)),
                    List.of())));
    public static final RegistryObject<Entry> SICKLE = ENTRIES.register("sickle", () -> Entry.createHidden(
            List.of(CASTING),
            Set.of(knowledge(TagsNF.SICKLE_OR_HEAD)),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.SICKLE_OR_HEAD)),
                    List.of())));
    public static final RegistryObject<Entry> SICKLE_SMITHING = ENTRIES.register("sickle_smithing",
            () -> Entry.createAddendum(List.of(SICKLE, SMITHING)));
    public static final RegistryObject<Entry> BUCKET = ENTRIES.register("bucket", () -> Entry.create(
            List.of(SMITHING),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.CORROSION_RESISTANT_METAL)),
                    List.of(WorldConditionsNF.WATER_SOURCE))));
    public static final RegistryObject<Entry> PLATE_ARMOR = ENTRIES.register("plate_armor", () -> Entry.create(
            List.of(SMITHING),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.LINEN_OR_ARMOR)),
                    List.of())));
    public static final RegistryObject<Entry> CHAINMAIL_ARMOR = ENTRIES.register("chainmail_armor", () -> Entry.create(
            List.of(PLATE_ARMOR),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.WIRES)),
                    List.of())));
    public static final RegistryObject<Entry> SCALE_ARMOR = ENTRIES.register("scale_armor", () -> Entry.create(
            List.of(SMITHING),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.PLATES), Ingredient.of(TagsNF.LEATHER_OR_ARMOR)),
                    List.of())));
    public static final RegistryObject<Entry> MACE = ENTRIES.register("mace", () -> Entry.create(
            List.of(SMITHING),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.MACE_PUZZLE_ITEM)),
                    List.of())));
    public static final RegistryObject<Entry> SHIELD = ENTRIES.register("shield", () -> Entry.create(
            List.of(SMITHING),
            Set.of(),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.SHIELD), Ingredient.of(TagsNF.WIRES)),
                    List.of())));
    public static final RegistryObject<Entry> SLAYER_PLATE = ENTRIES.register("slayer_plate", () -> Entry.createHidden(
            List.of(PLATE_ARMOR),
            Set.of(knowledge(TagsNF.SLAYER_PLATE)),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.SLAYER_PLATE)),
                    List.of())));
    public static final RegistryObject<Entry> SLAYER_CHAINMAIL = ENTRIES.register("slayer_chainmail", () -> Entry.createHidden(
            List.of(CHAINMAIL_ARMOR),
            Set.of(knowledge(TagsNF.SLAYER_CHAINMAIL)),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.SLAYER_CHAINMAIL)),
                    List.of())));
    public static final RegistryObject<Entry> SLAYER_SCALE = ENTRIES.register("slayer_scale", () -> Entry.createHidden(
            List.of(SCALE_ARMOR),
            Set.of(knowledge(TagsNF.SLAYER_SCALE)),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.SLAYER_SCALE)),
                    List.of())));
    public static final RegistryObject<Entry> EXPLORER_PLATE = ENTRIES.register("explorer_plate", () -> Entry.createHidden(
            List.of(PLATE_ARMOR),
            Set.of(knowledge(TagsNF.EXPLORER_PLATE)),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.EXPLORER_PLATE)),
                    List.of())));
    public static final RegistryObject<Entry> EXPLORER_CHAINMAIL = ENTRIES.register("explorer_chainmail", () -> Entry.createHidden(
            List.of(CHAINMAIL_ARMOR),
            Set.of(knowledge(TagsNF.EXPLORER_CHAINMAIL)),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.EXPLORER_CHAINMAIL)),
                    List.of())));
    public static final RegistryObject<Entry> EXPLORER_SCALE = ENTRIES.register("explorer_scale", () -> Entry.createHidden(
            List.of(SCALE_ARMOR),
            Set.of(knowledge(TagsNF.EXPLORER_SCALE)),
            new Puzzle(List.of(),
                    List.of(Ingredient.of(TagsNF.EXPLORER_SCALE)),
                    List.of())));

    public static void register() {
        ENTRIES.register(Nightfall.MOD_EVENT_BUS);
    }

    public static Entry get(ResourceLocation id) {
        return RegistriesNF.getEntries().getValue(id);
    }

    public static boolean contains(ResourceLocation id) {
        return RegistriesNF.getEntries().containsKey(id);
    }

    private static RegistryObject<ItemKnowledge> knowledge(RegistryObject<? extends Item> item) {
        return KnowledgeNF.ITEMS.get(item);
    }

    private static RegistryObject<ItemTagKnowledge> knowledge(TagKey<Item> tag) {
        return KnowledgeNF.ITEM_TAGS.get(tag);
    }
}
