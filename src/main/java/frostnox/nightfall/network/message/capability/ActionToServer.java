package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.item.IActionableItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.registry.ActionsNF;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ActionToServer {
    private boolean mainHand;
    private ResourceLocation actionID;
    private boolean isValid;

    public ActionToServer(boolean mainHand, ResourceLocation actionID) {
        this.mainHand = mainHand;
        this.actionID = actionID;
        isValid = true;
    }

    private ActionToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeBoolean(mainHand);
            b.writeResourceLocation(actionID);
        }
    }

    public static ActionToServer read(FriendlyByteBuf b) {
        ActionToServer msg = new ActionToServer();
        msg.mainHand = b.readBoolean();
        msg.actionID = b.readResourceLocation();
        msg.isValid = true;
        return msg;
    }

    public static void handle(ActionToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        if(!msg.isValid) {
            Nightfall.LOGGER.warn("ActionToServer is invalid.");
            return;
        }
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("ActionToServer received on client.");
        }
        else if(sideReceived.isServer()) {
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                ctx.enqueueWork(() -> doServerWork(msg, player));
            }
            else Nightfall.LOGGER.warn("ServerPlayer is null.");
        }
    }

    private static void doServerWork(ActionToServer msg, ServerPlayer player) {
        if(!player.isAlive()) return;
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        Item item = player.getMainHandItem().getItem();
        if(!msg.mainHand) item = player.getOffhandItem().getItem();
        if(item instanceof IActionableItem actionableItem) {
            if(msg.mainHand) capP.setMainhandActive();
            else capP.setOffhandActive();
            if(!ActionsNF.get(msg.actionID).canStart(player) || !actionableItem.hasAction(msg.actionID, player)) {
                capA.dequeue();
                NetworkHandler.toClient(player, new ActionTrackerToClient(capA.writeNBT(), player.getId()));
            }
            else {
                capA.startAction(msg.actionID);
                NetworkHandler.toAllTracking(player, new ActionToClient(capA.getActionID(), player.getId()));
            }
        }
    }
}
