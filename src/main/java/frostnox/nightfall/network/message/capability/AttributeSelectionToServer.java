package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.entity.PlayerAttribute;
import frostnox.nightfall.registry.forge.AttributesNF;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumMap;
import java.util.function.Supplier;

public class AttributeSelectionToServer {
    private EnumMap<PlayerAttribute, Integer> attributes;
    private boolean isValid;

    public AttributeSelectionToServer(EnumMap<PlayerAttribute, Integer> attributes) {
        isValid = true;
        this.attributes = attributes;
        if(attributes.size() != PlayerAttribute.values().length) isValid = false;
        else {
            int sum = 0;
            for(Integer points : attributes.values()) {
                sum += points;
                if(points < -3 || points > 3) {
                    isValid = false;
                    break;
                }
            }
            if(sum > 0) isValid = false;
        }
    }

    private AttributeSelectionToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            for(Integer points : attributes.values()) {
                b.writeVarInt(points);
            }
        }
    }

    public static AttributeSelectionToServer read(FriendlyByteBuf b) {
        if(!b.isReadable(PlayerAttribute.values().length)) return new AttributeSelectionToServer();
        EnumMap<PlayerAttribute, Integer> attributes = new EnumMap<>(PlayerAttribute.class);
        for(PlayerAttribute attribute : PlayerAttribute.values()) {
            attributes.put(attribute, b.readVarInt());
        }
        return new AttributeSelectionToServer(attributes);
    }

    public static void handle(AttributeSelectionToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("AttributeSelectionToServer received on client.");
        }
        else if(sideReceived.isServer()) {
            if(!msg.isValid) return;
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                if(player.isAlive()) ctx.enqueueWork(() -> doServerWork(msg, player));
            }
            else Nightfall.LOGGER.warn("ServerPlayer is null or dead.");
        }
    }

    private static void doServerWork(AttributeSelectionToServer msg, ServerPlayer player) {
        IPlayerData capP = PlayerData.get(player);
        for(PlayerAttribute attribute : msg.attributes.keySet()) {
            capP.setAttributePoints(attribute, msg.attributes.get(attribute));
        }
        capP.setNeedsAttributeSelection(false);
        player.setHealth(player.getMaxHealth());
        capP.setStamina(AttributesNF.getMaxStamina(player));
    }
}
