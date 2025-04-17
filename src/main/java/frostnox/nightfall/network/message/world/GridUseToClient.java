package frostnox.nightfall.network.message.world;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.IMicroGrid;
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
public class GridUseToClient {
    private boolean isValid;
    private BlockPos blockEntityPos;
    private CompoundTag gridData;

    public GridUseToClient(BlockPos blockEntityPos, CompoundTag gridData) {
        this.blockEntityPos = blockEntityPos;
        this.gridData = gridData;
        isValid = true;
    }

    private GridUseToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeBlockPos(blockEntityPos);
            b.writeNbt(gridData);
        }
    }

    public static GridUseToClient read(FriendlyByteBuf b) {
        GridUseToClient msg = new GridUseToClient();
        msg.blockEntityPos = b.readBlockPos();
        msg.gridData = b.readNbt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(GridUseToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
                if(!world.get().isLoaded(msg.blockEntityPos) || !(world.get().getBlockEntity(msg.blockEntityPos) instanceof IMicroGrid gridEntity)) {
                    return;
                }
                for(int x = 0; x < gridEntity.getGridXSize(); x++) {
                    for(int y = 0; y < gridEntity.getGridYSize(); y++) {
                        for(int z = 0; z < gridEntity.getGridZSize(); z++) {
                            String id = IMicroGrid.idFromPos(x, y, z);
                            if(msg.gridData.getAllKeys().contains(id)) gridEntity.getGrid()[x][y][z] = msg.gridData.getBoolean(id);
                        }
                    }
                }
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("GridUseToClient received on server.");
        }
    }
}
