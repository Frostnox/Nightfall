package frostnox.nightfall.network.message.world;

import com.mojang.math.Vector3d;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class ClimbPositionToClient {
    private int entityID;
    private double x, y, z;
    private boolean isValid;

    public ClimbPositionToClient(Vector3d position, int entityID) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        this.entityID = entityID;
        isValid = true;
    }

    private ClimbPositionToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeFloat((float) x);
            b.writeFloat((float) y);
            b.writeFloat((float) z);
            b.writeInt(entityID);
        }
    }

    public static ClimbPositionToClient read(FriendlyByteBuf b) {
        ClimbPositionToClient msg = new ClimbPositionToClient();
        msg.x = b.readFloat();
        msg.y = b.readFloat();
        msg.z = b.readFloat();
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(ClimbPositionToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
                if(!(world.get().getEntity(msg.entityID) instanceof Player player)) {
                    Nightfall.LOGGER.warn("Entity is invalid.");
                    return;
                }
                if(!player.isAlive()) {
                    Nightfall.LOGGER.warn("LocalPlayer is null or dead.");
                    return;
                }
                IPlayerData capP = PlayerData.get(player);
                capP.setClimbPosition(new Vector3d(msg.x, msg.y, msg.z));
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("ClimbPositionToClient received on server.");
        }
    }
}
