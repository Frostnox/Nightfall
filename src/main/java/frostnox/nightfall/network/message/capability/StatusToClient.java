package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.entity.ActionableEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class StatusToClient {
    private int entityID, duration;
    private Status status;
    private boolean isValid;

    public StatusToClient(int duration, int entityID, Status status) {
        this.entityID = entityID;
        this.duration = duration;
        this.status = status;
        isValid = true;
    }

    private StatusToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(entityID);
            b.writeInt(duration);
            b.writeEnum(status);
        }
    }

    public static StatusToClient read(FriendlyByteBuf b) {
        StatusToClient msg = new StatusToClient();
        msg.entityID = b.readInt();
        msg.duration = b.readInt();
        msg.status = b.readEnum(Status.class);
        msg.isValid = true;
        return msg;
    }

    public static void handle(StatusToClient msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        if(!msg.isValid) {
            Nightfall.LOGGER.warn("StatusToClient is invalid.");
            return;
        }
        //Handle by side
        if(sideReceived.isClient()) {
            Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
            ctx.enqueueWork(() -> {
                if(!world.isPresent()) {
                    Nightfall.LOGGER.warn("Level could not be found.");
                    return;
                }
                Entity entity = world.get().getEntity(msg.entityID);
                if(entity instanceof Player player) {
                    IActionTracker capA = ActionTracker.get(player);
                    switch(msg.status) {
                        case STUN -> capA.stun(msg.duration, true);
                        case BLEEDING -> capA.setBleedDuration(msg.duration);
                        case POISON -> capA.setPoisonDuration(msg.duration);
                    }
                }
                else if(entity instanceof ActionableEntity animEntity) {
                    IActionTracker capA = animEntity.getActionTracker();
                    switch(msg.status) {
                        case STUN -> capA.stun(msg.duration, true);
                        case BLEEDING -> capA.setBleedDuration(msg.duration);
                        case POISON -> capA.setPoisonDuration(msg.duration);
                    }
                }
                else Nightfall.LOGGER.warn("Entity does not have an ActionTracker to set status.");
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("StatusToClient received on server.");
        }
    }

    public enum Status {
        STUN, BLEEDING, POISON
    }
}
