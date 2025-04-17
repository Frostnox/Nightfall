package frostnox.nightfall.network.message.world;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.IMicroGrid;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Used for updating a small amount of micro-grid cubes
 */
public class AnvilSlagToClient {
    private boolean isValid;
    private BlockPos blockEntityPos;
    private CompoundTag gridData;

    public AnvilSlagToClient(BlockPos blockEntityPos, CompoundTag gridData) {
        this.blockEntityPos = blockEntityPos;
        this.gridData = gridData;
        isValid = true;
    }

    private AnvilSlagToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeBlockPos(blockEntityPos);
            b.writeNbt(gridData);
        }
    }

    public static AnvilSlagToClient read(FriendlyByteBuf b) {
        AnvilSlagToClient msg = new AnvilSlagToClient();
        msg.blockEntityPos = b.readBlockPos();
        msg.gridData = b.readNbt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(AnvilSlagToClient msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
            ctx.enqueueWork(() -> {
                if(!world.isPresent()) {
                    Nightfall.LOGGER.warn("ClientLevel does not exist.");
                    return;
                }
                if(!world.get().isLoaded(msg.blockEntityPos) || !(world.get().getBlockEntity(msg.blockEntityPos) instanceof TieredAnvilBlockEntity anvil)) {
                    return;
                }
                for(int x = 0; x < anvil.getGridXSize(); x++) {
                    for(int y = 0; y < anvil.getGridYSize(); y++) {
                        for(int z = 0; z < anvil.getGridZSize(); z++) {
                            String id = IMicroGrid.idFromPos(x, y, z);
                            if(msg.gridData.getAllKeys().contains(id)) anvil.slag[x][y][z] = msg.gridData.getBoolean(id);
                        }
                    }
                }
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("AnvilSlagToClient received on server.");
        }
    }
}
