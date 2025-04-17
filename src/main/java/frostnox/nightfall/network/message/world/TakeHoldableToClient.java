package frostnox.nightfall.network.message.world;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.registry.ActionsNF;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class TakeHoldableToClient {
    private int playerID;
    private CompoundTag heldContents;
    private boolean isValid;

    public TakeHoldableToClient(CompoundTag heldContents, int playerID) {
        this.playerID = playerID;
        this.heldContents = heldContents;
        this.isValid = true;
    }

    private TakeHoldableToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(playerID);
            b.writeNbt(heldContents);
        }
    }

    public static TakeHoldableToClient read(FriendlyByteBuf b) {
        TakeHoldableToClient msg = new TakeHoldableToClient();
        msg.playerID = b.readInt();
        msg.heldContents = b.readNbt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(TakeHoldableToClient msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
            if(!world.isPresent()) {
                Nightfall.LOGGER.warn("Level could not be found.");
                return;
            }
            ctx.enqueueWork(() -> {
                Player player = (Player) world.get().getEntity(msg.playerID);
                if(player == null || !player.isAlive()) {
                    Nightfall.LOGGER.warn("Player is null or dead.");
                    return;
                }
                IActionTracker capA = ActionTracker.get(player);
                capA.startAction(ActionsNF.HOLD_ENTITY.getId());
                IPlayerData capP = PlayerData.get(player);
                capP.setHeldContents(msg.heldContents);
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("TakeHoldableToClient received on server.");
        }
    }
}
