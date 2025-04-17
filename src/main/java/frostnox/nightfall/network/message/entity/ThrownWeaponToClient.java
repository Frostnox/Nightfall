package frostnox.nightfall.network.message.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.entity.entity.projectile.ThrownWeaponEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class ThrownWeaponToClient {
    private int entityID;
    private ItemStack item;
    private ResourceLocation actionID;
    private boolean isValid;

    public ThrownWeaponToClient(ItemStack item, ResourceLocation actionID, int entityID) {
        this.item = item;
        this.actionID = actionID;
        this.entityID = entityID;
        isValid = true;
    }

    private ThrownWeaponToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeItem(item);
            b.writeResourceLocation(actionID);
            b.writeInt(entityID);
        }
    }

    public static ThrownWeaponToClient read(FriendlyByteBuf b) {
        ThrownWeaponToClient msg = new ThrownWeaponToClient();
        msg.item = b.readItem();
        msg.actionID = b.readResourceLocation();
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(ThrownWeaponToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
            Nightfall.LOGGER.warn("ThrownWeaponToClient received on server.");
        }
    }

    private static void doClientWork(ThrownWeaponToClient msg, Level world) {
        if(!(world.getEntity(msg.entityID) instanceof ThrownWeaponEntity thrownWeapon)) {
            Nightfall.LOGGER.warn("Entity is invalid, expected ThrownWeaponEntity.");
            return;
        }
        thrownWeapon.setItem(msg.item);
        thrownWeapon.setAction(msg.actionID, (LivingEntity) thrownWeapon.getOwner());
    }
}
