package frostnox.nightfall.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.entity.EctoplasmInnerModel;
import frostnox.nightfall.client.model.entity.EctoplasmOuterModel;
import frostnox.nightfall.client.render.entity.layer.EctoplasmBodyLayer;
import frostnox.nightfall.entity.entity.monster.EctoplasmEntity;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class EctoplasmRenderer extends AnimatedMobRenderer<EctoplasmEntity, AnimatedModel<EctoplasmEntity>> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/ectoplasm/ectoplasm.png");

    public EctoplasmRenderer(EntityRendererProvider.Context renderer, float shadowRadius, ModelLayerLocation inner, ModelLayerLocation outerInner, ModelLayerLocation outer) {
        super(renderer, new EctoplasmInnerModel(renderer.bakeLayer(inner)), shadowRadius);
        addLayer(new EctoplasmBodyLayer(this, new EctoplasmOuterModel(renderer.bakeLayer(outerInner))));
        addLayer(new EctoplasmBodyLayer(this, new EctoplasmOuterModel(renderer.bakeLayer(outer))));
    }

    @Override
    protected int getBlockLightLevel(EctoplasmEntity pEntity, BlockPos pPos) {
        return 15;
    }

    @Override
    protected float getFlipDegrees(EctoplasmEntity pLivingEntity) {
        return 0F;
    }

    @Override
    protected void setupRotations(EctoplasmEntity pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        if(isShaking(pEntityLiving)) pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-((float)(Math.cos((double)pEntityLiving.tickCount * 3.25D) * Math.PI * (double)0.4F))));
        if(isEntityUpsideDown(pEntityLiving)) {
            pMatrixStack.translate(0.0D, pEntityLiving.getBbHeight() + 0.1F, 0.0D);
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        }
    }

    @Override
    protected void scale(EctoplasmEntity entity, PoseStack pMatrixStack, float pPartialTickTime) {
        float highestProgress = 0;
        if(ActionTracker.isPresent(entity)) {
            IActionTracker capA = entity.getActionTracker();
            float animScale = 0.25F;
            if(capA.isStunned()) {
                float progress = AnimationUtil.getStunProgress(capA.getStunFrame(), capA.getStunDuration(), pPartialTickTime);
                if(capA.getStunFrame() < capA.getStunDuration() / 2) progress = Easing.outQuart.apply(progress);
                else Easing.inOutSine.apply(progress);
                animScale *= capA.getStunFrame() < capA.getStunDuration() / 2 ? (1F - progress) : 0;
                progress *= 0.08F;
                if(progress > highestProgress) highestProgress = progress;
            }
            if(capA.getState() <= 1 && (capA.getActionID().equals(ActionsNF.ECTOPLASM_EXPLODE_LARGE.getId()) || capA.getActionID().equals(ActionsNF.ECTOPLASM_EXPLODE_MEDIUM.getId()))) {
                float progress = (capA.getState() == 0 ? Easing.inOutSine.apply(capA.getProgress(pPartialTickTime)) : Easing.inQuart.apply(1F - capA.getProgress(pPartialTickTime))) * animScale;
                if(progress > highestProgress) highestProgress = progress;
            }
        }
        if(entity.deathTime > 0) {
            float progress = ((float) entity.deathTime + pPartialTickTime - 1.0F) / 20.0F * 1.6F;
            if(progress > 1.0F) progress = 1.0F;
            progress = Easing.outSine.apply(progress);
            progress *= 0.25F;
            if(progress > highestProgress) highestProgress = progress;
        }
        if(highestProgress > 0) pMatrixStack.scale(1F - highestProgress / 2, 1F - highestProgress, 1F - highestProgress / 2);
    }

    @Override
    public ResourceLocation getTextureLocation(EctoplasmEntity pEntity) {
        return TEXTURE;
    }
}
