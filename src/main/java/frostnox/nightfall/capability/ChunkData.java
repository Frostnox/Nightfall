package frostnox.nightfall.capability;

import com.google.common.collect.ImmutableSet;
import frostnox.nightfall.block.IDropsItems;
import frostnox.nightfall.block.IFallable;
import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import frostnox.nightfall.network.message.world.ChunkClimateToClient;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.DataUtil;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.data.WrappedBool;
import frostnox.nightfall.world.ContinentalWorldType;
import frostnox.nightfall.world.Season;
import frostnox.nightfall.world.Weather;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import frostnox.nightfall.world.spawngroup.SpawnGroup;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.tags.ITag;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ChunkData implements IChunkData {
    public static final Capability<IChunkData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {}); //Reference to manager instance
    private final LevelChunk chunk;
    private final Level level;
    private final boolean clientSide;
    private long lastTickingGameTime = Long.MAX_VALUE, lastLoadedDayTime;
    private int ticksToSpawn = 0;
    private boolean isNew = true, spawnedUndead;
    private final float[] temperature = new float[256];
    private final float[] humidity = new float[256];
    private final float[] weather = new float[256];
    private final Set<BlockPos> physicsTicks, wardingEffigies;
    private final Set<ObjectObjectImmutablePair<TickPriority, BlockPos>> timeSimulatables;
    private final Set<UUID> undeadIds;

    public ChunkData(LevelChunk chunk) {
        this.chunk = chunk;
        this.level = chunk.getLevel();
        this.clientSide = chunk.getLevel().isClientSide;
        if(clientSide) {
            physicsTicks = null;
            wardingEffigies = null;
            timeSimulatables = null;
            undeadIds = null;
        }
        else {
            Hash.Strategy<BlockPos> compactPosHash = new Hash.Strategy<>() {
                @Override
                public int hashCode(BlockPos pos) {
                    return DataUtil.hashPos(pos);
                }

                @Override
                public boolean equals(BlockPos pos1, BlockPos pos2) {
                    if(pos1 == null || pos2 == null) return pos1 == pos2;
                    else return pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY() && pos1.getZ() == pos2.getZ();
                }
            };
            physicsTicks = new ObjectOpenCustomHashSet<>(8, compactPosHash);
            wardingEffigies = new ObjectOpenCustomHashSet<>(1, compactPosHash);
            timeSimulatables = new ObjectRBTreeSet<>((p1, p2) -> {
                int priority = Integer.compare(p1.key().getValue(), p2.key().getValue());
                if(priority == 0) return Integer.compare(pseudoRandom(p1.value().hashCode()), pseudoRandom(p2.value().hashCode()));
                else return priority;
            });
            undeadIds = new ObjectOpenHashSet<>();
        }
    }

    private static int pseudoRandom(int val) {
        //Seems good from limited testing, but if patterns arise in ordering this is why
        val = (val ^ 0x5a5a5a5a) * 0x45d9f3b;
        return (val ^ (val >> 16));
    }

    private void init() {
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                if(x % 5 == 0 && z % 5 == 0) continue;
                int minX = (x / 5) * 5, maxX = Math.min(15, minX + 5), minZ = (z / 5) * 5, maxZ = Math.min(15, minZ + 5);
                float xAmount = (x % 5) / 5F, zAmount = (z % 5) / 5F;
                setTemperature(x, z, MathUtil.lerp2D(xAmount, zAmount, getBaseTemperature(minX, minZ),
                        getBaseTemperature(maxX, minZ), getBaseTemperature(minX, maxZ), getBaseTemperature(maxX, maxZ)));
                setHumidity(x, z, MathUtil.lerp2D(xAmount, zAmount, getHumidity(minX, minZ),
                        getHumidity(maxX, minZ), getHumidity(minX, maxZ), getHumidity(maxX, maxZ)));
            }
        }

        for(int i = 0; i < weather.length; i++) {
            float temp = temperature[i] - 0.5F;
            weather[i] = humidity[i] * (1F - 2F * temp * temp) * 0.3F; //0 to 0.3
        }
    }

    @Override
    public LevelChunk getChunk() {
        return chunk;
    }

    /**
     * @return true if chunk was loaded for the first time
     */
    @Override
    public boolean isNew() {
        return isNew;
    }

    /**
     * Set new flag to false
     */
    @Override
    public void setOld() {
        if(isNew) init();
        isNew = false;
    }

    @Override
    public ChunkClimateToClient createClimateMessageToClient() {
        return new ChunkClimateToClient(chunk, temperature, humidity);
    }

    @Override
    public float getTemperature(BlockPos pos) {
        if(pos.getY() < ContinentalChunkGenerator.SEA_LEVEL - 64) return 0.5F;
        float temp = getBaseTemperature(pos.getX(), pos.getZ());
        if(pos.getY() > 624) return temp - Math.min((pos.getY() - 624) / 208F * 0.3F, 0.3F);
        else if(pos.getY() < ContinentalChunkGenerator.SEA_LEVEL - 32) return Mth.lerp(Math.abs(pos.getY() - (ContinentalChunkGenerator.SEA_LEVEL - 32)) / 32F, temp, 0.5F);
        else return temp;
    }

    @Override
    public float getBaseTemperature(int x, int z) {
        return temperature[((x < 0 ? ((x % 16) + 16) : x) % 16) * 16 + ((z < 0 ? ((z % 16) + 16) : z) % 16)];
    }

    @Override
    public float getHumidity(BlockPos pos) {
        return getHumidity(pos.getX(), pos.getZ());
    }

    @Override
    public float getHumidity(int x, int z) {
        return humidity[((x < 0 ? ((x % 16) + 16) : x) % 16) * 16 + ((z < 0 ? ((z % 16) + 16) : z) % 16)];
    }

    @Override
    public float getWeatherAddend(int x, int z) {
        return weather[((x < 0 ? ((x % 16) + 16) : x) % 16) * 16 + ((z < 0 ? ((z % 16) + 16) : z) % 16)];
    }

    @Override
    public void setTemperature(int x, int z, float temperature) {
        this.temperature[x * 16 + z] = temperature;
    }

    @Override
    public void setHumidity(int x, int z, float humidity) {
        this.humidity[x * 16 + z] = humidity;
    }

    @Override
    public boolean hasSpawnedUndead() {
        return spawnedUndead;
    }

    @Override
    public void setSpawnedUndead(boolean spawned) {
        spawnedUndead = spawned;
    }

    @Override
    public long getLastTickingGameTime() {
        return lastTickingGameTime;
    }

    @Override
    public void setLastTickingGameTime(long time) {
        lastTickingGameTime = time;
    }

    @Override
    public long getLastLoadedDayTime() {
        return lastLoadedDayTime;
    }

    @Override
    public void setLastLoadedDayTime(long time) {
        lastLoadedDayTime = time;
    }

    @Override
    public void tickPhysics() {
        if(physicsTicks.isEmpty()) return;
        Set<BlockPos> processedTicks = ImmutableSet.copyOf(physicsTicks);
        physicsTicks.clear();
        for(BlockPos pos : processedTicks) {
            BlockState state = chunk.getBlockState(pos);
            boolean floats = state.is(TagsNF.FLOATS);
            int maxDist = 0;
            if(state.is(TagsNF.SUPPORT_1)) maxDist += 1;
            if(state.is(TagsNF.SUPPORT_2)) maxDist += 2;
            if(state.is(TagsNF.SUPPORT_4)) maxDist += 4;
            if(state.is(TagsNF.SUPPORT_8)) maxDist += 8;
            WrappedBool fell = new WrappedBool(true);
            if(maxDist == 0) {
                if(!state.getCollisionShape(level, pos).getFaceShape(Direction.DOWN).isEmpty()) {
                    BlockPos belowPos = pos.below();
                    BlockState belowState = chunk.getBlockState(belowPos);
                    if(!LevelUtil.canFallThrough(belowState) || (floats && belowState.getBlock() instanceof LiquidBlock)) fell.val = false;
                }
            }
            else {
                Object2FloatOpenHashMap<BlockPos> visited = new Object2FloatOpenHashMap<>(maxDist * 2);
                visited.defaultReturnValue(Float.MAX_VALUE);
                checkForSupport(pos, state, visited, fell, 0, maxDist, floats, pos);
            }
            if(fell.val) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if(state.getBlock() instanceof IFallable fallable) fallable.onFall(state, (ServerLevel) level, pos, blockEntity);
                if(blockEntity instanceof IDropsItems droppable && droppable.dropOnFall()) {
                    NonNullList<ItemStack> drops = droppable.getContainerDrops();
                    Containers.dropContents(level, pos, drops);
                    drops.clear();
                }
                CompoundTag data = blockEntity != null ? blockEntity.saveWithId() : null;
                if(blockEntity instanceof Clearable clearable) clearable.clearContent();
                MovingBlockEntity block = MovingBlockEntity.fall(level, pos, state);
                block.blockData = data;
                schedulePhysicsTickAround(pos);
            }
        }
    }

    @Override
    public void schedulePhysicsTick(BlockPos pos) {
        physicsTicks.add(pos);
    }

    @Override
    public void schedulePhysicsTickAround(BlockPos pos) {
        int chunkX = pos.getX() & 15, chunkZ = pos.getZ() & 15;
        for(Direction dir : LevelUtil.PHYSICS_DIRECTIONS) {
            BlockPos neighborPos = pos.relative(dir);
            if(physicsTicks.contains(neighborPos)) continue;
            BlockState neighbor = level.getBlockState(neighborPos);
            if(neighbor.is(TagsNF.HAS_PHYSICS)) {
                if(Math.abs(chunkX - (neighborPos.getX() & 15)) > 1 || Math.abs(chunkZ - (neighborPos.getZ() & 15)) > 1) {
                    ChunkData.get(level.getChunkAt(neighborPos)).schedulePhysicsTick(neighborPos);
                }
                else schedulePhysicsTick(neighborPos);
            }
        }
    }

    private void tryEntitySpawn(boolean spawnFriendlies, boolean spawnEnemies, BiFunction<ServerLevel, LevelChunk, BlockPos> centerPosProvider, TagKey<SpawnGroup> spawnTag, Function<BlockPos, BlockPos> poolPosProvider) {
        ServerLevel level = (ServerLevel) this.level;
        BlockPos centerPos = centerPosProvider.apply(level, chunk);
        if(centerPos == null) return;
        double spawnX = centerPos.getX() + 0.5, spawnY = centerPos.getY(), spawnZ = centerPos.getZ() + 0.5;
        if(LevelUtil.getShortestDistanceSqrToPlayer(level, spawnX, spawnY, spawnZ) > 80 * 80) {
            //Generate possible groups
            ITag<SpawnGroup> tag = RegistriesNF.getSpawnGroups().tags().getTag(spawnTag);
            Object2IntArrayMap<SpawnGroup> weightedGroups = new Object2IntArrayMap<>(tag.size());
            int totalWeight = 0;
            BlockPos belowPos = centerPos.below();
            BlockState blockBelow = chunk.getBlockState(belowPos);
            BlockState blockAt = chunk.getBlockState(centerPos);
            int skyLight = level.getBrightness(LightLayer.SKY, centerPos);
            float temperature = getTemperature(centerPos);
            float humidity = getHumidity(centerPos);
            for(Iterator<SpawnGroup> it = tag.iterator(); it.hasNext();) {
                SpawnGroup group = it.next();
                if((spawnFriendlies && group.isFriendly()) || (spawnEnemies && !group.isFriendly())) {
                    if(group.canSpawnAt(level, centerPos, group.getPlacementType() == SpawnPlacements.Type.ON_GROUND ? blockBelow : blockAt, skyLight, temperature, humidity)) {
                        totalWeight += group.getWeight();
                        weightedGroups.put(group, group.getWeight());
                    }
                }
            }
            //Pick group
            if(totalWeight > 0) {
                SpawnGroup group = null;
                int weight = 0;
                int rand = level.random.nextInt(totalWeight);
                for(var entry : weightedGroups.object2IntEntrySet()) {
                    weight += entry.getIntValue();
                    if(rand < weight) {
                        group = entry.getKey();
                        break;
                    }
                }
                if(group != null) {
                    EntityType<?>[] types = group.createGroup(level, centerPos, blockBelow, skyLight, temperature, humidity);
                    SpawnGroupData data = group.getGroupData(level, centerPos, blockBelow, skyLight, temperature, humidity, types.length);
                    //Create pool of valid positions to spawn at
                    ObjectArrayList<BlockPos> openPositions = ObjectArrayList.of();
                    for(int x = -3; x <= 3; x++) {
                        for(int z = -3; z <= 3; z++) {
                            int xPos = centerPos.getX() + x, zPos = centerPos.getZ() + z;
                            if(!level.hasChunkAt(xPos, zPos)) continue;
                            BlockPos pos = poolPosProvider.apply(new BlockPos(xPos, centerPos.getY(), zPos));
                            openPositions.add(pos);
                        }
                    }
                    Collections.shuffle(openPositions, level.random);
                    boolean spawnedAny = false;
                    //Spawn entities
                    for(int i = 0; i < types.length; i++) {
                        if(openPositions.isEmpty()) break;
                        EntityType<?> type = types[i];
                        Entity entity = type.create(level);
                        if(!(entity instanceof PathfinderMob mob) || mob.checkSpawnRules(level, MobSpawnType.NATURAL)) {
                            for(BlockPos pos : openPositions) {
                                if(NaturalSpawner.isSpawnPositionOk(group.getPlacementType(), level, pos, type)) {
                                    entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                                    if(entity instanceof Mob mob && !mob.checkSpawnObstruction(level)) continue;
                                    entity.moveTo(pos, level.random.nextFloat() * 360, 0);
                                    entity.setYBodyRot(entity.getYRot());
                                    entity.setYHeadRot(entity.getYRot());
                                    if(entity instanceof ActionableEntity actionable) actionable.noDespawnTicks = ticksToSpawn;
                                    if(entity instanceof PathfinderMob mob) mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, data, null);
                                    level.addFreshEntityWithPassengers(entity);
                                    spawnedAny = true;
                                    openPositions.remove(pos);
                                    break;
                                }
                            }
                        }
                    }
                    if(spawnedAny) chunk.setUnsaved(true);
                }
            }
        }
    }

    @Override
    public void tryEntitySpawn(boolean spawnFriendlies, boolean spawnEnemies) {
        if(!level.isClientSide) {
            ticksToSpawn--;
            if(ticksToSpawn <= 0) {
                ticksToSpawn = (int) (ContinentalWorldType.DAY_LENGTH * 1.5F) + level.random.nextInt((int) ContinentalWorldType.DAY_LENGTH);
                ILevelData levelData = LevelData.get(level);
                float temp = temperature[7 * 16 + 7] + Season.getTemperatureInfluence(levelData.getSeasonTime());
                float hum = humidity[7 * 16 + 7];
                float spawnChance = 0.0045F + Mth.sqrt(temp * temp + hum * hum) * (0.0045F / 5F);
                //Surface
                if(level.random.nextFloat() < spawnChance) {
                    tryEntitySpawn(spawnFriendlies, spawnEnemies, LevelUtil::getRandomSurfacePos, TagsNF.SURFACE_GROUPS,
                            (pos) -> new BlockPos(pos.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()), pos.getZ())
                    );
                }
                //Ocean (floor to surface)
                if(level.random.nextFloat() < spawnChance * 8) { //Increase chance since ocean is very large
                    tryEntitySpawn(spawnFriendlies, spawnEnemies, LevelUtil::getRandomWaterPos, TagsNF.OCEAN_GROUPS,
                            (pos) -> pos);
                }
                //Freshwater (floor to surface)
                if(level.random.nextFloat() < spawnChance) {
                    tryEntitySpawn(spawnFriendlies, spawnEnemies, LevelUtil::getRandomWaterPos, TagsNF.FRESHWATER_GROUPS,
                            (pos) -> pos);
                }
                //Random (completely random)
                for(int i = 0; i < 10; i++) {
                    tryEntitySpawn(spawnFriendlies, spawnEnemies, LevelUtil::getRandomPos, TagsNF.RANDOM_GROUPS,
                            (pos) -> pos);
                }
            }
        }
    }

    @Override
    public void addWardingEffigy(BlockPos pos) {
        wardingEffigies.add(pos);
    }

    @Override
    public void removeWardingEffigy(BlockPos pos) {
        wardingEffigies.remove(pos);
    }

    @Override
    public boolean isUndeadSpawnBlocked(BlockPos spawnPos) {
        for(BlockPos pos : wardingEffigies) {
            if(Math.abs(pos.getX() - spawnPos.getX()) < 16 && Math.abs(pos.getY() - spawnPos.getY()) < 16 && Math.abs(pos.getZ() - spawnPos.getZ()) < 16) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void simulateTime(long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        ServerLevel serverLevel = (ServerLevel) level;
        GlobalChunkData.get(chunk).simulateBreakProgress(elapsedTime);
        for(var pair : timeSimulatables) {
            BlockPos pos = pair.right();
            BlockState state = chunk.getBlockState(pos);
            if(state.getBlock() instanceof ITimeSimulatedBlock simulatable) {
                simulatable.simulateTime(serverLevel, chunk, this, pos, state, elapsedTime, gameTime, dayTime, seasonTime, seasonalTemp, randomTickChance, random);
            }
            else {
                timeSimulatables.remove(pair);
            }
        }
        //Snow & frazil simulation
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        //Only simulate up to one year
        if(elapsedTime == Long.MAX_VALUE) elapsedTime = Season.YEAR_LENGTH;
        else {
            elapsedTime = elapsedTime % Season.YEAR_LENGTH;
            if(elapsedTime == 0) elapsedTime = Season.YEAR_LENGTH;
        }
        int chunkX = chunk.getPos().getMinBlockX(), chunkZ = chunk.getPos().getMinBlockZ();
        ILevelData levelData = LevelData.get(level);
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                int surfaceY = y + 1;
                BlockState surfaceBlock = chunk.getBlockState(pos.set(x, y, z));
                FluidState surfaceFluid = surfaceBlock.getFluidState();
                float temp = temperature[x * 16 + z];
                if(surfaceFluid.isSource()) {
                    Block block = surfaceBlock.getBlock();
                    if(block == BlocksNF.WATER.get() || block == BlocksNF.SEAWATER.get()) {
                        boolean fresh = block == BlocksNF.WATER.get();
                        BlockState ice = fresh ? BlocksNF.ICE.get().defaultBlockState() : BlocksNF.SEA_ICE.get().defaultBlockState();
                        BlockState frazil = fresh ? BlocksNF.FRAZIL.get().defaultBlockState() : BlocksNF.SEA_FRAZIL.get().defaultBlockState();
                        float seasonalFrazilTemp = (fresh ? LevelData.FRAZIL_TEMP : LevelData.SEA_FRAZIL_TEMP) - temp;
                        if(Math.abs(seasonalFrazilTemp) < Season.TEMP_INFLUENCE) {
                            LongLongPair frazilWindow = Season.getTimesAtTemperatureInfluence(seasonalFrazilTemp);
                            //Ensure frazil or ice wouldn't have melted (both melt at frazil temp)
                            if(seasonalTemp > seasonalFrazilTemp && MathUtil.getRandomSuccesses(randomTickChance, Season.getTimePassedWithin(
                                    seasonTime, elapsedTime, frazilWindow.secondLong(), seasonTime % Season.YEAR_LENGTH), 1, random) >= 1) continue;
                            float seasonalIceTemp = (fresh ? LevelData.WATER_FREEZE_TEMP : LevelData.SEAWATER_FREEZE_TEMP) - temp;
                            if(Math.abs(seasonalIceTemp) < Season.TEMP_INFLUENCE) {
                                LongLongPair iceWindow = Season.getTimesAtTemperatureInfluence(seasonalIceTemp);
                                //Pre-ice frazil is before and during the ice window
                                long preFrazilTime = Season.getTimePassedWithin(seasonTime, elapsedTime, iceWindow.firstLong(), frazilWindow.secondLong());
                                IntLongPair preFrazil = MathUtil.getRandomSuccessesAndRemainingTrials(4D/52D/256D, preFrazilTime, 1, random);
                                if(preFrazil.firstInt() == 1) {
                                    //Ice is always in one window
                                    long iceTime = Season.getTimePassedWithin(seasonTime, elapsedTime, iceWindow.firstLong(), iceWindow.secondLong());
                                    //If frazil formed inside ice window, remaining time will be shorter than ice time
                                    if(MathUtil.getRandomSuccesses(randomTickChance, Math.min(preFrazil.secondLong(), iceTime), 1, random) >= 1) {
                                        level.setBlock(pos.set(chunkX + x, y, chunkZ + z), ice, 18);
                                    }
                                    else level.setBlock(pos.set(chunkX + x, surfaceY, chunkZ + z), frazil, 18);
                                }
                                //Try post-ice frazil only if ice or frazil hasn't formed yet
                                else if(MathUtil.getRandomSuccesses(4D/52D/256D, Season.getTimePassedWithin(
                                        seasonTime, elapsedTime, frazilWindow.firstLong(), iceWindow.firstLong()), 1, random) >= 1) {
                                    level.setBlock(pos.set(chunkX + x, surfaceY, chunkZ + z), frazil, 18);
                                }
                            }
                            else {
                                //No ice, so frazil is one window
                                if(MathUtil.getRandomSuccesses(4D/52D/256D, Season.getTimePassedWithin(
                                        seasonTime, elapsedTime, frazilWindow.firstLong(), frazilWindow.secondLong()), 1, random) >= 1) {
                                    level.setBlock(pos.set(chunkX + x, surfaceY, chunkZ + z), frazil, 18);
                                }
                            }
                        }
                    }
                }
                else {
                    if(!surfaceBlock.is(BlockTags.ICE) && chunk.getBlockState(pos.setY(surfaceY)).isAir() &&
                            Block.isFaceFull(surfaceBlock.getBlockSupportShape(level, pos.setY(y)), Direction.UP)) {
                        float seasonalSnowTemp = LevelData.SNOW_TEMP - temp;
                        //Cold enough to always snow
                        if(seasonalSnowTemp >= Season.TEMP_INFLUENCE) {
                            if(MathUtil.getRandomSuccesses(4D/52D/256D, elapsedTime, 1, random) >= 1) {
                                level.setBlock(pos.set(chunkX + x, surfaceY, chunkZ + z), BlocksNF.SNOW.get().defaultBlockState(), 18);
                            }
                        }
                        else if(Math.abs(seasonalSnowTemp) < Season.TEMP_INFLUENCE) {
                            LongLongPair snowWindow = Season.getTimesAtTemperatureInfluence(seasonalSnowTemp);
                            //Check for future melting
                            if(seasonalTemp > seasonalSnowTemp && MathUtil.getRandomSuccesses(randomTickChance, Season.getTimePassedWithin(
                                    seasonTime, elapsedTime, snowWindow.secondLong(), seasonTime % Season.YEAR_LENGTH), 1, random) >= 1) continue;
                            //Check that snow placed
                            long snowTime = Season.getTimePassedWithin(seasonTime, elapsedTime, snowWindow.firstLong(), snowWindow.secondLong());
                            if(snowTime > 0) {
                                long snowWindowStart = (seasonTime % Season.YEAR_LENGTH) - Math.floorMod(snowWindow.secondLong(), Season.YEAR_LENGTH);
                                double snowChance = levelData.getWeatherPercentageAboveIntensityOverTime(Weather.GLOBAL_CLOUDS_THRESHOLD - weather[x * 16 + z],
                                        Math.max(0, snowWindowStart), snowWindowStart + snowTime);
                                if(MathUtil.getRandomSuccesses(4D/52D/256D * snowChance, snowTime, 1, random) >= 1) {
                                    level.setBlock(pos.set(chunkX + x, surfaceY, chunkZ + z), BlocksNF.SNOW.get().defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addSimulatableBlock(TickPriority priority, BlockPos pos) {
        timeSimulatables.add(new ObjectObjectImmutablePair<>(priority, pos) {
            @Override
            public int hashCode() {
                return right.hashCode();
            }
        });
    }

    @Override
    public void removeSimulatableBlock(TickPriority priority, BlockPos pos) {
        timeSimulatables.remove(new ObjectObjectImmutablePair<>(priority, pos));
    }

    @Override
    public double getWeatherPercentageAboveIntensityOverTime(ILevelData levelData, BlockPos pos, float intensity, long startTime, long endTime) {
        return levelData.getWeatherPercentageAboveIntensityOverTime(intensity - getWeatherAddend(pos.getX(), pos.getZ()), startTime, endTime);
    }

    @Override
    public void addUndeadUUID(UUID id) {
        undeadIds.add(id);
    }

    @Override
    public void clearUndeadUUIDs() {
        undeadIds.clear();
    }

    @Override
    public boolean areUndeadLoaded() {
        if(level instanceof ServerLevel serverLevel) {
            for(UUID id : undeadIds) {
                if(serverLevel.getEntity(id) != null) return true;
            }
        }
        return false;
    }

    private void checkForSupport(BlockPos pos, BlockState state, Object2FloatOpenHashMap<BlockPos> visited, WrappedBool fell, float dist, int maxDist, boolean floats, BlockPos origin) {
        if(!fell.val || dist >= visited.getFloat(pos) || dist > maxDist) return;
        visited.put(pos, dist);
        BlockPos belowPos = pos.below();
        VoxelShape shape = state.getCollisionShape(level, pos);
        if(!visited.containsKey(belowPos) && !shape.getFaceShape(Direction.DOWN).isEmpty()) {
            if(floats && level.getBlockState(belowPos).getBlock() instanceof LiquidBlock) {
                fell.val = false;
                return;
            }
            if(pos.equals(origin)) {
                if(!LevelUtil.canFallThrough(level.getBlockState(belowPos))) {
                    fell.val = false;
                    return;
                }
            }
        }
        for(Direction dir : LevelUtil.HORIZONTAL_DOWN_DIRECTIONS) {
            VoxelShape face = shape.getFaceShape(dir);
            if(face.isEmpty()) continue;
            BlockPos supportPos = pos.relative(dir);
            BlockState supportState = level.getBlockState(supportPos);
            VoxelShape shapeS = supportState.getCollisionShape(level, supportPos);
            if(!shapeS.getFaceShape(dir.getOpposite()).isEmpty()) {
                boolean joined = false;
                //Treat center of face as center of mass
                Direction.Axis otherAxis = dir.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
                double xzCenter = (face.min(otherAxis) + face.max(otherAxis)) / 2D;
                double yCenter = (face.min(Direction.Axis.Y) + face.max(Direction.Axis.Y)) / 2D;
                //Check if any touching faces contain center of mass
                for(AABB box : shapeS.toAabbs()) {
                    if(dir.getAxisDirection() != Direction.AxisDirection.POSITIVE) {
                        if(box.max(dir.getAxis()) < 1D) continue;
                    }
                    else if(box.min(dir.getAxis()) > 0D) continue;
                    if(xzCenter >= box.min(otherAxis) && xzCenter <= box.max(otherAxis) && yCenter >= box.minY && yCenter <= box.maxY) {
                        joined = true;
                        break;
                    }
                }
                if(joined) {
                    if(dir == Direction.DOWN) {
                        fell.val = false;
                        return;
                    }
                    checkForSupport(supportPos, supportState, visited, fell,
                            supportState.hasProperty(BlockStateProperties.AXIS) && supportState.getValue(BlockStateProperties.AXIS) == dir.getAxis() ? (dist + 0.666F) : (dist + 1),
                            maxDist, floats, origin);
                    if(!fell.val) return;
                }
            }
        }
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag temps = new ListTag(), hums = new ListTag();
        //Compress temperature/humidity data since most of it can be recreated
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                if(x % 5 == 0 && z % 5 == 0) {
                    int i = x * 16 + z;
                    temps.add(FloatTag.valueOf(temperature[i]));
                    hums.add(FloatTag.valueOf(humidity[i]));
                }
            }
        }
        tag.put("temperature", temps);
        tag.put("humidity", hums);

        tag.putBoolean("new", isNew);
        tag.putBoolean("undead", spawnedUndead);
        tag.putLong("lastTickingGameTime", lastTickingGameTime);
        tag.putLong("loadDay", lastLoadedDayTime);
        tag.putInt("ticksToSpawn", ticksToSpawn);

        if(!physicsTicks.isEmpty()) {
            ListTag hashes = new ListTag();
            for(BlockPos pos : physicsTicks) {
                hashes.add(IntTag.valueOf(((pos.getX() & 15) << 22) | ((pos.getZ() & 15) << 12) | (pos.getY() & 4095)));
            }
            tag.put("physicsTicks", hashes);
        }
        if(!wardingEffigies.isEmpty()) {
            ListTag hashes = new ListTag();
            for(BlockPos pos : wardingEffigies) {
                hashes.add(IntTag.valueOf(((pos.getX() & 15) << 22) | ((pos.getZ() & 15) << 12) | (pos.getY() & 4095)));
            }
            tag.put("wardingEffigies", hashes);
        }
        if(!timeSimulatables.isEmpty()) {
            EnumMap<TickPriority, ListTag> priorityToTag = new EnumMap<>(TickPriority.class);
            for(var pair : timeSimulatables) {
                ListTag listTag = priorityToTag.computeIfAbsent(pair.key(), k -> new ListTag());
                BlockPos pos = pair.right();
                listTag.add(IntTag.valueOf(((pos.getX() & 15) << 22) | ((pos.getZ() & 15) << 12) | (pos.getY() & 4095)));
            }
            for(var entry : priorityToTag.entrySet()) tag.put("timeSimulatables" + entry.getKey().getValue(), entry.getValue());
        }
        if(!undeadIds.isEmpty()) {
            ListTag ids = new ListTag();
            for(UUID id : undeadIds) ids.add(NbtUtils.createUUID(id));
            tag.put("undeadIds", ids);
        }
        return tag;
    }

    @Override
    public void readNBT(CompoundTag tag) {
        ListTag temps = tag.getList("temperature", ListTag.TAG_FLOAT);
        ListTag hums = tag.getList("humidity", ListTag.TAG_FLOAT);
        int tagIndex = 0;
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                if(x % 5 == 0 && z % 5 == 0) {
                    int i = x * 16 + z;
                    temperature[i] = ((FloatTag) temps.get(tagIndex)).getAsFloat();
                    humidity[i] = ((FloatTag) hums.get(tagIndex)).getAsFloat();
                    tagIndex++;
                }
            }
        }

        if(tag.contains("new", Tag.TAG_BYTE)) isNew = tag.getBoolean("new");
        spawnedUndead = tag.getBoolean("undead");
        if(tag.contains("lastTickingGameTime")) lastTickingGameTime = tag.getLong("lastTickingGameTime");
        lastLoadedDayTime = tag.getLong("loadDay");
        ticksToSpawn = tag.getInt("ticksToSpawn");

        int minX = chunk.getPos().getMinBlockX(), minZ = chunk.getPos().getMinBlockZ();
        if(tag.contains("physicsTicks")) {
            ListTag hashes = tag.getList("physicsTicks", ListTag.TAG_INT);
            for(Tag hashTag : hashes) {
                int hash = ((IntTag) hashTag).getAsInt();
                physicsTicks.add(new BlockPos(minX + (hash >>> 22), hash & 4095, minZ + ((hash >>> 12) & 1023)));
            }
        }
        if(tag.contains("wardingEffigies")) {
            ListTag hashes = tag.getList("wardingEffigies", ListTag.TAG_INT);
            for(Tag hashTag : hashes) {
                int hash = ((IntTag) hashTag).getAsInt();
                wardingEffigies.add(new BlockPos(minX + (hash >>> 22), hash & 4095, minZ + ((hash >>> 12) & 1023)));
            }
        }
        for(TickPriority tickPriority : TickPriority.values()) {
            String key = "timeSimulatables" + tickPriority.getValue();
            if(tag.contains(key)) {
                ListTag hashes = tag.getList(key, ListTag.TAG_INT);
                for(Tag hashTag : hashes) {
                    int hash = ((IntTag) hashTag).getAsInt();
                    timeSimulatables.add(new ObjectObjectImmutablePair<>(tickPriority,
                            new BlockPos(minX + (hash >>> 22), hash & 4095, minZ + ((hash >>> 12) & 1023))));
                }
            }
        }
        if(tag.contains("undeadIds")) {
            ListTag ids = tag.getList("undeadIds", ListTag.TAG_INT_ARRAY);
            for(Tag idTag : ids) {
                undeadIds.add(NbtUtils.loadUUID(idTag));
            }
        }

        init(); //Populate unsaved fields
    }

    public static IChunkData get(LevelChunk chunk) {
        return chunk.getCapability(CAPABILITY, null).orElseThrow(() -> new IllegalArgumentException("Null in LazyOptional."));
    }

    public static boolean isPresent(LevelChunk chunk) {
        return chunk.getCapability(CAPABILITY).isPresent();
    }

    public static class ChunkDataCapability implements ICapabilitySerializable<CompoundTag> {
        private final ChunkData cap;
        private final LazyOptional<IChunkData> holder;

        public ChunkDataCapability(LevelChunk chunk) {
            cap = new ChunkData(chunk);
            holder = LazyOptional.of(() -> cap);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> c, Direction side) {
            return CAPABILITY == c ? (LazyOptional<T>) holder : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return cap.writeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag NBT) {
            cap.readNBT(NBT);
        }
    }
}
