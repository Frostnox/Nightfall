package frostnox.nightfall.network.message.world;

import com.mojang.math.Vector3d;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClimbPositionToServer {
    private boolean isValid;
    private double x, y, z;

    public ClimbPositionToServer(Vector3d position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        isValid = true;
    }

    private ClimbPositionToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeFloat((float) x);
            b.writeFloat((float) y);
            b.writeFloat((float) z);
        }
    }

    public static ClimbPositionToServer read(FriendlyByteBuf b) {
        ClimbPositionToServer msg = new ClimbPositionToServer();
        msg.x = b.readFloat();
        msg.y = b.readFloat();
        msg.z = b.readFloat();
        msg.isValid = true;
        return msg;
    }

    public static void handle(ClimbPositionToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("ClimbPositionToServer received on client.");
        }
        else if(sideReceived.isServer()) {
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                ctx.enqueueWork(() -> doWork(msg, player));
            }
            else Nightfall.LOGGER.warn("ServerPlayer is null.");
        }
    }

    private static void doWork(ClimbPositionToServer msg, ServerPlayer player) {
        IPlayerData capP = PlayerData.get(player);
        capP.setClimbPosition(new Vector3d(msg.x, msg.y, msg.z));
        NetworkHandler.toAllTracking(player, new ClimbPositionToClient(capP.getClimbPosition(), player.getId()));
    }
}
