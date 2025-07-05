package frostnox.nightfall.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientboundUpdateMobEffectPacket.class)
public abstract class ClientboundUpdateMobEffectPacketMixin implements Packet<ClientGamePacketListener> {
    @ModifyConstant(method = "<init>(ILnet/minecraft/world/effect/MobEffectInstance;)V", constant = @Constant(intValue = 32767))
    private int nightfall$adjustMaxDuration(int i) {
        return 20 * 60 * 60 * 24; //1 day
    }

    @ModifyConstant(method = "isSuperLongDuration", constant = @Constant(intValue = 32767))
    private int nightfall$adjustMaxDuration2(int i) {
        return 20 * 60 * 60 * 24; //1 day
    }
}
