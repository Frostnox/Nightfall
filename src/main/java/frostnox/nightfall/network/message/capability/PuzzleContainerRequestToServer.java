package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
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

public class PuzzleContainerRequestToServer {
    private ResourceLocation entryId;
    private boolean isValid;

    public PuzzleContainerRequestToServer(ResourceLocation entryId) {
        this.entryId = entryId;
        isValid = true;
    }

    private PuzzleContainerRequestToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeResourceLocation(entryId);
        }
    }

    public static PuzzleContainerRequestToServer read(FriendlyByteBuf b) {
        PuzzleContainerRequestToServer msg = new PuzzleContainerRequestToServer();
        msg.entryId = b.readResourceLocation();
        msg.isValid = EntriesNF.contains(msg.entryId);
        return msg;
    }

    public static void handle(PuzzleContainerRequestToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn(PuzzleContainerRequestToServer.class.getSimpleName() + " received on client.");
        }
        else if(sideReceived.isServer()) {
            if(!msg.isValid) return;
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                if(player.isAlive()) ctx.enqueueWork(() -> doServerWork(msg, player));
            }
        }
    }

    private static void doServerWork(PuzzleContainerRequestToServer msg, ServerPlayer player) {
        if(!PlayerData.get(player).hasEntryStage(msg.entryId, EntryStage.PUZZLE)) {
            Nightfall.LOGGER.error("Player " + player.getName().getString() + " requested puzzle container but does not have access to " + msg.entryId);
        }
        else NetworkHooks.openGui(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TranslatableComponent(Nightfall.MODID + ".encyclopedia_puzzle");
            }

            @Override
            public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                return new PuzzleContainer(pPlayerInventory, pContainerId, msg.entryId);
            }
        }, buf -> buf.writeResourceLocation(msg.entryId));
    }
}
