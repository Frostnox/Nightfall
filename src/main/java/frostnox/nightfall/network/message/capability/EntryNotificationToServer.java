package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.encyclopedia.EntryStage;
import frostnox.nightfall.encyclopedia.PuzzleContainer;
import frostnox.nightfall.registry.EntriesNF;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class EntryNotificationToServer {
    private ResourceLocation entryId;
    private boolean remove;
    private boolean isValid;

    public EntryNotificationToServer(ResourceLocation entryId, boolean remove) {
        this.entryId = entryId;
        this.remove = remove;
        isValid = true;
    }

    private EntryNotificationToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeResourceLocation(entryId);
            b.writeBoolean(remove);
        }
    }

    public static EntryNotificationToServer read(FriendlyByteBuf b) {
        EntryNotificationToServer msg = new EntryNotificationToServer();
        msg.entryId = b.readResourceLocation();
        msg.remove = b.readBoolean();
        msg.isValid = EntriesNF.contains(msg.entryId);
        return msg;
    }

    public static void handle(EntryNotificationToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn(EntryNotificationToServer.class.getSimpleName() + " received on client.");
        }
        else if(sideReceived.isServer()) {
            if(!msg.isValid) return;
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                if(player.isAlive()) ctx.enqueueWork(() -> doServerWork(msg, player));
            }
        }
    }

    private static void doServerWork(EntryNotificationToServer msg, ServerPlayer player) {
        IPlayerData capP = PlayerData.get(player);
        if(msg.remove) capP.removeEntryNotification(msg.entryId);
        else capP.addEntryNotification(msg.entryId);
    }
}
