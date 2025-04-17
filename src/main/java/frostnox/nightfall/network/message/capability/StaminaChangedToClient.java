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

public class StaminaChangedToClient {
    private int entityID;
    private double stamina;
    private boolean isValid;

    public StaminaChangedToClient(double stamina, int entityID) {
        this.entityID = entityID;
        this.stamina = stamina;
        isValid = true;
    }

    private StaminaChangedToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(entityID);
            b.writeDouble(stamina);
        }
    }

    public static StaminaChangedToClient read(FriendlyByteBuf b) {
        StaminaChangedToClient msg = new StaminaChangedToClient();
        msg.entityID = b.readInt();
        msg.stamina = b.readDouble();
        msg.isValid = true;
        return msg;
    }

    public static void handle(StaminaChangedToClient msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        if(!msg.isValid) {
            Nightfall.LOGGER.warn("StaminaChangedToClient is invalid.");
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
                    Nightfall.LOGGER.warn("Entity in StaminaChangedToClient is null or dead.");
                }
                if(entity instanceof Player player) {
                    IPlayerData capP = PlayerData.get(player);
                    capP.setStamina(msg.stamina);
                    capP.updateLastStamina();
                }
                else Nightfall.LOGGER.warn("Entity received in StaminaChangedToClient is not a Player.");
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("StaminaChangedToClient received on server.");
        }
    }
}
