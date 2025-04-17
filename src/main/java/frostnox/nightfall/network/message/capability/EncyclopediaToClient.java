package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.capability.IPlayerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class EncyclopediaToClient {
    private int entityID;
    private CompoundTag NBT;
    private boolean isValid;

    public EncyclopediaToClient(CompoundTag NBT, int entityID) {
        this.entityID = entityID;
        this.NBT = NBT;
        isValid = true;
    }

    private EncyclopediaToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeNbt(NBT);
            b.writeInt(entityID);
        }
    }

    public static EncyclopediaToClient read(FriendlyByteBuf b) {
        EncyclopediaToClient msg = new EncyclopediaToClient();
        msg.NBT = b.readNbt();
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(EncyclopediaToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
            Nightfall.LOGGER.warn("EncyclopediaToClient received on server.");
        }
    }

    private static void doClientWork(EncyclopediaToClient msg, Level world) {
        if(!(world.getEntity(msg.entityID) instanceof Player player)) {
            Nightfall.LOGGER.warn("Entity is invalid.");
            return;
        }
        if(!player.isAlive()) {
            Nightfall.LOGGER.warn("LocalPlayer is null or dead.");
            return;
        }
        IPlayerData capP = PlayerData.get(player);
        capP.readEncyclopediaNBT(msg.NBT);
    }
}
