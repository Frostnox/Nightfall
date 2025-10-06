package frostnox.nightfall.registry.forge;

import com.google.common.collect.ImmutableMultimap;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.player.PlayerActionSet;
import frostnox.nightfall.block.*;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.entity.animal.DeerEntity;
import frostnox.nightfall.entity.entity.animal.RabbitEntity;
import frostnox.nightfall.entity.entity.monster.CockatriceEntity;
import frostnox.nightfall.item.*;
import frostnox.nightfall.item.item.*;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.DataUtil;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemsNF {
    //Overwrite vanilla tab IDs to effectively replace them
    //This is generally bad practice because it could conflict with other mods, but it's fine for a total conversion
    public static final CreativeModeTab NATURAL_TAB = new CreativeModeTab(0, Nightfall.MODID + ".natural_blocks") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemsNF.COVERED_DIRT.get(SoilCover.GRASS).get());
        }
    };
    public static final CreativeModeTab FUNCTIONAL_TAB = new CreativeModeTab(1, Nightfall.MODID + ".functional_blocks") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemsNF.POT.get());
        }
    };
    public static final CreativeModeTab BUILDING_TAB = new CreativeModeTab(2, Nightfall.MODID + ".building_materials") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemsNF.PLANKS.get(Tree.OAK).get());
        }
    };
    public static final CreativeModeTab INGREDIENTS_TAB = new CreativeModeTab(3, Nightfall.MODID + ".ingredients") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemsNF.FLINT.get());
        }
    };
    public static final CreativeModeTab FOOD_TAB = new CreativeModeTab(6, Nightfall.MODID + ".food") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemsNF.MEAT_STEW.get());
        }
    };
    public static final CreativeModeTab ARMAMENTS_TAB = new CreativeModeTab(7, Nightfall.MODID + ".armaments") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.STEEL).get(Armament.SWORD).get());
        }
    };
    public static final CreativeModeTab ARMOR_TAB = new CreativeModeTab(8, Nightfall.MODID + ".armor") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.STEEL_SCALE_SURVIVOR).get());
        }
    };
    public static final CreativeModeTab UTILITIES_TAB = new CreativeModeTab(9, Nightfall.MODID + ".utilities") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemsNF.POUCH.get());
        }
    };
    public static final CreativeModeTab CONSUMABLES_TAB = new CreativeModeTab(10, Nightfall.MODID + ".consumables") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemsNF.BANDAGE.get());
        }
    };
    static {
        CreativeModeTab.TAB_HOTBAR = new CreativeModeTab(4, "hotbar") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Blocks.BOOKSHELF); //TODO: Replace with something fitting later
            }

            @Override
            public void fillItemList(NonNullList<ItemStack> p_40820_) {
                throw new RuntimeException("Implement exception client-side.");
            }

            @Override
            public boolean isAlignedRight() {
                return true;
            }
        };
        CreativeModeTab.TAB_SEARCH = (new CreativeModeTab(5, "search") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(Items.COMPASS); //TODO: Replace with something fitting later
            }
        }).setBackgroundSuffix("item_search.png");
        CreativeModeTab.TAB_INVENTORY = (new CreativeModeTab(11, "inventory") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(CHESTS.get(Tree.OAK).get());
            }
        }).setBackgroundSuffix("inventory.png").hideScroll().hideTitle();
    }

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Nightfall.MODID);
    private static final Random RANDOM = new Random(94814035);
    private static int ARROW_ID = 0;
    //Resources
    public static final RegistryObject<FireStarterItem> FLINT = ITEMS.register("flint", () -> new FireStarterItem(
            Map.of(TagsNF.FLINT_FIRE_STARTER_WEAK, 10, TagsNF.FLINT_FIRE_STARTER_STRONG, 0),
            () -> SoundEvents.FLINTANDSTEEL_USE, 1F/32F, ingredient()));
    public static final RegistryObject<Item> BONE_SHARD = register("bone_shard", INGREDIENTS_TAB);
    public static final RegistryObject<Item> OBSIDIAN_SHARD = ITEMS.register("obsidian_shard", () -> new Item(ingredient()));
    public static final RegistryObject<Item> ANCHORING_RESIN = ITEMS.register("anchoring_resin", () -> new Item(ingredient()));
    public static final RegistryObject<FireStarterItem> STICK = ITEMS.register("stick", () -> new FireStarterItem(Map.of(TagsNF.STICK_FIRE_STARTER, 20),
            SoundsNF.STICK_FIRE_STRIKE, 1F/16F, ingredient()));
    public static final RegistryObject<BuildingMaterialItem> PLANT_FIBERS = ITEMS.register("plant_fibers", () -> new BuildingMaterialItem(building()));
    public static final RegistryObject<Item> RAWHIDE = ITEMS.register("rawhide", () -> new Item(ingredient()));
    public static final RegistryObject<Item> LEATHER = ITEMS.register("leather", () -> new Item(ingredient()));
    public static final RegistryObject<Item> FLAX_FIBERS = ITEMS.register("flax_fibers", () -> new Item(ingredient()));
    public static final RegistryObject<Item> LINEN = ITEMS.register("linen", () -> new Item(ingredient()));
    public static final RegistryObject<Item> YARROW = ITEMS.register("yarrow", () -> new Item(ingredient()));
    public static final RegistryObject<Item> YARROW_POWDER = ITEMS.register("yarrow_powder", () -> new Item(ingredient()));
    public static final RegistryObject<Item> SULFUR = register("sulfur", () -> new SulfurItem(ingredient()));
    public static final RegistryObject<Item> LIME = register("lime", () -> new LimeItem(ingredient()));
    public static final RegistryObject<Item> SALT = ITEMS.register("salt", () -> new SaltItem(ingredient()));
    public static final RegistryObject<Item> TIN_CHUNK = ITEMS.register("tin_chunk", () -> new Item(ingredient().stacksTo(16)));
    public static final RegistryObject<Item> COPPER_CHUNK = ITEMS.register("copper_chunk", () -> new Item(ingredient().stacksTo(16)));
    public static final RegistryObject<BuildingMaterialItem> AZURITE_CHUNK = ITEMS.register("azurite_chunk", () -> new BuildingMaterialItem(ingredient().stacksTo(16)));
    public static final RegistryObject<BuildingMaterialItem> HEMATITE_CHUNK = ITEMS.register("hematite_chunk", () -> new BuildingMaterialItem(ingredient().stacksTo(16)));
    public static final RegistryObject<Item> METEORITE_CHUNK = ITEMS.register("meteorite_chunk", () -> new Item(ingredient().stacksTo(16)));
    public static final RegistryObject<Item> TIN_NUGGET = ITEMS.register("tin_nugget", () -> new Item(ingredient()));
    public static final RegistryObject<Item> COPPER_NUGGET = ITEMS.register("copper_nugget", () -> new Item(ingredient()));
    public static final RegistryObject<Item> AZURITE_NUGGET = ITEMS.register("azurite_nugget", () -> new Item(ingredient()));
    public static final RegistryObject<Item> HEMATITE_NUGGET = ITEMS.register("hematite_nugget", () -> new Item(ingredient()));
    public static final RegistryObject<Item> METEORITE_NUGGET = ITEMS.register("meteorite_nugget", () -> new Item(ingredient()));
    public static final RegistryObject<Item> STEEL_NUGGET = ITEMS.register("steel_nugget", () -> new Item(ingredient()));
    public static final RegistryObject<FuelItem> COKE = register("coke", () -> new FuelItem(20 * 60 * 9 / 4, 1200F, ingredient()));
    public static final RegistryObject<FuelItem> COAL = ITEMS.register("coal", () -> new FuelItem(20 * 60 * 10 / 4, 1000F, ingredient()));
    public static final RegistryObject<FuelItem> CHARCOAL = register("charcoal", () -> new FuelItem(20 * 60 * 10 / 4, 1000F, ingredient()));
    public static final RegistryObject<Item> WATER = ITEMS.register("water", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SEAWATER = ITEMS.register("seawater", () -> new Item(new Item.Properties()));
    //Entity
    public static final RegistryObject<Item> ROTTEN_FLESH = register("rotten_flesh", INGREDIENTS_TAB);
    public static final RegistryObject<Item> LIVING_BONE = register("living_bone", INGREDIENTS_TAB);
    public static final RegistryObject<Item> DREG_HEART = register("dreg_heart", INGREDIENTS_TAB);
    public static final RegistryObject<Item> COCKATRICE_FEATHER = register("cockatrice_feather", INGREDIENTS_TAB);
    public static final Map<RabbitEntity.Type, RegistryObject<Item>> RABBIT_PELTS = DataUtil.mapEnum(RabbitEntity.Type.class, type ->
            register(type.name().toLowerCase(Locale.ROOT) + "_rabbit_pelt", () -> new Item(ingredient().stacksTo(32))));
    public static final Map<DeerEntity.Type, RegistryObject<Item>> DEER_HIDES = DataUtil.mapEnum(DeerEntity.Type.class, type ->
            register(type.name().toLowerCase(Locale.ROOT) + "_deer_hide", () -> new Item(ingredient().stacksTo(16))));
    public static final Map<CockatriceEntity.Type, RegistryObject<Item>> COCKATRICE_SKINS = DataUtil.mapEnum(CockatriceEntity.Type.class, type ->
            register(type.name().toLowerCase(Locale.ROOT) + "_cockatrice_skin", () -> new Item(ingredient().stacksTo(16))));
    public static final RegistryObject<MeleeWeaponItem> RUSTED_MAUL = register("rusted_maul", () -> new MeleeWeaponItem(TieredItemMaterial.METEORITE,
            PlayerActionSet.MAUL, HurtSphere.MAUL, HurtSphere.WEAPONS_TP.get(HurtSphere.MAUL), true, armament(), List.of(), DamageType.STRIKING, DamageType.SLASHING));
    public static final RegistryObject<MeleeWeaponItem> RUSTED_SPEAR = ITEMS.register("rusted_spear", () -> new MeleeWeaponItem(TieredItemMaterial.METEORITE,
            PlayerActionSet.SPEAR, HurtSphere.SPEAR, HurtSphere.WEAPONS_TP.get(HurtSphere.SPEAR), false, armament(), Armament.SPEAR.getToolActions(), DamageType.PIERCING));
    public static final RegistryObject<BowItemNF> TWISTED_BOW = register("twisted_bow", () -> new BowItemNF(
            ActionsNF.TWISTED_BOW_SHOOT, ItemTags.ARROWS, armament().durability(240)));
    public static final RegistryObject<Item> SILK = ITEMS.register("silk", () -> new Item(ingredient()));
    public static final RegistryObject<Item> ROCKY_SCALE = ITEMS.register("rocky_scale", () -> new Item(ingredient()));
    public static final RegistryObject<Item> RAW_JELLYFISH = ITEMS.register("raw_jellyfish", () -> new Item(ingredient().stacksTo(4)));
    public static final RegistryObject<Item> RAW_PALE_FLESH = ITEMS.register("raw_pale_flesh", () -> new Item(ingredient().stacksTo(4)));
    public static final RegistryObject<Item> PIT_DEVIL_TOOTH = ITEMS.register("pit_devil_tooth", () -> new Item(ingredient()));
    public static final RegistryObject<Item> BONE = ITEMS.register("bone", () -> new Item(ingredient()));
    public static final RegistryObject<Item> ECTOPLASM = ITEMS.register("ectoplasm", () -> new Item(ingredient()));
    public static final RegistryObject<Item> SKARA_SHELLS = ITEMS.register("skara_shells", () -> new Item(ingredient()));
    public static final RegistryObject<Item> DRAKEFOWL_EGG = ITEMS.register("drakefowl_egg", () -> new Item(ingredient().stacksTo(4)));
    //Food
    public static final RegistryObject<Item> POTATO = ITEMS.register("potato", () -> new Item(new Item.Properties().food(
            food(2, 0F)).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> CARROT = ITEMS.register("carrot", () -> new Item(new Item.Properties().food(
            food(2, 0F)).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> ROASTED_POTATO = ITEMS.register("roasted_potato", () -> new Item(new Item.Properties().food(
            food(3, 0.2F)).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> ROASTED_CARROT = ITEMS.register("roasted_carrot", () -> new Item(new Item.Properties().food(
            food(3, 0.2F)).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> RAW_GAME = ITEMS.register("raw_game", () -> new FoodItem(48, new Item.Properties().food(
            meat(3, 0.2F)).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> RAW_VENISON = ITEMS.register("raw_venison", () -> new FoodItem(48, new Item.Properties().food(
            meat(4, 0.25F)).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> RAW_POULTRY = ITEMS.register("raw_poultry", () -> new FoodItem(48, new Item.Properties().food(
            meat(4, 0.25F)).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> COOKED_GAME = ITEMS.register("cooked_game", () -> new Item(new Item.Properties().food(
            meat(4, 0.3F)).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> COOKED_VENISON = ITEMS.register("cooked_venison", () -> new Item(new Item.Properties().food(
            meat(5, 0.35F)).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> COOKED_POULTRY = ITEMS.register("cooked_poultry", () -> new Item(new Item.Properties().food(
            meat(5, 0.35F)).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> COOKED_PALE_FLESH = ITEMS.register("cooked_pale_flesh", () -> new FoodItem(48, new Item.Properties().food(
            meat(3, 0.2F)).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> CURED_GAME = ITEMS.register("cured_game", () -> new Item(new Item.Properties().food(
            meat(4, 0.3F)).tab(FOOD_TAB).stacksTo(8)));
    public static final RegistryObject<Item> CURED_VENISON = ITEMS.register("cured_venison", () -> new Item(new Item.Properties().food(
            meat(5, 0.35F)).tab(FOOD_TAB).stacksTo(8)));
    public static final RegistryObject<Item> CURED_POULTRY = ITEMS.register("cured_poultry", () -> new Item(new Item.Properties().food(
            meat(5, 0.35F)).tab(FOOD_TAB).stacksTo(8)));
    public static final RegistryObject<Item> CURED_JELLYFISH = ITEMS.register("cured_jellyfish", () -> new FoodItem(48, new Item.Properties().food(
            meat(1, 0F)).tab(FOOD_TAB).stacksTo(8)));
    public static final RegistryObject<Item> CURED_PALE_FLESH = ITEMS.register("cured_pale_flesh", () -> new FoodItem(48, new Item.Properties().food(
            meat(3, 0.2F)).tab(FOOD_TAB).stacksTo(8)));
    public static final RegistryObject<Item> BERRIES = ITEMS.register("berries", () -> new Item(new Item.Properties().food(
            foodBuilder(1, 0F).build()).tab(FOOD_TAB).stacksTo(8)));
    public static final RegistryObject<Item> APPLE = ITEMS.register("apple", () -> new Item(new Item.Properties().food(
            foodBuilder(4, 0.1F).build()).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> COCONUT = ITEMS.register("coconut", () -> new Item(new Item.Properties().stacksTo(4).tab(INGREDIENTS_TAB)));
    public static final RegistryObject<Item> COCONUT_HALF = ITEMS.register("coconut_half", () -> new Item(new Item.Properties().food(
            foodBuilder(2, 0.25F).build()).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> COCOA_POD = ITEMS.register("cocoa_pod", () -> new ChangeOnUseFinishItem(ItemsNF.COCOA_BEANS,
            new Item.Properties().food(foodBuilder(2, 0F).build()).tab(FOOD_TAB).stacksTo(4)));
    public static final RegistryObject<Item> COCOA_BEANS = register("cocoa_beans", INGREDIENTS_TAB);
    public static final RegistryObject<Item> MEAT_STEW = ITEMS.register("meat_stew", () -> new ChangeOnUseFinishItem(ItemsNF.WOODEN_BOWL,
            new Item.Properties().food(food(7, 0.4F)).tab(FOOD_TAB).stacksTo(1)));
    public static final RegistryObject<Item> VEGETABLE_STEW = ITEMS.register("vegetable_stew", () -> new ChangeOnUseFinishItem(ItemsNF.WOODEN_BOWL,
            new Item.Properties().food(food(5, 0.35F)).tab(FOOD_TAB).stacksTo(1)));
    public static final RegistryObject<Item> HEARTY_STEW = ITEMS.register("hearty_stew", () -> new ChangeOnUseFinishItem(ItemsNF.WOODEN_BOWL,
            new Item.Properties().food(food(6, 0.4F)).tab(FOOD_TAB).stacksTo(1)));
    public static final RegistryObject<Item> SUSPICIOUS_STEW = ITEMS.register("suspicious_stew", () -> new ChangeOnUseFinishItem(ItemsNF.WOODEN_BOWL,
            new Item.Properties().food(foodBuilder(3, 0.1F).effect(() -> new MobEffectInstance(EffectsNF.POISON.get(),
                    30 * 20, 0), 0.5F).build()).tab(FOOD_TAB).stacksTo(1)));
    public static final RegistryObject<Item> BURNT_FOOD = ITEMS.register("burnt_food", () -> new Item(new Item.Properties().food(
            food(1, 0F)).tab(FOOD_TAB).stacksTo(4)));
    //Seeds
    public static final RegistryObject<Item> POTATO_SEEDS = ITEMS.register("potato_seeds", () -> new ItemNameBlockItem(BlocksNF.POTATOES.get(), ingredient()));
    public static final RegistryObject<Item> CARROT_SEEDS = ITEMS.register("carrot_seeds", () -> new ItemNameBlockItem(BlocksNF.CARROTS.get(), ingredient()));
    public static final RegistryObject<Item> FLAX_SEEDS = ITEMS.register("flax_seeds", () -> new ItemNameBlockItem(BlocksNF.FLAX.get(), ingredient()));
    public static final RegistryObject<Item> YARROW_SEEDS = ITEMS.register("yarrow_seeds", () -> new ItemNameBlockItem(BlocksNF.YARROW.get(), ingredient()));
    //Building materials
    public static final Map<Soil, RegistryObject<BuildingMaterialItem>> SOILS = DataUtil.mapEnum(Soil.class, soil ->
            register(soil.getName(), () -> new BuildingMaterialItem(building())));
    public static final Map<Stone, RegistryObject<BuildingMaterialItem>> ROCKS = DataUtil.mapEnum(Stone.class, stone ->
            register(stone.getName() + "_rock", () -> new BuildingMaterialItem(building())));
    public static final Map<Stone, RegistryObject<BuildingMaterialItem>> STONE_BRICKS = DataUtil.mapEnum(Stone.class, stone ->
            register(stone.getName() + "_brick", () -> new BuildingMaterialItem(building())));
    public static final Map<Tree, RegistryObject<BuildingMaterialItem>> PLANKS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_plank", () -> new BuildingMaterialItem(building())));
    public static final RegistryObject<BuildingMaterialItem> MUD = register("mud", () -> new BuildingMaterialItem(building()));
    public static final RegistryObject<FuelItem> FIREWOOD = register("firewood", () -> new FuelItem(20 * 60 * 10 / 4, 800F, building()));
    public static final RegistryObject<BuildingMaterialItem> CLAY = register("clay", () -> new BuildingMaterialItem(building()));
    public static final RegistryObject<BuildingMaterialItem> FIRE_CLAY = register("fire_clay", () -> new BuildingMaterialItem(building()));
    public static final RegistryObject<BuildingMaterialItem> TERRACOTTA_SHARD = register("terracotta_shard", () -> new BuildingMaterialItem(building()));
    public static final RegistryObject<BuildingMaterialItem> MUD_BRICK = register("mud_brick", () -> new BuildingMaterialItem(building()));
    public static final RegistryObject<BuildingMaterialItem> BRICK = register("brick", () -> new BuildingMaterialItem(building()));
    public static final RegistryObject<BuildingMaterialItem> FIRE_BRICK = register("fire_brick", () -> new BuildingMaterialItem(building()));
    public static final RegistryObject<BuildingMaterialItem> GLASS = register("glass", () -> new BuildingMaterialItem(building()));
    public static final RegistryObject<BuildingMaterialItem> SNOWBALL = register("snowball", () -> new BuildingMaterialItem(building()));
    public static final RegistryObject<BuildingMaterialItem> SLAG = register("slag", () -> new BuildingMaterialItem(building().stacksTo(32)));
    //Utilities
    public static final Map<Tree, RegistryObject<ArmorStandDummyItem>> ARMOR_STANDS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_armor_stand", () -> new ArmorStandDummyItem(PLANKS.get(tree).getId(), new Item.Properties().stacksTo(1))));
    public static final Map<Tree, RegistryObject<BoatItemNF>> BOATS = DataUtil.mapEnum(Tree.class, tree -> tree.getHardness() > Tree.OAK.getHardness(), tree ->
            register(tree.getName() + "_boat", () -> new BoatItemNF(PLANKS.get(tree).getId(), new Item.Properties().stacksTo(1))));

    public static final RegistryObject<EmptyBucketItem> WOODEN_BUCKET = ITEMS.register("wooden_bucket", () -> new EmptyBucketItem(new Item.Properties().stacksTo(16).tab(UTILITIES_TAB)));
    public static final RegistryObject<FilledBucketItem> WOODEN_WATER_BUCKET = ITEMS.register("wooden_water_bucket", () -> new FilledBucketItem(false, FluidsNF.WATER, new Item.Properties().craftRemainder(WOODEN_BUCKET.get()).stacksTo(1).tab(UTILITIES_TAB)));
    public static final RegistryObject<FilledBucketItem> WOODEN_SEAWATER_BUCKET = ITEMS.register("wooden_seawater_bucket", () -> new FilledBucketItem(false, FluidsNF.SEAWATER, new Item.Properties().craftRemainder(WOODEN_BUCKET.get()).stacksTo(1).tab(UTILITIES_TAB)));
    public static final RegistryObject<EmptyBucketItem> BRONZE_BUCKET = ITEMS.register("bronze_bucket", () -> new EmptyBucketItem(new Item.Properties().stacksTo(16).tab(UTILITIES_TAB)));
    public static final RegistryObject<FilledBucketItem> BRONZE_WATER_BUCKET = ITEMS.register("bronze_water_bucket", () -> new FilledBucketItem(true, FluidsNF.WATER, new Item.Properties().craftRemainder(BRONZE_BUCKET.get()).stacksTo(1).tab(UTILITIES_TAB)));
    public static final RegistryObject<FilledBucketItem> BRONZE_SEAWATER_BUCKET = ITEMS.register("bronze_seawater_bucket", () -> new FilledBucketItem(true, FluidsNF.SEAWATER, new Item.Properties().craftRemainder(BRONZE_BUCKET.get()).stacksTo(1).tab(UTILITIES_TAB)));
    public static final RegistryObject<EmptyBucketItem> ALKIMIUM_BUCKET = ITEMS.register("alkimium_bucket", () -> new EmptyBucketItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<FilledBucketItem> ALKIMIUM_WATER_BUCKET = ITEMS.register("alkimium_water_bucket", () -> new FilledBucketItem(true, FluidsNF.WATER, new Item.Properties().craftRemainder(ALKIMIUM_BUCKET.get()).stacksTo(1)));
    public static final RegistryObject<FilledBucketItem> ALKIMIUM_SEAWATER_BUCKET = ITEMS.register("alkimium_seawater_bucket", () -> new FilledBucketItem(true, FluidsNF.SEAWATER, new Item.Properties().craftRemainder(ALKIMIUM_BUCKET.get()).stacksTo(1)));
    public static final RegistryObject<FilledBucketItem> ALKIMIUM_LAVA_BUCKET = ITEMS.register("alkimium_lava_bucket", () -> new FilledBucketItem(true, FluidsNF.LAVA, new Item.Properties().craftRemainder(ALKIMIUM_BUCKET.get()).stacksTo(1)));

    public static final RegistryObject<SnowballItem> SNOWBALL_THROWABLE = ITEMS.register("snowball_throwable", () -> new SnowballItem(new Item.Properties()));

    private static final int BEAST_COLOR = 0x866131, UNDEAD_COLOR = 0x88b5b8;
    public static final RegistryObject<SpawnEggItem> RABBIT_SPAWN_EGG = register(EntitiesNF.RABBIT.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.RABBIT, 0xd7bb91, BEAST_COLOR, utility()));
    public static final RegistryObject<SpawnEggItem> DEER_SPAWN_EGG = register(EntitiesNF.DEER.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.DEER, 0xd6cec1, BEAST_COLOR, utility()));
    public static final RegistryObject<SpawnEggItem> DRAKEFOWL_ROOSTER_SPAWN_EGG = register(EntitiesNF.DRAKEFOWL_ROOSTER.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.DRAKEFOWL_ROOSTER, 0x526c1f, 0xc25217, utility()));
    public static final RegistryObject<SpawnEggItem> DRAKEFOWL_HEN_SPAWN_EGG = register(EntitiesNF.DRAKEFOWL_HEN.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.DRAKEFOWL_HEN, 0x526c1f, 0xc25217, utility()));
    public static final RegistryObject<SpawnEggItem> DRAKEFOWL_CHICK_SPAWN_EGG = register(EntitiesNF.DRAKEFOWL_CHICK.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.DRAKEFOWL_CHICK, 0x526c1f, 0xc25217, utility()));
    public static final RegistryObject<SpawnEggItem> HUSK_SPAWN_EGG = register(EntitiesNF.HUSK.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.HUSK, 0x6d727e, UNDEAD_COLOR, utility()));
    public static final RegistryObject<SpawnEggItem> SKELETON_SPAWN_EGG = register(EntitiesNF.SKELETON.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.SKELETON, 0xb4bbca, UNDEAD_COLOR, utility()));
    public static final RegistryObject<SpawnEggItem> DREG_SPAWN_EGG = register(EntitiesNF.DREG.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.DREG, 0x8896bd, UNDEAD_COLOR, utility()));
    public static final RegistryObject<SpawnEggItem> CREEPER_SPAWN_EGG = register(EntitiesNF.CREEPER.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.CREEPER, 0x206a19, 0x63a731, utility()));
    public static final RegistryObject<SpawnEggItem> COCKATRICE_SPAWN_EGG = register(EntitiesNF.COCKATRICE.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.COCKATRICE, 0x526c1f, 0xc25217, utility()));
    public static final RegistryObject<SpawnEggItem> SPIDER_SPAWN_EGG = register(EntitiesNF.SPIDER.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.SPIDER, 0x95150d, 0x2c2622, utility()));
    public static final RegistryObject<SpawnEggItem> ROCKWORM_SPAWN_EGG = register(EntitiesNF.ROCKWORM.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.ROCKWORM, 0xcbc2b7, 0x4f5963, utility()));
    public static final RegistryObject<SpawnEggItem> PIT_DEVIL_SPAWN_EGG = register(EntitiesNF.PIT_DEVIL.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.PIT_DEVIL, 0x523936, 0x58626c, utility()));
    public static final RegistryObject<SpawnEggItem> ECTOPLASM_SPAWN_EGG = register("ectoplasm_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.ECTOPLASM_LARGE, 0xe7e0c6, 0xb9b9cd, utility()));
//    public static final RegistryObject<SpawnEggItem> SCORPION_SPAWN_EGG = register(EntitiesNF.SCORPION.getId().getPath() + "_spawn_egg",
//            () -> new ForgeSpawnEggItem(EntitiesNF.SCORPION, 0x95150d, 0x2c2622, utility()));
    public static final RegistryObject<SpawnEggItem> SKARA_SWARM_SPAWN_EGG = register(EntitiesNF.SKARA_SWARM.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.SKARA_SWARM, 0x9b5218, 0x1b2e4a, utility()));
//    public static final RegistryObject<SpawnEggItem> TROLL_SPAWN_EGG = register(EntitiesNF.TROLL.getId().getPath() + "_spawn_egg",
//            () -> new ForgeSpawnEggItem(EntitiesNF.TROLL, 0x95150d, 0x2c2622, utility()));
//    public static final RegistryObject<SpawnEggItem> OLMUR_SPAWN_EGG = register(EntitiesNF.OLMUR.getId().getPath() + "_spawn_egg",
//            () -> new ForgeSpawnEggItem(EntitiesNF.OLMUR, 0x95150d, 0x2c2622, utility()));
    public static final RegistryObject<SpawnEggItem> JELLYFISH_SPAWN_EGG = register(EntitiesNF.JELLYFISH.getId().getPath() + "_spawn_egg",
            () -> new ForgeSpawnEggItem(EntitiesNF.JELLYFISH, 0xec5c83, 0xf5b7cf, utility()));
    //Armaments
    public static final Map<TieredItemMaterial, Map<Armament, RegistryObject<MeleeWeaponItem>>> METAL_ARMAMENTS = DataUtil.mapEnum(TieredItemMaterial.class,
            material -> material.getMetal() == null, material -> DataUtil.mapEnum(Armament.class, armament -> {
                if(armament == Armament.CHISEL) {
                    return register(material.getName() + "_" + armament.getName(), () -> new PairedMeleeWeaponItem(material, armament.getActionSet(),
                            PlayerActionSet.HAMMER, PlayerActionSet.CHISEL_AND_HAMMER,
                            armament.getHurtSpheres(), HurtSphere.WEAPONS_TP.get(armament.getHurtSpheres()), armament.canDig(),
                            new Item.Properties().tab(ARMAMENTS_TAB), armament.getToolActions(), armament.getDefaultDamageType()));
                }
                else {
                    return register(material.getName() + "_" + armament.getName(), () -> new MeleeWeaponItem(material, armament.getActionSet(),
                            armament.getHurtSpheres(), HurtSphere.WEAPONS_TP.get(armament.getHurtSpheres()), armament.canDig(),
                            new Item.Properties().tab(ARMAMENTS_TAB), armament.getToolActions(), armament.getDefaultDamageType()));
                }
            }));
    //Equipment
    public static final Map<TieredArmorMaterial, RegistryObject<TieredArmorItem>> HELMETS = DataUtil.mapEnum(TieredArmorMaterial.class,
            material -> register(material.getName() + "_helmet", () -> new DyeableTieredArmorItem(material, EquipmentSlot.HEAD, new Item.Properties().tab(ARMOR_TAB))));
    public static final Map<TieredArmorMaterial, RegistryObject<TieredArmorItem>> CHESTPLATES = DataUtil.mapEnum(TieredArmorMaterial.class,
            material -> register(material.getName() + "_chestplate", () -> new DyeableTieredArmorItem(material, EquipmentSlot.CHEST, new Item.Properties().tab(ARMOR_TAB))));
    public static final Map<TieredArmorMaterial, RegistryObject<TieredArmorItem>> LEGGINGS = DataUtil.mapEnum(TieredArmorMaterial.class,
            material -> register(material.getName() + "_leggings", () -> new DyeableTieredArmorItem(material, EquipmentSlot.LEGS, new Item.Properties().tab(ARMOR_TAB))));
    public static final Map<TieredArmorMaterial, RegistryObject<TieredArmorItem>> BOOTS = DataUtil.mapEnum(TieredArmorMaterial.class,
            material -> register(material.getName() + "_boots", () -> new DyeableTieredArmorItem(material, EquipmentSlot.FEET, new Item.Properties().tab(ARMOR_TAB))));

    //Terrain blocks
    public static final Map<SoilCover, RegistryObject<BlockItemNF>> COVERED_SILT = DataUtil.mapEnum(SoilCover.class,
            cover -> register(BlocksNF.COVERED_SILT.get(cover), NATURAL_TAB));
    public static final Map<SoilCover, RegistryObject<BlockItemNF>> COVERED_DIRT = DataUtil.mapEnum(SoilCover.class,
            cover -> register(BlocksNF.COVERED_DIRT.get(cover), NATURAL_TAB));
    public static final Map<SoilCover, RegistryObject<BlockItemNF>> COVERED_LOAM = DataUtil.mapEnum(SoilCover.class,
            cover -> register(BlocksNF.COVERED_LOAM.get(cover), NATURAL_TAB));
    public static final RegistryObject<BlockItemNF> SILT = register(BlocksNF.SILT);
    public static final RegistryObject<BlockItemNF> DIRT = register(BlocksNF.DIRT);
    public static final RegistryObject<BlockItemNF> LOAM = register(BlocksNF.LOAM);
    public static final RegistryObject<BlockItemNF> ASH = register(BlocksNF.ASH);
    public static final RegistryObject<BlockItemNF> GRAVEL = register(BlocksNF.GRAVEL);
    public static final RegistryObject<BlockItemNF> BLUE_GRAVEL = register(BlocksNF.BLUE_GRAVEL);
    public static final RegistryObject<BlockItemNF> BLACK_GRAVEL = register(BlocksNF.BLACK_GRAVEL);
    public static final RegistryObject<BlockItemNF> SAND = register(BlocksNF.SAND);
    public static final RegistryObject<BlockItemNF> RED_SAND = register(BlocksNF.RED_SAND);
    public static final RegistryObject<BlockItemNF> WHITE_SAND = register(BlocksNF.WHITE_SAND);
    public static final Map<Soil, RegistryObject<BlockItemNF>> STRANGE_SOILS = DataUtil.mapEnum(Soil.class,
            soil -> register(BlocksNF.STRANGE_SOILS.get(soil), NATURAL_TAB));

    public static final RegistryObject<BlockItemNF> SNOW = register(BlocksNF.SNOW);
    public static final RegistryObject<BlockItemNF> PACKED_SNOW = register(BlocksNF.PACKED_SNOW);
    public static final RegistryObject<BlockItemNF> MUD_BLOCK = register(BlocksNF.MUD);
    public static final RegistryObject<BlockItemNF> CLAY_BLOCK = register(BlocksNF.CLAY);
    public static final RegistryObject<BlockItemNF> FIRE_CLAY_BLOCK = register(BlocksNF.FIRE_CLAY);
    public static final RegistryObject<BlockItemNF> ICE = register(BlocksNF.ICE, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> SEA_ICE = register(BlocksNF.SEA_ICE, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> SEASHELL = register(BlocksNF.SEASHELL, NATURAL_TAB);

    public static final RegistryObject<BlockItemNF> BEDROCK = register(BlocksNF.BEDROCK, NATURAL_TAB);
    //Plants
    public static final RegistryObject<BlockItemNF> SHORT_GRASS = register(BlocksNF.SHORT_GRASS, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> GRASS = register(BlocksNF.GRASS, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> TALL_GRASS = register(BlocksNF.TALL_GRASS, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> SMALL_FERN = register(BlocksNF.SMALL_FERN, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> FERN = register(BlocksNF.FERN, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> LARGE_FERN = register(BlocksNF.LARGE_FERN, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> VINES = register(BlocksNF.VINES, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> DEAD_BUSH = register(BlocksNF.DEAD_BUSH, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> DEAD_PLANT = register(BlocksNF.DEAD_PLANT, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> DEAD_CROP = register(BlocksNF.DEAD_CROP, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> BERRY_BUSH = register(BlocksNF.BERRY_BUSH, NATURAL_TAB);
    //Stone blocks
    public static final Map<Stone, RegistryObject<BlockItemNF>> STONE_BLOCKS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.STONE_BLOCKS.get(stone), NATURAL_TAB));
    public static final Map<Stone, RegistryObject<BlockItemNF>> ROCK_CLUSTERS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.ROCK_CLUSTERS.get(stone), NATURAL_TAB));
    public static final RegistryObject<BlockItemNF> FLINT_CLUSTER = register(BlocksNF.FLINT_CLUSTER, NATURAL_TAB);

    public static final Map<Stone, RegistryObject<BlockItemNF>> TIN_ORES = DataUtil.mapEnum(Stone.class, stone -> !BlocksNF.TIN_ORES.containsKey(stone),
            stone -> register(BlocksNF.TIN_ORES.get(stone), NATURAL_TAB));
    public static final Map<Stone, RegistryObject<BlockItemNF>> COPPER_ORES = DataUtil.mapEnum(Stone.class, stone -> !BlocksNF.COPPER_ORES.containsKey(stone),
            stone -> register(BlocksNF.COPPER_ORES.get(stone), NATURAL_TAB));
    public static final Map<Stone, RegistryObject<BlockItemNF>> AZURITE_ORES = DataUtil.mapEnum(Stone.class, stone -> !BlocksNF.AZURITE_ORES.containsKey(stone),
            stone -> register(BlocksNF.AZURITE_ORES.get(stone), NATURAL_TAB));
    public static final Map<Stone, RegistryObject<BlockItemNF>> HEMATITE_ORES = DataUtil.mapEnum(Stone.class, stone -> !BlocksNF.HEMATITE_ORES.containsKey(stone),
            stone -> register(BlocksNF.HEMATITE_ORES.get(stone), NATURAL_TAB));
    public static final Map<Stone, RegistryObject<BlockItemNF>> COAL_ORES = DataUtil.mapEnum(Stone.class, stone -> !BlocksNF.COAL_ORES.containsKey(stone),
            stone -> register(BlocksNF.COAL_ORES.get(stone), NATURAL_TAB));
    public static final Map<Stone, RegistryObject<BlockItemNF>> HALITE_ORES = DataUtil.mapEnum(Stone.class, stone -> !BlocksNF.HALITE_ORES.containsKey(stone),
            stone -> register(BlocksNF.HALITE_ORES.get(stone), NATURAL_TAB));
    public static final RegistryObject<BlockItemNF> METEORITE_ORE = register(BlocksNF.METEORITE_ORE, NATURAL_TAB);

    public static final RegistryObject<BlockItemNF> OBSIDIAN = register(BlocksNF.OBSIDIAN, NATURAL_TAB);
    //Tree blocks
    public static final Map<Tree, RegistryObject<LogItem>> LOGS = DataUtil.mapEnum(Tree.class, tree ->
            ITEMS.register(BlocksNF.LOGS.get(tree).getId().getPath(), () -> new LogItem(BlocksNF.LOGS.get(tree).get(), building().stacksTo(16))));
    public static final Map<Tree, RegistryObject<BlockItemNF>> STRIPPED_LOGS = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.STRIPPED_LOGS.get(tree), BUILDING_TAB));
    public static final Map<Tree, RegistryObject<BlockItemNF>> LEAVES = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.LEAVES.get(tree), NATURAL_TAB));
    public static final Map<Tree, RegistryObject<BlockItemNF>> FRUIT_LEAVES = DataUtil.mapEnum(Tree.class, tree -> !BlocksNF.FRUIT_LEAVES.containsKey(tree), tree ->
            register(BlocksNF.FRUIT_LEAVES.get(tree), NATURAL_TAB));
    public static final Map<Tree, RegistryObject<BlockItemNF>> BRANCHES = DataUtil.mapEnum(Tree.class, tree -> !BlocksNF.BRANCHES.containsKey(tree), tree ->
            register(BlocksNF.BRANCHES.get(tree), NATURAL_TAB));
    public static final Map<Tree, RegistryObject<BlockItemNF>> TREE_SEEDS = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.TREE_SEEDS.get(tree), INGREDIENTS_TAB));
    //Building
    public static final Map<Tree, RegistryObject<BlockItemNF>> PLANK_BLOCKS = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.PLANK_BLOCKS.get(tree)));
    public static final Map<Tree, RegistryObject<BlockItemNF>> PLANK_STAIRS = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.PLANK_STAIRS.get(tree)));
    public static final Map<Tree, RegistryObject<BlockItemNF>> PLANK_SLABS = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.PLANK_SLABS.get(tree)));
    public static final Map<Tree, RegistryObject<BlockItemNF>> PLANK_SIDINGS = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.PLANK_SIDINGS.get(tree)));
    public static final Map<Tree, RegistryObject<BlockItemNF>> PLANK_FENCES = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.PLANK_FENCES.get(tree)));
    public static final Map<Tree, RegistryObject<BlockItemNF>> PLANK_FENCE_GATES = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.PLANK_FENCE_GATES.get(tree)));
    public static final Map<Tree, RegistryObject<BlockItemNF>> PLANK_DOORS = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.PLANK_DOORS.get(tree)));
    public static final Map<Tree, RegistryObject<BlockItemNF>> PLANK_TRAPDOORS = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.PLANK_TRAPDOORS.get(tree)));
    public static final Map<Tree, RegistryObject<BlockItemNF>> PLANK_HATCHES = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.PLANK_HATCHES.get(tree)));
    public static final Map<Tree, RegistryObject<BlockItemNF>> PLANK_LADDERS = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.PLANK_LADDERS.get(tree)));
    public static final Map<Tree, RegistryObject<SignItem>> PLANK_SIGNS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_sign", () -> new SignItem(new Item.Properties().stacksTo(1),
                    BlocksNF.PLANK_STANDING_SIGNS.get(tree).get(), BlocksNF.PLANK_WALL_SIGNS.get(tree).get())));
    public static final Map<Tree, RegistryObject<BlockItemNF>> WOODEN_ITEM_FRAMES = DataUtil.mapEnum(Tree.class, tree ->
            register(BlocksNF.WOODEN_ITEM_FRAMES.get(tree)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> TILED_STONE = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.TILED_STONE.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> POLISHED_STONE = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.POLISHED_STONE.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> POLISHED_STONE_STAIRS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.POLISHED_STONE_STAIRS.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> POLISHED_STONE_SLABS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.POLISHED_STONE_SLABS.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> POLISHED_STONE_SIDINGS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.POLISHED_STONE_SIDINGS.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> STACKED_STONE = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.STACKED_STONE.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> STACKED_STONE_STAIRS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.STACKED_STONE_STAIRS.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> STACKED_STONE_SLABS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.STACKED_STONE_SLABS.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> STACKED_STONE_SIDINGS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.STACKED_STONE_SIDINGS.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> COBBLED_STONE = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.COBBLED_STONE.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> COBBLED_STONE_STAIRS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.COBBLED_STONE_STAIRS.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> COBBLED_STONE_SLABS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.COBBLED_STONE_SLABS.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> COBBLED_STONE_SIDINGS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.COBBLED_STONE_SIDINGS.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> STONE_BRICK_BLOCKS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.STONE_BRICK_BLOCKS.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> STONE_BRICK_STAIRS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.STONE_BRICK_STAIRS.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> STONE_BRICK_SLABS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.STONE_BRICK_SLABS.get(stone)));
    public static final Map<Stone, RegistryObject<BlockItemNF>> STONE_BRICK_SIDINGS = DataUtil.mapEnum(Stone.class, stone ->
            register(BlocksNF.STONE_BRICK_SIDINGS.get(stone)));
    public static final RegistryObject<BlockItemNF> TERRACOTTA = register(BlocksNF.TERRACOTTA, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> TERRACOTTA_TILES = register(BlocksNF.TERRACOTTA_TILES);
    public static final RegistryObject<BlockItemNF> TERRACOTTA_TILE_STAIRS = register(BlocksNF.TERRACOTTA_TILE_STAIRS);
    public static final RegistryObject<BlockItemNF> TERRACOTTA_TILE_SLAB = register(BlocksNF.TERRACOTTA_TILE_SLAB);
    public static final RegistryObject<BlockItemNF> TERRACOTTA_TILE_SIDING = register(BlocksNF.TERRACOTTA_TILE_SIDING);
    public static final RegistryObject<BlockItemNF> TERRACOTTA_MOSAIC = register(BlocksNF.TERRACOTTA_MOSAIC);
    public static final RegistryObject<BlockItemNF> TERRACOTTA_MOSAIC_STAIRS = register(BlocksNF.TERRACOTTA_MOSAIC_STAIRS);
    public static final RegistryObject<BlockItemNF> TERRACOTTA_MOSAIC_SLAB = register(BlocksNF.TERRACOTTA_MOSAIC_SLAB);
    public static final RegistryObject<BlockItemNF> TERRACOTTA_MOSAIC_SIDING = register(BlocksNF.TERRACOTTA_MOSAIC_SIDING);
    public static final RegistryObject<BlockItemNF> MUD_BRICKS = register(BlocksNF.MUD_BRICKS);
    public static final RegistryObject<BlockItemNF> MUD_BRICK_STAIRS = register(BlocksNF.MUD_BRICK_STAIRS);
    public static final RegistryObject<BlockItemNF> MUD_BRICK_SLAB = register(BlocksNF.MUD_BRICK_SLAB);
    public static final RegistryObject<BlockItemNF> MUD_BRICK_SIDING = register(BlocksNF.MUD_BRICK_SIDING);
    public static final RegistryObject<BlockItemNF> BRICKS = register(BlocksNF.BRICKS);
    public static final RegistryObject<BlockItemNF> BRICK_STAIRS = register(BlocksNF.BRICK_STAIRS);
    public static final RegistryObject<BlockItemNF> BRICK_SLAB = register(BlocksNF.BRICK_SLAB);
    public static final RegistryObject<BlockItemNF> BRICK_SIDING = register(BlocksNF.BRICK_SIDING);
    public static final RegistryObject<BlockItemNF> FIRE_BRICKS = register(BlocksNF.FIRE_BRICKS);
    public static final RegistryObject<BlockItemNF> FIRE_BRICK_STAIRS = register(BlocksNF.FIRE_BRICK_STAIRS);
    public static final RegistryObject<BlockItemNF> FIRE_BRICK_SLAB = register(BlocksNF.FIRE_BRICK_SLAB);
    public static final RegistryObject<BlockItemNF> FIRE_BRICK_SIDING = register(BlocksNF.FIRE_BRICK_SIDING);
    public static final RegistryObject<BlockItemNF> THATCH = register(BlocksNF.THATCH);
    public static final RegistryObject<BlockItemNF> THATCH_STAIRS = register(BlocksNF.THATCH_STAIRS);
    public static final RegistryObject<BlockItemNF> THATCH_SLAB = register(BlocksNF.THATCH_SLAB);
    public static final RegistryObject<BlockItemNF> THATCH_SIDING = register(BlocksNF.THATCH_SIDING);
    /*public static final RegistryObject<BlockItemNF> PLASTER = register(BlocksNF.PLASTER);
    public static final RegistryObject<BlockItemNF> PLASTER_STAIRS = register(BlocksNF.PLASTER_STAIRS);
    public static final RegistryObject<BlockItemNF> PLASTER_SLAB = register(BlocksNF.PLASTER_SLAB);
    public static final RegistryObject<BlockItemNF> PLASTER_SIDING = register(BlocksNF.PLASTER_SIDING);*/
    public static final RegistryObject<BlockItemNF> GLASS_BLOCK = register(BlocksNF.GLASS_BLOCK);
    public static final RegistryObject<BlockItemNF> GLASS_SLAB = register(BlocksNF.GLASS_SLAB);
    public static final RegistryObject<BlockItemNF> GLASS_SIDING = register(BlocksNF.GLASS_SIDING);
    //Survival
    public static final RegistryObject<BlockItemNF> WET_MUD_BRICKS = register(BlocksNF.WET_MUD_BRICKS);
    public static final RegistryObject<BlockItemNF> CLAY_BRICKS = register(BlocksNF.CLAY_BRICKS);
    public static final RegistryObject<BlockItemNF> FIRE_CLAY_BRICKS = register(BlocksNF.FIRE_CLAY_BRICKS);
    public static final Map<Tree, RegistryObject<BlockItemNF>> BARRELS = DataUtil.mapEnum(Tree.class, tree -> register(BlocksNF.BARRELS.get(tree), 8));
    public static final Map<Tree, RegistryObject<BlockItemNF>> CHESTS = DataUtil.mapEnum(Tree.class, tree -> register(BlocksNF.CHESTS.get(tree), 8));
    public static final Map<Tree, RegistryObject<BlockItemNF>> RACKS = DataUtil.mapEnum(Tree.class, tree -> register(BlocksNF.RACKS.get(tree)));
    public static final Map<Tree, RegistryObject<BlockItemNF>> SHELVES = DataUtil.mapEnum(Tree.class, tree -> register(BlocksNF.SHELVES.get(tree)));
    public static final Map<Tree, RegistryObject<BlockItemNF>> CHAIRS = DataUtil.mapEnum(Tree.class, tree -> register(BlocksNF.CHAIRS.get(tree)));
    public static final Map<Tree, RegistryObject<BlockItemNF>> TROUGHS = DataUtil.mapEnum(Tree.class, tree -> register(BlocksNF.TROUGHS.get(tree)));
    public static final RegistryObject<TorchItem> TORCH = ITEMS.register("torch", () -> new TorchItem(BlocksNF.TORCH.get(), BlocksNF.WALL_TORCH.get(),
            ItemsNF.TORCH_UNLIT, new Item.Properties().tab(UTILITIES_TAB)));
    public static final RegistryObject<IgnitableItem> TORCH_UNLIT = register("torch_unlit", () -> new IgnitableItem(TORCH, utility()));
    public static final RegistryObject<RopeBlockItem> ROPE = ITEMS.register("rope", () -> new RopeBlockItem(BlocksNF.ROPE.get(),
            new Item.Properties().tab(UTILITIES_TAB)));
    public static final RegistryObject<BlockItemNF> WOODEN_BOWL = register(BlocksNF.WOODEN_BOWL, FUNCTIONAL_TAB);
    public static final RegistryObject<BlockItemNF> CAMPFIRE = register(BlocksNF.CAMPFIRE, 8);
    public static final RegistryObject<BlockItemNF> CAULDRON = register(BlocksNF.CAULDRON, 8, FUNCTIONAL_TAB);
    public static final RegistryObject<BlockItemNF> UNFIRED_CAULDRON = register(BlocksNF.UNFIRED_CAULDRON, 8);
    public static final RegistryObject<BlockItemNF> POT = register(BlocksNF.POT, 8, FUNCTIONAL_TAB);
    public static final RegistryObject<BlockItemNF> UNFIRED_POT = register(BlocksNF.UNFIRED_POT, 8);
    public static final RegistryObject<BlockItemNF> WARDING_EFFIGY = register(BlocksNF.WARDING_EFFIGY, FUNCTIONAL_TAB);
    public static final RegistryObject<MeleeWeaponItem> WOODEN_CLUB = ITEMS.register("wooden_club", () -> new MeleeWeaponItem(TieredItemMaterial.WOOD,
            PlayerActionSet.CLUB, HurtSphere.CLUB, HurtSphere.WEAPONS_TP.get(HurtSphere.CLUB), false, new Item.Properties().tab(ARMAMENTS_TAB), List.of(), DamageType.STRIKING));
    public static final RegistryObject<MeleeWeaponItem> FLINT_ADZE = ITEMS.register("flint_adze", () -> new MeleeWeaponItem(TieredItemMaterial.FLINT,
            PlayerActionSet.ADZE, HurtSphere.ADZE, HurtSphere.WEAPONS_TP.get(HurtSphere.ADZE), true, new Item.Properties().tab(ARMAMENTS_TAB), Armament.ADZE.getToolActions(), DamageType.SLASHING));
    public static final RegistryObject<MeleeWeaponItem> FLINT_AXE = ITEMS.register("flint_axe", () -> new MeleeWeaponItem(TieredItemMaterial.FLINT,
            PlayerActionSet.AXE, HurtSphere.AXE, HurtSphere.WEAPONS_TP.get(HurtSphere.AXE), true, new Item.Properties().tab(ARMAMENTS_TAB), Armament.AXE.getToolActions(), DamageType.SLASHING));
    public static final RegistryObject<MeleeWeaponItem> FLINT_DAGGER = ITEMS.register("flint_dagger", () -> new MeleeWeaponItem(TieredItemMaterial.FLINT,
            PlayerActionSet.DAGGER, HurtSphere.FLINT_DAGGER, HurtSphere.WEAPONS_TP.get(HurtSphere.FLINT_DAGGER), true, new Item.Properties().tab(ARMAMENTS_TAB), Armament.DAGGER.getToolActions(), DamageType.SLASHING));
    public static final RegistryObject<PairedMeleeWeaponItem> FLINT_CHISEL = ITEMS.register("flint_chisel", () -> new PairedMeleeWeaponItem(
            TieredItemMaterial.FLINT, PlayerActionSet.CHISEL, PlayerActionSet.HAMMER, PlayerActionSet.FLINT_CHISEL_AND_HAMMER, HurtSphere.CHISEL,
            HurtSphere.WEAPONS_TP.get(HurtSphere.CHISEL), true, new Item.Properties().tab(ARMAMENTS_TAB), Armament.CHISEL.getToolActions(), DamageType.PIERCING));
    public static final RegistryObject<MeleeWeaponItem> FLINT_HAMMER = ITEMS.register("flint_hammer", () -> new MeleeWeaponItem(TieredItemMaterial.FLINT,
            PlayerActionSet.HAMMER, HurtSphere.HAMMER, HurtSphere.WEAPONS_TP.get(HurtSphere.HAMMER), true, new Item.Properties().tab(ARMAMENTS_TAB), Armament.HAMMER.getToolActions(), DamageType.STRIKING));
    public static final RegistryObject<MeleeWeaponItem> FLINT_SHOVEL = ITEMS.register("flint_shovel", () -> new MeleeWeaponItem(TieredItemMaterial.FLINT,
            PlayerActionSet.SHOVEL, HurtSphere.SHOVEL, HurtSphere.WEAPONS_TP.get(HurtSphere.SHOVEL), true, new Item.Properties().tab(ARMAMENTS_TAB), Armament.SHOVEL.getToolActions(), DamageType.STRIKING));
    public static final RegistryObject<MeleeWeaponItem> FLINT_SPEAR = ITEMS.register("flint_spear", () -> new MeleeWeaponItem(TieredItemMaterial.FLINT,
            PlayerActionSet.SPEAR, HurtSphere.SPEAR, HurtSphere.WEAPONS_TP.get(HurtSphere.SPEAR), false, new Item.Properties().tab(ARMAMENTS_TAB), Armament.SPEAR.getToolActions(), DamageType.PIERCING));
    public static final RegistryObject<SlingItem> SLING = ITEMS.register("sling", () -> new SlingItem(
            ActionsNF.SLING_THROW, TagsNF.SLING_AMMO, 2F, new Item.Properties().durability(120).tab(ARMAMENTS_TAB)));
    public static final RegistryObject<SlingItem> SLING_REINFORCED = ITEMS.register("reinforced_sling", () -> new SlingItem(
            ActionsNF.SLING_THROW, TagsNF.SLING_AMMO, 1F, new Item.Properties().durability(440).tab(ARMAMENTS_TAB)));
    public static final Map<Tree, RegistryObject<BowItemNF>> BOWS = DataUtil.mapEnum(Tree.class, tree -> tree.getHardness() < 1.4F || tree.getHardness() > 2.0F,
            tree -> ITEMS.register(tree.getName() + "_bow", () -> new BowItemNF(
                    ActionsNF.BOW_SHOOT, ItemTags.ARROWS, new Item.Properties().durability((int) (tree.getHardness() * 120)).tab(ARMAMENTS_TAB))));
    public static final RegistryObject<ProjectileItem> FLINT_ARROW = register("flint_arrow", () -> new ProjectileItem(
            15F, 1F, 0.5F, new DamageType[] {DamageType.PIERCING}, ARROW_ID++, new Item.Properties().tab(ARMAMENTS_TAB)));
    public static final RegistryObject<ProjectileItem> BONE_ARROW = register("bone_arrow", () -> new ProjectileItem(
            15F, 1F, 0.5F, new DamageType[] {DamageType.PIERCING}, ARROW_ID++, new Item.Properties().tab(ARMAMENTS_TAB)));
    public static final RegistryObject<ProjectileItem> RUSTED_ARROW = register("rusted_arrow", () -> new ProjectileItem(
            20F, 0.95F, 0.7F, new DamageType[] {DamageType.PIERCING}, ARROW_ID++, new Item.Properties().tab(ARMAMENTS_TAB)));
    public static final RegistryObject<DyeableAttributeEquipmentItem> BACKPACK = ITEMS.register("backpack", () -> new DyeableAttributeEquipmentItem(
            EquipmentSlot.CHEST, RenderUtil.COLOR_LEATHER, ImmutableMultimap.of(AttributesNF.INVENTORY_CAPACITY, new AttributeModifier(new UUID(RANDOM.nextLong(), RANDOM.nextLong()),
            "inventory_capacity", 12, AttributeModifier.Operation.ADDITION)), new Item.Properties().tab(UTILITIES_TAB).stacksTo(1)));
    public static final RegistryObject<PouchItem> POUCH = ITEMS.register("pouch", () -> new PouchItem(ImmutableMultimap.of(
            AttributesNF.INVENTORY_CAPACITY, new AttributeModifier(new UUID(RANDOM.nextLong(), RANDOM.nextLong()), "inventory_capacity",
                    4, AttributeModifier.Operation.ADDITION)), new Item.Properties().tab(UTILITIES_TAB).stacksTo(1)));
    public static final RegistryObject<MaskItem> MASK = ITEMS.register("mask", () -> new MaskItem(ImmutableMultimap.of(
            AttributesNF.POISON_RESISTANCE, new AttributeModifier(new UUID(RANDOM.nextLong(), RANDOM.nextLong()), "mask_poison_resistance",
                    0.2, AttributeModifier.Operation.ADDITION)), new Item.Properties().tab(UTILITIES_TAB).stacksTo(1)));
    public static final RegistryObject<ActionableItem> FIBER_BANDAGE = ITEMS.register("fiber_bandage", () -> new ActionableItem(
            ActionsNF.FIBER_BANDAGE_USE, new Item.Properties().tab(CONSUMABLES_TAB).stacksTo(16)));
    public static final RegistryObject<ActionableItem> BANDAGE = ITEMS.register("bandage", () -> new ActionableItem(
            ActionsNF.BANDAGE_USE, new Item.Properties().tab(CONSUMABLES_TAB).stacksTo(16)));
    public static final RegistryObject<ActionableItem> MEDICINAL_BANDAGE = ITEMS.register("medicinal_bandage", () -> new ActionableItem(
            ActionsNF.MEDICINAL_BANDAGE_USE, new Item.Properties().tab(CONSUMABLES_TAB).stacksTo(16)));
    private static final float[] SHIELD_DEFENSES = new float[]{0.5F, 0.5F, 0.5F, 0.3F, 0.4F, 0.4F};
    public static final RegistryObject<ShieldItemNF> IRONWOOD_SHIELD = ITEMS.register("ironwood_shield", () -> new ShieldItemNF(SHIELD_DEFENSES,
            ActionsNF.SHIELD_GUARD, new Item.Properties().durability((int) (Tree.IRONWOOD.getHardness() * 150)).tab(ARMAMENTS_TAB)));
    public static final RegistryObject<DyedShieldItem> IRONWOOD_SHIELD_DYED = ITEMS.register("ironwood_shield_dyed", () -> new DyedShieldItem(SHIELD_DEFENSES,
            ActionsNF.SHIELD_GUARD, new Item.Properties().durability((int) (Tree.IRONWOOD.getHardness() * 150))));
    public static final RegistryObject<WardingCharmItem> WARDING_CHARM = ITEMS.register("warding_charm", () -> new WardingCharmItem(utility().stacksTo(1)));
    //Metallurgy
    public static final Map<Metal, RegistryObject<BuildingMaterialItem>> INGOTS = DataUtil.mapEnum(Metal.class, metal ->
            register(metal.getName() + "_ingot", () -> new BuildingMaterialItem(ingredient().stacksTo(32))));
    public static final Map<Metal, RegistryObject<Item>> BILLETS = DataUtil.mapEnum(Metal.class, metal ->
            register(metal.getName() + "_billet", 16, INGREDIENTS_TAB));
    public static final Map<Metal, RegistryObject<Item>> WIRES = DataUtil.mapEnum(Metal.class, metal ->
            register(metal.getName() + "_wire", INGREDIENTS_TAB));
    public static final Map<Metal, RegistryObject<Item>> PLATES = DataUtil.mapEnum(Metal.class, metal ->
            register(metal.getName() + "_plate", 32, INGREDIENTS_TAB));
    public static final Map<Metal, RegistryObject<Item>> CHAINMAIL = DataUtil.mapEnum(Metal.class, metal -> metal.getCategory() == IMetal.Category.NOBLE,
            metal -> register(metal.getName() + "_chainmail", INGREDIENTS_TAB));
    public static final Map<Metal, RegistryObject<Item>> SCALES = DataUtil.mapEnum(Metal.class, metal -> metal.getCategory() == IMetal.Category.NOBLE,
            metal -> register(metal.getName() + "_scales", INGREDIENTS_TAB));
    public static final Map<TieredItemMaterial, Map<Armament, RegistryObject<Item>>> ARMAMENT_HEADS = DataUtil.mapEnum(TieredItemMaterial.class,
            material -> material.getMetal() == null || material.getMetal().getCategory() == IMetal.Category.NOBLE, material ->
            DataUtil.mapEnum(Armament.class, armament -> armament == Armament.HAMMER, armament -> register(material.getName() + "_" + armament.getName() + "_head", 1)));
    public static final Map<TieredItemMaterial, RegistryObject<Item>> METAL_ARROWHEADS = DataUtil.mapEnum(TieredItemMaterial.class,
            material -> material.getMetal() == null || material.getMetal().getCategory() == IMetal.Category.NOBLE, material ->
                    register(material.getName() + "_arrowhead"));

    private static float[] getShieldDefenses(Metal metal) {
        float[] defenses = new float[6];
        for(int i = 0; i < defenses.length; i++) {
            defenses[i] = 0.3F + metal.getBaseDefenses().get(i);
        }
        return defenses;
    }
    public static final Map<Metal, RegistryObject<ShieldItemNF>> METAL_SHIELDS = DataUtil.mapEnum(Metal.class, metal -> metal.getCategory() == IMetal.Category.NOBLE, metal ->
            register(metal.getName() + "_shield", () -> new ShieldItemNF(getShieldDefenses(metal),
                    ActionsNF.SHIELD_GUARD, new Item.Properties().durability((int) (metal.getStrength() * 70)).tab(ARMAMENTS_TAB))));
    public static final Map<Metal, RegistryObject<DyedShieldItem>> METAL_SHIELDS_DYED = DataUtil.mapEnum(Metal.class, metal -> metal.getCategory() == IMetal.Category.NOBLE, metal ->
            register(metal.getName() + "_shield_dyed", () -> new DyedShieldItem(getShieldDefenses(metal),
                    ActionsNF.SHIELD_GUARD, new Item.Properties().durability((int) (metal.getStrength() * 70)))));
    public static final Map<TieredItemMaterial, RegistryObject<ProjectileItem>> METAL_ARROWS = DataUtil.mapEnum(TieredItemMaterial.class,
            (material) -> material.getMetal() == null || material.getMetal().getCategory() == IMetal.Category.NOBLE, (material) -> register(
                    material.getName() + "_arrow", () -> new ProjectileItem(30 * material.getDamageMultiplier(),
                            1F, 0.5F, DamageType.PIERCING.asArray(), ARROW_ID++, armament())));

    public static final RegistryObject<LightItem> IRON_BLOOM = ITEMS.register("iron_bloom", () -> new LightItem(
            1.15, 13, 5, ItemsNF.SLAG, ingredient().stacksTo(16)));

    public static final Map<Metal, RegistryObject<BlockItemNF>> METAL_BLOCKS = DataUtil.mapEnum(Metal.class, metal -> !BlocksNF.METAL_BLOCKS.containsKey(metal),
            metal -> register(BlocksNF.METAL_BLOCKS.get(metal)));
    public static final Map<Metal, RegistryObject<BlockItemNF>> INGOT_PILES = DataUtil.mapEnum(Metal.class, metal -> !BlocksNF.INGOT_PILES.containsKey(metal),
            metal -> register(BlocksNF.INGOT_PILES.get(metal)));
    public static final RegistryObject<BlockItemNF> STEEL_INGOT_PILE_POOR = register(BlocksNF.STEEL_INGOT_PILE_POOR, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> STEEL_INGOT_PILE_FAIR = register(BlocksNF.STEEL_INGOT_PILE_FAIR, NATURAL_TAB);
    public static final Map<Metal, RegistryObject<LanternItem>> LANTERNS = DataUtil.mapEnum(Metal.class, metal ->
            register(BlocksNF.LANTERNS.get(metal).getId().getPath(), () -> new LanternItem(BlocksNF.LANTERNS.get(metal).get(),
                    ItemsNF.LANTERNS_UNLIT.get(metal), new Item.Properties().stacksTo(16).tab(UTILITIES_TAB))));
    public static final Map<Metal, RegistryObject<UnlitLanternItem>> LANTERNS_UNLIT = DataUtil.mapEnum(Metal.class, metal ->
            register(BlocksNF.LANTERNS_UNLIT.get(metal).getId().getPath(), () -> new UnlitLanternItem(ItemsNF.LANTERNS.get(metal),
                    BlocksNF.LANTERNS_UNLIT.get(metal).get(), utility().stacksTo(16))));

    public static final Map<Tree, RegistryObject<BlockItemNF>> ANVILS_LOG = DataUtil.mapEnum(Tree.class, tree -> !BlocksNF.ANVILS_LOG.containsKey(tree),
            tree -> register(BlocksNF.ANVILS_LOG.get(tree), 1, FUNCTIONAL_TAB));
    public static final Map<Stone, RegistryObject<BlockItemNF>> ANVILS_STONE = DataUtil.mapEnum(Stone.class, stone -> !BlocksNF.ANVILS_STONE.containsKey(stone),
            stone -> register(BlocksNF.ANVILS_STONE.get(stone), 1, FUNCTIONAL_TAB));
    public static final Map<Metal, RegistryObject<BlockItemNF>> ANVILS_METAL = DataUtil.mapEnum(Metal.class, metal -> !BlocksNF.ANVILS_METAL.containsKey(metal),
            metal -> register(BlocksNF.ANVILS_METAL.get(metal), 1, FUNCTIONAL_TAB));

    public static final Map<Armament, RegistryObject<BlockItemNF>> ARMAMENT_MOLDS = DataUtil.mapEnum(Armament.class, a -> !BlocksNF.ARMAMENT_MOLDS.containsKey(a),
            armament -> register(BlocksNF.ARMAMENT_MOLDS.get(armament), FUNCTIONAL_TAB));
    public static final RegistryObject<BlockItemNF> INGOT_MOLD = register(BlocksNF.INGOT_MOLD, FUNCTIONAL_TAB);
    public static final RegistryObject<BlockItemNF> ARROWHEAD_MOLD = register(BlocksNF.ARROWHEAD_MOLD, FUNCTIONAL_TAB);
    public static final Map<Armament, RegistryObject<BlockItemNF>> UNFIRED_ARMAMENT_MOLDS = DataUtil.mapEnum(Armament.class, a -> !BlocksNF.UNFIRED_ARMAMENT_MOLDS.containsKey(a),
            armament -> register(BlocksNF.UNFIRED_ARMAMENT_MOLDS.get(armament)));
    public static final RegistryObject<BlockItemNF> UNFIRED_INGOT_MOLD = register(BlocksNF.UNFIRED_INGOT_MOLD);
    public static final RegistryObject<BlockItemNF> UNFIRED_ARROWHEAD_MOLD = register(BlocksNF.UNFIRED_ARROWHEAD_MOLD);

    public static final RegistryObject<BlockItemNF> COKE_BLOCK = register(BlocksNF.COKE);
    public static final RegistryObject<BlockItemNF> COAL_BLOCK = register(BlocksNF.COAL);
    public static final RegistryObject<BlockItemNF> CHARCOAL_BLOCK = register(BlocksNF.CHARCOAL);
    public static final RegistryObject<BlockItemNF> FIREWOOD_BLOCK = register(BlocksNF.FIREWOOD);
    public static final RegistryObject<BlockItemNF> SLAG_BLOCK = register(BlocksNF.SLAG);
    public static final RegistryObject<BlockItemNF> AZURITE_BLOCK = register(BlocksNF.AZURITE);
    public static final RegistryObject<BlockItemNF> HEMATITE_BLOCK = register(BlocksNF.HEMATITE);
    public static final RegistryObject<BlockItemNF> SMELTED_AZURITE = register(BlocksNF.SMELTED_AZURITE, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> SMELTED_HEMATITE = register(BlocksNF.SMELTED_HEMATITE, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> UNFIRED_CRUCIBLE = register(BlocksNF.UNFIRED_CRUCIBLE, 8);
    public static final RegistryObject<BlockItemNF> CRUCIBLE = register(BlocksNF.CRUCIBLE, 8, FUNCTIONAL_TAB);

    public static final RegistryObject<BlockItemNF> SPIDER_WEB = register(BlocksNF.SPIDER_WEB, 16, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> SPIDER_NEST = register(BlocksNF.SPIDER_NEST, NATURAL_TAB);
    public static final RegistryObject<BlockItemNF> ANCHORING_RESIN_BLOCK = register(BlocksNF.ANCHORING_RESIN, NATURAL_TAB);
    public static final Map<Stone, RegistryObject<BlockItemNF>> STONE_TUNNELS = DataUtil.mapEnum(Stone.class, stone -> !BlocksNF.STONE_TUNNELS.containsKey(stone), stone ->
            register(BlocksNF.STONE_TUNNELS.get(stone), NATURAL_TAB));
    public static final Map<Stone, RegistryObject<BlockItemNF>> SKARA_ROCK_CLUSTERS = DataUtil.mapEnum(Stone.class, stone -> !BlocksNF.SKARA_ROCK_CLUSTERS.containsKey(stone), stone ->
            register(BlocksNF.SKARA_ROCK_CLUSTERS.get(stone), NATURAL_TAB));
    public static final RegistryObject<BlockItemNF> DRAKEFOWL_NEST = register(BlocksNF.DRAKEFOWL_NEST, NATURAL_TAB);

    public static void register() {
        ITEMS.register(Nightfall.MOD_EVENT_BUS);
    }

    public static Set<RegistryObject<TieredArmorItem>> getTieredArmors() {
        return Stream.of(HELMETS.values(), CHESTPLATES.values(), LEGGINGS.values(), BOOTS.values()).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item) {
        return ITEMS.register(name, item);
    }

    private static RegistryObject<Item> register(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> register(String name, int stackSize) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().stacksTo(stackSize)));
    }

    private static RegistryObject<Item> register(String name, CreativeModeTab tab) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().tab(tab)));
    }

    private static RegistryObject<Item> register(String name, int stackSize, CreativeModeTab tab) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().stacksTo(stackSize).tab(tab)));
    }

    private static RegistryObject<BlockItemNF> register(RegistryObject<? extends Block> block, int stackSize, CreativeModeTab tab) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItemNF(block.get(), new Item.Properties().stacksTo(stackSize).tab(tab)));
    }

    private static RegistryObject<BlockItemNF> register(RegistryObject<? extends Block> block, CreativeModeTab tab) {
        return register(block, 16, tab);
    }

    private static RegistryObject<BlockItemNF> register(RegistryObject<? extends Block> block, int stackSize) {
        return register(block, stackSize, null);
    }

    private static RegistryObject<BlockItemNF> register(RegistryObject<? extends Block> block) {
        return register(block, 16, null);
    }

    private static Item.Properties armament() {
        return new Item.Properties().tab(ARMAMENTS_TAB);
    }

    private static Item.Properties ingredient() {
        return new Item.Properties().tab(INGREDIENTS_TAB);
    }

    private static Item.Properties building() {
        return new Item.Properties().tab(BUILDING_TAB);
    }

    private static Item.Properties utility() {
        return new Item.Properties().tab(UTILITIES_TAB);
    }

    private static FoodProperties food(int hunger, float saturation) {
        return new FoodProperties.Builder().nutrition(hunger).saturationMod(saturation).build();
    }

    private static FoodProperties meat(int hunger, float saturation) {
        return new FoodProperties.Builder().nutrition(hunger).saturationMod(saturation).meat().build();
    }

    private static FoodProperties.Builder foodBuilder(int hunger, float saturation) {
        return new FoodProperties.Builder().nutrition(hunger).saturationMod(saturation);
    }
}
