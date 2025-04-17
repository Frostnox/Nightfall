package frostnox.nightfall.network.message.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.entity.IHungerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class EatItemToClient {
    private int entityID;
    private ItemStack item;
    private boolean isValid;

    public EatItemToClient(ItemStack item, int entityID) {
        this.item = item;
        this.entityID = entityID;
        isValid = true;
    }

    private EatItemToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeItem(item);
            b.writeInt(entityID);
        }
    }

    public static EatItemToClient read(FriendlyByteBuf b) {
        EatItemToClient msg = new EatItemToClient();
        msg.item = b.readItem();
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(EatItemToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
            Nightfall.LOGGER.warn(EatItemToClient.class.getSimpleName() + " received on server.");
        }
    }

    private static void doClientWork(EatItemToClient msg, Level world) {
        if(!(world.getEntity(msg.entityID) instanceof IHungerEntity hungerEntity)) {
            Nightfall.LOGGER.warn("Invalid entity in " + EatItemToClient.class.getSimpleName());
            return;
        }
        hungerEntity.doEatParticlesClient(msg.item);
    }
}
