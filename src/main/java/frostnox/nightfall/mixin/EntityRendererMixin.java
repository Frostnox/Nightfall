package frostnox.nightfall.mixin;

import frostnox.nightfall.capability.LightData;
import frostnox.nightfall.client.EntityLightEngine;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    /**
     * Add light from entities to other entities
     */
    @Inject(method = "getBlockLightLevel", at = @At("TAIL"), cancellable = true)
    private void nightfall$addEntityLightToEntity(T entity, BlockPos pos, CallbackInfoReturnable<Integer> callbackInfo) {
        int blockLight = callbackInfo.getReturnValue();
        double entityLight = EntityLightEngine.get().getLight(pos);
        if(entity.getCapability(LightData.CAPABILITY).isPresent()) {
            int selfLight = LightData.get(entity).getBrightness();
            if(selfLight > entityLight) entityLight = selfLight;
        }
        if(entityLight > blockLight) callbackInfo.setReturnValue((int) entityLight);
    }
}
