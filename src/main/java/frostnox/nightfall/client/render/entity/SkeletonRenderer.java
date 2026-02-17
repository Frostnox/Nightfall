package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.SkeletonModel;
import frostnox.nightfall.client.render.RenderTypeNF;
import frostnox.nightfall.client.render.entity.layer.ArmorLayer;
import frostnox.nightfall.client.render.entity.layer.EquipmentLayer;
import frostnox.nightfall.client.render.entity.layer.GlowingLayer;
import frostnox.nightfall.client.render.entity.layer.HeldItemLayer;
import frostnox.nightfall.entity.entity.monster.SkeletonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SkeletonRenderer extends UndeadMobRenderer<SkeletonEntity, SkeletonModel> {
    public static final ResourceLocation TEXTURE_0 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/skeleton/skeleton_0.png");
    public static final ResourceLocation TEXTURE_1 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/skeleton/skeleton_1.png");
    public static final ResourceLocation TEXTURE_2 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/skeleton/skeleton_2.png");
    public static final ResourceLocation TEXTURE_3 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/skeleton/skeleton_3.png");
    public static final ResourceLocation TEXTURE_4 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/skeleton/skeleton_4.png");

    public SkeletonRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new SkeletonModel(renderer.bakeLayer(ModelRegistryNF.SKELETON)), 0.45F);
        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new GlowingLayer<>(this, 97.5, RenderTypeNF.ENTITY_EYES.apply(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/skeleton/skeleton_eyes.png"))));
        this.addLayer(new ArmorLayer<>(this, renderer));
        this.addLayer(new EquipmentLayer<>(this, renderer));
    }

    @Override
    public ResourceLocation getTextureLocation(SkeletonEntity entity) {
        int i = entity.getSynchedRandom() % 100;
        if(i < 24) return TEXTURE_0;
        else if(i < 48) return TEXTURE_1;
        else if(i < 72) return TEXTURE_2;
        else if(i < 96) return TEXTURE_3;
        else return TEXTURE_4;
    }
}