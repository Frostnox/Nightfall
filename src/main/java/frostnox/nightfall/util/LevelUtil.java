package frostnox.nightfall.util;

import com.mojang.authlib.GameProfile;
import com.mojang.math.Vector3d;
import frostnox.nightfall.block.*;
import frostnox.nightfall.capability.*;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.recipe.BuildingRecipe;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.network.message.world.DestroyBlockNoSoundToClient;
import frostnox.nightfall.registry.KnowledgeNF;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.data.WrappedInt;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.ContinentalWorldType;
import frostnox.nightfall.world.OrientedEntityHitResult;
import frostnox.nightfall.world.inventory.AccessorySlot;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class LevelUtil {
    public static final Predicate<Entity> ALIVE_ACTION_TRACKER_ENTITY = (entity) -> {
        return entity.isAlive() && (entity instanceof ActionableEntity || (entity instanceof Player player && !player.getAbilities().invulnerable));
    };
    public static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static final Direction[] PHYSICS_DIRECTIONS = new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP};
    public static final Direction[] HORIZONTAL_DIRECTIONS = new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] HORIZONTAL_DOWN_DIRECTIONS = new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN};
    public static final BlockPos NULL_POS = new BlockPos(0, -32768, 0);
    public static final GameProfile FAKE_PROFILE = new GameProfile(UUID.fromString("f8e91fce-7ddd-47d3-a0fe-d4992193510f"), "fakeNightfallPlayer");
    public static final double UNDEAD_MIN_SPAWN_DIST_SQR = 24D * 24D;
    public static final double UNDEAD_MAX_SPAWN_DIST_SQR = 128 * 128;
    public static final long MORNING_TIME = 0L;
    public static final long DAY_TIME = 1000L * (ContinentalWorldType.DAY_LENGTH / 24000L);
    public static final long NOON_TIME = 6000L * (ContinentalWorldType.DAY_LENGTH / 24000L);
    public static final long SUNSET_TIME = 12000L * (ContinentalWorldType.DAY_LENGTH / 24000L);
    public static final long NIGHT_TIME = 13000L * (ContinentalWorldType.DAY_LENGTH / 24000L);
    public static final long MIDNIGHT_TIME = 18000L * (ContinentalWorldType.DAY_LENGTH / 24000L);
    public static final long SUNRISE_TIME = 23000L * (ContinentalWorldType.DAY_LENGTH / 24000L);
    public static final long UNDEAD_START_TIME = 13500L * (ContinentalWorldType.DAY_LENGTH / 24000L);
    private static final int MAX_FURNACE_AREA = 9;
    private static final int MAX_BLAST_FURNACE_AREA = 16;

    public static long getDayLength(Level level) {
        return LevelData.isPresent(level) ? ContinentalWorldType.DAY_LENGTH : 24000;
    }

    public static boolean isDay(LevelAccessor level) {
        return isDay(level.dayTime(), level);
    }

    public static boolean isDay(long dayTime, LevelAccessor level) {
        float time = level.dimensionType().timeOfDay(dayTime);
        return time > 0.75F || time < 0.25F;
    }

    public static boolean isNight(LevelAccessor level) {
        return !isDay(level);
    }

    public static boolean isNight(long dayTime, LevelAccessor level) {
        return !isDay(dayTime, level);
    }

    public static boolean hasPassedNight(long lastLoadDayTime, Level level) {
        long dayLength = getDayLength(level);
        long time = level.dayTime() - lastLoadDayTime;
        return time > dayLength / 2 || (isDay(lastLoadDayTime, level) != isDay(level));
    }

    /**
     * @param startTime 0 <= time <= day length
     * @param endTime 0 <= time <= day length
     */
    public static boolean isDayTimeWithin(Level level, long startTime, long endTime) {
        return isDayTimeWithin(level, level.getDayTime(), startTime, endTime);
    }

    /**
     * @param startTime 0 <= time <= day length
     * @param endTime 0 <= time <= day length
     */
    public static boolean isDayTimeWithin(Level level, long dayTime, long startTime, long endTime) {
        long time = Math.floorMod(dayTime, getDayLength(level));
        if(endTime < startTime) return time <= endTime || time >= startTime;
        else return time >= startTime && time <= endTime;
    }

    public static long getDayTimePassed(Level level, long elapsedTime) {
        return getDayTimePassedWithin(level, level.getDayTime(), elapsedTime, SUNRISE_TIME, NIGHT_TIME);
    }

    public static long getDayTimePassed(Level level, long dayTime, long elapsedTime) {
        return getDayTimePassedWithin(level, dayTime, elapsedTime, SUNRISE_TIME, NIGHT_TIME);
    }

    /**
     * @param startTime 0 <= time <= day length
     * @param endTime 0 <= time <= day length
     */
    public static long getDayTimePassedWithin(Level level, long elapsedTime, long startTime, long endTime) {
        return getDayTimePassedWithin(level, level.getDayTime(), elapsedTime, startTime, endTime);
    }

    /**
     * @param startTime 0 <= time <= day length
     * @param endTime 0 <= time <= day length
     */
    public static long getDayTimePassedWithin(Level level, long dayTime, long elapsedTime, long startTime, long endTime) {
        if(elapsedTime <= 0) return 0;
        long dayLength = getDayLength(level);
        long fullDays = elapsedTime / dayLength;
        long activeTime = endTime - startTime;
        if(activeTime != dayLength) activeTime = Math.floorMod(activeTime, dayLength);
        //Count time for full days
        long timePassed = activeTime * fullDays;
        //Count time for remaining partial day
        long currentPartialTime = Math.floorMod(dayTime, dayLength);
        long pastPartialTime = currentPartialTime - (elapsedTime % dayLength);
        if(currentPartialTime == pastPartialTime) return timePassed;
        //Wrap around case
        if(endTime < startTime) {
            if(pastPartialTime < endTime) timePassed += Math.min(endTime, currentPartialTime) - pastPartialTime;
            if(currentPartialTime > startTime) timePassed += currentPartialTime - Math.max(pastPartialTime, startTime);
        }
        //Standard case
        else {
            if(currentPartialTime > startTime && pastPartialTime < endTime) {
                timePassed += Math.min(currentPartialTime, endTime) - Math.max(pastPartialTime, startTime);
            }
        }
        return timePassed;
    }

    public static BlockPos getRandomSurfacePos(Level level, LevelChunk chunk) {
        ChunkPos chunkpos = chunk.getPos();
        int x = chunkpos.getMinBlockX() + level.random.nextInt(16);
        int z = chunkpos.getMinBlockZ() + level.random.nextInt(16);
        int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 1;
        return new BlockPos(x, y, z);
    }

    public static @Nullable BlockPos getRandomWaterPos(Level level, LevelChunk chunk) {
        ChunkPos chunkpos = chunk.getPos();
        int x = chunkpos.getMinBlockX() + level.random.nextInt(16);
        int z = chunkpos.getMinBlockZ() + level.random.nextInt(16);
        int minY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        int maxY = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if(minY >= maxY) return null;
        return new BlockPos(x, minY + level.random.nextInt(maxY - minY + 1), z);
    }

    public static BlockPos getRandomPos(Level level, LevelChunk chunk) {
        ChunkPos chunkpos = chunk.getPos();
        int x = chunkpos.getMinBlockX() + level.random.nextInt(16);
        int z = chunkpos.getMinBlockZ() + level.random.nextInt(16);
        return new BlockPos(x, level.getMinBuildHeight() + level.random.nextInt(level.getMaxBuildHeight()), z);
    }

    public static boolean canFallThrough(BlockState state) {
        Material material = state.getMaterial();
        return state.isAir() || state.is(TagsNF.FALLING_DESTROYABLE) || material.isLiquid() || material.isReplaceable();
    }

    /**
     * @param level must be server level
     */
    public static void uncheckedDropDestroyBlockNoSound(Level level, BlockPos pos, BlockState state, BlockState replaceState, @Nullable Entity entity, int flags) {
        NetworkHandler.toAllTrackingChunk(level.getChunkAt(pos), new DestroyBlockNoSoundToClient(pos, Block.getId(state)));
        Block.dropResources(state, level, pos, null, entity, ItemStack.EMPTY);
        level.setBlock(pos, replaceState, flags);
    }

    public static boolean destroyBlockNoSound(LevelAccessor level, BlockPos pos, boolean dropBlock) {
        return destroyBlockNoSound(level, pos, dropBlock, null);
    }

    /**
     * Modified version of {@link Level#destroyBlock(BlockPos, boolean, Entity, int)}
     */
    public static boolean destroyBlockNoSound(LevelAccessor level, BlockPos pos, boolean dropBlock, @Nullable Entity entity) {
        BlockState block = level.getBlockState(pos);
        if(block.isAir()) return false;
        else {
            FluidState fluid = level.getFluidState(pos);

            if(level instanceof ServerLevel serverLevel) {
                if(!(block.getBlock() instanceof BaseFireBlock)) {
                    NetworkHandler.toAllTrackingChunk(serverLevel.getChunkAt(pos), new DestroyBlockNoSoundToClient(pos, Block.getId(block)));
                }
                if(dropBlock) {
                    BlockEntity blockentity = block.hasBlockEntity() ? serverLevel.getBlockEntity(pos) : null;
                    Block.dropResources(block, serverLevel, pos, blockentity, entity, ItemStack.EMPTY);
                }
            }

            boolean success = level.setBlock(pos, fluid.createLegacyBlock(), 3, 512);
            if(success) level.gameEvent(entity, GameEvent.BLOCK_DESTROY, pos);

            return success;
        }
    }

    public static boolean isPointVisuallyInBlock(Level level, BlockPos blockPos, Vec3 pos) {
        VoxelShape shape = level.getBlockState(blockPos).getVisualShape(level, blockPos, CollisionContext.empty()).move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if(!shape.isEmpty()) {
            for(AABB box : shape.toAabbs()) if(box.contains(pos)) return true;
        }
        return false;
    }

    public static BlockPos getFirstBlockInDirection(TagKey<Block> blockTag, Level level, BlockPos startPos, Direction direction, int distance) {
        for(int i = 0; i < distance; i++) {
            BlockPos pos = startPos.relative(direction, i + 1);
            if(level.getBlockState(pos).is(blockTag)) return pos;
        }
        return startPos;
    }

    public static int getBlockHeatResistanceTier(BlockState state) {
        if(state.is(TagsNF.HEAT_RESISTANT_4)) return 4;
        else if(state.is(TagsNF.HEAT_RESISTANT_3)) return 3;
        else if(state.is(TagsNF.HEAT_RESISTANT_2)) return 2;
        else if(state.is(TagsNF.HEAT_RESISTANT_1)) return 1;
        else return 0;
    }

    public static int getNearbySmelterTier(Level level, BlockPos pos) {
        return Math.max(getNearbyKilnBaseTier(level, pos), getNearbyFurnaceTier(level, pos));
    }

    public static int getNearbyKilnBaseTier(Level level, BlockPos pos) {
        int i = 0;
        for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState state = level.getBlockState(pos.relative(direction));
            if(!state.is(TagsNF.HEAT_RESISTANT_1) || !state.isFaceSturdy(level, pos, direction.getOpposite())) {
                i++;
                if(i > 1) return 0;
            }
        }
        return 1;
    }

    public static int getNearbyKilnTier(Level level, BlockPos pos) {
        for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState state = level.getBlockState(pos.relative(direction));
            if(!state.is(TagsNF.HEAT_RESISTANT_1) || !state.isFaceSturdy(level, pos, direction.getOpposite())) return 0;
        }
        return 1;
    }

    public static int getNearbyFurnaceTier(Level level, BlockPos pos) {
        //Check that block is contained in an area no larger than the specified size
        WrappedInt area = new WrappedInt(1), lowestTier = new WrappedInt(4);
        IntSet visited = new IntOpenHashSet(MAX_FURNACE_AREA);
        visited.add(DataUtil.hashPos(pos));
        for(Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos dirPos = pos.relative(dir);
            checkFurnaceArea(level, dirPos, DataUtil.hashPos(dirPos), visited, area, lowestTier);
        }
        if(area.val <= MAX_FURNACE_AREA) return lowestTier.val;
        else return 0;
    }

    private static void checkFurnaceArea(Level level, BlockPos pos, int hash, IntSet visited, WrappedInt area, WrappedInt lowestTier) {
        if(visited.contains(hash)) return;
        visited.add(hash);
        if(area.val > MAX_FURNACE_AREA) return;
        else {
            int heatResistance = getBlockHeatResistanceTier(level.getBlockState(pos));
            if(heatResistance > 0) {
                if(lowestTier.val > heatResistance) lowestTier.val = heatResistance;
                return;
            }
        }
        area.val++;
        for(Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos dirPos = pos.relative(dir);
            checkFurnaceArea(level, dirPos, DataUtil.hashPos(dirPos), visited, area, lowestTier);
        }
    }

    public static boolean isBlockBurningCarbon(BlockState state) {
        return state.is(BlocksNF.CHARCOAL_BURNING.get()) || state.is(BlocksNF.COAL_BURNING.get());
    }

    public static boolean isBlockCovered(Level level, BlockPos pos) {
        for(Direction direction : Direction.values()) {
            if(!level.getBlockState(pos.relative(direction)).isFaceSturdy(level, pos, direction.getOpposite())) return false;
        }
        return true;
    }

    public static boolean isBlockSmothered(Level level, BlockPos pos) {
        for(Direction direction : Direction.values()) {
            BlockState state = level.getBlockState(pos.relative(direction));
            if(!(state.getBlock() instanceof IHeatSource || !(state.getBlock() instanceof IIgnitable)) || !state.isFaceSturdy(level, pos, direction.getOpposite())) return false;
        }
        return true;
    }

    public static boolean isBlockFullySupportedHorizontally(AABB blockBox, LevelReader level, BlockPos supportPos, Direction supportDir) {
        BlockState supportState = level.getBlockState(supportPos);
        if(supportState.is(TagsNF.UNSTABLE_SUPPORT_HORIZONTAL)) return false;
        VoxelShape supportShape = supportState.getCollisionShape(level, supportPos);
        if(supportShape == Shapes.block()) return true;
        else if(supportShape.isEmpty()) return false;
        supportShape = supportShape.getFaceShape(supportDir);
        if(supportShape.isEmpty()) return false;

        double minXZBest = 1D, minY = 1D, maxY = 0D, minXZ;
        if(supportDir.getAxis() == Direction.Axis.X) {
            minXZ = blockBox.minZ;
            for(AABB sBox : supportShape.toAabbs()) {
                if(sBox.minZ <= blockBox.minZ && sBox.maxZ >= blockBox.maxZ) {
                    if(sBox.minZ < minXZBest) minXZBest = sBox.minZ;
                    if(sBox.minY < minY) minY = sBox.minY;
                    if(sBox.maxY > maxY) maxY = sBox.maxY;
                }
            }
        }
        else {
            minXZ = blockBox.minX;
            for(AABB sBox : supportShape.toAabbs()) {
                if(sBox.minX <= blockBox.minX && sBox.maxX >= blockBox.maxX) {
                    if(sBox.minX < minXZBest) minXZBest = sBox.minX;
                    if(sBox.minY < minY) minY = sBox.minY;
                    if(sBox.maxY > maxY) maxY = sBox.maxY;
                }
            }
        }

        return minY <= blockBox.minY && maxY >= blockBox.maxY && minXZBest <= minXZ;
    }

    public static void preventBlockLowerHalfDrop(Level level, BlockPos pos, BlockState state, Player pPlayer) {
        DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        if(half == DoubleBlockHalf.UPPER) {
            BlockPos lowerPos = pos.below();
            BlockState lowerState = level.getBlockState(lowerPos);
            if(lowerState.is(state.getBlock()) && lowerState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                BlockState newState = Blocks.AIR.defaultBlockState();
                if(lowerState.hasProperty(BlockStatePropertiesNF.WATER_LEVEL)) {
                    int waterLevel = lowerState.getValue(BlockStatePropertiesNF.WATER_LEVEL);
                    if(waterLevel > 0) {
                        newState = lowerState.getValue(BlockStatePropertiesNF.WATERLOG_TYPE).fluid.get().defaultFluidState().createLegacyBlock();
                        if(waterLevel < 7) newState = newState.setValue(LiquidBlock.LEVEL, waterLevel);
                    }
                }
                level.setBlock(lowerPos, newState, 35);
                level.levelEvent(pPlayer, 2001, lowerPos, Block.getId(lowerState)); //Sound & breaking particles
            }
        }
    }

    public static void breakItem(ItemStack item, Player player, InteractionHand hand) {
        if(!player.level.isClientSide && !player.getAbilities().instabuild) {
            player.broadcastBreakEvent(hand);
            item.shrink(1);
        }
    }

    public static boolean isTouchingWater(Level level, BlockPos pos) {
        boolean touching = false;
        for(Direction direction : Direction.values()) {
            if(direction == Direction.DOWN) continue;
            BlockPos touchPos = pos.relative(direction);
            if(level.getFluidState(touchPos).is(Fluids.WATER)) touching = true;
            if(touching) return true;
        }
        return false;
    }

    @Nullable
    public static OrientedEntityHitResult getHitEntity(Level level, Entity projectile, Vec3 start, Vec3 end, AABB box, Predicate<Entity> filter) {
        double minDistSqr = Double.MAX_VALUE;
        Entity closestEntity = null;
        Vec3 collisionVec = null;
        double inflation = Math.max(projectile.getBbWidth() * 0.5, projectile.getBbHeight() * 0.5);
        int boxIndex = -1;
        for(Entity entity : level.getEntities(projectile, box.inflate(IOrientedHitBoxes.MAX_DIST_FROM_AABB), filter)) {
            if(!(entity instanceof IOrientedHitBoxes hitBoxesEntity) || hitBoxesEntity.includeAABB()) {
                AABB aabb = entity.getBoundingBox().inflate(inflation);
                Optional<Vec3> point = aabb.clip(start, end);
                if(point.isPresent()) {
                    double distSqr = start.distanceToSqr(point.get());
                    if(distSqr < minDistSqr) {
                        closestEntity = entity;
                        minDistSqr = distSqr;
                        collisionVec = point.get();
                        continue;
                    }
                }
            }
            if(entity instanceof IOrientedHitBoxes hitBoxesEntity) {
                Vec3 startOrigin = start.subtract(entity.position()), endOrigin = end.subtract(entity.position());
                OBB[] obbs = hitBoxesEntity.getOBBs(1F);
                for(int i = 0; i < obbs.length; i++) {
                    OBB obb = obbs[i];
                    obb.extents = obb.extents.scale(1D + inflation);
                    Optional<Vec3> obbPoint = obb.rayCast(startOrigin, endOrigin);
                    if(obbPoint.isPresent() && box.contains(obbPoint.get().add(entity.position()))) { //Ray is infinite so check box again
                        double distSqr = startOrigin.distanceToSqr(obbPoint.get());
                        if(distSqr < minDistSqr) {
                            closestEntity = entity;
                            minDistSqr = distSqr;
                            collisionVec = obbPoint.get().add(entity.position());
                            boxIndex = i;
                        }
                        break;
                    }
                }
            }
        }
        return closestEntity == null ? null : new OrientedEntityHitResult(closestEntity, collisionVec, boxIndex);
    }

    public static HitResult getHitResult(Entity entity, Predicate<Entity> filter) {
        Level level = entity.level;
        Vec3 start = entity.position();
        Vec3 end = start.add(entity.getDeltaMovement());
        HitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        if(blockHit.getType() != HitResult.Type.MISS) end = blockHit.getLocation();
        HitResult entityHit = getHitEntity(level, entity, start, end, entity.getBoundingBox().expandTowards(entity.getDeltaMovement()).inflate(1.0D), filter);
        return entityHit != null ? entityHit : blockHit;
    }

    public static VoxelShape getBlockClimbingShape(LivingEntity user, BlockPos pos) {
        BlockState state = user.level.getBlockState(pos);
        if(state.isAir() || state.is(TagsNF.UNCLIMBABLE) || state.is(BlockTags.CLIMBABLE)) return Shapes.empty();
        else return state.getCollisionShape(user.level, pos).move(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isPositionFullyClimbable(LivingEntity user, Vector3d position) {
        BlockPos abovePos = new BlockPos(position.x, Math.ceil(position.y) + 1D/16D, position.z);
        BlockState state = user.level.getBlockState(abovePos);
        return state.is(TagsNF.FULLY_CLIMBABLE);
    }

    public static boolean disallowPlayerSprint(Player player) {
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        return capP.getStamina() <= 0F || (!capA.isInactive() && !capA.getAction().allowSprinting()) || player.isUsingItem();
    }

    public static void extinguishItemEntity(ItemEntity entity, Item extinguishedItem, boolean smoke) {
        if(entity.level instanceof ServerLevel serverLevel) {
            entity.setItem(new ItemStack(extinguishedItem, entity.getItem().getCount()));
            NetworkHandler.toAllTracking(entity, new GenericEntityToClient(NetworkHandler.Type.REMOVE_LIGHT_SOURCE_CLIENT, entity.getId()));
            serverLevel.playSound(null, entity.getX(), entity.getEyeY(), entity.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.2F + entity.level.random.nextFloat());
            if(smoke) serverLevel.sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getEyeY(), entity.getZ(), 1, 0, 0, 0, 0);
        }
    }

    public static void spreadHeat(Level level, BlockPos spreadPos, BlockState spreadState, TieredHeat heat, Direction fromDir) {
        if(spreadState.getBlock() instanceof IHeatable heatable) {
            heatable.applyHeat(level, spreadPos, spreadState, heat, fromDir);
        }
        if(heat != TieredHeat.NONE && spreadState.getBlock() instanceof IIgnitable ignitable) {
            ignitable.tryToIgnite(level, spreadPos, spreadState, ItemStack.EMPTY, heat);
        }
    }

    /**
     * Index is always accepted when sent to the server, so make sure to validate before use
     * @return unsafe modifiable index associated with item in player's hand
     */
    public static int getModifiableItemIndex(Level level, Player player, InteractionHand hand) {
        return level.isClientSide
                ? (hand == InteractionHand.MAIN_HAND ? ClientEngine.get().getModifiableIndexMain() : ClientEngine.get().getModifiableIndexOff())
                : PlayerData.get(player).getCachedModifiableIndex();
    }

    public static ItemStack pickCloneItem(Block block, Player player) {
        if(player.isCreative() && ClientEngine.get().isCtrlHeld()) return new ItemStack(block);
        for(BuildingRecipe recipe : player.level.getRecipeManager().getAllRecipesFor(BuildingRecipe.TYPE)) {
            if(recipe.output instanceof BlockItem blockItem && blockItem.getBlock() == block) return new ItemStack(recipe.baseItem);
        }
        return new ItemStack(block);
    }

    public static List<ItemStack> getUnlockedIngredients(Ingredient ingredient, @Nullable Player player) {
        if(player == null) return List.of(ingredient.getItems());
        else {
            List<ItemStack> items = new ObjectArrayList<>(ingredient.getItems().length);
            IPlayerData capP = PlayerData.get(player);
            for(ItemStack item : ingredient.getItems()) {
                ResourceLocation id = ResourceLocation.parse(item.getItem().getRegistryName().toString() + "_item");
                if(!KnowledgeNF.contains(id) || capP.hasKnowledge(id)) items.add(item);
            }
            return List.copyOf(items);
        }
    }

    public static ItemStack chooseUnlockedIngredient(Ingredient ingredient, @Nullable Player player) {
        List<ItemStack> items = getUnlockedIngredients(ingredient, player);
        if(items.isEmpty()) return ItemStack.EMPTY;
        else if(player == null) return items.get(0);
        else return items.get(player.tickCount / 24 % items.size());
    }

    public static double getShortestDistanceSqrUndeadMinToPlayer(Level level, double x, double y, double z, boolean checkCharm) {
        double closestDist = Double.MAX_VALUE;
        for(Player player : level.players()) {
            if(!player.isAlive()) continue;
            if(EntitySelector.NO_SPECTATORS.test(player)) {
                double dist = player.distanceToSqr(x, y ,z);
                if(checkCharm && PlayerData.get(player).getAccessoryInventory().getItem(AccessorySlot.NECK).is(ItemsNF.WARDING_CHARM.get())) {
                    dist = Math.max(0, dist - 6 * 6);
                }
                if(dist < closestDist) closestDist = dist;
            }
        }
        return closestDist;
    }

    public static double getShortestDistanceSqrUndeadMaxToPlayer(Level level, double x, double y, double z, boolean checkKills) {
        double closestDist = Double.MAX_VALUE;
        for(Player player : level.players()) {
            if(!player.isAlive()) continue;
            if(EntitySelector.NO_SPECTATORS.test(player)) {
                if(checkKills && PlayerData.get(player).getUndeadKilledThisNight() >= 40) continue;
                double dist = player.distanceToSqr(x, y ,z);
                if(dist < closestDist) closestDist = dist;
            }
        }
        return closestDist;
    }

    public static double getShortestDistanceSqrToPlayer(Level level, double x, double y, double z) {
        double closestDist = Double.MAX_VALUE;
        for(Player player : level.players()) {
            if(!player.isAlive()) continue;
            if(EntitySelector.NO_SPECTATORS.test(player)) {
                double dist = player.distanceToSqr(x, y ,z);
                if(dist < closestDist) closestDist = dist;
            }
        }
        return closestDist;
    }

    public static void warpClientPlayer(Player player, boolean playToSelf) {
        if(playToSelf || ClientEngine.get().getPlayer() != player) {
            player.level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundsNF.ENTITY_WARP.get(), SoundSource.PLAYERS, 1F, 1F, false);
        }
        float height = player.isAlive() ? player.getBbHeight() : 0.25F;
        for(int i = 0; i < 10 + player.getRandom().nextInt(6); i++) {
            player.level.addParticle(ParticleTypesNF.ESSENCE.get(), player.getRandomX(1D),
                    player.getY() + player.getRandom().nextFloat() * height, player.getRandomZ(1D), 0, 0, 0);
        }
    }

    public static void warpServerPlayer(Player player, boolean playToSelf) {
        if(player.level instanceof ServerLevel level) {
            player.level.playSound(playToSelf ? null : player, player.getX(), player.getY(), player.getZ(), SoundsNF.ENTITY_WARP.get(), SoundSource.PLAYERS, 1F, 1F);
            float height = player.isAlive() ? player.getBbHeight() : 0.25F;
            level.sendParticles(ParticleTypesNF.ESSENCE.get(), player.getX(), player.getY(), player.getZ(), 10 + player.getRandom().nextInt(6),
                    player.getBbWidth() * 0.5, height * 0.5, player.getBbWidth() * 0.5, 0);
        }
    }

    public static Color getMetalColor(ItemStack item) {
        for(var entry : RegistriesNF.getMetals()) {
            IMetal metal = entry.value;
            if(item.is(metal.getTag())) return metal.getColor();
        }
        return Color.WHITE;
    }

    public static void giveItemToPlayer(ItemStack item, Player player, boolean pickupSound) {
        int count = item.getCount();
        if(!player.getInventory().add(item)) {
            player.drop(item, false, true);
        }
        if(pickupSound && item.getCount() != count) {
            player.level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
    }

    public static Set<Item> getAllLootItems(ResourceLocation tableLoc, ServerLevel level) {
        LootTable table = level.getServer().getLootTables().get(tableLoc);
        Set<Item> items = new ObjectArraySet<>();
        //Forced to use reflection here because the pools field of this class breaks upon downloading sources because...? No one knows and I don't care anymore
        for(LootPool pool : ((List<LootPool>) ObfuscationReflectionHelper.getPrivateValue(LootTable.class, table, "f_79109_"))) {
            for(LootPoolEntryContainer entry : pool.entries) {
                if(entry instanceof LootItem lootItem) {
                    items.add(lootItem.item);
                }
                else if(entry instanceof TagEntry tagEntry) {
                    for(Item item : ForgeRegistries.ITEMS.tags().getTag(tagEntry.tag)) {
                        items.add(item);
                    }
                }
                else if (entry instanceof LootTableReference ref) {
                    items.addAll(getAllLootItems(ref.name, level));
                }
            }
        }
        return items;
    }
}
