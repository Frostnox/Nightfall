package frostnox.nightfall.network.message.world;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.item.IActionableItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.block.IMicroGrid;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GridUseToServer {
    private boolean isValid;
    private int type;
    private int x, y, z;
    private BlockPos blockEntityPos;

    public GridUseToServer(int type, Vec3i gridPos, BlockPos blockEntityPos) {
        this.type = type;
        this.x = gridPos.getX();
        this.y = gridPos.getY();
        this.z = gridPos.getZ();
        this.blockEntityPos = blockEntityPos;
        isValid = true;
    }

    private GridUseToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(type);
            b.writeInt(x);
            b.writeInt(y);
            b.writeInt(z);
            b.writeBlockPos(blockEntityPos);
        }
    }

    public static GridUseToServer read(FriendlyByteBuf b) {
        GridUseToServer msg = new GridUseToServer();
        msg.type = b.readInt();
        msg.x = b.readInt();
        msg.y = b.readInt();
        msg.z = b.readInt();
        msg.blockEntityPos = b.readBlockPos();
        msg.isValid = true;
        return msg;
    }

    public static void handle(GridUseToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("SmithHitToServer received on client.");
        }
        else if(sideReceived.isServer()) {
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                ctx.enqueueWork(() -> doWork(msg, player));
            }
            else Nightfall.LOGGER.warn("ServerPlayer is null.");
        }
    }

    private static void doWork(GridUseToServer msg, ServerPlayer player) {
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        BlockPos pos = msg.blockEntityPos;
        if(!player.isAlive()) {
            Nightfall.LOGGER.warn("Player {} tried to use grid but is not alive.", player.getName().getString());
        }
        else if(pos.distToCenterSqr(player.getX(), player.getY(), player.getZ()) > 9 || !player.level.isLoaded(pos)) {
            Nightfall.LOGGER.warn("Player {} tried to use grid but is not within reach.", player.getName().getString());
        }
        else if(!(player.level.getBlockEntity(pos) instanceof IMicroGrid gridEntity)) {
            Nightfall.LOGGER.warn("Player {} tried to use grid on an invalid block entity.", player.getName().getString());
        }
        else if(!gridEntity.canUseGrid(capA.getAction())) {
            Nightfall.LOGGER.warn("Player {} tried to use grid while performing an invalid action.", player.getName().getString());
        }
        else if(!(player.getItemInHand(capP.getActiveHand()).getItem() instanceof IActionableItem item) || !item.hasAction(capA.getActionID(), player)) {
            Nightfall.LOGGER.warn("Player {} tried to use grid with an invalid item.", player.getName().getString());
        }
        else if(capP.hasInteracted()) {
            Nightfall.LOGGER.warn("Player {} tried to use grid multiple times during one action.", player.getName().getString());
        }
        else if(!gridEntity.isValidActionType(msg.type)) {
            Nightfall.LOGGER.warn("Player {} tried to use grid with invalid action type.", player.getName().getString());
        }
        else {
            capP.setInteracted(true);
            Vec3i hitPos = new Vec3i(msg.x, msg.y, msg.z);
            gridEntity.useGrid(msg.type, hitPos, player, player.getItemInHand(capP.getActiveHand()));
            capP.setHitStopFrame(capA.getFrame());
            NetworkHandler.toAllTracking(player, new GenericEntityToClient(NetworkHandler.Type.HITSTOP_CLIENT, player.getId()));
        }
    }
}
