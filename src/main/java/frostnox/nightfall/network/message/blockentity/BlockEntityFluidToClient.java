package frostnox.nightfall.network.message.blockentity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.crucible.CrucibleBlockEntity;
import frostnox.nightfall.block.block.mold.ItemMoldBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class BlockEntityFluidToClient {
    private boolean isValid;
    private BlockPos blockEntityPos;
    private FluidStack fluidResult;

    public BlockEntityFluidToClient(BlockPos blockEntityPos, FluidStack fluidResult) {
        this.blockEntityPos = blockEntityPos;
        this.fluidResult = fluidResult;
        isValid = true;
    }

    private BlockEntityFluidToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeBlockPos(blockEntityPos);
            b.writeFluidStack(fluidResult);
            b.writeEnum(TieredHeat.RED);
        }
    }

    public static BlockEntityFluidToClient read(FriendlyByteBuf b) {
        BlockEntityFluidToClient msg = new BlockEntityFluidToClient();
        msg.blockEntityPos = b.readBlockPos();
        msg.fluidResult = b.readFluidStack();
        msg.isValid = true;
        return msg;
    }

    public static void handle(BlockEntityFluidToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
                if(!world.get().isLoaded(msg.blockEntityPos)) return;
                BlockEntity blockEntity = world.get().getBlockEntity(msg.blockEntityPos);
                if(blockEntity instanceof ItemMoldBlockEntity mold) mold.setInputFluid(msg.fluidResult);
            });
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("BlockEntityFluidToClient received on server.");
        }
    }
}
