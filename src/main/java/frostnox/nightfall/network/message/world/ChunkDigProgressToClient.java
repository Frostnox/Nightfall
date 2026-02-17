package frostnox.nightfall.network.message.world;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.GlobalChunkData;
import frostnox.nightfall.capability.IGlobalChunkData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.util.DataUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class ChunkDigProgressToClient {
    private int chunkX, chunkZ;
    private int[] packedPositions;
    private float[] progress;
    private boolean isValid;

    public ChunkDigProgressToClient(int chunkX, int chunkZ, int[] packedPositions, float[] progress) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.packedPositions = packedPositions;
        this.progress = progress;
        isValid = packedPositions.length == progress.length && progress.length <= 2000;
    }

    private ChunkDigProgressToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeVarInt(chunkX);
            b.writeVarInt(chunkZ);
            b.writeVarIntArray(packedPositions);
            b.writeVarInt(progress.length);
            for(float f : progress) b.writeFloat(f);
        }
    }

    public static ChunkDigProgressToClient read(FriendlyByteBuf b) {
        ChunkDigProgressToClient msg = new ChunkDigProgressToClient();
        msg.chunkX = b.readVarInt();
        msg.chunkZ = b.readVarInt();
        msg.packedPositions = b.readVarIntArray();
        msg.progress = new float[b.readVarInt()];
        for(int i = 0; i < msg.progress.length; i++) {
            msg.progress[i] = b.readFloat();
        }
        msg.isValid = true;
        return msg;
    }

    public static void handle(ChunkDigProgressToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
                Level level = world.get();
                if(!level.hasChunk(msg.chunkX, msg.chunkZ)) {
                    Nightfall.LOGGER.warn("Cannot update dig progress at unloaded chunk [" + msg.chunkX + ", " + msg.chunkZ + "].");
                    return;
                }
                LevelChunk chunk = level.getChunk(msg.chunkX, msg.chunkZ);
                IGlobalChunkData chunkData = GlobalChunkData.get(chunk);
                ChunkPos chunkPos = chunk.getPos();
                int chunkX = chunkPos.getMinBlockX(), chunkZ = chunkPos.getMinBlockZ();
                for(int i = 0; i < msg.packedPositions.length; i++) {
                    BlockPos pos = DataUtil.unpackChunkPos(chunkX, chunkZ, msg.packedPositions[i]);
                    BlockState block = chunk.getBlockState(pos);
                    float progress = msg.progress[i];
                    if(progress >= 1.0F) {
                        level.levelEvent(null, 2001, pos, Block.getId(block)); //Destroy particles
                        ClientEngine.get().visuallyDestroyBlock(pos, -1);
                        chunkData.removeBreakProgress(pos);
                    }
                    else {
                        ClientEngine.get().visuallyDestroyBlock(pos, (int) (progress * 10F) - 1);
                        chunkData.setBreakProgress(pos, progress, block);
                    }
                }
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn(ChunkDigProgressToClient.class.getSimpleName() + " received on server.");
        }
    }
}
