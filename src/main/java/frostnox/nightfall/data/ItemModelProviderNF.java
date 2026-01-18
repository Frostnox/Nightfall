package frostnox.nightfall.data;

import com.google.gson.JsonObject;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.Stone;
import frostnox.nightfall.block.Tree;
import frostnox.nightfall.block.block.pile.PileBlock;
import frostnox.nightfall.client.model.AnimatedItemModelBuilder;
import frostnox.nightfall.data.extensible.TransformTypeNF;
import frostnox.nightfall.item.Armament;
import frostnox.nightfall.item.IActionableItem;
import frostnox.nightfall.item.TieredArmorMaterial;
import frostnox.nightfall.item.item.TieredArmorItem;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.nio.file.Files;
import java.nio.file.Path;

public class ItemModelProviderNF extends ModelProvider<AnimatedItemModelBuilder> {
    protected final String externalPath;

    public ItemModelProviderNF(DataGenerator generator, String id, ExistingFileHelper helper) {
        super(generator, id, ITEM_FOLDER, AnimatedItemModelBuilder::new, helper);
        String outputString = generator.getOutputFolder().toString();
        this.externalPath = outputString.substring(0, outputString.lastIndexOf("\\src\\")) + "\\src\\main\\resources\\assets\\" + id + "\\models\\";
    }

    public AnimatedItemModelBuilder getUnsavedBuilder(String path) {
        AnimatedItemModelBuilder builder = getBuilder(path);
        generatedModels.remove(extendWithFolder(path.contains(":") ? ResourceLocation.parse(path) : ResourceLocation.fromNamespaceAndPath(modid, path)), builder);
        return builder;
    }

    protected Path getExternalImagePath(ResourceLocation loc) {
        return generator.getOutputFolder().getFileSystem().getPath(externalPath + loc.getPath() + ".json");
    }

    protected ResourceLocation extendWithFolder(ResourceLocation location) {
        if(location.getPath().contains("/")) return location;
        return ResourceLocation.fromNamespaceAndPath(location.getNamespace(), folder + "/" + location.getPath());
    }

    protected ResourceLocation resource(Block block) {
        return ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "block/" + block.getRegistryName().getPath());
    }

    protected ResourceLocation itemLoc(Item item) {
        return modLoc("item/" + item.getRegistryName().getPath());
    }

    protected ResourceLocation itemLoc(Item item, String suffix) {
        return modLoc("item/" + item.getRegistryName().getPath() + suffix);
    }

    protected ResourceLocation itemLoc(String name) {
        return modLoc("item/" + name);
    }

    protected ResourceLocation blockLoc(Block block) {
        return modLoc("block/" + block.getRegistryName().getPath());
    }

    protected ResourceLocation blockLoc(Block block, String suffix) {
        return modLoc("block/" + block.getRegistryName().getPath() + suffix);
    }

    protected ResourceLocation blockLoc(String name) {
        return modLoc("block/" + name);
    }

    protected void blockAsDoubleLayerItem(Item item) {
        withExistingParent(item.getRegistryName().getPath(), modLoc("item/generated"))
                .texture("layer0", modLoc("block/" + item.getRegistryName().getPath()))
                .texture("layer1", modLoc("block/" + item.getRegistryName().getPath() + "_overlay"));
    }

    protected void doubleLayerItem(Item item) {
        doubleLayerItem(item, modLoc("item/" + item.getRegistryName().getPath()),
                modLoc("item/" + item.getRegistryName().getPath() + "_overlay"));
    }

    protected void doubleLayerItem(Item item, ResourceLocation loc1, ResourceLocation loc2) {
        withExistingParent(item.getRegistryName().getPath(), modLoc("item/generated"))
                .texture("layer0", loc1).texture("layer1", loc2);
    }

    protected void genericBlockAsItem(Item item) {
        withExistingParent(item.getRegistryName().getPath(), modLoc("item/generated")).texture("layer0", modLoc("block/" + item.getRegistryName().getPath()));
    }

    protected void genericBlockAsItem(Item item, String texture) {
        withExistingParent(item.getRegistryName().getPath(), modLoc("item/generated")).texture("layer0", modLoc("block/" + texture));
    }

    protected void handheldItem(Item item) {
        withExistingParent(item.getRegistryName().getPath(), mcLoc("item/handheld")).texture("layer0", modLoc("item/" + item.getRegistryName().getPath()));
    }

    protected void templateItem(Item item, ResourceLocation parent) {
        withExistingParent(item.getRegistryName().getPath(), parent).texture("layer0", modLoc("item/" + item.getRegistryName().getPath()));
    }

    protected void existingItem(Item item, ResourceLocation parent) {
        withExistingParent(item.getRegistryName().getPath(), parent);
    }

    protected void genericItem(Item item) {
        withExistingParent(item.getRegistryName().getPath(), modLoc("item/generated")).texture("layer0", modLoc("item/" + item.getRegistryName().getPath()));
    }

    protected void genericItem(Item item, Item textureItem) {
        withExistingParent(item.getRegistryName().getPath(), modLoc("item/generated")).texture("layer0", modLoc("item/" + textureItem.getRegistryName().getPath()));
    }

    protected void genericBlock(Item item) {
        withExistingParent(item.getRegistryName().getPath(), modLoc("block/" + item.getRegistryName().getPath()));
    }

    protected void genericBlock(Item item, String suffix) {
        withExistingParent(item.getRegistryName().getPath(), modLoc("block/" + item.getRegistryName().getPath() + suffix));
    }

    protected void chest(Tree type) {
        withExistingParent(ItemsNF.CHESTS.get(type).getId().getPath(), mcLoc("item/chest")).texture("particle", modLoc("block/" + ItemsNF.PLANK_BLOCKS.get(type).getId().getPath()));
    }

    protected void sword(Item item) {
        doubleModelAnimatedItem(item, "sword", 0.6F, -0.6F);
    }

    protected void sabre(Item item) {
        doubleModelAnimatedItem(item, "sabre", 0.6F, -0.65F);
    }

    protected void mace(Item item) {
        doubleModelAnimatedItem(item, "mace", 0.6F, -0.65F);
    }

    protected void axe(Item item) {
        doubleModelAnimatedItem(item, "axe", 0.75F, -0.12F);
    }

    protected void knife(Item item) {
        doubleModelAnimatedItem(item, "knife");
    }

    protected void spear(Item item) {
        doubleModelAnimatedItem(item, "spear", 0.45F, -0.8F);
    }

    protected void chisel(Item item) {
        doubleModelAnimatedItem(item, "chisel");
    }

    protected void hammer(Item item) {
        doubleModelAnimatedItem(item, "hammer", 0.7F, -0.2F);
    }

    protected void shovel(Item item) {
        doubleModelAnimatedItem(item, "shovel", 0.6F, -0.6F);
    }

    protected void pickaxe(Item item) {
        doubleModelAnimatedItem(item, "pickaxe", 0.75F, -0.12F);
    }

    protected void adze(Item item) {
        doubleModelAnimatedItem(item, "adze", 0.8F, -0.1F);
    }

    protected void sickle(Item item) {
        doubleModelAnimatedItem(item, "sickle", 0.75F, -0.25F);
    }

    protected void shield(Item item) {
        JsonObject base = getUnsavedBuilder(item.getRegistryName().getPath()).parent(getExistingFile(modLoc("item/shield")))
                .texture("layer0", itemLoc(item)).texture("layer1", itemLoc(item, "_overlay")).texture("particle", itemLoc(item, "_inventory"))
                .transforms().transform(TransformTypeNF.RACK).rotation(0, -90, 0).translation(5, 2, 0).end().end().toJson();
        JsonObject inventory = getUnsavedBuilder(item.getRegistryName().getPath()).parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", itemLoc(item, "_inventory")).texture("layer1", itemLoc(item, "_inventory_overlay")).toJson();
        getBuilder(item.getRegistryName().getPath()).base(base).inventory(inventory).animatedLoader();
    }

    protected void dyedShield(Item item) {
        String undyedLoc = item.getRegistryName().getPath().toString().replace("_dyed", "");
        JsonObject base = getUnsavedBuilder(item.getRegistryName().getPath()).parent(getExistingFile(modLoc("item/shield")))
                .texture("layer0", itemLoc("shield_dyed")).texture("layer1", itemLoc(undyedLoc + "_overlay")).texture("particle", itemLoc(undyedLoc + "_inventory"))
                .transforms().transform(TransformTypeNF.RACK).rotation(0, -90, 0).translation(5, 2, 0).end().end().toJson();
        JsonObject inventory = getUnsavedBuilder(item.getRegistryName().getPath()).parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", itemLoc("shield_dyed_inventory")).texture("layer1", itemLoc(undyedLoc + "_inventory_overlay")).toJson();
        getBuilder(item.getRegistryName().getPath()).base(base).inventory(inventory).animatedLoader();
    }

    protected void sling(Item item) {
        ModelFile swinging = getBuilder(item.getRegistryName().getPath() + "_swinging").parent(getExistingFile(modLoc("item/template_sling_swinging")))
                .texture("0", itemLoc(item)).texture("particle", itemLoc(item));
        JsonObject base = getUnsavedBuilder(item.getRegistryName().getPath()).itemModelBuilder().parent(getExistingFile(modLoc("item/template_sling")))
                .texture("0", itemLoc(item)).texture("particle", itemLoc(item, "_inventory"))
                .override().predicate(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "swinging"), 1).model(swinging).end().toJson();
        JsonObject inventory = getUnsavedBuilder(item.getRegistryName().getPath()).parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", itemLoc(item, "_inventory")).toJson();
        getBuilder(item.getRegistryName().getPath()).swapSpeed(1).swapYOffset(0).base(base).inventory(inventory).animatedLoader();
    }

    protected void doubleModelAnimatedItem(Item item, String baseModelName) {
        doubleModelAnimatedItem(item, baseModelName, 1F, 0F);
    }

    protected void doubleModelAnimatedItem(Item item, String baseModelName, float swapSpeed, float swapYOffset) {
        JsonObject base = getUnsavedBuilder(item.getRegistryName().getPath()).parent(getExistingFile(modLoc("item/" + baseModelName)))
                .texture("base", itemLoc(item)).texture("particle", itemLoc(item, "_inventory")).toJson();
        JsonObject inventory = getUnsavedBuilder(item.getRegistryName().getPath()).parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", itemLoc(item, "_inventory")).toJson();
        getBuilder(item.getRegistryName().getPath()).swapSpeed(swapSpeed).swapYOffset(swapYOffset).base(base).inventory(inventory).animatedLoader();
    }

    protected void animatedHandheldItem(Item item) {
        AnimatedItemModelBuilder builder = getUnsavedBuilder(item.getRegistryName().getPath())
                .parent(getExistingFile(mcLoc("item/handheld"))).texture("layer0", itemLoc(item));
        getBuilder(item.getRegistryName().getPath()).base(builder.toJson()).inventory(builder.toJson()).animatedLoader();
    }

    protected void animatedItem(Item item) {
        AnimatedItemModelBuilder builder = getUnsavedBuilder(item.getRegistryName().getPath())
                .parent(getExistingFile(mcLoc("item/generated"))).texture("layer0", itemLoc(item));
        getBuilder(item.getRegistryName().getPath()).base(builder.toJson()).inventory(builder.toJson()).animatedLoader();
    }

    protected void bow(Item bow) {
        AnimatedItemModelBuilder builder = getUnsavedBuilder(bow.getRegistryName().getPath())
                .parent(getExistingFile(mcLoc("item/bow"))).texture("layer0", itemLoc(bow));
        bowOverride(builder.itemModelBuilder(), bow, ItemsNF.FLINT_ARROW.get(), ItemsNF.FLINT_ARROW.get().getAmmoId());
        bowOverride(builder.itemModelBuilder(), bow, ItemsNF.BONE_ARROW.get(), ItemsNF.BONE_ARROW.get().getAmmoId());
        bowOverride(builder.itemModelBuilder(), bow, ItemsNF.RUSTED_ARROW.get(), ItemsNF.RUSTED_ARROW.get().getAmmoId());
        for(var arrow : ItemsNF.METAL_ARROWS.values()) bowOverride(builder.itemModelBuilder(), bow, arrow.get(), arrow.get().getAmmoId());
        builder.transforms().transform(TransformTypeNF.RACK).rotation(0, 180, 0).translation(-1.5F, 13 - 1.5F, 7);
        getBuilder(bow.getRegistryName().getPath()).base(builder.toJson()).inventory(builder.toJson()).animatedLoader();
    }

    protected void bowOverride(ItemModelBuilder builder, Item bow, Item arrow, int index) {
        String name = bow.getRegistryName().getPath() + "_" + arrow.getRegistryName().getPath();
        withExistingParent(name + "_pulling_0", mcLoc("item/bow"))
                .texture("layer0", itemLoc(bow, "_pulling_0")).texture("layer1", itemLoc(arrow, "_nocked_0"));
        withExistingParent(name + "_pulling_1", mcLoc("item/bow"))
                .texture("layer0", itemLoc(bow, "_pulling_1")).texture("layer1", itemLoc(arrow, "_nocked_1"));
        withExistingParent(name + "_pulling_2", mcLoc("item/bow"))
                .texture("layer0", itemLoc(bow, "_pulling_2")).texture("layer1", itemLoc(arrow, "_nocked_2"));
        builder.override().predicate(modLoc("pull"), 0.001F).predicate(modLoc("ammo"), index).model(getExistingFile(itemLoc(name + "_pulling_0"))).end()
                .override().predicate(modLoc("pull"), 0.6F).predicate(modLoc("ammo"), index).model(getExistingFile(itemLoc(name + "_pulling_1"))).end()
                .override().predicate(modLoc("pull"), 0.9F).predicate(modLoc("ammo"), index).model(getExistingFile(itemLoc(name + "_pulling_2"))).end();
    }

    @Override
    protected void registerModels() {
        genericBlockAsItem(ItemsNF.SHORT_GRASS.get());
        genericBlockAsItem(ItemsNF.GRASS.get());
        genericBlockAsItem(ItemsNF.TALL_GRASS.get());
        genericBlockAsItem(ItemsNF.SMALL_FERN.get());
        genericBlockAsItem(ItemsNF.FERN.get());
        genericBlockAsItem(ItemsNF.LARGE_FERN.get());
        genericBlockAsItem(ItemsNF.VINES.get());
        genericBlockAsItem(ItemsNF.DEAD_BUSH.get());
        genericBlockAsItem(ItemsNF.DEAD_PLANT.get());
        genericBlockAsItem(ItemsNF.DEAD_CROP.get(), "dead_crop_1");
        genericItem(ItemsNF.SEASHELL.get());
        doubleLayerItem(ItemsNF.BERRY_BUSH.get());
        genericItem(ItemsNF.POTATO_SEEDS.get());
        genericItem(ItemsNF.CARROT_SEEDS.get());
        genericItem(ItemsNF.FLAX_SEEDS.get());
        genericItem(ItemsNF.YARROW_SEEDS.get());

        for(Stone type : Stone.values()) {
            genericBlock(ItemsNF.ROCK_CLUSTERS.get(type).get(), "_3");
        }
        genericBlock(ItemsNF.FLINT_CLUSTER.get(), "_3");

        for(Tree type : Tree.values()) {
            templateItem(ItemsNF.PLANKS.get(type).get(), modLoc("item/handheld_small"));
            genericItem(ItemsNF.LOGS.get(type).get());
            genericItem(ItemsNF.STRIPPED_LOGS.get(type).get());
            genericItem(ItemsNF.TREE_SEEDS.get(type).get());
            withExistingParent(ItemsNF.PLANK_FENCES.get(type).get().getRegistryName().getPath(), blockLoc("fence_inventory")).texture("0", resource(BlocksNF.PLANK_FENCES.get(type).get()));
            withExistingParent(ItemsNF.PLANK_FENCE_GATES.get(type).get().getRegistryName().getPath(), blockLoc("template_fence_gate")).texture("0", resource(BlocksNF.PLANK_FENCES.get(type).get()));
            genericItem(ItemsNF.PLANK_DOORS.get(type).get());
            genericBlock(ItemsNF.PLANK_TRAPDOORS.get(type).get(), "_bottom");
            genericBlockAsItem(ItemsNF.PLANK_LADDERS.get(type).get());
            genericItem(ItemsNF.PLANK_SIGNS.get(type).get());
            genericItem(ItemsNF.WOODEN_ITEM_FRAMES.get(type).get());
            genericItem(ItemsNF.RACKS.get(type).get());
            genericItem((ItemsNF.CHAIRS.get(type).get()));
            genericBlock(ItemsNF.TROUGHS.get(type).get(), "_0");
        }
        genericItem(ItemsNF.CHARRED_LOG.get());

        handheldItem(ItemsNF.STICK.get());
        handheldItem(ItemsNF.LIVING_BONE.get());
        animatedHandheldItem(ItemsNF.BONE.get());
        genericItem(ItemsNF.SNOWBALL_THROWABLE.get(), ItemsNF.SNOWBALL.get());

        templateItem(ItemsNF.TORCH.get(), modLoc("item/handheld_small_flipped"));
        templateItem(ItemsNF.TORCH_UNLIT.get(), modLoc("item/handheld_small_flipped"));
        genericItem(ItemsNF.ROPE.get());
        genericItem(ItemsNF.WOODEN_BOWL.get());
        genericItem(ItemsNF.CAMPFIRE.get());

        for(var material : ItemsNF.METAL_ARMAMENTS.keySet()) {
            var set = ItemsNF.METAL_ARMAMENTS.get(material);
            adze(set.get(Armament.ADZE).get());
            axe(set.get(Armament.AXE).get());
            knife(set.get(Armament.KNIFE).get());
            chisel(set.get(Armament.CHISEL).get());
            hammer(set.get(Armament.HAMMER).get());
            mace(set.get(Armament.MACE).get());
            pickaxe(set.get(Armament.PICKAXE).get());
            sabre(set.get(Armament.SABRE).get());
            sickle(set.get(Armament.SICKLE).get());
            shovel(set.get(Armament.SHOVEL).get());
            spear(set.get(Armament.SPEAR).get());
            sword(set.get(Armament.SWORD).get());
        }
        for(RegistryObject<TieredArmorItem> item : ItemsNF.getTieredArmors()) {
            TieredArmorMaterial material = (TieredArmorMaterial) item.get().material;
            if(material == TieredArmorMaterial.LEATHER ||
                    (material == TieredArmorMaterial.PADDED && item.get().slot != EquipmentSlot.CHEST) ||
                    (material == TieredArmorMaterial.RAGGED && (item.get().slot == EquipmentSlot.HEAD || item.get().slot == EquipmentSlot.FEET))) {
                genericItem(item.get());
            }
            else doubleLayerItem(item.get());
        }
        spear(ItemsNF.FLINT_SPEAR.get());
        spear(ItemsNF.RUSTED_SPEAR.get());
        sling(ItemsNF.SLING.get());
        sling(ItemsNF.SLING_REINFORCED.get());
        for(var item : ItemsNF.BOWS.values()) bow(item.get());
        bow(ItemsNF.TWISTED_BOW.get());
        doubleLayerItem(ItemsNF.POUCH.get());
        shield(ItemsNF.IRONWOOD_SHIELD.get());
        dyedShield(ItemsNF.IRONWOOD_SHIELD_DYED.get());
        genericItem(ItemsNF.WARDING_EFFIGY.get());

        for(var item : ItemsNF.METAL_SHIELDS.values()) shield(item.get());
        for(var item : ItemsNF.METAL_SHIELDS_DYED.values()) dyedShield(item.get());
        for(var item : ItemsNF.LANTERNS_UNLIT.values()) genericItem(item.get());
        for(var item : ItemsNF.LANTERNS.values()) genericItem(item.get());
        for(var item : ItemsNF.ARMAMENT_MOLDS.values()) genericItem(item.get());
        genericItem(ItemsNF.INGOT_MOLD.get());
        genericItem(ItemsNF.ARROWHEAD_MOLD.get());
        for(var item : ItemsNF.UNFIRED_ARMAMENT_MOLDS.values()) genericItem(item.get());
        genericItem(ItemsNF.UNFIRED_INGOT_MOLD.get());
        genericItem(ItemsNF.UNFIRED_ARROWHEAD_MOLD.get());
        genericBlock(ItemsNF.CRUCIBLE.get(), "_none");

        genericBlockAsItem(ItemsNF.SPIDER_WEB.get());
        genericBlock(ItemsNF.DRAKEFOWL_NEST.get(), "_0");

        existingItem(ItemsNF.DRAKEFOWL_ROOSTER_SPAWN_EGG.get(), modLoc("item/template_spawn_egg_male"));
        existingItem(ItemsNF.DRAKEFOWL_HEN_SPAWN_EGG.get(), modLoc("item/template_spawn_egg_female"));
        existingItem(ItemsNF.DRAKEFOWL_CHICK_SPAWN_EGG.get(), modLoc("item/template_spawn_egg_baby"));
        existingItem(ItemsNF.MERBOR_TUSKER_SPAWN_EGG.get(), modLoc("item/template_spawn_egg_male"));
        existingItem(ItemsNF.MERBOR_SOW_SPAWN_EGG.get(), modLoc("item/template_spawn_egg_female"));
        existingItem(ItemsNF.MERBOR_PIGLET_SPAWN_EGG.get(), modLoc("item/template_spawn_egg_baby"));

        for(RegistryObject<? extends Item> item : ItemsNF.ITEMS.getEntries()) {
            ResourceLocation loc = extendWithFolder(ResourceLocation.fromNamespaceAndPath(modid, item.getId().getPath()));
            if(!generatedModels.containsKey(loc) && !Files.exists(getExternalImagePath(loc))) {
                if(item.get() instanceof BlockItem blockItem) {
                    if(blockItem.getBlock() instanceof PileBlock) genericBlock(item.get(), "_8");
                    else genericBlock(item.get());
                }
                else if(item.get() instanceof IActionableItem) animatedItem(item.get());
                else if(item.get() instanceof SpawnEggItem) withExistingParent(item.getId().getPath(), modLoc("item/template_spawn_egg"));
                else genericItem(item.get());
            }
        }
    }

    @Override
    public String getName() {
        return "Item Models: " + modid;
    }
}
