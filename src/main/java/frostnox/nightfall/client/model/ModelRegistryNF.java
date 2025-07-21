package frostnox.nightfall.client.model;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.render.blockentity.CauldronRenderer;
import frostnox.nightfall.client.render.blockentity.ChestRendererNF;
import frostnox.nightfall.client.model.entity.*;
import frostnox.nightfall.client.render.entity.BoatRendererNF;
import frostnox.nightfall.item.ArmorType;
import frostnox.nightfall.item.Style;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.item.ITieredArmorMaterial;
import frostnox.nightfall.item.TieredArmorMaterial;
import frostnox.nightfall.registry.forge.ItemsNF;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.EntityRenderersEvent;

import javax.annotation.Nullable;
import java.util.Map;

public class ModelRegistryNF {
    private static final Map<ModelLayerLocation, LayerDefinition> layers = new Object2ObjectOpenHashMap<>();
    private static final Map<ITieredArmorMaterial, ModelLayerLocation> materialMap = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, Pair<EntityPart, ModelLayerLocation>> equipmentMap = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, Pair<EntityPart, ModelLayerLocation>> accessoryMap = new Object2ObjectOpenHashMap<>();
    //Block entities
    public static ModelLayerLocation CHEST, DOUBLE_CHEST_LEFT, DOUBLE_CHEST_RIGHT;
    public static ModelLayerLocation SIGN;
    public static ModelLayerLocation CAULDRON;
    //Entities
    public static ModelLayerLocation ARMOR_STAND, BOAT;
    public static ModelLayerLocation PLAYER, PLAYER_SLIM;
    public static ModelLayerLocation RABBIT, DEER;
    public static ModelLayerLocation HUSK, SKELETON, DREG;
    public static ModelLayerLocation CREEPER;
    public static ModelLayerLocation COCKATRICE;
    public static ModelLayerLocation SPIDER;
    public static ModelLayerLocation ROCKWORM;
    public static ModelLayerLocation PIT_DEVIL;
    public static ModelLayerLocation SLIME;
    public static ModelLayerLocation SCORPION;
    public static ModelLayerLocation SCARAB;
    public static ModelLayerLocation TROLL;
    public static ModelLayerLocation OLMUR;
    public static ModelLayerLocation JELLYFISH_INNER, JELLYFISH_OUTER;
    //Armor
    public static ModelLayerLocation INNER_ARMOR, OUTER_ARMOR; //Vanilla
    public static ModelLayerLocation FLAT_ARMOR, TAPERED_ARMOR, SLIM_ARMOR;
    public static ModelLayerLocation PLATE_SURVIVOR;
    public static ModelLayerLocation PLATE_EXPLORER;
    public static ModelLayerLocation CHAINMAIL_EXPLORER;
    public static ModelLayerLocation PLATE_SLAYER;
    public static ModelLayerLocation SCALE_SLAYER;
    public static ModelLayerLocation CHAINMAIL_SLAYER;
    //Equipment
    public static ModelLayerLocation BACKPACK;
    //Accessories
    public static ModelLayerLocation POUCH, MASK, NECKLACE, LANTERN;

    public static void init(EntityRenderersEvent.RegisterLayerDefinitions event) {
        LayerDefinition innerArmor = LayerDefinition.create(ArmorModel.createVanillaMesh(new CubeDeformation(0.5F)), 64, 32);
        LayerDefinition outerArmor = LayerDefinition.create(ArmorModel.createVanillaMesh(new CubeDeformation(1F)), 64, 32);
        //Block entities
        CHEST = register("chest", ChestRendererNF.createSingleBodyLayer());
        DOUBLE_CHEST_LEFT = register("double_chest_left", ChestRendererNF.createDoubleBodyLeftLayer());
        DOUBLE_CHEST_RIGHT = register("double_chest_right", ChestRendererNF.createDoubleBodyRightLayer());
        SIGN = register("sign", SignRenderer.createSignLayer());
        CAULDRON = register("cauldron", CauldronRenderer.createLayer());
        //Entities
        ARMOR_STAND = register("armor_stand", ArmorStandDummyModel.createBodyLayer());
        BOAT = register("boat", BoatRendererNF.createModel());
        PLAYER = register("player_combat", LayerDefinition.create(PlayerModelNF.createMesh(CubeDeformation.NONE, false), 64, 64));
        PLAYER_SLIM = register("player_slim_combat", LayerDefinition.create(PlayerModelNF.createMesh(CubeDeformation.NONE, true), 64, 64));
        RABBIT = register("rabbit", RabbitModel.createBodyLayer());
        DEER = register("deer", DeerModel.createBodyLayer());
        HUSK = register("husk", HuskModel.createBodyLayer());
        SKELETON = register("skeleton", SkeletonModel.createBodyLayer());
        DREG = register("dreg", DregModel.createBodyLayer());
        CREEPER = register("creeper", CreeperModel.createBodyLayer());
        COCKATRICE = register("cockatrice", CockatriceModel.createBodyLayer());
        SPIDER = register("spider", SpiderModel.createBodyLayer());
        ROCKWORM = register("rockworm", RockwormModel.createBodyLayer());
        PIT_DEVIL = register("pit_devil", PitDevilModel.createBodyLayer());
        SLIME = register("slime", SlimeModel.createBodyLayer());
        SCORPION = register("scorpion", ScorpionModel.createBodyLayer());
        SCARAB = register("scarab", ScarabModel.createBodyLayer());
        TROLL = register("troll", TrollModel.createBodyLayer());
        OLMUR = register("olmur", OlmurModel.createBodyLayer());
        JELLYFISH_INNER = register("jellyfish_inner", JellyfishInnerModel.createInnerLayer());
        JELLYFISH_OUTER = register("jellyfish_outer", JellyfishOuterModel.createOuterLayer());
        //Armor
        INNER_ARMOR = register("armor", innerArmor, "inner_armor"); //Vanilla
        OUTER_ARMOR = register("armor", outerArmor, "outer_armor"); //Vanilla
        FLAT_ARMOR = register("armor", ArmorModel.createFlatLayer(), "flat");
        TAPERED_ARMOR = register("armor", ArmorModel.createTaperedLayer(), "tapered");
        SLIM_ARMOR = register("armor", ArmorModel.createSlimLayer(), "slim");
        PLATE_SURVIVOR = register("armor", ArmorModel.createPlateSurvivorLayer(), "plate_survivor");
        PLATE_EXPLORER = register("armor", ArmorModel.createPlateExplorerLayer(), "plate_explorer");
        CHAINMAIL_EXPLORER = register("armor", ArmorModel.createChainmailExplorerLayer(), "chainmail_explorer");
        PLATE_SLAYER = register("armor", ArmorModel.createPlateSlayerLayer(), "plate_slayer");
        SCALE_SLAYER = register("armor", ArmorModel.createScaleSlayerLayer(), "scale_slayer");
        CHAINMAIL_SLAYER = register("armor", ArmorModel.createChainmailSlayerLayer(), "chainmail_slayer");
        for(TieredArmorMaterial mat : TieredArmorMaterial.values()) {
            if(mat == TieredArmorMaterial.RAGGED) mapArmor(mat, SLIM_ARMOR);
            else if(mat == TieredArmorMaterial.RUSTED) mapArmor(mat, TAPERED_ARMOR);
            else if(mat.getArmorType() == ArmorType.PLATE) {
                if(mat.getStyle() == Style.SURVIVOR) mapArmor(mat, PLATE_SURVIVOR);
                else if(mat.getStyle() == Style.EXPLORER) mapArmor(mat, PLATE_EXPLORER);
                else if(mat.getStyle() == Style.SLAYER) mapArmor(mat, PLATE_SLAYER);
            }
            else if(mat.getArmorType() == ArmorType.SCALE) {
                if(mat.getStyle() == Style.SURVIVOR) mapArmor(mat, TAPERED_ARMOR);
                else if(mat.getStyle() == Style.EXPLORER) mapArmor(mat, FLAT_ARMOR);
                else if(mat.getStyle() == Style.SLAYER) mapArmor(mat, SCALE_SLAYER);
            }
            else if(mat.getArmorType() == ArmorType.CHAINMAIL) {
                if(mat.getStyle() == Style.SURVIVOR) mapArmor(mat, FLAT_ARMOR);
                else if(mat.getStyle() == Style.EXPLORER) mapArmor(mat, CHAINMAIL_EXPLORER);
                else if(mat.getStyle() == Style.SLAYER) mapArmor(mat, CHAINMAIL_SLAYER);
            }
            else mapArmor(mat, TAPERED_ARMOR);
        }
        //Equipment
        BACKPACK = register("equipment", AttachedEntityModel.createBackpackLayer(), "backpack");
        mapEquipment(ItemsNF.BACKPACK.get(), EntityPart.BODY, BACKPACK);
        //Accessories
        POUCH = register("accessory", AttachedEntityModel.createPouchLayer(), "pouch");
        mapAccessory(ItemsNF.POUCH.get(), EntityPart.BODY, POUCH);
        MASK = register("accessory", AttachedEntityModel.createMaskLayer(), "mask");
        mapAccessory(ItemsNF.MASK.get(), EntityPart.HEAD, MASK);
        NECKLACE = register("accessory", AttachedEntityModel.createNecklaceLayer(), "necklace");
        mapAccessory(ItemsNF.WARDING_CHARM.get(), EntityPart.BODY, NECKLACE);
        LANTERN = register("accessory", AttachedEntityModel.createLanternLayer(), "lantern");
        for(var item : ItemsNF.LANTERNS.values()) mapAccessory(item.get(), EntityPart.BODY, LANTERN);
        for(var item : ItemsNF.LANTERNS_UNLIT.values()) mapAccessory(item.get(), EntityPart.BODY, LANTERN);

        for(ModelLayerLocation location : layers.keySet()) {
            event.registerLayerDefinition(location, () -> layers.get(location));
        }
    }

    public static ModelLayerLocation getArmor(ITieredArmorMaterial material) {
        if(!materialMap.containsKey(material)) throw new IllegalStateException("Material " + material.getName() + " is missing in armor model map");
        return materialMap.get(material);
    }

    /**
     * Call during EntityRenderersEvent.RegisterLayerDefinitions for compatibility with ArmorLayer
     */
    public static void mapArmor(ITieredArmorMaterial material, ModelLayerLocation model) {
        if(materialMap.containsKey(material)) Nightfall.LOGGER.warn("Duplicate material in armor model map, overwriting " + material.getName());
        materialMap.put(material, model);
    }

    public static @Nullable Pair<EntityPart, ModelLayerLocation> getEquipment(Item equipment) {
        return equipmentMap.get(equipment);
    }

    /**
     * Call during EntityRenderersEvent.RegisterLayerDefinitions for compatibility with EquipmentLayer
     */
    public static void mapEquipment(Item equipment, EntityPart attachedPart, ModelLayerLocation model) {
        if(equipmentMap.containsKey(equipment)) Nightfall.LOGGER.warn("Duplicate equipment in model map, overwriting " + equipment.getRegistryName());
        equipmentMap.put(equipment, Pair.of(attachedPart, model));
    }

    public static @Nullable Pair<EntityPart, ModelLayerLocation> getAccessory(Item accessory) {
        return accessoryMap.get(accessory);
    }

    /**
     * Call during EntityRenderersEvent.RegisterLayerDefinitions for compatibility with PlayerEquipmentLayer (accessories)
     */
    public static void mapAccessory(Item accessory, EntityPart attachedPart, ModelLayerLocation model) {
        if(accessoryMap.containsKey(accessory)) Nightfall.LOGGER.warn("Duplicate accessory in model map, overwriting " + accessory.getRegistryName());
        accessoryMap.put(accessory, Pair.of(attachedPart, model));
    }

    private static ModelLayerLocation register(String pathName, LayerDefinition layer) {
        return register(pathName, layer, "main");
    }

    private static ModelLayerLocation register(String pathName, LayerDefinition layer, String layerName) {
        ModelLayerLocation location = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, pathName), layerName);
        layers.put(location, layer);
        return location;
    }
}
