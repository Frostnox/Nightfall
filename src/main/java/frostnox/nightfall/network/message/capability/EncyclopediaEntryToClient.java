package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.encyclopedia.EntryStage;
import frostnox.nightfall.registry.EntriesNF;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class EncyclopediaEntryToClient {
    private ResourceLocation id;
    private int entityID, stage;
    private boolean isValid;

    public EncyclopediaEntryToClient(ResourceLocation id, EntryStage stage, int entityID) {
        this.id = id;
        this.stage = stage.ordinal();
        this.entityID = entityID;
        isValid = true;
    }

    private EncyclopediaEntryToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeResourceLocation(id);
            b.writeInt(stage);
            b.writeInt(entityID);
        }
    }

    public static EncyclopediaEntryToClient read(FriendlyByteBuf b) {
        EncyclopediaEntryToClient msg = new EncyclopediaEntryToClient();
        msg.id = b.readResourceLocation();
        msg.stage = b.readInt();
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(EncyclopediaEntryToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
            Nightfall.LOGGER.warn("EncyclopediaEntryToClient received on server.");
        }
    }

    private static void doClientWork(EncyclopediaEntryToClient msg, Level world) {
        if(!(world.getEntity(msg.entityID) instanceof Player player)) {
            Nightfall.LOGGER.warn("Entity is invalid.");
            return;
        }
        if(!player.isAlive()) {
            Nightfall.LOGGER.warn("LocalPlayer is null or dead.");
            return;
        }
        IPlayerData capP = PlayerData.get(player);
        Entry entry = EntriesNF.get(msg.id);
        EntryStage stage = EntryStage.values()[msg.stage];
        capP.addEntry(msg.id, stage);
        if(entry.shouldUnlock(capP)) {
            if(stage == EntryStage.PUZZLE) ClientEngine.get().tryCategoryNotification(msg.id);
            if(entry.isAddendum || (entry.isHidden && stage == EntryStage.PUZZLE)) ClientEngine.get().doEntryNotification(msg.id);
        }
    }
}
