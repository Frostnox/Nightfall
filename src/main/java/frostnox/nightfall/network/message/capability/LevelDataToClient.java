package frostnox.nightfall.network.message.capability;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ILevelData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.client.render.ContinentalEffects;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LevelDataToClient {
    private final CompoundTag NBT;

    public LevelDataToClient(CompoundTag NBT) {
        this.NBT = NBT;
    }

    public static void write(LevelDataToClient msg, FriendlyByteBuf b) {
        b.writeNbt(msg.NBT);
    }

    public static LevelDataToClient read(FriendlyByteBuf b) {
        return new LevelDataToClient(b.readNbt());
    }

    public static void handle(LevelDataToClient msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        ctx.get().enqueueWork(() -> {
            Level level;
            if(ctx.get().getDirection().getReceptionSide().isClient()) level = LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide()).orElse(null);
            else {
                Nightfall.LOGGER.error("Received LevelDataToClient on server from " + ctx.get().getSender().getName().getString());
                return;
            }
            if(level == null || !LevelData.isPresent(level)) return;
            ILevelData c = LevelData.get(level);
            c.readNBT(msg.NBT);
            if(msg.NBT.contains("seed")) {
                c.updateWeather();
                c.updateWind();
                ((ContinentalEffects) DimensionSpecialEffects.EFFECTS.get(ContinentalWorldType.LOCATION)).init(c.getSeed());
            }
            if(msg.NBT.contains("weatherIntensity")) {
                c.updateWeather();
                ((ContinentalEffects) DimensionSpecialEffects.EFFECTS.get(ContinentalWorldType.LOCATION)).regenClouds = true;
            }
        });
    }
}
