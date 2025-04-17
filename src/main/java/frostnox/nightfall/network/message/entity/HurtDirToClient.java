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

public class HurtDirToClient {
    private float hurtDir;
    private int entityID;
    private boolean isValid;

    public HurtDirToClient(float hurtDir, int entityID) {
        this.hurtDir = hurtDir;
        this.entityID = entityID;
        this.isValid = true;
    }

    private HurtDirToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeFloat(hurtDir);
            b.writeInt(entityID);
        }
    }

    public static HurtDirToClient read(FriendlyByteBuf b) {
        HurtDirToClient msg = new HurtDirToClient();
        msg.hurtDir = b.readFloat();
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(HurtDirToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
                    Nightfall.LOGGER.warn("Player is null or dead.");
                    return;
                }
                entity.hurtDir = msg.hurtDir;
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("HurtDirToClient received on server.");
        }
    }
}
