package frostnox.nightfall.network.message.world;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.GlobalChunkData;
import frostnox.nightfall.capability.IGlobalChunkData;
import frostnox.nightfall.client.ClientEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class DigBlockToClient {
    private int x, y, z;
    private float progress;
    private boolean isValid;

    public DigBlockToClient(int x, int y, int z, float progress) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.progress = progress;
        isValid = true;
    }

    private DigBlockToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(x);
            b.writeInt(y);
            b.writeInt(z);
            b.writeFloat(progress);
        }
    }

    public static DigBlockToClient read(FriendlyByteBuf b) {
        DigBlockToClient msg = new DigBlockToClient();
        msg.x = b.readInt();
        msg.y = b.readInt();
        msg.z = b.readInt();
        msg.progress = b.readFloat();
        msg.isValid = true;
        return msg;
    }

    public static void handle(DigBlockToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
                BlockPos pos = new BlockPos(msg.x, msg.y, msg.z);
                BlockState block = world.get().getBlockState(pos);
                IGlobalChunkData chunkData = GlobalChunkData.get(world.get().getChunkAt(pos));
                if(msg.progress >= 1.0F) {
                    world.get().levelEvent(null, 2001, pos, Block.getId(block)); //Destroy particles
                    ClientEngine.get().visuallyDestroyBlock(pos, -1);
                    chunkData.removeBreakProgress(pos);
                }
                else {
                    ClientEngine.get().visuallyDestroyBlock(pos, (int) (msg.progress * 10F) - 1);
                    chunkData.setBreakProgress(pos, msg.progress, block);
                }
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn(DigBlockToClient.class.getSimpleName() + " received on server.");
        }
    }
}
