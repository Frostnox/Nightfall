package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class PlayerDataToClient {
    private final CompoundTag NBT;

    public PlayerDataToClient(CompoundTag NBT, int entityID) {
        NBT.putInt("entityid", entityID);
        this.NBT = NBT;
    }

    private PlayerDataToClient(CompoundTag NBT) {
        this.NBT = NBT;
    }

    public static void write(PlayerDataToClient msg, FriendlyByteBuf b) {
        b.writeNbt(msg.NBT);
    }

    public static PlayerDataToClient read(FriendlyByteBuf b) {
        return new PlayerDataToClient(b.readNbt());
    }

    public static void handle(PlayerDataToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
            Nightfall.LOGGER.warn("PlayerDataToClient received on server.");
        }
    }

    private static void doClientWork(PlayerDataToClient msg, Level world) {
        if(!(world.getEntity(msg.NBT.getInt("entityid")) instanceof Player player)) {
            Nightfall.LOGGER.warn("Entity is invalid.");
            return;
        }
        if(!player.isAlive()) {
            Nightfall.LOGGER.warn("LocalPlayer is null or dead.");
            return;
        }
        IPlayerData capP = PlayerData.get(player);
        capP.readNBT(msg.NBT);
    }
}
