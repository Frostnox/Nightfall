package frostnox.nightfall.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.EntityLightEngine;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    /**
     * Add light from entities to block
     */
    @Inject(method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At("TAIL"), cancellable = true)
    private static void nightfall$addEntityLightToBlock(BlockAndTintGetter level, BlockState state, BlockPos pos, CallbackInfoReturnable<Integer> callbackInfo) {
        if(!state.isSolidRender(level, pos)) {
            callbackInfo.setReturnValue(EntityLightEngine.get().adjustPackedLight(pos, callbackInfo.getReturnValue()));
        }
    }

    /**
     * Update target just before rendering translucent blocks
     */
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLcom/mojang/math/Matrix4f;)V", ordinal = 5))
    private void nightfall$updateTranslucentTarget(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo callbackInfo) {
        updateTranslucentTarget();
    }

    /**
     * Same as above but for code targeting fabulous graphics setting
     */
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLcom/mojang/math/Matrix4f;)V", ordinal = 3))
    private void nightfall$updateTranslucentTargetFabulous(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo callbackInfo) {
        updateTranslucentTarget();
    }

    private static void updateTranslucentTarget() {
        if(Nightfall.isRubidiumLoaded) {
            RenderTarget translucentTarget = ClientEngine.get().getTranslucentTarget();
            translucentTarget.clear(Minecraft.ON_OSX);
            translucentTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
        }
        else if(!Minecraft.useShaderTransparency()) {
            RenderTarget translucentTarget = ClientEngine.get().getTranslucentTarget();
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
            translucentTarget.copyDepthFrom(main);
            GlStateManager._glBindFramebuffer(36160, main.frameBufferId);
        }
    }
}
