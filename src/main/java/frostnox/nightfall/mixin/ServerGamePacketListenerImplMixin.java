package frostnox.nightfall.mixin;

import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements ServerPlayerConnection, ServerGamePacketListener {
    @ModifyConstant(method = "handleSetCreativeModeSlot", constant = @Constant(intValue = 45))
    private int nightfall$increaseSlotIndexLimit(int maxSlotIndex) {
        return 48;
    }
}
