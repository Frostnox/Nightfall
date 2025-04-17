package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.DregModel;
import frostnox.nightfall.client.render.RenderTypeNF;
import frostnox.nightfall.client.render.entity.layer.*;
import frostnox.nightfall.entity.entity.monster.DregEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DregRenderer extends UndeadMobRenderer<DregEntity, DregModel> {
    public static final ResourceLocation TEXTURE_0 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/dreg/dreg_0.png");
    public static final ResourceLocation TEXTURE_1 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/dreg/dreg_1.png");
    public static final ResourceLocation TEXTURE_2 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/dreg/dreg_2.png");
    public static final ResourceLocation TEXTURE_3 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/dreg/dreg_3.png");
    public static final ResourceLocation TEXTURE_4 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/dreg/dreg_4.png");

    public DregRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new DregModel(renderer.bakeLayer(ModelRegistryNF.DREG)), 0.375F);
        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new GlowingLayer<>(this, 95, RenderTypeNF.EYES.apply(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/dreg/dreg_eyes.png"))));
        this.addLayer(new ArmorLayer<>(this, renderer));
        this.addLayer(new EquipmentLayer<>(this, renderer));
    }

    @Override
    public ResourceLocation getTextureLocation(DregEntity entity) {
        int i = entity.getSynchedRandom() % 100;
        if(i < 24) return TEXTURE_0;
        else if(i < 48) return TEXTURE_1;
        else if(i < 72) return TEXTURE_2;
        else if(i < 96) return TEXTURE_3;
        else return TEXTURE_4;
    }
}
