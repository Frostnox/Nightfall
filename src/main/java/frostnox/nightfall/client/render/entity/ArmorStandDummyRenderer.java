package frostnox.nightfall.client.render.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.ArmorStandDummyModel;
import frostnox.nightfall.client.render.entity.layer.ArmorLayer;
import frostnox.nightfall.client.render.entity.layer.EquipmentLayer;
import frostnox.nightfall.client.render.entity.layer.HeldItemLayer;
import frostnox.nightfall.entity.entity.ArmorStandDummyEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.Map;

public class ArmorStandDummyRenderer extends LivingEntityRenderer<ArmorStandDummyEntity, ArmorStandDummyModel> {
    private static final Map<String, ResourceLocation> TEXTURE_CACHE = Maps.newHashMap();

    public ArmorStandDummyRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new ArmorStandDummyModel(renderer.bakeLayer(ModelRegistryNF.ARMOR_STAND)), 0.0F);
        this.addLayer(new ArmorLayer<>(this, renderer));
        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new ElytraLayer<>(this, renderer.getModelSet()));
        this.addLayer(new EquipmentLayer<>(this, renderer));
    }

    @Override
    public ResourceLocation getTextureLocation(ArmorStandDummyEntity pEntity) {
        String key = pEntity.getMaterial();
        ResourceLocation texture = TEXTURE_CACHE.get(pEntity.getMaterial());
        if(texture == null) {
            int split = key.indexOf(":");
            String nameSpace = key.substring(0, split);
            String name = key.substring(split + 1);
            texture = ResourceLocation.fromNamespaceAndPath(nameSpace, "textures/entity/armorstand/" + name + ".png");
            TEXTURE_CACHE.put(key, texture);
        }
        return texture;
    }

    @Override
    protected void setupRotations(ArmorStandDummyEntity pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pRotationYaw));
        float f = (float)(pEntityLiving.level.getGameTime() - pEntityLiving.lastHit) + pPartialTicks;
        if (f < 5.0F) {
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(Mth.sin(f / 1.5F * (float)Math.PI) * 3.0F));
        }

    }

    @Override
    protected boolean shouldShowName(ArmorStandDummyEntity pEntity) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
        float f = pEntity.isCrouching() ? 32.0F : 64.0F;
        return !(d0 >= (double) (f * f)) && pEntity.isCustomNameVisible();
    }

    @Override
    @Nullable
    protected RenderType getRenderType(ArmorStandDummyEntity pLivingEntity, boolean pBodyVisible, boolean pTranslucent, boolean pGlowing) {
        if (!pLivingEntity.isMarker()) {
            return super.getRenderType(pLivingEntity, pBodyVisible, pTranslucent, pGlowing);
        } else {
            ResourceLocation resourcelocation = this.getTextureLocation(pLivingEntity);
            if (pTranslucent) {
                return RenderType.entityTranslucent(resourcelocation, false);
            } else {
                return pBodyVisible ? RenderType.entityCutoutNoCull(resourcelocation, false) : null;
            }
        }
    }

    @Override
    protected void scale(ArmorStandDummyEntity pLivingEntity, PoseStack pMatrixStack, float pPartialTickTime) {
        float f = 0.9375F;
        pMatrixStack.scale(f, f, f);
    }
}
