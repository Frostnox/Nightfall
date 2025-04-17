package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class ActionTrackerToClient {
    private final CompoundTag NBT;

    public ActionTrackerToClient(CompoundTag NBT, int entityID) {
        NBT.putInt("entityid", entityID);
        this.NBT = NBT;
    }

    private ActionTrackerToClient(CompoundTag NBT) {
        this.NBT = NBT;
    }

    public static void write(ActionTrackerToClient msg, FriendlyByteBuf b) {
        b.writeNbt(msg.NBT);
    }

    public static ActionTrackerToClient read(FriendlyByteBuf b) {
        return new ActionTrackerToClient(b.readNbt());
    }

    public static void handle(ActionTrackerToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
            Nightfall.LOGGER.warn("ActionTrackerToClient received on server.");
        }
    }

    private static void doClientWork(ActionTrackerToClient msg, Level world) {
        if(!(world.getEntity(msg.NBT.getInt("entityid")) instanceof LivingEntity entity)) {
            Nightfall.LOGGER.warn("Entity is invalid.");
            return;
        }
        if(!entity.isAlive()) {
            Nightfall.LOGGER.warn("LivingEntity is null or dead.");
            return;
        }
        IActionTracker capA = ActionTracker.get(entity);
        capA.readNBT(msg.NBT);
    }
}
