package frostnox.nightfall.network.message.world;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class ChunkClimateToClient {
    private static final int SIZE = 16, SIZE_SQR = SIZE * SIZE;
    private int chunkX, chunkZ;
    private final float[] temperature = new float[SIZE], humidity = new float[SIZE];
    private boolean isValid;

    public ChunkClimateToClient(LevelChunk chunk, float[] temperature, float[] humidity) {
        this.chunkX = chunk.getPos().x;
        this.chunkZ = chunk.getPos().z;
        isValid = temperature.length == SIZE_SQR && humidity.length == SIZE_SQR;
        if(isValid) {
            int newI = 0;
            for(int x = 0; x < 16; x++) {
                for(int z = 0; z < 16; z++) {
                    if(x % 5 == 0 && z % 5 == 0) {
                        int i = x * 16 + z;
                        this.temperature[newI] = temperature[i];
                        this.humidity[newI] = humidity[i];
                        newI++;
                    }
                }
            }
        }
        else Nightfall.LOGGER.error("Invalid parameter array size in " + getClass().getSimpleName());
    }

    private ChunkClimateToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(chunkX);
            b.writeInt(chunkZ);
            for(int i = 0; i < SIZE; i++) {
                b.writeFloat(temperature[i]);
                b.writeFloat(humidity[i]);
            }
        }
    }

    public static ChunkClimateToClient read(FriendlyByteBuf b) {
        ChunkClimateToClient msg = new ChunkClimateToClient();
        msg.chunkX = b.readInt();
        msg.chunkZ = b.readInt();
        for(int i = 0; i < SIZE; i++) {
            msg.temperature[i] = b.readFloat();
            msg.humidity[i] = b.readFloat();
        }
        msg.isValid = msg.temperature.length == SIZE && msg.humidity.length == SIZE;
        return msg;
    }

    public static void handle(ChunkClimateToClient msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
            ctx.enqueueWork(() -> {
                if(!world.isPresent()) {
                    Nightfall.LOGGER.warn("ClientLevel could not be found.");
                    return;
                }
                IChunkData capC = ChunkData.get(world.get().getChunk(msg.chunkX, msg.chunkZ));
                int i = 0;
                //Get sampled points
                for(int x = 0; x < 16; x += 5) {
                    for(int z = 0; z < 16; z += 5) {
                        capC.setTemperature(x, z, msg.temperature[i]);
                        capC.setHumidity(x, z, msg.humidity[i]);
                        i++;
                    }
                }
                capC.setOld();
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("ChunkClimateToClient received on server.");
        }
    }
}
