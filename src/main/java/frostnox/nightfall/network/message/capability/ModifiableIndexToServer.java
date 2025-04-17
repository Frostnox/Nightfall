package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.PlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ModifiableIndexToServer {
    private int index;
    private boolean isValid;

    public ModifiableIndexToServer(int index) {
        if(index < 0) isValid = false;
        else {
            this.index = index;
            isValid = true;
        }
    }

    private ModifiableIndexToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeVarInt(index);
        }
    }

    public static ModifiableIndexToServer read(FriendlyByteBuf b) {
        ModifiableIndexToServer msg = new ModifiableIndexToServer();
        if(!b.isReadable(1)) return msg;
        msg.index = b.readVarInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(ModifiableIndexToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("ModifiableIndexToServer received on client.");
        }
        else if(sideReceived.isServer()) {
            if(!msg.isValid) return;
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                if(player.isAlive()) ctx.enqueueWork(() -> doServerWork(msg, player));
            }
            else Nightfall.LOGGER.warn("ServerPlayer is null or dead.");
        }
    }

    private static void doServerWork(ModifiableIndexToServer msg, ServerPlayer player) {
        PlayerData.get(player).setCachedModifiableIndex(msg.index); //Blindly set this here, validity of index should be handled elsewhere
    }
}
