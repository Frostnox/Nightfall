package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class ShiveringToClient {
    private int entityID;
    private boolean shivering;
    private boolean isValid;

    public ShiveringToClient(boolean shivering, int entityID) {
        this.entityID = entityID;
        this.shivering = shivering;
        isValid = true;
    }

    private ShiveringToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(entityID);
            b.writeBoolean(shivering);
        }
    }

    public static ShiveringToClient read(FriendlyByteBuf b) {
        ShiveringToClient msg = new ShiveringToClient();
        msg.entityID = b.readInt();
        msg.shivering = b.readBoolean();
        msg.isValid = true;
        return msg;
    }

    public static void handle(ShiveringToClient msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        if(!msg.isValid) {
            Nightfall.LOGGER.warn("ShiveringToClient is invalid.");
            return;
        }
        //Handle by side
        if(sideReceived.isClient()) {
            Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
            if(!world.isPresent()) {
                Nightfall.LOGGER.warn("Level could not be found.");
                return;
            }
            ctx.enqueueWork(() -> {
                Entity entity = world.get().getEntity(msg.entityID);
                if(entity == null || !entity.isAlive()) {
                    Nightfall.LOGGER.warn("Entity in ShiveringToClient is null or dead.");
                }
                if(entity instanceof Player player) {
                    IPlayerData capP = PlayerData.get(player);
                    capP.setShivering(msg.shivering);
                }
                else Nightfall.LOGGER.warn("Entity received in ShiveringToClient is not a Player.");
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("ShiveringToClient received on server.");
        }
    }
}
