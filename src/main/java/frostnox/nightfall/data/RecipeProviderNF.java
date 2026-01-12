package frostnox.nightfall.data;

import frostnox.nightfall.block.*;
import frostnox.nightfall.data.recipe.BowlCrushingRecipe;
import frostnox.nightfall.data.recipe.CauldronRecipe;
import frostnox.nightfall.data.recipe.HeldToolRecipe;
import frostnox.nightfall.data.recipe.builder.*;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.item.*;
import frostnox.nightfall.registry.EntriesNF;
import frostnox.nightfall.registry.KnowledgeNF;
import frostnox.nightfall.registry.forge.FluidsNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.world.ContinentalWorldType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Consumer;

public class RecipeProviderNF extends RecipeProvider {
    private static final int DAY_LENGTH = (int) ContinentalWorldType.DAY_LENGTH;
    public RecipeProviderNF(DataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        BuildingRecipeBuilder.base(ItemsNF.CLAY.get(), 4, ItemsNF.CLAY_BLOCK.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.CLAY.get(), 4, ItemsNF.CLAY_BRICKS.get()).order(1).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.CLAY.get(), 8, ItemsNF.UNFIRED_POT.get()).order(2).requirement(EntriesNF.POTTERY).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.CLAY.get(), 6, ItemsNF.UNFIRED_CAULDRON.get()).order(3).requirement(EntriesNF.COOKING).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.CLAY.get(), 4, ItemsNF.UNFIRED_CRUCIBLE.get()).order(4).requirement(EntriesNF.POTTERY).save(consumer);
        for(var type : ItemsNF.UNFIRED_ARMAMENT_MOLDS.keySet()) {
            BuildingRecipeBuilder.base(ItemsNF.CLAY.get(), 4, ItemsNF.UNFIRED_ARMAMENT_MOLDS.get(type).get())
                    .requirement(type == Armament.CHISEL ? EntriesNF.CHISEL_MOLD : (type == Armament.SICKLE ? EntriesNF.SICKLE : EntriesNF.CASTING)).save(consumer);
        }
        BuildingRecipeBuilder.base(ItemsNF.CLAY.get(), 4, ItemsNF.UNFIRED_INGOT_MOLD.get()).requirement(EntriesNF.CASTING).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.CLAY.get(), 4, ItemsNF.UNFIRED_ARROWHEAD_MOLD.get()).requirement(EntriesNF.ARROWHEAD_MOLD).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.FIRE_CLAY.get(), 4, ItemsNF.FIRE_CLAY_BLOCK.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.FIRE_CLAY.get(), 4, ItemsNF.FIRE_CLAY_BRICKS.get()).order(1).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.MUD.get(), 4, ItemsNF.MUD_BLOCK.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.MUD.get(), 4, ItemsNF.WET_MUD_BRICKS.get()).order(1).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.SNOWBALL.get(), 1, ItemsNF.SNOWBALL_THROWABLE.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.SNOWBALL.get(), 1, ItemsNF.SNOW.get()).order(1).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.SNOWBALL.get(), 8, ItemsNF.PACKED_SNOW.get()).order(2).save(consumer);
        for(Soil type : Soil.values()) {
            Item soil = ItemsNF.SOILS.get(type).get();
            Item soilBlock = switch(type) {
                case SILT -> ItemsNF.SILT.get();
                case DIRT -> ItemsNF.DIRT.get();
                case LOAM -> ItemsNF.LOAM.get();
                case ASH -> ItemsNF.ASH.get();
                case GRAVEL -> ItemsNF.GRAVEL.get();
                case BLUE_GRAVEL -> ItemsNF.BLUE_GRAVEL.get();
                case BLACK_GRAVEL -> ItemsNF.BLACK_GRAVEL.get();
                case SAND -> ItemsNF.SAND.get();
                case RED_SAND -> ItemsNF.RED_SAND.get();
                case WHITE_SAND -> ItemsNF.WHITE_SAND.get();
            };
            BuildingRecipeBuilder.base(soil, 4, soilBlock).order(0).save(consumer);
        }
        for(Stone type : Stone.values()) {
            Item rock = ItemsNF.ROCKS.get(type).get();
            Item brick = ItemsNF.STONE_BRICKS.get(type).get();
            BuildingRecipeBuilder.base(rock, 1, ItemsNF.ROCK_CLUSTERS.get(type).get()).order(0).save(consumer);
            BuildingRecipeBuilder.base(rock, 4, ItemsNF.STACKED_STONE.get(type).get()).order(1).save(consumer);
            BuildingRecipeBuilder.base(rock, 3, ItemsNF.STACKED_STONE_STAIRS.get(type).get()).order(2).save(consumer);
            BuildingRecipeBuilder.base(rock, 2, ItemsNF.STACKED_STONE_SLABS.get(type).get()).order(3).save(consumer);
            BuildingRecipeBuilder.base(rock, 2, ItemsNF.STACKED_STONE_SIDINGS.get(type).get()).order(4).save(consumer);
            BuildingRecipeBuilder.base(rock, 4, ItemsNF.COBBLED_STONE.get(type).get()).addExtra(TagsNF.COBBLE_MORTAR).order(5).save(consumer);
            BuildingRecipeBuilder.base(rock, 3, ItemsNF.COBBLED_STONE_STAIRS.get(type).get()).addExtra(TagsNF.COBBLE_MORTAR).order(6).save(consumer);
            BuildingRecipeBuilder.base(rock, 2, ItemsNF.COBBLED_STONE_SLABS.get(type).get()).addExtra(TagsNF.COBBLE_MORTAR).order(7).save(consumer);
            BuildingRecipeBuilder.base(rock, 2, ItemsNF.COBBLED_STONE_SIDINGS.get(type).get()).addExtra(TagsNF.COBBLE_MORTAR).order(8).save(consumer);
            BuildingRecipeBuilder.base(brick, 4, ItemsNF.STONE_BRICK_BLOCKS.get(type).get()).order(0).save(consumer);
            BuildingRecipeBuilder.base(brick, 3, ItemsNF.STONE_BRICK_STAIRS.get(type).get()).order(1).save(consumer);
            BuildingRecipeBuilder.base(brick, 2, ItemsNF.STONE_BRICK_SLABS.get(type).get()).order(2).save(consumer);
            BuildingRecipeBuilder.base(brick, 2, ItemsNF.STONE_BRICK_SIDINGS.get(type).get()).order(3).save(consumer);
            HeldToolRecipe.saveHeldTool(Ingredient.of(rock), Ingredient.of(TagsNF.RECIPE_TOOL_STONE_CARVE), brick, 1, -1, entryKnowledge(EntriesNF.TOOLS), consumer);
        }
        for(Tree type : Tree.values()) {
            Item plank = ItemsNF.PLANKS.get(type).get();
            BuildingRecipeBuilder.base(plank, 4, ItemsNF.PLANK_BLOCKS.get(type).get()).order(0).save(consumer);
            BuildingRecipeBuilder.base(plank, 3, ItemsNF.PLANK_STAIRS.get(type).get()).order(1).save(consumer);
            BuildingRecipeBuilder.base(plank, 2, ItemsNF.PLANK_SLABS.get(type).get()).order(2).save(consumer);
            BuildingRecipeBuilder.base(plank, 2, ItemsNF.PLANK_SIDINGS.get(type).get()).order(3).save(consumer);
            BuildingRecipeBuilder.base(plank, 3, ItemsNF.PLANK_FENCES.get(type).get()).order(4).requirement(EntriesNF.WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 3, ItemsNF.PLANK_FENCE_GATES.get(type).get()).order(5).requirement(EntriesNF.WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 6, ItemsNF.PLANK_DOORS.get(type).get()).order(6).requirement(EntriesNF.WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 3, ItemsNF.PLANK_TRAPDOORS.get(type).get()).order(7).requirement(EntriesNF.ADVANCED_WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 3, ItemsNF.PLANK_HATCHES.get(type).get()).order(8).requirement(EntriesNF.ADVANCED_WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 3, ItemsNF.PLANK_LADDERS.get(type).get()).order(9).requirement(EntriesNF.WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 3, ItemsNF.PLANK_SIGNS.get(type).get()).order(11).requirement(EntriesNF.WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 2, ItemsNF.WOODEN_ITEM_FRAMES.get(type).get()).requirement(EntriesNF.ADVANCED_WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 12, ItemsNF.ARMOR_STANDS.get(type).get()).requirement(EntriesNF.ADVANCED_WOODWORKING).save(consumer);
            if(ItemsNF.BOATS.containsKey(type)) BuildingRecipeBuilder.base(plank, 16, ItemsNF.BOATS.get(type).get()).requirement(EntriesNF.ADVANCED_WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 16, ItemsNF.BARRELS.get(type).get()).requirement(EntriesNF.TANNING).save(consumer);
            BuildingRecipeBuilder.base(plank, 16, ItemsNF.CHESTS.get(type).get()).order(10).requirement(EntriesNF.ADVANCED_WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 4, ItemsNF.RACKS.get(type).get()).requirement(EntriesNF.WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 8, ItemsNF.SHELVES.get(type).get()).requirement(EntriesNF.WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 4, ItemsNF.CHAIRS.get(type).get()).requirement(EntriesNF.ADVANCED_WOODWORKING).save(consumer);
            BuildingRecipeBuilder.base(plank, 8, ItemsNF.TROUGHS.get(type).get()).requirement(EntriesNF.TROUGH).save(consumer);
            if(ItemsNF.BOWS.containsKey(type)) CraftingRecipeBuilder.base(ItemsNF.BOWS.get(type).get(), EntriesNF.BOW_AND_ARROW).define('P', plank).define('L', ItemsNF.FLAX_FIBERS.get()).pattern(" PL").pattern("P L").pattern(" PL").save(consumer);
        }
        for(Metal type : ItemsNF.INGOTS.keySet()) {
            BuildingRecipeBuilder.base(ItemsNF.INGOTS.get(type).get(), 4, ItemsNF.INGOT_PILES.get(type).get()).order(0).save(consumer);
        }

        BuildingRecipeBuilder.base(ItemsNF.TERRACOTTA_SHARD.get(), 4, ItemsNF.TERRACOTTA_TILES.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.TERRACOTTA_SHARD.get(), 3, ItemsNF.TERRACOTTA_TILE_STAIRS.get()).order(1).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.TERRACOTTA_SHARD.get(), 2, ItemsNF.TERRACOTTA_TILE_SLAB.get()).order(2).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.TERRACOTTA_SHARD.get(), 2, ItemsNF.TERRACOTTA_TILE_SIDING.get()).order(3).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.TERRACOTTA_SHARD.get(), 4, ItemsNF.TERRACOTTA_MOSAIC.get()).order(4).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.TERRACOTTA_SHARD.get(), 3, ItemsNF.TERRACOTTA_MOSAIC_STAIRS.get()).order(5).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.TERRACOTTA_SHARD.get(), 2, ItemsNF.TERRACOTTA_MOSAIC_SLAB.get()).order(6).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.TERRACOTTA_SHARD.get(), 2, ItemsNF.TERRACOTTA_MOSAIC_SIDING.get()).order(7).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.BRICK.get(), 4, ItemsNF.BRICKS.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.BRICK.get(), 3, ItemsNF.BRICK_STAIRS.get()).order(1).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.BRICK.get(), 2, ItemsNF.BRICK_SLAB.get()).order(2).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.BRICK.get(), 2, ItemsNF.BRICK_SIDING.get()).order(3).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.MUD_BRICK.get(), 4, ItemsNF.MUD_BRICKS.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.MUD_BRICK.get(), 3, ItemsNF.MUD_BRICK_STAIRS.get()).order(1).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.MUD_BRICK.get(), 2, ItemsNF.MUD_BRICK_SLAB.get()).order(2).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.MUD_BRICK.get(), 2, ItemsNF.MUD_BRICK_SIDING.get()).order(3).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.FIRE_BRICK.get(), 4, ItemsNF.FIRE_BRICKS.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.FIRE_BRICK.get(), 3, ItemsNF.FIRE_BRICK_STAIRS.get()).order(1).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.FIRE_BRICK.get(), 2, ItemsNF.FIRE_BRICK_SLAB.get()).order(2).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.FIRE_BRICK.get(), 2, ItemsNF.FIRE_BRICK_SIDING.get()).order(3).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.PLANT_FIBERS.get(), 4, ItemsNF.THATCH.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.PLANT_FIBERS.get(), 3, ItemsNF.THATCH_STAIRS.get()).order(1).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.PLANT_FIBERS.get(), 2, ItemsNF.THATCH_SLAB.get()).order(2).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.PLANT_FIBERS.get(), 2, ItemsNF.THATCH_SIDING.get()).order(3).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.GLASS.get(), 4, ItemsNF.GLASS_BLOCK.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.GLASS.get(), 2, ItemsNF.GLASS_SLAB.get()).order(1).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.GLASS.get(), 2, ItemsNF.GLASS_SIDING.get()).order(2).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.FIREWOOD.get(), 8, ItemsNF.FIREWOOD_BLOCK.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.FIREWOOD.get(), 4, ItemsNF.CAMPFIRE.get()).order(1).requirement(EntriesNF.CAMPFIRE).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.COAL.get(), 4, ItemsNF.COAL_BLOCK.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.CHARCOAL.get(), 4, ItemsNF.CHARCOAL_BLOCK.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.COKE.get(), 4, ItemsNF.COKE_BLOCK.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.SLAG.get(), 4, ItemsNF.SLAG_BLOCK.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.AZURITE_CHUNK.get(), 4, ItemsNF.AZURITE_BLOCK.get()).order(0).save(consumer);
        BuildingRecipeBuilder.base(ItemsNF.HEMATITE_CHUNK.get(), 4, ItemsNF.HEMATITE_BLOCK.get()).order(0).save(consumer);

        SingleRecipeBuilder.base(ItemsNF.ROASTED_POTATO.get(), 1).input(ItemsNF.POTATO.get()).cookTime(60 * 20).requirement(EntriesNF.CAMPFIRE.getId()).saveCampfire(consumer);
        SingleRecipeBuilder.base(ItemsNF.ROASTED_CARROT.get(), 1).input(ItemsNF.CARROT.get()).cookTime(60 * 20).requirement(EntriesNF.CAMPFIRE.getId()).saveCampfire(consumer);
        SingleRecipeBuilder.base(ItemsNF.COOKED_GAME.get(), 1).input(ItemsNF.RAW_GAME.get()).cookTime(60 * 20).requirement(EntriesNF.CAMPFIRE.getId()).saveCampfire(consumer);
        SingleRecipeBuilder.base(ItemsNF.COOKED_VENISON.get(), 1).input(ItemsNF.RAW_VENISON.get()).cookTime(60 * 20).requirement(EntriesNF.CAMPFIRE.getId()).saveCampfire(consumer);
        SingleRecipeBuilder.base(ItemsNF.COOKED_POULTRY.get(), 1).input(ItemsNF.RAW_POULTRY.get()).cookTime(60 * 20).requirement(EntriesNF.CAMPFIRE.getId()).saveCampfire(consumer);
        SingleRecipeBuilder.base(ItemsNF.COOKED_PORK.get(), 1).input(ItemsNF.RAW_PORK.get()).cookTime(60 * 20).requirement(EntriesNF.CAMPFIRE.getId()).saveCampfire(consumer);
        SingleRecipeBuilder.base(ItemsNF.COOKED_PALE_FLESH.get(), 1).input(ItemsNF.RAW_PALE_FLESH.get()).cookTime(60 * 20).requirement(EntriesNF.CAMPFIRE.getId()).saveCampfire(consumer);

        MixtureRecipeBuilder.base(ItemsNF.MEAT_STEW.get())
                .addIngredient(ItemsNF.WATER.get(), 1, 1)
                .addIngredient(TagsNF.MEAT, 3, Float.MAX_VALUE)
                .unitsPerOutput(4).cookTime(CauldronRecipe.COOK_TIME).priority(1).requirement(itemKnowledge(ItemsNF.MEAT_STEW)).saveCauldron(consumer);
        MixtureRecipeBuilder.base(ItemsNF.VEGETABLE_STEW.get())
                .addIngredient(ItemsNF.WATER.get(), 1, 1)
                .addIngredient(TagsNF.MEAT, 0, 1)
                .addIngredient(TagsNF.VEGETABLE, 2, Float.MAX_VALUE)
                .unitsPerOutput(4).cookTime(CauldronRecipe.COOK_TIME).priority(1).requirement(itemKnowledge(ItemsNF.VEGETABLE_STEW)).saveCauldron(consumer);
        MixtureRecipeBuilder.base(ItemsNF.HEARTY_STEW.get())
                .addIngredient(ItemsNF.WATER.get(), 1, 1)
                .addIngredient(TagsNF.MEAT, 2, Float.MAX_VALUE)
                .addIngredient(TagsNF.VEGETABLE, 1, Float.MAX_VALUE)
                .unitsPerOutput(4).cookTime(CauldronRecipe.COOK_TIME).priority(1).requirement(itemKnowledge(ItemsNF.HEARTY_STEW)).saveCauldron(consumer);
        MixtureRecipeBuilder.base(ItemsNF.HAM_ROAST.get())
                .addIngredient(ItemsNF.WATER.get(), 1, 1)
                .addIngredient(TagsNF.PORK, 2, Float.MAX_VALUE)
                .unitsPerOutput(4).cookTime(CauldronRecipe.COOK_TIME).priority(2).requirement(itemKnowledge(ItemsNF.HAM_ROAST)).saveCauldron(consumer);
        MixtureRecipeBuilder.base(ItemsNF.BOILED_EGG.get())
                .addIngredient(ItemsNF.WATER.get(), 1, 1)
                .addIngredient(TagsNF.EGG, 2, Float.MAX_VALUE)
                .unitsPerOutput(3).cookTime(CauldronRecipe.COOK_TIME).priority(0).requirement(KnowledgeNF.ITEM_TAGS.get(TagsNF.EGG).getId()).saveCauldron(consumer);
        MixtureRecipeBuilder.base(ItemsNF.SOUFFLE.get())
                .addIngredient(ItemsNF.WATER.get(), 0, 0)
                .addIngredient(TagsNF.EGG, 2, Float.MAX_VALUE)
                .unitsPerOutput(4).cookTime(CauldronRecipe.COOK_TIME).priority(1).requirement(itemKnowledge(ItemsNF.SOUFFLE)).saveCauldron(consumer);
        MixtureRecipeBuilder.base(ItemsNF.FRUIT_SOUFFLE.get())
                .addIngredient(ItemsNF.WATER.get(), 0, 0)
                .addIngredient(TagsNF.EGG, 2, Float.MAX_VALUE)
                .addIngredient(TagsNF.FRUIT, 2, Float.MAX_VALUE)
                .unitsPerOutput(4).cookTime(CauldronRecipe.COOK_TIME).priority(2).requirement(itemKnowledge(ItemsNF.FRUIT_SOUFFLE)).saveCauldron(consumer);
        MixtureRecipeBuilder.base(ItemsNF.SAVORY_SOUFFLE.get())
                .addIngredient(ItemsNF.WATER.get(), 0, 0)
                .addIngredient(TagsNF.EGG, 2, Float.MAX_VALUE)
                .addIngredient(TagsNF.FRUIT_OR_VEGETABLE, 1, Float.MAX_VALUE)
                .addIngredient(TagsNF.MEAT, 1, Float.MAX_VALUE)
                .unitsPerOutput(4).cookTime(CauldronRecipe.COOK_TIME).priority(2).requirement(itemKnowledge(ItemsNF.SAVORY_SOUFFLE)).saveCauldron(consumer);

        BowlCrushingRecipe.saveBowl(Ingredient.of(ItemsNF.YARROW.get()), Ingredient.of(TagsNF.CHISEL), ItemsNF.YARROW_POWDER.get(), 1, EntriesNF.WOODCARVING.getId(), consumer);
        BowlCrushingRecipe.saveBowl(Ingredient.of(TagsNF.CRUSHABLE_TO_LIME), Ingredient.of(TagsNF.CHISEL_OR_HAMMER), ItemsNF.LIME.get(), 1, EntriesNF.WOODCARVING.getId(), consumer);
        BowlCrushingRecipe.saveBowl(Ingredient.of(TagsNF.CRUSHABLE_TO_BONE_SHARD), Ingredient.of(TagsNF.HAMMER), ItemsNF.BONE_SHARD.get(), 2, EntriesNF.WOODCARVING.getId(), consumer);

        HeldToolRecipe.saveHeldTool(Ingredient.of(ItemTags.LOGS), Ingredient.of(TagsNF.RECIPE_TOOL_WOOD_COMPLEX), ItemsNF.WOODEN_BOWL.get(), 1, -1, EntriesNF.WOODCARVING.getId(), consumer);
        HeldToolRecipe.saveHeldTool(Ingredient.of(ItemTags.LOGS), Ingredient.of(TagsNF.RECIPE_TOOL_WOOD_SIMPLE), ItemsNF.WOODEN_CLUB.get(), 1, -1, EntriesNF.WOODCARVING.getId(), consumer);
        HeldToolRecipe.saveHeldTool(Ingredient.of(ItemsNF.COCONUT.get()), Ingredient.of(TagsNF.RECIPE_TOOL_COCONUT), ItemsNF.COCONUT_HALF.get(), 2, -1, entryKnowledge(EntriesNF.TOOLS), consumer);
        HeldToolRecipe.saveHeldTool(Ingredient.of(ItemsNF.FLINT.get()), Ingredient.of(TagsNF.RECIPE_TOOL_FLINT), ItemsNF.FLINT_ARMAMENT_HEADS.get(Armament.ADZE).get(), 1, 0, EntriesNF.TOOLS.getId(), consumer);
        HeldToolRecipe.saveHeldTool(Ingredient.of(ItemsNF.FLINT.get()), Ingredient.of(TagsNF.RECIPE_TOOL_FLINT), ItemsNF.FLINT_ARMAMENT_HEADS.get(Armament.AXE).get(), 1, 1, EntriesNF.TOOLS.getId(), consumer);
        HeldToolRecipe.saveHeldTool(Ingredient.of(ItemsNF.FLINT.get()), Ingredient.of(TagsNF.RECIPE_TOOL_FLINT), ItemsNF.FLINT_ARMAMENT_HEADS.get(Armament.DAGGER).get(), 1, 2, EntriesNF.TOOLS.getId(), consumer);
        HeldToolRecipe.saveHeldTool(Ingredient.of(ItemsNF.FLINT.get()), Ingredient.of(TagsNF.RECIPE_TOOL_FLINT), ItemsNF.FLINT_ARMAMENT_HEADS.get(Armament.HAMMER).get(), 1, 3, EntriesNF.TOOLS.getId(), consumer);
        HeldToolRecipe.saveHeldTool(Ingredient.of(ItemsNF.FLINT.get()), Ingredient.of(TagsNF.RECIPE_TOOL_FLINT), ItemsNF.FLINT_ARMAMENT_HEADS.get(Armament.SHOVEL).get(), 1, 4, EntriesNF.TOOLS.getId(), consumer);
        HeldToolRecipe.saveHeldTool(Ingredient.of(ItemsNF.FLINT.get()), Ingredient.of(TagsNF.RECIPE_TOOL_FLINT), ItemsNF.FLINT_ARMAMENT_HEADS.get(Armament.SPEAR).get(), 1, 5, EntriesNF.TOOLS.getId(), consumer);
        HeldToolRecipe.saveHeldTool(Ingredient.of(ItemsNF.FLINT.get()), Ingredient.of(TagsNF.RECIPE_TOOL_FLINT), ItemsNF.FLINT_CHISEL.get(), 1, 6, EntriesNF.TOOLS.getId(), consumer);

        CraftingRecipeBuilder.base(ItemsNF.FLINT_ADZE.get(), EntriesNF.TOOLS).define('F', ItemsNF.FLINT.get()).define('X', ItemsNF.PLANT_FIBERS.get()).define('S', ItemsNF.STICK.get()).pattern(" X").pattern("FS").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.FLINT_AXE.get(), EntriesNF.TOOLS).define('F', ItemsNF.FLINT.get()).define('X', ItemsNF.PLANT_FIBERS.get()).define('S', ItemsNF.STICK.get()).pattern("FX").pattern(" S").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.FLINT_DAGGER.get(), EntriesNF.TOOLS).define('F', ItemsNF.FLINT.get()).define('S', ItemsNF.STICK.get()).pattern("F").pattern("S").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.FLINT_HAMMER.get(), EntriesNF.TOOLS).define('F', ItemsNF.FLINT.get()).define('X', ItemsNF.PLANT_FIBERS.get()).define('S', ItemsNF.STICK.get()).pattern("FXF").pattern(" S ").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.FLINT_SHOVEL.get(), EntriesNF.TOOLS).define('F', ItemsNF.FLINT.get()).define('X', ItemsNF.PLANT_FIBERS.get()).define('S', ItemsNF.STICK.get()).pattern("F").pattern("X").pattern("S").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.FLINT_SPEAR.get(), EntriesNF.TOOLS).define('F', ItemsNF.FLINT.get()).define('S', ItemsNF.STICK.get()).pattern("F  ").pattern(" S ").pattern("  S").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.ROPE.get(), EntriesNF.TOOLS).define('F', ItemsNF.PLANT_FIBERS.get()).pattern("F").pattern("F").pattern("F").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.FIBER_BANDAGE.get(), EntriesNF.TOOLS).define('F', ItemsNF.PLANT_FIBERS.get()).pattern("FF").pattern("FF").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.FLINT_CHISEL.get(), EntriesNF.TOOLS).define('F', ItemsNF.FLINT.get()).pattern("F").pattern("F").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.WOODEN_BUCKET.get(), EntriesNF.WOODWORKING).define('P', TagsNF.PLANK).define('R', ItemsNF.ROPE.get()).pattern(" R ").pattern("P P").pattern("PPP").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.IRONWOOD_SHIELD.get(), EntriesNF.WOODEN_SHIELD).define('P', ItemsNF.PLANKS.get(Tree.IRONWOOD).get()).pattern("PPP").pattern("PPP").pattern("PPP").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.TORCH_UNLIT.get(), EntriesNF.CAMPFIRE).define('S', ItemsNF.STICK.get()).define('F', ItemsNF.PLANT_FIBERS.get()).pattern("F").pattern("S").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.HELMETS.get(TieredArmorMaterial.LEATHER).get(), EntriesNF.TANNING).define('A', ItemsNF.LEATHER.get()).pattern("AAA").pattern("A A").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.LEATHER).get(), EntriesNF.TANNING).define('A', ItemsNF.LEATHER.get()).pattern("A A").pattern("AAA").pattern("AAA").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.LEGGINGS.get(TieredArmorMaterial.LEATHER).get(), EntriesNF.TANNING).define('A', ItemsNF.LEATHER.get()).pattern("AAA").pattern("A A").pattern("A A").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.BOOTS.get(TieredArmorMaterial.LEATHER).get(), EntriesNF.TANNING).define('A', ItemsNF.LEATHER.get()).pattern("A A").pattern("A A").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.BACKPACK.get(), EntriesNF.TANNING).define('A', ItemsNF.LEATHER.get()).pattern("AA").pattern("AA").pattern("AA").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.LINEN.get(), EntriesNF.WEAVING).define('L', ItemsNF.FLAX_FIBERS.get()).pattern("LL").pattern("LL").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.MASK.get(), EntriesNF.WEAVING).define('L', ItemsNF.LINEN.get()).pattern("LLL").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.POUCH.get(), EntriesNF.WEAVING).define('L', ItemsNF.LINEN.get()).pattern("L L").pattern(" L ").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.BANDAGE.get(), EntriesNF.WEAVING).define('L', ItemsNF.LINEN.get()).pattern("L").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.HELMETS.get(TieredArmorMaterial.PADDED).get(), EntriesNF.WEAVING).define('A', ItemsNF.LINEN.get()).pattern("AAA").pattern("A A").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.PADDED).get(), EntriesNF.WEAVING).define('A', ItemsNF.LINEN.get()).pattern("A A").pattern("AAA").pattern("AAA").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.LEGGINGS.get(TieredArmorMaterial.PADDED).get(), EntriesNF.WEAVING).define('A', ItemsNF.LINEN.get()).pattern("AAA").pattern("A A").pattern("A A").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.BOOTS.get(TieredArmorMaterial.PADDED).get(), EntriesNF.WEAVING).define('A', ItemsNF.LINEN.get()).pattern("A A").pattern("A A").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.MEDICINAL_BANDAGE.get(), EntriesNF.MEDICINAL_BANDAGE).define('B', ItemsNF.BANDAGE.get()).define('M', ItemsNF.YARROW_POWDER.get()).pattern("M").pattern("B").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.FLINT_ARROW.get(), EntriesNF.BOW_AND_ARROW).define('A', ItemsNF.FLINT.get()).define('S', ItemsNF.STICK.get()).define('F', TagsNF.FLETCHING).pattern("A").pattern("S").pattern("F").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.BONE_ARROW.get(), EntriesNF.BONE_ARROW).define('A', ItemsNF.BONE_SHARD.get()).define('S', ItemsNF.STICK.get()).define('F', TagsNF.FLETCHING).pattern("A").pattern("S").pattern("F").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.SLING.get(), EntriesNF.SLING).define('F', ItemsNF.ROPE.get()).pattern(" FF").pattern("F F").pattern(" F ").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.SLING_REINFORCED.get(), EntriesNF.REINFORCED_SLING).define('F', ItemsNF.ROPE.get()).define('L', ItemsNF.LEATHER.get()).pattern(" FL").pattern("F F").pattern(" F ").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.WARDING_CHARM.get(), EntriesNF.WARDING_CHARM).define('F', ItemsNF.PLANT_FIBERS.get()).define('B', ItemsNF.LIVING_BONE.get()).pattern(" F ").pattern("F F").pattern(" B ").save(consumer);
        CraftingRecipeBuilder.base(ItemsNF.WARDING_EFFIGY.get(), EntriesNF.WARDING_EFFIGY).define('F', ItemsNF.ROTTEN_FLESH.get()).define('B', ItemsNF.LIVING_BONE.get()).define('H', ItemsNF.DREG_HEART.get()).pattern("FFF").pattern("BHB").pattern("FBF").save(consumer);
        for(TieredItemMaterial material : ItemsNF.METAL_ARMAMENTS.keySet()) {
            var armaments = ItemsNF.METAL_ARMAMENTS.get(material);
            for(Armament armament : armaments.keySet()) {
                if(armament == Armament.CHISEL) {
                    var head = ItemsNF.ARMAMENT_HEADS.get(material).get(armament);
                    CraftingRecipeBuilder.base(armaments.get(armament).get(), KnowledgeNF.ITEMS.get(head)).define('H', head.get())
                            .define('S', ItemsNF.STICK.get()).pattern("H").pattern("S").save(consumer);
                }
                else {
                    var head = armament == Armament.HAMMER ? ItemsNF.INGOTS.get(material.getMetal()) : ItemsNF.ARMAMENT_HEADS.get(material).get(armament);
                    if(material.getTier() < 3) CraftingRecipeBuilder.base(armaments.get(armament).get(), KnowledgeNF.ITEMS.get(head)).define('H', head.get())
                            .define('S', ItemsNF.STICK.get()).define('X', ItemsNF.PLANT_FIBERS.get()).pattern("H").pattern("X").pattern("S").save(consumer);
                    else CraftingRecipeBuilder.base(armaments.get(armament).get()).define('H', head.get())
                            .define('S', ItemsNF.STICK.get()).define('X', ItemsNF.LEATHER.get()).pattern("H").pattern("X").pattern("S").save(consumer);
                }
            }
        }
        for(Metal metal : ItemsNF.CHAINMAIL.keySet()) {
            CraftingRecipeBuilder.base(ItemsNF.CHAINMAIL.get(metal).get(), EntriesNF.CHAINMAIL_ARMOR)
                    .define('W', ItemsNF.WIRES.get(metal).get()).pattern("WW").pattern("WW").save(consumer);
        }
        CraftingRecipeBuilder.base(ItemsNF.BRONZE_BUCKET.get(), EntriesNF.BUCKET).define('P', ItemsNF.PLATES.get(Metal.BRONZE).get()).define('W', ItemsNF.WIRES.get(Metal.BRONZE).get()).pattern("W").pattern("P").save(consumer);
        for(TieredArmorMaterial material : TieredArmorMaterial.values()) {
            Metal metal = (Metal) Metal.fromString(material.getName());
            ArmorType type = material.getArmorType();
            if(metal != null && type != null) {
                IStyle style = material.getStyle();
                if(style == Style.SURVIVOR) {
                    Item armorMat = switch(type) {
                        case PLATE -> ItemsNF.PLATES.get(metal).get();
                        case SCALE -> ItemsNF.SCALES.get(metal).get();
                        case CHAINMAIL -> ItemsNF.CHAINMAIL.get(metal).get();
                    };
                    RegistryObject<?> entry = switch(type) {
                        case PLATE -> EntriesNF.PLATE_ARMOR;
                        case SCALE -> EntriesNF.SCALE_ARMOR;
                        case CHAINMAIL -> EntriesNF.CHAINMAIL_ARMOR;
                    };
                    TieredArmorMaterial baseMat = type == ArmorType.SCALE ? TieredArmorMaterial.LEATHER : TieredArmorMaterial.PADDED;
                    CraftingRecipeBuilder.base(ItemsNF.HELMETS.get(material).get(), entry).define('A', armorMat).define('B', ItemsNF.HELMETS.get(baseMat).get()).pattern("ABA").save(consumer);
                    CraftingRecipeBuilder.base(ItemsNF.CHESTPLATES.get(material).get(), entry).define('A', armorMat).define('B', ItemsNF.CHESTPLATES.get(baseMat).get()).pattern(" A ").pattern("ABA").pattern(" A ").save(consumer);
                    CraftingRecipeBuilder.base(ItemsNF.LEGGINGS.get(material).get(), entry).define('A', armorMat).define('B', ItemsNF.LEGGINGS.get(baseMat).get()).pattern(" A ").pattern("ABA").save(consumer);
                    CraftingRecipeBuilder.base(ItemsNF.BOOTS.get(material).get(), entry).define('A', armorMat).define('B', ItemsNF.BOOTS.get(baseMat).get()).pattern("ABA").save(consumer);
                }
                else {
                    Ingredient styleMat = null;
                    RegistryObject<?> entry = null;
                    if(style == Style.EXPLORER) {
                        styleMat = Ingredient.of(ItemsNF.RAWHIDE.get());
                        entry = switch(type) {
                            case PLATE -> EntriesNF.EXPLORER_PLATE;
                            case SCALE -> EntriesNF.EXPLORER_SCALE;
                            case CHAINMAIL -> EntriesNF.EXPLORER_CHAINMAIL;
                        };
                    }
                    else if(style == Style.SLAYER) {
                        styleMat = Ingredient.of(TagsNF.MONSTER_HIDE);
                        entry = switch(type) {
                            case PLATE -> EntriesNF.SLAYER_PLATE;
                            case SCALE -> EntriesNF.SLAYER_SCALE;
                            case CHAINMAIL -> EntriesNF.SLAYER_CHAINMAIL;
                        };
                    }
                    ITieredArmorMaterial baseMat = TieredArmorMaterial.fromString(metal.getName() + "_" + type.name().toLowerCase() + "_survivor");
                    CraftingRecipeBuilder.base(ItemsNF.HELMETS.get(material).get(), entry).define('S', styleMat).define('B', ItemsNF.HELMETS.get(baseMat).get()).pattern("S").pattern("B").save(consumer);
                    CraftingRecipeBuilder.base(ItemsNF.CHESTPLATES.get(material).get(), entry).define('S', styleMat).define('B', ItemsNF.CHESTPLATES.get(baseMat).get()).pattern("S").pattern("B").save(consumer);
                    CraftingRecipeBuilder.base(ItemsNF.LEGGINGS.get(material).get(), entry).define('S', styleMat).define('B', ItemsNF.LEGGINGS.get(baseMat).get()).pattern("S").pattern("B").save(consumer);
                    CraftingRecipeBuilder.base(ItemsNF.BOOTS.get(material).get(), entry).define('S', styleMat).define('B', ItemsNF.BOOTS.get(baseMat).get()).pattern("S").pattern("B").save(consumer);
                }
            }
        }
        for(Metal metal : ItemsNF.METAL_SHIELDS.keySet()) {
            CraftingRecipeBuilder.base(ItemsNF.METAL_SHIELDS.get(metal).get(), EntriesNF.SHIELD).define('S', TagsNF.WOODEN_SHIELD).define('W', ItemsNF.WIRES.get(metal).get()).pattern("WWW").pattern("WSW").pattern("WWW").save(consumer);
        }

        SingleRecipeBuilder.base(FluidsNF.METAL.get(Metal.COPPER).get(), 20).input(ItemsNF.COPPER_NUGGET.get()).cookTime(20 * 5).saveCrucible(consumer);
        SingleRecipeBuilder.base(FluidsNF.METAL.get(Metal.COPPER).get(), 100).input(ItemsNF.COPPER_CHUNK.get()).cookTime(20 * 20).saveCrucible(consumer);
        SingleRecipeBuilder.base(FluidsNF.METAL.get(Metal.TIN).get(), 20).input(ItemsNF.TIN_NUGGET.get()).cookTime(20 * 5).saveCrucible(consumer);
        SingleRecipeBuilder.base(FluidsNF.METAL.get(Metal.TIN).get(), 100).input(ItemsNF.TIN_CHUNK.get()).cookTime(20 * 20).saveCrucible(consumer);
        SingleRecipeBuilder.base(FluidsNF.METAL.get(Metal.COPPER).get(), 10).input(ItemsNF.AZURITE_NUGGET.get()).cookTime(20 * 5).saveCrucible(consumer);
        SingleRecipeBuilder.base(FluidsNF.METAL.get(Metal.COPPER).get(), 50).input(ItemsNF.AZURITE_CHUNK.get()).cookTime(20 * 20).saveCrucible(consumer);
        SingleRecipeBuilder.base(FluidsNF.METAL.get(Metal.IRON).get(), 10).input(ItemsNF.HEMATITE_NUGGET.get()).cookTime(20 * 5).saveCrucible(consumer);
        SingleRecipeBuilder.base(FluidsNF.METAL.get(Metal.IRON).get(), 50).input(ItemsNF.HEMATITE_CHUNK.get()).cookTime(20 * 20).saveCrucible(consumer);
        SingleRecipeBuilder.base(FluidsNF.METAL.get(Metal.METEORITE).get(), 20).input(ItemsNF.METEORITE_NUGGET.get()).cookTime(20 * 5).saveCrucible(consumer);
        SingleRecipeBuilder.base(FluidsNF.METAL.get(Metal.METEORITE).get(), 100).input(ItemsNF.METEORITE_CHUNK.get()).cookTime(20 * 20).saveCrucible(consumer);
        SingleRecipeBuilder.base(FluidsNF.METAL.get(Metal.STEEL).get(), 20).input(ItemsNF.STEEL_NUGGET.get()).cookTime(20 * 5).saveCrucible(consumer);
        for(Metal metal : ItemsNF.INGOTS.keySet()) SingleRecipeBuilder.base(FluidsNF.METAL.get(metal).get(), 100).input(ItemsNF.INGOTS.get(metal).get()).cookTime(20 * 20).saveCrucible(consumer);
        for(Metal metal : ItemsNF.BILLETS.keySet()) SingleRecipeBuilder.base(FluidsNF.METAL.get(metal).get(), 200).input(ItemsNF.BILLETS.get(metal).get()).cookTime(20 * 60).saveCrucible(consumer);
        for(Metal metal : ItemsNF.PLATES.keySet()) SingleRecipeBuilder.base(FluidsNF.METAL.get(metal).get(), 100).input(ItemsNF.PLATES.get(metal).get()).cookTime(20 * 20).saveCrucible(consumer);
        for(Metal metal : ItemsNF.WIRES.keySet()) SingleRecipeBuilder.base(FluidsNF.METAL.get(metal).get(), 10).input(ItemsNF.WIRES.get(metal).get()).cookTime(20 * 3).saveCrucible(consumer);
        for(Metal metal : ItemsNF.CHAINMAIL.keySet()) SingleRecipeBuilder.base(FluidsNF.METAL.get(metal).get(), 40).input(ItemsNF.CHAINMAIL.get(metal).get()).cookTime(20 * 6).saveCrucible(consumer);
        for(Metal metal : ItemsNF.SCALES.keySet()) SingleRecipeBuilder.base(FluidsNF.METAL.get(metal).get(), 40).input(ItemsNF.SCALES.get(metal).get()).cookTime(20 * 10).saveCrucible(consumer);
        for(TieredItemMaterial material : ItemsNF.ARMAMENT_HEADS.keySet()) {
            Metal metal = (Metal) material.getMetal();
            var map = ItemsNF.ARMAMENT_HEADS.get(material);
            for(var item : map.values()) {
                SingleRecipeBuilder.base(FluidsNF.METAL.get(metal).get(), 100).input(item.get()).cookTime(20 * 20).saveCrucible(consumer);
            }
        }
        SingleRecipeBuilder.base(ItemsNF.GLASS.get(), 1).input(TagsNF.SAND_ITEM).cookTime(20 * 3).requirement(entryKnowledge(EntriesNF.CASTING)).saveCrucible(consumer);
        SingleRecipeBuilder.base(ItemsNF.SALT.get(), 1).input(ItemsNF.SEAWATER.get()).cookTime(CauldronRecipe.COOK_TIME).requirement(entryKnowledge(EntriesNF.CASTING)).saveCrucible(consumer);

        BarrelRecipeBuilder.base(ItemsNF.RAWHIDE.get(), 1).input(TagsNF.ANIMAL_HIDE_SMALL).input(ItemsNF.LIME.get()).input(ItemsNF.WATER.get())
                .soakTime(DAY_LENGTH / 2).requirement(EntriesNF.TANNING.getId()).save(consumer);
        BarrelRecipeBuilder.base(ItemsNF.RAWHIDE.get(), 2).input(TagsNF.ANIMAL_HIDE_MEDIUM).input(ItemsNF.LIME.get()).input(ItemsNF.WATER.get())
                .soakTime(DAY_LENGTH / 2).requirement(EntriesNF.TANNING.getId()).save(consumer);
        BarrelRecipeBuilder.base(ItemsNF.RAWHIDE.get(), 4).input(TagsNF.ANIMAL_HIDE_LARGE).input(ItemsNF.LIME.get()).input(ItemsNF.WATER.get())
                .soakTime(DAY_LENGTH / 2).requirement(EntriesNF.TANNING.getId()).save(consumer);
        BarrelRecipeBuilder.base(ItemsNF.LEATHER.get(), 2).input(TagsNF.RAWHIDE_SMALL).input(TagsNF.TANNIN).input(ItemsNF.WATER.get())
                .soakTime(DAY_LENGTH).requirement(EntriesNF.TANNING.getId()).save(consumer);
        BarrelRecipeBuilder.base(ItemsNF.LEATHER.get(), 4).input(TagsNF.RAWHIDE_MEDIUM).input(TagsNF.TANNIN).input(ItemsNF.WATER.get())
                .soakTime(DAY_LENGTH).requirement(EntriesNF.TANNING.getId()).save(consumer);
        BarrelRecipeBuilder.base(ItemsNF.LEATHER.get(), 8).input(TagsNF.RAWHIDE_LARGE).input(TagsNF.TANNIN).input(ItemsNF.WATER.get())
                .soakTime(DAY_LENGTH).requirement(EntriesNF.TANNING.getId()).save(consumer);
        BarrelRecipeBuilder.base(ItemsNF.CURED_GAME.get(), 1).input(ItemsNF.SALT.get()).input(ItemsNF.RAW_GAME.get()).input(ItemsNF.SALT.get())
                .soakTime(DAY_LENGTH).fixedSoakTime().requirement(EntriesNF.CURING.getId()).save(consumer);
        BarrelRecipeBuilder.base(ItemsNF.CURED_VENISON.get(), 1).input(ItemsNF.SALT.get()).input(ItemsNF.RAW_VENISON.get()).input(ItemsNF.SALT.get())
                .soakTime(DAY_LENGTH * 2).fixedSoakTime().requirement(EntriesNF.CURING.getId()).save(consumer);
        BarrelRecipeBuilder.base(ItemsNF.CURED_POULTRY.get(), 1).input(ItemsNF.SALT.get()).input(ItemsNF.RAW_POULTRY.get()).input(ItemsNF.SALT.get())
                .soakTime(DAY_LENGTH).fixedSoakTime().requirement(EntriesNF.CURING.getId()).save(consumer);
        BarrelRecipeBuilder.base(ItemsNF.CURED_PORK.get(), 1).input(ItemsNF.SALT.get()).input(ItemsNF.RAW_PORK.get()).input(ItemsNF.SALT.get())
                .soakTime(DAY_LENGTH).fixedSoakTime().requirement(EntriesNF.CURING.getId()).save(consumer);
        BarrelRecipeBuilder.base(ItemsNF.CURED_JELLYFISH.get(), 1).input(ItemsNF.SALT.get()).input(ItemsNF.RAW_JELLYFISH.get()).input(ItemsNF.SALT.get())
                .soakTime(DAY_LENGTH / 2).fixedSoakTime().requirement(EntriesNF.CURING.getId()).save(consumer);
        BarrelRecipeBuilder.base(ItemsNF.CURED_PALE_FLESH.get(), 1).input(ItemsNF.SALT.get()).input(ItemsNF.RAW_PALE_FLESH.get()).input(ItemsNF.SALT.get())
                .soakTime(DAY_LENGTH).fixedSoakTime().requirement(EntriesNF.CURING.getId()).save(consumer);

        for(TieredItemMaterial material : ItemsNF.ARMAMENT_HEADS.keySet()) {
            Metal metal = (Metal) material.getMetal();
            Ingredient chunk = switch(metal) {
                case COPPER -> Ingredient.of(ItemsNF.COPPER_CHUNK.get());
                case METEORITE -> Ingredient.of(ItemsNF.METEORITE_CHUNK.get());
                default -> null;
            };
            Ingredient ingot = Ingredient.of(ItemsNF.INGOTS.get(metal).get());
            List<AnvilEntry> entries = new ObjectArrayList<>();
            entries.add(new AnvilEntry(ingot, MicroGridShape.INGOT, 0, 1024));
            if(chunk != null) entries.add(new AnvilEntry(chunk, MicroGridShape.CHUNK, 4, 10));

            for(var entry : entries) {
                TieredAnvilRecipeBuilder.base(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.ADZE).get(), material.getTier()).addIngredient(entry.item)
                        .randRange(entry.randMin, entry.randMax).addStartShape(entry.shape).addFinishShape(MicroGridShape.ADZE).requirement(EntriesNF.SMITHING).save(consumer);
                TieredAnvilRecipeBuilder.base(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.AXE).get(), material.getTier()).addIngredient(entry.item)
                        .randRange(entry.randMin, entry.randMax).addStartShape(entry.shape).addFinishShape(MicroGridShape.AXE).requirement(EntriesNF.SMITHING).save(consumer);
                TieredAnvilRecipeBuilder.base(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.CHISEL).get(), material.getTier()).addIngredient(entry.item)
                        .randRange(entry.randMin, entry.randMax).addStartShape(entry.shape).addFinishShape(MicroGridShape.CHISEL).requirement(EntriesNF.SMITHING).save(consumer);
                TieredAnvilRecipeBuilder.base(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.DAGGER).get(), material.getTier()).addIngredient(entry.item)
                        .randRange(entry.randMin, entry.randMax).addStartShape(entry.shape).addFinishShape(MicroGridShape.DAGGER).requirement(EntriesNF.SMITHING).save(consumer);
                TieredAnvilRecipeBuilder.base(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.MACE).get(), material.getTier()).addIngredient(entry.item)
                        .randRange(entry.randMin, entry.randMax).addStartShape(entry.shape).addFinishShape(MicroGridShape.MACE).requirement(EntriesNF.MACE).save(consumer);
                TieredAnvilRecipeBuilder.base(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.PICKAXE).get(), material.getTier()).addIngredient(entry.item)
                        .randRange(entry.randMin, entry.randMax).addStartShape(entry.shape).addFinishShape(MicroGridShape.PICKAXE).requirement(EntriesNF.SMITHING).save(consumer);
                TieredAnvilRecipeBuilder.base(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.SABRE).get(), material.getTier()).addIngredient(entry.item)
                        .randRange(entry.randMin, entry.randMax).addStartShape(entry.shape).addFinishShape(MicroGridShape.SABRE).requirement(EntriesNF.SABRE).save(consumer);
                TieredAnvilRecipeBuilder.base(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.SHOVEL).get(), material.getTier()).addIngredient(entry.item)
                        .randRange(entry.randMin, entry.randMax).addStartShape(entry.shape).addFinishShape(MicroGridShape.SHOVEL).requirement(EntriesNF.SMITHING).save(consumer);
                TieredAnvilRecipeBuilder.base(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.SICKLE).get(), material.getTier()).addIngredient(entry.item)
                        .randRange(entry.randMin, entry.randMax).addStartShape(entry.shape).addFinishShape(MicroGridShape.SICKLE).requirement(EntriesNF.SICKLE_SMITHING).save(consumer);
                TieredAnvilRecipeBuilder.base(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.SPEAR).get(), material.getTier()).addIngredient(entry.item)
                        .randRange(entry.randMin, entry.randMax).addStartShape(entry.shape).addFinishShape(MicroGridShape.SPEAR).requirement(EntriesNF.SMITHING).save(consumer);
                TieredAnvilRecipeBuilder.base(ItemsNF.ARMAMENT_HEADS.get(material).get(Armament.SWORD).get(), material.getTier()).addIngredient(entry.item)
                        .randRange(entry.randMin, entry.randMax).addStartShape(entry.shape).addFinishShape(MicroGridShape.SWORD).requirement(EntriesNF.SMITHING).save(consumer);
            }
        }
        for(Metal metal : Metal.values()) {
            Ingredient chunk = switch(metal) {
                case TIN -> Ingredient.of(ItemsNF.TIN_CHUNK.get());
                case COPPER -> Ingredient.of(ItemsNF.COPPER_CHUNK.get());
                case METEORITE -> Ingredient.of(ItemsNF.METEORITE_CHUNK.get());
                default -> null;
            };
            if(chunk != null) TieredAnvilRecipeBuilder.base(ItemsNF.INGOTS.get(metal).get(), metal.getWorkTier()).addIngredient(chunk).randRange(4, 10)
                    .addStartShape(MicroGridShape.CHUNK).addFinishShape(MicroGridShape.INGOT).requirement(EntriesNF.SMITHING).save(consumer);
            Ingredient ingot = Ingredient.of(ItemsNF.INGOTS.get(metal).get());
            Ingredient plate = Ingredient.of(ItemsNF.PLATES.get(metal).get());
            Ingredient wire = Ingredient.of(ItemsNF.WIRES.get(metal).get());
            Ingredient billet = Ingredient.of(ItemsNF.BILLETS.get(metal).get());
            TieredAnvilRecipeBuilder.base(ItemsNF.PLATES.get(metal).get(), metal.getWorkTier()).addIngredient(ingot)
                    .addStartShape(MicroGridShape.INGOT).addFinishShape(MicroGridShape.PLATE).requirement(EntriesNF.SMITHING).save(consumer);
            TieredAnvilRecipeBuilder.base(ItemsNF.WIRES.get(metal).get(), 8, metal.getWorkTier()).addIngredient(plate)
                    .addStartShape(MicroGridShape.PLATE).addFinishShape(MicroGridShape.WIRES).requirement(EntriesNF.SMITHING).save(consumer);
            TieredAnvilRecipeBuilder.base(ItemsNF.BILLETS.get(metal).get(), metal.getWorkTier()).addIngredient(ingot).addIngredient(TagsNF.FLUX).addIngredient(ingot)
                    .addStartShape(MicroGridShape.DOUBLE_INGOT_TALL).addFinishShape(MicroGridShape.BILLET).requirement(EntriesNF.SMITHING).save(consumer);
            TieredAnvilRecipeBuilder.base(ItemsNF.METAL_BLOCKS.get(metal).get(), metal.getWorkTier()).addIngredient(billet).addIngredient(TagsNF.FLUX).addIngredient(billet)
                    .addStartShape(MicroGridShape.DOUBLE_BILLET_TALL).addFinishShape(MicroGridShape.BLOCK).requirement(EntriesNF.SMITHING).save(consumer);
            if(ItemsNF.ANVILS_METAL.containsKey(metal)) TieredAnvilRecipeBuilder.base(ItemsNF.ANVILS_METAL.get(metal).get(), metal.getWorkTier())
                    .addIngredient(billet).addIngredient(TagsNF.FLUX).addIngredient(billet)
                    .addStartShape(MicroGridShape.DOUBLE_BILLET_TALL).addFinishShape(MicroGridShape.ANVIL).requirement(EntriesNF.SMITHING).save(consumer);
            if(ItemsNF.SCALES.containsKey(metal)) TieredAnvilRecipeBuilder.base(ItemsNF.SCALES.get(metal).get(), 2, metal.getWorkTier())
                    .addIngredient(plate).addStartShape(MicroGridShape.PLATE).addFinishShape(MicroGridShape.SCALES).requirement(EntriesNF.SCALE_ARMOR).save(consumer);
        }
        TieredAnvilRecipeBuilder.base(ItemsNF.INGOTS.get(Metal.IRON).get(), Metal.IRON.getWorkTier()).addIngredient(ItemsNF.IRON_BLOOM.get()).slagChance(0.7F).randRange(4, 10)
                .addStartShape(MicroGridShape.CHUNK).addFinishShape(MicroGridShape.INGOT).requirement(itemKnowledge(ItemsNF.IRON_BLOOM)).save(consumer);
    }

    private record AnvilEntry(Ingredient item, MicroGridShape shape, int randMin, int randMax) {}

    private static ResourceLocation itemKnowledge(RegistryObject<? extends Item> item) {
        return KnowledgeNF.ITEMS.get(item).getId();
    }

    private static ResourceLocation entryKnowledge(RegistryObject<Entry> entry) {
        return KnowledgeNF.get(entry).getId();
    }
}
