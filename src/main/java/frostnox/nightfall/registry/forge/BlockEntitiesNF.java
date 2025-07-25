package frostnox.nightfall.registry.forge;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlockEntity;
import frostnox.nightfall.block.block.barrel.BarrelBlockEntityNF;
import frostnox.nightfall.block.block.bowl.BowlBlockEntity;
import frostnox.nightfall.block.block.itemframe.ItemFrameBlockEntity;
import frostnox.nightfall.block.block.nest.GuardedNestBlockEntity;
import frostnox.nightfall.block.block.nest.NestBlockEntity;
import frostnox.nightfall.block.block.campfire.CampfireBlockEntityNF;
import frostnox.nightfall.block.block.cauldron.CauldronBlockEntity;
import frostnox.nightfall.block.block.TimeDataBlockEntity;
import frostnox.nightfall.block.block.fireable.FireableBlockEntity;
import frostnox.nightfall.block.block.chest.ChestBlockEntityNF;
import frostnox.nightfall.block.block.fireable.FireableHoldableBlockEntity;
import frostnox.nightfall.block.block.fuel.BurningFuelBlockEntity;
import frostnox.nightfall.block.block.crucible.CrucibleBlockEntity;
import frostnox.nightfall.block.block.mold.ItemMoldBlockEntity;
import frostnox.nightfall.block.block.nest.RockwormNestBlockEntity;
import frostnox.nightfall.block.block.pot.PotBlockEntity;
import frostnox.nightfall.block.block.rack.RackBlockEntity;
import frostnox.nightfall.block.block.shelf.ShelfBlockEntity;
import frostnox.nightfall.block.block.sign.SignBlockEntityNF;
import frostnox.nightfall.block.block.strangesoil.StrangeSoilBlockEntity;
import frostnox.nightfall.block.block.tree.TreeTrunkBlockEntity;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.entity.entity.animal.RabbitEntity;
import frostnox.nightfall.entity.entity.monster.RockwormEntity;
import frostnox.nightfall.entity.entity.monster.SpiderEntity;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.stream.Stream;

public class BlockEntitiesNF {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Nightfall.MODID);
    public static final RegistryObject<BlockEntityType<BarrelBlockEntityNF>> BARREL = BLOCK_ENTITIES.register("barrel", () -> BlockEntityType.Builder.of(BarrelBlockEntityNF::new, BlocksNF.BARRELS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<BowlBlockEntity>> BOWL = BLOCK_ENTITIES.register("bowl", () -> BlockEntityType.Builder.of(BowlBlockEntity::new, BlocksNF.WOODEN_BOWL.get()).build(null));
    public static final RegistryObject<BlockEntityType<ChestBlockEntityNF>> CHEST = BLOCK_ENTITIES.register("chest", () -> BlockEntityType.Builder.of(ChestBlockEntityNF::new, BlocksNF.CHESTS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<SignBlockEntityNF>> SIGN = BLOCK_ENTITIES.register("sign", () -> BlockEntityType.Builder.of(SignBlockEntityNF::new, Stream.of(BlocksNF.PLANK_STANDING_SIGNS.values(), BlocksNF.PLANK_WALL_SIGNS.values()).flatMap(Collection::stream).map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<ItemFrameBlockEntity>> ITEM_FRAME = BLOCK_ENTITIES.register("item_frame", () -> BlockEntityType.Builder.of(ItemFrameBlockEntity::new, BlocksNF.WOODEN_ITEM_FRAMES.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<RackBlockEntity>> RACK = BLOCK_ENTITIES.register("rack", () -> BlockEntityType.Builder.of(RackBlockEntity::new, BlocksNF.RACKS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<ShelfBlockEntity>> SHELF = BLOCK_ENTITIES.register("shelf", () -> BlockEntityType.Builder.of(ShelfBlockEntity::new, BlocksNF.SHELVES.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<StrangeSoilBlockEntity>> STRANGE_SOIL = BLOCK_ENTITIES.register("strange_soil", () -> BlockEntityType.Builder.of(StrangeSoilBlockEntity::new, BlocksNF.STRANGE_SOILS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<TreeTrunkBlockEntity>> TREE_TRUNK = BLOCK_ENTITIES.register("tree_trunk", () -> BlockEntityType.Builder.of(TreeTrunkBlockEntity::new, BlocksNF.TRUNKS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<CauldronBlockEntity>> CAULDRON = BLOCK_ENTITIES.register("cauldron", () -> BlockEntityType.Builder.of(CauldronBlockEntity::new, BlocksNF.CAULDRON.get()).build(null));
    public static final RegistryObject<BlockEntityType<PotBlockEntity>> POT = BLOCK_ENTITIES.register("pot", () -> BlockEntityType.Builder.of(PotBlockEntity::new, BlocksNF.POT.get()).build(null));
    public static final RegistryObject<BlockEntityType<CampfireBlockEntityNF>> CAMPFIRE = BLOCK_ENTITIES.register("campfire", () -> BlockEntityType.Builder.of(CampfireBlockEntityNF::new, BlocksNF.CAMPFIRE.get()).build(null));
    public static final RegistryObject<BlockEntityType<ItemMoldBlockEntity>> ITEM_MOLD = BLOCK_ENTITIES.register("item_mold", () -> BlockEntityType.Builder.of(ItemMoldBlockEntity::new, Stream.concat(BlocksNF.ARMAMENT_MOLDS.values().stream().map(RegistryObject::get), Stream.of(BlocksNF.INGOT_MOLD.get(), BlocksNF.ARROWHEAD_MOLD.get())).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE = BLOCK_ENTITIES.register("crucible", () -> BlockEntityType.Builder.of(CrucibleBlockEntity::new, BlocksNF.CRUCIBLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<FireableBlockEntity>> FIREABLE = BLOCK_ENTITIES.register("fireable", () -> BlockEntityType.Builder.of(FireableBlockEntity::new, BlocksNF.AZURITE.get(), BlocksNF.HEMATITE.get(), BlocksNF.CLAY_BRICKS.get(), BlocksNF.FIRE_CLAY_BRICKS.get(), BlocksNF.INGOT_PILES.get(Metal.IRON).get(), BlocksNF.CLAY.get()).build(null));
    public static final RegistryObject<BlockEntityType<FireableHoldableBlockEntity>> FIREABLE_POTTERY = BLOCK_ENTITIES.register("fireable_pottery", () -> BlockEntityType.Builder.of(FireableHoldableBlockEntity::new, Stream.concat(BlocksNF.UNFIRED_ARMAMENT_MOLDS.values().stream().map(RegistryObject::get), Stream.of(BlocksNF.UNFIRED_CRUCIBLE.get(), BlocksNF.UNFIRED_CAULDRON.get(), BlocksNF.UNFIRED_POT.get(), BlocksNF.UNFIRED_INGOT_MOLD.get(), BlocksNF.UNFIRED_ARROWHEAD_MOLD.get())).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<BurningFuelBlockEntity>> FUEL = BLOCK_ENTITIES.register("fuel", () -> BlockEntityType.Builder.of(BurningFuelBlockEntity::new, BlocksNF.COKE_BURNING.get(), BlocksNF.COAL_BURNING.get(), BlocksNF.CHARCOAL_BURNING.get(), BlocksNF.FIREWOOD_BURNING.get()).build(null));
    public static final RegistryObject<BlockEntityType<TieredAnvilBlockEntity>> ANVIL = BLOCK_ENTITIES.register("anvil", () -> BlockEntityType.Builder.of(TieredAnvilBlockEntity::new, Stream.of(BlocksNF.ANVILS_LOG.values(), BlocksNF.ANVILS_STONE.values(), BlocksNF.ANVILS_METAL.values()).flatMap(Collection::stream).map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<NestBlockEntity>> RABBIT_BURROW = BLOCK_ENTITIES.register("rabbit_burrow", () ->
            BlockEntityType.Builder.of((pos, state) -> new NestBlockEntity(BlockEntitiesNF.RABBIT_BURROW.get(), pos, state, 1, (int) ContinentalWorldType.DAY_LENGTH,
                            (level, homePos) -> {
                                RabbitEntity rabbit = EntitiesNF.RABBIT.get().create(level.getLevel());
                                rabbit.setHomePos(homePos);
                                float temp, humidity;
                                if(LevelData.isPresent(level)) {
                                    IChunkData chunkData = ChunkData.get(level.getChunkAt(homePos));
                                    temp = chunkData.getTemperature(homePos);
                                    humidity = chunkData.getHumidity(homePos);
                                }
                                else {
                                    temp = 0.5F;
                                    humidity = 0.5F;
                                }
                                rabbit.finalizeSpawn(level, level.getCurrentDifficultyAt(homePos), MobSpawnType.STRUCTURE, RabbitEntity.GroupData.create(temp, humidity), null);
                                return rabbit;
                            }),
                    BlocksNF.RABBIT_BURROW.get()).build(null));
    public static final RegistryObject<BlockEntityType<GuardedNestBlockEntity>> SPIDER_NEST = BLOCK_ENTITIES.register("spider_nest", () ->
            BlockEntityType.Builder.of((pos, state) -> new GuardedNestBlockEntity(BlockEntitiesNF.SPIDER_NEST.get(), pos, state, 5, (int) ContinentalWorldType.DAY_LENGTH / 2,
                            (level, homePos) -> {
                                SpiderEntity spider = EntitiesNF.SPIDER.get().create(level.getLevel());
                                spider.setHomePos(homePos);
                                float humidity;
                                if(LevelData.isPresent(level)) {
                                    IChunkData chunkData = ChunkData.get(level.getChunkAt(homePos));
                                    humidity = chunkData.getHumidity(homePos);
                                }
                                else humidity = 0.5F;
                                spider.finalizeSpawn(level, level.getCurrentDifficultyAt(homePos), MobSpawnType.STRUCTURE, SpiderEntity.GroupData.create(humidity), null);
                                return spider;
                            }, 8),
                    BlocksNF.SPIDER_NEST.get()).build(null));
    public static final RegistryObject<BlockEntityType<RockwormNestBlockEntity>> ROCKWORM_NEST = BLOCK_ENTITIES.register("rockworm_nest", () ->
            BlockEntityType.Builder.of((pos, state) -> new RockwormNestBlockEntity(BlockEntitiesNF.ROCKWORM_NEST.get(), pos, state, 1, (int) ContinentalWorldType.DAY_LENGTH * 7,
                            (level, homePos) -> {
                                RockwormEntity rockworm = EntitiesNF.ROCKWORM.get().create(level.getLevel());
                                rockworm.finalizeSpawn(level, level.getCurrentDifficultyAt(homePos), MobSpawnType.STRUCTURE, null, null);
                                return rockworm;
                            }, 9),
                    BlocksNF.ANCHORING_RESIN.get()).build(null));
    public static final RegistryObject<BlockEntityType<TimeDataBlockEntity>> TIME_DATA = BLOCK_ENTITIES.register("time_data", () ->
            BlockEntityType.Builder.of(TimeDataBlockEntity::new, BlocksNF.WET_MUD_BRICKS.get(), BlocksNF.POTATOES.get(), BlocksNF.CARROTS.get(), BlocksNF.FLAX.get(),
                    BlocksNF.YARROW.get(), BlocksNF.BERRY_BUSH.get()).build(null));

    public static void register() {
        BLOCK_ENTITIES.register(Nightfall.MOD_EVENT_BUS);
    }
}
