package frostnox.nightfall.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.OrientedEntityHitResult;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements ResourceManagerReloadListener, AutoCloseable {
    @Inject(method = "resize", at = @At("HEAD"))
    private void nightfall$resizeShaders(int width, int height, CallbackInfo callbackInfo) {
        if(ClientEngine.get() != null) ClientEngine.get().resize(width, height);
    }

    /**
     * Cancel view bob since it is applied to the camera directly now
     */
    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void nightfall$stopBobView(GameRenderer renderer, PoseStack stack, float partialTick) {

    }

    /**
     * Support for entities implementing {@link frostnox.nightfall.entity.IOrientedHitBoxes}
     */
    @Redirect(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"))
    private EntityHitResult nightfall$changePickResult(Entity viewer, Vec3 start, Vec3 end, AABB box, Predicate<Entity> filter, double lengthSqr) {
        Level level = viewer.level;
        double bestDistSqr = lengthSqr;
        Entity bestEntity = null;
        Vec3 bestPoint = null;
        int boxIndex = -1;
        for(Entity entity : level.getEntities(viewer, box.inflate(IOrientedHitBoxes.MAX_DIST_FROM_AABB), filter)) {
            if(!(entity instanceof IOrientedHitBoxes hitBoxesEntity) || hitBoxesEntity.includeAABB()) {
                AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
                Optional<Vec3> result = aabb.clip(start, end);
                if(aabb.contains(start)) {
                    if(bestDistSqr >= 0.0D) {
                        bestEntity = entity;
                        bestPoint = result.orElse(start);
                        bestDistSqr = 0.0D;
                    }
                }
                else if(result.isPresent() && box.contains(result.get())) {
                    Vec3 point = result.get();
                    double distSqr = start.distanceToSqr(point);
                    if(distSqr < bestDistSqr || bestDistSqr == 0.0D) {
                        if(entity.getRootVehicle() == viewer.getRootVehicle() && !entity.canRiderInteract()) {
                            if(bestDistSqr == 0.0D) {
                                bestEntity = entity;
                                bestPoint = point;
                            }
                        }
                        else {
                            bestEntity = entity;
                            bestPoint = point;
                            bestDistSqr = distSqr;
                        }
                    }
                }
            }
            if(entity instanceof IOrientedHitBoxes hitBoxesEntity) {
                Vec3 startOrigin = start.subtract(entity.position()), endOrigin = end.subtract(entity.position());
                OBB[] obbs = hitBoxesEntity.getOBBs(1F);
                for(int i = 0; i < obbs.length; i++) {
                    OBB obb = obbs[i];
                    obb.extents = obb.extents.scale(1D + entity.getPickRadius());
                    Optional<Vec3> obbPoint = obb.rayCast(startOrigin, endOrigin);
                    if(obb.contains(startOrigin)) {
                        if(bestDistSqr >= 0.0D) {
                            bestEntity = entity;
                            bestPoint = obbPoint.isPresent() ? obbPoint.get().add(entity.position()) : start;
                            bestDistSqr = 0.0D;
                            boxIndex = i;
                        }
                    }
                    else if(obbPoint.isPresent() && box.contains(obbPoint.get().add(entity.position()))) { //Ray is infinite so check box again
                        double distSqr = startOrigin.distanceToSqr(obbPoint.get());
                        if(distSqr < bestDistSqr || bestDistSqr == 0.0D) {
                            if(entity.getRootVehicle() == viewer.getRootVehicle() && !entity.canRiderInteract()) {
                                if(bestDistSqr == 0.0D) {
                                    bestEntity = entity;
                                    bestPoint = obbPoint.get().add(entity.position());
                                    boxIndex = i;
                                }
                            }
                            else {
                                bestEntity = entity;
                                bestPoint = obbPoint.get().add(entity.position());
                                bestDistSqr = distSqr;
                                boxIndex = i;
                            }
                        }
                    }
                }
            }
        }
        return bestEntity == null ? null : new OrientedEntityHitResult(bestEntity, bestPoint, boxIndex);
    }
}
