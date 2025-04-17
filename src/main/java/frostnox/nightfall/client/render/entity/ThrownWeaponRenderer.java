package frostnox.nightfall.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.data.extensible.TransformTypeNF;
import frostnox.nightfall.entity.entity.projectile.ThrownWeaponEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ThrownWeaponRenderer extends EntityRenderer<ThrownWeaponEntity> {
    public ThrownWeaponRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownWeaponEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(pEntity.getPickupItem(), Minecraft.getInstance().level, null, pEntity.tickCount);
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot()) - 90.0F));
        if(pEntity.spinning && !pEntity.inGround()) {
            float speed = 64 * Math.min((float) pEntity.getDeltaMovement().horizontalDistance(), 1F);
            float tick = pEntity.getAirTicks() * speed;
            pMatrixStack.mulPose(Vector3f.ZN.rotationDegrees(Mth.lerp(pPartialTicks, tick - speed, tick)));
        }
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot()) - 90.0F + pEntity.getCurve()));
        Minecraft.getInstance().getItemRenderer().render(pEntity.getPickupItem(), TransformTypeNF.THROWN, false, pMatrixStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY, model);
        pMatrixStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownWeaponEntity pEntity) {
        return null;
    }
}
