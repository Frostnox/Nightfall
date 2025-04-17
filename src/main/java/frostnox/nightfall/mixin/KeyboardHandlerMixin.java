package frostnox.nightfall.mixin;

import frostnox.nightfall.client.ClientEngine;
import net.minecraft.Util;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cancels various debug commands in survival mode.
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    private static final String ERROR = "debug.error";
    @Shadow @Final private Minecraft minecraft;
    @Shadow private long debugCrashKeyTime;

    @Shadow protected abstract void debugFeedbackTranslated(String pMessage, Object... pArgs);

    @Inject(method = "handleDebugKeys", at = @At(value = "HEAD"), cancellable = true)
    private void nightfall$handleDebugKeys(int key, CallbackInfoReturnable<Boolean> callbackInfo) {
        if(debugCrashKeyTime > 0L && debugCrashKeyTime < Util.getMillis() - 100L) return; //Check guard clause
        if(minecraft.player == null || minecraft.player.isCreative() || minecraft.player.isSpectator() || ClientEngine.get().isDevVersion()) return; //Target survival/adventure mode players
        if(key == 66 && minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes()) return; //Allow disabling hitboxes
        if(key == 71 && !minecraft.debugRenderer.switchRenderChunkborder()) { //Allow disabling chunk borders
            minecraft.debugRenderer.switchRenderChunkborder(); //Re-switch after checking
            return;
        }
        if(key == 66 || key == 67 || key == 71 || key == 73 || key == 76) {
            if(key == 71) minecraft.debugRenderer.switchRenderChunkborder(); //Re-switch from failed check earlier
            debugFeedbackTranslated(ERROR, minecraft.gameMode == null ? "" : minecraft.gameMode.getPlayerMode().getName());
            callbackInfo.setReturnValue(true); //Return true to release F3 key
        }
    }
}
