package frostnox.nightfall.network.message.world;

import frostnox.nightfall.Nightfall;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class DestroyBlockNoSoundToClient {
    private BlockPos pos;
    private int blockID;
    private boolean isValid;

    public DestroyBlockNoSoundToClient(BlockPos pos, int blockID) {
        this.pos = pos;
        this.blockID = blockID;
        isValid = true;
    }

    private DestroyBlockNoSoundToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeBlockPos(pos);
            b.writeInt(blockID);
        }
    }

    public static DestroyBlockNoSoundToClient read(FriendlyByteBuf b) {
        DestroyBlockNoSoundToClient msg = new DestroyBlockNoSoundToClient();
        msg.pos = b.readBlockPos();
        msg.blockID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(DestroyBlockNoSoundToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
                world.get().addDestroyBlockEffect(msg.pos, Block.stateById(msg.blockID));
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn(DestroyBlockNoSoundToClient.class.getSimpleName() + " received on server.");
        }
    }
}
