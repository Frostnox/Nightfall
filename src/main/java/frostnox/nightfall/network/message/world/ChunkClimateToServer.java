package frostnox.nightfall.network.message.world;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChunkClimateToServer {
    private int chunkX, chunkZ;
    private boolean isValid;

    public ChunkClimateToServer(LevelChunk chunk) {
        this.chunkX = chunk.getPos().x;
        this.chunkZ = chunk.getPos().z;
        isValid = true;
    }

    private ChunkClimateToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(chunkX);
            b.writeInt(chunkZ);
        }
    }

    public static ChunkClimateToServer read(FriendlyByteBuf b) {
        ChunkClimateToServer msg = new ChunkClimateToServer();
        msg.chunkX = b.readInt();
        msg.chunkZ = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(ChunkClimateToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("ChunkClimateToServer received on client.");
        }
        else if(sideReceived.isServer() && ctx.getSender() != null) {
            ServerLevel level = (ServerLevel) ctx.getSender().level;
            if(!LevelData.isPresent(level) || !level.hasChunk(msg.chunkX, msg.chunkZ)) return; //Chunk must be loaded
            LevelChunk chunk = level.getChunk(msg.chunkX, msg.chunkZ);
            if(!level.getChunkSource().chunkMap.getPlayers(chunk.getPos(), false).contains(ctx.getSender())) return; //Player must be near chunk
            IChunkData capC = ChunkData.get(chunk);
            NetworkHandler.toClient(ctx.getSender(), capC.createClimateMessageToClient());
        }
    }
}
