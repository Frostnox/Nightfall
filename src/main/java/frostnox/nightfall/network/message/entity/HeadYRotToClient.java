package frostnox.nightfall.network.message.entity;

import frostnox.nightfall.Nightfall;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class HeadYRotToClient {
    private float yRot;
    private int entityID;
    private boolean isValid;

    public HeadYRotToClient(float yRot, int entityID) {
        this.yRot = yRot;
        this.entityID = entityID;
        this.isValid = true;
    }

    private HeadYRotToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeFloat(yRot);
            b.writeInt(entityID);
        }
    }

    public static HeadYRotToClient read(FriendlyByteBuf b) {
        HeadYRotToClient msg = new HeadYRotToClient();
        msg.yRot = b.readFloat();
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(HeadYRotToClient msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
            if(!world.isPresent()) {
                Nightfall.LOGGER.warn("Level could not be found.");
                return;
            }
            ctx.enqueueWork(() -> {
                LivingEntity entity = (LivingEntity) world.get().getEntity(msg.entityID);
                if(entity == null || !entity.isAlive()) {
                    Nightfall.LOGGER.warn("LivingEntity is null or dead.");
                    return;
                }
                entity.setYHeadRot(msg.yRot);
                entity.yHeadRotO = msg.yRot;
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("HeadYRotToClient received on server.");
        }
    }
}
