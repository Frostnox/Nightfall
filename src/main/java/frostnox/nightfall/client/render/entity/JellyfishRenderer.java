package frostnox.nightfall.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.JellyfishInnerModel;
import frostnox.nightfall.client.model.entity.JellyfishOuterModel;
import frostnox.nightfall.client.render.entity.layer.TranslucentBodyLayer;
import frostnox.nightfall.entity.entity.ambient.JellyfishEntity;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.EnumMap;

public class JellyfishRenderer extends AnimatedMobRenderer<JellyfishEntity, AnimatedModel<JellyfishEntity>> {
    protected static final float XZ_SCALE = 0.25F, Y_SCALE = 0.05F;
    protected static final EnumMap<JellyfishEntity.Type, ResourceLocation> TEXTURES;
    static {
        TEXTURES = new EnumMap<>(JellyfishEntity.Type.class);
        for(JellyfishEntity.Type type : JellyfishEntity.Type.values()) {
            TEXTURES.put(type, ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/jellyfish/" + type.name().toLowerCase() + ".png"));
        }
    }

    public JellyfishRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new JellyfishInnerModel(renderer.bakeLayer(ModelRegistryNF.JELLYFISH_INNER)), 0.225F);
        addLayer(new TranslucentBodyLayer<>(this, new JellyfishOuterModel(renderer.bakeLayer(ModelRegistryNF.JELLYFISH_OUTER))));
    }

    @Override
    protected int getBlockLightLevel(JellyfishEntity pEntity, BlockPos pPos) {
        return 15;
    }

    @Override
    protected void setupRotations(JellyfishEntity pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        if(!pEntityLiving.isInWaterOrBubble()) pMatrixStack.translate(0, -9F/16F, 0);
    }

    @Override
    protected void scale(JellyfishEntity entity, PoseStack pMatrixStack, float pPartialTickTime) {
        int propulsion = entity.getPropulsionTicks();
        if(propulsion >= 0) {
            float progress = Math.min(1, Mth.lerp(pPartialTickTime, propulsion + 1, propulsion) / (float) JellyfishEntity.PROPULSION_DURATION);
            if(entity.isDeflating()) {
                progress = Easing.inQuart.apply(progress);
                pMatrixStack.scale(1 + progress * XZ_SCALE, 1 + progress * Y_SCALE, 1 + progress * XZ_SCALE);
            }
            else {
                progress = Easing.inOutCubic.apply(progress);
                pMatrixStack.scale(1 + XZ_SCALE - progress * XZ_SCALE, 1 + Y_SCALE - progress * Y_SCALE, 1 + XZ_SCALE - progress * XZ_SCALE);
            }
        }
        if(entity.deathTime > 0) {
            float progress = ((float) entity.deathTime + pPartialTickTime - 1.0F) / 20.0F * 1.6F;
            if(progress > 1.0F) progress = 1.0F;
            progress = Easing.outSine.apply(progress);
            progress *= XZ_SCALE;
            pMatrixStack.scale(1F - progress, 1F - progress, 1F - progress);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(JellyfishEntity pEntity) {
        return TEXTURES.get(pEntity.getJellyfishType());
    }
}
