package frostnox.nightfall.network.message.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.util.AnimationUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class DodgeToClient {
    private int entityID;
    private int dir;
    private boolean isValid;

    public DodgeToClient(int direction, int entityID) {
        this.dir = direction;
        this.entityID = entityID;
        isValid = true;
    }

    private DodgeToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(dir);
            b.writeInt(entityID);
        }
    }

    public static DodgeToClient read(FriendlyByteBuf b) {
        DodgeToClient msg = new DodgeToClient();
        msg.dir = b.readInt();
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(DodgeToClient msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
            if(!world.isPresent()) {
                Nightfall.LOGGER.warn("ClientLevel could not be found.");
                return;
            }
            ctx.enqueueWork(() -> doClientWork(msg, world.get()));
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("DodgeToClient received on server.");
        }
    }

    private static void doClientWork(DodgeToClient msg, Level world) {
        if(!(world.getEntity(msg.entityID) instanceof Player player)) {
            Nightfall.LOGGER.warn("Entity is invalid.");
            return;
        }
        if(!player.isAlive()) {
            Nightfall.LOGGER.warn("LocalPlayer is null or dead.");
            return;
        }
        IPlayerData capP = PlayerData.get(player);
        capP.setLastDodgeTick(player.tickCount);
        capP.setDodgeDirection(msg.dir);
        AnimationUtil.createDodgeParticles(player);
    }
}
