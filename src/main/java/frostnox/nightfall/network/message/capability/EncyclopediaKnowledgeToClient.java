package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.registry.KnowledgeNF;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class EncyclopediaKnowledgeToClient {
    private ResourceLocation id;
    private boolean remove;
    private int entityID;
    private boolean isValid;

    public EncyclopediaKnowledgeToClient(ResourceLocation id, boolean remove, int entityID) {
        this.id = id;
        this.remove = remove;
        this.entityID = entityID;
        isValid = true;
    }

    private EncyclopediaKnowledgeToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeResourceLocation(id);
            b.writeBoolean(remove);
            b.writeInt(entityID);
        }
    }

    public static EncyclopediaKnowledgeToClient read(FriendlyByteBuf b) {
        EncyclopediaKnowledgeToClient msg = new EncyclopediaKnowledgeToClient();
        msg.id = b.readResourceLocation();
        msg.remove = b.readBoolean();
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(EncyclopediaKnowledgeToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
            Nightfall.LOGGER.warn("EncyclopediaKnowledgeToClient received on server.");
        }
    }

    private static void doClientWork(EncyclopediaKnowledgeToClient msg, Level world) {
        if(!(world.getEntity(msg.entityID) instanceof Player player)) {
            Nightfall.LOGGER.warn("Entity is invalid.");
            return;
        }
        if(!player.isAlive()) {
            Nightfall.LOGGER.warn("LocalPlayer is null or dead.");
            return;
        }
        IPlayerData capP = PlayerData.get(player);
        if(msg.remove) capP.removeKnowledge(msg.id);
        else capP.addKnowledge(msg.id);
    }
}
