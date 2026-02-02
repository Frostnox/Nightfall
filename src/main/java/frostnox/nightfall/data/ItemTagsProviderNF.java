package frostnox.nightfall.data;

import frostnox.nightfall.block.*;
import frostnox.nightfall.item.*;
import frostnox.nightfall.item.item.TieredArmorItem;
import frostnox.nightfall.registry.forge.ItemsNF;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class ItemTagsProviderNF extends ItemTagsProvider {
    public ItemTagsProviderNF(DataGenerator generator, BlockTagsProvider provider, String id, @Nullable ExistingFileHelper helper) {
        super(generator, provider, id, helper);
    }

    @Override
    protected void addTags() {
        //Replace all item tags to prevent vanilla items from appearing when querying collections
        //This breaks tons of vanilla functionality but that's okay
        for(Field field : ItemTags.class.getDeclaredFields()) {
            TagKey<Item> tag;
            try {
                tag = (TagKey<Item>) field.get(null);
            }
            catch(IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if(tag.location().getNamespace().equals("minecraft")) tag(tag).replace();
        }
        //Recopy block tags
        copy(BlockTags.WOOL, ItemTags.WOOL);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.STONE_BRICKS, ItemTags.STONE_BRICKS);
        copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        copy(BlockTags.BUTTONS, ItemTags.BUTTONS);
        copy(BlockTags.CARPETS, ItemTags.CARPETS);
        copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        copy(BlockTags.DOORS, ItemTags.DOORS);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
        copy(BlockTags.OAK_LOGS, ItemTags.OAK_LOGS);
        copy(BlockTags.DARK_OAK_LOGS, ItemTags.DARK_OAK_LOGS);
        copy(BlockTags.BIRCH_LOGS, ItemTags.BIRCH_LOGS);
        copy(BlockTags.ACACIA_LOGS, ItemTags.ACACIA_LOGS);
        copy(BlockTags.SPRUCE_LOGS, ItemTags.SPRUCE_LOGS);
        copy(BlockTags.JUNGLE_LOGS, ItemTags.JUNGLE_LOGS);
        copy(BlockTags.CRIMSON_STEMS, ItemTags.CRIMSON_STEMS);
        copy(BlockTags.WARPED_STEMS, ItemTags.WARPED_STEMS);
        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.SAND, ItemTags.SAND);
        copy(BlockTags.SLABS, ItemTags.SLABS);
        copy(BlockTags.WALLS, ItemTags.WALLS);
        copy(BlockTags.STAIRS, ItemTags.STAIRS);
        copy(BlockTags.ANVIL, ItemTags.ANVIL);
        copy(BlockTags.RAILS, ItemTags.RAILS);
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        copy(BlockTags.TRAPDOORS, ItemTags.TRAPDOORS);
        copy(BlockTags.SMALL_FLOWERS, ItemTags.SMALL_FLOWERS);
        copy(BlockTags.BEDS, ItemTags.BEDS);
        copy(BlockTags.FENCES, ItemTags.FENCES);
        copy(BlockTags.TALL_FLOWERS, ItemTags.TALL_FLOWERS);
        copy(BlockTags.FLOWERS, ItemTags.FLOWERS);
        copy(BlockTags.SOUL_FIRE_BASE_BLOCKS, ItemTags.SOUL_FIRE_BASE_BLOCKS);
        copy(BlockTags.CANDLES, ItemTags.CANDLES);
        copy(BlockTags.OCCLUDES_VIBRATION_SIGNALS, ItemTags.OCCLUDES_VIBRATION_SIGNALS);
        copy(BlockTags.GOLD_ORES, ItemTags.GOLD_ORES);
        copy(BlockTags.IRON_ORES, ItemTags.IRON_ORES);
        copy(BlockTags.DIAMOND_ORES, ItemTags.DIAMOND_ORES);
        copy(BlockTags.REDSTONE_ORES, ItemTags.REDSTONE_ORES);
        copy(BlockTags.LAPIS_ORES, ItemTags.LAPIS_ORES);
        copy(BlockTags.COAL_ORES, ItemTags.COAL_ORES);
        copy(BlockTags.EMERALD_ORES, ItemTags.EMERALD_ORES);
        copy(BlockTags.COPPER_ORES, ItemTags.COPPER_ORES);
        copy(BlockTags.DIRT, ItemTags.DIRT);
        copy(BlockTags.TERRACOTTA, ItemTags.TERRACOTTA);

        tag(TagsNF.ACCESSORY_FACE).add(ItemsNF.MASK.get());
        tag(TagsNF.ACCESSORY_NECK).add(ItemsNF.WARDING_CHARM.get());
        tag(TagsNF.ACCESSORY_WAIST).add(ItemsNF.POUCH.get());
        for(var item : ItemsNF.LANTERNS.values()) tag(TagsNF.ACCESSORY_WAIST).add(item.get());
        for(var item : ItemsNF.LANTERNS_UNLIT.values()) tag(TagsNF.ACCESSORY_WAIST).add(item.get());

        tag(TagsNF.FLUX).add(ItemsNF.LIME.get(), ItemsNF.SOILS.get(Soil.SAND).get(), ItemsNF.SOILS.get(Soil.WHITE_SAND).get());
        tag(TagsNF.TANNIN).addTag(TagsNF.LUMBER_TANNIN);
        tag(TagsNF.LUMBER_TANNIN).add(ItemsNF.LOGS.get(Tree.OAK).get(), ItemsNF.LOGS.get(Tree.LARCH).get(), ItemsNF.LOGS.get(Tree.BIRCH).get(), ItemsNF.LOGS.get(Tree.IRONWOOD).get(), ItemsNF.LOGS.get(Tree.ACACIA).get());
        for(var item : ItemsNF.BARRELS.values()) tag(Tags.Items.BARRELS_WOODEN).add(item.get());
        for(var item : ItemsNF.CHESTS.values()) tag(Tags.Items.CHESTS_WOODEN).add(item.get());
        for(var item : ItemsNF.PLANK_SIGNS.values()) tag(ItemTags.SIGNS).add(item.get());

        for(Metal metal : ItemsNF.INGOTS.keySet()) tag(metal.getTag()).add(ItemsNF.INGOTS.get(metal).get());
        for(TieredItemMaterial material : ItemsNF.ARMAMENT_HEADS.keySet()) {
            for(var item : ItemsNF.ARMAMENT_HEADS.get(material).values()) tag(material.getMetal().getTag()).add(item.get());
        }
        for(var item : ItemsNF.METAL_ARROWHEADS.values()) tag(TagsNF.ARROWHEAD).add(item.get());
        for(var item : ItemsNF.METAL_ARROWS.values()) tag(TagsNF.METAL_ARROW).add(item.get());
        for(TieredItemMaterial material : ItemsNF.METAL_ARMAMENTS.keySet()) {
            tag(TagsNF.ADZE).add(ItemsNF.METAL_ARMAMENTS.get(material).get(Armament.ADZE).get());
            tag(TagsNF.AXE).add(ItemsNF.METAL_ARMAMENTS.get(material).get(Armament.AXE).get());
            tag(TagsNF.KNIFE).add(ItemsNF.METAL_ARMAMENTS.get(material).get(Armament.KNIFE).get());
            tag(TagsNF.CHISEL_METAL).add(ItemsNF.METAL_ARMAMENTS.get(material).get(Armament.CHISEL).get());
            tag(TagsNF.HAMMER).add(ItemsNF.METAL_ARMAMENTS.get(material).get(Armament.HAMMER).get());
            tag(TagsNF.MACE).add(ItemsNF.METAL_ARMAMENTS.get(material).get(Armament.MACE).get());
            tag(TagsNF.PICKAXE).add(ItemsNF.METAL_ARMAMENTS.get(material).get(Armament.PICKAXE).get());
            tag(TagsNF.SABRE).add(ItemsNF.METAL_ARMAMENTS.get(material).get(Armament.SABRE).get());
            tag(TagsNF.SICKLE).add(ItemsNF.METAL_ARMAMENTS.get(material).get(Armament.SICKLE).get());
            tag(TagsNF.SHOVEL).add(ItemsNF.METAL_ARMAMENTS.get(material).get(Armament.SHOVEL).get());
            tag(TagsNF.SPEAR).add(ItemsNF.METAL_ARMAMENTS.get(material).get(Armament.SPEAR).get());
            tag(TagsNF.SWORD).add(ItemsNF.METAL_ARMAMENTS.get(material).get(Armament.SWORD).get());
            tag(TagsNF.ADZE_HEAD).add(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.ADZE).get());
            tag(TagsNF.AXE_HEAD).add(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.AXE).get());
            tag(TagsNF.CHISEL_HEAD).add(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.CHISEL).get());
            tag(TagsNF.KNIFE_HEAD).add(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.KNIFE).get());
            tag(TagsNF.MACE_HEAD).add(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.MACE).get());
            tag(TagsNF.PICKAXE_HEAD).add(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.PICKAXE).get());
            tag(TagsNF.SABRE_HEAD).add(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.SABRE).get());
            tag(TagsNF.SHOVEL_HEAD).add(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.SHOVEL).get());
            tag(TagsNF.SICKLE_HEAD).add(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.SICKLE).get());
            tag(TagsNF.SPEAR_HEAD).add(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.SPEAR).get());
            tag(TagsNF.SWORD_HEAD).add(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.SWORD).get());
        }
        for(var item :ItemsNF.FLINT_ARMAMENT_HEADS.values()) tag(TagsNF.FLINT_ARMAMENT_HEAD).add(item.get());
        tag(TagsNF.AXE).add(ItemsNF.FLINT_AXE.get());
        tag(TagsNF.SHOVEL).add(ItemsNF.FLINT_SHOVEL.get());
        tag(TagsNF.ADZE).add(ItemsNF.FLINT_ADZE.get());
        tag(TagsNF.KNIFE).add(ItemsNF.FLINT_KNIFE.get());
        tag(TagsNF.CHISEL).add(ItemsNF.FLINT_CHISEL.get());
        tag(TagsNF.CHISEL).addTag(TagsNF.CHISEL_METAL);
        tag(TagsNF.HAMMER).add(ItemsNF.FLINT_HAMMER.get());
        tag(TagsNF.SPEAR).add(ItemsNF.FLINT_SPEAR.get(), ItemsNF.RUSTED_SPEAR.get());
        tag(TagsNF.MACE).add(ItemsNF.WOODEN_CLUB.get());
        for(var item : ItemsNF.BOWS.values()) tag(TagsNF.WOODEN_BOW).add(item.get());
        tag(TagsNF.BOW).addTag(TagsNF.WOODEN_BOW);
        tag(TagsNF.BOW).add(ItemsNF.TWISTED_BOW.get());
        tag(TagsNF.WOODEN_SHIELD).add(ItemsNF.IRONWOOD_SHIELD.get(), ItemsNF.IRONWOOD_SHIELD_DYED.get());
        tag(TagsNF.SHIELD).addTags(TagsNF.WOODEN_SHIELD, TagsNF.UNDYED_METAL_SHIELD);
        for(var item : ItemsNF.METAL_SHIELDS.values()) tag(TagsNF.UNDYED_METAL_SHIELD).add(item.get());
        for(var item : ItemsNF.METAL_SHIELDS_DYED.values()) tag(TagsNF.SHIELD).add(item.get());
        tag(TagsNF.TOOL).addTags(TagsNF.ADZE, TagsNF.AXE, TagsNF.CHISEL, TagsNF.KNIFE, TagsNF.HAMMER, TagsNF.PICKAXE, TagsNF.SICKLE, TagsNF.SHOVEL);
        tag(TagsNF.TOOL).add(ItemsNF.RUSTED_MAUL.get());
        tag(TagsNF.ARMAMENT).addTags(TagsNF.TOOL, TagsNF.MACE, TagsNF.SABRE, TagsNF.SPEAR, TagsNF.SWORD);
        tag(TagsNF.ARMAMENT).add(ItemsNF.FIRESTARTER.get());
        tag(TagsNF.CHISEL_OR_HAMMER).addTags(TagsNF.CHISEL, TagsNF.HAMMER);
        tag(TagsNF.SABRE_OR_HEAD).addTags(TagsNF.SABRE, TagsNF.SABRE_HEAD);
        tag(TagsNF.SICKLE_OR_HEAD).addTags(TagsNF.SICKLE, TagsNF.SICKLE_HEAD);
        tag(TagsNF.RECIPE_TOOL_WOOD_SIMPLE).addTags(TagsNF.ADZE, TagsNF.AXE, TagsNF.KNIFE);
        tag(TagsNF.RECIPE_TOOL_WOOD_COMPLEX).addTag(TagsNF.ADZE);
        tag(TagsNF.RECIPE_TOOL_STONE_CARVE).addTag(TagsNF.CHISEL_METAL);
        tag(TagsNF.RECIPE_TOOL_FLINT).add(ItemsNF.FLINT.get(), ItemsNF.BONE.get());
        tag(TagsNF.RECIPE_TOOL_COCONUT).addTags(TagsNF.RECIPE_TOOL_FLINT, TagsNF.HAMMER);
        for(Stone type : ItemsNF.ROCKS.keySet()) {
            var item = ItemsNF.ROCKS.get(type);
            tag(TagsNF.SLING_AMMO).add(item.get());
            if(type.getType() == StoneType.SEDIMENTARY) tag(TagsNF.SEDIMENTARY).add(item.get());
            else if(type.getType() == StoneType.METAMORPHIC) tag(TagsNF.METAMORPHIC).add(item.get());
            else tag(TagsNF.IGNEOUS).add(item.get());
        }
        tag(TagsNF.FIRESTARTER_AMMO).add(ItemsNF.SULFUR.get());
        tag(TagsNF.FIRESTARTER_IRON_INGREDIENT).add(ItemsNF.HEMATITE_CHUNK.get(), ItemsNF.INGOTS.get(Metal.IRON).get(), ItemsNF.INGOTS.get(Metal.STEEL).get());
        tag(TagsNF.NO_HITSTOP).addTags(TagsNF.KNIFE, TagsNF.SICKLE);
        tag(TagsNF.GRID_INTERACTABLE).addTag(TagsNF.HAMMER);
        tag(TagsNF.MIXTURE_1);
        tag(TagsNF.MIXTURE_2).add(ItemsNF.RAW_VENISON.get(), ItemsNF.COOKED_VENISON.get(), ItemsNF.APPLE.get());
        tag(TagsNF.MIXTURE_3);
        tag(TagsNF.MIXTURE_4);
        tag(TagsNF.MIXTURE_5);
        tag(TagsNF.MIXTURE_10);
        tag(TagsNF.MIXTURE_20);
        tag(TagsNF.MIXTURE_30);
        tag(TagsNF.MIXTURE_40);
        tag(TagsNF.MIXTURE_50);
        tag(TagsNF.MIXTURE_100);
        tag(TagsNF.MIXTURE_INGREDIENT).addTags(TagsNF.MIXTURE_1, TagsNF.MIXTURE_2, TagsNF.MIXTURE_3, TagsNF.MIXTURE_4, TagsNF.MIXTURE_5,
                TagsNF.MIXTURE_10, TagsNF.MIXTURE_20, TagsNF.MIXTURE_30, TagsNF.MIXTURE_40, TagsNF.MIXTURE_50, TagsNF.MIXTURE_100);

        tag(TagsNF.SMELT_TIER_0);
        tag(TagsNF.SMELT_TIER_2).addTags(Metal.COPPER.getTag(), Metal.BRONZE.getTag(), Metal.METEORITE.getTag(), TagsNF.SAND_ITEM);
        tag(TagsNF.SMELT_TIER_3);
        tag(TagsNF.SMELT_TIER_4).addTags(Metal.IRON.getTag(), Metal.STEEL.getTag());
        tag(TagsNF.SMELT_TIER_5);
        tag(TagsNF.SMELT_TIER_CUSTOM).addTags(TagsNF.SMELT_TIER_0, TagsNF.SMELT_TIER_2, TagsNF.SMELT_TIER_3, TagsNF.SMELT_TIER_4, TagsNF.SMELT_TIER_5);
        for(var item : ItemsNF.getTieredArmors()) {
            TieredArmorItem armorItem = item.get();
            IStyle style = armorItem.material.getStyle();
            ArmorType type = armorItem.material.getArmorType();
            EquipmentSlot slot = armorItem.slot;
            if(style == Style.SURVIVOR) {
                if(type != ArmorType.SCALE) tag(TagsNF.WAIST_OFFSET_EXTRA).add(armorItem);
                if(type == ArmorType.PLATE && slot == EquipmentSlot.CHEST) tag(TagsNF.NECK_OFFSET_EXTRA).add(armorItem);
            }
            else if(style == Style.EXPLORER) {
                tag(TagsNF.WAIST_OFFSET_EXTRA).add(armorItem);
            }
            else if(style == Style.SLAYER) {
                if(type == ArmorType.PLATE && slot == EquipmentSlot.CHEST) tag(TagsNF.NECK_OFFSET_EXTRA).add(armorItem);
            }
        }
        tag(TagsNF.NECK_OFFSET_NONE).add(ItemsNF.BACKPACK.get());
        tag(TagsNF.WAIST_OFFSET_NONE).add(ItemsNF.BACKPACK.get());
        tag(TagsNF.STICK_FIRE_STARTER).add(ItemsNF.STICK.get());
        tag(TagsNF.FLINT_FIRE_STARTER_WEAK).add(ItemsNF.OBSIDIAN_SHARD.get(), ItemsNF.ROCKS.get(Stone.DEEPSLATE).get(), ItemsNF.ROCKS.get(Stone.STYGFEL).get(),
                ItemsNF.ROCKS.get(Stone.GRANITE).get(), ItemsNF.ROCKS.get(Stone.MOONSTONE).get());
        tag(TagsNF.FLINT_FIRE_STARTER_STRONG).add(ItemsNF.INGOTS.get(Metal.IRON).get(), ItemsNF.INGOTS.get(Metal.STEEL).get(), ItemsNF.BILLETS.get(Metal.IRON).get(),
                ItemsNF.BILLETS.get(Metal.STEEL).get(), ItemsNF.PLATES.get(Metal.IRON).get(), ItemsNF.PLATES.get(Metal.STEEL).get(),
                ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.CHISEL).get(), ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.STEEL).get(Armament.CHISEL).get(),
                ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.KNIFE).get(), ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.STEEL).get(Armament.KNIFE).get());
        tag(TagsNF.FLINT_FIRE_STARTER_STRONG).addTag(TagsNF.IRON_ORE);
        tag(TagsNF.RACK_ITEM).addTags(TagsNF.ARMAMENT, TagsNF.BOW, TagsNF.SHIELD);
        for(var item : ItemsNF.ROCKS.values()) tag(TagsNF.ROCK).add(item.get());
        tag(TagsNF.STONE).addTags(TagsNF.ROCK, TagsNF.CRUSHABLE_TO_BONE_SHARD);
        tag(TagsNF.STONE).add(ItemsNF.FLINT.get(), ItemsNF.OBSIDIAN_SHARD.get(), ItemsNF.BONE_SHARD.get());
        for(var item : ItemsNF.SOILS.values()) tag(TagsNF.SOIL_ITEM).add(item.get());
        tag(TagsNF.SAND_ITEM).add(ItemsNF.SOILS.get(Soil.SAND).get(), ItemsNF.SOILS.get(Soil.RED_SAND).get(), ItemsNF.SOILS.get(Soil.WHITE_SAND).get());
        tag(TagsNF.FLUID_ITEM).add(ItemsNF.WATER.get(), ItemsNF.SEAWATER.get());
        tag(TagsNF.COBBLE_MORTAR).add(ItemsNF.SOILS.get(Soil.SILT).get(), ItemsNF.CLAY.get(), ItemsNF.SOILS.get(Soil.SAND).get(),
                ItemsNF.SOILS.get(Soil.RED_SAND).get(), ItemsNF.SOILS.get(Soil.WHITE_SAND).get());
        for(Tree tree : ItemsNF.PLANKS.keySet()) {
            var item = ItemsNF.PLANKS.get(tree).get();
            tag(TagsNF.PLANK).add(item);
            if(tree.getHardness() < 1F) tag(TagsNF.PLANK_SOFT).add(item);
            else if(tree.getHardness() < 1.6F) tag(TagsNF.PLANK_FAIR).add(item);
            else tag(TagsNF.PLANK_HARD).add(item);
            if(tree.getHardness() >= 1.4F && tree.getHardness() <= 2F) tag(TagsNF.PLANK_BOW).add(item);
        }
        tag(TagsNF.ANIMAL_HIDE_LARGE);
        for(var item : ItemsNF.DEER_HIDES.values()) tag(TagsNF.ANIMAL_HIDE_MEDIUM).add(item.get());
        for(var item : ItemsNF.WOLF_PELTS.values()) tag(TagsNF.ANIMAL_HIDE_MEDIUM).add(item.get());
        for(var item : ItemsNF.MERBOR_HIDES.values()) tag(TagsNF.ANIMAL_HIDE_MEDIUM).add(item.get());
        tag(TagsNF.ANIMAL_HIDE_MEDIUM).add(ItemsNF.BLACK_WOLF_PELT.get(), ItemsNF.GOLDEN_MERBOR_HIDE.get());
        for(var item : ItemsNF.RABBIT_PELTS.values()) tag(TagsNF.ANIMAL_HIDE_SMALL).add(item.get());
        tag(TagsNF.ANIMAL_HIDE).addTags(TagsNF.ANIMAL_HIDE_SMALL, TagsNF.ANIMAL_HIDE_MEDIUM, TagsNF.ANIMAL_HIDE_LARGE);
        tag(TagsNF.RAWHIDE_LARGE);
        tag(TagsNF.CRUSHABLE_TO_LIME).add(ItemsNF.ROCKS.get(Stone.LIMESTONE).get(), ItemsNF.SEASHELL.get());
        tag(TagsNF.CRUSHABLE_TO_BONE_SHARD).add(ItemsNF.BONE.get(), ItemsNF.LIVING_BONE.get(), ItemsNF.PIT_DEVIL_TOOTH.get(), ItemsNF.MERBOR_TUSK.get());
        for(var item : ItemsNF.COCKATRICE_SKINS.values()) {
            tag(TagsNF.MONSTER_HIDE).add(item.get());
            tag(TagsNF.RAWHIDE_MEDIUM).add(item.get());
        }
        tag(TagsNF.RAWHIDE_SMALL).add(ItemsNF.RAWHIDE.get());
        tag(TagsNF.FLETCHING).add(ItemsNF.SCARLET_FEATHER.get());
        tag(TagsNF.CAULDRON_FLUID_MEAL).add(ItemsNF.MEAT_STEW.get(), ItemsNF.VEGETABLE_STEW.get(), ItemsNF.HEARTY_STEW.get(), ItemsNF.HAM_ROAST.get(), ItemsNF.SUSPICIOUS_STEW.get(), ItemsNF.BOILED_EGG.get());
        tag(TagsNF.COOKED_MEAT).add(ItemsNF.COOKED_GAME.get(), ItemsNF.COOKED_POULTRY.get(), ItemsNF.COOKED_PORK.get(), ItemsNF.COOKED_VENISON.get(), ItemsNF.COOKED_PALE_FLESH.get());
        tag(TagsNF.CURED_MEAT).add(ItemsNF.CURED_GAME.get(), ItemsNF.CURED_POULTRY.get(), ItemsNF.CURED_PORK.get(), ItemsNF.CURED_VENISON.get(), ItemsNF.CURED_PALE_FLESH.get());
        tag(TagsNF.CURABLE_FOOD).add(ItemsNF.RAW_GAME.get(), ItemsNF.RAW_VENISON.get(), ItemsNF.RAW_POULTRY.get(), ItemsNF.RAW_PORK.get(), ItemsNF.RAW_JELLYFISH.get(), ItemsNF.RAW_PALE_FLESH.get());
        tag(TagsNF.COOKED_VEGETABLE).add(ItemsNF.ROASTED_CARROT.get(), ItemsNF.ROASTED_POTATO.get());
        tag(ItemTags.ARROWS).add(ItemsNF.FLINT_ARROW.get(), ItemsNF.BONE_ARROW.get(), ItemsNF.RUSTED_ARROW.get());
        for(var arrow : ItemsNF.METAL_ARROWS.values()) tag(ItemTags.ARROWS).add(arrow.get());
        tag(Tags.Items.SEEDS).add(ItemsNF.CARROT_SEEDS.get(), ItemsNF.POTATO_SEEDS.get(), ItemsNF.YARROW_SEEDS.get());
        tag(TagsNF.MEAT).add(ItemsNF.RAW_GAME.get(), ItemsNF.RAW_VENISON.get(), ItemsNF.RAW_POULTRY.get(), ItemsNF.RAW_PORK.get(), ItemsNF.RAW_PALE_FLESH.get());
        tag(TagsNF.MEAT).addTags(TagsNF.COOKED_MEAT, TagsNF.CURED_MEAT);
        tag(TagsNF.VEGETABLE).add(ItemsNF.POTATO.get(), ItemsNF.CARROT.get(), ItemsNF.ROASTED_POTATO.get(), ItemsNF.ROASTED_CARROT.get());
        tag(TagsNF.FRUIT).add(ItemsNF.BERRIES.get(), ItemsNF.APPLE.get(), ItemsNF.COCONUT_HALF.get(), ItemsNF.COCOA_POD.get());
        tag(TagsNF.GRAIN);
        tag(TagsNF.HERB);
        tag(TagsNF.EGG).add(ItemsNF.DRAKEFOWL_EGG.get());
        tag(TagsNF.PORK).add(ItemsNF.RAW_PORK.get(), ItemsNF.COOKED_PORK.get(), ItemsNF.CURED_PORK.get());
        tag(TagsNF.FRUIT_OR_VEGETABLE).addTags(TagsNF.FRUIT, TagsNF.VEGETABLE);
        tag(TagsNF.FOOD_INGREDIENT).addTags(TagsNF.MEAT, TagsNF.VEGETABLE, TagsNF.FRUIT, TagsNF.GRAIN, TagsNF.HERB, TagsNF.EGG);

        tag(TagsNF.HERBIVORE_FOOD).add(ItemsNF.CARROT.get(), ItemsNF.ROASTED_CARROT.get(), ItemsNF.ROASTED_POTATO.get());
        tag(TagsNF.HERBIVORE_FOOD).addTags(TagsNF.FRUIT, TagsNF.GRAIN);
        tag(TagsNF.CARNIVORE_FOOD).addTags(TagsNF.MEAT, TagsNF.EGG);
        tag(TagsNF.CARNIVORE_FOOD).add(ItemsNF.CURED_JELLYFISH.get(), ItemsNF.BOILED_EGG.get(), ItemsNF.SOUFFLE.get());
        tag(TagsNF.OMNIVORE_FOOD).addTags(TagsNF.HERBIVORE_FOOD, TagsNF.CARNIVORE_FOOD);
        tag(TagsNF.OMNIVORE_FOOD).add(ItemsNF.FRUIT_SOUFFLE.get(), ItemsNF.SAVORY_SOUFFLE.get());
        tag(TagsNF.OMNIVORE_SEEDS_FOOD).addTags(TagsNF.OMNIVORE_FOOD, Tags.Items.SEEDS);

        copy(TagsNF.HEAT_RESISTANT_1, TagsNF.HEAT_RESISTANT_ITEM_1);
        copy(TagsNF.HEAT_RESISTANT_2, TagsNF.HEAT_RESISTANT_ITEM_2);
        copy(TagsNF.HEAT_RESISTANT_3, TagsNF.HEAT_RESISTANT_ITEM_3);
        copy(TagsNF.HEAT_RESISTANT_4, TagsNF.HEAT_RESISTANT_ITEM_4);
        copy(TagsNF.WOODEN_HATCHES, TagsNF.WOODEN_HATCHES_ITEM);
        copy(TagsNF.WOODEN_LADDERS, TagsNF.WOODEN_LADDERS_ITEM);
        copy(TagsNF.WOODEN_CHESTS, TagsNF.WOODEN_CHESTS_ITEM);
        copy(TagsNF.WOODEN_RACKS, TagsNF.WOODEN_RACKS_ITEM);
        copy(TagsNF.WOODEN_SHELVES, TagsNF.WOODEN_SHELVES_ITEM);
        copy(TagsNF.WOODEN_BARRELS, TagsNF.WOODEN_BARRELS_ITEM);
        copy(TagsNF.WOODEN_FENCE_GATES, TagsNF.WOODEN_FENCE_GATES_ITEM);
        copy(TagsNF.CHAIRS, TagsNF.CHAIRS_ITEM);
        copy(TagsNF.TROUGHS, TagsNF.TROUGHS_ITEM);
        copy(TagsNF.ITEM_FRAMES, TagsNF.ITEM_FRAMES_ITEM);
        copy(TagsNF.ANVILS, TagsNF.ANVILS_ITEM);
        copy(TagsNF.METAL_ANVILS, TagsNF.METAL_ANVILS_ITEM);
        copy(TagsNF.METAL_BLOCKS, TagsNF.METAL_BLOCKS_ITEM);

        for(var item : ItemsNF.INGOTS.values()) tag(Tags.Items.INGOTS).add(item.get());
        for(var item : ItemsNF.WIRES.values()) tag(TagsNF.WIRES).add(item.get());
        for(var item : ItemsNF.PLATES.values()) tag(TagsNF.PLATES).add(item.get());
        for(var item : ItemsNF.CHAINMAIL.values()) tag(TagsNF.CHAINMAIL).add(item.get());
        for(var item : ItemsNF.SCALES.values()) tag(TagsNF.SCALES).add(item.get());
        for(var item : ItemsNF.BILLETS.values()) tag(TagsNF.BILLETS).add(item.get());
        tag(TagsNF.CRUCIBLE_METAL).addTags(Tags.Items.INGOTS, TagsNF.METAL_CHUNKS);
        tag(TagsNF.METAL_CHUNKS).add(ItemsNF.COPPER_CHUNK.get(), ItemsNF.AZURITE_CHUNK.get(), ItemsNF.TIN_CHUNK.get(), ItemsNF.HEMATITE_CHUNK.get(), ItemsNF.METEORITE_CHUNK.get());
        tag(TagsNF.METAL_WORKPIECE).addTags(Tags.Items.INGOTS, TagsNF.NATIVE_METAL, TagsNF.PLATES);
        tag(TagsNF.METAL_WORKPIECE).add(ItemsNF.IRON_BLOOM.get());
        tag(TagsNF.NATIVE_METAL).add(ItemsNF.COPPER_CHUNK.get(), ItemsNF.METEORITE_CHUNK.get(), ItemsNF.TIN_CHUNK.get());
        tag(TagsNF.NATIVE_METAL_INGOT).add(ItemsNF.INGOTS.get(Metal.COPPER).get(), ItemsNF.INGOTS.get(Metal.METEORITE).get(), ItemsNF.INGOTS.get(Metal.TIN).get());
        tag(TagsNF.IRON_ORE).add(ItemsNF.HEMATITE_CHUNK.get());
        tag(TagsNF.CORROSION_RESISTANT_METAL).add(ItemsNF.INGOTS.get(Metal.BRONZE).get(), ItemsNF.WIRES.get(Metal.BRONZE).get(), ItemsNF.PLATES.get(Metal.BRONZE).get(),
                ItemsNF.CHAINMAIL.get(Metal.BRONZE).get(), ItemsNF.SCALES.get(Metal.BRONZE).get(), ItemsNF.BILLETS.get(Metal.BRONZE).get());
        tag(TagsNF.HEAT_RESISTANT_MATERIAL_1).add(ItemsNF.TERRACOTTA_SHARD.get(), ItemsNF.MUD_BRICK.get());
        tag(TagsNF.HEAT_RESISTANT_MATERIAL_2).add(ItemsNF.BRICK.get());
        tag(TagsNF.HEAT_RESISTANT_MATERIAL_3).add(ItemsNF.FIRE_BRICK.get());
        tag(TagsNF.HEAT_RESISTANT_MATERIAL_1).addTag(TagsNF.HEAT_RESISTANT_MATERIAL_2);
        tag(TagsNF.HEAT_RESISTANT_MATERIAL_2).addTag(TagsNF.HEAT_RESISTANT_MATERIAL_3);
        tag(TagsNF.LINEN_OR_ARMOR).add(ItemsNF.LINEN.get(), ItemsNF.HELMETS.get(TieredArmorMaterial.PADDED).get(), ItemsNF.CHESTPLATES.get(TieredArmorMaterial.PADDED).get(),
                ItemsNF.LEGGINGS.get(TieredArmorMaterial.PADDED).get(), ItemsNF.BOOTS.get(TieredArmorMaterial.PADDED).get());
        tag(TagsNF.LEATHER_OR_ARMOR).add(ItemsNF.LEATHER.get(), ItemsNF.HELMETS.get(TieredArmorMaterial.LEATHER).get(), ItemsNF.CHESTPLATES.get(TieredArmorMaterial.LEATHER).get(),
                ItemsNF.LEGGINGS.get(TieredArmorMaterial.LEATHER).get(), ItemsNF.BOOTS.get(TieredArmorMaterial.LEATHER).get());
        tag(TagsNF.MACE_PUZZLE_ITEM).addTags(TagsNF.MACE, TagsNF.MACE_HEAD);
        tag(TagsNF.MACE_PUZZLE_ITEM).add(ItemsNF.WOODEN_CLUB.get());
        tag(TagsNF.SURVIVOR_PLATE).addTags(TagsNF.SURVIVOR_PLATE_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.SURVIVOR_SCALE).addTags(TagsNF.SURVIVOR_SCALE_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.SURVIVOR_CHAINMAIL).addTags(TagsNF.SURVIVOR_CHAINMAIL_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.EXPLORER_PLATE).addTags(TagsNF.EXPLORER_PLATE_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.EXPLORER_SCALE).addTags(TagsNF.EXPLORER_SCALE_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.EXPLORER_CHAINMAIL).addTags(TagsNF.EXPLORER_CHAINMAIL_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.SLAYER_PLATE).addTags(TagsNF.SLAYER_PLATE_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.SLAYER_SCALE).addTags(TagsNF.SLAYER_SCALE_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.SLAYER_CHAINMAIL).addTags(TagsNF.SLAYER_CHAINMAIL_PIECES.values().toArray(TagKey[]::new));
        for(var item : ItemsNF.getTieredArmors()) {
            ArmorType type = item.get().material.getArmorType();
            if(type == null) continue;
            Style style = (Style) item.get().material.getStyle();
            EquipmentSlot slot = item.get().slot;
            switch(style) {
                case SURVIVOR -> {
                    switch(type) {
                        case PLATE -> tag(TagsNF.SURVIVOR_PLATE_PIECES.get(slot)).add(item.get());
                        case SCALE -> tag(TagsNF.SURVIVOR_SCALE_PIECES.get(slot)).add(item.get());
                        case CHAINMAIL -> tag(TagsNF.SURVIVOR_CHAINMAIL_PIECES.get(slot)).add(item.get());
                    }
                }
                case EXPLORER -> {
                    switch(type) {
                        case PLATE -> tag(TagsNF.EXPLORER_PLATE_PIECES.get(slot)).add(item.get());
                        case SCALE -> tag(TagsNF.EXPLORER_SCALE_PIECES.get(slot)).add(item.get());
                        case CHAINMAIL -> tag(TagsNF.EXPLORER_CHAINMAIL_PIECES.get(slot)).add(item.get());
                    }
                }
                case SLAYER -> {
                    switch(type) {
                        case PLATE -> tag(TagsNF.SLAYER_PLATE_PIECES.get(slot)).add(item.get());
                        case SCALE -> tag(TagsNF.SLAYER_SCALE_PIECES.get(slot)).add(item.get());
                        case CHAINMAIL -> tag(TagsNF.SLAYER_CHAINMAIL_PIECES.get(slot)).add(item.get());
                    }
                }
            }
        }

        tag(TagsNF.RECIPE_GROUP).addTags(ItemTags.WOODEN_DOORS, ItemTags.WOODEN_TRAPDOORS, Tags.Items.CHESTS, TagsNF.WOODEN_CHESTS_ITEM, TagsNF.ARMOR_STAND,
                TagsNF.ROCK, TagsNF.PLANK, Tags.Items.INGOTS, TagsNF.ARROWHEAD, TagsNF.WOODEN_HATCHES_ITEM, TagsNF.WOODEN_LADDERS_ITEM, TagsNF.WOODEN_RACKS_ITEM,
                TagsNF.WOODEN_SHELVES_ITEM, TagsNF.WOODEN_BARRELS_ITEM, ItemTags.WOODEN_FENCES, TagsNF.WOODEN_FENCE_GATES_ITEM, TagsNF.ANVILS_ITEM, TagsNF.METAL_ANVILS_ITEM,
                TagsNF.METAL_BLOCKS_ITEM, TagsNF.WIRES, TagsNF.PLATES, TagsNF.CHAINMAIL, TagsNF.SCALES, TagsNF.BILLETS, TagsNF.ADZE_HEAD, TagsNF.AXE_HEAD, TagsNF.CHISEL_HEAD,
                TagsNF.KNIFE_HEAD, TagsNF.MACE_HEAD, TagsNF.PICKAXE_HEAD, TagsNF.SABRE_HEAD, TagsNF.SHOVEL_HEAD, TagsNF.SICKLE_HEAD, TagsNF.SPEAR_HEAD,
                TagsNF.SWORD_HEAD, TagsNF.ADZE, TagsNF.AXE, TagsNF.CHISEL, TagsNF.KNIFE, TagsNF.HAMMER, TagsNF.MACE, TagsNF.PICKAXE, TagsNF.SABRE, TagsNF.SICKLE, TagsNF.SHOVEL,
                TagsNF.SPEAR, TagsNF.SWORD, TagsNF.BOW, TagsNF.SHIELD, TagsNF.WOODEN_BOW, TagsNF.UNDYED_METAL_SHIELD, TagsNF.NATIVE_METAL, TagsNF.NATIVE_METAL_INGOT,
                TagsNF.LUMBER_TANNIN, TagsNF.CURABLE_FOOD, TagsNF.CURED_MEAT, ItemTags.SIGNS, TagsNF.CHAIRS_ITEM, TagsNF.TROUGHS_ITEM, TagsNF.ITEM_FRAMES_ITEM, ItemTags.BOATS,
                TagsNF.FLINT_ARMAMENT_HEAD, TagsNF.METAL_ARROW);
        tag(TagsNF.RECIPE_GROUP).addTags(TagsNF.SURVIVOR_PLATE_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.RECIPE_GROUP).addTags(TagsNF.SURVIVOR_SCALE_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.RECIPE_GROUP).addTags(TagsNF.SURVIVOR_CHAINMAIL_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.RECIPE_GROUP).addTags(TagsNF.EXPLORER_PLATE_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.RECIPE_GROUP).addTags(TagsNF.EXPLORER_SCALE_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.RECIPE_GROUP).addTags(TagsNF.EXPLORER_CHAINMAIL_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.RECIPE_GROUP).addTags(TagsNF.SLAYER_PLATE_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.RECIPE_GROUP).addTags(TagsNF.SLAYER_SCALE_PIECES.values().toArray(TagKey[]::new));
        tag(TagsNF.RECIPE_GROUP).addTags(TagsNF.SLAYER_CHAINMAIL_PIECES.values().toArray(TagKey[]::new));
        for(var item : ItemsNF.ARMOR_STANDS.values()) tag(TagsNF.ARMOR_STAND).add(item.get());
        for(var item : ItemsNF.BOATS.values()) tag(ItemTags.BOATS).add(item.get());
    }

    @Override
    protected void copy(TagKey<Block> pBlockTag, TagKey<Item> pItemTag) {
        Tag.Builder itemTagBuilder = getOrCreateRawBuilder(pItemTag);
        Tag.Builder blockTagBuilder = blockTags.apply(pBlockTag);
        blockTagBuilder.getEntries().forEach((entry) -> {
            String name = entry.entry().toString();
            if(name.charAt(0) == '#' || (ResourceLocation.isValidResourceLocation(name) && ForgeRegistries.ITEMS.containsKey(ResourceLocation.parse(name)))) {
                itemTagBuilder.add(entry);
            }
        });
    }
}
