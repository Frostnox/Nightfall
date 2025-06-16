package frostnox.nightfall.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.util.AnimationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public class TranslucentBodyLayer<T extends LivingEntity, M extends AnimatedModel<T>> extends RenderLayer<T, M> {
    protected final M model;

    public TranslucentBodyLayer(RenderLayerParent<T, M> renderer, M model) {
        super(renderer);
        this.model = model;
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T entity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        Minecraft mc = Minecraft.getInstance();
        boolean invisibleGlow = mc.shouldEntityAppearGlowing(entity) && entity.isInvisible();
        if(!entity.isInvisible() || invisibleGlow) {
            VertexConsumer buffer;
            if(invisibleGlow) buffer = pBuffer.getBuffer(RenderType.outline(getTextureLocation(entity)));
            else buffer = pBuffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
            getParentModel().copyPropertiesTo(model);
            model.prepareMobModel(entity, pLimbSwing, pLimbSwingAmount, pPartialTick);
            model.setupAnim(entity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
            model.renderToBuffer(pPoseStack, buffer, pPackedLight, AnimationUtil.getOverlayCoords(entity, 0), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
