package frostnox.nightfall.data;

import frostnox.nightfall.block.*;
import frostnox.nightfall.block.block.*;
import frostnox.nightfall.block.block.anvil.MetalAnvilBlock;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlock;
import frostnox.nightfall.block.block.barrel.BarrelBlockNF;
import frostnox.nightfall.block.block.chest.ChestBlockNF;
import frostnox.nightfall.block.block.rack.RackBlock;
import frostnox.nightfall.block.block.shelf.ShelfBlock;
import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BlockTagsProviderNF extends BlockTagsProvider {
    public BlockTagsProviderNF(DataGenerator generator, String id, @Nullable ExistingFileHelper helper) {
        super(generator, id, helper);
    }

    @Override
    protected void addTags() {
        for(var block : BlocksNF.COVERED_SILT.values()) tag(TagsNF.SILT).add(block.get());
        for(var block : BlocksNF.COVERED_DIRT.values()) tag(BlockTags.DIRT).add(block.get());
        for(var block : BlocksNF.COVERED_LOAM.values()) tag(TagsNF.LOAM).add(block.get());
        tag(TagsNF.SILT).add(BlocksNF.SILT.get(), BlocksNF.TILLED_SILT.get(), BlocksNF.STRANGE_SOILS.get(Soil.SILT).get());
        tag(BlockTags.DIRT).add(BlocksNF.DIRT.get(), BlocksNF.TILLED_DIRT.get(), BlocksNF.STRANGE_SOILS.get(Soil.DIRT).get());
        tag(TagsNF.LOAM).add(BlocksNF.LOAM.get(), BlocksNF.TILLED_LOAM.get(), BlocksNF.STRANGE_SOILS.get(Soil.LOAM).get());
        tag(Tags.Blocks.GRAVEL).add(BlocksNF.GRAVEL.get(), BlocksNF.BLUE_GRAVEL.get(), BlocksNF.BLACK_GRAVEL.get(),
                BlocksNF.STRANGE_SOILS.get(Soil.GRAVEL).get(), BlocksNF.STRANGE_SOILS.get(Soil.BLUE_GRAVEL).get(), BlocksNF.STRANGE_SOILS.get(Soil.BLACK_GRAVEL).get());
        tag(Tags.Blocks.SAND).add(BlocksNF.SAND.get(), BlocksNF.RED_SAND.get(), BlocksNF.WHITE_SAND.get(),
                BlocksNF.STRANGE_SOILS.get(Soil.SAND).get(), BlocksNF.STRANGE_SOILS.get(Soil.RED_SAND).get(), BlocksNF.STRANGE_SOILS.get(Soil.WHITE_SAND).get());
        tag(TagsNF.SOIL).addTags(TagsNF.SILT, BlockTags.DIRT, TagsNF.LOAM, Tags.Blocks.GRAVEL, Tags.Blocks.SAND);
        for(var block : BlocksNF.getCoveredSoils()) {
            tag(TagsNF.SOIL).add(block.get());
            tag(TagsNF.NATURAL_SOIL).add(block.get());
        }
        tag(TagsNF.NATURAL_SOIL).addTags(Tags.Blocks.GRAVEL, Tags.Blocks.SAND);
        tag(TagsNF.NATURAL_SOIL).add(BlocksNF.SILT.get(), BlocksNF.DIRT.get(), BlocksNF.LOAM.get(), BlocksNF.MUD.get(), BlocksNF.CLAY.get(), BlocksNF.FIRE_CLAY.get());
        for(var block : BlocksNF.STONE_BLOCKS.values()) tag(TagsNF.NATURAL_STONE).add(block.get());
        for(var block : BlocksNF.getOres()) tag(TagsNF.NATURAL_STONE).add(block.get());
        tag(TagsNF.NATURAL_TERRAIN).addTags(TagsNF.NATURAL_SOIL, TagsNF.NATURAL_STONE);
        for(Stone type : Stone.values()) {
            tag(Tags.Blocks.STONE).add(BlocksNF.TILED_STONE.get(type).get());
            tag(Tags.Blocks.STONE).add(BlocksNF.POLISHED_STONE.get(type).get());
            tag(Tags.Blocks.STONE).add(BlocksNF.POLISHED_STONE_STAIRS.get(type).get());
            tag(Tags.Blocks.STONE).add(BlocksNF.POLISHED_STONE_SLABS.get(type).get());
            tag(Tags.Blocks.STONE).add(BlocksNF.POLISHED_STONE_SIDINGS.get(type).get());
            tag(Tags.Blocks.STONE).add(BlocksNF.STACKED_STONE.get(type).get());
            tag(Tags.Blocks.STONE).add(BlocksNF.STACKED_STONE_STAIRS.get(type).get());
            tag(Tags.Blocks.STONE).add(BlocksNF.STACKED_STONE_SLABS.get(type).get());
            tag(Tags.Blocks.STONE).add(BlocksNF.STACKED_STONE_SIDINGS.get(type).get());
            tag(Tags.Blocks.STONE).add(BlocksNF.COBBLED_STONE.get(type).get());
            tag(Tags.Blocks.STONE).add(BlocksNF.COBBLED_STONE_STAIRS.get(type).get());
            tag(Tags.Blocks.STONE).add(BlocksNF.COBBLED_STONE_SLABS.get(type).get());
            tag(Tags.Blocks.STONE).add(BlocksNF.COBBLED_STONE_SIDINGS.get(type).get());
            tag(BlockTags.STONE_BRICKS).add(BlocksNF.STONE_BRICK_BLOCKS.get(type).get());
            tag(BlockTags.STONE_BRICKS).add(BlocksNF.STONE_BRICK_STAIRS.get(type).get());
            tag(BlockTags.STONE_BRICKS).add(BlocksNF.STONE_BRICK_SLABS.get(type).get());
            tag(BlockTags.STONE_BRICKS).add(BlocksNF.STONE_BRICK_SIDINGS.get(type).get());
        }
        tag(Tags.Blocks.STONE).addTag(TagsNF.NATURAL_STONE);
        tag(Tags.Blocks.STONE).add(BlocksNF.METEORITE_ORE.get(), BlocksNF.OBSIDIAN.get());
        tag(Tags.Blocks.STONE).addTag(BlockTags.STONE_BRICKS);
        tag(BlockTags.REPLACEABLE_PLANTS).add(BlocksNF.TALL_GRASS.get(), BlocksNF.GRASS.get(), BlocksNF.SHORT_GRASS.get(), BlocksNF.SMALL_FERN.get(), BlocksNF.FERN.get(),
                BlocksNF.LARGE_FERN.get(), BlocksNF.VINES.get());
        tag(BlockTags.CLIMBABLE).add(BlocksNF.VINES.get(), BlocksNF.ROPE.get());
        tag(BlockTags.SNOW).add(BlocksNF.SNOW.get(), BlocksNF.PACKED_SNOW.get());
        tag(BlockTags.CROPS).add(BlocksNF.DEAD_CROP.get(), BlocksNF.POTATOES.get(), BlocksNF.CARROTS.get(), BlocksNF.FLAX.get(), BlocksNF.YARROW.get());
        tag(BlockTags.FIRE).add(BlocksNF.FIRE.get());
        tag(TagsNF.TERRACOTTA).add(BlocksNF.TERRACOTTA.get(), BlocksNF.TERRACOTTA_TILES.get(), BlocksNF.TERRACOTTA_TILE_STAIRS.get(),
                BlocksNF.TERRACOTTA_TILE_SLAB.get(), BlocksNF.TERRACOTTA_TILE_SIDING.get(), BlocksNF.TERRACOTTA_MOSAIC.get(),
                BlocksNF.TERRACOTTA_MOSAIC_STAIRS.get(), BlocksNF.TERRACOTTA_MOSAIC_SLAB.get(), BlocksNF.TERRACOTTA_MOSAIC_SIDING.get());
        for(var block : BlocksNF.STONE_TUNNELS.values()) tag(TagsNF.STONE_TUNNELS).add(block.get());

        for(Tree type : Tree.values()) {
            tag(TagsNF.TREE_WOOD).addTag(type.getTag());
            tag(type.getTag()).add(BlocksNF.STEMS.get(type).get(), BlocksNF.TRUNKS.get(type).get());
            tag(BlockTags.LOGS_THAT_BURN).add(BlocksNF.STEMS.get(type).get(), BlocksNF.TRUNKS.get(type).get());
            tag(BlockTags.LOGS_THAT_BURN).add(BlocksNF.LOGS.get(type).get(), BlocksNF.STRIPPED_LOGS.get(type).get());
            tag(BlockTags.LEAVES).add(BlocksNF.LEAVES.get(type).get());
            tag(BlockTags.PLANKS).add(BlocksNF.PLANK_BLOCKS.get(type).get());
            tag(BlockTags.WOODEN_STAIRS).add(BlocksNF.PLANK_STAIRS.get(type).get());
            tag(BlockTags.WOODEN_SLABS).add(BlocksNF.PLANK_SLABS.get(type).get());
            tag(TagsNF.WOODEN_SIDINGS).add(BlocksNF.PLANK_SIDINGS.get(type).get());
            tag(BlockTags.WOODEN_FENCES).add(BlocksNF.PLANK_FENCES.get(type).get());
            tag(Tags.Blocks.FENCE_GATES_WOODEN).add(BlocksNF.PLANK_FENCE_GATES.get(type).get());
            tag(TagsNF.WOODEN_FENCE_GATES).add(BlocksNF.PLANK_FENCE_GATES.get(type).get());
            tag(BlockTags.WOODEN_DOORS).add(BlocksNF.PLANK_DOORS.get(type).get());
            tag(BlockTags.WOODEN_TRAPDOORS).add(BlocksNF.PLANK_TRAPDOORS.get(type).get());
            tag(TagsNF.WOODEN_HATCHES).add(BlocksNF.PLANK_HATCHES.get(type).get());
            tag(TagsNF.WOODEN_LADDERS).add(BlocksNF.PLANK_LADDERS.get(type).get());
            tag(BlockTags.STANDING_SIGNS).add(BlocksNF.PLANK_STANDING_SIGNS.get(type).get());
            tag(BlockTags.WALL_SIGNS).add(BlocksNF.PLANK_WALL_SIGNS.get(type).get());
            tag(TagsNF.ITEM_FRAMES).add(BlocksNF.WOODEN_ITEM_FRAMES.get(type).get());
            tag(Tags.Blocks.CHESTS_WOODEN).add(BlocksNF.CHESTS.get(type).get());
            tag(TagsNF.WOODEN_CHESTS).add(BlocksNF.CHESTS.get(type).get());
            tag(TagsNF.WOODEN_RACKS).add(BlocksNF.RACKS.get(type).get());
            tag(TagsNF.WOODEN_SHELVES).add(BlocksNF.SHELVES.get(type).get());
            tag(TagsNF.CHAIRS).add(BlocksNF.CHAIRS.get(type).get());
            tag(TagsNF.TROUGHS).add(BlocksNF.TROUGHS.get(type).get());
            tag(Tags.Blocks.BARRELS_WOODEN).add(BlocksNF.BARRELS.get(type).get());
            tag(TagsNF.WOODEN_BARRELS).add(BlocksNF.BARRELS.get(type).get());
            tag(BlockTags.CLIMBABLE).add(BlocksNF.PLANK_LADDERS.get(type).get());
        }
        for(var block : BlocksNF.FRUIT_LEAVES.values()) tag(BlockTags.LEAVES).add(block.get());
        tag(TagsNF.BRANCHES_OR_LEAVES).addTag(BlockTags.LEAVES);
        for(var block : BlocksNF.BRANCHES.values()) tag(TagsNF.BRANCHES_OR_LEAVES).add(block.get());

        tag(BlockTags.LOGS).addTag(BlockTags.LOGS_THAT_BURN);

        for(var block : BlocksNF.METAL_BLOCKS.values()) tag(TagsNF.METAL_BLOCKS).add(block.get());

        tag(TagsNF.TILLABLE_SOIL).addTags(BlockTags.DIRT, TagsNF.SILT, TagsNF.LOAM);
        tag(TagsNF.TILLABLE_OR_AQUATIC_SOIL).addTag(TagsNF.TILLABLE_SOIL);
        tag(TagsNF.TILLABLE_OR_AQUATIC_SOIL).add(BlocksNF.MUD.get(), BlocksNF.CLAY.get());
        tag(TagsNF.TILLED_SOIL).add(BlocksNF.TILLED_SILT.get(), BlocksNF.TILLED_DIRT.get(), BlocksNF.TILLED_LOAM.get());
        for(Stone type : Stone.values()) {
            if(type.getType() == StoneType.METAMORPHIC) tag(TagsNF.HEAT_RESISTANT_1).add(BlocksNF.STONE_BRICK_BLOCKS.get(type).get());
            else if(type.getType() == StoneType.IGNEOUS) tag(TagsNF.HEAT_RESISTANT_2).add(BlocksNF.STONE_BRICK_BLOCKS.get(type).get());
        }
        tag(TagsNF.FULLY_CLIMBABLE).addTags(BlockTags.STAIRS, BlockTags.SLABS);
        tag(TagsNF.CAN_IGNITE_ITEMS).add(BlocksNF.TORCH.get(), BlocksNF.WALL_TORCH.get());
        for(var block : BlocksNF.LANTERNS.values()) tag(TagsNF.CAN_IGNITE_ITEMS).add(block.get());
        tag(TagsNF.UNCLIMBABLE).addTags(TagsNF.BRANCHES_OR_LEAVES, BlockTags.FENCES, BlockTags.WALLS, BlockTags.FENCE_GATES);
        tag(TagsNF.HEAT_RESISTANT_1).add(BlocksNF.TERRACOTTA.get(), BlocksNF.TERRACOTTA_TILES.get(), BlocksNF.TERRACOTTA_MOSAIC.get(), BlocksNF.MUD_BRICKS.get());
        tag(TagsNF.HEAT_RESISTANT_2).add(BlocksNF.BRICKS.get());
        tag(TagsNF.HEAT_RESISTANT_3).add(BlocksNF.FIRE_BRICKS.get());
        tag(TagsNF.HEAT_RESISTANT_4).add(BlocksNF.OBSIDIAN.get());
        tag(TagsNF.HEAT_RESISTANT_1).addTags(TagsNF.HEAT_RESISTANT_2);
        tag(TagsNF.HEAT_RESISTANT_2).addTags(TagsNF.HEAT_RESISTANT_3);
        tag(TagsNF.HEAT_RESISTANT_3).addTags(TagsNF.HEAT_RESISTANT_4);

        tag(TagsNF.MINEABLE_WITH_DAGGER).addTag(TagsNF.MINEABLE_WITH_SICKLE);
        tag(TagsNF.MINEABLE_WITH_ADZE).addTags(BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.MINEABLE_WITH_AXE);
        tag(TagsNF.MINEABLE_WITH_MAUL).addTags(BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.MINEABLE_WITH_AXE, BlockTags.MINEABLE_WITH_PICKAXE);

        tag(TagsNF.MINEABLE_WITH_SICKLE).addTags(TagsNF.BRANCHES_OR_LEAVES, BlockTags.MINEABLE_WITH_HOE, BlockTags.REPLACEABLE_PLANTS, BlockTags.CROPS);
        tag(TagsNF.MINEABLE_WITH_SICKLE).add(BlocksNF.DEAD_PLANT.get(), BlocksNF.THATCH.get(), BlocksNF.THATCH_STAIRS.get(), BlocksNF.THATCH_SLAB.get(),
                BlocksNF.THATCH_SIDING.get(), BlocksNF.ROPE.get(), BlocksNF.MOON_ESSENCE.get(), BlocksNF.SPIDER_WEB.get(), BlocksNF.SPIDER_NEST.get(),
                BlocksNF.DRAKEFOWL_NEST.get());
        for(var block : BlocksNF.TREE_SEEDS.values()) tag(TagsNF.MINEABLE_WITH_SICKLE).add(block.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).addTags(Tags.Blocks.STONE, BlockTags.ICE, TagsNF.METAL_ANVILS, TagsNF.TERRACOTTA, Tags.Blocks.GLASS,
                TagsNF.METAL_BLOCKS);
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BlocksNF.MUD_BRICKS.get(), BlocksNF.MUD_BRICK_STAIRS.get(), BlocksNF.MUD_BRICK_SLAB.get(),
                BlocksNF.MUD_BRICK_SIDING.get(), BlocksNF.BRICKS.get(), BlocksNF.BRICK_STAIRS.get(), BlocksNF.BRICK_SLAB.get(),
                BlocksNF.BRICK_SIDING.get(), BlocksNF.FIRE_BRICKS.get(), BlocksNF.FIRE_BRICK_STAIRS.get(), BlocksNF.FIRE_BRICK_SLAB.get(),
                BlocksNF.FIRE_BRICK_SIDING.get(), BlocksNF.MOON_ESSENCE.get(), BlocksNF.FLINT_CLUSTER.get(), BlocksNF.SLAG.get(),
                BlocksNF.AZURITE.get(), BlocksNF.HEMATITE.get(), BlocksNF.SMELTED_AZURITE.get(), BlocksNF.SMELTED_HEMATITE.get(),
                BlocksNF.ANCHORING_RESIN.get());
        for(var block : BlocksNF.ROCK_CLUSTERS.values()) tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get());
        for(var block : BlocksNF.SKARA_ROCK_CLUSTERS.values()) tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get());
        for(var block : BlocksNF.ANVILS_STONE.values()) tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get());

        tag(BlockTags.MINEABLE_WITH_AXE).addTags(BlockTags.LOGS, BlockTags.WOODEN_STAIRS, BlockTags.WOODEN_SLABS, TagsNF.WOODEN_SIDINGS,
                BlockTags.WOODEN_FENCES, TagsNF.WOODEN_FENCE_GATES, BlockTags.WOODEN_DOORS, BlockTags.WOODEN_TRAPDOORS, TagsNF.WOODEN_HATCHES,
                TagsNF.WOODEN_LADDERS, TagsNF.WOODEN_BARRELS, TagsNF.WOODEN_CHESTS, TagsNF.WOODEN_RACKS, TagsNF.WOODEN_SHELVES,
                Tags.Blocks.GLASS, TagsNF.BRANCHES_OR_LEAVES);
        tag(BlockTags.MINEABLE_WITH_AXE).add(BlocksNF.DEAD_BUSH.get(), BlocksNF.CAMPFIRE.get(), BlocksNF.FIREWOOD.get(), BlocksNF.FIREWOOD_BURNING.get(),
                BlocksNF.MOON_ESSENCE.get());
        for(var block : BlocksNF.ANVILS_LOG.values()) tag(BlockTags.MINEABLE_WITH_AXE).add(block.get());

        tag(BlockTags.MINEABLE_WITH_SHOVEL).addTags(TagsNF.SOIL);
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(BlocksNF.SNOW.get(), BlocksNF.PACKED_SNOW.get(), BlocksNF.MUD.get(), BlocksNF.CLAY.get(),
                BlocksNF.FIRE_CLAY.get(), BlocksNF.WET_MUD_BRICKS.get(), BlocksNF.CLAY_BRICKS.get(), BlocksNF.FIRE_CLAY_BRICKS.get(),
                BlocksNF.CAMPFIRE.get(), BlocksNF.BERRY_BUSH.get(), BlocksNF.STEEL_INGOT_PILE_POOR.get(), BlocksNF.STEEL_INGOT_PILE_FAIR.get(),
                BlocksNF.COKE.get(), BlocksNF.COKE_BURNING.get(), BlocksNF.COAL.get(), BlocksNF.COAL_BURNING.get(),
                BlocksNF.CHARCOAL.get(), BlocksNF.CHARCOAL_BURNING.get(), BlocksNF.FIREWOOD.get(), BlocksNF.FIREWOOD_BURNING.get(),
                BlocksNF.SLAG.get(), BlocksNF.AZURITE.get(), BlocksNF.HEMATITE.get(), BlocksNF.SMELTED_AZURITE.get(),
                BlocksNF.SMELTED_HEMATITE.get(), BlocksNF.MOON_ESSENCE.get());
        for(var block : BlocksNF.INGOT_PILES.values()) tag(BlockTags.MINEABLE_WITH_SHOVEL).add(block.get());

        tag(Tags.Blocks.GLASS_COLORLESS).add(BlocksNF.GLASS_BLOCK.get(), BlocksNF.GLASS_SLAB.get(), BlocksNF.GLASS_SIDING.get());
        tag(BlockTags.IMPERMEABLE).addTags(Tags.Blocks.GLASS);
        tag(BlockTags.ICE).add(BlocksNF.ICE.get(), BlocksNF.SEA_ICE.get());
        tag(TagsNF.SHATTER_ON_FALL).addTags(Tags.Blocks.GLASS, BlockTags.ICE);
        tag(TagsNF.SHATTER_ON_FALL).add(BlocksNF.POT.get());
        for(var block : BlocksNF.STRANGE_SOILS.values()) tag(TagsNF.SHATTER_ON_FALL).add(block.get());
        tag(TagsNF.SALT_MELTS).add(BlocksNF.ICE.get());
        tag(TagsNF.SALT_MELTS).addTag(BlockTags.SNOW);

        for(var block : BlocksNF.ROCK_CLUSTERS.values()) tag(TagsNF.FALLING_DESTROYABLE).add(block.get());
        for(var block : BlocksNF.SKARA_ROCK_CLUSTERS.values()) tag(TagsNF.FALLING_DESTROYABLE).add(block.get());
        tag(TagsNF.FALLING_DESTROYABLE).add(BlocksNF.TORCH.get(), BlocksNF.TORCH_UNLIT.get(), BlocksNF.WALL_TORCH.get(), BlocksNF.WALL_TORCH_UNLIT.get(),
                BlocksNF.ROPE.get(), BlocksNF.CRUCIBLE.get(), BlocksNF.UNFIRED_CRUCIBLE.get(), BlocksNF.CAULDRON.get(), BlocksNF.UNFIRED_CAULDRON.get(),
                BlocksNF.POT.get(), BlocksNF.UNFIRED_POT.get(), BlocksNF.INGOT_MOLD.get(), BlocksNF.ARROWHEAD_MOLD.get(), BlocksNF.UNFIRED_INGOT_MOLD.get(),
                BlocksNF.UNFIRED_ARROWHEAD_MOLD.get(), BlocksNF.FLINT_CLUSTER.get(), BlocksNF.SEASHELL.get(), BlocksNF.SPIDER_WEB.get(),
                BlocksNF.DRAKEFOWL_NEST.get());
        for(var block : BlocksNF.LANTERNS.values()) tag(TagsNF.FALLING_DESTROYABLE).add(block.get());
        for(var block : BlocksNF.LANTERNS_UNLIT.values()) tag(TagsNF.FALLING_DESTROYABLE).add(block.get());
        for(var block : BlocksNF.ARMAMENT_MOLDS.values()) tag(TagsNF.FALLING_DESTROYABLE).add(block.get());

        tag(TagsNF.UNSTABLE_SUPPORT_HORIZONTAL).addTags(BlockTags.DOORS, BlockTags.TRAPDOORS, TagsNF.HATCHES, TagsNF.LADDERS);

        tag(TagsNF.HAS_PHYSICS).addTags(TagsNF.SUPPORT_1, TagsNF.SUPPORT_2, TagsNF.SUPPORT_4, TagsNF.SUPPORT_8);
        //TODO: Make this function off hardness instead? And keep this for exceptions
        tag(TagsNF.FLOATS).addTags(BlockTags.ICE, BlockTags.PLANKS, BlockTags.WOODEN_STAIRS, BlockTags.WOODEN_SLABS, TagsNF.WOODEN_SIDINGS);
        tag(TagsNF.FLOATS).add(BlocksNF.THATCH.get(), BlocksNF.THATCH_STAIRS.get(), BlocksNF.THATCH_SIDING.get());
        HashMap<RegistryObject<? extends Block>, Integer> sup = new HashMap<>(256);
        for(var block : BlocksNF.getCoveredSoils()) sup.put(block, 0);
        for(var block : BlocksNF.STRANGE_SOILS.values()) sup.put(block, 0);
        for(Stone type : Stone.values()) {
            addSupports(sup, 0, BlocksNF.STACKED_STONE.get(type), BlocksNF.STACKED_STONE_STAIRS.get(type), BlocksNF.STACKED_STONE_SLABS.get(type),
                    BlocksNF.STACKED_STONE_SIDINGS.get(type));
            addSupports(sup, 2, BlocksNF.COBBLED_STONE.get(type), BlocksNF.COBBLED_STONE_STAIRS.get(type),
                    BlocksNF.COBBLED_STONE_SLABS.get(type), BlocksNF.COBBLED_STONE_SIDINGS.get(type));
            addSupports(sup, 4, BlocksNF.TILED_STONE.get(type), BlocksNF.POLISHED_STONE.get(type), BlocksNF.POLISHED_STONE_STAIRS.get(type),
                    BlocksNF.POLISHED_STONE_SLABS.get(type), BlocksNF.POLISHED_STONE_SIDINGS.get(type));
            addSupports(sup, 6, BlocksNF.STONE_BLOCKS.get(type), BlocksNF.STONE_BRICK_BLOCKS.get(type), BlocksNF.STONE_BRICK_STAIRS.get(type),
                    BlocksNF.STONE_BRICK_SLABS.get(type), BlocksNF.STONE_BRICK_SIDINGS.get(type));
        }
        for(var block : BlocksNF.STONE_TUNNELS.values()) sup.put(block, 6);
        for(var block : BlocksNF.getOres()) {
            sup.put(block, 4);
        }
        for(Tree type : Tree.values()) {
            addSupports(sup, 0, BlocksNF.BARRELS.get(type), BlocksNF.CHESTS.get(type), BlocksNF.SHELVES.get(type), BlocksNF.TROUGHS.get(type));
            addSupports(sup, 6, BlocksNF.LOGS.get(type), BlocksNF.STRIPPED_LOGS.get(type), BlocksNF.PLANK_BLOCKS.get(type),
                    BlocksNF.PLANK_STAIRS.get(type), BlocksNF.PLANK_SLABS.get(type), BlocksNF.PLANK_SIDINGS.get(type),
                    BlocksNF.PLANK_FENCES.get(type));
        }
        for(Metal type : BlocksNF.METAL_BLOCKS.keySet()) sup.put(BlocksNF.METAL_BLOCKS.get(type), 1);
        for(Metal type : BlocksNF.INGOT_PILES.keySet()) sup.put(BlocksNF.INGOT_PILES.get(type), 0);
        for(var block : BlocksNF.ANVILS_LOG.values()) sup.put(block, 0);
        for(var block : BlocksNF.ANVILS_STONE.values()) sup.put(block, 0);
        for(Metal type : BlocksNF.ANVILS_METAL.keySet()) sup.put(BlocksNF.ANVILS_METAL.get(type), 0);
        addSupports(sup, 0, BlocksNF.SILT, BlocksNF.DIRT, BlocksNF.LOAM, BlocksNF.ASH, BlocksNF.GRAVEL, BlocksNF.BLUE_GRAVEL, BlocksNF.BLACK_GRAVEL,
                BlocksNF.SAND, BlocksNF.RED_SAND, BlocksNF.WHITE_SAND, BlocksNF.MUD, BlocksNF.COKE, BlocksNF.COKE_BURNING, BlocksNF.COAL, BlocksNF.COAL_BURNING,
                BlocksNF.CHARCOAL, BlocksNF.CHARCOAL_BURNING, BlocksNF.FIREWOOD, BlocksNF.SLAG, BlocksNF.AZURITE, BlocksNF.HEMATITE, BlocksNF.SMELTED_AZURITE,
                BlocksNF.SMELTED_HEMATITE, BlocksNF.CAMPFIRE, BlocksNF.CAULDRON, BlocksNF.POT, BlocksNF.WET_MUD_BRICKS, BlocksNF.CLAY_BRICKS, BlocksNF.FIRE_CLAY_BRICKS,
                BlocksNF.STEEL_INGOT_PILE_POOR, BlocksNF.STEEL_INGOT_PILE_FAIR, BlocksNF.CRUCIBLE, BlocksNF.UNFIRED_CRUCIBLE, BlocksNF.CAULDRON, BlocksNF.UNFIRED_CAULDRON,
                BlocksNF.POT, BlocksNF.UNFIRED_POT, BlocksNF.SPIDER_NEST);
        addSupports(sup, 1, BlocksNF.CLAY, BlocksNF.FIRE_CLAY, BlocksNF.PACKED_SNOW);
        addSupports(sup, 2, BlocksNF.THATCH, BlocksNF.THATCH_STAIRS, BlocksNF.THATCH_SLAB, BlocksNF.THATCH_SIDING, BlocksNF.TERRACOTTA, BlocksNF.TERRACOTTA_TILES,
                BlocksNF.TERRACOTTA_TILE_STAIRS, BlocksNF.TERRACOTTA_TILE_SLAB, BlocksNF.TERRACOTTA_TILE_SIDING, BlocksNF.TERRACOTTA_MOSAIC, BlocksNF.TERRACOTTA_MOSAIC_STAIRS,
                BlocksNF.TERRACOTTA_MOSAIC_SLAB, BlocksNF.TERRACOTTA_MOSAIC_SIDING, BlocksNF.ICE, BlocksNF.SEA_ICE);
        addSupports(sup, 4, BlocksNF.MUD_BRICKS, BlocksNF.MUD_BRICK_STAIRS, BlocksNF.MUD_BRICK_SLAB, BlocksNF.MUD_BRICK_SIDING);
        addSupports(sup, 6, BlocksNF.BRICKS, BlocksNF.BRICK_STAIRS, BlocksNF.BRICK_SLAB, BlocksNF.BRICK_SIDING,
                BlocksNF.FIRE_BRICKS, BlocksNF.FIRE_BRICK_STAIRS, BlocksNF.FIRE_BRICK_SLAB, BlocksNF.FIRE_BRICK_SIDING);
        addSupports(sup, 10, BlocksNF.GLASS_BLOCK, BlocksNF.GLASS_SLAB, BlocksNF.GLASS_SIDING);
        addSupports(sup, 15, BlocksNF.OBSIDIAN);
        for(Map.Entry<RegistryObject<? extends Block>, Integer> entry : sup.entrySet()) {
            if(entry.getValue() == 0) tag(TagsNF.HAS_PHYSICS).add(entry.getKey().get());
            else {
                if((entry.getValue() & 1) == 1) tag(TagsNF.SUPPORT_1).add(entry.getKey().get());
                if((entry.getValue() >> 1 & 1) == 1) tag(TagsNF.SUPPORT_2).add(entry.getKey().get());
                if((entry.getValue() >> 2 & 1) == 1) tag(TagsNF.SUPPORT_4).add(entry.getKey().get());
                if((entry.getValue() >> 3 & 1) == 1) tag(TagsNF.SUPPORT_8).add(entry.getKey().get());
            }
        }

        tag(TagsNF.NO_BREAKING_TEXTURE).addTags(BlockTags.REPLACEABLE_PLANTS, BlockTags.CROPS);

        tag(TagsNF.DRAKEFOWL_NEST_BLOCK).add(BlocksNF.THATCH.get(), BlocksNF.COVERED_SILT.get(SoilCover.GRASS).get(), BlocksNF.COVERED_DIRT.get(SoilCover.GRASS).get(),
                BlocksNF.COVERED_LOAM.get(SoilCover.GRASS).get(), BlocksNF.COVERED_SILT.get(SoilCover.FOREST).get(), BlocksNF.COVERED_DIRT.get(SoilCover.FOREST).get(),
                BlocksNF.COVERED_LOAM.get(SoilCover.FOREST).get());
        tag(TagsNF.COCKATRICE_SPAWN_BLOCK).addTag(TagsNF.NATURAL_SOIL);
        tag(TagsNF.DEER_SPAWN_BLOCK).addTag(TagsNF.NATURAL_SOIL);
        tag(TagsNF.RABBIT_SPAWN_BLOCK).addTag(TagsNF.TILLABLE_SOIL);
        tag(TagsNF.CREEPER_SPAWN_BLOCK).addTag(TagsNF.TILLABLE_SOIL);
        tag(TagsNF.SPIDER_FREE_TRAVEL_BLOCK).add(BlocksNF.SPIDER_WEB.get(), BlocksNF.SPIDER_NEST.get());
        tag(TagsNF.TREE_REPLACEABLE).addTags(BlockTags.REPLACEABLE_PLANTS);
        tag(TagsNF.STRUCTURE_REPLACEABLE).addTags(TagsNF.SOIL, TagsNF.TREE_REPLACEABLE);
        for(var block : BlocksNF.STONE_BLOCKS.values()) tag(TagsNF.STRUCTURE_REPLACEABLE).add(block.get());
        for(var block : BlocksNF.ROCK_CLUSTERS.values()) tag(TagsNF.TREE_REPLACEABLE).add(block.get());
        tag(TagsNF.TREE_REPLACEABLE).add(BlocksNF.SNOW.get(), BlocksNF.WATER.get(), BlocksNF.SEAWATER.get(), Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR,
                BlocksNF.SPIDER_WEB.get());
        tag(TagsNF.STRUCTURE_REPLACEABLE).add(BlocksNF.ASH.get());
        tag(TagsNF.STRUCTURE_POST_PROCESS).addTags(BlockTags.FENCES, TagsNF.LADDERS);
        tag(TagsNF.STRUCTURE_POST_PROCESS).add(BlocksNF.TORCH.get(), BlocksNF.WALL_TORCH.get(), BlocksNF.TORCH_UNLIT.get(), BlocksNF.WALL_TORCH_UNLIT.get());

        tag(TagsNF.TECHNICAL).add(BlocksNF.WALL_TORCH.get(), BlocksNF.WALL_TORCH_UNLIT.get());
        for(RegistryObject<Block> entry : BlocksNF.BLOCKS.getEntries()) {
            Block block = entry.get();
            if(block instanceof StairBlockNF) tag(BlockTags.STAIRS).add(block);
            else if(block instanceof SlabBlockNF) tag(BlockTags.SLABS).add(block);
            else if(block instanceof SidingBlock) tag(TagsNF.SIDINGS).add(block);
            else if(block instanceof TrapdoorBlockNF) tag(BlockTags.TRAPDOORS).add(block);
            else if(block instanceof DoorBlockNF) tag(BlockTags.DOORS).add(block);
            else if(block instanceof HatchBlock) tag(TagsNF.HATCHES).add(block);
            else if(block instanceof LadderBlockNF) tag(TagsNF.LADDERS).add(block);
            else if(block instanceof FenceBlockNF) tag(BlockTags.FENCES).add(block);
            else if(block instanceof FenceGateBlockNF) tag(BlockTags.FENCE_GATES).add(block);
            else if(block instanceof TieredAnvilBlock) {
                tag(TagsNF.ANVILS).add(block);
                if(block instanceof MetalAnvilBlock) tag(TagsNF.METAL_ANVILS).add(block);
            }
            else if(block instanceof BarrelBlockNF) tag(Tags.Blocks.BARRELS).add(block);
            else if(block instanceof ChestBlockNF) tag(Tags.Blocks.CHESTS).add(block);
            else if(block instanceof RackBlock) tag(TagsNF.RACKS).add(block);
            else if(block instanceof ShelfBlock) tag(TagsNF.SHELVES).add(block);
        }
    }

    @SafeVarargs
    protected static void addSupports(HashMap<RegistryObject<? extends Block>, Integer> map, int support, RegistryObject<? extends Block>... blocks) {
        for(var block : blocks) map.put(block, support);
    }
}
