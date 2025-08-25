package frostnox.nightfall.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.EctoplasmInnerModel;
import frostnox.nightfall.client.model.entity.EctoplasmOuterModel;
import frostnox.nightfall.client.render.entity.layer.TranslucentBodyLayer;
import frostnox.nightfall.entity.entity.monster.EctoplasmEntity;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class EctoplasmRenderer extends AnimatedMobRenderer<EctoplasmEntity, AnimatedModel<EctoplasmEntity>> {
    public static final ResourceLocation LARGE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/ectoplasm/large.png");

    public EctoplasmRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new EctoplasmInnerModel(renderer.bakeLayer(ModelRegistryNF.ECTOPLASM_LARGE_INNER)), 0.75F);
        addLayer(new TranslucentBodyLayer<>(this, new EctoplasmOuterModel(renderer.bakeLayer(ModelRegistryNF.ECTOPLASM_LARGE_OUTER))) {
            @Override
            protected float getAlpha(EctoplasmEntity entity) {
                return Math.min(super.getAlpha(entity), entity.getTransparency());
            }
        });
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
        if(this.isShaking(pEntityLiving)) pRotationYaw += (float)(Math.cos((double)pEntityLiving.tickCount * 3.25D) * Math.PI * (double)0.4F);
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pRotationYaw));
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
            if(capA.isStunned()) {
                float progress = AnimationUtil.getStunProgress(capA.getStunFrame(), capA.getStunDuration(), pPartialTickTime);
                if(capA.getStunFrame() < capA.getStunDuration() / 2) progress = Easing.outQuart.apply(progress);
                else Easing.inOutSine.apply(progress);
                progress *= 0.08F;
                if(progress > highestProgress) highestProgress = progress;
            }
        }
        if(entity.deathTime > 0) {
            float progress = ((float) entity.deathTime + pPartialTickTime - 1.0F) / 20.0F * 1.6F;
            if(progress > 1.0F) progress = 1.0F;
            progress = Easing.outSine.apply(progress);
            progress *= 0.2F;
            if(progress > highestProgress) highestProgress = progress;
        }
        if(highestProgress > 0) pMatrixStack.scale(1F - highestProgress, 1F - highestProgress, 1F - highestProgress);
    }

    @Override
    public ResourceLocation getTextureLocation(EctoplasmEntity pEntity) {
        return LARGE;
    }
}
