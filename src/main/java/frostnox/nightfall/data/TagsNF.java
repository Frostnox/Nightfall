package frostnox.nightfall.data;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.util.DataUtil;
import frostnox.nightfall.world.spawngroup.SpawnGroup;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Map;

public class TagsNF {
    public static final ObjectOpenHashSet<TagKey<Item>> ITEM_TAGS = new ObjectOpenHashSet<>(128);

    public static final TagKey<Action> SMITHING_ACTION = actionTag("smithing");
    public static final TagKey<Action> BOW_ACTION = actionTag("bow");
    public static final TagKey<Action> SLING_ACTION = actionTag("sling");
    public static final TagKey<Action> ADZE_ACTION = actionTag("adze");
    public static final TagKey<Action> CHOPPING_ACTION = actionTag("chopping");
    public static final TagKey<Action> SLOW_PLAYER_HARVEST_ACTION = actionTag("slow_player_harvest");

    public static final TagKey<Biome> LAND = biomeTag("land_biome");
    public static final TagKey<Biome> CAVE = biomeTag("cave_biome");
    public static final TagKey<Biome> GEN_RUINS_SURFACE = biomeTag("has_structure/ruins_surface");
    public static final TagKey<Biome> GEN_COTTAGE_RUINS = biomeTag("has_structure/cottage_ruins");
    public static final TagKey<Biome> GEN_SLAYER_RUINS = biomeTag("has_structure/slayer_ruins");
    public static final TagKey<Biome> GEN_EXPLORER_RUINS = biomeTag("has_structure/explorer_ruins");
    public static final TagKey<Biome> GEN_DESERTED_CAMP = biomeTag("has_structure/deserted_camp");

    public static final TagKey<Block> SILT = blockTag("silt_block");
    public static final TagKey<Block> LOAM = blockTag("loam_block");
    public static final TagKey<Block> SOIL = blockTag("soil");
    public static final TagKey<Block> NATURAL_SOIL = blockTag("natural_soil");
    public static final TagKey<Block> NATURAL_STONE = blockTag("natural_stone");
    public static final TagKey<Block> NATURAL_TERRAIN = blockTag("natural_terrain");
    public static final TagKey<Block> TILLABLE_SOIL = blockTag("tillable_soil");
    public static final TagKey<Block> TILLABLE_OR_AQUATIC_SOIL = blockTag("tillable_or_aquatic_soil");
    public static final TagKey<Block> TILLED_SOIL = blockTag("tilled_soil");
    public static final TagKey<Block> TERRACOTTA = blockTag("terracotta");
    public static final TagKey<Block> TREE_WOOD = blockTag("tree_wood");
    public static final TagKey<Block> BRANCHES_OR_LEAVES = blockTag("branches_or_leaves");
    public static final TagKey<Block> SIDINGS = blockTag("sidings");
    public static final TagKey<Block> WOODEN_SIDINGS = blockTag("sidings/wooden_sidings");
    public static final TagKey<Block> HATCHES = blockTag("hatches");
    public static final TagKey<Block> WOODEN_HATCHES = blockTag("hatches/wooden_hatches");
    public static final TagKey<Block> LADDERS = blockTag("ladders");
    public static final TagKey<Block> RACKS = blockTag("racks");
    public static final TagKey<Block> SHELVES = blockTag("shelves");
    public static final TagKey<Block> CHAIRS = blockTag("chairs");
    public static final TagKey<Block> TROUGHS = blockTag("troughs");
    public static final TagKey<Block> ITEM_FRAMES = blockTag("item_frames");
    public static final TagKey<Block> WOODEN_LADDERS = blockTag("ladders/wooden_ladders");
    public static final TagKey<Block> WOODEN_CHESTS = blockTag("chests/wooden_chests");
    public static final TagKey<Block> WOODEN_RACKS = blockTag("wooden_racks");
    public static final TagKey<Block> WOODEN_SHELVES = blockTag("wooden_shelves");
    public static final TagKey<Block> WOODEN_BARRELS = blockTag("wooden_barrels");
    public static final TagKey<Block> WOODEN_FENCE_GATES = blockTag("wooden_fence_gates");
    public static final TagKey<Block> STONE_TUNNELS = blockTag("stone_tunnels");
    public static final TagKey<Block> ANVILS = blockTag("anvils");
    public static final TagKey<Block> METAL_ANVILS = blockTag("metal_anvils");
    public static final TagKey<Block> METAL_BLOCKS = blockTag("metal_blocks");
    public static final TagKey<Block> UNCLIMBABLE = blockTag("unclimbable_block");
    public static final TagKey<Block> FULLY_CLIMBABLE = blockTag("fully_climbable_block");
    public static final TagKey<Block> CAN_IGNITE_ITEMS = blockTag("can_ignite_items");
    public static final TagKey<Block> HEAT_RESISTANT_1 = blockTag("metallurgy/heat_resistant_1");
    public static final TagKey<Block> HEAT_RESISTANT_2 = blockTag("metallurgy/heat_resistant_2");
    public static final TagKey<Block> HEAT_RESISTANT_3 = blockTag("metallurgy/heat_resistant_3");
    public static final TagKey<Block> HEAT_RESISTANT_4 = blockTag("metallurgy/heat_resistant_4");
    public static final TagKey<Block> MINEABLE_WITH_SICKLE = blockTag("mineable_with_sickle");
    public static final TagKey<Block> MINEABLE_WITH_DAGGER = blockTag("mineable_with_dagger");
    public static final TagKey<Block> MINEABLE_WITH_ADZE = blockTag("mineable_with_adze");
    public static final TagKey<Block> MINEABLE_WITH_MAUL = blockTag("mineable_with_maul");
    public static final TagKey<Block> FALLING_DESTROYABLE = blockTag("falling_destroyable");
    public static final TagKey<Block> SHATTER_ON_FALL = blockTag("shatter_on_fall");
    public static final TagKey<Block> SALT_MELTS = blockTag("salt_melts");
    public static final TagKey<Block> UNSTABLE_SUPPORT_HORIZONTAL = blockTag("unstable_support_horizontal");
    public static final TagKey<Block> HAS_PHYSICS = blockTag("support/has_physics");
    public static final TagKey<Block> FLOATS = blockTag("support/floats");
    public static final TagKey<Block> SUPPORT_1 = blockTag("support/units_1");
    public static final TagKey<Block> SUPPORT_2 = blockTag("support/units_2");
    public static final TagKey<Block> SUPPORT_4 = blockTag("support/units_4");
    public static final TagKey<Block> SUPPORT_8 = blockTag("support/units_8");
    public static final TagKey<Block> NO_BREAKING_TEXTURE = blockTag("no_breaking_texture");
    public static final TagKey<Block> DRAKEFOWL_NEST_BLOCK = blockTag("drakefowl_nest_block");
    public static final TagKey<Block> COCKATRICE_SPAWN_BLOCK = blockTag("cockatrice_spawn_block");
    public static final TagKey<Block> DEER_SPAWN_BLOCK = blockTag("deer_spawn_block");
    public static final TagKey<Block> RABBIT_SPAWN_BLOCK = blockTag("rabbit_spawn_block");
    public static final TagKey<Block> CREEPER_SPAWN_BLOCK = blockTag("creeper_spawn_block");
    public static final TagKey<Block> SPIDER_FREE_TRAVEL_BLOCK = blockTag("spider_free_travel_block");
    public static final TagKey<Block> TREE_REPLACEABLE = blockTag("tree_replaceable");
    public static final TagKey<Block> STRUCTURE_REPLACEABLE = blockTag("structure_replaceable");
    public static final TagKey<Block> STRUCTURE_POST_PROCESS = blockTag("structure_post_process");
    //Mark blocks that only exist for specific technical purposes
    public static final TagKey<Block> TECHNICAL = blockTag("technical");

    public static final TagKey<EntityType<?>> BOAT_PASSENGER = entityTag("boat_passenger");
    public static final TagKey<EntityType<?>> IMPACT_TYPE_BONE = entityTag("impact_type/bone"); //Determines sound when hit (flesh is default)
    public static final TagKey<EntityType<?>> IMPACT_TYPE_STONE = entityTag("impact_type/stone");
    public static final TagKey<EntityType<?>> IMPACT_TYPE_GASEOUS = entityTag("impact_type/gaseous");
    public static final TagKey<EntityType<?>> AQUATIC_ENTITY = entityTag("aquatic_entity");
    public static final TagKey<EntityType<?>> RABBIT_PREDATOR = entityTag("rabbit_predator");
    public static final TagKey<EntityType<?>> DEER_PREDATOR = entityTag("deer_predator");
    public static final TagKey<EntityType<?>> DRAKEFOWL_PREDATOR = entityTag("drakefowl_predator");
    public static final TagKey<EntityType<?>> DRAKEFOWL_PREY = entityTag("drakefowl_prey");
    public static final TagKey<EntityType<?>> MERBOR_PREDATOR = entityTag("merbor_predator");
    public static final TagKey<EntityType<?>> COCKATRICE_PREDATOR = entityTag("cockatrice_predator");
    public static final TagKey<EntityType<?>> COCKATRICE_PREY = entityTag("cockatrice_prey");
    public static final TagKey<EntityType<?>> SPIDER_PREDATOR = entityTag("spider_predator");
    public static final TagKey<EntityType<?>> SPIDER_PREY = entityTag("spider_prey");
    public static final TagKey<EntityType<?>> PIT_DEVIL_PREDATOR = entityTag("pit_devil_predator");
    public static final TagKey<EntityType<?>> PIT_DEVIL_PREY = entityTag("pit_devil_prey");
    public static final TagKey<EntityType<?>> SKARA_SWARM_PREY = entityTag("skara_swarm_prey");
    public static final TagKey<EntityType<?>> JELLYFISH_IMMUNE = entityTag("jellyfish_immune");
    public static final TagKey<EntityType<?>> EDIBLE_CORPSE = entityTag("edible_corpse");

    public static final TagKey<Fluid> FRESHWATER = fluidTag("freshwater");
    public static final TagKey<Fluid> SEAWATER = fluidTag("seawater");

    public static final TagKey<Item> RECIPE_GROUP = itemTag("no_recipe_grouping/recipe_group");
    public static final TagKey<Item> ACCESSORY_FACE = itemTag("accessory_face");
    public static final TagKey<Item> ACCESSORY_NECK = itemTag("accessory_neck");
    public static final TagKey<Item> ACCESSORY_WAIST = itemTag("accessory_waist");
    public static final TagKey<Item> SEDIMENTARY = itemTag("rock/sedimentary");
    public static final TagKey<Item> METAMORPHIC = itemTag("rock/metamorphic");
    public static final TagKey<Item> IGNEOUS = itemTag("rock/igneous");
    public static final TagKey<Item> FLUX = itemTag("flux");
    public static final TagKey<Item> TANNIN = itemTag("tannin");
    public static final TagKey<Item> LUMBER_TANNIN = itemTag("lumber_tannin");
    public static final TagKey<Item> ARROWHEAD = itemTag("arrowhead");
    public static final TagKey<Item> ADZE_HEAD = itemTag("adze_head");
    public static final TagKey<Item> AXE_HEAD = itemTag("axe_head");
    public static final TagKey<Item> CHISEL_HEAD = itemTag("chisel_head");
    public static final TagKey<Item> DAGGER_HEAD = itemTag("dagger_head");
    public static final TagKey<Item> MACE_HEAD = itemTag("mace_head");
    public static final TagKey<Item> PICKAXE_HEAD = itemTag("pickaxe_head");
    public static final TagKey<Item> SABRE_HEAD = itemTag("sabre_head");
    public static final TagKey<Item> SHOVEL_HEAD = itemTag("shovel_head");
    public static final TagKey<Item> SICKLE_HEAD = itemTag("sickle_head");
    public static final TagKey<Item> SPEAR_HEAD = itemTag("spear_head");
    public static final TagKey<Item> SWORD_HEAD = itemTag("sword_head");
    public static final TagKey<Item> PICKAXE = itemTag("pickaxe");
    public static final TagKey<Item> AXE = itemTag("axe");
    public static final TagKey<Item> SHOVEL = itemTag("shovel");
    public static final TagKey<Item> ADZE = itemTag("adze");
    public static final TagKey<Item> SICKLE = itemTag("sickle");
    public static final TagKey<Item> DAGGER = itemTag("dagger");
    public static final TagKey<Item> CHISEL = itemTag("chisel");
    public static final TagKey<Item> CHISEL_METAL = itemTag("chisel_metal");
    public static final TagKey<Item> HAMMER = itemTag("hammer");
    public static final TagKey<Item> SWORD = itemTag("sword");
    public static final TagKey<Item> SPEAR = itemTag("spear");
    public static final TagKey<Item> SABRE = itemTag("sabre");
    public static final TagKey<Item> MACE = itemTag("mace");
    public static final TagKey<Item> WOODEN_BOW = itemTag("wooden_bow");
    public static final TagKey<Item> BOW = itemTag("bow");
    public static final TagKey<Item> UNDYED_METAL_SHIELD = itemTag("undyed_metal_shield");
    public static final TagKey<Item> SHIELD = itemTag("shield");
    public static final TagKey<Item> TOOL = itemTag("tool");
    public static final TagKey<Item> ARMAMENT = itemTag("armament"); //Tools and weapons
    public static final TagKey<Item> CHISEL_OR_HAMMER = itemTag("chisel_or_hammer");
    public static final TagKey<Item> SABRE_OR_HEAD = itemTag("sabre_or_head");
    public static final TagKey<Item> SICKLE_OR_HEAD = itemTag("sickle_or_head");
    public static final TagKey<Item> SLING_AMMO = itemTag("sling_ammo");
    public static final TagKey<Item> NO_HITSTOP = itemTag("no_hitstop");
    public static final TagKey<Item> GRID_INTERACTABLE = itemTag("grid_interactable");
    public static final TagKey<Item> MIXTURE_INGREDIENT = itemTag("mixture_ingredient"); //Specifies unit amounts for items, additive
    public static final TagKey<Item> MIXTURE_1 = itemTag("mixture_ingredient/units_1");
    public static final TagKey<Item> MIXTURE_2 = itemTag("mixture_ingredient/units_2");
    public static final TagKey<Item> MIXTURE_3 = itemTag("mixture_ingredient/units_3");
    public static final TagKey<Item> MIXTURE_4 = itemTag("mixture_ingredient/units_4");
    public static final TagKey<Item> MIXTURE_5 = itemTag("mixture_ingredient/units_5");
    public static final TagKey<Item> MIXTURE_10 = itemTag("mixture_ingredient/units_10");
    public static final TagKey<Item> MIXTURE_20 = itemTag("mixture_ingredient/units_20");
    public static final TagKey<Item> MIXTURE_30 = itemTag("mixture_ingredient/units_30");
    public static final TagKey<Item> MIXTURE_40 = itemTag("mixture_ingredient/units_40");
    public static final TagKey<Item> MIXTURE_50 = itemTag("mixture_ingredient/units_50");
    public static final TagKey<Item> MIXTURE_100 = itemTag("mixture_ingredient/units_100");
    public static final TagKey<Item> SMELT_TIER_CUSTOM = itemTag("smelt_tier"); //Minimum fire tier required to smelt this item in recipes (defaults to 1)
    public static final TagKey<Item> SMELT_TIER_0 = itemTag("smelt_tier/tier_0");
    public static final TagKey<Item> SMELT_TIER_2 = itemTag("smelt_tier/tier_2");
    public static final TagKey<Item> SMELT_TIER_3 = itemTag("smelt_tier/tier_3");
    public static final TagKey<Item> SMELT_TIER_4 = itemTag("smelt_tier/tier_4");
    public static final TagKey<Item> SMELT_TIER_5 = itemTag("smelt_tier/tier_5");
    public static final TagKey<Item> FACE_OFFSET_NONE = itemTag("client/face_offset_none"); //Adjusts appearance of accessories when tagged item is in equipment slot
    public static final TagKey<Item> FACE_OFFSET_EXTRA = itemTag("client/face_offset_extra");
    public static final TagKey<Item> NECK_OFFSET_NONE = itemTag("client/neck_offset_none");
    public static final TagKey<Item> NECK_OFFSET_EXTRA = itemTag("client/neck_offset_extra");
    public static final TagKey<Item> WAIST_OFFSET_NONE = itemTag("client/waist_offset_none");
    public static final TagKey<Item> WAIST_OFFSET_EXTRA = itemTag("client/waist_offset_extra");
    public static final TagKey<Item> FLINT_FIRE_STARTER_WEAK = itemTag("flint_fire_starter_weak");
    public static final TagKey<Item> FLINT_FIRE_STARTER_STRONG = itemTag("flint_fire_starter_strong");
    public static final TagKey<Item> STICK_FIRE_STARTER = itemTag("stick_fire_starter");
    public static final TagKey<Item> RACK_ITEM = itemTag("rack_item");
    public static final TagKey<Item> ROCK = itemTag("rock");
    public static final TagKey<Item> SAND_ITEM = itemTag("sand_item");
    public static final TagKey<Item> SOIL_ITEM = itemTag("soil_item");
    public static final TagKey<Item> FLUID_ITEM = itemTag("fluid_item");
    public static final TagKey<Item> COBBLE_MORTAR = itemTag("cobble_mortar");
    public static final TagKey<Item> PLANK = itemTag("plank");
    public static final TagKey<Item> PLANK_SOFT = itemTag("plank_soft");
    public static final TagKey<Item> PLANK_FAIR = itemTag("plank_fair");
    public static final TagKey<Item> PLANK_HARD = itemTag("plank_hard");
    public static final TagKey<Item> PLANK_BOW = itemTag("plank_bow");
    public static final TagKey<Item> MONSTER_HIDE = itemTag("monster_hide");
    public static final TagKey<Item> ANIMAL_HIDE = itemTag("animal_hide");
    public static final TagKey<Item> ANIMAL_HIDE_SMALL = itemTag("animal_hide_small");
    public static final TagKey<Item> ANIMAL_HIDE_MEDIUM = itemTag("animal_hide_medium");
    public static final TagKey<Item> ANIMAL_HIDE_LARGE = itemTag("animal_hide_large");
    public static final TagKey<Item> RAWHIDE_SMALL = itemTag("rawhide_small");
    public static final TagKey<Item> RAWHIDE_MEDIUM = itemTag("rawhide_medium");
    public static final TagKey<Item> RAWHIDE_LARGE = itemTag("rawhide_large");
    public static final TagKey<Item> CRUSHABLE_TO_LIME = itemTag("crushable_to_lime");
    public static final TagKey<Item> CRUSHABLE_TO_BONE_SHARD = itemTag("crushable_to_bone_shard");
    public static final TagKey<Item> FLETCHING = itemTag("fletching");
    public static final TagKey<Item> WARMING_ITEM = itemTag("warming_item");
    public static final TagKey<Item> CAULDRON_FLUID_MEAL = itemTag("cauldron_fluid_meal");
    public static final TagKey<Item> COOKED_MEAT = itemTag("cooked_meat");
    public static final TagKey<Item> CURED_MEAT = itemTag("cured_meat");
    public static final TagKey<Item> CURABLE_FOOD = itemTag("curable_food");
    public static final TagKey<Item> COOKED_VEGETABLE = itemTag("cooked_vegetable");
    public static final TagKey<Item> ARMOR_STAND = itemTag("armor_stand");
    public static final TagKey<Item> WIRES = itemTag("wires");
    public static final TagKey<Item> PLATES = itemTag("plates");
    public static final TagKey<Item> CHAINMAIL = itemTag("chainmail");
    public static final TagKey<Item> SCALES = itemTag("scales");
    public static final TagKey<Item> BILLETS = itemTag("billets");
    public static final TagKey<Item> CRUCIBLE_METAL = itemTag("crucible_metal");
    public static final TagKey<Item> METAL_CHUNKS = itemTag("metal_chunks");
    public static final TagKey<Item> NATIVE_METAL = itemTag("native_metal");
    public static final TagKey<Item> NATIVE_METAL_INGOT = itemTag("native_metal_ingot");
    public static final TagKey<Item> IRON_ORE = itemTag("iron_ore");
    public static final TagKey<Item> CORROSION_RESISTANT_METAL = itemTag("corrosion_resistant_metal");
    public static final TagKey<Item> HEAT_RESISTANT_MATERIAL_1 = itemTag("heat_resistant_material_1");
    public static final TagKey<Item> HEAT_RESISTANT_MATERIAL_2 = itemTag("heat_resistant_material_2");
    public static final TagKey<Item> HEAT_RESISTANT_MATERIAL_3 = itemTag("heat_resistant_material_3");
    public static final TagKey<Item> LINEN_OR_ARMOR = itemTag("linen_or_armor");
    public static final TagKey<Item> LEATHER_OR_ARMOR = itemTag("leather_or_armor");
    public static final TagKey<Item> MACE_PUZZLE_ITEM = itemTag("mace_puzzle_item");
    public static final TagKey<Item> WOODEN_SHIELD = itemTag("wooden_shield");
    public static final TagKey<Item> SURVIVOR_PLATE = itemTag("no_recipe_grouping/survivor_plate");
    public static final TagKey<Item> SURVIVOR_SCALE = itemTag("no_recipe_grouping/survivor_scale");
    public static final TagKey<Item> SURVIVOR_CHAINMAIL = itemTag("no_recipe_grouping/survivor_chainmail");
    public static final TagKey<Item> EXPLORER_PLATE = itemTag("no_recipe_grouping/explorer_plate");
    public static final TagKey<Item> EXPLORER_SCALE = itemTag("no_recipe_grouping/explorer_scale");
    public static final TagKey<Item> EXPLORER_CHAINMAIL = itemTag("no_recipe_grouping/explorer_chainmail");
    public static final TagKey<Item> SLAYER_PLATE = itemTag("no_recipe_grouping/slayer_plate");
    public static final TagKey<Item> SLAYER_SCALE = itemTag("no_recipe_grouping/slayer_scale");
    public static final TagKey<Item> SLAYER_CHAINMAIL = itemTag("no_recipe_grouping/slayer_chainmail");
    public static final Map<EquipmentSlot, TagKey<Item>> SURVIVOR_PLATE_PIECES = DataUtil.mapEnum(EquipmentSlot.class, (slot) -> slot.getType() == EquipmentSlot.Type.HAND,
            (slot) -> itemTag("survivor_plate_" + DataUtil.getName(slot)));
    public static final Map<EquipmentSlot, TagKey<Item>> SURVIVOR_SCALE_PIECES = DataUtil.mapEnum(EquipmentSlot.class, (slot) -> slot.getType() == EquipmentSlot.Type.HAND,
            (slot) -> itemTag("survivor_scale_" + DataUtil.getName(slot)));
    public static final Map<EquipmentSlot, TagKey<Item>> SURVIVOR_CHAINMAIL_PIECES = DataUtil.mapEnum(EquipmentSlot.class, (slot) -> slot.getType() == EquipmentSlot.Type.HAND,
            (slot) -> itemTag("survivor_chainmail_" + DataUtil.getName(slot)));
    public static final Map<EquipmentSlot, TagKey<Item>> EXPLORER_PLATE_PIECES = DataUtil.mapEnum(EquipmentSlot.class, (slot) -> slot.getType() == EquipmentSlot.Type.HAND,
            (slot) -> itemTag("explorer_plate_" + DataUtil.getName(slot)));
    public static final Map<EquipmentSlot, TagKey<Item>> EXPLORER_SCALE_PIECES = DataUtil.mapEnum(EquipmentSlot.class, (slot) -> slot.getType() == EquipmentSlot.Type.HAND,
            (slot) -> itemTag("explorer_scale_" + DataUtil.getName(slot)));
    public static final Map<EquipmentSlot, TagKey<Item>> EXPLORER_CHAINMAIL_PIECES = DataUtil.mapEnum(EquipmentSlot.class, (slot) -> slot.getType() == EquipmentSlot.Type.HAND,
            (slot) -> itemTag("explorer_chainmail_" + DataUtil.getName(slot)));
    public static final Map<EquipmentSlot, TagKey<Item>> SLAYER_PLATE_PIECES = DataUtil.mapEnum(EquipmentSlot.class, (slot) -> slot.getType() == EquipmentSlot.Type.HAND,
            (slot) -> itemTag("slayer_plate_" + DataUtil.getName(slot)));
    public static final Map<EquipmentSlot, TagKey<Item>> SLAYER_SCALE_PIECES = DataUtil.mapEnum(EquipmentSlot.class, (slot) -> slot.getType() == EquipmentSlot.Type.HAND,
            (slot) -> itemTag("slayer_scale_" + DataUtil.getName(slot)));
    public static final Map<EquipmentSlot, TagKey<Item>> SLAYER_CHAINMAIL_PIECES = DataUtil.mapEnum(EquipmentSlot.class, (slot) -> slot.getType() == EquipmentSlot.Type.HAND,
            (slot) -> itemTag("slayer_chainmail_" + DataUtil.getName(slot)));

    //An item should only belong to one group
    public static final TagKey<Item> MEAT = itemTag("food_group/meat");
    public static final TagKey<Item> VEGETABLE = itemTag("food_group/vegetable");
    public static final TagKey<Item> FRUIT = itemTag("food_group/fruit");
    public static final TagKey<Item> GRAIN = itemTag("food_group/grain");
    public static final TagKey<Item> HERB = itemTag("food_group/herb");
    public static final TagKey<Item> EGG = itemTag("food_group/egg");
    public static final TagKey<Item> PORK = itemTag("food_group/pork");
    public static final TagKey<Item> FRUIT_OR_VEGETABLE = itemTag("food_group/fruit_or_vegetable");
    public static final TagKey<Item> FOOD_INGREDIENT = itemTag("food_group/ingredient"); //Controls item insertion in cooking containers
    public static final TagKey<Item> HERBIVORE_FOOD = itemTag("herbivore_food");
    public static final TagKey<Item> CARNIVORE_FOOD = itemTag("carnivore_food");
    public static final TagKey<Item> OMNIVORE_FOOD = itemTag("omnivore_food");
    public static final TagKey<Item> OMNIVORE_SEEDS_FOOD = itemTag("omnivore_seeds_food");
    //Block tag copies
    public static final TagKey<Item> HEAT_RESISTANT_ITEM_1 = itemTag(HEAT_RESISTANT_1);
    public static final TagKey<Item> HEAT_RESISTANT_ITEM_2 = itemTag(HEAT_RESISTANT_2);
    public static final TagKey<Item> HEAT_RESISTANT_ITEM_3 = itemTag(HEAT_RESISTANT_3);
    public static final TagKey<Item> HEAT_RESISTANT_ITEM_4 = itemTag(HEAT_RESISTANT_4);
    public static final TagKey<Item> WOODEN_HATCHES_ITEM = itemTag(WOODEN_HATCHES);
    public static final TagKey<Item> WOODEN_LADDERS_ITEM = itemTag(WOODEN_LADDERS);
    public static final TagKey<Item> WOODEN_CHESTS_ITEM = itemTag(WOODEN_CHESTS);
    public static final TagKey<Item> WOODEN_RACKS_ITEM = itemTag(WOODEN_RACKS);
    public static final TagKey<Item> WOODEN_SHELVES_ITEM = itemTag(WOODEN_SHELVES);
    public static final TagKey<Item> WOODEN_BARRELS_ITEM = itemTag(WOODEN_BARRELS);
    public static final TagKey<Item> WOODEN_FENCE_GATES_ITEM = itemTag(WOODEN_FENCE_GATES);
    public static final TagKey<Item> CHAIRS_ITEM = itemTag(CHAIRS);
    public static final TagKey<Item> TROUGHS_ITEM = itemTag(TROUGHS);
    public static final TagKey<Item> ITEM_FRAMES_ITEM = itemTag(ITEM_FRAMES);
    public static final TagKey<Item> ANVILS_ITEM = itemTag(ANVILS);
    public static final TagKey<Item> METAL_ANVILS_ITEM = itemTag(METAL_ANVILS);
    public static final TagKey<Item> METAL_BLOCKS_ITEM = itemTag(METAL_BLOCKS);

    public static final List<TagKey<Item>> FOOD_GROUPS = List.of(TagsNF.EGG, TagsNF.PORK, TagsNF.MEAT, TagsNF.VEGETABLE, TagsNF.FRUIT, TagsNF.GRAIN, TagsNF.HERB, TagsNF.FRUIT_OR_VEGETABLE);

    public static final TagKey<SpawnGroup> SURFACE_GROUPS = spawnGroupTag("surface_groups");
    public static final TagKey<SpawnGroup> FRESHWATER_GROUPS = spawnGroupTag("freshwater_groups");
    public static final TagKey<SpawnGroup> OCEAN_GROUPS = spawnGroupTag("ocean_groups");
    public static final TagKey<SpawnGroup> RANDOM_GROUPS = spawnGroupTag("random_groups");

    static {
        ITEM_TAGS.trim();
    }

    private static TagKey<Action> actionTag(String name) {
        return TagKey.create(RegistriesNF.ACTIONS_KEY, ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, name));
    }

    private static TagKey<Biome> biomeTag(String name) {
        return TagKey.create(Registry.BIOME_REGISTRY, ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, name));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registry.BLOCK_REGISTRY, ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, name));
    }

    private static TagKey<EntityType<?>> entityTag(String name) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, name));
    }

    private static TagKey<Fluid> fluidTag(String name) {
        return TagKey.create(Registry.FLUID_REGISTRY, ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, name));
    }

    private static TagKey<Item> itemTag(String name) {
        TagKey<Item> key = TagKey.create(Registry.ITEM_REGISTRY, ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, name));
        ITEM_TAGS.add(key);
        return key;
    }

    private static TagKey<Item> itemTag(TagKey<Block> blockTag) {
        TagKey<Item> key = TagKey.create(Registry.ITEM_REGISTRY, ResourceLocation.fromNamespaceAndPath(blockTag.location().getNamespace(), blockTag.location().getPath()));
        ITEM_TAGS.add(key);
        return key;
    }

    private static TagKey<SpawnGroup> spawnGroupTag(String name) {
        return TagKey.create(RegistriesNF.SPAWN_GROUPS_KEY, ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, name));
    }
}
