package frostnox.nightfall.mixin;

import frostnox.nightfall.capability.*;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.data.TickingChunk;
import frostnox.nightfall.world.MoonPhase;
import frostnox.nightfall.world.Season;
import frostnox.nightfall.world.Weather;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin extends ChunkSource {
    @Shadow @Final ServerLevel level;
    @Shadow @Final ChunkMap chunkMap;
    @Shadow long lastInhabitedUpdate;
    @Shadow boolean spawnEnemies;
    @Shadow boolean spawnFriendlies;
    @Unique private static final int RANDOM_SECTIONS_DIVISOR = 4;
    @Unique private static final BlockState SNOW_LAYER = BlocksNF.SNOW.get().defaultBlockState();

    /**
     * Replace chunk ticking for Nightfall levels with custom function since layering on top via events has a big impact
     * on performance. Random ticks are altered to always run if the chunk is loaded (instead of only when players are near) and
     * entity spawning is handled intermittently instead of constantly.
     */
    @Inject(method = "tickChunks", at = @At(value = "HEAD"), cancellable = true)
    private void nightfall$tickChunks(CallbackInfo callbackInfo) {
        if(LevelData.isPresent(level)) {
            callbackInfo.cancel();
            ILevelData capL = LevelData.get(level);
            long gameTime = level.getGameTime();
            long timePassed = gameTime - lastInhabitedUpdate;
            lastInhabitedUpdate = gameTime;
            long simGameTime = gameTime - 1;
            long simSeasonTime = capL.getSeasonTime() - 1;
            float seasonalTemp = Season.getTemperatureInfluence(simSeasonTime);
            long simDayTime = level.getDayTime() - 1;
            boolean isDay = LevelUtil.isDay(level);
            boolean raining = level.isRaining();
            boolean thundering = raining && level.isThundering();
            MoonPhase moonPhase = MoonPhase.get(level);
            int spawnsMod = 4;
            if(moonPhase == MoonPhase.WANING_CRESCENT || moonPhase == MoonPhase.WAXING_CRESCENT) spawnsMod--;
            else if(moonPhase == MoonPhase.WANING_GIBBOUS || moonPhase == MoonPhase.WAXING_GIBBOUS || moonPhase == MoonPhase.FULL) spawnsMod++;
            Random random = level.random;
            int randomSectionsCount = level.getSectionsCount() / RANDOM_SECTIONS_DIVISOR;
            int randomTickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
            //Chance for any given block to be randomly ticked is (P(section) = 1/4) * (P(picked) = 1 - P(not picked) = 1 - (1 - 1 / (16 * 16 * 16))^ticks)
            //This includes cases where the block was chosen multiple times in a single chunk tick, but we just treat this as one random tick
            double randomTickChance = (1D - Math.pow(1D - 1D / (16D * 16D * 16D), randomTickSpeed)) / RANDOM_SECTIONS_DIVISOR;
            boolean mobSpawning = level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
            boolean spawnEnemies = mobSpawning && this.spawnEnemies, spawnFriendlies = mobSpawning && this.spawnFriendlies;
            int loadedChunks = getLoadedChunksCount();
            List<TickingChunk> chunks = new ObjectArrayList<>(loadedChunks);
            for(ChunkHolder chunkHolder : level.getChunkSource().chunkMap.getChunks()) {
                LevelChunk tickingChunk = chunkHolder.getTickingChunk();
                if(tickingChunk != null) {
                    chunks.add(new TickingChunk(tickingChunk, chunkHolder, ChunkData.get(tickingChunk), GlobalChunkData.get(tickingChunk)));
                }
            }
            Collections.shuffle(chunks);
            //Simulate time for chunks that started ticking again
            for(TickingChunk tickingChunk : chunks) {
                LevelChunk chunk = tickingChunk.chunk;
                chunk.incrementInhabitedTime(timePassed);
                IChunkData capC = tickingChunk.chunkData;
                long lastTickTime = capC.getLastTickingGameTime();
                long elapsedTime = lastTickTime == Long.MAX_VALUE ? lastTickTime : (simGameTime - lastTickTime);
                if(elapsedTime > 0L) capC.simulateTime(elapsedTime, simGameTime, simDayTime, simSeasonTime, seasonalTemp, randomTickChance, random);
                capC.setLastTickingGameTime(gameTime);
            }
            //Tick chunks
            for(TickingChunk tickingChunk : chunks) {
                LevelChunk chunk = tickingChunk.chunk;
                ChunkPos chunkPos = tickingChunk.chunkPos;
                int minX = chunkPos.getMinBlockX(), minZ = chunkPos.getMinBlockZ();
                IChunkData capC = tickingChunk.chunkData;
                tickingChunk.globalChunkData.tick();
                //Physics ticks
                capC.tickPhysics();
                //Weather effects
                if(thundering && random.nextInt(1000000) == 0) {
                    BlockPos lightningPos = level.findLightningTargetAround(level.getBlockRandomPos(minX, 0, minZ, 15));
                    level.playSound(null, lightningPos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER,
                            10000.0F, 0.8F + level.random.nextFloat() * 0.2F);
                    //TODO: Lightning (remove above later)
                    /*if(level.isRainingAt(lightningPos)) {
                        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                        lightning.moveTo(Vec3.atBottomCenterOf(lightningPos));
                        level.addFreshEntity(lightning);
                    }*/
                }
                int randI = random.nextInt(52);
                if(randI < 4) {
                    BlockPos randPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, level.getBlockRandomPos(minX, 0, minZ, 15));
                    BlockPos randPosBelow = randPos.below();
                    BlockState belowBlock = level.getBlockState(randPosBelow);
                    FluidState belowFluid = belowBlock.getFluidState();
                    if(belowFluid.isSource()) {
                        float temperature = capL.getSeasonalTemperature(capC, randPosBelow);
                        if(temperature <= LevelData.FRAZIL_TEMP) {
                            Block block = belowBlock.getBlock();
                            if(block == BlocksNF.WATER.get()) level.setBlockAndUpdate(randPos, BlocksNF.FRAZIL.get().defaultBlockState());
                            else if(block == BlocksNF.SEAWATER.get() && temperature <= LevelData.SEA_FRAZIL_TEMP) {
                                level.setBlockAndUpdate(randPos, BlocksNF.SEA_FRAZIL.get().defaultBlockState());
                            }
                        }
                    }
                    else {
                        Weather weather = capL.getWeather(capC, randPos);
                        if(weather.isPrecipitation) {
                            Biome.Precipitation precipitation = switch(weather) {
                                case RAIN -> Biome.Precipitation.RAIN;
                                case SNOW -> Biome.Precipitation.SNOW;
                                default -> Biome.Precipitation.NONE;
                            };
                            if(precipitation == Biome.Precipitation.SNOW && !belowBlock.is(BlockTags.ICE) && level.getBlockState(randPos).isAir() &&
                                    Block.isFaceFull(belowBlock.getBlockSupportShape(level, randPosBelow), Direction.UP)) {
                                level.setBlockAndUpdate(randPos, SNOW_LAYER);
                            }
                            belowBlock.getBlock().handlePrecipitation(belowBlock, level, randPosBelow, precipitation);
                        }
                    }
                }
                //Random ticks
                if(randomTickSpeed > 0) {
                    //Only tick one group of sections at a time to reduce impact on server performance
                    //World height doubles the sections and ticking without nearby players potentially increases this further
                    //This also lowers the tick frequency which was too high in the vanilla game for what Nightfall blocks use it for
                    int sectionOffset = (randI % RANDOM_SECTIONS_DIVISOR) * randomSectionsCount;
                    LevelChunkSection[] sections = chunk.getSections();
                    for(int i = sectionOffset; i < sectionOffset + randomSectionsCount; i++) {
                        LevelChunkSection section = sections[i];
                        if(section.isRandomlyTicking()) {
                            int bottomY = section.bottomBlockY();
                            for(int j = 0; j < randomTickSpeed; j++) {
                                BlockPos randomPos = level.getBlockRandomPos(minX, bottomY, minZ, 15);
                                BlockState block = section.getBlockState(randomPos.getX() - minX, randomPos.getY() - bottomY, randomPos.getZ() - minZ);
                                if(block.isRandomlyTicking()) block.randomTick(level, randomPos, random);
                                FluidState fluid = block.getFluidState();
                                if(fluid.isRandomlyTicking()) fluid.randomTick(level, randomPos, random);
                            }
                        }
                    }
                }
                //Standard entity spawning
                if(mobSpawning) capC.tryEntitySpawn(spawnFriendlies, spawnEnemies);
                //Undead spawning
                if(isDay) {
                    if(capC.hasSpawnedUndead()) {
                        capC.clearUndeadUUIDs();
                        capC.setSpawnedUndead(false);
                        chunk.setUnsaved(true);
                    }
                }
                else if(moonPhase != MoonPhase.NEW && random.nextInt(2048) == 0) {
                    boolean fullMoon = moonPhase == MoonPhase.FULL;
                    BlockPos centerPos = LevelUtil.getRandomSurfacePos(level, chunk);
                    double spawnX = centerPos.getX() + 0.5, spawnY = centerPos.getY(), spawnZ = centerPos.getZ() + 0.5;
                    double maxDist = LevelUtil.getShortestDistanceSqrUndeadMaxToPlayer(level, spawnX, spawnY, spawnZ, fullMoon);
                    if(maxDist < LevelUtil.UNDEAD_MAX_SPAWN_DIST_SQR &&
                            LevelUtil.getShortestDistanceSqrUndeadMinToPlayer(level, spawnX, spawnY, spawnZ, !fullMoon) > LevelUtil.UNDEAD_MIN_SPAWN_DIST_SQR) {
                        if(!capC.hasSpawnedUndead() && random.nextFloat() < (fullMoon ? 0.87F : 0.9F) || (!fullMoon && isUndeadSpawnBlocked(centerPos, capC, chunkPos))) {
                            capC.setSpawnedUndead(true);
                            chunk.setUnsaved(true);
                        }
                        else if((!capC.hasSpawnedUndead() || (fullMoon && maxDist < 64 * 64 && !capC.areUndeadLoaded())) && spawnEnemies) {
                            //Generate group, always include one melee & one ranged
                            ObjectArrayList<EntityType<?>> types = ObjectArrayList.of(EntitiesNF.HUSK.get(), EntitiesNF.SKELETON.get());
                            int supportWeight = 2;
                            for(int i = 0; i < (random.nextInt() & Integer.MAX_VALUE) % spawnsMod; i++) {
                                float supportThreshold = supportWeight / (supportWeight + 2F);
                                float rand = random.nextFloat();
                                if(rand < supportThreshold) {
                                    types.add(EntitiesNF.DREG.get());
                                    if(supportWeight > 0) supportWeight--;
                                }
                                else if(rand < supportThreshold + (1F - supportThreshold) / 2F) types.add(EntitiesNF.HUSK.get());
                                else types.add(EntitiesNF.SKELETON.get());
                            }
                            //Create pool of valid positions to spawn at
                            ObjectArrayList<BlockPos> openPositions = ObjectArrayList.of();
                            int xDist = 1, zDist = 1;
                            while(openPositions.size() < types.size()) {
                                for(int x = -xDist; x <= xDist; x += xDist) {
                                    for(int z = -zDist; z <= zDist; z+= zDist) {
                                        if(x == 0 && z == 0) continue;
                                        int xPos = centerPos.getX() + x, zPos = centerPos.getZ() + z;
                                        if(!level.hasChunkAt(xPos, zPos)) continue;
                                        BlockPos pos = new BlockPos(xPos, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xPos, zPos), zPos);
                                        //All undead have similar spawn conditions so test early here with Husk
                                        //TODO: Should do undead spawn restrictions here
                                        if(NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, level, pos, EntitiesNF.HUSK.get())) {
                                            openPositions.add(pos);
                                        }
                                    }
                                }
                                xDist++;
                                zDist++;
                                if(xDist > 4) break;
                            }
                            if(NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, level, centerPos, EntitiesNF.HUSK.get())) {
                                openPositions.add(centerPos);
                            }
                            //Spawn entities
                            for(int i = 0; i < types.size(); i++) {
                                if(openPositions.isEmpty()) break;
                                ObjectArrayList<BlockPos> positions = openPositions.clone();
                                BlockPos pos = positions.remove((random.nextInt() & Integer.MAX_VALUE) % positions.size());
                                EntityType<?> type = types.get(i);
                                PathfinderMob entity = (PathfinderMob) type.create(level);
                                entity.moveTo(pos, random.nextFloat() * 360.0F, 0.0F);
                                if(entity.checkSpawnRules(level, MobSpawnType.NATURAL)) {
                                    int spawnsLeft = types.size() - i;
                                    boolean cancelSpawn = false;
                                    //Try to reposition if colliding
                                    while(!entity.checkSpawnObstruction(level)) {
                                        if(spawnsLeft <= positions.size()) {
                                            cancelSpawn = true;
                                            break;
                                        }
                                        pos = positions.remove((random.nextInt() & Integer.MAX_VALUE) % positions.size());
                                    }
                                    if(cancelSpawn) break;
                                    openPositions.remove(pos);
                                    entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
                                    level.addFreshEntityWithPassengers(entity);
                                    capC.addUndeadUUID(entity.getUUID());
                                }
                            }
                            capC.setSpawnedUndead(true);
                            chunk.setUnsaved(true);
                        }
                    }
                }
            }
            for(TickingChunk tickingChunk : chunks) tickingChunk.holder.broadcastChanges(tickingChunk.chunk);
            chunkMap.tick();
        }
    }

    @Unique
    private boolean isUndeadSpawnBlocked(BlockPos spawnPos, IChunkData centerChunkData, ChunkPos chunkPos) {
        if(centerChunkData.isUndeadSpawnBlocked(spawnPos)) return true;
        for(int x = -1; x <= 1; x++) {
            for(int z = -1; z <= 1; z++) {
                if(x == 0 && z == 0) continue;
                if(ChunkData.get(level.getChunk(chunkPos.x + x, chunkPos.z + z)).isUndeadSpawnBlocked(spawnPos)) {
                    return true;
                }
            }
        }
        return false;
    }
}
