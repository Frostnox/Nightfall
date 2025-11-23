package frostnox.nightfall.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.entity.monster.UndeadEntity;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;

public abstract class AnimatedMobRenderer<T extends ActionableEntity, M extends AnimatedModel<T>> extends MobRenderer<T, M> {
    public AnimatedMobRenderer(EntityRendererProvider.Context renderer, M entity, float shadowRadius) {
        super(renderer, entity, shadowRadius);
    }

    @Override
    public void render(T entity, float p_225623_2_, float partialTick, PoseStack matrix, MultiBufferSource buffers, int light) {
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Pre<T, M>(entity, this, partialTick, matrix, buffers, light))) return;
        matrix.pushPose();
        this.model.attackTime = this.getAttackAnim(entity, partialTick);

        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
        this.model.riding = shouldSit;
        this.model.young = entity.isBaby();
        float f = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float f1 = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = f1 - f;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity.getVehicle();
            f = Mth.rotLerp(partialTick, livingentity.yBodyRotO, livingentity.yBodyRot);
            netHeadYaw = f1 - f;
            float f3 = Mth.wrapDegrees(netHeadYaw);
            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            f = f1 - f3;
            if (f3 * f3 > 2500.0F) {
                f += f3 * 0.2F;
            }

            netHeadYaw = f1 - f;
        }
        netHeadYaw = Mth.wrapDegrees(netHeadYaw);

        float headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        if (entity.getPose() == Pose.SLEEPING) {
            Direction direction = entity.getBedOrientation();
            if (direction != null) {
                float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                matrix.translate((double)((float)(-direction.getStepX()) * f4), 0.0D, (double)((float)(-direction.getStepZ()) * f4));
            }
        }

        float time = this.getBob(entity, partialTick);
        this.setupRotations(entity, matrix, time, f, partialTick);
        matrix.scale(-1.0F, -1.0F, 1.0F);
        this.scale(entity, matrix, partialTick);
        matrix.translate(0.0D, (double)-1.501F, 0.0D);
        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;
        if (!shouldSit && entity.isAlive()) {
            if(entity.animationSpeed == 1.5F) entity.animationSpeed = entity.animationSpeedOld;
            limbSwingAmount = Mth.lerp(partialTick, entity.animationSpeedOld, entity.animationSpeed);
            limbSwing = entity.animationPosition - entity.animationSpeed * (1.0F - partialTick);
            if (entity.isBaby()) {
                limbSwing *= 3.0F;
            }

            if (limbSwingAmount > 1.0F) {
                limbSwingAmount = 1.0F;
            }
        }

        this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, time, netHeadYaw, headPitch);
        this.model.doCombatAnimations(entity, matrix);
        Minecraft minecraft = Minecraft.getInstance();
        float alpha = getAlpha(entity);
        boolean flag = this.isBodyVisible(entity);
        boolean flag1 = !flag && !entity.isInvisibleTo(minecraft.player);
        boolean flag2 = minecraft.shouldEntityAppearGlowing(entity);
        RenderType renderType = this.getRenderType(entity, flag, flag1 || alpha < 1.0F, flag2);
        if(renderType != null) {
            VertexConsumer ivertexbuilder = buffers.getBuffer(renderType);
            int i = AnimationUtil.getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTick));
            this.model.renderToBuffer(matrix, ivertexbuilder, light, i, 1.0F, 1.0F, 1.0F, alpha);
        }

        if(!entity.isSpectator()) {
            for(RenderLayer<T, M> layerrenderer : this.layers) {
                layerrenderer.render(matrix, buffers, light, entity, limbSwing, limbSwingAmount, partialTick, time, netHeadYaw, headPitch);
            }
        }

        matrix.popPose();
        net.minecraftforge.client.event.RenderNameplateEvent renderNameplateEvent = new net.minecraftforge.client.event.RenderNameplateEvent(entity, entity.getDisplayName(), this, matrix, buffers, light, partialTick);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
        if (renderNameplateEvent.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY && (renderNameplateEvent.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || this.shouldShowName(entity))) {
            this.renderNameTag(entity, renderNameplateEvent.getContent(), matrix, buffers, light);
        }
        Entity leashHolder = entity.getLeashHolder();
        if(leashHolder != null) renderLeash(entity, partialTick, matrix, buffers, leashHolder);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<T, M>(entity, this, partialTick, matrix, buffers, light));
    }

    @Override
    protected void setupRotations(T pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        if(this.isShaking(pEntityLiving)) {
            pRotationYaw += (float)(Math.cos((double)pEntityLiving.tickCount * 3.25D) * Math.PI * (double)0.4F);
        }
        Pose pose = pEntityLiving.getPose();
        if(pose != Pose.SLEEPING) pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pRotationYaw));

        if(pEntityLiving.deathTime > 0) {
            float f;
            if(pEntityLiving instanceof UndeadEntity undead && undead.isResurrecting()) f = AnimationUtil.applyEasing(Mth.clamp(((float)pEntityLiving.deathTime - pPartialTicks + 1.0F) / 20.0F, 0F, 1F), Easing.inOutSine);
            else {
                f = ((float) pEntityLiving.deathTime + pPartialTicks - 1.0F) / 20.0F * 1.6F;
                f = Mth.sqrt(f);
                if (f > 1.0F) {
                    f = 1.0F;
                }
            }
            int dir = pEntityLiving.getSynchedRandom() % 2 == 0 ? 1 : -1;
            EntityDimensions dimensions = pEntityLiving.getType().getDimensions();

            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * getFlipDegrees(pEntityLiving) * dir));
            pMatrixStack.translate(dimensions.width / 4F * f * dir, -dimensions.height / 2F * f, 0);
        }
        else if(isEntityUpsideDown(pEntityLiving)) {
            pMatrixStack.translate(0.0D, pEntityLiving.getBbHeight() + 0.1F, 0.0D);
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        }
    }

    protected float getAlpha(T entity) {
        return !isBodyVisible(entity) && !entity.isInvisibleTo(Minecraft.getInstance().player) ? 0.15F : 1.0F;
    }
}
