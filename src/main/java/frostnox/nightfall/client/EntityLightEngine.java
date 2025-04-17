package frostnox.nightfall.client;

import frostnox.nightfall.capability.ILightData;
import frostnox.nightfall.capability.LightData;
import frostnox.nightfall.util.math.AxisDirection;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Set;

/**
 * Handles lighting from players and dropped items.
 * Based on https://github.com/LambdAurora/LambDynamicLights/blob/1.17/HOW_DOES_IT_WORK.md
 */
public class EntityLightEngine {
    private static final EntityLightEngine INSTANCE = new EntityLightEngine();
    private static final int MAX_LIGHT_SOURCES = 300;
    private static final int MAX_DIRTY_LIGHT_SOURCES = 60;

    private final Minecraft mc = Minecraft.getInstance();
    private final Set<SectionPos> dirtySections = new ObjectOpenHashSet<>();
    private final Set<ILightData> lightSources = new ObjectOpenHashSet<>(50);
    private final Set<ILightData> activeLightSources = new ObjectOpenHashSet<>(50);
    private final ObjectArrayFIFOQueue<ILightData> dirtyLightSources = new ObjectArrayFIFOQueue<>(MAX_DIRTY_LIGHT_SOURCES);
    private boolean processedLastFrame = false;
    private int lastTickAtFrameProcess = -1;

    private EntityLightEngine() {

    }

    public static EntityLightEngine get() {
        return INSTANCE;
    }

    public void clear() {
        dirtySections.clear();
        lightSources.clear();
        activeLightSources.clear();
        processedLastFrame = false;
        lastTickAtFrameProcess = -1;
    }

    public void addLightSource(Entity entity) {
        if(lightSources.size() < MAX_LIGHT_SOURCES) {
            ILightData source = LightData.get(entity);
            source.init();
            lightSources.add(source);
            enqueueDirtySource(source);
        }
    }

    public void removeLightSource(Entity entity) {
        ILightData source = LightData.get(entity);
        if(!lightSources.remove(source)) return;
        activeLightSources.remove(source);
        source.setBrightness(0);
        source.setLightRadiusSqr(0);
        if(!source.isLightDirty()) {
            source.setLightDirty(true);
            dirtyLightSources.enqueueFirst(source);
        }
    }

    public void handleBlockUpdate(BlockPos pos) {
        for(ILightData source : activeLightSources) {
            if(!source.isLightDirty() && source.getLightMap().containsKey(pos)) enqueueDirtySource(source);
        }
    }

    public void tickStart() {
        for(ILightData source : lightSources) {
            source.updateLight();
            if(!source.isLightDirty() && needsUpdate(source)) enqueueDirtySource(source);
        }
    }

    public void tickRenderStart() {
        if(!mc.isPaused()) {
            float partial = mc.getFrameTime();
            if(processedLastFrame) {
                if(mc.player.tickCount > lastTickAtFrameProcess) processedLastFrame = false;
            }
            else {
                //Always update and process player once per tick
                ILightData source = LightData.get(mc.player);
                source.updateLight();
                processLight(source, partial);
                if(!dirtySections.isEmpty()) {
                    processedLastFrame = true;
                    lastTickAtFrameProcess = mc.player.tickCount;
                }
            }
            //Process one light source per frame
            if(!dirtyLightSources.isEmpty()) {
                ILightData source = dirtyLightSources.dequeue();
                processLight(source, partial);
                source.setLightDirty(false);
            }
            if(!dirtySections.isEmpty()) {
                for(SectionPos pos : dirtySections) mc.levelRenderer.setSectionDirty(pos.x(), pos.y(), pos.z());
                dirtySections.clear();
            }
        }
    }

    public int adjustPackedLight(BlockPos pos, int packedLight) {
        double entityLight = getLight(pos);
        if(entityLight > LightTexture.block(packedLight)) return (packedLight & 0xFFF00000) | ((int) (entityLight * 16D));
        else return packedLight;
    }

    public double getLight(BlockPos pos) {
        double maxLight = 0D;
        for(ILightData source : activeLightSources) {
            double light = source.getLightMap().getDouble(pos);
            if(light > maxLight) maxLight = light;
        }
        return maxLight;
    }

    private void enqueueDirtySource(ILightData source) {
        if(dirtyLightSources.size() < MAX_DIRTY_LIGHT_SOURCES) {
            source.setLightDirty(true);
            dirtyLightSources.enqueue(source);
        }
    }

    private boolean needsUpdate(ILightData source) {
        Entity entity = source.getEntity();
        Vec3 entityPos = entity.getPosition(1F);
        double camDist = mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(entityPos);
        if(camDist > 50D * 50D) {
            int updateSpeed = camDist < 200D * 200D ? 2 : 3;
            if(entity.tickCount % updateSpeed != 0) return false;
        }
        double oldX = source.getLightX(), oldY = source.getLightY(), oldZ = source.getLightZ();
        double lightX = entityPos.x, lightY = entityPos.y + entity.getBbHeight() * 0.5F, lightZ = entityPos.z;
        int brightness = source.getBrightness(), lastBrightness = source.getLastProcessedBrightness();
        double radiusSqr = source.getLightRadiusSqr(), lastRadiusSqr = source.getLastProcessedLightRadiusSqr();
        boolean moved = Math.abs(lightX - oldX) > 0.1 || Math.abs(lightY - oldY) > 0.1 || Math.abs(lightZ - oldZ) > 0.1;
        return brightness != lastBrightness || radiusSqr != lastRadiusSqr || (moved && (brightness > 0D || lastBrightness > 0D));
    }

    private void processLight(ILightData source, float partial) {
        double oldX = source.getLightX(), oldY = source.getLightY(), oldZ = source.getLightZ();
        Entity entity = source.getEntity();
        Vec3 entityPos = entity.getPosition(partial);
        double lightX = entityPos.x, lightY = entityPos.y + entity.getBbHeight() * 0.5F, lightZ = entityPos.z;
        int brightness = source.getBrightness(), lastBrightness = source.getLastProcessedBrightness();
        double radiusSqr = source.getLightRadiusSqr(), lastRadiusSqr = source.getLastProcessedLightRadiusSqr();
        boolean moved = Math.abs(lightX - oldX) > 0.1 || Math.abs(lightY - oldY) > 0.1 || Math.abs(lightZ - oldZ) > 0.1;
        if(source.isLightDirty() || brightness != lastBrightness || radiusSqr != lastRadiusSqr || (moved && (brightness > 0D || lastBrightness > 0D))) {
            source.setLightX(lightX);
            source.setLightY(lightY);
            source.setLightZ(lightZ);
            source.setLastProcessedBrightness(brightness);
            source.setLastProcessedLightRadiusSqr(radiusSqr);
            int bX = Mth.floor(lightX), bY = Mth.floor(lightY), bZ = Mth.floor(lightZ);
            int bOldX = Mth.floor(oldX), bOldY = Mth.floor(oldY), bOldZ = Mth.floor(oldZ);
            double radius = Math.sqrt(radiusSqr);
            int lastRadius = (int) Math.sqrt(lastRadiusSqr);
            Object2DoubleMap<BlockPos> lightMap = source.getLightMap();
            //Remove old light
            if(lastBrightness > 0) lightMap.clear();
            //Mark old sections
            if(moved || radiusSqr < lastRadiusSqr) collectDirtySections(bOldX, bOldY, bOldZ, lastRadius, lastRadiusSqr);
            if(radiusSqr > 0D) {
                //Mark new sections
                collectDirtySections(bX, bY, bZ, (int) radius, radiusSqr);
                //Breadth-first propagation
                BlockPos startPos = new BlockPos(bX, bY, bZ);
                double dropOff = Math.max(1D, brightness / radius);
                lightMap.put(startPos, source.getBrightness() * (1D - getDist(startPos, source) / radius));
                ObjectOpenHashSet<BlockPos> visited = new ObjectOpenHashSet<>((int) (1.3334 * radiusSqr * radius + 4 * radiusSqr + 4.6667 * radius + 7));
                visited.add(startPos);
                ObjectArrayFIFOQueue<BlockPos> positions = new ObjectArrayFIFOQueue<>((int) (4 * radiusSqr + 4 * radius + 2));
                for(Direction dir : Direction.values()) positions.enqueue(startPos.relative(dir));
                BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
                int sectionX = SectionPos.blockToSectionCoord(startPos.getX()), sectionZ = SectionPos.blockToSectionCoord(startPos.getZ());
                ChunkAccess chunk = mc.level.getChunk(sectionX, sectionZ);
                int sectionIndex = chunk.getSectionIndex(startPos.getY());
                LevelChunkSection section = chunk.isOutsideBuildHeight(startPos.getY()) ? null : chunk.getSection(sectionIndex);
                while(!positions.isEmpty()) {
                    BlockPos pos = positions.dequeue();
                    double dist = getDist(pos, source);
                    if(dist > radius) continue;
                    BlockState state;
                    if(chunk.isOutsideBuildHeight(pos.getY())) state = Blocks.AIR.defaultBlockState();
                    else {
                        int newSectionX = SectionPos.blockToSectionCoord(pos.getX()), newSectionZ = SectionPos.blockToSectionCoord(pos.getZ());
                        if(sectionX != newSectionX || sectionZ != newSectionZ) {
                            sectionX = newSectionX;
                            sectionZ = newSectionZ;
                            chunk = mc.level.getChunk(sectionX, sectionZ);
                            sectionIndex = chunk.getSectionIndex(pos.getY());
                            section = chunk.getSection(sectionIndex);
                        }
                        else {
                            int newSectionIndex = chunk.getSectionIndex(pos.getY());
                            if(sectionIndex != newSectionIndex) {
                                sectionIndex = newSectionIndex;
                                section = chunk.getSection(sectionIndex);
                            }
                        }
                        state = mc.level.isOutsideBuildHeight(pos.getY()) ? Blocks.AIR.defaultBlockState() :
                                section.getBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
                    }
                    if(!state.isAir()) dist += state.getLightBlock(mc.level, pos);
                    double light = source.getBrightness() * (1D - dist / radius);
                    if(light > 0D) {
                        double maxNeighborLight = Double.NEGATIVE_INFINITY;
                        for(AxisDirection axisDir : AxisDirection.values()) {
                            mutablePos.set(pos.getX() + axisDir.x, pos.getY() + axisDir.y, pos.getZ() + axisDir.z);
                            if(!lightMap.containsKey(mutablePos)) continue;
                            double neighborLight = lightMap.getDouble(mutablePos);
                            if(neighborLight <= 0) continue;
                            if(maxNeighborLight == Double.NEGATIVE_INFINITY) {
                                if(state.canOcclude()) {
                                    VoxelShape shape = state.getFaceOcclusionShape(mc.level, pos, axisDir.normal);
                                    if(Block.isShapeFullBlock(shape)) {
                                        maxNeighborLight = 0;
                                        continue;
                                    }
                                }
                                BlockState neighborState;
                                if(chunk.isOutsideBuildHeight(mutablePos.getY())) neighborState = Blocks.AIR.defaultBlockState();
                                else {
                                    int neighborSectionX = SectionPos.blockToSectionCoord(mutablePos.getX()), neighborSectionZ = SectionPos.blockToSectionCoord(mutablePos.getZ());
                                    if(sectionX != neighborSectionX || sectionZ != neighborSectionZ) {
                                        sectionX = neighborSectionX;
                                        sectionZ = neighborSectionZ;
                                        chunk = mc.level.getChunk(sectionX, sectionZ);
                                        sectionIndex = chunk.getSectionIndex(mutablePos.getY());
                                        section = chunk.getSection(sectionIndex);
                                    }
                                    else {
                                        int newSectionIndex = chunk.getSectionIndex(mutablePos.getY());
                                        if(sectionIndex != newSectionIndex) {
                                            sectionIndex = newSectionIndex;
                                            section = chunk.getSection(sectionIndex);
                                        }
                                    }
                                    neighborState = section.getBlockState(mutablePos.getX() & 15, mutablePos.getY() & 15, mutablePos.getZ() & 15);
                                }
                                if(neighborState.canOcclude()) {
                                    VoxelShape neighborShape = neighborState.getFaceOcclusionShape(mc.level, mutablePos, axisDir.getOpposite().normal);
                                    if(Block.isShapeFullBlock(neighborShape)) {
                                        maxNeighborLight = 0;
                                        continue;
                                    }
                                    else if(state.canOcclude()) {
                                        VoxelShape shape = state.getFaceOcclusionShape(mc.level, pos, axisDir.normal);
                                        if(Shapes.faceShapeOccludes(shape, neighborShape)) {
                                            maxNeighborLight = 0;
                                            continue;
                                        }
                                    }
                                }
                            }
                            if(neighborLight > maxNeighborLight) maxNeighborLight = neighborLight;
                        }
                        if(light > (maxNeighborLight - 0.001D) && maxNeighborLight != Double.NEGATIVE_INFINITY) light = maxNeighborLight - dropOff;
                        if(light > 0) {
                            for(AxisDirection axisDir : AxisDirection.values()) {
                                mutablePos.set(pos.getX() + axisDir.x, pos.getY() + axisDir.y, pos.getZ() + axisDir.z);
                                if(state.canOcclude()) {
                                    VoxelShape shape = state.getFaceOcclusionShape(mc.level, pos, axisDir.normal);
                                    if(Block.isShapeFullBlock(shape)) {
                                        if(!lightMap.containsKey(mutablePos)) lightMap.put(mutablePos.immutable(), 0);
                                        continue;
                                    }
                                }
                                BlockState neighborState;
                                if(chunk.isOutsideBuildHeight(mutablePos.getY())) neighborState = Blocks.AIR.defaultBlockState();
                                else {
                                    int neighborSectionX = SectionPos.blockToSectionCoord(mutablePos.getX()), neighborSectionZ = SectionPos.blockToSectionCoord(mutablePos.getZ());
                                    if(sectionX != neighborSectionX || sectionZ != neighborSectionZ) {
                                        sectionX = neighborSectionX;
                                        sectionZ = neighborSectionZ;
                                        chunk = mc.level.getChunk(sectionX, sectionZ);
                                        sectionIndex = chunk.getSectionIndex(mutablePos.getY());
                                        section = chunk.getSection(sectionIndex);
                                    }
                                    else {
                                        int newSectionIndex = chunk.getSectionIndex(mutablePos.getY());
                                        if(sectionIndex != newSectionIndex) {
                                            sectionIndex = newSectionIndex;
                                            section = chunk.getSection(sectionIndex);
                                        }
                                    }
                                    neighborState = section.getBlockState(mutablePos.getX() & 15, mutablePos.getY() & 15, mutablePos.getZ() & 15);
                                }
                                if(neighborState.canOcclude()) {
                                    VoxelShape neighborShape = neighborState.getFaceOcclusionShape(mc.level, mutablePos, axisDir.getOpposite().normal);
                                    if(Block.isShapeFullBlock(neighborShape)) {
                                        if(!lightMap.containsKey(mutablePos)) lightMap.put(mutablePos.immutable(), 0);
                                        continue;
                                    }
                                    else if(state.canOcclude()) {
                                        VoxelShape shape = state.getFaceOcclusionShape(mc.level, pos, axisDir.normal);
                                        if(Shapes.faceShapeOccludes(shape, neighborShape)) {
                                            if(!lightMap.containsKey(mutablePos)) lightMap.put(mutablePos.immutable(), 0);
                                            continue;
                                        }
                                    }
                                }
                                if(!visited.contains(mutablePos)) {
                                    BlockPos neighborPos = mutablePos.immutable();
                                    visited.add(neighborPos);
                                    positions.enqueue(neighborPos);
                                }
                            }
                        }
                    }
                    lightMap.put(pos, light);
                }
                activeLightSources.add(source);
            }
            else activeLightSources.remove(source);
        }
    }

    private static double getDist(BlockPos pos, ILightData source) {
        return Math.abs(source.getLightX() - pos.getX() - 0.5) + Math.abs(source.getLightY() - pos.getY() - 0.5) + Math.abs(source.getLightZ() - pos.getZ() - 0.5);
    }

    private void collectDirtySections(int blockX, int blockY, int blockZ, int radius, double radiusSqr) {
        Set<SectionPos> sections;
        //Case radius < 8: 8 possible sections
        if(radius < 8) {
            sections = new ObjectOpenHashSet<>(8);
            int xMin = SectionPos.blockToSectionCoord(blockX - radius);
            int yMin = SectionPos.blockToSectionCoord(blockY - radius);
            int zMin = SectionPos.blockToSectionCoord(blockZ - radius);
            int xMax = SectionPos.blockToSectionCoord(blockX + radius);
            int yMax = SectionPos.blockToSectionCoord(blockY + radius);
            int zMax = SectionPos.blockToSectionCoord(blockZ + radius);
            sections.add(SectionPos.of(xMin, yMin, zMin));
            sections.add(SectionPos.of(xMax, yMin, zMin));
            sections.add(SectionPos.of(xMin, yMin, zMax));
            sections.add(SectionPos.of(xMin, yMax, zMin));
            sections.add(SectionPos.of(xMax, yMin, zMax));
            sections.add(SectionPos.of(xMax, yMax, zMin));
            sections.add(SectionPos.of(xMin, yMax, zMax));
            sections.add(SectionPos.of(xMax, yMax, zMax));
        }
        //Case radius >= 8: 27 possible sections
        //Note that the worst case of a radius of 14 will only intersect 22 sections
        else {
            sections = new ObjectOpenHashSet<>(22);
            for(int x = blockX - radius; x <= blockX + radius; x += radius) {
                for(int y = blockY - radius; y <= blockY + radius; y += radius) {
                    for(int z = blockZ - radius; z <= blockZ + radius; z += radius) {
                        sections.add(SectionPos.of(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(y), SectionPos.blockToSectionCoord(z)));
                    }
                }
            }
        }
        //Only flag sections in the light sphere
        for(SectionPos section : sections) {
            if(dirtySections.contains(section)) continue;
            int minX = SectionPos.sectionToBlockCoord(section.getX());
            int minY = SectionPos.sectionToBlockCoord(section.getY());
            int minZ = SectionPos.sectionToBlockCoord(section.getZ());
            int xDist = Mth.clamp(blockX, minX, minX + 15) - blockX;
            int yDist = Mth.clamp(blockY, minY, minY + 15) - blockY;
            int zDist = Mth.clamp(blockZ, minZ, minZ + 15) - blockZ;
            if(xDist * xDist + yDist * yDist + zDist * zDist <= radiusSqr) dirtySections.add(section);
        }
    }
}
