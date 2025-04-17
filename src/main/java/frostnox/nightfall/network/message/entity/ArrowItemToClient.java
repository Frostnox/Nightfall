package frostnox.nightfall.network.message.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.entity.entity.projectile.ArrowEntity;
import frostnox.nightfall.item.IProjectileItem;
import frostnox.nightfall.registry.forge.ItemsNF;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class ArrowItemToClient {
    private int entityID;
    private IProjectileItem item;
    private boolean isValid;

    public ArrowItemToClient(IProjectileItem item, int entityID) {
        this.item = item;
        this.entityID = entityID;
        isValid = true;
    }

    private ArrowItemToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeUtf(item.getItem().getRegistryName().toString());
            b.writeInt(entityID);
        }
    }

    public static ArrowItemToClient read(FriendlyByteBuf b) {
        ArrowItemToClient msg = new ArrowItemToClient();
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(b.readUtf()));
        if(item instanceof IProjectileItem) msg.item = (IProjectileItem) item;
        else msg.item = ItemsNF.FLINT_ARROW.get();
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(ArrowItemToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
            Nightfall.LOGGER.warn("ArrowItemToClient received on server.");
        }
    }

    private static void doClientWork(ArrowItemToClient msg, Level world) {
        if(!(world.getEntity(msg.entityID) instanceof ArrowEntity arrow)) {
            Nightfall.LOGGER.warn("Entity is invalid, expected ArrowEntity.");
            return;
        }
        arrow.setItem(msg.item);
    }
}
