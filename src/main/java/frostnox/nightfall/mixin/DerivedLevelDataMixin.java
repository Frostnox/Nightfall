package frostnox.nightfall.mixin;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DerivedLevelData.class)
public abstract class DerivedLevelDataMixin implements ServerLevelData {
    @Inject(method = "getDifficulty", at = @At("TAIL"), cancellable = true)
    private void nightfall$getDifficulty(CallbackInfoReturnable<Difficulty> callbackInfo) {
        Difficulty difficulty = callbackInfo.getReturnValue();
        if(difficulty == Difficulty.EASY || difficulty == Difficulty.HARD) callbackInfo.setReturnValue(Difficulty.NORMAL);
    }
}
