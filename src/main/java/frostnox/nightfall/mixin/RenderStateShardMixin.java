package frostnox.nightfall.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.ClientEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderStateShard.class)
public abstract class RenderStateShardMixin {
    @Inject(method = "lambda$static$28", at = @At("HEAD"))
    private static void nightfall$copyTranslucentSetupState(CallbackInfo callbackInfo) {
        if(!Minecraft.useShaderTransparency()) {
            if(Nightfall.isRubidiumLoaded) {
                //Set up target to capture translucent renders
                ClientEngine.get().getTranslucentTarget().bindWrite(false);
                //Disable fog to avoid nearby water pixels blending with distant foggy water pixels (fog is done in post)
                ClientEngine.get().tempFogStart = RenderSystem.getShaderFogStart();
                RenderSystem.setShaderFogStart(Float.MAX_VALUE);
            }
            else {
                //Extra params for core shader
                RenderSystem.setShaderTexture(6, ClientEngine.get().getTranslucentTarget().getDepthTextureId());
                ShaderInstance translucentShader = GameRenderer.getRendertypeTranslucentShader();
                if(translucentShader.SCREEN_SIZE != null) {
                    Window window = Minecraft.getInstance().getWindow();
                    translucentShader.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
                }
                if(translucentShader.LINE_WIDTH != null) { //Use empty parameter to store depth modifier
                    float depth = RenderSystem.getShaderFogEnd() / 16F;
                    if(depth > 4F) depth = Math.min(20F, depth + depth - 4F);
                    translucentShader.LINE_WIDTH.set(depth);
                }
            }
        }
    }


    @Inject(method = "lambda$static$29", at = @At("HEAD"))
    private static void nightfall$doTranslucentClearState(CallbackInfo callbackInfo) {
        if(!Minecraft.useShaderTransparency()) {
            if(Nightfall.isRubidiumLoaded) {
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                RenderSystem.setShaderFogStart(ClientEngine.get().tempFogStart);
            }
            else RenderSystem.setShaderTexture(6, 0);
        }
    }
}
