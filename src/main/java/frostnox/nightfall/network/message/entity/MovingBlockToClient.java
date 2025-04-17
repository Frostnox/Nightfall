package frostnox.nightfall.network.message.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Extra data that needs to be synced to client
 */
public class MovingBlockToClient {
    private int slideTime, entityID;
    private Direction slideDir;
    private boolean resetVelocity, isValid;

    public MovingBlockToClient(int slideTime, Direction slideDir, boolean resetVelocity, int entityID) {
        this.slideTime = slideTime;
        this.slideDir = slideDir;
        this.resetVelocity = resetVelocity;
        this.entityID = entityID;
        isValid = true;
    }

    private MovingBlockToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(slideTime);
            b.writeEnum(slideDir);
            b.writeBoolean(resetVelocity);
            b.writeInt(entityID);
        }
    }

    public static MovingBlockToClient read(FriendlyByteBuf b) {
        MovingBlockToClient msg = new MovingBlockToClient();
        msg.slideTime = b.readInt();
        msg.slideDir = b.readEnum(Direction.class);
        msg.resetVelocity = b.readBoolean();
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(MovingBlockToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
            Nightfall.LOGGER.warn("MovingBlockToClient received on server.");
        }
    }

    private static void doClientWork(MovingBlockToClient msg, Level world) {
        if(!(world.getEntity(msg.entityID) instanceof MovingBlockEntity entity)) {
            Nightfall.LOGGER.warn("MovingBlockEntity is invalid.");
            return;
        }
        entity.slideDir = msg.slideDir;
        entity.setSlideTime(msg.slideTime);
        if(msg.resetVelocity) {
            entity.setDeltaMovement(0, 0, 0);
            entity.setPos(entity.getBlockX() + 0.5, entity.getBlockY(), entity.getBlockZ() + 0.5);
        }
    }
}
