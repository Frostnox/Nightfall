package frostnox.nightfall.data;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.block.*;
import frostnox.nightfall.block.block.ClusterBlock;
import frostnox.nightfall.block.block.FruitBushBlock;
import frostnox.nightfall.block.block.SidingBlock;
import frostnox.nightfall.block.block.pile.PileBlock;
import frostnox.nightfall.data.loot.*;
import frostnox.nightfall.entity.entity.animal.DeerEntity;
import frostnox.nightfall.entity.entity.animal.RabbitEntity;
import frostnox.nightfall.entity.entity.monster.CockatriceEntity;
import frostnox.nightfall.item.Armament;
import frostnox.nightfall.item.TieredArmorMaterial;
import frostnox.nightfall.item.TieredItemMaterial;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.vanilla.LootTablesNF;
import frostnox.nightfall.world.generation.structure.ExplorerRuinsPiece;
import frostnox.nightfall.world.generation.structure.SlayerRuinsPiece;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.ChestLoot;
import net.minecraft.data.loot.EntityLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LootTableProviderNF extends LootTableProvider {
    public static final int TREE_SEED_STANDARD = -58, TREE_SEED_RARE = -88, TREE_SEED_COMMON = -28;
    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> tables = ImmutableList.of(
            Pair.of(BlockLootNF::new, LootContextParamSets.BLOCK),
            Pair.of(EntityLootNF::new, LootContextParamSets.ENTITY),
            Pair.of(ChestLootNF::new, LootContextParamSets.CHEST));

    public LootTableProviderNF(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected List<com.mojang.datafixers.util.Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return tables;
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {

    }

    private record PoolEntry(ItemLike item, float min, float max, int weight, List<LootItemFunction.Builder> functions, LootItemCondition.Builder... conditions) {}

    private static PoolEntry entry(ItemLike item, float min, float max, int weight, List<LootItemFunction.Builder> functions, LootItemCondition.Builder... conditions) {
        return new PoolEntry(item, min, max, weight, functions, conditions);
    }

    private static PoolEntry entry(ItemLike item, float min, float max, List<LootItemFunction.Builder> functions, LootItemCondition.Builder... conditions) {
        return entry(item, min, max, 1, functions, conditions);
    }

    private static PoolEntry entry(ItemLike item, float min, float max, LootItemCondition.Builder... conditions) {
        return entry(item, min, max, List.of(), conditions);
    }

    private static PoolEntry entry(ItemLike item, float min, float max, int weight, LootItemCondition.Builder... conditions) {
        return entry(item, min, max, weight, List.of(), conditions);
    }

    private static LootPool.Builder pool(PoolEntry... entries) {
        return pool(1, entries);
    }

    private static LootPool.Builder pool(float rolls, PoolEntry... entries) {
        return pool(ConstantValue.exactly(rolls), entries);
    }

    private static LootPool.Builder pool(NumberProvider rolls, PoolEntry... entries) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(rolls);
        for(PoolEntry entry : entries) {
            var itemBuilder = LootItem.lootTableItem(entry.item).setWeight(entry.weight).apply(SetItemCountFunction.setCount(
                    entry.min == entry.max ? ConstantValue.exactly(entry.min) : UniformGenerator.between(entry.min, entry.max)));
            for(LootItemFunction.Builder function : entry.functions) {
                itemBuilder = itemBuilder.apply(function);
            }
            for(LootItemCondition.Builder condition : entry.conditions) {
                itemBuilder = itemBuilder.when(condition);
            }
            pool.add(itemBuilder);
        }
        return pool;
    }

    private static class BlockLootNF extends BlockLoot {
        private final Set<Block> addedBlocks = new HashSet<>();

        @Override
        protected void add(Block pBlock, LootTable.Builder pLootTableBuilder) {
            super.add(pBlock, pLootTableBuilder);
            addedBlocks.add(pBlock);
        }

        @Override
        public Iterable<Block> getKnownBlocks() {
            return ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block.getRegistryName().getNamespace().equals(Nightfall.MODID)
                    && !block.getLootTable().equals(BuiltInLootTables.EMPTY) && !block.defaultBlockState().is(TagsNF.TECHNICAL)).collect(Collectors.toSet());
        }

        @Override
        protected void addTables() {
            dropOther(BlocksNF.SILT.get(), ItemsNF.SOILS.get(Soil.SILT).get(), 4);
            dropOther(BlocksNF.DIRT.get(), ItemsNF.SOILS.get(Soil.DIRT).get(), 4);
            dropOther(BlocksNF.LOAM.get(), ItemsNF.SOILS.get(Soil.LOAM).get(), 4);
            dropOther(BlocksNF.ASH.get(), ItemsNF.SOILS.get(Soil.ASH).get(), 4);
            dropOthersRandomReplace(BlocksNF.GRAVEL.get(), ItemsNF.SOILS.get(Soil.GRAVEL).get(), 3, ItemsNF.FLINT.get(), 0.125F, 1);
            dropOthersRandomReplace(BlocksNF.BLUE_GRAVEL.get(), ItemsNF.SOILS.get(Soil.BLUE_GRAVEL).get(), 3, ItemsNF.FLINT.get(), 0.125F, 1);
            dropOthersRandomReplace(BlocksNF.BLACK_GRAVEL.get(), ItemsNF.SOILS.get(Soil.BLACK_GRAVEL).get(), 3, ItemsNF.FLINT.get(), 0.125F, 1);
            dropOther(BlocksNF.SAND.get(), ItemsNF.SOILS.get(Soil.SAND).get(), 4);
            dropOther(BlocksNF.RED_SAND.get(), ItemsNF.SOILS.get(Soil.RED_SAND).get(), 4);
            dropOther(BlocksNF.WHITE_SAND.get(), ItemsNF.SOILS.get(Soil.WHITE_SAND).get(), 4);
            for(var block : BlocksNF.COVERED_SILT.values()) dropOther(block.get(), ItemsNF.SOILS.get(Soil.SILT).get(), 4);
            for(var block : BlocksNF.COVERED_DIRT.values()) dropOther(block.get(), ItemsNF.SOILS.get(Soil.DIRT).get(), 4);
            for(var block : BlocksNF.COVERED_LOAM.values()) dropOther(block.get(), ItemsNF.SOILS.get(Soil.LOAM).get(), 4);
            dropOther(BlocksNF.TILLED_SILT.get(), ItemsNF.SOILS.get(Soil.SILT).get(), 4);
            dropOther(BlocksNF.TILLED_DIRT.get(), ItemsNF.SOILS.get(Soil.DIRT).get(), 4);
            dropOther(BlocksNF.TILLED_LOAM.get(), ItemsNF.SOILS.get(Soil.LOAM).get(), 4);
            for(Soil soil : BlocksNF.STRANGE_SOILS.keySet()) dropOther(BlocksNF.STRANGE_SOILS.get(soil).get(), ItemsNF.SOILS.get(soil).get(), 3);

            dropOtherRequireLiving(BlocksNF.SNOW.get(), ItemsNF.SNOWBALL.get(), 1);
            dropOther(BlocksNF.PACKED_SNOW.get(), ItemsNF.SNOWBALL.get(), 8);
            dropOther(BlocksNF.MUD.get(), ItemsNF.MUD.get(), 4);
            dropOther(BlocksNF.CLAY.get(), ItemsNF.CLAY.get(), 4);
            dropOther(BlocksNF.FIRE_CLAY.get(), ItemsNF.FIRE_CLAY.get(), 4);

            for(Stone type : BlocksNF.STONE_BLOCKS.keySet()) dropStone(BlocksNF.STONE_BLOCKS.get(type).get(), type, 4);
            for(Stone type : BlocksNF.STONE_TUNNELS.keySet()) dropStone(BlocksNF.STONE_TUNNELS.get(type).get(), type, 4);
            for(Stone type : BlocksNF.ROCK_CLUSTERS.keySet()) dropCluster(BlocksNF.ROCK_CLUSTERS.get(type).get());
            dropCluster(BlocksNF.FLINT_CLUSTER.get());
            for(var block : BlocksNF.TIN_ORES.values()) {
                dropOthersPerception(block.get(), ItemsNF.TIN_CHUNK.get(), 1, ItemsNF.TIN_NUGGET.get(), 1, 0.25F, 0.025F, 4);
            }
            for(var block : BlocksNF.COPPER_ORES.values()) {
                dropOthersPerception(block.get(), ItemsNF.COPPER_CHUNK.get(), 1, ItemsNF.COPPER_NUGGET.get(), 1, 0.2F, 0.02F, 3);
            }
            for(var block : BlocksNF.AZURITE_ORES.values()) {
                dropOthersPerception(block.get(), ItemsNF.AZURITE_CHUNK.get(), 1, ItemsNF.AZURITE_NUGGET.get(), 1, 0.2F, 0.02F, 3);
            }
            for(var block : BlocksNF.HEMATITE_ORES.values()) {
                dropOthersPerception(block.get(), ItemsNF.HEMATITE_CHUNK.get(), 1, ItemsNF.HEMATITE_NUGGET.get(), 1, 0.2F, 0.02F, 3);
            }
            for(var block : BlocksNF.COAL_ORES.values()) {
                dropOtherPerception(block.get(), ItemsNF.COAL.get(), 1, 1, 0.2F, 0.02F, 1);
            }
            for(var block : BlocksNF.HALITE_ORES.values()) {
                dropOtherPerception(block.get(), ItemsNF.SALT.get(), 3, 1, 0.5F, 0.05F, 1);
            }
            dropOthersPerception(BlocksNF.METEORITE_ORE.get(), ItemsNF.METEORITE_CHUNK.get(), 1,
                    ItemsNF.METEORITE_NUGGET.get(), 1, 0.2F, 0.02F, 3);
            dropOtherPerception(BlocksNF.OBSIDIAN.get(), ItemsNF.OBSIDIAN_SHARD.get(), 2, 1, 0.5F, 0.05F, 1);

            dropOtherRequireLiving(BlocksNF.SHORT_GRASS.get(), ItemsNF.PLANT_FIBERS.get(), 2);
            dropOtherPerceptionRequireLiving(BlocksNF.GRASS.get(), ItemsNF.PLANT_FIBERS.get(), 2, 1, 0.5F, 0.02F, 1);
            dropOtherPerceptionRequireLiving(BlocksNF.TALL_GRASS.get(), ItemsNF.PLANT_FIBERS.get(), 3, 1, 0.5F, 0.025F, 1);
            dropOtherRequireLiving(BlocksNF.SMALL_FERN.get(), ItemsNF.PLANT_FIBERS.get(), 1);
            dropOtherPerceptionRequireLiving(BlocksNF.FERN.get(), ItemsNF.PLANT_FIBERS.get(), 1, 1, 0.3F, 0.012F, 1);
            dropOtherPerceptionRequireLiving(BlocksNF.LARGE_FERN.get(), ItemsNF.PLANT_FIBERS.get(), 2, 1, 0.3F, 0.015F, 1);
            dropOtherPerceptionRequireLiving(BlocksNF.VINES.get(), ItemsNF.PLANT_FIBERS.get(), 3, 1, 0.5F, 0.025F, 1);
            dropOther(BlocksNF.DEAD_BUSH.get(), ItemsNF.STICK.get(), 1);
            dropVegetableCrop(BlocksNF.POTATOES.get(), ItemsNF.POTATO.get(), 2, 4, ItemsNF.POTATO_SEEDS.get(), 0.15F, 0.025F);
            dropVegetableCrop(BlocksNF.CARROTS.get(), ItemsNF.CARROT.get(), 2, 3, ItemsNF.CARROT_SEEDS.get(), 0.15F, 0.025F);
            dropVegetableCrop(BlocksNF.FLAX.get(), ItemsNF.FLAX_FIBERS.get(), 2, 3, ItemsNF.FLAX_SEEDS.get(), 0.5F, 0.025F);
            dropVegetableCrop(BlocksNF.YARROW.get(), ItemsNF.YARROW.get(), 2, 3, ItemsNF.YARROW_SEEDS.get(), 0.35F, 0.025F);
            dropFruitBush(BlocksNF.BERRY_BUSH.get(), 1, 1, 2, 3, 0.5F, 0.025F);
            for(Tree type : Tree.values()) {
                dropOther(BlocksNF.TRUNKS.get(type).get(), BlocksNF.LOGS.get(type).get(), 1);
                dropOther(BlocksNF.STEMS.get(type).get(), BlocksNF.LOGS.get(type).get(), 1);
                int min = -68;
                if(type == Tree.PALM) min = -28;
                else if(type == Tree.LARCH || type == Tree.SPRUCE) min = -88;
                else if(type == Tree.BIRCH || type == Tree.ACACIA) min = -48;
                else if(type == Tree.REDWOOD) min = -38;
                dropLeaves(BlocksNF.LEAVES.get(type).get(), ItemsNF.TREE_SEEDS.get(type).get(), min, 1, 1, ItemsNF.STICK.get(), -1, 1);
                if(BlocksNF.BRANCHES.containsKey(type)) dropBranches(BlocksNF.BRANCHES.get(type).get(), ItemsNF.STICK.get(), -1, 1);
                dropSpecialAction(BlocksNF.LOGS.get(type).get(), BlocksNF.LOGS.get(type).get(), 1,
                        TagsNF.CHOPPING_ACTION, ItemsNF.FIREWOOD.get(), 4);
                dropSpecialAction(BlocksNF.STRIPPED_LOGS.get(type).get(), BlocksNF.STRIPPED_LOGS.get(type).get(), 1,
                        TagsNF.CHOPPING_ACTION, ItemsNF.PLANKS.get(type).get(), 8);
                dropOther(BlocksNF.PLANK_BLOCKS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 4);
                dropOther(BlocksNF.PLANK_STAIRS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 3);
                dropSlab(BlocksNF.PLANK_SLABS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 2);
                dropSiding(BlocksNF.PLANK_SIDINGS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 2);
                dropOther(BlocksNF.PLANK_FENCES.get(type).get(), ItemsNF.PLANKS.get(type).get(), 3);
                dropOther(BlocksNF.PLANK_FENCE_GATES.get(type).get(), ItemsNF.PLANKS.get(type).get(), 3);
                add(BlocksNF.PLANK_DOORS.get(type).get(), createDoubleBlockDrops(BlocksNF.PLANK_DOORS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 6));
                dropOther(BlocksNF.PLANK_TRAPDOORS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 3);
                dropOther(BlocksNF.PLANK_HATCHES.get(type).get(), ItemsNF.PLANKS.get(type).get(), 3);
                dropOther(BlocksNF.PLANK_LADDERS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 3);
                dropOther(BlocksNF.PLANK_STANDING_SIGNS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 3);
                dropOther(BlocksNF.PLANK_WALL_SIGNS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 3);
                dropOther(BlocksNF.WOODEN_ITEM_FRAMES.get(type).get(), ItemsNF.PLANKS.get(type).get(), 2);
                dropOther(BlocksNF.BARRELS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 16);
                dropOther(BlocksNF.CHESTS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 16);
                dropOther(BlocksNF.RACKS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 4);
                dropOther(BlocksNF.SHELVES.get(type).get(), ItemsNF.PLANKS.get(type).get(), 8);
                add(BlocksNF.CHAIRS.get(type).get(), createDoubleBlockDrops(BlocksNF.CHAIRS.get(type).get(), ItemsNF.PLANKS.get(type).get(), 4));
            }
            dropFruitLeaves(BlocksNF.FRUIT_LEAVES.get(Tree.JUNGLE).get(), ItemsNF.TREE_SEEDS.get(Tree.JUNGLE).get(), -68, 1, 1, ItemsNF.STICK.get(), -1, 1, ItemsNF.COCOA_POD.get());
            dropFruitLeaves(BlocksNF.FRUIT_LEAVES.get(Tree.OAK).get(), ItemsNF.TREE_SEEDS.get(Tree.OAK).get(), -68, 1, 1, ItemsNF.STICK.get(), -1, 1, ItemsNF.APPLE.get());
            dropFruitLeaves(BlocksNF.FRUIT_LEAVES.get(Tree.PALM).get(), ItemsNF.TREE_SEEDS.get(Tree.PALM).get(), -28, 1, 1, ItemsNF.STICK.get(), -1, 1, ItemsNF.COCONUT.get());

            for(Stone type : Stone.values()) {
                dropStone(BlocksNF.TILED_STONE.get(type).get(), type, 4);
                dropStone(BlocksNF.POLISHED_STONE.get(type).get(), type, 4);
                dropStone(BlocksNF.POLISHED_STONE_STAIRS.get(type).get(), type, 3);
                dropStoneSlab(BlocksNF.POLISHED_STONE_SLABS.get(type).get(), type, 2);
                dropStoneSiding(BlocksNF.POLISHED_STONE_SIDINGS.get(type).get(), type, 2);
                dropOther(BlocksNF.STACKED_STONE.get(type).get(), ItemsNF.ROCKS.get(type).get(), 4);
                dropOther(BlocksNF.STACKED_STONE_STAIRS.get(type).get(), ItemsNF.ROCKS.get(type).get(), 3);
                dropSlab(BlocksNF.STACKED_STONE_SLABS.get(type).get(), ItemsNF.ROCKS.get(type).get(), 2);
                dropSiding(BlocksNF.STACKED_STONE_SIDINGS.get(type).get(), ItemsNF.ROCKS.get(type).get(), 2);
                dropOther(BlocksNF.COBBLED_STONE.get(type).get(), ItemsNF.ROCKS.get(type).get(), 4);
                dropOther(BlocksNF.COBBLED_STONE_STAIRS.get(type).get(), ItemsNF.ROCKS.get(type).get(), 3);
                dropSlab(BlocksNF.COBBLED_STONE_SLABS.get(type).get(), ItemsNF.ROCKS.get(type).get(), 2);
                dropSiding(BlocksNF.COBBLED_STONE_SIDINGS.get(type).get(), ItemsNF.ROCKS.get(type).get(), 2);
                dropOther(BlocksNF.STONE_BRICK_BLOCKS.get(type).get(), ItemsNF.STONE_BRICKS.get(type).get(), 4);
                dropOther(BlocksNF.STONE_BRICK_STAIRS.get(type).get(), ItemsNF.STONE_BRICKS.get(type).get(), 3);
                dropSlab(BlocksNF.STONE_BRICK_SLABS.get(type).get(), ItemsNF.STONE_BRICKS.get(type).get(), 2);
                dropSiding(BlocksNF.STONE_BRICK_SIDINGS.get(type).get(), ItemsNF.STONE_BRICKS.get(type).get(), 2);
            }

            dropOther(BlocksNF.TERRACOTTA.get(), ItemsNF.TERRACOTTA_SHARD.get(), 8);
            dropOther(BlocksNF.TERRACOTTA_TILES.get(), ItemsNF.TERRACOTTA_SHARD.get(), 4);
            dropOther(BlocksNF.TERRACOTTA_TILE_STAIRS.get(), ItemsNF.TERRACOTTA_SHARD.get(), 3);
            dropSiding(BlocksNF.TERRACOTTA_TILE_SIDING.get(), ItemsNF.TERRACOTTA_SHARD.get(), 2);
            dropSlab(BlocksNF.TERRACOTTA_TILE_SLAB.get(), ItemsNF.TERRACOTTA_SHARD.get(), 2);
            dropOther(BlocksNF.TERRACOTTA_MOSAIC.get(), ItemsNF.TERRACOTTA_SHARD.get(), 4);
            dropOther(BlocksNF.TERRACOTTA_MOSAIC_STAIRS.get(), ItemsNF.TERRACOTTA_SHARD.get(), 3);
            dropSiding(BlocksNF.TERRACOTTA_MOSAIC_SIDING.get(), ItemsNF.TERRACOTTA_SHARD.get(), 2);
            dropSlab(BlocksNF.TERRACOTTA_MOSAIC_SLAB.get(), ItemsNF.TERRACOTTA_SHARD.get(), 2);
            dropOther(BlocksNF.MUD_BRICKS.get(), ItemsNF.MUD_BRICK.get(), 4);
            dropOther(BlocksNF.MUD_BRICK_STAIRS.get(), ItemsNF.MUD_BRICK.get(), 3);
            dropSiding(BlocksNF.MUD_BRICK_SIDING.get(), ItemsNF.MUD_BRICK.get(), 2);
            dropSlab(BlocksNF.MUD_BRICK_SLAB.get(), ItemsNF.MUD_BRICK.get(), 2);
            dropOther(BlocksNF.BRICKS.get(), ItemsNF.BRICK.get(), 4);
            dropOther(BlocksNF.BRICK_STAIRS.get(), ItemsNF.BRICK.get(), 3);
            dropSlab(BlocksNF.BRICK_SLAB.get(), ItemsNF.BRICK.get(), 2);
            dropSiding(BlocksNF.BRICK_SIDING.get(), ItemsNF.BRICK.get(), 2);
            dropOther(BlocksNF.FIRE_BRICKS.get(), ItemsNF.FIRE_BRICK.get(), 4);
            dropOther(BlocksNF.FIRE_BRICK_STAIRS.get(), ItemsNF.FIRE_BRICK.get(), 3);
            dropSiding(BlocksNF.FIRE_BRICK_SIDING.get(), ItemsNF.FIRE_BRICK.get(), 2);
            dropSlab(BlocksNF.FIRE_BRICK_SLAB.get(), ItemsNF.FIRE_BRICK.get(), 2);
            dropOther(BlocksNF.THATCH.get(), ItemsNF.PLANT_FIBERS.get(), 4);
            dropOther(BlocksNF.THATCH_STAIRS.get(), ItemsNF.PLANT_FIBERS.get(), 3);
            dropSlab(BlocksNF.THATCH_SLAB.get(), ItemsNF.PLANT_FIBERS.get(), 2);
            dropSiding(BlocksNF.THATCH_SIDING.get(), ItemsNF.PLANT_FIBERS.get(), 2);

            dropOther(BlocksNF.WET_MUD_BRICKS.get(), ItemsNF.MUD.get(), 4);
            dropOther(BlocksNF.CLAY_BRICKS.get(), ItemsNF.CLAY.get(), 4);
            dropOther(BlocksNF.FIRE_CLAY_BRICKS.get(), ItemsNF.FIRE_CLAY.get(), 4);
            dropCampfire(BlocksNF.CAMPFIRE.get());
            dropOther(BlocksNF.UNFIRED_POT.get(), ItemsNF.CLAY.get(), 8);
            dropOther(BlocksNF.UNFIRED_CAULDRON.get(), ItemsNF.CLAY.get(), 6);
            dropOther(BlocksNF.UNFIRED_CRUCIBLE.get(), ItemsNF.CLAY.get(), 4);
            for(var block : BlocksNF.UNFIRED_ARMAMENT_MOLDS.values()) dropOther(block.get(), ItemsNF.CLAY.get(), 4);
            dropOther(BlocksNF.UNFIRED_INGOT_MOLD.get(), ItemsNF.CLAY.get(), 4);
            dropOther(BlocksNF.UNFIRED_ARROWHEAD_MOLD.get(), ItemsNF.CLAY.get(), 4);
            dropOther(BlocksNF.TORCH.get(), ItemsNF.TORCH.get());
            dropOther(BlocksNF.TORCH_UNLIT.get(), ItemsNF.STICK.get());

            for(Tree type : BlocksNF.ANVILS_LOG.keySet()) dropSpecialAction(BlocksNF.ANVILS_LOG.get(type).get(), BlocksNF.LOGS.get(type).get(), 1,
                    TagsNF.CHOPPING_ACTION, ItemsNF.FIREWOOD.get(), 4);
            for(Stone type : BlocksNF.ANVILS_STONE.keySet()) dropStone(BlocksNF.ANVILS_STONE.get(type).get(), type, 4);
            for(Metal type : BlocksNF.INGOT_PILES.keySet())  dropOther(BlocksNF.INGOT_PILES.get(type).get(), ItemsNF.INGOTS.get(type).get(), 4);
            dropOthers(BlocksNF.STEEL_INGOT_PILE_POOR.get(), entry(ItemsNF.INGOTS.get(Metal.STEEL).get(), 2, 2),
                    entry(ItemsNF.INGOTS.get(Metal.IRON).get(), 2, 2));
            dropOthers(BlocksNF.STEEL_INGOT_PILE_FAIR.get(), entry(ItemsNF.INGOTS.get(Metal.STEEL).get(), 3, 3),
                    entry(ItemsNF.INGOTS.get(Metal.IRON).get(), 1, 1));
            dropOther(BlocksNF.COKE.get(), ItemsNF.COKE.get(), 4);
            dropOtherRandom(BlocksNF.COKE_BURNING.get(), ItemsNF.COKE.get(), 1, -1, 1, 1);
            dropOther(BlocksNF.COAL.get(), ItemsNF.COAL.get(), 4);
            dropOtherRandom(BlocksNF.COAL_BURNING.get(), ItemsNF.COAL.get(), 1, -1, 1, 1);
            dropOther(BlocksNF.CHARCOAL.get(), ItemsNF.CHARCOAL.get(), 4);
            dropOtherRandom(BlocksNF.CHARCOAL_BURNING.get(), ItemsNF.CHARCOAL.get(), 1, -1, 1, 1);
            dropOther(BlocksNF.FIREWOOD.get(), ItemsNF.FIREWOOD.get(), 8);
            dropOtherRandom(BlocksNF.FIREWOOD_BURNING.get(), ItemsNF.FIREWOOD.get(), 1, -1, 1, 1);
            dropOther(BlocksNF.SLAG.get(), ItemsNF.SLAG.get(), 4);
            dropOther(BlocksNF.AZURITE.get(), ItemsNF.AZURITE_CHUNK.get(), 4);
            dropOther(BlocksNF.HEMATITE.get(), ItemsNF.HEMATITE_CHUNK.get(), 4);
            addExplodable(BlocksNF.SMELTED_AZURITE.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.SLAG.get(), 2, 2)))
                            .withPool(pool(entry(ItemsNF.AZURITE_CHUNK.get(), 2, 2))));
            addExplodable(BlocksNF.SMELTED_HEMATITE.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.SLAG.get(), 2, 2)))
                    .withPool(pool(entry(ItemsNF.IRON_BLOOM.get(), 2, 2)))
                    .withPool(pool(entry(ItemsNF.STEEL_NUGGET.get(), 0, 0, List.of(PerceptionCountFunction.with(1, 0.15F, 0.01F))))));

            dropOthers(BlocksNF.SPIDER_WEB.get(), entry(ItemsNF.SILK.get(), -1, 1));
            dropOthers(BlocksNF.SPIDER_NEST.get(), entry(ItemsNF.SILK.get(), 10, 12));
            dropOtherPerception(BlocksNF.ANCHORING_RESIN.get(), ItemsNF.ANCHORING_RESIN.get(), 3, 1, 0.5F, 0.1F, 1);

            for(var block : getKnownBlocks()) {
                if(!addedBlocks.contains(block)) dropSelf(block);
            }
        }
        protected void addExplodable(Block block, LootTable.Builder table) {
            add(block, applyExplosionDecay(block, table));
        }

        protected void dropOther(Block block, ItemLike drop, int amount) {
            add(block, createSingleItemTable(drop, amount));
        }

        protected void dropOtherRequireLiving(Block block, ItemLike drop, int amount) {
            add(block, LootTable.lootTable().withPool(applyExplosionCondition(drop, LootPool.lootPool().setRolls(ConstantValue.exactly(amount))
                    .add(LootItem.lootTableItem(drop)).when(LootItemEntityCondition.of(LootItemEntityCondition.Test.LIVING_PRESENT)))));
        }

        protected void dropOthers(Block block, PoolEntry... drops) {
            LootTable.Builder table = LootTable.lootTable();
            for(PoolEntry entry : drops) table = table.withPool(pool(entry));
            add(block, applyExplosionDecay(block, table));
        }

        protected void dropOtherPerception(Block block, ItemLike drop, int amount, int pAmount, float chance, float increment, int pRolls) {
            dropOthersPerception(block, drop, amount, drop, pAmount, chance, increment, pRolls);
        }

        protected void dropOtherPerceptionRequireLiving(Block block, ItemLike drop, int amount, int pAmount, float chance, float increment, int pRolls) {
            dropOthersPerceptionRequireLiving(block, drop, amount, drop, pAmount, chance, increment, pRolls);
        }

        protected void dropOthersPerception(Block block, ItemLike drop, int amount, ItemLike pDrop, int pAmount, float chance, float increment, int pRolls) {
            add(block, createSingleItemTable(drop, amount).withPool(applyExplosionCondition(pDrop, LootPool.lootPool().setRolls(ConstantValue.exactly(pRolls)).add(
                    LootItem.lootTableItem(pDrop).apply(SetItemCountFunction.setCount(ConstantValue.exactly(0)))
                            .apply(PerceptionCountFunction.with(pAmount, chance, increment))))));
        }

        protected void dropOthersPerceptionRequireLiving(Block block, ItemLike drop, int amount, ItemLike pDrop, int pAmount, float chance, float increment, int pRolls) {
            add(block, LootTable.lootTable().withPool(applyExplosionCondition(drop, LootPool.lootPool().setRolls(ConstantValue.exactly(amount))
                    .add(LootItem.lootTableItem(drop)).when(LootItemEntityCondition.of(LootItemEntityCondition.Test.LIVING_PRESENT))))
                    .withPool(applyExplosionCondition(pDrop, LootPool.lootPool().setRolls(ConstantValue.exactly(pRolls)).add(
                    LootItem.lootTableItem(pDrop).apply(SetItemCountFunction.setCount(ConstantValue.exactly(0)))
                            .apply(PerceptionCountFunction.with(pAmount, chance, increment)))
                    .when(LootItemEntityCondition.of(LootItemEntityCondition.Test.LIVING_PRESENT)))));
        }

        protected void dropLeaves(Block block, ItemLike drop, int rMin, int rMax, int rRolls, ItemLike pDrop, int pMin, int pMax) {
            add(block, applyExplosionDecay(block, LootTable.lootTable()
                    //Seed
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(rRolls))
                            .add(LootItem.lootTableItem(drop)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(rMin, rMax)))))
                    //Stick
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(pDrop)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(pMin, pMax))))
                            .when(LootItemEntityCondition.of(LootItemEntityCondition.Test.LIVING_PRESENT)))));
        }

        protected void dropFruitLeaves(Block block, ItemLike drop, int rMin, int rMax, int rRolls, ItemLike pDrop, int pMin, int pMax, ItemLike fruit) {
            add(block, applyExplosionDecay(block, LootTable.lootTable()
                    //Seed
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(rRolls))
                            .add(LootItem.lootTableItem(drop)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(rMin, rMax)))))
                    //Fruit
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(fruit)))
                    //Stick
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(pDrop)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(pMin, pMax))))
                            .when(LootItemEntityCondition.of(LootItemEntityCondition.Test.LIVING_PRESENT)))));
        }

        protected void dropBranches(Block block, ItemLike pDrop, int pMin, int pMax) {
            add(block, applyExplosionDecay(block, LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(pDrop)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(pMin, pMax))))
                            .when(LootItemEntityCondition.of(LootItemEntityCondition.Test.LIVING_PRESENT)))));
        }

        protected void dropStone(Block block, Stone type, int amount) {
            add(block, LootTable.lootTable()
                    .withPool(pool(entry(ItemsNF.ROCKS.get(type).get(), amount, amount))
                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TagsNF.CHISEL_METAL)).invert()))
                    .withPool(pool(entry(ItemsNF.STONE_BRICKS.get(type).get(), amount, amount))
                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TagsNF.CHISEL_METAL)))));
        }

        protected void dropStoneSlab(Block block, Stone type, int amount) {
            add(block, LootTable.lootTable()
                    .withPool(pool(entry(ItemsNF.ROCKS.get(type).get(), amount, amount))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount * 2)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE))))
                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TagsNF.CHISEL_METAL)).invert()))
                    .withPool(pool(entry(ItemsNF.STONE_BRICKS.get(type).get(), amount, amount))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount * 2)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE))))
                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TagsNF.CHISEL_METAL)))));
        }

        protected void dropStoneSiding(Block block, Stone type, int amount) {
            add(block, LootTable.lootTable()
                    .withPool(pool(entry(ItemsNF.ROCKS.get(type).get(), amount, amount))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount * 2)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SidingBlock.TYPE, SidingBlock.Type.DOUBLE))))
                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TagsNF.CHISEL_METAL)).invert()))
                    .withPool(pool(entry(ItemsNF.STONE_BRICKS.get(type).get(), amount, amount))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount * 2)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SidingBlock.TYPE, SidingBlock.Type.DOUBLE))))
                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TagsNF.CHISEL_METAL)))));
        }

        protected void dropOtherRandom(Block block, ItemLike drop, int amount, int rMin, int rMax, int rRolls) {
            dropOthersRandom(block, drop, amount, drop, rMin, rMax, rRolls);
        }

        protected void dropOthersRandom(Block block, ItemLike drop, int amount, ItemLike rDrop, int rMin, int rMax, int rRolls) {
            add(block, createSingleItemTable(drop, amount).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(rRolls))
                    .add(applyExplosionDecay(rDrop, LootItem.lootTableItem(rDrop)
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(0)))
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(rMin, rMax)))))));
        }

        protected void dropOthersRandomReplace(Block block, ItemLike drop, int amount, ItemLike rDrop, float chance, int rRolls) {
            add(block, createSingleItemTable(drop, amount).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(rRolls))
                    .add(applyExplosionCondition(rDrop, LootItem.lootTableItem(rDrop).when(LootItemRandomChanceCondition.randomChance(chance))
                            .otherwise(LootItem.lootTableItem(drop))))));
        }

        protected void dropSlab(Block block, ItemLike drop, int amount) {
            add(block, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                    .add(applyExplosionDecay(drop, LootItem.lootTableItem(drop)
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE)).invert()))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount * 2)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE))))))));
        }

        protected void dropSiding(Block block, ItemLike drop, int amount) {
            add(block, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                    .add(applyExplosionDecay(drop, LootItem.lootTableItem(drop)
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SidingBlock.TYPE, SidingBlock.Type.DOUBLE)).invert()))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount * 2)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SidingBlock.TYPE, SidingBlock.Type.DOUBLE))))))));
        }

        protected void dropCampfire(Block block) {
            add(block, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                    .add(applyExplosionDecay(ItemsNF.FIREWOOD.get(), LootItem.lootTableItem(ItemsNF.FIREWOOD.get())
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStatePropertiesNF.FIREWOOD, 0))))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStatePropertiesNF.FIREWOOD, 2))))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStatePropertiesNF.FIREWOOD, 3))))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStatePropertiesNF.FIREWOOD, 4)))))))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(applyExplosionDecay(ItemsNF.CHARCOAL.get(), LootItem.lootTableItem(ItemsNF.CHARCOAL.get())
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
                                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStatePropertiesNF.FIREWOOD, 0)).invert()))))));
        }

        protected void dropCluster(ClusterBlock block) {
            add(block, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                    .add(applyExplosionDecay(block.drop.get(), LootItem.lootTableItem(block.drop.get())
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ClusterBlock.COUNT, 2))))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ClusterBlock.COUNT, 3))))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ClusterBlock.COUNT, 4))))))));
        }

        protected void dropPile(PileBlock block) {
            add(block, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                    .add(applyExplosionDecay(block.drop.get(), LootItem.lootTableItem(block.drop.get())
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.COUNT, 2))))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.COUNT, 3))))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.COUNT, 4))))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(5.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.COUNT, 5))))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(6.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.COUNT, 6))))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(7.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.COUNT, 7))))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(8.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.COUNT, 8))))))));
        }

        protected void dropSpecialAction(Block block, ItemLike drop, int amount, TagKey<Action> tag, ItemLike actionDrop, int actionAmount) {
            add(block, applyExplosionDecay(block, LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(drop).apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount))))
                            .when(ActionTagCondition.of(tag).invert()))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(actionDrop).apply(SetItemCountFunction.setCount(ConstantValue.exactly(actionAmount))))
                            .when(ActionTagCondition.of(tag)))));
        }

        protected void dropVegetableCrop(Block block, ItemLike vegetable, float min, float max, ItemLike seeds, float pChance, float pIncrement) {
            add(block, applyExplosionDecay(block, LootTable.lootTable()
                    //1 seed
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                    .add(LootItem.lootTableItem(seeds)))
                    //Vegetables if stage 8 and not destroyed by hungry animal
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                    .add(LootItem.lootTableItem(vegetable).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))))
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                            .hasProperty(BlockStatePropertiesNF.STAGE_8, 8)))
                    .when(LootItemEntityCondition.of(LootItemEntityCondition.Test.HUNGRY).invert()))
                    //Perception chance seed if stage 8 and not destroyed by hungry animal
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                    .add(LootItem.lootTableItem(seeds).apply(SetItemCountFunction.setCount(ConstantValue.exactly(0)))
                            .apply(PerceptionCountFunction.with(1, pChance, pIncrement)))
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                            .hasProperty(BlockStatePropertiesNF.STAGE_8, 8)))
                    .when(LootItemEntityCondition.of(LootItemEntityCondition.Test.HUNGRY).invert()))));
        }

        protected void dropFruitBush(FruitBushBlock block, int stage3Min, int stage3Max, int stage4Min, int stage4Max, float pChance, float pIncrement) {
            add(block, applyExplosionDecay(block, LootTable.lootTable()
                    //Drop self if destroyed by shovel or uprooted and mature
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(block))
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockStatePropertiesNF.STAGE_4, 2)).or(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockStatePropertiesNF.STAGE_4, 3))).or(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockStatePropertiesNF.STAGE_4, 4))))
                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TagsNF.SHOVEL)).or(
                                    LootItemEntityCondition.of(LootItemEntityCondition.Test.LIVING_PRESENT).invert())))
                    //Drop sticks if destroyed by not shovel and entity present and mature
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(ItemsNF.STICK.get())
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1F, 2F))))
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockStatePropertiesNF.STAGE_4, 2)).or(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockStatePropertiesNF.STAGE_4, 3))).or(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockStatePropertiesNF.STAGE_4, 4))))
                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TagsNF.SHOVEL)).invert())
                            .when(LootItemEntityCondition.of(LootItemEntityCondition.Test.LIVING_PRESENT)))
                    //Drop stick if not mature
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(ItemsNF.STICK.get())
                                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1F))))
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockStatePropertiesNF.STAGE_4, 1))))
                    //Fruit if stage 3 and not destroyed by hungry animal
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(block.fruitItem.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(stage3Min, stage3Max))))
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockStatePropertiesNF.STAGE_4, 3)))
                            .when(LootItemEntityCondition.of(LootItemEntityCondition.Test.HUNGRY).invert()))
                    //Fruit if stage 4 and not destroyed by hungry animal
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(block.fruitItem.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(stage4Min, stage4Max))))
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockStatePropertiesNF.STAGE_4, 4)))
                            .when(LootItemEntityCondition.of(LootItemEntityCondition.Test.HUNGRY).invert()))
                    //Perception chance fruit if stage 3/4 and not destroyed by hungry animal
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(block.fruitItem.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(0)))
                                    .apply(PerceptionCountFunction.with(1, pChance, pIncrement)))
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockStatePropertiesNF.STAGE_4, 3)).or(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(BlockStatePropertiesNF.STAGE_4, 4))))
                            .when(LootItemEntityCondition.of(LootItemEntityCondition.Test.HUNGRY).invert()))));
        }

        protected static LootTable.Builder createSingleItemTable(ItemLike item, int amount) {
            return LootTable.lootTable().withPool(applyExplosionCondition(item, LootPool.lootPool().setRolls(ConstantValue.exactly(amount)).add(LootItem.lootTableItem(item))));
        }

        protected static LootTable.Builder createDoubleBlockDrops(Block block, ItemLike drop, int amount) {
            return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(amount))
                    .add(applyExplosionDecay(block, LootItem.lootTableItem(drop)
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER))))));
        }
    }

    private static class EntityLootNF extends EntityLoot {
        @Override
        protected Iterable<EntityType<?>> getKnownEntities() {
            return ForgeRegistries.ENTITIES.getValues().stream().filter((e) -> e.getRegistryName().getNamespace().equals(Nightfall.MODID)).toList();
        }

        @Override
        protected void addTables() {
            add(EntitiesNF.RABBIT.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.RAW_GAME.get(), 2F, 3F)))
                    .withPool(pool(entry(ItemsNF.RABBIT_PELTS.get(RabbitEntity.Type.BRUSH).get(), 1F, 1F, LootItemEntityCondition.of(LootItemEntityCondition.Test.RABBIT_BRUSH))))
                    .withPool(pool(entry(ItemsNF.RABBIT_PELTS.get(RabbitEntity.Type.COTTONTAIL).get(), 1F, 1F, LootItemEntityCondition.of(LootItemEntityCondition.Test.RABBIT_COTTONTAIL))))
                    .withPool(pool(entry(ItemsNF.RABBIT_PELTS.get(RabbitEntity.Type.ARCTIC).get(), 1F, 1F, LootItemEntityCondition.of(LootItemEntityCondition.Test.RABBIT_ARCTIC))))
                    .withPool(pool(entry(ItemsNF.RABBIT_PELTS.get(RabbitEntity.Type.STRIPED).get(), 1F, 1F, LootItemEntityCondition.of(LootItemEntityCondition.Test.RABBIT_STRIPED)))));
            add(EntitiesNF.DEER.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.RAW_VENISON.get(), 3, 4)))
                    .withPool(pool(entry(ItemsNF.DEER_HIDES.get(DeerEntity.Type.BRIAR).get(), 1F, 1F, LootItemEntityCondition.of(LootItemEntityCondition.Test.DEER_BRIAR))))
                    .withPool(pool(entry(ItemsNF.DEER_HIDES.get(DeerEntity.Type.RED).get(), 1F, 1F, LootItemEntityCondition.of(LootItemEntityCondition.Test.DEER_RED))))
                    .withPool(pool(entry(ItemsNF.DEER_HIDES.get(DeerEntity.Type.SPOTTED).get(), 1F, 1F, LootItemEntityCondition.of(LootItemEntityCondition.Test.DEER_SPOTTED)))));
            add(EntitiesNF.HUSK.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.ROTTEN_FLESH.get(), 1, 2))));
            add(EntitiesNF.SKELETON.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.BONE_SHARD.get(), 1, 2)))
                    .withPool(pool(entry(ItemsNF.BONE_SHARD.get(), 1, 2, DamageTypeCondition.of(DamageType.STRIKING))))
                    .withPool(pool(entry(ItemsNF.LIVING_BONE.get(), 0, 0, List.of(PerceptionCountFunction.with(1, 0.35F, 0.04F))))));
            add(EntitiesNF.DREG.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.ROTTEN_FLESH.get(), 0, 1)))
                    .withPool(pool(entry(ItemsNF.DREG_HEART.get(), 0, 0, List.of(PerceptionCountFunction.with(1, 0.25F, 0.02F))))));
            add(EntitiesNF.CREEPER.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.SULFUR.get(), 2, 3))));
            add(EntitiesNF.COCKATRICE.get(), LootTable.lootTable()
                    .withPool(pool(entry(ItemsNF.COCKATRICE_SKINS.get(CockatriceEntity.Type.BRONZE).get(), 1F, 1F, LootItemEntityCondition.of(LootItemEntityCondition.Test.COCKATRICE_BRONZE))))
                    .withPool(pool(entry(ItemsNF.COCKATRICE_SKINS.get(CockatriceEntity.Type.EMERALD).get(), 1F, 1F, LootItemEntityCondition.of(LootItemEntityCondition.Test.COCKATRICE_EMERALD))))
                    .withPool(pool(entry(ItemsNF.COCKATRICE_FEATHER.get(), 6, 8)))
                    .withPool(pool(entry(ItemsNF.RAW_GAME.get(), 1, 2))).withPool(pool(entry(ItemsNF.RAW_POULTRY.get(), 2, 2))));
            add(EntitiesNF.SPIDER.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.SILK.get(), 1, 2))));
            add(EntitiesNF.ROCKWORM.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.SILK.get(), 1, 2))));
            add(EntitiesNF.PIT_DEVIL.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.SILK.get(), 1, 2))));
            add(EntitiesNF.SLIME.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.SILK.get(), 1, 2))));
            add(EntitiesNF.SCORPION.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.SILK.get(), 1, 2))));
            add(EntitiesNF.SCARAB.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.SILK.get(), 1, 2))));
            add(EntitiesNF.TROLL.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.SILK.get(), 1, 2))));
            add(EntitiesNF.OLMUR.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.SILK.get(), 1, 2))));
            add(EntitiesNF.JELLYFISH.get(), LootTable.lootTable().withPool(pool(entry(ItemsNF.RAW_JELLYFISH.get(), 1, 1))));
        }
    }

    private static class ChestLootNF extends ChestLoot {
        @Override
        public void accept(BiConsumer<ResourceLocation, LootTable.Builder> builder) {
            builder.accept(LootTablesNF.COTTAGE_RUINS_LOOT, LootTable.lootTable()
                    .withPool(pool(
                            entry(ItemsNF.TERRACOTTA_SHARD.get(),8, 16, 5),
                            entry(ItemsNF.COAL.get(),3, 6, 2),
                            entry(ItemsNF.BONE_SHARD.get(),3, 7, 1)))
                    .withPool(pool(
                            entry(ItemsNF.COPPER_NUGGET.get(), 5, 7, 3),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.BRONZE).get(Armament.ADZE).get(), 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.BRONZE).get(Armament.AXE).get(), 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.BRONZE).get(Armament.SHOVEL).get(), 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.BRONZE).get(Armament.SICKLE).get(),1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.ADZE).get(), 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.AXE).get(), 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.SHOVEL).get(), 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.SICKLE).get(),1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F))))
                    ).apply(PerceptionCheckFunction.with(0.5F, 0.04F))));
            builder.accept(LootTablesNF.DESERTED_CAMP_LOOT, LootTable.lootTable()
                    .withPool(pool(4,
                            entry(ItemsNF.STICK.get(),3, 6, 2),
                            entry(ItemsNF.PLANT_FIBERS.get(),6, 8, 2),
                            entry(ItemsNF.FLINT.get(),2, 4, 1)))
                    .withPool(pool(2,
                            entry(ItemsNF.COCKATRICE_FEATHER.get(), 6, 8, 1),
                            entry(ItemsNF.CLAY.get(), 20, 26, 2),
                            entry(ItemsNF.METEORITE_NUGGET.get(), 3, 5, 1),
                            entry(ItemsNF.METEORITE_CHUNK.get(), 1, 1, 1),
                            entry(ItemsNF.RABBIT_PELTS.get(RabbitEntity.Type.BRUSH).get(), 1, 2, 2),
                            entry(ItemsNF.DEER_HIDES.get(DeerEntity.Type.SPOTTED).get(), 1, 1, 1)
                    ).apply(PerceptionCheckFunction.with(0.5F, 0.04F))));
            builder.accept(LootTablesNF.SLAYER_RUINS_BARREL_LOOT, LootTable.lootTable()
                    .withPool(pool(2,
                            entry(ItemsNF.METAL_ARROWS.get(TieredItemMaterial.IRON).get(),2, 4, 3),
                            entry(ItemsNF.ROPE.get(),6, 9, 1),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.SPEAR).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.15F, 0.25F))))))
                    .withPool(pool(1,
                            entry(ItemsNF.METAL_ARROWS.get(TieredItemMaterial.IRON).get(),2, 4, 1)
                    ).apply(PerceptionCheckFunction.with(0.5F, 0.04F))));
            builder.accept(LootTablesNF.SLAYER_RUINS_CHEST_LOOT, LootTable.lootTable()
                    .withPool(pool(3,
                            entry(ItemsNF.BANDAGE.get(),1, 2, 3),
                            entry(ItemsNF.METAL_ARROWS.get(TieredItemMaterial.IRON).get(),4, 6, 4),
                            entry(ItemsNF.BOWS.get(Tree.OAK).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.BOWS.get(Tree.PALM).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.SWORD).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.15F, 0.2F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.MACE).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.15F, 0.2F)))),
                            entry(ItemsNF.METAL_SHIELDS_DYED.get(Metal.IRON).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.2F, 0.25F)),
                                            SetItemColorFunction.color(SlayerRuinsPiece.ITEM_COLOR)))))
                    .withPool(pool(1,
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.SABRE).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.15F, 0.2F))))
                    ).apply(PerceptionCheckFunction.with(0.5F, 0.04F))));
            builder.accept(LootTablesNF.EXPLORER_RUINS_LOOT, LootTable.lootTable()
                    .withPool(pool(1,
                            entry(ItemsNF.TERRACOTTA_SHARD.get(),8, 16, 7),
                            entry(ItemsNF.COAL.get(), 3, 6, 3),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.BRONZE).get(Armament.AXE).get(), 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.AXE).get(), 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.BRONZE).get(Armament.PICKAXE).get(), 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.PICKAXE).get(), 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.BRONZE).get(Armament.SICKLE).get(),1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.SICKLE).get(),1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.BRONZE).get(Armament.SHOVEL).get(), 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)))),
                            entry(ItemsNF.METAL_ARMAMENTS.get(TieredItemMaterial.IRON).get(Armament.SHOVEL).get(), 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F))))
                            ))
                    .withPool(pool(1,
                            entry(ItemsNF.HELMETS.get(TieredArmorMaterial.BRONZE_CHAINMAIL_EXPLORER).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)),
                                        SetItemColorFunction.color(ExplorerRuinsPiece.ITEM_COLOR))),
                            entry(ItemsNF.HELMETS.get(TieredArmorMaterial.IRON_CHAINMAIL_EXPLORER).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)),
                                            SetItemColorFunction.color(ExplorerRuinsPiece.ITEM_COLOR))),
                            entry(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.BRONZE_CHAINMAIL_EXPLORER).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)),
                                            SetItemColorFunction.color(ExplorerRuinsPiece.ITEM_COLOR))),
                            entry(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.IRON_CHAINMAIL_EXPLORER).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)),
                                            SetItemColorFunction.color(ExplorerRuinsPiece.ITEM_COLOR))),
                            entry(ItemsNF.HELMETS.get(TieredArmorMaterial.BRONZE_PLATE_EXPLORER).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)),
                                            SetItemColorFunction.color(ExplorerRuinsPiece.ITEM_COLOR))),
                            entry(ItemsNF.HELMETS.get(TieredArmorMaterial.IRON_PLATE_EXPLORER).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)),
                                            SetItemColorFunction.color(ExplorerRuinsPiece.ITEM_COLOR))),
                            entry(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.BRONZE_PLATE_EXPLORER).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)),
                                            SetItemColorFunction.color(ExplorerRuinsPiece.ITEM_COLOR))),
                            entry(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.IRON_PLATE_EXPLORER).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)),
                                            SetItemColorFunction.color(ExplorerRuinsPiece.ITEM_COLOR))),
                            entry(ItemsNF.HELMETS.get(TieredArmorMaterial.BRONZE_SCALE_EXPLORER).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)),
                                            SetItemColorFunction.color(ExplorerRuinsPiece.ITEM_COLOR))),
                            entry(ItemsNF.HELMETS.get(TieredArmorMaterial.IRON_SCALE_EXPLORER).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)),
                                            SetItemColorFunction.color(ExplorerRuinsPiece.ITEM_COLOR))),
                            entry(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.BRONZE_SCALE_EXPLORER).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)),
                                            SetItemColorFunction.color(ExplorerRuinsPiece.ITEM_COLOR))),
                            entry(ItemsNF.CHESTPLATES.get(TieredArmorMaterial.IRON_SCALE_EXPLORER).get(),1, 1, 1,
                                    List.of(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.15F)),
                                            SetItemColorFunction.color(ExplorerRuinsPiece.ITEM_COLOR)))
                    ).apply(PerceptionCheckFunction.with(0.35F, 0.028F))));
        }
    }
}
