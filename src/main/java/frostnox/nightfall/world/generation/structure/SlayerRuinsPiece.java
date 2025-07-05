package frostnox.nightfall.world.generation.structure;

import frostnox.nightfall.block.Stone;
import frostnox.nightfall.block.Tree;
import frostnox.nightfall.block.block.ClusterBlock;
import frostnox.nightfall.block.block.SidingBlock;
import frostnox.nightfall.block.block.barrel.BarrelBlockNF;
import frostnox.nightfall.entity.entity.ArmorStandDummyEntity;
import frostnox.nightfall.item.TieredArmorMaterial;
import frostnox.nightfall.item.item.DyeableTieredArmorItem;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.forge.StructuresNF;
import frostnox.nightfall.registry.vanilla.LootTablesNF;
import frostnox.nightfall.util.math.noise.FractalSimplexNoiseFast;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.Random;

public class SlayerRuinsPiece extends StructurePieceNF {
    public static final int X_SIZE = 15;
    public static final int Z_SIZE = 13;
    public static final int ITEM_COLOR = 0x844545;
    private static final Direction[] BARREL_DIRECTIONS = new Direction[] {
            Direction.UP, Direction.EAST, Direction.NORTH, Direction.SOUTH
    };
    protected static FractalSimplexNoiseFast heightNoise;
    protected long lastSeed;

    public SlayerRuinsPiece(Random random, int x, int z) {
        this(x, z, getRandomHorizontalDirection(random));
    }

    public SlayerRuinsPiece(int x, int z, Direction orientation) {
        super(StructuresNF.SLAYER_RUINS_PIECE, 0, makeBoundingBox(x, 0, z, orientation, X_SIZE, 5, Z_SIZE));
        setOrientation(orientation);
    }

    public SlayerRuinsPiece(CompoundTag pTag) {
        super(StructuresNF.SLAYER_RUINS_PIECE, pTag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {

    }

    @Override
    public void postProcess(WorldGenLevel level, StructureFeatureManager pStructureFeatureManager, ChunkGenerator gen, Random random, BoundingBox box, ChunkPos pChunkPos, BlockPos centerPos) {
        if(updateHeightAverage(level, 0)) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            long seed = level.getSeed();
            if(heightNoise == null || lastSeed != seed) {
                heightNoise = new FractalSimplexNoiseFast(seed, 0.09F, 2, 0.5F, 2.0F);
                lastSeed = seed;
            }
            Tree tree = Tree.IRONWOOD;
            Stone stone;
            if(gen instanceof ContinentalChunkGenerator contGen) {
                stone = (Stone) contGen.getCachedStoneGroup(pChunkPos).igneousType;
                if(stone == Stone.GRANITE) stone = Stone.BASALT;
                else if(stone == Stone.STYGFEL) stone = Stone.DEEPSLATE;
            }
            else stone = Stone.DEEPSLATE;
            BlockState stoneBrick = BlocksNF.STONE_BRICK_BLOCKS.get(stone).get().defaultBlockState();
            BlockState stoneBrickSiding = BlocksNF.STONE_BRICK_SIDINGS.get(stone).get().defaultBlockState();
            BlockState stoneBrickSidingInner = stoneBrickSiding.setValue(SidingBlock.SHAPE, SidingBlock.Shape.POSITIVE_INNER);
            BlockState stoneBrickSlabTop = BlocksNF.STONE_BRICK_SLABS.get(stone).get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
            BlockState planks = BlocksNF.PLANK_BLOCKS.get(tree).get().defaultBlockState();
            BlockState plankStairs = BlocksNF.PLANK_STAIRS.get(tree).get().defaultBlockState();
            //Interior
            placeAirBox(level, pos, 2, 0, 2, X_SIZE - 3, 3, 6, box);
            placeAirBox(level, pos, 5, 0, 7, 9, 3, 10, box);
            placeAirBox(level, pos, 6, -1, 8, 8, -1, 10, box);
            //Inner walls
            for(int x = 1; x <= X_SIZE - 2; x++) {
                float height = getHeight(x, 1);
                for(int y = x == X_SIZE/2 ? 2 : -1; y <= height; y++) {
                    placeBlock(level, pickBrick(stone, y, height, random), pos, x, y, 1, box);
                }
            }
            for(int z = 2; z <= 7; z++) {
                float height = getHeight(1, z);
                for(int y = -1; y <= height; y++) {
                    placeBlock(level, pickBrick(stone, y, height, random), pos, 1, y, z, box);
                }
                height = getHeight(X_SIZE - 2, z);
                for(int y = -1; y <= height; y++) {
                    placeBlock(level, pickBrick(stone, y, height, random), pos, X_SIZE - 2, y, z, box);
                }
            }
            for(int x = 1; x <= 4; x++) {
                float height = getHeight(x, 7);
                for(int y = -1; y <= height; y++) {
                    placeBlock(level, pickBrick(stone, y, height, random), pos, x, y, 7, box);
                }
            }
            for(int x = 10; x <= X_SIZE - 3; x++) {
                float height = getHeight(x, 1);
                for(int y = -1; y <= height; y++) {
                    placeBlock(level, pickBrick(stone, y, height, random), pos, x, y, 7, box);
                }
            }
            for(int z = 8; z <= Z_SIZE - 2; z++) {
                float height = getHeight(4, z);
                for(int y = -1; y <= height; y++) {
                    placeBlock(level, pickBrick(stone, y, height, random), pos, 4, y, z, box);
                }
                height = getHeight(10, z);
                for(int y = -1; y <= height; y++) {
                    placeBlock(level, pickBrick(stone, y, height, random), pos, 10, y, z, box);
                }
            }
            for(int x = 5; x <= 9; x++) {
                float height = getHeight(x, Z_SIZE - 2);
                for(int y = -1; y <= height; y++) {
                    placeBlock(level, pickBrick(stone, y, height, random), pos, x, y, Z_SIZE - 2, box);
                }
            }
            //Windows
            placeBlock(level, stoneBrickSlabTop, pos, 3, 1, 1, box);
            placeBlock(level, stoneBrickSlabTop, pos, 5, 1, 1, box);
            placeBlock(level, stoneBrickSlabTop, pos, 9, 1, 1, box);
            placeBlock(level, stoneBrickSlabTop, pos, 11, 1, 1, box);
            placeBlock(level, stoneBrickSlabTop, pos, 1, 1, 4, box);
            placeBlock(level, stoneBrickSlabTop, pos, X_SIZE - 2, 1, 4, box);
            placeBlock(level, stoneBrickSlabTop, pos, 4, 1, Z_SIZE - 3, box);
            placeBlock(level, stoneBrickSlabTop, pos, 10, 1, Z_SIZE - 3, box);
            placeBlock(level, stoneBrickSlabTop, pos, 5, 1, Z_SIZE - 2, box);
            placeBlock(level, stoneBrickSlabTop, pos, 9, 1, Z_SIZE - 2, box);
            //Walls
            for(int x = 0; x <= X_SIZE - 1; x++) {
                if(x == 3 || x == 5 || x == 7 || x == 9 || x == 11) continue;
                BlockState siding = stoneBrickSiding.setValue(SidingBlock.TYPE, SidingBlock.Type.NORTH);
                if(x == 0) siding = siding.setValue(SidingBlock.SHAPE, SidingBlock.Shape.POSITIVE_QUARTET);
                else if(x == X_SIZE - 1) siding = siding.setValue(SidingBlock.SHAPE, SidingBlock.Shape.NEGATIVE_QUARTET);
                for(int y = -1; y <= getHeight(x, 0); y++) {
                    tryReplaceBlock(level, siding, pos, x, y, 0, box);
                }
            }
            for(int z = 1; z <= 7; z++) {
                if(z == 2 || z == 4 || z == 6) continue;
                for(int y = -1; y <= getHeight(0, z); y++) {
                    tryReplaceBlock(level, stoneBrickSiding.setValue(SidingBlock.TYPE, SidingBlock.Type.EAST), pos, 0, y, z, box);
                }
                for(int y = -1; y <= getHeight(X_SIZE - 1, z); y++) {
                    tryReplaceBlock(level, stoneBrickSiding.setValue(SidingBlock.TYPE, SidingBlock.Type.WEST), pos, X_SIZE - 1, y, z, box);
                }
            }
            BlockState innerSiding = stoneBrickSidingInner.setValue(SidingBlock.TYPE, SidingBlock.Type.EAST);
            for(int y = -1; y <= getHeight(3, 8); y++) tryReplaceBlock(level, innerSiding, pos, 3, y, 8, box);
            for(int x = 0; x <= 2; x++) {
                BlockState siding = stoneBrickSiding.setValue(SidingBlock.TYPE, SidingBlock.Type.SOUTH);
                if(x == 0) siding = siding.setValue(SidingBlock.SHAPE, SidingBlock.Shape.NEGATIVE_QUARTET);
                for(int y = -1; y <= getHeight(x, 8); y++) {
                    tryReplaceBlock(level, siding, pos, x, y, 8, box);
                }
            }
            innerSiding = stoneBrickSidingInner.setValue(SidingBlock.TYPE, SidingBlock.Type.SOUTH);
            for(int y = -1; y <= getHeight(11, 8); y++) tryReplaceBlock(level, innerSiding, pos, 11, y, 8, box);
            for(int x = 12; x <= X_SIZE - 1; x++) {
                BlockState siding = stoneBrickSiding.setValue(SidingBlock.TYPE, SidingBlock.Type.SOUTH);
                if(x == X_SIZE - 1) siding = siding.setValue(SidingBlock.SHAPE, SidingBlock.Shape.POSITIVE_QUARTET);
                for(int y = -1; y <= getHeight(x, 8); y++) {
                    tryReplaceBlock(level, siding, pos, x, y, 8, box);
                }
            }
            for(int z = 9; z <= Z_SIZE - 1; z++) {
                if(z == 10) continue;
                BlockState siding = stoneBrickSiding.setValue(SidingBlock.TYPE, SidingBlock.Type.EAST);
                if(z == Z_SIZE - 1) siding = siding.setValue(SidingBlock.SHAPE, SidingBlock.Shape.POSITIVE_QUARTET);
                for(int y = -1; y <= getHeight(3, z); y++) {
                    tryReplaceBlock(level, siding, pos, 3, y, z, box);
                }
                siding = stoneBrickSiding.setValue(SidingBlock.TYPE, SidingBlock.Type.WEST);
                if(z == Z_SIZE - 1) siding = siding.setValue(SidingBlock.SHAPE, SidingBlock.Shape.NEGATIVE_QUARTET);
                for(int y = -1; y <= getHeight(11, z); y++) {
                    tryReplaceBlock(level, siding, pos, 11, y, z, box);
                }
            }
            for(int x = 4; x <= 10; x++) {
                if(x == 5 || x == 7 || x == 9) continue;
                BlockState siding = stoneBrickSiding.setValue(SidingBlock.TYPE, SidingBlock.Type.SOUTH);
                for(int y = -1; y <= getHeight(x, Z_SIZE - 1); y++) {
                    tryReplaceBlock(level, siding, pos, x, y, Z_SIZE - 1, box);
                }
            }
            //Floor
            placeBlock(level, planks, pos, X_SIZE/2, -1, 1, box);
            for(int x = 2; x <= X_SIZE - 3; x++) {
                for(int z = 2; z <= 6; z++) {
                    placeBlock(level, planks, pos, x, -1, z, box);
                }
            }
            for(int x = 5; x <= 9; x++) {
                placeBlock(level, planks, pos, x, -1, 7, box);
            }
            placeBlock(level, planks, pos, 5, -1, 8, box);
            placeBlock(level, planks, pos, 5, -1, 9, box);
            placeBlock(level, planks, pos, 5, -1, 10, box);
            placeBlock(level, planks, pos, 9, -1, 8, box);
            placeBlock(level, planks, pos, 9, -1, 9, box);
            placeBlock(level, planks, pos, 9, -1, 10, box);
            placeBlock(level, planks, pos, 6, -1, 8, box);
            placeBlock(level, planks, pos, 7, -1, 8, box);
            //Rubble
            for(int x = 2; x <= X_SIZE - 3; x++) {
                for(int z = 2; z <= 6; z++) {
                    float height = getRubbleHeight(x, z);
                    for(int y = 0; y <= height; y++) {
                        placeBlock(level, pickRubble(stone, y, height, random), pos, x, y, z, box);
                    }
                }
            }
            for(int x = 5; x <= 9; x++) {
                for(int z = 7; z <= 10; z++) {
                    float height = getRubbleHeight(x, z);
                    for(int y = 0; y <= height; y++) {
                        placeBlock(level, pickRubble(stone, y, height, random), pos, x, y, z, box);
                    }
                }
            }
            //Basement space
            for(int x = X_SIZE/2 - 1; x <= X_SIZE/2 + 1; x++) {
                for(int z = 2; z <= Z_SIZE - 3; z++) {
                    for(int y = -4; y <= -2; y++) {
                        placeBlock(level, Blocks.AIR.defaultBlockState(), pos, x, y, z, box);
                    }
                    placeBlock(level, BlocksNF.STACKED_STONE.get(stone).get().defaultBlockState(), pos, x, -5, z, box);
                }
            }
            //Stairs
            for(int y = -4; y <= -1; y++) {
                placeBlock(level, BlocksNF.LOGS.get(tree).get().defaultBlockState(), pos, X_SIZE/2, y, 9, box);
            }
            placeBlock(level, plankStairs.setValue(StairBlock.FACING, Direction.SOUTH), pos, 8, -1, 8, box);
            placeBlock(level, plankStairs.setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, Half.TOP), pos, 8, -2, 8, box);
            placeBlock(level, plankStairs.setValue(StairBlock.FACING, Direction.SOUTH), pos, 8, -2, 9, box);
            placeBlock(level, plankStairs.setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, Half.TOP), pos, 8, -3, 9, box);
            placeBlock(level, planks, pos, 8, -3, 10, box);
            placeBlock(level, plankStairs.setValue(StairBlock.FACING, Direction.EAST), pos, 7, -3, 10, box);
            placeBlock(level, plankStairs.setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, Half.TOP), pos, 7, -4, 10, box);
            placeBlock(level, planks, pos, 6, -4, 10, box);
            placeBlock(level, plankStairs.setValue(StairBlock.FACING, Direction.NORTH), pos, 6, -4, 9, box);
            BlockState cobble = BlocksNF.COBBLED_STONE.get(stone).get().defaultBlockState();
            placeBlock(level, cobble, pos, 8, -1, 9, box);
            placeBlock(level, cobble, pos, 8, -1, 10, box);
            placeBlock(level, cobble, pos, 8, -2, 10, box);
            placeBlock(level, cobble, pos, 7, -1, 10, box);
            placeBlock(level, cobble, pos, 7, -2, 10, box);
            placeBlock(level, cobble, pos, 6, -1, 10, box);
            placeBlock(level, cobble, pos, 6, -2, 10, box);
            placeBlock(level, cobble, pos, 6, -3, 10, box);
            placeBlock(level, cobble, pos, 6, -1, 9, box);
            placeBlock(level, cobble, pos, 6, -2, 9, box);
            placeBlock(level, cobble, pos, 6, -3, 9, box);
            //Basement walls
            for(int x = X_SIZE/2 - 1; x <= X_SIZE/2 + 1; x++) {
                for(int y = -4; y <= -2; y++) {
                    placeBlock(level, stoneBrick, pos, x, y, 1, box);
                    placeBlock(level, stoneBrick, pos, x, y, Z_SIZE - 2, box);
                }
            }
            for(int z = 1; z <= Z_SIZE - 2; z++) {
                for(int y = -4; y <= -2; y++) {
                    placeBlock(level, stoneBrick, pos, X_SIZE/2 - 2, y, z, box);
                    placeBlock(level, stoneBrick, pos, X_SIZE/2 + 2, y, z, box);
                }
            }
            //Barrels
            for(int i = 0; i < (random.nextBoolean() ? 1 : 2); i++) {
                int z = 3 + random.nextInt(4);
                placeContainer(level, box, random, X_SIZE/2 - 1, -4, z,
                        BlocksNF.BARRELS.get(Tree.LARCH).get().defaultBlockState().setValue(BarrelBlockNF.FACING, BARREL_DIRECTIONS[random.nextInt(4)]),
                        LootTablesNF.SLAYER_RUINS_BARREL_LOOT, false);
            }
            //Chest
            int chestZ = 3 + random.nextInt(4);
            placeContainer(level, box, random, X_SIZE/2 + 1, -4, chestZ,
                    BlocksNF.CHESTS.get(Tree.LARCH).get().defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST),
                    LootTablesNF.SLAYER_RUINS_CHEST_LOOT, false);
            //Rocks
            for(int x = X_SIZE/2 - 1; x <= X_SIZE/2 + 1; x++) {
                for(int z = 3; z <= Z_SIZE - 3; z++) {
                    pos.set(getWorldX(x, z), getWorldY(-4), getWorldZ(x, z));
                    if(level.getBlockState(pos).isAir() && random.nextInt() % 3 == 0) {
                        placeBlock(level, BlocksNF.ROCK_CLUSTERS.get(stone).get().defaultBlockState().setValue(ClusterBlock.COUNT, 1 + random.nextInt(4)), pos, box, false);
                    }
                }
            }
            //Armor stand
            BlockPos entityPos = getWorldPos(X_SIZE/2, -4, 2);
            if(box.isInside(entityPos)) {
                ArmorStandDummyEntity armorStand = EntitiesNF.ARMOR_STAND.get().create(level.getLevel());
                armorStand.setMaterial(ItemsNF.ARMOR_STANDS.get(Tree.LARCH).get().sourceMaterial);
                armorStand.moveTo(entityPos.getX() + 0.5, entityPos.getY(), entityPos.getZ() + 0.5, getOrientation().toYRot(), 0);
                TieredArmorMaterial material;
                float f = random.nextFloat();
                if(f < 0.33F) material = TieredArmorMaterial.IRON_CHAINMAIL_SLAYER;
                else if(f < 0.66F) material = TieredArmorMaterial.IRON_SCALE_SLAYER;
                else material = TieredArmorMaterial.IRON_PLATE_SLAYER;
                ItemStack helmet = new ItemStack(ItemsNF.HELMETS.get(material).get());
                DyeableTieredArmorItem armorItem = (DyeableTieredArmorItem) helmet.getItem();
                armorItem.setColor(helmet, ITEM_COLOR);
                helmet.setDamageValue((int) (helmet.getMaxDamage() * (1F - random.nextFloat() * 0.05F)));
                armorStand.setItemSlot(EquipmentSlot.HEAD, helmet);
                if(random.nextFloat() < 0.3F) {
                    ItemStack chestPlate = new ItemStack(ItemsNF.CHESTPLATES.get(material).get());
                    armorItem.setColor(chestPlate, ITEM_COLOR);
                    chestPlate.setDamageValue((int) (chestPlate.getMaxDamage() * (1F - random.nextFloat() * 0.05F)));
                    armorStand.setItemSlot(EquipmentSlot.CHEST, chestPlate);
                }
                if(random.nextFloat() < 0.3F) {
                    ItemStack leggings = new ItemStack(ItemsNF.LEGGINGS.get(material).get());
                    armorItem.setColor(leggings, ITEM_COLOR);
                    leggings.setDamageValue((int) (leggings.getMaxDamage() * (1F - random.nextFloat() * 0.05F)));
                    armorStand.setItemSlot(EquipmentSlot.LEGS, leggings);
                }
                if(random.nextFloat() < 0.3F) {
                    ItemStack boots = new ItemStack(ItemsNF.BOOTS.get(material).get());
                    armorItem.setColor(boots, ITEM_COLOR);
                    boots.setDamageValue((int) (boots.getMaxDamage() * (1F - random.nextFloat() * 0.05F)));
                    armorStand.setItemSlot(EquipmentSlot.FEET, boots);
                }
                level.addFreshEntityWithPassengers(armorStand);
            }
        }
    }

    private float getHeight(int x, int z) {
        return 1.2F + Math.abs(heightNoise.noise2D(getWorldX(x, z), getWorldZ(x, z))) * 1.6F;
    }

    private float getRubbleHeight(int x, int z) {
        return -0.25F + Math.abs(heightNoise.noise2D(getWorldX(x, z), getWorldZ(x, z))) * 1.8F;
    }

    private static BlockState pickBrick(Stone stone, int y, float height, Random random) {
        if(y < (int) height) return BlocksNF.STONE_BRICK_BLOCKS.get(stone).get().defaultBlockState();
        else {
            float fraction = height % 1F;
            if(fraction < 0.5F) return BlocksNF.STONE_BRICK_SLABS.get(stone).get().defaultBlockState();
            else {
                return BlocksNF.STONE_BRICK_STAIRS.get(stone).get().defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
            }
        }
    }

    private static BlockState pickRubble(Stone stone, int y, float height, Random random) {
        if(y < (int) height) return BlocksNF.COBBLED_STONE.get(stone).get().defaultBlockState();
        else {
            float fraction = height % 1F;
            if(fraction < 0.5F) return BlocksNF.COBBLED_STONE_SLABS.get(stone).get().defaultBlockState();
            else {
                return BlocksNF.COBBLED_STONE_STAIRS.get(stone).get().defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
            }
        }
    }
}