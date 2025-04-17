package frostnox.nightfall.network.message.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class SetAllyToClient {
    private int allyID, entityID;
    private boolean isValid;

    public SetAllyToClient(int allyID, int entityID) {
        this.allyID = allyID;
        this.entityID = entityID;
        isValid = true;
    }

    private SetAllyToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(allyID);
            b.writeInt(entityID);
        }
    }

    public static SetAllyToClient read(FriendlyByteBuf b) {
        SetAllyToClient msg = new SetAllyToClient();
        msg.allyID = b.readInt();
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(SetAllyToClient msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
            if(!world.isPresent()) {
                Nightfall.LOGGER.warn("ClientLevel could not be found.");
                return;
            }
            ctx.enqueueWork(() -> doClientWork(msg, world.get()));
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("SetAllyToClient received on server.");
        }
    }

    private static void doClientWork(SetAllyToClient msg, Level world) {
        if(!(world.getEntity(msg.entityID) instanceof ActionableEntity entity)) {
            Nightfall.LOGGER.warn("Entity is invalid.");
            return;
        }
        if(!entity.isAlive()) {
            Nightfall.LOGGER.warn("Entity is null or dead.");
            return;
        }
        if(!(world.getEntity(msg.allyID) instanceof ActionableEntity ally)) {
            Entity ally = world.getEntity(msg.allyID);
            if(ally != null && !ally.isRemoved()) Nightfall.LOGGER.warn("Ally " + world.getEntity(msg.allyID) + " is invalid.");
            return;
        }
        entity.setAlly(ally);
    }
}
