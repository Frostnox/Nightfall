package frostnox.nightfall.data;

import frostnox.nightfall.block.*;
import frostnox.nightfall.item.*;
import frostnox.nightfall.item.item.TieredArmorItem;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class TextureProviderNF extends TextureProvider {
    private int y = 0;
    private final EnumMap<TieredHeat, List<Integer>> HEAT_PALETTES = new EnumMap<>(TieredHeat.class);
    private final List<Integer> WATER, SEAWATER, LAVA;
    private final List<Integer> STICK, STICK5, STICK3, STICK2, TREATED_STICK, TREATED_STICK_REDUCED;
    private final List<Integer> FIBER, TWINE, TWINE_LIGHT2, TWINE3, TWINE2;
    private final List<Integer> LEATHER, LEATHER3, LEATHER2;
    private final List<Integer> FLINT, FLINT4, FLINT5;
    private final List<Integer> SNOW, SNOW_LIGHT;
    private final List<Integer> CLAY, TERRACOTTA;
    private final List<Integer> METAL_GENERIC_LIGHT, METAL_GENERIC_DARK;
    private final Map<Metal, List<Integer>> METAL_PALETTES = new HashMap<>();
    private final Map<Metal, List<Integer>> METAL_REDUCED_PALETTES = new HashMap<>();
    private final Map<Metal, List<Integer>> METAL_DULL_PALETTES = new HashMap<>();
    private final Map<Metal, List<Integer>> METAL5_PALETTES = new HashMap<>(), METAL6_PALETTES = new HashMap<>(), METAL_NUGGET_PALETTES = new HashMap<>();
    private final Map<Metal, List<Integer>> METAL_SCALE_PALETTES = new HashMap<>();
    private final Map<Metal, List<Integer>> METAL_PLATE_PALETTES = new HashMap<>();
    private final Map<Metal, List<Integer>> METAL_CHAINMAIL_PALETTES = new HashMap<>();
    private final Map<Metal, List<Integer>> LANTERN_PALETTES = new HashMap<>();
    private final Map<Stone, List<Integer>> STONE_PALETTES = new HashMap<>();
    private final Map<Stone, List<Integer>> RAW_STONE_PALETTES = new HashMap<>(), STONE7_PALETTES = new HashMap<>(), DARK_STONE_PALETTES = new HashMap<>();
    private final Map<Stone, List<Integer>> STONE_BRICK_ITEM_PALETTES = new HashMap<>();
    private final Map<Tree, List<Integer>> WOOD_FULL_PALETTES = new HashMap<>();
    private final Map<Tree, List<Integer>> WOOD_PALETTES = new HashMap<>();
    private final Map<Tree, List<Integer>> BOW_PALETTES = new HashMap<>();
    private final Map<Tree, List<Integer>> STRIPPED_WOOD_PALETTES = new HashMap<>(), STRIPPED_WOOD_ITEM_PALETTES = new HashMap<>();
    private final Map<Tree, List<Integer>> WOOD_REDUCED_PALETTES = new HashMap<>();
    private final Map<Tree, List<Integer>> BARREL_OPEN_PALETTES = new HashMap<>();

    public TextureProviderNF(DataGenerator pGenerator, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, modId, existingFileHelper);
        String palettePath = getExternalImagePath(ResourceLocation.fromNamespaceAndPath(modId, "palette")).toString();
        BufferedImage image;
        try {
            image = ImageIO.read(new File(palettePath));
        }
        catch (IOException ioException) {
            throw new IllegalStateException("No palette image at path " + palettePath);
        }
        //In vertical order that palettes appear in image
        for(TieredHeat type : TieredHeat.values()) {
            if(type != TieredHeat.NONE) HEAT_PALETTES.put(type, fill(image));
        }
        WATER = fill(image);
        SEAWATER = fill(image);
        LAVA = fill(image);
        STICK = fill(image);
        STICK5 = subset(STICK, 5, 1);
        STICK3 = subset(STICK, 3, 0);
        STICK2 = subset(STICK, 2, 0);
        TREATED_STICK = fill(image);
        TREATED_STICK_REDUCED = subset(TREATED_STICK, 2, 0);
        FIBER = fill(image);
        TWINE = fill(image);
        TWINE_LIGHT2 = subset(TWINE, 2, 2);
        TWINE3 = subset(TWINE, 3, 0);
        TWINE2 =  subset(TWINE, 2, 0);
        LEATHER = fill(image);
        LEATHER3 = subset(LEATHER, 3, 0);
        LEATHER2 = subset(LEATHER, 2, 0);
        FLINT = fill(image);
        FLINT5 = subset(FLINT, 5, 1);
        FLINT4 = subset(FLINT, 4, 2);
        SNOW = fill(image);
        SNOW_LIGHT = subset(SNOW, 3, 3);
        CLAY = fill(image);
        TERRACOTTA = fill(image);
        METAL_GENERIC_LIGHT = fill(image);
        METAL_GENERIC_DARK = fill(image);
        for(Metal type : Metal.values()) {
            METAL_PALETTES.put(type, fill(image));
            List<Integer> metal = METAL_PALETTES.get(type);
            METAL_REDUCED_PALETTES.put(type, subset(METAL_PALETTES.get(type), 6, 2));
            METAL_DULL_PALETTES.put(type, subset(METAL_PALETTES.get(type), 5, 2));
            METAL5_PALETTES.put(type, subset(METAL_PALETTES.get(type), 5, 0));
            METAL6_PALETTES.put(type, subset(METAL_PALETTES.get(type), 6, 0));
            METAL_NUGGET_PALETTES.put(type, List.of(metal.get(0), metal.get(1), metal.get(2), metal.get(4), metal.get(5), metal.get(7)));
            METAL_SCALE_PALETTES.put(type, List.of(metal.get(0), metal.get(1), metal.get(3), metal.get(4), metal.get(5), metal.get(6)));
            METAL_PLATE_PALETTES.put(type, List.of(metal.get(0), metal.get(1), metal.get(3), metal.get(4), metal.get(5), metal.get(6), metal.get(7)));
            METAL_CHAINMAIL_PALETTES.put(type, List.of(metal.get(0), metal.get(1), metal.get(3), metal.get(4), metal.get(5), metal.get(6), metal.get(7)));
            LANTERN_PALETTES.put(type, List.of(metal.get(0), metal.get(1), metal.get(2), metal.get(3), metal.get(5)));
        }
        for(Stone type : Stone.values()) {
            STONE_PALETTES.put(type, fill(image));
            List<Integer> stone = STONE_PALETTES.get(type);
            DARK_STONE_PALETTES.put(type, subset(STONE_PALETTES.get(type), 3, 2));
            if(type == Stone.SHALE || type == Stone.LIMESTONE || type == Stone.MARBLE || type == Stone.BASALT || type == Stone.NIXWACKE || type == Stone.STYGFEL || type == Stone.PUMICE) {
                RAW_STONE_PALETTES.put(type, subset(STONE_PALETTES.get(type), 4, 4));
            }
            else {
                RAW_STONE_PALETTES.put(type, subset(STONE_PALETTES.get(type), 5, 3));
            }
            STONE7_PALETTES.put(type, combine(List.of(stone.get(0), stone.get(1)), subset(STONE_PALETTES.get(type), 5, 3)));
            STONE_BRICK_ITEM_PALETTES.put(type, List.of(stone.get(0), stone.get(1), stone.get(3), stone.get(4), stone.get(5), stone.get(7)));
        }
        for(Tree type : Tree.values()) {
            WOOD_FULL_PALETTES.put(type, fill(image));
            BOW_PALETTES.put(type, subset(WOOD_FULL_PALETTES.get(type), 8, 0));
            WOOD_PALETTES.put(type, subset(WOOD_FULL_PALETTES.get(type), 8, 1));
            List<Integer> base = WOOD_PALETTES.get(type);
            STRIPPED_WOOD_PALETTES.put(type, subset(base, 5, 3));
            STRIPPED_WOOD_ITEM_PALETTES.put(type, subset(base, 7, 1));
            WOOD_REDUCED_PALETTES.put(type, subset(base, 6, 1));
            BARREL_OPEN_PALETTES.put(type, combine(subset(base, 3, 0), subset(base, 3, 4)));
        }
    }

    protected ResourceLocation itemInv(RegistryObject<?> item) {
        return ResourceLocation.fromNamespaceAndPath(modId, "item/" + item.getId().getPath() + "_inventory");
    }

    protected ResourceLocation armor(ITieredArmorMaterial material) {
        return ResourceLocation.fromNamespaceAndPath(modId, "models/armor/" + material.getName());
    }

    protected ResourceLocation armor(String name) {
        return ResourceLocation.fromNamespaceAndPath(modId, "models/armor/" + name);
    }

    protected ResourceLocation tempArmor(String name) {
        return armor(name + temp);
    }

    protected ResourceLocation equipment(RegistryObject<?> item) {
        return equipment(item.getId().getPath());
    }

    protected ResourceLocation equipment(String name) {
        return ResourceLocation.fromNamespaceAndPath(modId, "models/equipment/" + name);
    }

    protected ResourceLocation tempEquipment(RegistryObject<?> item) {
        return tempEquipment(item.getId().getPath());
    }

    protected ResourceLocation tempEquipment(String name) {
        return equipment(name + temp);
    }

    private List<Integer> fill(BufferedImage image) {
        List<Integer> list = new ArrayList<>();
        for(int x = 0; x < image.getWidth(); x++) {
            int rgb = image.getRGB(x, y);
            int alpha = (rgb & 0xff000000) >>> 24;
            if(alpha == 0) break;
            list.add(rgb);
        }
        y += 3;
        return list;
    }

    @SafeVarargs
    private static List<Integer> combine(List<Integer>... sets) {
        List<Integer> newSet = new ArrayList<>();
        for(var set : sets) newSet.addAll(set);
        return newSet;
    }

    private static List<Integer> subset(List<Integer> set, int size, int offset) {
        List<Integer> subset = new ArrayList<>(size);
        for(int i = 0; i < size; i++) subset.add(set.get(i + offset));
        return subset;
    }

    @Override
    protected void addTextures() {
        for(Soil type : List.of(Soil.SILT, Soil.DIRT, Soil.LOAM)) {
            replaceImagePalette(tempBlock("snowy_" + type.getName() + "_side_overlay"), block("snowy_" + type.getName() + "_side_overlay"), SNOW);
            layerImages(block("snowy_" + type.getName() + "_side"), block("grassy_" + type.getName() + "_side"), tempBlock("snowy_" + type.getName() + "_side_overlay"));
        }
        for(Soil type : BlocksNF.STRANGE_SOILS.keySet()) {
            layerImages(block(BlocksNF.STRANGE_SOILS.get(type)), block(type.getName() + "_block"), block(BlocksNF.STRANGE_SOILS.get(type)));
        }
        replaceImagePalette(block(BlocksNF.SNOW), block(BlocksNF.SNOW), SNOW_LIGHT);
        replaceImagePalette(item(ItemsNF.SNOWBALL), item(ItemsNF.SNOWBALL), SNOW);
        for(Stone type : Stone.values()) {
            String category = type.getType().name().toLowerCase(Locale.ROOT);
            if(type == Stone.PUMICE) {
                replaceImagePalette(block(BlocksNF.STONE_BLOCKS.get(type)), block(BlocksNF.STONE_BLOCKS.get(type)), RAW_STONE_PALETTES.get(type));
            }
            else {
                replaceImagePalette(block(BlocksNF.STONE_BLOCKS.get(type)), block(BlocksNF.STONE_BLOCKS.get(type)), RAW_STONE_PALETTES.get(type));
                List<RegistryObject<Block>> ores;
                if(type == Stone.MOONSTONE) ores = List.of(BlocksNF.METEORITE_ORE);
                else ores = List.of(BlocksNF.TIN_ORES.get(type), BlocksNF.COPPER_ORES.get(type), BlocksNF.AZURITE_ORES.get(type), BlocksNF.HEMATITE_ORES.get(type),
                        BlocksNF.COAL_ORES.get(type));
                for(var ore : ores) {
                    String metal = ore.getId().getPath().replace(type.getName() + "_", "");
                    replaceImagePalette(tempBlock(ore, "_overlay"), block(metal + "_overlay"), DARK_STONE_PALETTES.get(type));
                    layerImages(block(ore), block(BlocksNF.STONE_BLOCKS.get(type)), tempBlock(ore, "_overlay"));
                }
                if(Files.exists(getExternalImagePath(block(BlocksNF.STONE_BLOCKS.get(type), "_top")))) {
                    if(type == Stone.AURGROT) replaceImagePalette(block(BlocksNF.STONE_BLOCKS.get(type), "_top"), block(BlocksNF.STONE_BLOCKS.get(type), "_top"), subset(RAW_STONE_PALETTES.get(type), 4, 0));
                    else replaceImagePalette(block(BlocksNF.STONE_BLOCKS.get(type), "_top"), block(BlocksNF.STONE_BLOCKS.get(type), "_top"), RAW_STONE_PALETTES.get(type));
                    for(var ore : ores) {
                        layerImages(block(ore, "_top"), block(BlocksNF.STONE_BLOCKS.get(type), "_top"), tempBlock(ore, "_overlay"));
                    }
                }
            }
            if(type.getType() == StoneType.IGNEOUS) {
                replaceImagePalette(block(BlocksNF.POLISHED_STONE.get(type)), block("polished_stone_igneous"), subset(STONE_PALETTES.get(type), 4, 4));
            }
            else replaceImagePalette(block(BlocksNF.POLISHED_STONE.get(type)), block("polished_stone"), subset(STONE_PALETTES.get(type), 3, 5));
            replaceImagePalette(block(BlocksNF.TILED_STONE.get(type)), block("tiled_stone"), subset(STONE_PALETTES.get(type), 5, 3));
            replaceImagePalette(block(BlocksNF.STACKED_STONE.get(type)), block("stacked_stone_" + category), STONE7_PALETTES.get(type));
            replaceImagePalette(block(BlocksNF.COBBLED_STONE.get(type)), block("cobbled_stone_" + category), STONE7_PALETTES.get(type));
            replaceImagePalette(block(BlocksNF.STONE_BRICK_BLOCKS.get(type)), block("stone_bricks_" + category), STONE7_PALETTES.get(type));
            String rock = "rock";
            if(type == Stone.SLATE || type == Stone.DEEPSLATE || type == Stone.SUNSCHIST || type == Stone.AURGROT) rock += "_plane";
            else if(type == Stone.PUMICE || type == Stone.LIMESTONE || type == Stone.NIXWACKE || type == Stone.BASALT) rock += "_porous";
            replaceImagePalette(item(ItemsNF.ROCKS.get(type)), item(rock), STONE7_PALETTES.get(type));
            replaceImagePalette(item(ItemsNF.STONE_BRICKS.get(type)), item("stone_brick"), STONE_BRICK_ITEM_PALETTES.get(type));
        }

        for(Tree type : Tree.values()) {
            replaceImagePalette(tempItem(ItemsNF.LOGS.get(type)), item("log_top"), STRIPPED_WOOD_PALETTES.get(type));
            layerImages(item(ItemsNF.LOGS.get(type)), item(ItemsNF.LOGS.get(type)), tempItem(ItemsNF.LOGS.get(type)));
            replaceImagePalette(item(ItemsNF.STRIPPED_LOGS.get(type)), item("stripped_log"), STRIPPED_WOOD_ITEM_PALETTES.get(type));
        }

        for(Tree type : BlocksNF.PLANK_BLOCKS.keySet()) {
            String style = "_" + type.getStyle();
            String plank;
            if(type.getHardness() > 1.6F) plank = "_hard";
            else if(type.getHardness() < 1F) plank = "_soft";
            else plank = "";
            List<Integer>[] palette;
            List<Integer> metal = METAL_GENERIC_LIGHT;
            if(style.contains("medieval")) {
                palette = new List[2];
                if(type == Tree.LARCH) metal = METAL_GENERIC_DARK;
            }
            else palette = new List[1];
            replaceImagePalette(block(BlocksNF.PLANK_BLOCKS.get(type)), block("planks" + plank), WOOD_REDUCED_PALETTES.get(type));
            palette[0] = WOOD_PALETTES.get(type);
            if(palette.length == 2) palette[1] = List.of(metal.get(0), metal.get(1), metal.get(4));
            replaceImagePalette(block(BlocksNF.PLANK_DOORS.get(type), "_bottom"), block("door_bottom" + style), palette);
            if(palette.length == 2) palette[1] = List.of(metal.get(1), metal.get(4));
            replaceImagePalette(block(BlocksNF.PLANK_DOORS.get(type), "_top"), block("door_top" + style), palette);
            palette[0] = combine(subset(WOOD_FULL_PALETTES.get(type), 2, 1), subset(WOOD_FULL_PALETTES.get(type), 4, 3));
            if(palette.length == 2) palette[1] = List.of(metal.get(0), metal.get(1), metal.get(4));
            replaceImagePalette(item(ItemsNF.PLANK_DOORS.get(type)), item("door" + style), palette);
            palette[0] = WOOD_PALETTES.get(type);
            if(palette.length == 2) palette[1] = List.of(metal.get(1), metal.get(4));
            replaceImagePalette(block(BlocksNF.PLANK_HATCHES.get(type)), block("hatch" + style), palette);
            rotateImage(block(BlocksNF.PLANK_TRAPDOORS.get(type)), block(BlocksNF.PLANK_HATCHES.get(type)), 90);
            palette[0] = WOOD_PALETTES.get(type);
            if(palette.length == 2) palette[1] = List.of(metal.get(3));
            replaceImagePalette(block(BlocksNF.PLANK_LADDERS.get(type)), block("ladder" + style), palette);
            replaceImagePalette(block(BlocksNF.WOODEN_ITEM_FRAMES.get(type)), block("item_frame_wooden"), WOOD_REDUCED_PALETTES.get(type));
            replaceImagePalette(item(BlocksNF.WOODEN_ITEM_FRAMES.get(type)), item("item_frame_wooden"), WOOD_REDUCED_PALETTES.get(type));
            replaceImagePalette(block(BlocksNF.BARRELS.get(type), "_end"), block("barrel_end"), WOOD_REDUCED_PALETTES.get(type));
            replaceImagePalette(block(BlocksNF.BARRELS.get(type), "_open"), block("barrel_open"), BARREL_OPEN_PALETTES.get(type));
            replaceImagePalette(block(BlocksNF.BARRELS.get(type), "_side"), block("barrel_side"), WOOD_REDUCED_PALETTES.get(type));
            palette[0] = WOOD_FULL_PALETTES.get(type);
            if(palette.length == 2) palette[1] = metal;
            replaceImagePalette(entity(BlocksNF.CHESTS.get(type), "chest", "_single"), entity("chest/chest_single" + style), palette);
            replaceImagePalette(entity(BlocksNF.CHESTS.get(type), "chest", "_left"), entity("chest/chest_left" + style), palette);
            replaceImagePalette(entity(BlocksNF.CHESTS.get(type), "chest", "_right"), entity("chest/chest_right" + style), palette);
            String signStyle = type.getHardness() >= Tree.MAPLE.getHardness() ? "_single" : "_multiple";
            replaceImagePalette(entity("sign/" + type.getName() + "_plank"), entity("sign/plank" + signStyle), subset(WOOD_FULL_PALETTES.get(type), 5, 2));
            replaceImagePalette(block(BlocksNF.SHELVES.get(type)), block("shelf_simple"), WOOD_PALETTES.get(type));
            palette[0] = style.contains("patterned") ? WOOD_PALETTES.get(type) : WOOD_REDUCED_PALETTES.get(type);
            if(palette.length == 2) palette[1] = List.of(metal.get(2));
            replaceImagePalette(block(BlocksNF.CHAIRS.get(type)), block("chair" + style), palette);
            palette[0] = combine(subset(WOOD_FULL_PALETTES.get(type), 2, 1), subset(WOOD_FULL_PALETTES.get(type), 3, 3));
            if(palette.length == 2) palette[1] = List.of(metal.get(2));
            replaceImagePalette(item(ItemsNF.CHAIRS.get(type)), item("chair" + style), palette);
            replaceImagePalette(item(ItemsNF.RACKS.get(type)), item("rack"), combine(subset(WOOD_FULL_PALETTES.get(type), 2, 1), subset(WOOD_FULL_PALETTES.get(type), 4, 3)));
            replaceImagePalette(item(ItemsNF.PLANKS.get(type)), item("plank"), WOOD_REDUCED_PALETTES.get(type));
            replaceImagePalette(item(ItemsNF.PLANK_SIGNS.get(type)), item("plank_sign" + signStyle), combine(subset(WOOD_FULL_PALETTES.get(type), 2, 1), subset(WOOD_FULL_PALETTES.get(type), 4, 3)));
            replaceImagePalette(block(BlocksNF.PLANK_FENCES.get(type)), block("fence"), subset(WOOD_FULL_PALETTES.get(type), 5, 2));
        }
        for(Tree type : BlocksNF.STRIPPED_LOGS.keySet()) {
            replaceImagePalette(block(BlocksNF.STRIPPED_LOGS.get(type)), block("log_stripped"), STRIPPED_WOOD_PALETTES.get(type));
            replaceImagePalette(block(BlocksNF.STRIPPED_LOGS.get(type), "_top"), block("log_stripped_top"), STRIPPED_WOOD_PALETTES.get(type));
            if(BlocksNF.LOGS.containsKey(type)) {
                layerImages(block(BlocksNF.LOGS.get(type), "_top"),
                        block(BlocksNF.STRIPPED_LOGS.get(type), "_top"),
                        block(BlocksNF.LOGS.get(type), "_top"));
            }
        }
        for(Tree type : ItemsNF.BOWS.keySet()) {
            replaceImagePalette(item(ItemsNF.BOWS.get(type)), item("bow"), BOW_PALETTES.get(type));
            replaceImagePalette(item(ItemsNF.BOWS.get(type), "_pulling_0"), item("bow_pulling_0"), BOW_PALETTES.get(type));
            replaceImagePalette(item(ItemsNF.BOWS.get(type), "_pulling_1"), item("bow_pulling_1"), BOW_PALETTES.get(type));
            replaceImagePalette(item(ItemsNF.BOWS.get(type), "_pulling_2"), item("bow_pulling_2"), BOW_PALETTES.get(type));
        }
        for(Metal metal : BlocksNF.METAL_BLOCKS.keySet()) {
            replaceImagePalette(block(BlocksNF.METAL_BLOCKS.get(metal)), block("metal_block"), subset(METAL_PALETTES.get(metal), 6, 2));
        }
        for(Metal metal : BlocksNF.INGOT_PILES.keySet()) {
            replaceImagePalette(block(BlocksNF.INGOT_PILES.get(metal)), block("ingot_pile"), subset(METAL_PALETTES.get(metal), 6, 2));
            replaceImagePalette(block(BlocksNF.INGOT_PILES.get(metal), "_top"), block("ingot_pile_top"), subset(METAL_PALETTES.get(metal), 6, 2));
        }
        replaceImagePalette(tempBlock(BlocksNF.STEEL_INGOT_PILE_POOR), block("ingot_pile_poor_overlay"), subset(METAL_PALETTES.get(Metal.STEEL), 6, 2));
        layerImages(block(BlocksNF.STEEL_INGOT_PILE_POOR), block(BlocksNF.INGOT_PILES.get(Metal.IRON)), tempBlock(BlocksNF.STEEL_INGOT_PILE_POOR));
        replaceImagePalette(tempBlock(BlocksNF.STEEL_INGOT_PILE_POOR, "_top"), block("ingot_pile_top_poor_overlay"), subset(METAL_PALETTES.get(Metal.STEEL), 6, 2));
        layerImages(block(BlocksNF.STEEL_INGOT_PILE_POOR, "_top"), block(BlocksNF.INGOT_PILES.get(Metal.IRON), "_top"), tempBlock(BlocksNF.STEEL_INGOT_PILE_POOR, "_top"));
        replaceImagePalette(tempBlock(BlocksNF.STEEL_INGOT_PILE_FAIR), block("ingot_pile_fair_overlay"), subset(METAL_PALETTES.get(Metal.STEEL), 6, 2));
        layerImages(block(BlocksNF.STEEL_INGOT_PILE_FAIR), block(BlocksNF.INGOT_PILES.get(Metal.IRON)), tempBlock(BlocksNF.STEEL_INGOT_PILE_FAIR));
        replaceImagePalette(tempBlock(BlocksNF.STEEL_INGOT_PILE_FAIR, "_top"), block("ingot_pile_top_fair_overlay"), subset(METAL_PALETTES.get(Metal.STEEL), 6, 2));
        layerImages(block(BlocksNF.STEEL_INGOT_PILE_FAIR, "_top"), block(BlocksNF.INGOT_PILES.get(Metal.IRON), "_top"), tempBlock(BlocksNF.STEEL_INGOT_PILE_FAIR, "_top"));
        for(Metal type : BlocksNF.LANTERNS.keySet()) {
            String base = "lantern_" + type.getCategory().name().toLowerCase(Locale.ROOT);
            replaceImagePalette(block(BlocksNF.LANTERNS_UNLIT.get(type)), block(base), LANTERN_PALETTES.get(type));
            layerImages(block(BlocksNF.LANTERNS.get(type)), block(BlocksNF.LANTERNS_UNLIT.get(type)), block(base + "_overlay"));
            replaceImagePalette(equipment(ItemsNF.LANTERNS_UNLIT.get(type)), equipment(base), LANTERN_PALETTES.get(type));
            layerImages(equipment(ItemsNF.LANTERNS.get(type)), equipment(ItemsNF.LANTERNS_UNLIT.get(type)), equipment(base + "_overlay"));
            replaceImagePalette(item(ItemsNF.LANTERNS_UNLIT.get(type)), item(base), LANTERN_PALETTES.get(type));
            layerImages(item(ItemsNF.LANTERNS.get(type)), item(ItemsNF.LANTERNS_UNLIT.get(type)), item(base + "_overlay"));
        }
        for(Metal type : BlocksNF.ANVILS_METAL.keySet()) {
            replaceImagePalette(block(BlocksNF.ANVILS_METAL.get(type)), block("anvil"), METAL5_PALETTES.get(type));
            replaceImagePalette(block(BlocksNF.ANVILS_METAL.get(type), "_top"), block("anvil_top"), METAL5_PALETTES.get(type));
        }
        for(TieredHeat type : HEAT_PALETTES.keySet()) {
            replaceImagePalette(tempBlock(BlocksNF.COAL_BURNING, "_overlay_" + type), block(BlocksNF.COAL_BURNING, "_overlay"), HEAT_PALETTES.get(type));
            layerImages(block(BlocksNF.COAL_BURNING, "_" + type), block(BlocksNF.COAL), tempBlock(BlocksNF.COAL_BURNING, "_overlay_" + type));
            layerImages(block(BlocksNF.COKE_BURNING, "_" + type), block(BlocksNF.COKE), tempBlock(BlocksNF.COAL_BURNING, "_overlay_" + type));
            replaceImagePalette(tempBlock(BlocksNF.CHARCOAL_BURNING, "_overlay_" + type), block(BlocksNF.CHARCOAL_BURNING, "_overlay"), HEAT_PALETTES.get(type));
            layerImages(block(BlocksNF.CHARCOAL_BURNING, "_" + type), block(BlocksNF.CHARCOAL), tempBlock(BlocksNF.CHARCOAL_BURNING, "_overlay_" + type));
            replaceImagePalette(tempBlock(BlocksNF.CHARCOAL_BURNING, "_top_overlay_" + type), block(BlocksNF.CHARCOAL_BURNING, "_top_overlay"), HEAT_PALETTES.get(type));
            layerImages(block(BlocksNF.CHARCOAL_BURNING, "_top_" + type), block(BlocksNF.CHARCOAL, "_top"), tempBlock(BlocksNF.CHARCOAL_BURNING, "_top_overlay_" + type));
            layerImages(block(BlocksNF.FIREWOOD_BURNING, "_" + type), block(BlocksNF.FIREWOOD), tempBlock(BlocksNF.CHARCOAL_BURNING, "_overlay_" + type));
            layerImages(block(BlocksNF.FIREWOOD_BURNING, "_top_" + type), block(BlocksNF.FIREWOOD, "_top"), tempBlock(BlocksNF.CHARCOAL_BURNING, "_top_overlay_" + type));
            replaceImagePalette(tempBlock(BlocksNF.CRUCIBLE, "_" + type), block(BlocksNF.CRUCIBLE, "_overlay"), HEAT_PALETTES.get(type));
            layerImages(block(BlocksNF.CRUCIBLE, "_" + type), block(BlocksNF.CRUCIBLE, "_none"), tempBlock(BlocksNF.CRUCIBLE, "_" + type));
        }
        replaceImagePalette(tempBlock(BlocksNF.SMELTED_AZURITE), block(BlocksNF.SMELTED_AZURITE, "_overlay"), subset(METAL_PALETTES.get(Metal.COPPER), 7, 0));
        layerImages(block(BlocksNF.SMELTED_AZURITE), block(BlocksNF.SLAG), tempBlock(BlocksNF.SMELTED_AZURITE));
        layerImages(block(BlocksNF.SMELTED_HEMATITE), block(BlocksNF.SLAG), block(BlocksNF.SMELTED_HEMATITE, "_overlay"));

        replaceImagePalette(item(ItemsNF.STICK), item(ItemsNF.STICK), STICK);
        replaceImagePalette(item(ItemsNF.LEATHER), item(ItemsNF.LEATHER), subset(LEATHER, 6, 1));

        replaceImagePalette(item(ItemsNF.TORCH), item(ItemsNF.TORCH), STICK);
        replaceImagePalette(item(ItemsNF.TORCH_UNLIT), item(ItemsNF.TORCH_UNLIT), STICK);
        replaceImagePalette(block(BlocksNF.TORCH), block(BlocksNF.TORCH), STICK3);
        replaceImagePalette(block(BlocksNF.TORCH_UNLIT), block(BlocksNF.TORCH_UNLIT), STICK3);

        replaceImagePalette(tempItem("seawater_bucket_overlay"), item("water_bucket_overlay"), SEAWATER);
        replaceImagePalette(tempItem("water_bucket_overlay"), item("water_bucket_overlay"), WATER);
        replaceImagePalette(tempItem("lava_bucket_overlay"), item("lava_bucket_overlay"), LAVA);
        replaceImagePalette(item(ItemsNF.WOODEN_BUCKET), item("empty_wooden_bucket"), WOOD_REDUCED_PALETTES.get(Tree.OAK));
        layerImages(item(ItemsNF.WOODEN_SEAWATER_BUCKET), item(ItemsNF.WOODEN_BUCKET), tempItem("seawater_bucket_overlay"));
        layerImages(item(ItemsNF.WOODEN_WATER_BUCKET), item(ItemsNF.WOODEN_BUCKET), tempItem("water_bucket_overlay"));
        replaceImagePalette(item(ItemsNF.BRONZE_BUCKET), item("empty_bronze_bucket"), METAL_PALETTES.get(Metal.BRONZE));
        layerImages(item(ItemsNF.BRONZE_SEAWATER_BUCKET), item(ItemsNF.BRONZE_BUCKET), tempItem("seawater_bucket_overlay"));
        layerImages(item(ItemsNF.BRONZE_WATER_BUCKET), item(ItemsNF.BRONZE_BUCKET), tempItem("water_bucket_overlay"));
        replaceImagePalette(item(ItemsNF.ALKIMIUM_BUCKET), item("empty_bronze_bucket"), METAL_PALETTES.get(Metal.BRONZE));
        layerImages(item(ItemsNF.ALKIMIUM_SEAWATER_BUCKET), item(ItemsNF.ALKIMIUM_BUCKET), tempItem("seawater_bucket_overlay"));
        layerImages(item(ItemsNF.ALKIMIUM_WATER_BUCKET), item(ItemsNF.ALKIMIUM_BUCKET), tempItem("water_bucket_overlay"));
        layerImages(item(ItemsNF.ALKIMIUM_LAVA_BUCKET), item(ItemsNF.ALKIMIUM_BUCKET), tempItem("lava_bucket_overlay"));

        replaceImagePalette(item(ItemsNF.FLINT_ADZE), item("flint_adze"), FLINT4, STICK5, FIBER);
        replaceImagePalette(itemInv(ItemsNF.FLINT_ADZE), item("flint_adze_inventory"), FLINT, STICK, FIBER);
        replaceImagePalette(item(ItemsNF.FLINT_AXE), item("flint_axe"), FLINT5, STICK5, FIBER);
        replaceImagePalette(itemInv(ItemsNF.FLINT_AXE), item("flint_axe_inventory"), FLINT, STICK, FIBER);
        replaceImagePalette(item(ItemsNF.FLINT_DAGGER), item("flint_dagger"), FLINT4, STICK5, FIBER);
        replaceImagePalette(itemInv(ItemsNF.FLINT_DAGGER), item("flint_dagger_inventory"), FLINT4, STICK, FIBER);
        replaceImagePalette(item(ItemsNF.FLINT_CHISEL), item("flint_chisel"), FLINT5);
        replaceImagePalette(itemInv(ItemsNF.FLINT_CHISEL), item("flint_chisel_inventory"), FLINT);
        replaceImagePalette(item(ItemsNF.FLINT_HAMMER), item("flint_hammer"), FLINT4, STICK5, FIBER);
        replaceImagePalette(itemInv(ItemsNF.FLINT_HAMMER), item("flint_hammer_inventory"), subset(FLINT, 5, 0), STICK, FIBER);
        replaceImagePalette(item(ItemsNF.FLINT_SHOVEL), item("flint_shovel"), subset(FLINT, 4, 0), STICK5, FIBER);
        replaceImagePalette(itemInv(ItemsNF.FLINT_SHOVEL), item("flint_shovel_inventory"), subset(FLINT, 5, 0), STICK, FIBER);
        replaceImagePalette(item(ItemsNF.FLINT_SPEAR), item("flint_spear"), FLINT4, STICK5, FIBER);
        replaceImagePalette(itemInv(ItemsNF.FLINT_SPEAR), item("flint_spear_inventory"), FLINT, STICK, FIBER);

        replaceImagePalette(item(ItemsNF.IRONWOOD_SHIELD), item("shield"),
                subset(WOOD_FULL_PALETTES.get(Tree.IRONWOOD), 6, 2));
        replaceImagePalette(itemInv(ItemsNF.IRONWOOD_SHIELD), item("shield_inventory"),
                subset(WOOD_FULL_PALETTES.get(Tree.IRONWOOD), 6, 2));
        replaceImagePalette(item(ItemsNF.IRONWOOD_SHIELD, "_inventory_overlay"), item("ironwood_shield_inventory_overlay"),
                subset(WOOD_FULL_PALETTES.get(Tree.IRONWOOD), 2, 0));
        replaceImagePalette(item(ItemsNF.IRONWOOD_SHIELD, "_overlay"), item("ironwood_shield_overlay"),
                subset(WOOD_FULL_PALETTES.get(Tree.IRONWOOD), 6, 2));

        for(Metal metal : ItemsNF.INGOTS.keySet()) {
            replaceImagePalette(item(ItemsNF.INGOTS.get(metal)), item("ingot"), METAL_PALETTES.get(metal));
            replaceImagePalette(item(ItemsNF.BILLETS.get(metal)), item("billet"), METAL_PALETTES.get(metal));
        }
        for(Metal metal : ItemsNF.WIRES.keySet()) replaceImagePalette(item(ItemsNF.WIRES.get(metal)), item("wire"), METAL_PALETTES.get(metal));
        for(Metal metal : ItemsNF.PLATES.keySet()) replaceImagePalette(item(ItemsNF.PLATES.get(metal)), item("plate"), METAL_PALETTES.get(metal));
        for(Metal metal : ItemsNF.CHAINMAIL.keySet()) replaceImagePalette(item(ItemsNF.CHAINMAIL.get(metal)), item("chainmail"), METAL_PALETTES.get(metal));
        for(Metal metal : ItemsNF.SCALES.keySet()) replaceImagePalette(item(ItemsNF.SCALES.get(metal)), item("scales"), METAL_PALETTES.get(metal));
        for(TieredItemMaterial type : ItemsNF.METAL_ARROWHEADS.keySet()) {
            var palette = METAL_PALETTES.get(type.getMetal());
            replaceImagePalette(item(ItemsNF.METAL_ARROWHEADS.get(type)), item("metal_arrowhead"),
                    List.of(palette.get(1), palette.get(2), palette.get(3), palette.get(4), palette.get(5), palette.get(7)));
        }
        for(Metal metal : ItemsNF.METAL_SHIELDS.keySet()) {
            var item = ItemsNF.METAL_SHIELDS.get(metal);
            replaceImagePalette(item(item), item("shield"),
                    subset(WOOD_FULL_PALETTES.get(Tree.IRONWOOD), 6, 2));
            replaceImagePalette(itemInv(item), item("shield_inventory"),
                    subset(WOOD_FULL_PALETTES.get(Tree.IRONWOOD), 6, 2));
            replaceImagePalette(tempItem(item, "_inventory_overlay"), item("shield_inventory_overlay"),
                    subset(METAL_PALETTES.get(metal), 7, 0));
            layerImages(item(item, "_inventory_overlay"), item(ItemsNF.IRONWOOD_SHIELD, "_inventory_overlay"), tempItem(item, "_inventory_overlay"));
            replaceImagePalette(tempItem(item, "_overlay"), item("shield_overlay"),
                    subset(METAL_PALETTES.get(metal), 4, 3));
            layerImages(item(item, "_overlay"), item(ItemsNF.IRONWOOD_SHIELD, "_overlay"), tempItem(item, "_overlay"));
        }
        for(TieredItemMaterial material : ItemsNF.ARMAMENT_HEADS.keySet()) {
            for(Armament armament : ItemsNF.ARMAMENT_HEADS.get(material).keySet()) {
                var item = ItemsNF.ARMAMENT_HEADS.get(material).get(armament);
                List<Integer> head;
                if(armament == Armament.HAMMER || armament == Armament.SHOVEL) head = METAL_DULL_PALETTES.get(material.getMetal());
                else head = METAL_REDUCED_PALETTES.get(material.getMetal());
                replaceImagePalette(item(item), item(armament.getName() + "_head"), head);
            }
        }
        for(Armament type : ItemsNF.UNFIRED_ARMAMENT_MOLDS.keySet()) {
            replaceImagePalette(item(ItemsNF.UNFIRED_ARMAMENT_MOLDS.get(type)), item(ItemsNF.ARMAMENT_MOLDS.get(type)), CLAY);
        }
        replaceImagePalette(item(ItemsNF.UNFIRED_INGOT_MOLD), item(ItemsNF.INGOT_MOLD), CLAY);
        replaceImagePalette(item(ItemsNF.UNFIRED_ARROWHEAD_MOLD), item(ItemsNF.ARROWHEAD_MOLD), CLAY);
        for(var item : ItemsNF.ARMAMENT_MOLDS.values()) {
            replaceImagePalette(item(item), item(item), TERRACOTTA);
        }
        replaceImagePalette(item(ItemsNF.INGOT_MOLD), item(ItemsNF.INGOT_MOLD), TERRACOTTA);
        replaceImagePalette(item(ItemsNF.ARROWHEAD_MOLD), item(ItemsNF.ARROWHEAD_MOLD), TERRACOTTA);
        for(TieredItemMaterial material : ItemsNF.METAL_ARROWS.keySet()) {
            var item = ItemsNF.METAL_ARROWS.get(material);
            var palette = METAL_PALETTES.get(material.getMetal());
            replaceImagePalette(item(item), item("metal_arrow"),
                    List.of(palette.get(1), palette.get(2), palette.get(3), palette.get(4), palette.get(5), palette.get(7)));
            for(int i = 0; i <= 2; i++) {
                replaceImagePalette(item(item, "_nocked_" + i), item("metal_arrow_nocked_" + i),
                        List.of(palette.get(1), palette.get(2), palette.get(3), palette.get(5), palette.get(7)));
            }
            replaceImagePalette(entity(item, "arrow"), entity("arrow/metal_arrow"),
                    List.of(palette.get(1), palette.get(2), palette.get(3), palette.get(5), palette.get(7)));
        }

        for(TieredItemMaterial material : ItemsNF.METAL_ARMAMENTS.keySet()) {
            boolean tier3 = material.getTier() > 2;
            Metal metal = (Metal) Metal.fromString(material.getName());
            for(Armament armament : ItemsNF.METAL_ARMAMENTS.get(material).keySet()) {
                List<Integer> handleReduced = tier3 ? TREATED_STICK_REDUCED : STICK2;
                List<Integer> handle = tier3 ? TREATED_STICK : STICK3;
                List<Integer> wrappingReduced = tier3 ? LEATHER2 : TWINE2;
                List<Integer> wrapping = tier3 ? LEATHER3 : TWINE3;
                List<Integer> head;
                if(armament == Armament.HAMMER || armament == Armament.SHOVEL) head = METAL_DULL_PALETTES.get(metal);
                else if(armament == Armament.DAGGER || armament == Armament.SWORD || armament == Armament.SABRE) {
                    head = METAL_PALETTES.get(metal);
                    if(tier3) {
                        handle = wrapping;
                        handleReduced = wrappingReduced;
                    }
                }
                else if(armament == Armament.MACE) head = METAL_PALETTES.get(metal);
                else if(armament == Armament.CHISEL) {
                    head = METAL_REDUCED_PALETTES.get(metal);
                    wrapping = null;
                    wrappingReduced = null;
                }
                else head = METAL_REDUCED_PALETTES.get(metal);
                replaceImagePalette(item(ItemsNF.METAL_ARMAMENTS.get(material).get(armament)), item(armament.getName()),
                        handleReduced, head, wrappingReduced);
                replaceImagePalette(itemInv(ItemsNF.METAL_ARMAMENTS.get(material).get(armament)), item(armament.getName() + "_inventory"),
                        handle, head, wrapping);
            }
        }

        for(Style type : Style.values()) {
            for(String material : type.getMaterials()) {
                String name = material + "_" + type.getName();
                splitImageBySaturation(0.5F, 0F, tempArmor(name), armor(name + "_overlay"), armor(name));
            }
        }
        for(var item : ItemsNF.getTieredArmors()) {
            if(!Files.exists(getExternalImagePath(item(item)))) {
                TieredArmorItem tieredArmorItem = item.get();
                ITieredArmorMaterial material = tieredArmorItem.material;
                IMetal metal = Metal.fromString(material.getName());
                if(metal != null) {
                    var palette = METAL_PALETTES.get(metal);
                    EquipmentSlot slot = tieredArmorItem.slot;
                    if(material.getStyle() == Style.SLAYER) {
                        palette = subset(METAL_PALETTES.get(metal), 7, 0);
                    }
                    else if(material.getArmorType() == ArmorType.SCALE) {
                        if(material.getStyle() == Style.SURVIVOR && slot != EquipmentSlot.HEAD) {
                            palette = METAL_SCALE_PALETTES.get(metal);
                        }
                        else if(material.getStyle() == Style.EXPLORER) {
                            if(slot == EquipmentSlot.HEAD || slot == EquipmentSlot.LEGS) {
                                palette = subset(METAL_PALETTES.get(metal), 7, 0);
                            }
                            else if(slot == EquipmentSlot.CHEST) palette = List.of(palette.get(0), palette.get(1),
                                    palette.get(3), palette.get(4), palette.get(5), palette.get(6));
                        }
                    }
                    else if(material.getArmorType() == ArmorType.PLATE) {
                        if(material.getStyle() == Style.SURVIVOR && slot != EquipmentSlot.CHEST) {
                            palette = METAL_PLATE_PALETTES.get(metal);
                        }
                        else if(material.getStyle() == Style.EXPLORER) {
                            if(slot == EquipmentSlot.LEGS) palette = subset(METAL_PALETTES.get(metal), 7, 0);
                            else if(slot == EquipmentSlot.FEET) palette = List.of(palette.get(0), palette.get(1),
                                    palette.get(3), palette.get(4), palette.get(5), palette.get(6), palette.get(7));
                        }
                    }
                    else if(material.getArmorType() == ArmorType.CHAINMAIL) {
                        if(material.getStyle() == Style.SURVIVOR) {
                            palette = METAL_CHAINMAIL_PALETTES.get(metal);
                        }
                        else if(material.getStyle() == Style.EXPLORER) {
                            if(slot == EquipmentSlot.HEAD || slot == EquipmentSlot.LEGS) palette = List.of(palette.get(0), palette.get(1),
                                    palette.get(2), palette.get(3), palette.get(4), palette.get(6), palette.get(7));
                        }
                    }
                    String base = item.getId().getPath().replace(metal.getName() + "_", "");
                    splitImageBySaturation(0.5F, 0F, tempItem(item), item(item), item(base));
                    replaceImagePalette(item(item, "_overlay"),
                            tempItem(item),
                            palette);
                }
            }
        }
        for(TieredArmorMaterial material : TieredArmorMaterial.values()) {
            IMetal metal = Metal.fromString(material.getName());
            if(metal != null && !Files.exists(getExternalImagePath(armor(material)))) {
                replaceImagePalette(armor(material), tempArmor(material.getStyledArmorName()), METAL_PALETTES.get(metal));
            }
        }

        for(Tree type : ItemsNF.ARMOR_STANDS.keySet()) {
            replaceImagePalette(item(ItemsNF.ARMOR_STANDS.get(type)), item("armor_stand"),
                    combine(subset(WOOD_FULL_PALETTES.get(type), 2, 1), subset(WOOD_FULL_PALETTES.get(type), 4, 3)));
            replaceImagePalette(entity("armorstand/" + type.getName() + "_plank"), entity("armorstand/plank"), subset(WOOD_FULL_PALETTES.get(type), 6, 2));
        }

        for(Tree type : ItemsNF.BOATS.keySet()) {
            replaceImagePalette(item(ItemsNF.BOATS.get(type)), item("boat"),
                    combine(subset(WOOD_FULL_PALETTES.get(type), 2, 1), subset(WOOD_FULL_PALETTES.get(type), 4, 3)));
            replaceImagePalette(entity("boat/" + type.getName() + "_plank"), entity("boat/plank"), subset(WOOD_FULL_PALETTES.get(type), 6, 2));
        }

        layerImages(item(ItemsNF.MEAT_STEW), item(ItemsNF.WOODEN_BOWL), item(ItemsNF.MEAT_STEW));
        layerImages(item(ItemsNF.VEGETABLE_STEW), item(ItemsNF.WOODEN_BOWL), item(ItemsNF.VEGETABLE_STEW));
        layerImages(item(ItemsNF.HEARTY_STEW), item(ItemsNF.WOODEN_BOWL), item(ItemsNF.HEARTY_STEW));
        layerImages(item(ItemsNF.SUSPICIOUS_STEW), item(ItemsNF.WOODEN_BOWL), item(ItemsNF.SUSPICIOUS_STEW));
    }
}
