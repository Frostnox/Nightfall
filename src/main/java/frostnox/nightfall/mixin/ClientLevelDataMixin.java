package frostnox.nightfall.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.ClientLevelData.class)
public abstract class ClientLevelDataMixin implements WritableLevelData {
    @Inject(method = "getDifficulty", at = @At("TAIL"), cancellable = true)
    private void nightfall$getDifficulty(CallbackInfoReturnable<Difficulty> callbackInfo) {
        Difficulty difficulty = callbackInfo.getReturnValue();
        if(difficulty == Difficulty.EASY || difficulty == Difficulty.HARD) callbackInfo.setReturnValue(Difficulty.NORMAL);
    }
}
