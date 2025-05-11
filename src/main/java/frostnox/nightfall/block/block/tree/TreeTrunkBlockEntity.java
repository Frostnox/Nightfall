package frostnox.nightfall.block.block.tree;

import frostnox.nightfall.block.ITree;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.world.generation.tree.TreeGenerator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class TreeTrunkBlockEntity extends BlockEntity {
    protected static boolean updating = false;
    public long lastTick;
    public int maxHeight = -1; //Tallest height the tree has ever reached
    protected long seed;

    public TreeTrunkBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesNF.TREE_TRUNK.get(), pos, state);
    }

    public void initSeed(WorldGenLevel level, Random random) {
        if(seed == 0) {
            seed = random.nextLong();
            lastTick = level.getLevelData().getGameTime();
        }
    }

    public void initSeed() {
        if(seed == 0 && level != null) {
            seed = level.random.nextLong();
            lastTick = level.getGameTime();
        }
    }

    public long getSeed() {
        return seed;
    }

    public ObjectSet<BlockPos> getTree() {
        if(!(level instanceof ServerLevel serverLevel)) return null;
        TreeTrunkBlock trunk = (TreeTrunkBlock) getBlockState().getBlock();
        return trunk.treeGenerator.getTree(serverLevel, this, false).collectTree();
    }

    public static List<TreeTrunkBlockEntity> getNearbyTrunks(Level level, ITree type, BlockPos aroundPos, Collection<BlockPos> possiblePositions) {
        int minX = aroundPos.getX(), maxX = aroundPos.getX(), minZ = aroundPos.getZ(), maxZ = aroundPos.getZ();
        for(BlockPos pos : possiblePositions) {
            if(pos.getX() < minX) minX = pos.getX();
            else if(pos.getX() > maxX) maxX = pos.getX();
            if(pos.getZ() < minZ) minZ = pos.getZ();
            else if(pos.getZ() > maxZ) maxZ = pos.getZ();
        }
        return getNearbyTrunks(level, type, aroundPos, minX, maxX, minZ, maxZ);
    }

    public static List<TreeTrunkBlockEntity> getNearbyTrunks(Level level, ITree type, BlockPos aroundPos, int minXBlock, int maxXBlock, int minZBlock, int maxZBlock) {
        if(level == null || level.isClientSide()) return List.of();
        TreeGenerator gen = type.getGenerator();
        int minX = SectionPos.blockToSectionCoord(minXBlock), maxX = SectionPos.blockToSectionCoord(maxXBlock);
        int minZ = SectionPos.blockToSectionCoord(minZBlock), maxZ = SectionPos.blockToSectionCoord(maxZBlock);
        List<LevelChunk> chunks = new ArrayList<>(4);
        chunks.add(level.getChunkSource().getChunk(minX, minZ, true));
        if(maxX != minX) chunks.add(level.getChunkSource().getChunk(maxX, minZ, true));
        if(maxZ != minZ) chunks.add(level.getChunkSource().getChunk(minX, maxZ, true));
        if(chunks.size() == 3) chunks.add(level.getChunkSource().getChunk(maxX, maxZ, true));
        List<TreeTrunkBlockEntity> nearbyTrunks = new ArrayList<>(16);
        for(LevelChunk chunk : chunks) {
            for(BlockEntity entity : chunk.getBlockEntities().values()) {
                if(entity instanceof TreeTrunkBlockEntity other && other.getBlockState().is(type.getTag())
                    && Math.abs(aroundPos.getY() - other.worldPosition.getY()) <= gen.maxPossibleHeight) {
                    nearbyTrunks.add(other);
                }
            }
        }
        return nearbyTrunks;
    }

    public void updateBlocks(BlockPos destroyPos, @Nullable TreeGenerator.Data simulatedData, boolean leavesOnly) {
        if(!(level instanceof ServerLevel serverLevel)) return;
        TreeTrunkBlock trunk = (TreeTrunkBlock) getBlockState().getBlock();
        TreeGenerator gen = trunk.treeGenerator;
        if(simulatedData == null) simulatedData = gen.getTree(serverLevel, this, true);
        ObjectSet<BlockPos> currentTree = destroyPos.equals(worldPosition) ? new ObjectOpenHashSet<>(0) : getTree();
        int minX = worldPosition.getX() - gen.maxLeavesDistXZ, maxX = worldPosition.getX() + gen.maxLeavesDistXZ;
        int minZ = worldPosition.getZ() - gen.maxLeavesDistXZ, maxZ = worldPosition.getZ() + gen.maxLeavesDistXZ;
        List<TreeTrunkBlockEntity> nearbyTrunks = getNearbyTrunks(level, trunk.type, worldPosition, minX, maxX, minZ, maxZ);
        ObjectSet<BlockPos> nearbyTrees = new ObjectOpenHashSet<>(60 * nearbyTrunks.size());
        for(TreeTrunkBlockEntity nearbyTrunk : nearbyTrunks) {
            if(nearbyTrunk == this) continue;
            nearbyTrees.addAll(nearbyTrunk.getTree());
        }
        //Any blocks that were part of this tree but are not currently should be destroyed
        int leavesDestroyed = 0;
        //Prevent tree blocks from trying to update again when removed from inside of this function
        updating = true;
        for(BlockPos pos : simulatedData.collectLeaves()) {
            if(!currentTree.contains(pos)) {
                BlockState state = serverLevel.getBlockState(pos);
                boolean isLeaves = state.is(trunk.leavesBlock);
                if((isLeaves || state.is(trunk.branchesBlock)) && !nearbyTrees.contains(pos)) {
                    LevelUtil.uncheckedDropDestroyBlockNoSound(level, pos, state, Blocks.AIR.defaultBlockState(), null, 3);
                    if(isLeaves) leavesDestroyed++;
                }
            }
        }
        if(!leavesOnly) {
            int woodDestroyed = 0;
            for(BlockPos pos : simulatedData.collectWood()) {
                if(!currentTree.contains(pos)) {
                    BlockState state = serverLevel.getBlockState(pos);
                    if(state.is(trunk.stemBlock) && !nearbyTrees.contains(pos)) {
                        LevelUtil.uncheckedDropDestroyBlockNoSound(level, pos, state, Blocks.AIR.defaultBlockState(), null, 3);
                        woodDestroyed++;
                    }
                }
            }
            if(leavesDestroyed > 5) {
                level.playSound(null, destroyPos, woodDestroyed >= 15 && leavesDestroyed >= 50 ? SoundsNF.BIG_TREE_FALL.get() : SoundsNF.SMALL_TREE_FALL.get(), SoundSource.BLOCKS, 1F, 0.96F + level.getRandom().nextFloat() * 0.08F);
            }
            else if(woodDestroyed > 2) level.playSound(null, destroyPos, SoundsNF.LOG_FALL.get(), SoundSource.BLOCKS, 1F, 0.92F + level.getRandom().nextFloat() * 0.16F);
        }
        updating = false;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        seed = tag.getLong("seed");
        lastTick = tag.getLong("lastTick");
        maxHeight = tag.getInt("maxHeight");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("seed", seed);
        tag.putLong("lastTick", lastTick);
        tag.putInt("maxHeight", maxHeight);
    }

    /*@Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }*/
}
