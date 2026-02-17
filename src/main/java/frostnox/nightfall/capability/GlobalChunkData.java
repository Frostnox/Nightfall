package frostnox.nightfall.capability;

import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.world.ChunkDigProgressToClient;
import frostnox.nightfall.network.message.world.DigBlockToClient;
import frostnox.nightfall.util.DataUtil;
import frostnox.nightfall.util.MathUtil;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Set;

public class GlobalChunkData implements IGlobalChunkData {
    public static final Capability<IGlobalChunkData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {}); //Reference to manager instance
    private final LevelChunk chunk;
    private final boolean isClientSide;
    private final Object2ObjectMap<BlockPos, BreakProgressData> breakingBlocks;

    public GlobalChunkData(LevelChunk chunk) {
        this.chunk = chunk;
        this.isClientSide = chunk.getLevel().isClientSide;
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
        breakingBlocks = new Object2ObjectOpenCustomHashMap<>(8, compactPosHash);
    }

    @Override
    public void tick() {
        if(breakingBlocks.isEmpty()) return;
        Set<BlockPos> removals = new ObjectArraySet<>(8);
        for(var entry : breakingBlocks.entrySet()) {
            BreakProgressData data = entry.getValue();
            int decay = data.getDecay();
            BlockPos pos = entry.getKey();
            //Checking every tick is not ideal but few blocks are breaking at once so this should be fine
            if(decay == 1 || !chunk.getBlockState(pos).is(Block.stateById(data.getStateID()).getBlock())) {
                removals.add(pos);
                NetworkHandler.toAllTrackingChunk(chunk, new DigBlockToClient(pos.getX(), pos.getY(), pos.getZ(), -1));
            }
            else {
                data.setDecay(decay - 1);
                if(decay <= 20 * 20 && decay % 40 == 0) {
                    float progress = data.getProgress() - 1F/10F;
                    if(progress <= 0F) {
                        removals.add(pos);
                        NetworkHandler.toAllTrackingChunk(chunk, new DigBlockToClient(pos.getX(), pos.getY(), pos.getZ(), -1));
                    }
                    else {
                        data.setProgress(progress);
                        NetworkHandler.toAllTrackingChunk(chunk, new DigBlockToClient(pos.getX(), pos.getY(), pos.getZ(), progress));
                    }
                }
            }
        }
        for(BlockPos key : removals) breakingBlocks.remove(key);
    }

    @Override
    public void setBreakProgress(BlockPos pos, int decay, float progress, BlockState block) {
        breakingBlocks.put(pos, new BreakProgressData(decay, progress, Block.getId(block)));
    }

    @Override
    public void setBreakProgress(BlockPos pos, float progress, BlockState block) {
        breakingBlocks.put(pos, new BreakProgressData(20 * 50, progress, Block.getId(block)));
    }

    @Override
    public float getBreakProgress(BlockPos pos) {
        BreakProgressData data = breakingBlocks.get(pos);
        if(data != null) return data.getProgress();
        else return 0;
    }

    @Override
    public void removeBreakProgress(BlockPos pos) {
        breakingBlocks.remove(pos);
    }

    @Override
    public void simulateBreakProgress(long elapsedTime) {
        if(breakingBlocks.isEmpty()) return;
        if(elapsedTime >= 20 * 50) breakingBlocks.clear();
        else {
            Set<BlockPos> removals = new ObjectArraySet<>(8);
            for(var entry : breakingBlocks.entrySet()) {
                BreakProgressData data = entry.getValue();
                int decay = data.getDecay();
                int newDecay = decay - (int) elapsedTime;
                if(newDecay <= 0) {
                    BlockPos pos = entry.getKey();
                    removals.add(pos);
                }
                else {
                    data.setDecay(newDecay);
                    if(newDecay <= 20 * 20) {
                        BlockPos pos = entry.getKey();
                        float progress = data.getProgress() - 1F/10F * MathUtil.getPassedIntervals(newDecay, Math.min(20 * 20, decay), 40);
                        if(progress <= 0F) removals.add(pos);
                        else data.setProgress(progress);
                    }
                }
            }
            for(BlockPos key : removals) breakingBlocks.remove(key);
        }
    }

    @Override
    public ChunkDigProgressToClient getBreakProgressMessage() {
        if(!isClientSide && !breakingBlocks.isEmpty()) {
            ChunkPos chunkPos = chunk.getPos();
            int[] packedPositions = new int[breakingBlocks.size()];
            float[] progress = new float[breakingBlocks.size()];
            int i = 0;
            for(var entry : breakingBlocks.entrySet()) {
                packedPositions[i] = DataUtil.packChunkPos(entry.getKey());
                progress[i] = entry.getValue().getProgress();
                i++;
            }
            return new ChunkDigProgressToClient(chunkPos.x, chunkPos.z, packedPositions, progress);
        }
        return null;
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();
        if(!breakingBlocks.isEmpty()) {
            ListTag hashes = new ListTag(), decay = new ListTag(), progress = new ListTag(), stateID = new ListTag();
            for(var entry : breakingBlocks.entrySet()) {
                BlockPos pos = entry.getKey();
                hashes.add(IntTag.valueOf(((pos.getX() & 15) << 22) | ((pos.getZ() & 15) << 12) | (pos.getY() & 4095)));
                BreakProgressData data = entry.getValue();
                decay.add(IntTag.valueOf(data.getDecay()));
                progress.add(FloatTag.valueOf(data.getProgress()));
                stateID.add(IntTag.valueOf(data.getStateID()));
            }
            tag.put("breakPositions", hashes);
            tag.put("breakDecay", decay);
            tag.put("breakProgress", progress);
            tag.put("breakStateID", stateID);
        }
        return tag;
    }

    @Override
    public void readNBT(CompoundTag tag) {
        int minX = chunk.getPos().getMinBlockX(), minZ = chunk.getPos().getMinBlockZ();
        if(tag.contains("breakPositions")) {
            ListTag hashes = tag.getList("breakPositions", ListTag.TAG_INT);
            ListTag decayTag = tag.getList("breakDecay", ListTag.TAG_INT);
            ListTag progressTag = tag.getList("breakProgress", ListTag.TAG_FLOAT);
            ListTag stateIDTag = tag.getList("breakStateID", ListTag.TAG_INT);
            for(int i = 0; i < hashes.size(); i++) {
                int hash = ((IntTag) hashes.get(i)).getAsInt();
                int decay = ((IntTag) decayTag.get(i)).getAsInt();
                float progress = ((FloatTag) progressTag.get(i)).getAsFloat();
                BlockPos pos = new BlockPos(minX + (hash >>> 22), hash & 4095, minZ + ((hash >>> 12) & 1023));
                int stateID = ((IntTag) stateIDTag.get(i)).getAsInt();
                breakingBlocks.put(pos, new BreakProgressData(decay, progress, stateID));
            }
        }
    }

    public static IGlobalChunkData get(LevelChunk chunk) {
        return chunk.getCapability(CAPABILITY, null).orElseThrow(() -> new IllegalArgumentException("Null in LazyOptional."));
    }

    public static class GlobalChunkDataCapability implements ICapabilitySerializable<CompoundTag> {
        private final GlobalChunkData cap;
        private final LazyOptional<IGlobalChunkData> holder;

        public GlobalChunkDataCapability(LevelChunk chunk) {
            cap = new GlobalChunkData(chunk);
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

    private static class BreakProgressData {
        private int decay;
        private float progress;
        private final int stateID;

        private BreakProgressData(int decay, float progress, int stateID) {
            this.decay = decay;
            this.progress = progress;
            this.stateID = stateID;
        }

        private int getStateID() {
            return stateID;
        }

        public int getDecay() {
            return decay;
        }

        public void setDecay(int decay) {
            this.decay = decay;
        }

        public float getProgress() {
            return progress;
        }

        public void setProgress(float progress) {
            this.progress = progress;
        }
    }
}
