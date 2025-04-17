package frostnox.nightfall.network.message.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static frostnox.nightfall.util.CombatUtil.DODGE_PENALTY_TICK;
import static frostnox.nightfall.util.CombatUtil.DODGE_STAMINA_COST;

public class DodgeToServer {
    private int dir;
    private boolean isValid;

    public DodgeToServer(int direction) {
        this.dir = direction;
        isValid = true;
    }

    public DodgeToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(dir);
        }
    }

    public static DodgeToServer read(FriendlyByteBuf b) {
        DodgeToServer msg = new DodgeToServer();
        msg.dir = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(DodgeToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("DodgeToServer received on client.");
        }
        else if(sideReceived.isServer()) {
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                if(player.isAlive()) ctx.enqueueWork(() -> doServerWork(msg, player));
            }
            else Nightfall.LOGGER.warn("ServerPlayer is null or dead.");
        }
    }

    private static void doServerWork(DodgeToServer msg, ServerPlayer player) {
        IPlayerData capP = PlayerData.get(player);
        if(capP.getStamina() <= 0 || player.isUsingItem() || player.tickCount - capP.getLastDodgeTick() < 4 || player.isInWater() || player.isInLava() || player.getPose() == Pose.SWIMMING) {
            Nightfall.LOGGER.warn("Player {} dodged incorrectly.", player.getName().getContents());
        }
        if(player.tickCount - capP.getLastDodgeTick() < DODGE_PENALTY_TICK) capP.addStamina(DODGE_STAMINA_COST * 2);
        else capP.addStamina(DODGE_STAMINA_COST);
        capP.setLastDodgeTick(player.tickCount);
        capP.setDodgeDirection(msg.dir);
        NetworkHandler.toAllTracking(player, new DodgeToClient(msg.dir, player.getId()));
    }
}
