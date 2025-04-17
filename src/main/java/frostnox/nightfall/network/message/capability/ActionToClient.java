package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class ActionToClient {
    private ResourceLocation actionID;
    private int entityID;
    private boolean isValid;

    public ActionToClient(ResourceLocation actionID, int entityID) {
        this.entityID = entityID;
        this.actionID = actionID;
        isValid = true;
    }

    private ActionToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(entityID);
            b.writeResourceLocation(actionID);
        }
    }

    public static ActionToClient read(FriendlyByteBuf b) {
        ActionToClient msg = new ActionToClient();
        msg.entityID = b.readInt();
        msg.actionID = b.readResourceLocation();
        msg.isValid = true;
        return msg;
    }

    public static void handle(ActionToClient msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        if(!msg.isValid) {
            Nightfall.LOGGER.warn("ActionToClient is invalid.");
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
                if(entity instanceof LivingEntity livingEntity) {
                    IActionTracker capA = ActionTracker.get(livingEntity);
                    capA.startAction(msg.actionID);
                }
                else Nightfall.LOGGER.warn("Entity is not an instance of LivingEntity.");
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("ActionToClient received on server.");
        }
    }
}
