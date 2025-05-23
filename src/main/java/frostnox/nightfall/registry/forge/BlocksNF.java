package frostnox.nightfall.registry.forge;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.*;
import frostnox.nightfall.block.block.*;
import frostnox.nightfall.block.block.IceBlock;
import frostnox.nightfall.block.block.SpiderWebBlock;
import frostnox.nightfall.block.block.anvil.MetalAnvilBlock;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlock;
import frostnox.nightfall.block.block.barrel.BarrelBlockNF;
import frostnox.nightfall.block.block.bowl.BowlBlock;
import frostnox.nightfall.block.block.nest.RabbitBurrowBlock;
import frostnox.nightfall.block.block.campfire.CampfireBlockNF;
import frostnox.nightfall.block.block.cauldron.CauldronBlockNF;
import frostnox.nightfall.block.block.crop.CropBlockNF;
import frostnox.nightfall.block.block.crop.DeadCropBlock;
import frostnox.nightfall.block.block.fireable.*;
import frostnox.nightfall.block.block.chest.ChestBlockNF;
import frostnox.nightfall.block.block.fuel.BurningFuelBlock;
import frostnox.nightfall.block.block.crucible.CrucibleBlock;
import frostnox.nightfall.block.block.fuel.BurningHorizontalFuelBlock;
import frostnox.nightfall.block.block.fuel.FuelBlock;
import frostnox.nightfall.block.block.fuel.HorizontalFuelBlock;
import frostnox.nightfall.block.block.liquid.LavaLiquidBlock;
import frostnox.nightfall.block.block.liquid.SizedLiquidBlock;
import frostnox.nightfall.block.block.mold.ItemMoldBlock;
import frostnox.nightfall.block.block.nest.SpiderNestBlock;
import frostnox.nightfall.block.block.pot.PotBlock;
import frostnox.nightfall.block.block.rack.RackBlock;
import frostnox.nightfall.block.block.shelf.ShelfBlock;
import frostnox.nightfall.block.block.strangesoil.StrangeSoilBlock;
import frostnox.nightfall.block.block.tree.*;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.item.Armament;
import frostnox.nightfall.util.DataUtil;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.IBlockRenderProperties;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlocksNF {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Nightfall.MODID);

    public static final Material SOLID_DECORATION = new Material(MaterialColor.NONE, false, true, true, false, false, false, PushReaction.NORMAL);
    public static final Material SOLID_FLAMMABLE_DECORATION = new Material(MaterialColor.NONE, false, true, true, false, true, false, PushReaction.NORMAL);
    public static final Material FLAMMABLE_DECORATION = new Material(MaterialColor.NONE, false, false, false, false, true, false, PushReaction.NORMAL);
    public static final Material REPLACEABLE_DECORATION = new Material(MaterialColor.NONE, false, false, false, false, false, true, PushReaction.DESTROY);
    public static final IBlockRenderProperties NO_BREAK_PARTICLES = new IBlockRenderProperties() {
        @Override
        public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
            return true;
        }

        @Override
        public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
            return true;
        }
    };

    //Terrain
    public static final Map<SoilCover, RegistryObject<? extends CoveredSoilBlock>> COVERED_SILT = DataUtil.mapEnum(SoilCover.class,
            cover -> register(cover.prefix + "_silt", () -> new CoveredSoilBlock(BlocksNF.SILT, BlocksNF.TILLED_SILT, Soil.SILT, cover, BlockBehaviour.Properties.of(Material.GRASS, cover.color)
                    .strength(Soil.SILT.getStrength() + 0.25F, Soil.SILT.getExplosionResistance()).sound(cover.sound).randomTicks())));
    public static final Map<SoilCover, RegistryObject<? extends CoveredSoilBlock>> COVERED_DIRT = DataUtil.mapEnum(SoilCover.class,
            cover -> register(cover.prefix + "_dirt", () -> new CoveredSoilBlock(BlocksNF.DIRT, BlocksNF.TILLED_DIRT, Soil.DIRT, cover, BlockBehaviour.Properties.of(Material.GRASS, cover.color)
                    .strength(Soil.DIRT.getStrength() + 0.25F, Soil.DIRT.getExplosionResistance()).sound(cover.sound).randomTicks())));
    public static final Map<SoilCover, RegistryObject<? extends CoveredSoilBlock>> COVERED_LOAM = DataUtil.mapEnum(SoilCover.class,
            cover -> register(cover.prefix + "_loam", () -> new CoveredSoilBlock(BlocksNF.LOAM, BlocksNF.TILLED_LOAM, Soil.LOAM, cover, BlockBehaviour.Properties.of(Material.GRASS, cover.color)
                    .strength(Soil.LOAM.getStrength() + 0.25F, Soil.LOAM.getExplosionResistance()).sound(cover.sound).randomTicks())));
    public static final RegistryObject<SoilBlock> SILT = register(Soil.SILT.getName() + "_block", () -> new SoilBlock(Soil.SILT.getSlideSound(),
            COVERED_SILT, BlocksNF.TILLED_SILT, BlockBehaviour.Properties.of(Material.DIRT, Soil.SILT.getBaseColor())
            .strength(Soil.SILT.getStrength(), Soil.SILT.getExplosionResistance()).randomTicks().sound(Soil.SILT.getSound())));
    public static final RegistryObject<DirtBlock> DIRT = register(Soil.DIRT.getName() + "_block", () -> new DirtBlock(Soil.DIRT.getSlideSound(),
            COVERED_DIRT, BlocksNF.TILLED_DIRT, BlockBehaviour.Properties.of(Material.DIRT, Soil.DIRT.getBaseColor())
            .strength(Soil.DIRT.getStrength(), Soil.DIRT.getExplosionResistance()).randomTicks().sound(Soil.DIRT.getSound())));
    public static final RegistryObject<SoilBlock> LOAM = register(Soil.LOAM.getName() + "_block", () -> new SoilBlock(Soil.LOAM.getSlideSound(),
            COVERED_LOAM, BlocksNF.TILLED_LOAM, BlockBehaviour.Properties.of(Material.DIRT, Soil.LOAM.getBaseColor())
            .strength(Soil.LOAM.getStrength(), Soil.LOAM.getExplosionResistance()).randomTicks().sound(Soil.LOAM.getSound())));
    public static final RegistryObject<UnstableBlock> ASH = soil(Soil.ASH);
    public static final RegistryObject<UnstableBlock> GRAVEL = soil(Soil.GRAVEL);
    public static final RegistryObject<UnstableBlock> BLUE_GRAVEL = soil(Soil.BLUE_GRAVEL);
    public static final RegistryObject<UnstableBlock> BLACK_GRAVEL = soil(Soil.BLACK_GRAVEL);
    public static final RegistryObject<UnstableBlock> SAND = soil(Soil.SAND);
    public static final RegistryObject<UnstableBlock> RED_SAND = soil(Soil.RED_SAND);
    public static final RegistryObject<UnstableBlock> WHITE_SAND = soil(Soil.WHITE_SAND);
    public static final RegistryObject<TilledSoilBlock> TILLED_SILT = tilledSoil(Soil.SILT, 0F, 0.5F, 1F, SILT);
    public static final RegistryObject<TilledSoilBlock> TILLED_DIRT = tilledSoil(Soil.DIRT, 0.125F, 0.5F, 1F, DIRT);
    public static final RegistryObject<TilledSoilBlock> TILLED_LOAM = tilledSoil(Soil.LOAM, 0.25F, 0.5F, 1F, LOAM);
    public static final Map<Soil, RegistryObject<StrangeSoilBlock>> STRANGE_SOILS = DataUtil.mapEnum(Soil.class,
            soil -> register("strange_" + soil.getName(), () -> new StrangeSoilBlock(soil.getSlideSound(), Soil.getBlock(soil), BlockBehaviour.Properties.of(Material.DIRT,
                    soil.getBaseColor()).strength(soil.getStrength(), soil.getExplosionResistance()).sound(soil.getSound()))));

    public static final RegistryObject<SnowBlock> SNOW = BLOCKS.register("snow", () -> new SnowBlock(BlockBehaviour.Properties.of(Material.TOP_SNOW)
            .strength(0.1F).noCollission().requiresCorrectToolForDrops().randomTicks().sound(SoundType.SNOW)));
    public static final RegistryObject<MeltableBlock> PACKED_SNOW = BLOCKS.register("packed_snow", () -> new MeltableBlock(BlocksNF.WATER, 0.3F,
            BlockBehaviour.Properties.of(Material.SNOW).strength(2F).requiresCorrectToolForDrops().randomTicks().sound(SoundType.SNOW)));
    public static final RegistryObject<UnstableBlock> MUD = BLOCKS.register("mud_block", () -> new UnstableBlock(SoundsNF.WET_SOIL_FALL,
            BlockBehaviour.Properties.of(Material.DIRT, MaterialColor.TERRACOTTA_BROWN).strength(Soil.DIRT.getStrength() + 0.2F)
                    .speedFactor(0.6F).jumpFactor(0.6F).sound(SoundsNF.MUD_TYPE)));
    public static final RegistryObject<SimpleFireableBlock> CLAY = BLOCKS.register("clay_block", () -> new SimpleFireableBlock(
            20 * 60 * 8, TieredHeat.ORANGE, BlocksNF.TERRACOTTA, BlockBehaviour.Properties.of(Material.CLAY).strength(1.5F).sound(SoundsNF.MUD_TYPE)));
    public static final RegistryObject<Block> FIRE_CLAY = BLOCKS.register("fire_clay_block", () -> new BlockNF(
            BlockBehaviour.Properties.of(Material.CLAY, MaterialColor.TERRACOTTA_RED).strength(1.5F).sound(SoundType.GRAVEL)));
    public static final RegistryObject<Block> BEDROCK = BLOCKS.register("bedrock", () -> new Block(BlockBehaviour.Properties.of(
            Material.STONE, MaterialColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noDrops()));
    public static final RegistryObject<IceBlock> ICE = BLOCKS.register("ice", () -> new IceBlock(BlocksNF.WATER, LevelData.FRAZIL_TEMP,
            BlockBehaviour.Properties.of(Material.ICE).strength(1.5F).noDrops().noOcclusion().friction(0.96F).randomTicks().sound(SoundType.GLASS)));
    public static final RegistryObject<IceBlock> SEA_ICE = BLOCKS.register("sea_ice", () -> new IceBlock(BlocksNF.SEAWATER, LevelData.SEA_FRAZIL_TEMP,
            BlockBehaviour.Properties.of(Material.ICE_SOLID).strength(2F).noDrops().friction(0.96F).randomTicks().sound(SoundType.GLASS)));
    public static final RegistryObject<FrazilBlock> FRAZIL = BLOCKS.register("frazil", () -> new FrazilBlock(LevelData.FRAZIL_TEMP,
            BlockBehaviour.Properties.of(REPLACEABLE_DECORATION).noDrops().noOcclusion().noCollission().instabreak().randomTicks().sound(SoundType.SNOW)));
    public static final RegistryObject<FrazilBlock> SEA_FRAZIL = BLOCKS.register("sea_frazil", () -> new FrazilBlock(LevelData.SEA_FRAZIL_TEMP,
            BlockBehaviour.Properties.of(REPLACEABLE_DECORATION).noDrops().noOcclusion().noCollission().instabreak().randomTicks().sound(SoundType.SNOW)));
    public static final RegistryObject<GroundItemBlock> SEASHELL = BLOCKS.register("seashell", () -> new GroundItemBlock(
            BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak().sound(SoundsNF.SEASHELL_TYPE)));

    public static final RegistryObject<FireBlockNF> FIRE = BLOCKS.register("fire", () -> new FireBlockNF(BlockBehaviour.Properties.of(Material.FIRE,
            MaterialColor.FIRE).noCollission().instabreak().noDrops().lightLevel((state) -> 15).sound(SoundType.WOOL)));
    public static final RegistryObject<SizedLiquidBlock> WATER = BLOCKS.register("water", () -> new SizedLiquidBlock(FluidsNF.WATER,
            BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100.0F).noDrops()));
    public static final RegistryObject<SizedLiquidBlock> SEAWATER = BLOCKS.register("seawater", () -> new SizedLiquidBlock(FluidsNF.SEAWATER,
            BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100.0F).noDrops()));
    public static final RegistryObject<LavaLiquidBlock> LAVA = BLOCKS.register("lava", () -> new LavaLiquidBlock(FluidsNF.LAVA,
            BlockBehaviour.Properties.of(Material.LAVA).noCollission().strength(100.0F).noDrops().randomTicks().lightLevel((state) -> 15)));
    //Plants
    public static final RegistryObject<VegetationBlock> SHORT_GRASS = BLOCKS.register("short_grass", () ->
            new VegetationBlock(Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D), 3000, 0F, 1F, 0F, 1F, false,
                    BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT).strength(0.6F).noCollission().sound(SoundType.AZALEA_LEAVES)));
    public static final RegistryObject<VegetationBlock> GRASS = BLOCKS.register("grass", () ->
            new VegetationBlock(Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D), 3000, 0F, 1F, 0.03F, 1F, false,
                    BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT).strength(0.7F).noCollission().sound(SoundType.AZALEA_LEAVES)));
    public static final RegistryObject<VegetationBlock> TALL_GRASS = BLOCKS.register("tall_grass", () ->
            new VegetationBlock(Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D), 3000, 0.42F, 0.58F, 0.05F, 0.3F, false,
                    BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT).strength(0.8F).noCollission().sound(SoundType.AZALEA_LEAVES)));
    public static final RegistryObject<VegetationBlock> SMALL_FERN = BLOCKS.register("small_fern", () ->
            new VegetationBlock(Block.box(2.0D, 0.0D, 2.0D, 14.0D, 9.0D, 14.0D), 200, 0.05F, 0.95F, 0.68F, 1F, true,
                    BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT).strength(0.6F).noCollission().sound(SoundType.AZALEA_LEAVES)));
    public static final RegistryObject<VegetationBlock> FERN = BLOCKS.register("fern", () ->
            new VegetationBlock(Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D), 150, 0.05F, 0.95F, 0.72F, 1F, true,
                    BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT).strength(0.7F).noCollission().sound(SoundType.AZALEA_LEAVES)));
    public static final RegistryObject<VegetationBlock> LARGE_FERN = BLOCKS.register("large_fern", () ->
            new VegetationBlock(Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D), 90, 0.05F, 0.95F, 0.75F, 1F, true,
                    BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT).strength(0.8F).noCollission().sound(SoundType.AZALEA_LEAVES)));
    public static final RegistryObject<VinesBlockNF> VINES = BLOCKS.register("vines", () -> new VinesBlockNF(BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT)
            .strength(0.6F).noCollission().randomTicks().sound(SoundType.VINE)));
    /*public static final RegistryObject<LichenBlock> LICHEN = BLOCKS.register("lichen", () -> new LichenBlock(BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT)
            .strength(0.4F).noCollission().sound(SoundType.GLOW_LICHEN)));*/
    public static final RegistryObject<BushBlockNF> DEAD_BUSH = BLOCKS.register("dead_bush", () -> new BushBlockNF(
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 11.0D, 12.0D), BlockBehaviour.Properties
            .of(Material.PLANT).noCollission().strength(1F).sound(SoundType.HANGING_ROOTS)));
    public static final RegistryObject<BushBlockNF> DEAD_PLANT = BLOCKS.register("dead_plant", () -> new BushBlockNF(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), BlockBehaviour.Properties
            .of(Material.PLANT).noCollission().noDrops().strength(0.6F).sound(SoundType.HANGING_ROOTS)));
    public static final RegistryObject<DeadCropBlock> DEAD_CROP = BLOCKS.register("dead_crop", () -> new DeadCropBlock(BlockBehaviour.Properties
            .of(Material.PLANT).noCollission().noDrops().strength(0.6F).sound(SoundType.HANGING_ROOTS)));
    public static final RegistryObject<CropBlockNF> POTATOES = BLOCKS.register("potatoes", () -> new CropBlockNF(
            Fertility.FAIR, 0.375F, 1F, 0.35F, 0.9F, 11, (int) (ContinentalWorldType.DAY_LENGTH * 7F/8F / 2F),
            ItemsNF.POTATO_SEEDS, false, BlockBehaviour.Properties.of(Material.PLANT).noCollission().strength(0.6F).sound(SoundType.CROP)));
    public static final RegistryObject<CropBlockNF> CARROTS = BLOCKS.register("carrots", () -> new CropBlockNF(
            Fertility.POOR, 0.25F, 1F, 0.2F, 0.85F, 8, (int) (ContinentalWorldType.DAY_LENGTH * 6F/8F / 2F),
            ItemsNF.CARROT_SEEDS, false, BlockBehaviour.Properties.of(Material.PLANT).noCollission().strength(0.6F).sound(SoundType.CROP)));
    public static final RegistryObject<CropBlockNF> FLAX = BLOCKS.register("flax", () -> new CropBlockNF(
            Fertility.POOR, 0.05F, 0.85F, 0.1F, 1F, 13, (int) (ContinentalWorldType.DAY_LENGTH / 2F),
            ItemsNF.FLAX_SEEDS, false, BlockBehaviour.Properties.of(Material.PLANT).noCollission().strength(0.6F).sound(SoundType.CROP)));
    public static final RegistryObject<CropBlockNF> YARROW = BLOCKS.register("yarrow", () -> new CropBlockNF(
            Fertility.POOR, 0.1F, 0.8F, 0.1F, 1F, 11, (int) (ContinentalWorldType.DAY_LENGTH * 6F/8F / 2F),
            ItemsNF.YARROW_SEEDS, true, BlockBehaviour.Properties.of(Material.PLANT).noCollission().strength(0.6F).sound(SoundType.CROP)));
    public static final RegistryObject<FruitBushBlock> BERRY_BUSH = BLOCKS.register("berry_bush", () -> new FruitBushBlock(
            0.1F, 0.9F, 0.1F, 0.9F, 8, (int) (ContinentalWorldType.DAY_LENGTH * 2F),
            ItemsNF.BERRIES, BlockBehaviour.Properties.of(Material.PLANT).noCollission().strength(3F)
            .speedFactor(0.8F).jumpFactor(0.9F).sound(SoundType.SWEET_BERRY_BUSH)));
    //Stone
    public static final Map<Stone, RegistryObject<StoneBlock>> STONE_BLOCKS = DataUtil.mapEnum(Stone.class, stone ->
            register(stone.getName(), () -> new StoneBlock(BlocksNF.ANVILS_STONE.get(stone),
                    BlockBehaviour.Properties.of(Material.STONE, stone.getBaseColor()).requiresCorrectToolForDrops()
                    .strength(stone.getStrength(), stone.getExplosionResistance()).sound(stone.getSound()))));
    private static final VoxelShape ROCK_SHAPE_12 = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 2.0D, 13.0D);
    private static final VoxelShape ROCK_SHAPE_34 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D);
    public static final Map<Stone, RegistryObject<ClusterBlock>> ROCK_CLUSTERS = DataUtil.mapEnum(Stone.class, stone -> register(stone.getName() + "_rocks",
            () -> new ClusterBlock(ItemsNF.ROCKS.get(stone), BlockBehaviour.Properties.of(Material.DECORATION).noCollission().strength(1F).sound(SoundType.STONE),
                    ROCK_SHAPE_12, ROCK_SHAPE_12, ROCK_SHAPE_34, ROCK_SHAPE_34)));
    public static final RegistryObject<ClusterBlock> FLINT_CLUSTER = register("flint_rocks", () -> new ClusterBlock(ItemsNF.FLINT,
            BlockBehaviour.Properties.of(Material.DECORATION).noCollission().strength(1F).sound(SoundType.STONE),
            ROCK_SHAPE_12, ROCK_SHAPE_12, ROCK_SHAPE_34, ROCK_SHAPE_34));

    public static final Map<Stone, RegistryObject<Block>> TIN_ORES = DataUtil.mapEnum(Stone.class, stone -> stone == Stone.PUMICE || stone == Stone.MOONSTONE, stone ->
            register(stone.getName() + "_tin_ore", () -> new Block(BlockBehaviour.Properties.copy(STONE_BLOCKS.get(stone).get()))));
    public static final Map<Stone, RegistryObject<Block>> COPPER_ORES = DataUtil.mapEnum(Stone.class, stone -> stone == Stone.PUMICE || stone == Stone.MOONSTONE, stone ->
            register(stone.getName() + "_copper_ore", () -> new Block(BlockBehaviour.Properties.copy(STONE_BLOCKS.get(stone).get()))));
    public static final Map<Stone, RegistryObject<Block>> AZURITE_ORES = DataUtil.mapEnum(Stone.class, stone -> stone == Stone.PUMICE || stone == Stone.MOONSTONE, stone ->
            register(stone.getName() + "_azurite_ore", () -> new Block(BlockBehaviour.Properties.copy(STONE_BLOCKS.get(stone).get()))));
    public static final Map<Stone, RegistryObject<Block>> HEMATITE_ORES = DataUtil.mapEnum(Stone.class, stone -> stone == Stone.PUMICE || stone == Stone.MOONSTONE, stone ->
            register(stone.getName() + "_hematite_ore", () -> new Block(BlockBehaviour.Properties.copy(STONE_BLOCKS.get(stone).get()))));
    public static final Map<Stone, RegistryObject<Block>> COAL_ORES = DataUtil.mapEnum(Stone.class, stone -> stone == Stone.PUMICE || stone == Stone.MOONSTONE, stone ->
            register(stone.getName() + "_coal_ore", () -> new Block(BlockBehaviour.Properties.copy(STONE_BLOCKS.get(stone).get()))));
    public static final RegistryObject<Block> METEORITE_ORE = BLOCKS.register("meteorite_ore", () -> new Block(BlockBehaviour.Properties
            .copy(STONE_BLOCKS.get(Stone.MOONSTONE).get())));

    public static final RegistryObject<Block> OBSIDIAN = BLOCKS.register("obsidian", () -> new Block(BlockBehaviour.Properties.of(Material.STONE,
            MaterialColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(55.0F, 1200.0F)));
    //Trees
    public static final Map<Tree, RegistryObject<LogBlock>> LOGS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_log", () -> new LogBlock(BlocksNF.STRIPPED_LOGS.get(tree), BlocksNF.ANVILS_LOG.get(tree),
                    BlockBehaviour.Properties.of(Material.WOOD, (state) ->
                                    state.getValue(LogBlock.AXIS) == Direction.Axis.Y ? tree.getBaseColor() : tree.getBarkColor())
                    .strength(tree.getStrength(), tree.getExplosionResistance()).sound(tree.getSound()))));

    public static final Map<Tree, RegistryObject<TreeStemBlock>> STEMS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_stem", () -> new TreeStemBlock(tree, BlockBehaviour.Properties.of(Material.WOOD, (state) ->
                            state.getValue(TreeStemBlock.TYPE) != TreeStemBlock.Type.TOP &&
                                    state.getValue(TreeStemBlock.TYPE) != TreeStemBlock.Type.ROTATED_TOP ? tree.getBarkColor() :
                                    (state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? tree.getBaseColor() : tree.getBarkColor()))
                            .strength(tree.getStrength(), tree.getExplosionResistance()).sound(tree.getSound())) {
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            }));

    public static final Map<Tree, RegistryObject<RotatedPillarBlock>> STRIPPED_LOGS = DataUtil.mapEnum(Tree.class, tree ->
            register("stripped_" + tree.getName() + "_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD,tree.getBaseColor())
                    .strength(tree.getStrength(), tree.getExplosionResistance()).sound(tree.getSound())) {
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            }));

    public static final Map<Tree, RegistryObject<TreeLeavesBlock>> LEAVES = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_leaves", () -> new TreeLeavesBlock(tree, BlockBehaviour.Properties.of(Material.LEAVES)
                    .strength(1F).speedFactor(0.6F).jumpFactor(0.9F).sound(SoundType.AZALEA_LEAVES)
                    .dynamicShape().noOcclusion().isSuffocating(BlocksNF::never).isViewBlocking(BlocksNF::never))));

    public static final Map<Tree, RegistryObject<TreeLeavesBlock>> FRUIT_LEAVES = DataUtil.mapEnum(Tree.class, tree -> tree != Tree.JUNGLE && tree != Tree.OAK && tree != Tree.PALM, tree ->
            register(tree.getName() + "_fruit_leaves", () -> new TreeLeavesBlock(tree, BlockBehaviour.Properties.of(Material.LEAVES)
                    .strength(1F).speedFactor(0.6F).jumpFactor(0.9F).sound(SoundType.AZALEA_LEAVES)
                    .dynamicShape().noOcclusion().isSuffocating(BlocksNF::never).isViewBlocking(BlocksNF::never))));

    public static final Map<Tree, RegistryObject<TreeBranchesBlock>> BRANCHES = DataUtil.mapEnum(Tree.class, tree -> !tree.isDeciduous(), tree ->
            register(tree.getName() + "_branches", () -> new TreeBranchesBlock(tree, BlockBehaviour.Properties.of(Material.LEAVES)
                    .strength(1F).speedFactor(0.6F).jumpFactor(0.9F).sound(SoundType.AZALEA_LEAVES)
                    .dynamicShape().noOcclusion().isSuffocating(BlocksNF::never).isViewBlocking(BlocksNF::never))));

    public static final Map<Tree, RegistryObject<TreeTrunkBlock>> TRUNKS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_trunk", () -> new TreeTrunkBlock(STEMS.get(tree).get(), LEAVES.get(tree).get(),
                    BRANCHES.containsKey(tree) ? BRANCHES.get(tree).get() : null, FRUIT_LEAVES.containsKey(tree) ? FRUIT_LEAVES.get(tree).get() : null,
                    tree.getGenerator(), BlockBehaviour.Properties.of(Material.WOOD, tree.getBaseColor()).strength(tree.getStrength(),
                    tree.getExplosionResistance()).sound(SoundType.WOOD).randomTicks())));

    public static final Map<Tree, RegistryObject<TreeSeedBlock>> TREE_SEEDS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_seed", () -> (tree != Tree.CAEDTAR ? new TreeSeedBlock(TRUNKS.get(tree).get(), BlockBehaviour.Properties.of(Material.PLANT)
                    .strength(0.4F).noCollission().randomTicks().sound(SoundType.AZALEA_LEAVES)) :
                    new TreeAquaticSeedBlock(TRUNKS.get(tree).get(), BlockBehaviour.Properties.of(Material.PLANT)
                            .strength(0.4F).noCollission().randomTicks().sound(SoundType.AZALEA_LEAVES)))));
    //Building
    public static final Map<Tree, RegistryObject<Block>> PLANK_BLOCKS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_planks", () -> new BlockNF(BlockBehaviour.Properties.of(Material.WOOD, tree.getBaseColor())
            .strength(tree.getStrength() * 0.8F, tree.getExplosionResistance() * 0.8F).sound(SoundType.WOOD)) {
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }
            }));

    public static final Map<Tree, RegistryObject<StairBlockNF>> PLANK_STAIRS = DataUtil.mapEnum(Tree.class, tree ->
            stairs(tree.getName(), PLANK_BLOCKS.get(tree)));

    public static final Map<Tree, RegistryObject<SlabBlockNF>> PLANK_SLABS = DataUtil.mapEnum(Tree.class, tree ->
            slab(tree.getName(), PLANK_BLOCKS.get(tree)));

    public static final Map<Tree, RegistryObject<SidingBlock>> PLANK_SIDINGS = DataUtil.mapEnum(Tree.class, tree ->
            siding(tree.getName(), PLANK_BLOCKS.get(tree)));

    public static final Map<Tree, RegistryObject<FenceBlockNF>> PLANK_FENCES = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_fence", () -> new FenceBlockNF(BlockBehaviour.Properties.copy(PLANK_BLOCKS.get(tree).get())) {
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }
            }));

    public static final Map<Tree, RegistryObject<FenceGateBlockNF>> PLANK_FENCE_GATES = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_fence_gate", () -> new FenceGateBlockNF(BlockBehaviour.Properties.copy(PLANK_BLOCKS.get(tree).get())) {
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }
            }));

    public static final Map<Tree, RegistryObject<DoorBlockNF>> PLANK_DOORS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_door", () -> new DoorBlockNF(BlockBehaviour.Properties.copy(PLANK_BLOCKS.get(tree).get()).noOcclusion(),
                    () -> SoundEvents.WOODEN_DOOR_OPEN, () -> SoundEvents.WOODEN_DOOR_CLOSE) {
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }
            }));

    public static final Map<Tree, RegistryObject<TrapdoorBlockNF>> PLANK_TRAPDOORS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_trapdoor", () -> new TrapdoorBlockNF(BlockBehaviour.Properties.copy(PLANK_BLOCKS.get(tree).get()).noOcclusion(),
                    () -> SoundEvents.WOODEN_TRAPDOOR_OPEN, () -> SoundEvents.WOODEN_TRAPDOOR_CLOSE) {
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }
            }));

    public static final Map<Tree, RegistryObject<HatchBlock>> PLANK_HATCHES = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_hatch", () -> new HatchBlock(BlockBehaviour.Properties.copy(PLANK_BLOCKS.get(tree).get()).noOcclusion(),
                    () -> SoundEvents.WOODEN_TRAPDOOR_OPEN, () -> SoundEvents.WOODEN_TRAPDOOR_CLOSE) {
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }
            }));

    public static final Map<Tree, RegistryObject<LadderBlockNF>> PLANK_LADDERS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_ladder", () -> new LadderBlockNF(BlockBehaviour.Properties.of(SOLID_FLAMMABLE_DECORATION).strength(0.4F).sound(SoundType.LADDER).noOcclusion()) {
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }
            }));

    public static final Map<Stone, RegistryObject<Block>> TILED_STONE = DataUtil.mapEnum(Stone.class, stone ->
            register("tiled_" + stone.getName(), () -> new BlockNF(BlockBehaviour.Properties.of(Material.STONE, stone.getBaseColor())
                    .strength(stone.getStrength(), stone.getExplosionResistance()).sound(stone.getSound()))));
    public static final Map<Stone, RegistryObject<Block>> POLISHED_STONE = DataUtil.mapEnum(Stone.class, stone ->
            register("polished_" + stone.getName(), () -> new BlockNF(BlockBehaviour.Properties.of(Material.STONE, stone.getBaseColor())
                    .strength(stone.getStrength(), stone.getExplosionResistance()).sound(stone.getSound()))));
    public static final Map<Stone, RegistryObject<StairBlockNF>> POLISHED_STONE_STAIRS = DataUtil.mapEnum(Stone.class, stone ->
            stairs("polished_" + stone.getName(), POLISHED_STONE.get(stone)));
    public static final Map<Stone, RegistryObject<SlabBlockNF>> POLISHED_STONE_SLABS = DataUtil.mapEnum(Stone.class, stone ->
            slab("polished_" + stone.getName(), POLISHED_STONE.get(stone)));
    public static final Map<Stone, RegistryObject<SidingBlock>> POLISHED_STONE_SIDINGS = DataUtil.mapEnum(Stone.class, stone ->
            siding("polished_" + stone.getName(), POLISHED_STONE.get(stone)));

    public static final Map<Stone, RegistryObject<Block>> STACKED_STONE = DataUtil.mapEnum(Stone.class, stone ->
            register("stacked_" + stone.getName(), () -> new BlockNF(BlockBehaviour.Properties.of(Material.STONE, stone.getBaseColor())
                    .strength(stone.getStrength() * 0.8F, stone.getExplosionResistance() * 0.8F).sound(stone.getSound()))));
    public static final Map<Stone, RegistryObject<StairBlockNF>> STACKED_STONE_STAIRS = DataUtil.mapEnum(Stone.class, stone ->
            stairs("stacked_" + stone.getName(), STACKED_STONE.get(stone)));
    public static final Map<Stone, RegistryObject<SlabBlockNF>> STACKED_STONE_SLABS = DataUtil.mapEnum(Stone.class, stone ->
            slab("stacked_" + stone.getName(), STACKED_STONE.get(stone)));
    public static final Map<Stone, RegistryObject<SidingBlock>> STACKED_STONE_SIDINGS = DataUtil.mapEnum(Stone.class, stone ->
            siding("stacked_" + stone.getName(), STACKED_STONE.get(stone)));

    public static final Map<Stone, RegistryObject<Block>> COBBLED_STONE = DataUtil.mapEnum(Stone.class, stone ->
            register("cobbled_" + stone.getName(), () -> new BlockNF(BlockBehaviour.Properties.of(Material.STONE, stone.getBaseColor())
                    .strength(stone.getStrength() * 0.8F, stone.getExplosionResistance() * 0.8F).sound(stone.getSound()))));
    public static final Map<Stone, RegistryObject<StairBlockNF>> COBBLED_STONE_STAIRS = DataUtil.mapEnum(Stone.class, stone ->
            stairs("cobbled_" + stone.getName(), COBBLED_STONE.get(stone)));
    public static final Map<Stone, RegistryObject<SlabBlockNF>> COBBLED_STONE_SLABS = DataUtil.mapEnum(Stone.class, stone ->
            slab("cobbled_" + stone.getName(), COBBLED_STONE.get(stone)));
    public static final Map<Stone, RegistryObject<SidingBlock>> COBBLED_STONE_SIDINGS = DataUtil.mapEnum(Stone.class, stone ->
            siding("cobbled_" + stone.getName(), COBBLED_STONE.get(stone)));

    public static final Map<Stone, RegistryObject<Block>> STONE_BRICK_BLOCKS = DataUtil.mapEnum(Stone.class, stone ->
            register(stone.getName() + "_bricks", () -> new BlockNF(BlockBehaviour.Properties.of(Material.STONE, stone.getBaseColor())
                    .strength(stone.getStrength() * 1.5F, stone.getExplosionResistance() * 1.5F).sound(stone.getSound()))));
    public static final Map<Stone, RegistryObject<StairBlockNF>> STONE_BRICK_STAIRS = DataUtil.mapEnum(Stone.class, stone ->
            stairs(stone.getName() + "_brick", STONE_BRICK_BLOCKS.get(stone)));
    public static final Map<Stone, RegistryObject<SlabBlockNF>> STONE_BRICK_SLABS = DataUtil.mapEnum(Stone.class, stone ->
            slab(stone.getName() + "_brick", STONE_BRICK_BLOCKS.get(stone)));
    public static final Map<Stone, RegistryObject<SidingBlock>> STONE_BRICK_SIDINGS = DataUtil.mapEnum(Stone.class, stone ->
            siding(stone.getName() + "_brick", STONE_BRICK_BLOCKS.get(stone)));

    public static final RegistryObject<Block> TERRACOTTA = BLOCKS.register("terracotta", () -> new BlockNF(BlockBehaviour.Properties.of(Material.STONE,
            MaterialColor.TERRACOTTA_ORANGE).strength(2F, 3.0F).sound(SoundsNF.TERRACOTTA_TYPE)));

    public static final RegistryObject<Block> TERRACOTTA_TILES = BLOCKS.register("terracotta_tiles", () -> new BlockNF(BlockBehaviour.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_ORANGE).strength(1.6F, 2.4F).sound(SoundsNF.CERAMIC_TYPE)));
    public static final RegistryObject<StairBlockNF> TERRACOTTA_TILE_STAIRS = stairs("terracotta_tile", TERRACOTTA_TILES);
    public static final RegistryObject<SlabBlockNF> TERRACOTTA_TILE_SLAB = slab("terracotta_tile", TERRACOTTA_TILES);
    public static final RegistryObject<SidingBlock> TERRACOTTA_TILE_SIDING = siding("terracotta_tile", TERRACOTTA_TILES);

    public static final RegistryObject<Block> TERRACOTTA_MOSAIC = BLOCKS.register("terracotta_mosaic", () -> new BlockNF(BlockBehaviour.Properties.of(
            Material.STONE, MaterialColor.TERRACOTTA_ORANGE).strength(1.6F, 2.4F).sound(SoundsNF.CERAMIC_TYPE)));
    public static final RegistryObject<StairBlockNF> TERRACOTTA_MOSAIC_STAIRS = stairs(TERRACOTTA_MOSAIC);
    public static final RegistryObject<SlabBlockNF> TERRACOTTA_MOSAIC_SLAB = slab(TERRACOTTA_MOSAIC);
    public static final RegistryObject<SidingBlock> TERRACOTTA_MOSAIC_SIDING = siding(TERRACOTTA_MOSAIC);

    public static final RegistryObject<Block> MUD_BRICKS = BLOCKS.register("mud_bricks", () -> new BlockNF(BlockBehaviour.Properties.of(Material.DIRT)
            .strength(5.0F).sound(SoundType.DRIPSTONE_BLOCK)));
    public static final RegistryObject<StairBlockNF> MUD_BRICK_STAIRS = stairs("mud_brick", MUD_BRICKS);
    public static final RegistryObject<SlabBlockNF> MUD_BRICK_SLAB = slab("mud_brick", MUD_BRICKS);
    public static final RegistryObject<SidingBlock> MUD_BRICK_SIDING = siding("mud_brick", MUD_BRICKS);

    public static final RegistryObject<Block> BRICKS = BLOCKS.register("bricks", () -> new BlockNF(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_RED)
            .strength(8.0F).sound(SoundType.STONE)));
    public static final RegistryObject<StairBlockNF> BRICK_STAIRS = stairs("brick", BRICKS);
    public static final RegistryObject<SlabBlockNF> BRICK_SLAB = slab("brick", BRICKS);
    public static final RegistryObject<SidingBlock> BRICK_SIDING = siding("brick", BRICKS);

    public static final RegistryObject<Block> FIRE_BRICKS = BLOCKS.register("fire_bricks", () -> new BlockNF(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.CRIMSON_HYPHAE)
            .strength(10.0F).sound(SoundType.STONE)));
    public static final RegistryObject<StairBlockNF> FIRE_BRICK_STAIRS = stairs("fire_brick", FIRE_BRICKS);
    public static final RegistryObject<SlabBlockNF> FIRE_BRICK_SLAB = slab("fire_brick", FIRE_BRICKS);
    public static final RegistryObject<SidingBlock> FIRE_BRICK_SIDING = siding("fire_brick", FIRE_BRICKS);

    public static final RegistryObject<SoftFallBlock> THATCH = BLOCKS.register("thatch", () -> new SoftFallBlock(3, 1F,
            BlockBehaviour.Properties.of(Material.GRASS, MaterialColor.GOLD).strength(2.0F).speedFactor(0.8F).sound(SoundType.GRASS)) {
        @Override
        public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return 30;
        }

        @Override
        public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return 60;
        }
    });
    public static final RegistryObject<StairBlockNF> THATCH_STAIRS = stairs(THATCH);
    public static final RegistryObject<SlabBlockNF> THATCH_SLAB = slab(THATCH);
    public static final RegistryObject<SidingBlock> THATCH_SIDING = siding(THATCH);

    /*public static final RegistryObject<Block> PLASTER = BLOCKS.register("plaster", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.QUARTZ)
            .strength(4.0F).sound(SoundType.STONE)));
    public static final RegistryObject<StairBlockNF> PLASTER_STAIRS = stairs(PLASTER);
    public static final RegistryObject<SlabBlockNF> PLASTER_SLAB = slab(PLASTER);
    public static final RegistryObject<SidingBlock> PLASTER_SIDING = siding(PLASTER);*/

    public static final RegistryObject<GlassBlockNF> GLASS_BLOCK = BLOCKS.register("glass_block", () -> new GlassBlockNF(BlockBehaviour.Properties.of(Material.GLASS)
            .strength(1.5F, 1F).noDrops().noOcclusion().isViewBlocking(BlocksNF::never).sound(SoundType.GLASS)));
    public static final RegistryObject<SlabBlockNF> GLASS_SLAB = slab("glass", GLASS_BLOCK);
    public static final RegistryObject<SidingBlock> GLASS_SIDING = siding("glass", GLASS_BLOCK);

    //Survival
    public static final RegistryObject<DryingUnstableBlock> WET_MUD_BRICKS = BLOCKS.register("wet_mud_bricks", () -> new DryingUnstableBlock(MUD_BRICKS,
            (int) ContinentalWorldType.DAY_LENGTH, 10, SoundsNF.WET_SOIL_FALL, fullCopy(MUD.get()).speedFactor(1).jumpFactor(1)));
    public static final RegistryObject<FireableBlock> CLAY_BRICKS = BLOCKS.register("clay_bricks", () -> new SimpleFireableBlock(
            20 * 60 * 8, TieredHeat.ORANGE, BRICKS, fullCopy(CLAY.get())));
    public static final RegistryObject<FireableBlock> FIRE_CLAY_BRICKS = BLOCKS.register("fire_clay_bricks", () -> new SimpleFireableBlock(
            20 * 60 * 8, TieredHeat.YELLOW, FIRE_BRICKS, fullCopy(FIRE_CLAY.get())));
    public static final RegistryObject<TorchBlockNF> TORCH = BLOCKS.register("torch", () -> new TorchBlockNF(true, BlocksNF.TORCH_UNLIT,
            BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak().randomTicks().lightLevel(state -> 15).sound(SoundType.WOOD)));
    public static final RegistryObject<TorchBlockNF> TORCH_UNLIT = BLOCKS.register("torch_unlit", () -> new TorchBlockNF(false, TORCH,
            BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak().sound(SoundType.WOOD)));
    public static final RegistryObject<WallTorchBlockNF> WALL_TORCH = BLOCKS.register("wall_torch", () -> new WallTorchBlockNF(true, BlocksNF.WALL_TORCH_UNLIT,
            BlockBehaviour.Properties.of(Material.DECORATION).noCollission().lootFrom(TORCH).instabreak().randomTicks().lightLevel(state -> 15).sound(SoundType.WOOD)));
    public static final RegistryObject<WallTorchBlockNF> WALL_TORCH_UNLIT = BLOCKS.register("wall_torch_unlit", () -> new WallTorchBlockNF(false, WALL_TORCH,
            BlockBehaviour.Properties.of(Material.DECORATION).noCollission().lootFrom(TORCH_UNLIT).instabreak().sound(SoundType.WOOD)));
    public static final RegistryObject<RopeBlock> ROPE = BLOCKS.register("rope", () -> new RopeBlock(BlockBehaviour.Properties.of(Material.DECORATION)
            .strength(0.2F).noCollission().sound(SoundType.WOOL)));
    public static final RegistryObject<BowlBlock> WOODEN_BOWL = BLOCKS.register("wooden_bowl", () -> new BowlBlock(BlockBehaviour.Properties.of(Material.DECORATION)
            .strength(0.2F).noCollission().noOcclusion().sound(SoundType.WOOD)));
    public static final RegistryObject<CampfireBlockNF> CAMPFIRE = BLOCKS.register("campfire", () -> new CampfireBlockNF(BlockBehaviour.Properties.of(
            Material.WOOD, MaterialColor.PODZOL).instabreak().lightLevel(litBlockEmission(15)).noOcclusion().dynamicShape().sound(SoundsNF.FIREWOOD_TYPE)));

    public static final RegistryObject<CauldronBlockNF> CAULDRON = BLOCKS.register("cauldron", () -> new CauldronBlockNF(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BROWN).strength(2.0F).sound(SoundsNF.CERAMIC_VESSEL_TYPE)));
    public static final RegistryObject<FireableAxisPartialBlock> UNFIRED_CAULDRON = BLOCKS.register("unfired_cauldron", () -> new FireableAxisPartialBlock(
            20 * 60 * 8, TieredHeat.ORANGE, CAULDRON, 0,
            BlockBehaviour.Properties.of(Material.CLAY).strength(1F, 1F).sound(SoundType.GRAVEL)));
    public static final RegistryObject<PotBlock> POT = BLOCKS.register("pot", () -> new PotBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BROWN).strength(2.5F).sound(SoundsNF.CERAMIC_VESSEL_TYPE)));
    public static final RegistryObject<FireablePartialBlock> UNFIRED_POT = BLOCKS.register("unfired_pot", () -> new FireablePartialBlock(
            20 * 60 * 8, TieredHeat.ORANGE, POT, 0,
            BlockBehaviour.Properties.of(Material.CLAY).strength(1F, 1F).sound(SoundType.GRAVEL)));

    public static final RegistryObject<WardingEffigyBlock> WARDING_EFFIGY = BLOCKS.register("warding_effigy", () -> new WardingEffigyBlock(
            BlockBehaviour.Properties.of(SOLID_DECORATION).strength(1.0F).sound(SoundType.NETHERRACK)));

    public static final Map<Tree, RegistryObject<BarrelBlockNF>> BARRELS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_barrel", () -> new BarrelBlockNF(BlockBehaviour.Properties.of(Material.WOOD, tree.getBaseColor())
                    .strength(tree.getStrength() * 2F, tree.getExplosionResistance() * 2F).sound(SoundType.WOOD))));

    public static final Map<Tree, RegistryObject<ChestBlockNF>> CHESTS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_chest", () -> new ChestBlockNF(BlockBehaviour.Properties.of(Material.WOOD, tree.getBaseColor())
                    .strength(tree.getStrength(), tree.getExplosionResistance()).sound(SoundType.WOOD), tree)));

    public static final Map<Tree, RegistryObject<RackBlock>> RACKS = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_rack", () -> new RackBlock(BlockBehaviour.Properties.of(SOLID_FLAMMABLE_DECORATION).noOcclusion().noCollission()
                    .strength(tree.getStrength() * 0.5F, tree.getExplosionResistance() * 0.5F).sound(SoundType.WOOD))));

    public static final Map<Tree, RegistryObject<ShelfBlock>> SHELVES = DataUtil.mapEnum(Tree.class, tree ->
            register(tree.getName() + "_shelf", () -> new ShelfBlock(BlockBehaviour.Properties.of(SOLID_FLAMMABLE_DECORATION)
                    .strength(tree.getStrength() * 0.5F, tree.getExplosionResistance() * 0.5F).sound(SoundType.WOOD))));

    //Metallurgy
    public static final Map<Metal, RegistryObject<Block>> METAL_BLOCKS = DataUtil.mapEnum(Metal.class, metal ->
            register(metal.getName() + "_block", () -> new Block(BlockBehaviour.Properties.of(Material.METAL, metal.getBaseColor())
                    .strength(metal.getStrength()/2F, metal.getExplosionResistance()/2F).sound(metal.getSound()))));
    public static final Map<Metal, RegistryObject<Block>> INGOT_PILES = DataUtil.mapEnum(Metal.class, metal ->
            register(metal.getName() + "_ingot_pile", metal == Metal.IRON ? () -> new FireableAxisBlock(
                    20 * 60 * 8, TieredHeat.WHITE,
                    BlockBehaviour.Properties.of(Material.METAL, metal.getBaseColor())
                    .strength(metal.getStrength()/8F, metal.getExplosionResistance()/2F).sound(metal.getSound())) {
                @Override
                public BlockState getFiredBlock(Level level, BlockPos pos, BlockState state) {
                    BlockState above = level.getBlockState(pos.above());
                    BlockState below = level.getBlockState(pos.below());
                    if(above.is(COAL_BURNING.get()) || below.is(COAL_BURNING.get())) return STEEL_INGOT_PILE_POOR.get().defaultBlockState();
                    else if(above.is(CHARCOAL_BURNING.get()) || below.is(CHARCOAL_BURNING.get())) return STEEL_INGOT_PILE_FAIR.get().defaultBlockState();
                    else return state.getBlock().defaultBlockState();
                }

                @Override
                public boolean isStructureValid(Level level, BlockPos pos, BlockState state) {
                    return LevelUtil.isBlockBurningCarbon(level.getBlockState(pos.below())) && LevelUtil.isBlockBurningCarbon(level.getBlockState(pos.above()))
                            && LevelUtil.getNearbySmelterTier(level, pos) >= 3;
                }
            } :
                    () -> new HorizontalAxisBlock(BlockBehaviour.Properties.of(Material.METAL, metal.getBaseColor())
                            .strength(metal.getStrength()/8F, metal.getExplosionResistance()/2F).sound(metal.getSound()))));
    public static final RegistryObject<HorizontalAxisBlock> STEEL_INGOT_PILE_POOR = register("poor_steel_ingot_pile", () -> new HorizontalAxisBlock(
            BlockBehaviour.Properties.of(Material.METAL, Metal.STEEL.getBaseColor()).strength(Metal.STEEL.getStrength()/8F,
                    Metal.STEEL.getExplosionResistance()/2F).sound(Metal.STEEL.getSound())));
    public static final RegistryObject<HorizontalAxisBlock> STEEL_INGOT_PILE_FAIR = register("fair_steel_ingot_pile", () -> new HorizontalAxisBlock(
            BlockBehaviour.Properties.of(Material.METAL, Metal.STEEL.getBaseColor()).strength(Metal.STEEL.getStrength()/8F,
                    Metal.STEEL.getExplosionResistance()/2F).sound(Metal.STEEL.getSound())));
    public static final Map<Metal, RegistryObject<LanternBlockNF>> LANTERNS = DataUtil.mapEnum(Metal.class,
            metal -> register(metal.getName() + "_lantern", () -> new LanternBlockNF(true, BlocksNF.LANTERNS_UNLIT.get(metal), BlockBehaviour.Properties.of(Material.METAL,
                    metal.getBaseColor()).strength(1F).lightLevel((state) -> 15).noOcclusion().sound(SoundType.LANTERN))));
    public static final Map<Metal, RegistryObject<LanternBlockNF>> LANTERNS_UNLIT = DataUtil.mapEnum(Metal.class,
            metal -> register("extinguished_" + metal.getName() + "_lantern", () -> new LanternBlockNF(false, LANTERNS.get(metal),
                    BlockBehaviour.Properties.of(Material.METAL, metal.getBaseColor()).strength(1F).noOcclusion().sound(SoundType.LANTERN))));

    public static final Map<Tree, RegistryObject<TieredAnvilBlock>> ANVILS_LOG = DataUtil.mapEnum(Tree.class, tree -> tree.getHardness() < 2F,
            tree -> register(tree.getName() + "_anvil", () -> new TieredAnvilBlock(1,
                    BlockBehaviour.Properties.of(Material.WOOD, tree.getBaseColor())
                            .strength(tree.getStrength(), tree.getExplosionResistance()).sound(tree.getSound()))));
    public static final Map<Stone, RegistryObject<TieredAnvilBlock>> ANVILS_STONE = DataUtil.mapEnum(Stone.class, stone -> stone.getExplosionResistance() < 12.5F,
            stone -> register(stone.getName() + "_anvil", () -> new TieredAnvilBlock(1,
                    BlockBehaviour.Properties.of(Material.WOOD, stone.getBaseColor())
                            .strength(stone.getStrength(), stone.getExplosionResistance()).sound(stone.getSound()))));
    public static final Map<Metal, RegistryObject<MetalAnvilBlock>> ANVILS_METAL = DataUtil.mapEnum(Metal.class, metal -> metal.getCategory() != IMetal.Category.HARD,
            metal -> register(metal.getName() + "_anvil", () -> new MetalAnvilBlock(metal.getTier() + 1,
                    BlockBehaviour.Properties.of(Material.HEAVY_METAL, metal.getBaseColor()).requiresCorrectToolForDrops()
                            .strength(metal.getStrength(), metal.getExplosionResistance()).sound(SoundType.NETHERITE_BLOCK))));

    public static final Map<Armament, RegistryObject<ItemMoldBlock>> ARMAMENT_MOLDS = DataUtil.mapEnum(Armament.class,
            a -> a == Armament.ADZE || a == Armament.HAMMER || a == Armament.MACE || a == Armament.SABRE || a == Armament.SWORD,
            armament -> register(armament.getName() + "_mold", () -> new ItemMoldBlock(switch(armament) {
                case AXE -> Block.box(3.5, 0, 4, 12.5, 2, 12);
                case CHISEL -> Block.box(1, 0, 5, 15, 2, 11);
                case DAGGER -> Block.box(1.5, 0, 5, 14.5, 2, 11);
                case PICKAXE -> Block.box(0, 0, 4.5, 16, 2, 11.5);
                case SHOVEL -> Block.box(3.5, 0, 4, 12.5, 2, 12);
                case SICKLE -> Block.box(0, 0, 3.5, 16, 2, 12.5);
                case SPEAR -> Block.box(3, 0, 5, 13, 2, 11);
                default -> Block.box(0, 0, 0, 16, 2, 16);
            }, switch(armament) {
                case AXE -> TagsNF.AXE_HEAD;
                case CHISEL -> TagsNF.CHISEL_HEAD;
                case DAGGER -> TagsNF.DAGGER_HEAD;
                case PICKAXE -> TagsNF.PICKAXE_HEAD;
                case SHOVEL -> TagsNF.SHOVEL_HEAD;
                case SICKLE -> TagsNF.SICKLE_HEAD;
                case SPEAR -> TagsNF.SPEAR_HEAD;
                default -> throw new IllegalArgumentException("Missing matching tag for armament mold.");
            }, 100, BlockBehaviour.Properties.of(Material.DECORATION).strength(0.3F).sound(SoundsNF.CERAMIC_SMALL_TYPE).noCollission())));
    public static final RegistryObject<ItemMoldBlock> INGOT_MOLD = register("ingot_mold", () -> new ItemMoldBlock(
            Block.box(3, 0, 4.5, 13, 3, 11.5), Tags.Items.INGOTS, 100,
            BlockBehaviour.Properties.of(Material.DECORATION).strength(0.3F).sound(SoundsNF.CERAMIC_SMALL_TYPE).noCollission()));
    public static final RegistryObject<ItemMoldBlock> ARROWHEAD_MOLD = register("arrowhead_mold", () -> new ItemMoldBlock(
            Block.box(4, 0, 4, 12, 2, 12), TagsNF.ARROWHEAD, 10,
            BlockBehaviour.Properties.of(Material.DECORATION).strength(0.3F).sound(SoundsNF.CERAMIC_SMALL_TYPE).noCollission()));
    public static final Map<Armament, RegistryObject<FireableFacingPartialBlock>> UNFIRED_ARMAMENT_MOLDS = DataUtil.mapEnum(Armament.class,
            armament -> !ARMAMENT_MOLDS.containsKey(armament),
            armament -> register("unfired_" + armament.getName() + "_mold", () -> new FireableFacingPartialBlock(
                    20 * 60 * 8, TieredHeat.ORANGE, ARMAMENT_MOLDS.get(armament), 0,
                    BlockBehaviour.Properties.of(Material.DECORATION).strength(0.3F).sound(SoundType.GRAVEL).noCollission())));
    public static final RegistryObject<FireableFacingPartialBlock> UNFIRED_INGOT_MOLD = register("unfired_ingot_mold", () -> new FireableFacingPartialBlock(
            20 * 60 * 8, TieredHeat.ORANGE, INGOT_MOLD, 0,
            BlockBehaviour.Properties.of(Material.DECORATION).strength(0.3F).sound(SoundType.GRAVEL).noCollission()));
    public static final RegistryObject<FireableFacingPartialBlock> UNFIRED_ARROWHEAD_MOLD = register("unfired_arrowhead_mold", () -> new FireableFacingPartialBlock(
            20 * 60 * 8, TieredHeat.ORANGE, ARROWHEAD_MOLD, 0,
            BlockBehaviour.Properties.of(Material.DECORATION).strength(0.3F).sound(SoundType.GRAVEL).noCollission()));

    public static final RegistryObject<FuelBlock> COKE = BLOCKS.register("coke_block", () -> new FuelBlock(BlocksNF.COKE_BURNING, BlockBehaviour.Properties.of(
            Material.STONE, MaterialColor.COLOR_GRAY).strength(2.4F, 2F).sound(SoundType.NETHER_BRICKS)));
    public static final RegistryObject<BurningFuelBlock> COKE_BURNING = BLOCKS.register("burning_coke_block", () -> new BurningFuelBlock(
            20 * 60 * 9, 1200F, COKE, BlockBehaviour.Properties.copy(COKE.get()).lightLevel(state -> 15)));
    public static final RegistryObject<FuelBlock> COAL = BLOCKS.register("coal_block", () -> new FuelBlock(BlocksNF.COAL_BURNING, BlockBehaviour.Properties.of(
            Material.STONE, MaterialColor.COLOR_BLACK).strength(2.4F, 2F).sound(SoundType.NETHER_BRICKS)));
    public static final RegistryObject<BurningFuelBlock> COAL_BURNING = BLOCKS.register("burning_coal_block", () -> new BurningFuelBlock(
            20 * 60 * 10, 1000F, (level, blockPos) -> LevelUtil.isBlockSmothered(level, blockPos) ? COKE.get() : null, COAL, BlockBehaviour.Properties
            .copy(COAL.get()).lightLevel(state -> 15)));
    public static final RegistryObject<HorizontalFuelBlock> CHARCOAL = BLOCKS.register("charcoal_block", () -> new HorizontalFuelBlock(BlocksNF.CHARCOAL_BURNING,
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(2F, 2F).sound(SoundType.NETHER_BRICKS)));
    public static final RegistryObject<BurningHorizontalFuelBlock> CHARCOAL_BURNING = BLOCKS.register("burning_charcoal_block", () -> new BurningHorizontalFuelBlock(
            20 * 60 * 10, 1000F, CHARCOAL, BlockBehaviour.Properties.copy(CHARCOAL.get()).lightLevel(state -> 15)));
    public static final RegistryObject<HorizontalFuelBlock> FIREWOOD = BLOCKS.register("firewood_block", () -> new HorizontalFuelBlock(BlocksNF.FIREWOOD_BURNING,
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).instabreak().sound(SoundsNF.FIREWOOD_TYPE)));
    public static final RegistryObject<BurningHorizontalFuelBlock> FIREWOOD_BURNING = BLOCKS.register("burning_firewood_block", () -> new BurningHorizontalFuelBlock(
            20 * 60 * 10, 800F, (level, blockPos) -> LevelUtil.isBlockSmothered(level, blockPos) ? CHARCOAL.get() : null, FIREWOOD,
            BlockBehaviour.Properties.copy(FIREWOOD.get()).lightLevel(state -> 15)));

    public static final RegistryObject<BlockNF> SLAG = BLOCKS.register("slag_block", () -> new BlockNF(BlockBehaviour.Properties.of(Material.STONE,
            MaterialColor.TERRACOTTA_BLACK).strength(12.0F, 20.0F).sound(SoundType.ANCIENT_DEBRIS)));
    public static final RegistryObject<FireableBlock> AZURITE = BLOCKS.register("azurite_block", () -> new SimpleFireableBlock(20 * 60 * 8, TieredHeat.ORANGE,
            BlocksNF.SMELTED_AZURITE, BlockBehaviour.Properties.of(Material.METAL, MaterialColor.LAPIS).strength(8.0F, 2000F).sound(SoundType.METAL)));
    public static final RegistryObject<FireableBlock> HEMATITE = BLOCKS.register("hematite_block", () -> new SimpleFireableBlock(20 * 60 * 8, TieredHeat.WHITE,
            BlocksNF.SMELTED_HEMATITE, BlockBehaviour.Properties.of(Material.METAL).strength(8.0F, 2000F).sound(SoundType.METAL)));
    public static final RegistryObject<BlockNF> SMELTED_AZURITE = BLOCKS.register("smelted_azurite", () -> new BlockNF(BlockBehaviour.Properties.of(Material.STONE,
            MaterialColor.TERRACOTTA_BLACK).strength(8.0F, 16.0F).sound(SoundType.ANCIENT_DEBRIS)));
    public static final RegistryObject<BlockNF> SMELTED_HEMATITE = BLOCKS.register("smelted_hematite", () -> new BlockNF(BlockBehaviour.Properties.of(Material.STONE,
            MaterialColor.TERRACOTTA_BLACK).strength(8.0F, 16.0F).sound(SoundType.ANCIENT_DEBRIS)
            .emissiveRendering(BlocksNF::always).lightLevel((state) -> 3)));

    public static final RegistryObject<CrucibleBlock> CRUCIBLE = BLOCKS.register("crucible", () -> new CrucibleBlock(200, TieredHeat.WHITE.getBaseTemp() - 0.1F,
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BROWN).strength(1.0F, 1F)
                    .sound(SoundsNF.CERAMIC_VESSEL_TYPE).emissiveRendering((state, level, pos) -> state.getValue(CrucibleBlock.HEAT) != 0)
                    .lightLevel((state) -> state.getValue(CrucibleBlock.HEAT) != 0 ? 2 : 0)));
    public static final RegistryObject<FireableAxisPartialBlock> UNFIRED_CRUCIBLE = BLOCKS.register("unfired_crucible", () -> new FireableAxisPartialBlock(
            20 * 60 * 8, TieredHeat.ORANGE, CRUCIBLE, 0,
            BlockBehaviour.Properties.of(Material.CLAY).strength(1.0F, 20F).sound(SoundType.GRAVEL)));

    //Decoration

    //Special
    public static final RegistryObject<MoonEssenceBlock> MOON_ESSENCE = BLOCKS.register("moon_essence", () -> new MoonEssenceBlock(
            ParticleTypesNF.ESSENCE_MOON, BlockBehaviour.Properties.of(SOLID_DECORATION, MaterialColor.DIAMOND).sound(SoundsNF.MOON_ESSENCE_TYPE)
            .isViewBlocking(BlocksNF::never).isSuffocating(BlocksNF::never).emissiveRendering(BlocksNF::always).strength(1F).noDrops().noOcclusion().randomTicks()));
    public static final RegistryObject<RabbitBurrowBlock> RABBIT_BURROW = BLOCKS.register("rabbit_burrow", () -> new RabbitBurrowBlock(
            BlockBehaviour.Properties.of(Material.DECORATION).noOcclusion().noCollission().randomTicks().noDrops().sound(SoundsNF.SILENT_TYPE)));
    public static final RegistryObject<SpiderWebBlock> SPIDER_WEB = BLOCKS.register("spider_web", () -> new SpiderWebBlock(
            BlockBehaviour.Properties.of(FLAMMABLE_DECORATION).strength(1F).noCollission().noOcclusion().speedFactor(0.5F).jumpFactor(0.6F).sound(SoundType.WOOL)));
    public static final RegistryObject<SpiderNestBlock> SPIDER_NEST = BLOCKS.register("spider_nest", () -> new SpiderNestBlock(
            BlockBehaviour.Properties.of(Material.WOOL).randomTicks().strength(8F).speedFactor(0.5F).jumpFactor(0.6F).sound(SoundType.WOOL)));

    public static void register() {
        BLOCKS.register(Nightfall.MOD_EVENT_BUS);
    }

    public static Set<RegistryObject<? extends CoveredSoilBlock>> getCoveredSoils() {
        return Stream.of(COVERED_SILT.values(), COVERED_DIRT.values(), COVERED_LOAM.values()).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public static Set<RegistryObject<? extends Block>> getOres() {
        return Stream.of(TIN_ORES.values(), COPPER_ORES.values(), AZURITE_ORES.values(), HEMATITE_ORES.values(), COAL_ORES.values(), List.of(METEORITE_ORE))
                .flatMap(Collection::stream).collect(Collectors.toSet());
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static RegistryObject<UnstableBlock> soil(ISoil soil) {
        return BLOCKS.register(soil.getName() + "_block", () -> new UnstableBlock(soil.getSlideSound(), BlockBehaviour.Properties.of(Material.DIRT,
                soil.getBaseColor()).strength(soil.getStrength(), soil.getExplosionResistance()).sound(soil.getSound())));
    }

    private static RegistryObject<TilledSoilBlock> tilledSoil(ISoil soil, float dryHumidity, float moistHumidity, float irrigatedHumidity, RegistryObject<? extends Block> untilledBlock) {
        return BLOCKS.register("tilled_" + soil.getName(), () -> new TilledSoilBlock(soil.getFertility(), dryHumidity, moistHumidity, irrigatedHumidity,
                untilledBlock, soil.getSlideSound(), BlockBehaviour.Properties.of(Material.DIRT, soil.getBaseColor()).randomTicks()
                .strength(soil.getStrength(), soil.getExplosionResistance()).sound(soil.getSound())));
    }

    private static RegistryObject<StairBlockNF> stairs(String name, RegistryObject<? extends Block> base) {
        return BLOCKS.register(name + "_stairs", () -> new StairBlockNF(base, copyWithSuffocation(base.get())
                .strength(base.get().defaultDestroyTime() * 0.75F, base.get().getExplosionResistance() * 0.75F)));
    }

    private static RegistryObject<StairBlockNF> stairs(RegistryObject<? extends Block> base) {
        return stairs(base.getId().getPath(), base);
    }

    private static RegistryObject<SlabBlockNF> slab(String name, RegistryObject<? extends Block> base) {
        return BLOCKS.register(name + "_slab", () -> new SlabBlockNF(base, copyWithSuffocation(base.get())
                .strength(base.get().defaultDestroyTime() * 0.5F, base.get().getExplosionResistance() * 0.5F)));
    }

    private static RegistryObject<SlabBlockNF> slab(RegistryObject<? extends Block> base) {
        return slab(base.getId().getPath(), base);
    }

    private static RegistryObject<SidingBlock> siding(String name, RegistryObject<? extends Block> base) {
        return BLOCKS.register(name + "_siding", () -> new SidingBlock(base, copyWithSuffocation(base.get())
                .strength(base.get().defaultDestroyTime() * 0.5F, base.get().getExplosionResistance() * 0.5F)));
    }

    private static RegistryObject<SidingBlock> siding(RegistryObject<? extends Block> base) {
        return siding(base.getId().getPath(), base);
    }

    protected static BlockBehaviour.Properties fullCopy(Block block) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.copy(block);
        if(block.getLootTable().equals(BuiltInLootTables.EMPTY)) properties.noDrops();
        return properties;
    }

    protected static BlockBehaviour.Properties copyWithSuffocation(Block block) {
        return fullCopy(block).isSuffocating(BlocksNF::blocksMotion).isViewBlocking(BlocksNF::blocksMotion);
    }

    protected static ToIntFunction<BlockState> litBlockEmission(int light) {
        return (blockState) -> blockState.getValue(BlockStateProperties.LIT) ? light : 0;
    }

    private static boolean always(BlockState p_50775_, BlockGetter p_50776_, BlockPos p_50777_) {
        return true;
    }

    private static boolean never(BlockState p_50806_, BlockGetter p_50807_, BlockPos p_50808_) {
        return false;
    }

    private static boolean blocksMotion(BlockState p_61036_, BlockGetter p_61037_, BlockPos p_61038_) {
        return p_61036_.getMaterial().blocksMotion();
    }
}
