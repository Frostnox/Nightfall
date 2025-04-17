package frostnox.nightfall.network.message.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class DamageFailToClient {
    private int attackerID;
    private int targetID;
    private boolean isValid;

    public DamageFailToClient(int attackerID, int targetID) {
        this.attackerID = attackerID;
        this.targetID = targetID;
        this.isValid = true;
    }

    private DamageFailToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(attackerID);
            b.writeInt(targetID);
        }
    }

    public static DamageFailToClient read(FriendlyByteBuf b) {
        DamageFailToClient msg = new DamageFailToClient();
        msg.attackerID = b.readInt();
        msg.targetID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(DamageFailToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
                Player player = (Player) world.get().getEntity(msg.attackerID);
                if(player == null || !player.isAlive()) {
                    Nightfall.LOGGER.warn("Player is null or dead.");
                    return;
                }
                IActionTracker capA = ActionTracker.get(player);
                if(capA.getHitEntities().contains(msg.targetID)) capA.getHitEntities().remove((Object) msg.targetID);
                if(world.get().getEntity(msg.targetID) instanceof LivingEntity) capA.setLivingEntitiesHit(capA.getLivingEntitiesHit() - 1);
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("DamageFailToClient received on server.");
        }
    }
}
