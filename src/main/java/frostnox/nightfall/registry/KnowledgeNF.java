package frostnox.nightfall.registry;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.Tree;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.encyclopedia.knowledge.ItemKnowledge;
import frostnox.nightfall.encyclopedia.knowledge.ItemTagKnowledge;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.util.DataUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class KnowledgeNF {
    public static final ResourceLocation KNOWLEDGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "gui/icon/knowledge");
    public static final ResourceLocation UNKNOWN_KNOWLEDGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "gui/icon/unknown_knowledge");

    public static final DeferredRegister<Knowledge> KNOWLEDGE = DeferredRegister.create(RegistriesNF.KNOWLEDGE_KEY, Nightfall.MODID);

    //Knowledge versions of entries, can use these to have entries be treated purely as knowledge (will be added automatically upon completion if using same naming scheme)
    public static final Map<RegistryObject<Entry>, RegistryObject<Knowledge>> ENTRIES = DataUtil.map(EntriesNF.ENTRIES.getEntries(),
            (entry) -> KNOWLEDGE.register(entry.getId().getPath() + "_entry", Knowledge::new));

    public static final Map<RegistryObject<Item>, RegistryObject<ItemKnowledge>> ITEMS = DataUtil.map(ItemsNF.ITEMS.getEntries(),
            (item) -> KNOWLEDGE.register(item.getId().getPath() + "_item", () -> new ItemKnowledge(item.get())));

    public static final Map<TagKey<Item>, RegistryObject<ItemTagKnowledge>> ITEM_TAGS = DataUtil.map(TagsNF.ITEM_TAGS,
            (tag) -> KNOWLEDGE.register(tag.location().getPath().replace("/", "_") + "_item_tag", () -> new ItemTagKnowledge(
                    ForgeRegistries.ITEMS.tags().getTag(tag).stream().findFirst().orElse(Items.AIR), tag)));
    public static final RegistryObject<ItemTagKnowledge> LOG_TAG = KNOWLEDGE.register("log_item_tag", () -> new ItemTagKnowledge(ItemsNF.LOGS.get(Tree.OAK).get(), ItemTags.LOGS));

    public static final RegistryObject<Knowledge> STARTED_FIRE = KNOWLEDGE.register("started_fire", () -> new Knowledge());
    public static final RegistryObject<Knowledge> STARTED_FIRE_IRON_ORE = KNOWLEDGE.register("started_fire_iron_ore", () -> new Knowledge());
    public static final RegistryObject<Knowledge> COLLECTED_WATER = KNOWLEDGE.register("collected_water", () -> new Knowledge());
    public static final RegistryObject<Knowledge> TAMED_ANIMAL = KNOWLEDGE.register("tamed_animal", () -> new Knowledge());
    public static final RegistryObject<Knowledge> ESSENCE = KNOWLEDGE.register("essence_knowledge", () -> new ItemKnowledge(ItemsNF.METEORITE_CHUNK.get()));
    public static final RegistryObject<Knowledge> UNDEAD_PRESENCE = KNOWLEDGE.register("undead_presence", () -> new Knowledge());
    public static final RegistryObject<Knowledge> MELTED_HARD_METAL = KNOWLEDGE.register("melted_hard_metal", () -> new Knowledge());
    public static final RegistryObject<Knowledge> MELTED_CASTABLE_METAL = KNOWLEDGE.register("melted_castable_metal", () -> new Knowledge());
    public static final RegistryObject<Knowledge> IMPROVISED_ANVIL = KNOWLEDGE.register("improvised_anvil", () -> new ItemTagKnowledge(ItemsNF.ANVILS_METAL.get(Metal.COPPER).get(), TagsNF.ANVILS_ITEM));
    public static final RegistryObject<Knowledge> WORKED_IRON_BLOOM = KNOWLEDGE.register("worked_iron_bloom", () -> new Knowledge());

    public static void register() {
        KNOWLEDGE.register(Nightfall.MOD_EVENT_BUS);
    }

    public static Knowledge get(ResourceLocation id) {
        return RegistriesNF.getKnowledge().getValue(id);
    }

    public static RegistryObject<Knowledge> get(RegistryObject<Entry> entry) {
        return ENTRIES.get(entry);
    }

    public static boolean contains(ResourceLocation id) {
        return RegistriesNF.getKnowledge().containsKey(id);
    }
}
