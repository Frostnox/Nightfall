package frostnox.nightfall.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.entity.entity.monster.UndeadEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;

public class GlowingLayer<T extends LivingEntity, M extends AnimatedModel<T>> extends RenderLayer<T, M> {
    private final double cullDistSqr;
    private final RenderType renderType;

    public GlowingLayer(RenderLayerParent<T, M> layer, double cullDist, RenderType renderType) {
        super(layer);
        this.cullDistSqr = cullDist * cullDist;
        this.renderType = renderType;
    }

    @Override
    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if(pLivingEntity.deathTime > 10 && !(pLivingEntity instanceof UndeadEntity undead && undead.isResurrecting())) return;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        //Cull if far away so pixels don't flicker
        if(pLivingEntity.getEyePosition(pPartialTicks).distanceToSqr(camera.getPosition()) * ClientEngine.get().getNormalizedFov() > cullDistSqr) return;
        getParentModel().renderToBuffer(pMatrixStack, pBuffer.getBuffer(renderType), 0xf000f0, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
    }
}
