package frostnox.nightfall.network.message.blockentity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.block.anvil.AnvilAction;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlockEntity;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.item.IActionableItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AnvilActionToServer {
    private boolean isValid;
    private AnvilAction action;
    private int index;
    private BlockPos blockEntityPos;

    public AnvilActionToServer(AnvilAction action, int index, BlockPos blockEntityPos) {
        this.action = action;
        this.index = index;
        this.blockEntityPos = blockEntityPos;
        isValid = true;
    }

    private AnvilActionToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(action.ordinal());
            b.writeInt(index);
            b.writeBlockPos(blockEntityPos);
        }
    }

    public static AnvilActionToServer read(FriendlyByteBuf b) {
        AnvilActionToServer msg = new AnvilActionToServer();
        msg.action = AnvilAction.values()[b.readInt()];
        msg.index = b.readInt();
        msg.blockEntityPos = b.readBlockPos();
        msg.isValid = true;
        return msg;
    }

    public static void handle(AnvilActionToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("AnvilActionToServer received on client.");
        }
        else if(sideReceived.isServer()) {
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                ctx.enqueueWork(() -> doWork(msg, player));
            }
            else Nightfall.LOGGER.warn("ServerPlayer is null.");
        }
    }

    private static void doWork(AnvilActionToServer msg, ServerPlayer player) {
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        BlockPos pos = msg.blockEntityPos;
        if(!player.isAlive()) {
            Nightfall.LOGGER.warn("Player {} tried to use anvil but is not alive.", player.getName().getString());
        }
        else if(pos.distToCenterSqr(player.getX(), player.getY(), player.getZ()) > 9 || !player.level.isLoaded(pos)) {
            Nightfall.LOGGER.warn("Player {} tried to use anvil but is not within reach.", player.getName().getString());
        }
        else if(!(player.level.getBlockEntity(pos) instanceof TieredAnvilBlockEntity anvil)) {
            Nightfall.LOGGER.warn("Player {} tried to use anvil on an invalid block entity.", player.getName().getString());
        }
        else if(!anvil.hasWorkpiece()) {
            Nightfall.LOGGER.warn("Player {} tried to use anvil but no workpiece is present.", player.getName().getString());
        }
        else {
            ItemStack item = player.getItemInHand(capP.getActiveHand());
            if(item.getItem() instanceof IActionableItem actionable && !actionable.hasAction(capA.getActionID(), player)) {
                Nightfall.LOGGER.warn("Player {} tried to use anvil with an invalid item.", player.getName().getString());
            }
            else if(!capA.isInactive() && capP.hasInteracted()) {
                Nightfall.LOGGER.warn("Player {} tried to use anvil multiple times during one action.", player.getName().getString());
            }
            else if((item.is(TagsNF.HAMMER) && msg.action != AnvilAction.STRIKE) || (item.is(TagsNF.CHISEL) && msg.action != AnvilAction.CUT) ||
                    (item.is(TagsNF.TONGS) && msg.action != AnvilAction.FLIP_XZ && msg.action != AnvilAction.FLIP_Y && msg.action != AnvilAction.TAKE)) {
                Nightfall.LOGGER.warn("Player {} tried to use anvil with invalid action type for item.", player.getName().getString());
            }
            else {
                anvil.actWorkpiece(msg.action, msg.index, player, item);
                capP.setInteracted(true);
                if(!capA.isInactive()) {
                    capP.setHitStopFrame(capA.getFrame());
                    NetworkHandler.toAllTracking(player, new GenericEntityToClient(NetworkHandler.Type.HITSTOP_CLIENT, player.getId()));
                }
            }
        }
    }
}
