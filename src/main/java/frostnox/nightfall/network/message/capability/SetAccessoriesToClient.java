package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.world.inventory.AccessoryInventory;
import frostnox.nightfall.world.inventory.AccessorySlot;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class SetAccessoriesToClient {
    private List<Pair<AccessorySlot, ItemStack>> accessories;
    private int entityID;
    private boolean isValid;

    public SetAccessoriesToClient(List<Pair<AccessorySlot, ItemStack>> accessories, int entityID) {
        this.accessories = accessories;
        this.entityID = entityID;
        isValid = true;
    }

    private SetAccessoriesToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeVarInt(entityID);
            int size = accessories.size();
            for(int i = 0; i < size; i++) {
                Pair<AccessorySlot, ItemStack> pair = accessories.get(i);
                boolean endFlag = i != size - 1;
                int ordinal = pair.left().ordinal();
                b.writeByte(endFlag ? ordinal | Byte.MIN_VALUE : ordinal);
                b.writeItem(pair.right());
                i++;
            }
        }
    }

    public static SetAccessoriesToClient read(FriendlyByteBuf b) {
        SetAccessoriesToClient msg = new SetAccessoriesToClient();
        msg.entityID = b.readVarInt();
        AccessorySlot[] accessorySlots = AccessorySlot.values();
        msg.accessories = new ObjectArrayList<>(1);
        int i;
        do {
            i = b.readByte();
            AccessorySlot slot = accessorySlots[i & Byte.MAX_VALUE];
            ItemStack item = b.readItem();
            msg.accessories.add(Pair.of(slot, item));
        } while((i & Byte.MIN_VALUE) != 0);
        msg.isValid = true;
        return msg;
    }

    public static void handle(SetAccessoriesToClient msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        if(!msg.isValid) {
            Nightfall.LOGGER.warn("SetAccessoriesToClient is invalid.");
            return;
        }
        //Handle by side
        if(sideReceived.isClient()) {
            Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
            ctx.enqueueWork(() -> {
                if(!world.isPresent()) {
                    Nightfall.LOGGER.warn("Level could not be found.");
                    return;
                }
                Entity entity = world.get().getEntity(msg.entityID);
                if(entity instanceof Player player && player.isAlive()) {
                    IPlayerData capP = PlayerData.get(player);
                    AccessoryInventory accessories = capP.getAccessoryInventory();
                    for(Pair<AccessorySlot, ItemStack> pair : msg.accessories) {
                        accessories.setItem(pair.left(), pair.right());
                    }
                }
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("SetAccessoriesToClient received on server.");
        }
    }
}
