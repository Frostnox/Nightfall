package frostnox.nightfall.network.message.world;

import frostnox.nightfall.Nightfall;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class UpdateBlockToClient {
    private BlockPos pos;
    private boolean isValid;

    public UpdateBlockToClient(BlockPos pos) {
        this.pos = pos;
        isValid = true;
    }

    private UpdateBlockToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeBlockPos(pos);
        }
    }

    public static UpdateBlockToClient read(FriendlyByteBuf b) {
        UpdateBlockToClient msg = new UpdateBlockToClient();
        msg.pos = b.readBlockPos();
        msg.isValid = true;
        return msg;
    }

    public static void handle(UpdateBlockToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
                BlockState block = world.get().getBlockState(msg.pos);
                world.get().sendBlockUpdated(msg.pos, block, block, 3);
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("UpdateBlockToClient received on server.");
        }
    }
}
