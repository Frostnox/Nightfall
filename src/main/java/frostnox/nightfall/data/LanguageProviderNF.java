package frostnox.nightfall.data;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.player.PlayerActionSet;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.CategoryToast;
import frostnox.nightfall.client.gui.EntryToast;
import frostnox.nightfall.client.gui.screen.container.CrucibleScreen;
import frostnox.nightfall.client.gui.screen.encyclopedia.EncyclopediaScreen;
import frostnox.nightfall.client.gui.screen.encyclopedia.EntryPuzzleScreen;
import frostnox.nightfall.data.recipe.CauldronRecipe;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.entity.PlayerAttribute;
import frostnox.nightfall.item.Armament;
import frostnox.nightfall.item.TieredArmorMaterial;
import frostnox.nightfall.network.command.*;
import frostnox.nightfall.network.command.argument.EntryArgument;
import frostnox.nightfall.network.command.argument.KnowledgeArgument;
import frostnox.nightfall.registry.KnowledgeNF;
import frostnox.nightfall.world.ToolActionsNF;
import frostnox.nightfall.world.inventory.AccessorySlot;
import frostnox.nightfall.registry.EntriesNF;
import frostnox.nightfall.registry.forge.*;
import frostnox.nightfall.util.DataUtil;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.text.WordUtils;

import java.util.HashSet;
import java.util.Set;

public class LanguageProviderNF extends LanguageProvider {
    protected final Set<Object> addedObjects = new HashSet<>();

    public LanguageProviderNF(DataGenerator gen, String id, String locale) {
        super(gen, id, locale);
    }

    @Override
    public void add(Item key, String name) {
        super.add(key, name);
        addedObjects.add(key);
    }

    @Override
    public void add(EntityType<?> key, String name) {
        super.add(key, name);
        addedObjects.add(key);
    }

    @Override
    public void add(MobEffect key, String name) {
        super.add(key, name);
        addedObjects.add(key);
    }

    protected void addAttack(String name, String text) {
        add("death.attack." + name, text);
    }

    protected void addAttackItem(DamageType type, String text) {
        add("death.attack." + type.toString(), text);
        add("death.attack." + type.toString() + ".item", text + " using %3$s");
    }

    protected void addFluid(RegistryObject<? extends Fluid> fluid, String text) {
        add(fluid.get().getAttributes().getTranslationKey(), text);
    }

    protected void addEntry(RegistryObject<? extends Entry> entry, String title) {
        add(entry.get().getDescriptionId(), title);
        addedObjects.add(entry.get());
    }

    protected void addItemClue(RegistryObject<? extends Entry> entry, int index, String text) {
        add("entry." + entry.getId().toString().replace(":", ".") + ".item_" + index, text);
    }

    protected void addKnowledgeClue(RegistryObject<? extends Entry> entry, int index, String text) {
        add("entry." + entry.getId().toString().replace(":", ".") + ".knowledge_" + index, text);
    }

    protected void addConditionClue(RegistryObject<? extends Entry> entry, int index, String text) {
        add("entry." + entry.getId().toString().replace(":", ".") + ".condition_" + index, text);
    }

    protected void addItemClues(RegistryObject<? extends Entry> entry, String... text) {
        for(int i = 0; i < text.length; i++) addItemClue(entry, i, text[i]);
    }

    protected void addKnowledgeClues(RegistryObject<? extends Entry> entry, String... text) {
        for(int i = 0; i < text.length; i++) addKnowledgeClue(entry, i, text[i]);
    }

    protected void addConditionClues(RegistryObject<? extends Entry> entry, String... text) {
        for(int i = 0; i < text.length; i++) addConditionClue(entry, i, text[i]);
    }

    protected void addSound(RegistryObject<? extends SoundEvent> sound, String text) {
        add(sound.getId().getNamespace() + "." + sound.getId().getPath(), text);
    }

    protected void addSound(SoundEvent sound, String text) {
        add(sound.getRegistryName().getNamespace() + "." + sound.getRegistryName().getPath(), text);
    }

    protected void addComponent(TranslatableComponent component, String text) {
        add(component.getKey(), text);
    }

    @Override
    protected void addTranslations() {
        add("selectWorld.mapFeatures.info", "");
        add("selectWorld.gameMode.survival.line1", "Search for resources, craft,");
        add("selectWorld.gameMode.survival.line2", "and manage health and hunger");
        add("selectWorld.gameMode.hardcore.line1", "Same as Survival Mode, but");
        add("selectWorld.gameMode.hardcore.line2", "death is permanent");
        add("selectWorld.gameMode.creative.line1", "Unlimited resources, free flying,");
        add("selectWorld.gameMode.creative.line2", "and destroy blocks instantly");
        add("selectWorld.allowCommands.info", "Commands like /gamemode");
        add("nightfall.message.controls_command", "Thank you for playing Nightfall. Type '/nightfall controls' for info on the mod's controls.");
        add("commands.info.encyclopedia", "Open your encyclopedia with '%s'.");
        add("commands.info.dash", "Perform a directional dash with '%s'.");
        add("commands.info.crawl", "Start crawling by double-tapping the sneak key '%s'.");
        add("commands.info.climb", "Start climbing by holding the jump key '%s' against a ledge.");
        add("commands.info.prioritize_offhand", "Hold '%s' to attack/use offhand items.");
        add("commands.info.modify_item_behavior", "Hold '%s' with certain items (such as building materials) to modify their use behavior.");

        add("debug.error", "Cannot execute command in %s mode");
        add("season.spring", "Spring");
        add("season.summer", "Summer");
        add("season.fall", "Fall");
        add("season.winter", "Winter");
        add("weather.clear", "Clear");
        add("weather.clouds", "Clouds");
        add("weather.rain", "Rain");
        add("weather.snow", "Snow");
        add("weather.fog", "Fog");
        add("action.basic", "Basic");
        add("action.alternate", "Alternate");
        add("action.technique", "Technique");
        add("action.basic_paired", "Paired Basic");
        add("action.alternate_paired", "Paired Alternate");
        add("action.guard", "Guard");
        add("action.throw", "Throw");
        add("action.upset", "Upset");
        add("action.riposte", "Riposte");
        add("action.wrap", "Wrap");
        add("action.control", " (%s)");
        add("action.control_held", " (Hold %s)");
        add("block.holdable", "Carriable");
        add("block.holdable.control", "(Crouch and use with empty hand)");
        add("block.fireable", "Fires at ");
        add("block.capacity", "%s Capacity");
        add("block.crucible.pour", "Carry and use to pour fluids");
        add("block.warding_effigy.info", "Prevents nearby Undead occurrence");
        add("item.on_use", "On Use: ");
        add("item.toolactions", "Can perform ");
        add("item.integrity", "%s Integrity");
        add("item.dig_speed", "%s%% Dig Speed");
        add("item.nutrition", "%s Nutrition");
        add("item.low_saturation", "Scarce Meal");
        add("item.medium_saturation", "Fair Meal");
        add("item.high_saturation", "Ample Meal");
        add("item.pair", "Pairs with ");
        add("item.durability_penalty", "%s%% Durability Penalty");
        add("item.armor.style.survivor", "+10% Durability");
        add("item.armor.negation", "Negation");
        add("action.guard.block", "Blocks %1$s %2$s");
        add("action.guard.riposte_1", "Perform basic after successful");
        add("action.guard.riposte_2", "block to trigger riposte");
        add("action.fiber_bandage_use.info", "Cures 30s of Bleeding");
        add("action.bandage_use.info_0", "Cures Bleeding");
        add("action.bandage_use.info_1", "+10 Health");
        add("action.medicinal_bandage_use.info", "+40 Regenerating Health");
        add("tooltip.expand_prompt", "[Hold shift for more]");
        add("toolaction." + ToolActionsNF.REFINE.name(), WordUtils.capitalize(ToolActionsNF.REFINE.name()));
        add("toolaction." + ToolActionsNF.STRIP.name(), WordUtils.capitalize(ToolActionsNF.STRIP.name()));
        add("toolaction." + ToolActionsNF.TILL.name(), WordUtils.capitalize(ToolActionsNF.TILL.name()));
        add("toolaction." + ToolActionsNF.SKIN.name(), WordUtils.capitalize(ToolActionsNF.SKIN.name()));
        add("anvil.action.context", "Smithing: ");
        add("anvil.action.context_charged", "Smithing (Charged): ");
        add("anvil.action.punch.info", "Punch 1x1 area");
        add("anvil.action.punch_line.info", "Punch 3x1 area");
        add("anvil.action.punch_square.info", "Punch 3x3 area");
        add("anvil.action.bend.info", "Shifts line backwards");
        add("anvil.action.draw.info", "Draws 3x1 edge forwards");
        add("anvil.action.draw_line.info", "Draws entire edge forwards");
        add("anvil.action.upset.info", "Upsets 3x1 edge backwards");
        add("anvil.action.upset_line.info", "Upsets entire edge backwards");
        for(PlayerActionSet set : PlayerActionSet.SETS) {
            add("action_set." + set.toString(), WordUtils.capitalize(set.toString().replace("_", " ")));
        }
        for(TieredHeat heat : TieredHeat.values()) {
            add("heat.tier." + heat.getTier(), "Heat Tier " + heat.getTier());
            add("block.heat_resistant." + heat.getTier(), "Heat Resistant " + heat.getTier());
        }
        add("item.nightfall.mask.info", "Hides name");
        add("item.nightfall.warding_charm.info", "Reduces nearby Undead occurrence");
        add(Nightfall.MODID + ".anvil", "Anvil");
        add(Nightfall.MODID + ".bowl_crushing", "Bowl");
        add(Nightfall.MODID + ".building", "Building");
        add(Nightfall.MODID + ".campfire", "Campfire");
        add(Nightfall.MODID + ".cauldron", "Cauldron");
        add(Nightfall.MODID + ".crucible", "Crucible");
        add(Nightfall.MODID + ".held_tool", "Held Tool");
        add(Nightfall.MODID + ".soaking", "Soaking");
        addComponent(CauldronRecipe.UNIT, "part");
        addComponent(CauldronRecipe.UNITS, "parts");
        add(CauldronRecipe.MIN_PHRASE, "At least %s ");
        add(CauldronRecipe.MAX_PHRASE, "At most %s ");
        add(CauldronRecipe.RANGE_PHRASE, "%1$s to %2$s ");
        for(Metal metal : Metal.values()) add("metal." + metal.getName(), WordUtils.capitalize(metal.getName().replace("_", " ")));
        add("food.small", "Small");
        add("food.medium", "Medium");
        add("food.large", "Large");

        add(EntryCommand.FORGET_SELF, "Forgot entry %s");
        add(EntryCommand.FORGET, "You forgot entry %s");
        add(EntryCommand.FORGET_OTHER, "%s forgot entry %s");
        addComponent(EntryCommand.FORGET_ALL_SELF, "Forgot all entries");
        addComponent(EntryCommand.FORGET_ALL, "You forgot all entries");
        add(EntryCommand.FORGET_ALL_OTHER, "%s forgot all entries");
        add(EntryCommand.LEARN_SELF, "Learned entry %s");
        add(EntryCommand.LEARN, "You learned entry %s");
        add(EntryCommand.LEARN_OTHER, "%s learned entry %s");
        addComponent(EntryCommand.LEARN_ALL_SELF, "Learned all entries");
        addComponent(EntryCommand.LEARN_ALL, "You learned all entries");
        add(EntryCommand.LEARN_ALL_OTHER, "%s learned all entries");
        add(EntryArgument.INVALID, "Invalid entry: %s");
        add(KnowledgeCommand.FORGET_SELF, "Forgot knowledge %s");
        add(KnowledgeCommand.FORGET, "You forgot knowledge %s");
        add(KnowledgeCommand.FORGET_OTHER, "%s forgot knowledge %s");
        addComponent(KnowledgeCommand.FORGET_ALL_SELF, "Forgot all knowledge");
        addComponent(KnowledgeCommand.FORGET_ALL, "You forgot all knowledge");
        add(KnowledgeCommand.FORGET_ALL_OTHER, "%s forgot all knowledge");
        add(KnowledgeCommand.LEARN_SELF, "Learned knowledge %s");
        add(KnowledgeCommand.LEARN, "You learned knowledge %s");
        add(KnowledgeCommand.LEARN_OTHER, "%s learned knowledge %s");
        addComponent(KnowledgeCommand.LEARN_ALL_SELF, "Learned all knowledge");
        addComponent(KnowledgeCommand.LEARN_ALL, "You learned all knowledge");
        add(KnowledgeCommand.LEARN_ALL_OTHER, "%s learned all knowledge");
        add(KnowledgeArgument.INVALID, "Invalid knowledge: %s");
        addComponent(GodModeCommand.ENABLE, "Godmode enabled");
        addComponent(GodModeCommand.DISABLE, "Godmode disabled");
        add(GodModeCommand.ENABLE_OTHER, "Godmode enabled for %s");
        add(GodModeCommand.DISABLE_OTHER, "Godmode disabled for %s");
        addComponent(ReselectAttributesCommand.RESELECT, "Reselecting attributes...");
        add(ReselectAttributesCommand.RESELECT_OTHER, "Prompted %s to reselect attributes");
        add(SeasonCommand.QUERY, "The season is %s");
        add(SeasonCommand.SET, "Set the season to %s");
        add(WeatherCommandNF.QUERY, "The weather is %1$s with global intensity %2$s (%3$s moving to %4$s)");
        add(WeatherCommandNF.SET, "Set the weather to %1$s with global intensity %2$s");

        add(ClientEngine.get().keyDash.getName(), "Dash");
        add(ClientEngine.get().keyOffhand.getName(), "Prioritize Offhand Interactions");
        add(ClientEngine.get().keyEncyclopedia.getName(), "Open/Close Encyclopedia");
        add(ClientEngine.get().keyModify.getName(), "Modify Item Behavior");
        add(ClientEngine.get().WYLDERY.name(), "Wyldery");
        add(ClientEngine.get().METALLURGY.name(), "Metallurgy");
        add("item.emits_light", "Emits light");
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            add(slot.getName(), WordUtils.capitalize(slot.getName()));
        }
        for(AccessorySlot slot : AccessorySlot.values()) {
            add(slot.toString(), WordUtils.capitalize(slot.toString()));
            add("item.modifiers." + slot, "When on " + WordUtils.capitalize(slot.toString()) + ":");
        }
        addComponent(RenderUtil.ATTRIBUTES_TEXT, "Attributes");
        addComponent(RenderUtil.RESISTANCES_TEXT, "Resistances");
        add("screen.defenses.info_0", "Resistances are displayed as 'Defense/Absorption'");
        add("screen.defenses.info_1", "Defense reduces damage directly");
        add("screen.defenses.info_2", "Absorption reduces damage by percentage");
        addComponent(RenderUtil.EFFECT_DEFENSE_TEXT, "Effects: ");
        addComponent(RenderUtil.ARMOR_DEFENSE_TEXT, "Armor average: ");
        addComponent(RenderUtil.FREE_POINTS_TEXT, "Free points: ");
        for(var component : RenderUtil.FOOD_GROUPS_TEXT) {
            addComponent(component, WordUtils.capitalize(component.getKey().substring(component.getKey().lastIndexOf('.') + 1)));
        }
        for(PlayerAttribute attribute : PlayerAttribute.values()) {
            if(attribute == PlayerAttribute.WILLPOWER) addComponent(RenderUtil.getAttributeText(attribute), "???");
            else addComponent(RenderUtil.getAttributeText(attribute), WordUtils.capitalize(attribute.toString()));
        }
        add("screen." + PlayerAttribute.VITALITY + ".info", "%s health");
        add("screen." + PlayerAttribute.ENDURANCE + ".info", "%1$s stamina / %2$s%% regeneration");
        add("screen." + PlayerAttribute.WILLPOWER + ".info", "???");
        add("screen." + PlayerAttribute.STRENGTH + ".info", "%s%% tool & weapon efficiency");
        add("screen." + PlayerAttribute.AGILITY + ".info", "%s%% movement speed");
        add("screen." + PlayerAttribute.PERCEPTION + ".info", "Influences rare item discovery");
        add("screen.finalize", "Finalize");
        for(DamageType type : DamageType.values()) {
            addComponent(RenderUtil.getDamageTypeText(type), WordUtils.capitalize(type.toString()));
        }
        for(var attribute : AttributesNF.ATTRIBUTES.getEntries()) {
            if(attribute == AttributesNF.STAMINA_REDUCTION) add(attribute.get().getDescriptionId(), "Stamina Used");
            else add(attribute.get().getDescriptionId(), DataUtil.getLocalName(attribute.getId().getPath()));
        }
        addComponent(EncyclopediaScreen.LOCKED_ENTRY, "???");
        addComponent(EntryPuzzleScreen.MISSING_KNOWLEDGE, "Knowledge requirements not met");
        addComponent(EntryPuzzleScreen.HIDDEN_KNOWLEDGE, "???");
        addComponent(CrucibleScreen.SOLID, "Solid");
        addComponent(CrucibleScreen.MOLTEN, "Molten");
        addComponent(CategoryToast.PREFIX, "Discovered ");
        addComponent(EntryToast.PREFIX, "Discovered ");
        addComponent(EntryToast.PREFIX_ADDENDUM, "Revised ");
        addComponent(EntryToast.PREFIX_HIDDEN, "Uncovered ");
        add("container.nightfall.search", "Search...");
        add("itemGroup.nightfall.natural_blocks", "Natural Blocks");
        add("itemGroup.nightfall.functional_blocks", "Functional Blocks");
        add("itemGroup.nightfall.building_materials", "Building Materials");
        add("itemGroup.nightfall.ingredients", "Ingredients");
        add("itemGroup.nightfall.food", "Foodstuffs");
        add("itemGroup.nightfall.armaments", "Armaments");
        add("itemGroup.nightfall.armor", "Armor");
        add("itemGroup.nightfall.utilities", "Utilities");
        add("itemGroup.nightfall.consumables", "Consumables");
        add("generator.nightfall.continental", "Continental");
        add("generator.nightfall.flat", "Superflat");

        add("nightfall.block.fall", "Block falls");
        addSound(SoundsNF.SIZZLE, "Something sizzles");
        addSound(SoundsNF.ANVIL_STRIKE, "Tool strikes anvil piece");
        addSound(SoundsNF.WOODEN_BOWL_CRUSH, "Something crushes in wooden bowl");
        addSound(SoundsNF.CRUCIBLE_POUR, "Metal pours from crucible");
        addSound(SoundsNF.FIRE_CRACKLE, "Fire crackles");
        add("nightfall.block.ceramic.open", "Ceramic vessel opens");
        addSound(SoundsNF.SWING, "Weapon whooshes");
        addSound(SoundsNF.SWING_CHARGED, "Weapon whooshes forcefully");
        addSound(SoundsNF.BLADE_SWING, "Blade whooshes");
        addSound(SoundsNF.BLADE_SWING_CHARGED, "Blade whooshes forcefully");
        addSound(SoundsNF.SHORT_BLADE_SWING, "Short blade whooshes");
        addSound(SoundsNF.SHORT_BLADE_SWING_CHARGED, "Short blade whooshes forcefully");
        addSound(SoundsNF.LONG_BLADE_SWING, "Long blade whooshes");
        addSound(SoundsNF.LONG_BLADE_SWING_CHARGED, "Long blade whooshes forcefully");
        addSound(SoundsNF.HEAVY_BLADE_SWING, "Heavy weapon whooshes");
        addSound(SoundsNF.HEAVY_BLADE_SWING_CHARGED, "Heavy weapon whooshes forcefully");
        addSound(SoundsNF.HAMMER_CHISEL_HIT, "Hammer hits chisel");
        addSound(SoundsNF.HAMMER_FLINT_CHISEL_HIT, "Hammer hits flint chisel");
        addSound(SoundsNF.BANDAGE_HEAL, "Bandage tightens");
        addSound(SoundsNF.BOW_PULL, "Bow stretches");
        addSound(SoundsNF.BOW_SHOOT, "Arrow fires");
        addSound(SoundsNF.BOW_SHOOT_CHARGED, "Arrow launches");
        addSound(SoundsNF.WEAPON_BLOCK, "Weapon clashes");
        addSound(SoundsNF.SLASH_FLESH, "Something slashes flesh");
        addSound(SoundsNF.SLASH_ARMOR, "Something slashes armor");
        addSound(SoundsNF.SLASH_BONE, "Something slashes bone");
        addSound(SoundsNF.SLASH_STONE, "Something slashes stone");
        addSound(SoundsNF.PIERCE_FLESH, "Something pierces flesh");
        addSound(SoundsNF.PIERCE_ARMOR, "Something pierces armor");
        addSound(SoundsNF.PIERCE_BONE, "Something pierces bone");
        addSound(SoundsNF.PIERCE_STONE, "Something pierces stone");
        addSound(SoundsNF.STRIKE_FLESH, "Something strikes flesh");
        addSound(SoundsNF.STRIKE_ARMOR, "Something strikes armor");
        addSound(SoundsNF.STRIKE_BONE, "Something strikes bone");
        addSound(SoundsNF.STRIKE_STONE, "Something strikes stone");
        addSound(SoundsNF.BLEEDING_HIT, "Bleeding hurts");
        addSound(SoundsNF.POISON_HIT, "Poison hurts");
        addSound(SoundsNF.TILL_SOIL, "Soil stirs");
        addSound(SoundsNF.STRIP_WOOD, "Bark peels");
        addSound(SoundsNF.SKIN_FLESH, "Skin peels");
        addSound(SoundsNF.DEER_DEATH, "Deer dies");
        addSound(SoundsNF.DEER_HURT, "Deer whines");
        addSound(SoundsNF.DEER_STEP, "Deer steps");
        addSound(SoundsNF.DREG_AMBIENT, "Dreg breathes");
        addSound(SoundsNF.DREG_DEATH, "Dreg collapses");
        addSound(SoundsNF.DREG_HURT, "Dreg wheezes");
        addSound(SoundsNF.DREG_STEP, "Dreg shambles");
        addSound(SoundsNF.DREG_RESURRECT, "Dreg resurrects undead");
        addSound(SoundsNF.HUSK_AMBIENT, "Husk groans");
        addSound(SoundsNF.HUSK_DEATH, "Husk collapses");
        addSound(SoundsNF.HUSK_HURT, "Husk grunts");
        addSound(SoundsNF.HUSK_STEP, "Husk shambles");
        addSound(SoundsNF.HUSK_SWING, "Husk swings");
        addSound(SoundsNF.SKELETON_AMBIENT, "Skeleton creaks");
        addSound(SoundsNF.SKELETON_DEATH, "Skeleton collapses");
        addSound(SoundsNF.SKELETON_HURT, "Skeleton grates");
        addSound(SoundsNF.SKELETON_STEP, "Skeleton shambles");
        addSound(SoundsNF.SKELETON_THRUST, "Skeleton thrusts");
        addSound(SoundsNF.SKELETON_BOW_PULL, "Skeleton pulls bow");
        addSound(SoundsNF.SKELETON_BOW_SHOOT, "Skeleton shoots bow");
        addSound(SoundsNF.COCKATRICE_AMBIENT, "Cockatrice clucks");
        addSound(SoundsNF.COCKATRICE_DEATH, "Cockatrice dies");
        addSound(SoundsNF.COCKATRICE_HURT, "Cockatrice screeches");
        addSound(SoundsNF.COCKATRICE_STEP, "Cockatrice steps");
        addSound(SoundsNF.COCKATRICE_FLAP, "Cockatrice flaps");
        addSound(SoundsNF.COCKATRICE_BITE, "Cockatrice bites");
        addSound(SoundsNF.COCKATRICE_SPIT, "Cockatrice spits");
        addSound(SoundsNF.SPIDER_BITE, "Spider bites");
        addSound(SoundsNF.PROJECTILE_POISON_IMPACT, "Poison corrodes");
        addSound(SoundsNF.PROJECTILE_ROCK_IMPACT, "Rock collides");
        addSound(SoundsNF.PLAYER_WARP, "Someone warps");
        addSound(SoundsNF.UNDEAD_WARP, "Undead warps");
        addSound(SoundsNF.LOG_FALL, "Log falls");
        addSound(SoundsNF.BIG_TREE_FALL, "Big tree falls");
        addSound(SoundsNF.SMALL_TREE_FALL, "Small tree falls");
        addSound(SoundsNF.FIRE_WHOOSH, "Fire flares");
        addSound(SoundsNF.LIGHT_ARMOR_EQUIP, "Armor rustles");
        addSound(SoundsNF.SCALE_ARMOR_EQUIP, "Scale armor clatters");
        addSound(SoundsNF.PLATE_ARMOR_EQUIP, "Plate armor clanks");
        addSound(SoundsNF.CARVE_WOOD, "Wood carves");
        addSound(SoundsNF.CARVE_STONE, "Stone carves");
        addSound(SoundsNF.STICK_FIRE_STRIKE, "Stick strikes");
        addSound(SoundsNF.QUENCH, "Hot item quenches");
        addSound(SoundsNF.METAL_BREAK, "Metal item breaks");

        add(ItemsNF.SNOWBALL_THROWABLE.get(), "Snowball");
        add(ItemsNF.COKE_BLOCK.get(), "Coke Block");
        add(ItemsNF.COAL_BLOCK.get(), "Coal Block");
        add(ItemsNF.CHARCOAL_BLOCK.get(), "Charcoal Block");
        add(ItemsNF.FIREWOOD_BLOCK.get(), "Firewood Block");
        add(ItemsNF.AZURITE_BLOCK.get(), "Azurite Block");
        add(ItemsNF.HEMATITE_BLOCK.get(), "Hematite Block");
        add(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.LEATHER).get(), "Leather Tunic");
        add(ItemsNF.LEGGINGS.get(TieredArmorMaterial.LEATHER).get(), "Leather Pants");
        add(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.PADDED).get(), "Padded Tunic");
        add(ItemsNF.LEGGINGS.get(TieredArmorMaterial.PADDED).get(), "Padded Pants");
        add(ItemsNF.HELMETS.get(TieredArmorMaterial.RAGGED).get(), "Ragged Headwraps");
        add(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.RAGGED).get(), "Ragged Tunic");
        add(ItemsNF.LEGGINGS.get(TieredArmorMaterial.RAGGED).get(), "Ragged Pants");
        add(ItemsNF.BOOTS.get(TieredArmorMaterial.RAGGED).get(), "Ragged Footwraps");
        add(ItemsNF.TORCH_UNLIT.get(), "Unlit Torch");
        for(var map : ItemsNF.ARMAMENT_HEADS.values()) {
            for(Armament armament : map.keySet()) {
                if(armament == Armament.DAGGER || armament == Armament.SABRE || armament == Armament.SICKLE || armament == Armament.SWORD) {
                    add(map.get(armament).get(), WordUtils.capitalize(map.get(armament).getId().getPath()
                            .replace("_head", " blade").replace("_", " ")));
                }
            }
        }
        add(ItemsNF.IRONWOOD_SHIELD_DYED.get(), "Ironwood Shield");
        for(var item : ItemsNF.METAL_BLOCKS.values()) add(item.get(), WordUtils.capitalize(item.getId().getPath().replace("_", " ")));
        for(var item : ItemsNF.METAL_SHIELDS_DYED.values()) add(item.get(),
                WordUtils.capitalize(item.getId().getPath().replace("_dyed", "").replace("_", " ")));

        for(var fluid : FluidsNF.METAL.values()) addFluid(fluid, WordUtils.capitalize(fluid.getId().getPath().replace("_", " ")));

        addAttackItem(DamageType.STRIKING, "%1$s was crushed by %2$s");
        addAttackItem(DamageType.SLASHING, "%1$s was cut down by %2$s");
        addAttackItem(DamageType.PIERCING, "%1$s was impaled by %2$s");
        addAttackItem(DamageType.FIRE, "%1$s was incinerated by %2$s");
        addAttackItem(DamageType.FROST, "%1$s was frozen by %2$s");
        addAttackItem(DamageType.ELECTRIC, "%1$s was electrocuted by %2$s");
        addAttackItem(DamageType.ABSOLUTE, "%1$s was slain by %2$s");
        addAttack(DamageTypeSource.BLEEDING.getMsgId(), "%1$s bled out");
        addAttack(DamageTypeSource.POISON.getMsgId(), "%1$s succumbed to poison");
        addAttack("explosion", "%1$s was blown up");
        addAttack("explosion.entity", "%1$s was blown up by %2$s");
        addAttack("projectile", "%1$s was shot by %2$s");
        addAttack("bite", "%1$s was devoured by %2$s");
        addAttack("swipe", "%1$s was torn apart by %2$s");
        addAttack("maul", "%1$s was mauled by %2$s");

        addItemClues(EntriesNF.TOOLS, "A hard, brittle rock", "A wooden handle", "A binding");
        addItemClues(EntriesNF.SLING, "Twisted fibers", "A round projectile");
        addItemClues(EntriesNF.WOODCARVING, "Fallen timber");
        addItemClues(EntriesNF.WOODWORKING, "A split plank of clean wood");
        addItemClues(EntriesNF.ADVANCED_WOODWORKING, "A plank of soft wood", "A plank of hard wood", "A plank of wood neither hard nor soft");
        addItemClues(EntriesNF.WOODEN_SHIELD, "A plank of exceptional hardness");
        addItemClues(EntriesNF.TANNING, "An animal hide or pelt", "Clean water", "Purifying earths", "Seeping wood");
        addKnowledgeClues(EntriesNF.CAMPFIRE, "Use a pair of items to spark a fire");
        addItemClues(EntriesNF.CAMPFIRE, "A split piece of timber");
        addKnowledgeClues(EntriesNF.POTTERY, "Find wet clay");
        addItemClues(EntriesNF.POTTERY, "An earthy material resistant to at least tier 1 heat");
        addConditionClues(EntriesNF.POTTERY, "A nearby heat source");
        addKnowledgeClues(EntriesNF.COOKING, "Collect water", "Roast a meat", "Roast a vegetable");
        addItemClues(EntriesNF.WEAVING, "Hardy plant fibers");
        addItemClues(EntriesNF.MEDICINAL_BANDAGE, "A flowery herb, crushed");
        addItemClues(EntriesNF.BOW_AND_ARROW, "A plank of hard yet flexible wood", "An arrow fletching");
        addItemClues(EntriesNF.WARDING_CHARM, "A lingering presence... a desire to carve...");
        addItemClues(EntriesNF.WARDING_EFFIGY, "A structure...", "An essence...", "A body...");

        addKnowledgeClues(EntriesNF.CASTING, "Melt a strong metal");
        addKnowledgeClues(EntriesNF.SMITHING, "Melt a hard, mundane metal", "Create an improvised anvil from a sturdy block");
        addItemClues(EntriesNF.SMELTING, "A material resistant to at least tier 2 heat");
        addKnowledgeClues(EntriesNF.IRONWORKING, "Work an iron ore into an iron ingot");
        addItemClues(EntriesNF.SABRE, "A sabre");
        addItemClues(EntriesNF.SICKLE, "A sickle");
        addItemClues(EntriesNF.BUCKET, "A metal resistant to water corrosion");
        addConditionClues(EntriesNF.BUCKET, "A nearby water source");
        addItemClues(EntriesNF.PLATE_ARMOR, "A material to cushion rigid metal plates");
        addItemClues(EntriesNF.CHAINMAIL_ARMOR, "A metal easily worked by hand into chains");
        addItemClues(EntriesNF.SCALE_ARMOR, "An ideal metal shape to smith a number of scales from", "A tough material fit for attaching scales");
        addItemClues(EntriesNF.MACE, "An early prototype");
        addItemClues(EntriesNF.SHIELD, "A functioning wooden shield", "A metal rim");
        addItemClues(EntriesNF.SLAYER_PLATE, "A piece of slayer plate armor");
        addItemClues(EntriesNF.SLAYER_CHAINMAIL, "A piece of slayer chainmail armor");
        addItemClues(EntriesNF.SLAYER_SCALE, "A piece of slayer scale armor");
        addItemClues(EntriesNF.EXPLORER_PLATE, "A piece of explorer plate armor");
        addItemClues(EntriesNF.EXPLORER_CHAINMAIL, "A piece of explorer chainmail armor");
        addItemClues(EntriesNF.EXPLORER_SCALE, "A piece of explorer scale armor");

        for(RegistryObject<? extends Item> item : ItemsNF.ITEMS.getEntries()) {
            if(!addedObjects.contains(item.get())) {
                add(item.get().getDescriptionId(), WordUtils.capitalize(item.getId().getPath().replace("_block", "")
                        .replace("_", " ")));
            }
        }
        for(RegistryObject<? extends EntityType<?>> entity : EntitiesNF.ENTITIES.getEntries()) {
            if(!addedObjects.contains(entity.get())) {
                add(entity.get().getDescriptionId(), WordUtils.capitalize(entity.getId().getPath().replace("_", " ")));
            }
        }
        for(RegistryObject<? extends MobEffect> effect : EffectsNF.EFFECTS.getEntries()) {
            if(!addedObjects.contains(effect.get())) {
                add(effect.get().getDescriptionId(), WordUtils.capitalize(effect.getId().getPath().replace("_", " ")));
            }
        }
        for(RegistryObject<? extends Entry> entry : EntriesNF.ENTRIES.getEntries()) {
            if(!addedObjects.contains(entry.get())) {
                add(entry.get().getDescriptionId(), WordUtils.capitalize(entry.getId().getPath().replace("_", " ").replace(" and ", " & ")));
            }
        }
        for(RegistryObject<? extends Knowledge> knowledge : KnowledgeNF.KNOWLEDGE.getEntries()) {
            if(!addedObjects.contains(knowledge.get())) {
                add(knowledge.get().getDescriptionId(), WordUtils.capitalize(knowledge.getId().getPath().replace("_", " ")));
            }
        }
    }
}
