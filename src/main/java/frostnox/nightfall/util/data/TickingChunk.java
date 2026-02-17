package frostnox.nightfall.util.data;

import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.IGlobalChunkData;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

public class TickingChunk {
    public final LevelChunk chunk;
    public final ChunkHolder holder;
    public final ChunkPos chunkPos;
    public final IChunkData chunkData;
    public final IGlobalChunkData globalChunkData;

    public TickingChunk(LevelChunk chunk, ChunkHolder holder, IChunkData chunkData, IGlobalChunkData globalChunkData) {
        this.chunk = chunk;
        this.holder = holder;
        this.chunkPos = chunk.getPos();
        this.chunkData = chunkData;
        this.globalChunkData = globalChunkData;
    }
}