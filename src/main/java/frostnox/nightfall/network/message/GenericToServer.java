package frostnox.nightfall.network.message;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.*;
import frostnox.nightfall.encyclopedia.EntryStage;
import frostnox.nightfall.encyclopedia.PuzzleContainer;
import frostnox.nightfall.network.NetworkHandler;

import frostnox.nightfall.network.message.capability.ActionTrackerToClient;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GenericToServer {
    private NetworkHandler.Type messageType;
    private boolean isValid;

    public GenericToServer(NetworkHandler.Type messageType) {
        this.messageType = messageType;
        isValid = true;
    }

    private GenericToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeEnum(messageType);
        }
    }

    public static GenericToServer read(FriendlyByteBuf b) {
        GenericToServer msg = new GenericToServer();
        msg.messageType = b.readEnum(NetworkHandler.Type.class);
        msg.isValid = true;
        return msg;
    }

    public static void handle(GenericToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("GenericToServer with " + msg.messageType + " received on client.");
        }
        else if(sideReceived.isServer()) {
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                ctx.enqueueWork(() -> doServerWork(msg, player));
            }
            else Nightfall.LOGGER.warn("ServerPlayer is null.");
        }
    }

    private static void doServerWork(GenericToServer msg, ServerPlayer player) {
        if(!player.isAlive()) return;
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        switch(msg.messageType) {
            case PUZZLE_EXPERIMENT_SERVER -> {
                if(player.containerMenu instanceof PuzzleContainer container) {
                    if(capP.hasEntryStage(container.entry.getRegistryName(), EntryStage.PUZZLE) && container.entry.puzzle != null) {
                        if(container.entry.puzzle.isSolved(capP, container.inventory)) {
                            capP.advanceStage(container.entry.getRegistryName());
                            NetworkHandler.toClient(player, new GenericEntityToClient(NetworkHandler.Type.PUZZLE_EXPERIMENT_SUCCESS_CLIENT, player.getId()));
                        }
                        else {
                            IntList icons = container.entry.puzzle.getItemIcons(container.inventory);
                            for(int i = 0; i < icons.size(); i++) container.itemIcons.set(i, icons.getInt(i));
                            NetworkHandler.toClient(player, new GenericEntityToClient(NetworkHandler.Type.PUZZLE_EXPERIMENT_FAIL_CLIENT, player.getId()));
                        }
                        container.inventory.clear();
                        return;
                    }
                }
                Nightfall.LOGGER.error("Player " + player.getName().getString() + " tried to solve puzzle but does not have access to entry");
            }
            case ACTIVATE_MAINHAND -> {
                if(capA.isInactive() || capA.getAction().isInterruptible() || (capA.getState() == capA.getAction().getTotalStates() - 1 && capA.getFrame() == capA.getDuration())) {
                    capP.setMainhandActive();
                    NetworkHandler.toAllTracking(player, new GenericEntityToClient(NetworkHandler.Type.ACTIVATE_MAINHAND, player.getId()));
                }
            }
            case ACTIVATE_OFFHAND -> {
                if(capA.isInactive() || capA.getAction().isInterruptible() || (capA.getState() == capA.getAction().getTotalStates() - 1 && capA.getFrame() == capA.getDuration())) {
                    capP.setOffhandActive();
                    NetworkHandler.toAllTracking(player, new GenericEntityToClient(NetworkHandler.Type.ACTIVATE_OFFHAND, player.getId()));
                }
            }
            case START_CRAWLING -> {
                if(capA.isInactive() || capA.getAction().isInterruptible()) {
                    capP.setCrawling(true);
                    player.setPose(Pose.SWIMMING);
                    player.setSprinting(false);
                }
                else NetworkHandler.toClient(player, new GenericEntityToClient(NetworkHandler.Type.STOP_CRAWLING, player.getId()));
            }
            case STOP_CRAWLING -> {
                if(capA.isInactive() || capA.getAction().isInterruptible()) capP.setCrawling(false);
                else NetworkHandler.toClient(player, new GenericEntityToClient(NetworkHandler.Type.START_CRAWLING, player.getId()));
            }
            case START_CLIMBING -> {
                capP.setClimbing(true);
                NetworkHandler.toAllTracking(player, new GenericEntityToClient(NetworkHandler.Type.START_CLIMBING, player.getId()));
            }
            case STOP_CLIMBING -> {
                capP.setClimbing(false);
                NetworkHandler.toAllTracking(player, new GenericEntityToClient(NetworkHandler.Type.STOP_CLIMBING, player.getId()));
            }
            case QUEUE_ACTION_TRACKER -> {
                Action action = capA.getAction();
                if(capA.isCharging()) {
                    capA.queue();
                    NetworkHandler.toAllTracking(player, new GenericEntityToClient(NetworkHandler.Type.QUEUE_ACTION_TRACKER, player.getId()));
                }
                else if(!action.getChain(player).get().isEmpty() && (capA.getState() == action.getChainState() || capA.getState() == action.getChainState() - 1)) {
                    capA.queue();
                    NetworkHandler.toAllTracking(player, new GenericEntityToClient(NetworkHandler.Type.QUEUE_ACTION_TRACKER, player.getId()));
                }
                else if(!(capA.getState() <= action.getChargeState() + 1 && capA.getFrame() <= 2) || action.chainsFrom().get().isEmpty()) {
                    NetworkHandler.toClient(player, new ActionTrackerToClient(capA.writeNBT(), player.getId()));
                }
            }
            default -> {
                Nightfall.LOGGER.error("No handler in " + GenericToServer.class.getSimpleName() + " for message " + msg.messageType);
            }
        }
    }
}
