package frostnox.nightfall.network.message.world;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.GlobalChunkData;
import frostnox.nightfall.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChunkDigProgressToServer {
    private int chunkX, chunkZ;
    private boolean isValid;

    public ChunkDigProgressToServer(LevelChunk chunk) {
        this.chunkX = chunk.getPos().x;
        this.chunkZ = chunk.getPos().z;
        isValid = true;
    }

    private ChunkDigProgressToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(chunkX);
            b.writeInt(chunkZ);
        }
    }

    public static ChunkDigProgressToServer read(FriendlyByteBuf b) {
        ChunkDigProgressToServer msg = new ChunkDigProgressToServer();
        msg.chunkX = b.readInt();
        msg.chunkZ = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(ChunkDigProgressToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("ChunkDigProgressToServer received on client.");
        }
        else if(sideReceived.isServer() && ctx.getSender() != null) {
            ServerLevel level = (ServerLevel) ctx.getSender().level;
            if(!level.hasChunk(msg.chunkX, msg.chunkZ)) return; //Chunk must be loaded
            LevelChunk chunk = level.getChunk(msg.chunkX, msg.chunkZ);
            if(!level.getChunkSource().chunkMap.getPlayers(chunk.getPos(), false).contains(ctx.getSender())) return; //Player must be near chunk
            ChunkDigProgressToClient message = GlobalChunkData.get(chunk).getBreakProgressMessage();
            if(message != null) NetworkHandler.toClient(ctx.getSender(), message);
        }
    }
}
