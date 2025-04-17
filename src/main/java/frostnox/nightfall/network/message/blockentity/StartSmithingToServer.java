package frostnox.nightfall.network.message.blockentity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.block.anvil.TieredAnvilContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StartSmithingToServer {
    private ResourceLocation recipeID;
    private int containerID;
    private boolean isValid;

    public StartSmithingToServer(ResourceLocation recipeID, int containerID) {
        this.recipeID = recipeID;
        this.containerID = containerID;
        isValid = true;
    }

    private StartSmithingToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeResourceLocation(recipeID);
            b.writeInt(containerID);
        }
    }

    public static StartSmithingToServer read(FriendlyByteBuf b) {
        StartSmithingToServer msg = new StartSmithingToServer();
        msg.recipeID = b.readResourceLocation();
        msg.containerID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(StartSmithingToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("StartSmithingToServer received on client.");
        }
        else if(sideReceived.isServer()) {
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                if(player.isAlive()) ctx.enqueueWork(() -> doServerWork(msg, player));
            }
            else Nightfall.LOGGER.warn("ServerPlayer is null or dead.");
        }
    }

    private static void doServerWork(StartSmithingToServer msg, ServerPlayer player) {
        if(player.containerMenu.containerId == msg.containerID && !player.isSpectator()) {
            if(player.containerMenu instanceof TieredAnvilContainer container && player.level.isLoaded(container.entity.getBlockPos())) {
                if(container.entity.getBlockPos().distToCenterSqr(player.getX(), player.getY(), player.getZ()) <= 25 && container.entity.canStartSmithing(player, msg.recipeID)) {
                    container.consumeInputs();
                    container.entity.startSmithing(msg.recipeID);
                    player.containerMenu.broadcastChanges();
                }
            }
        }
    }
}
