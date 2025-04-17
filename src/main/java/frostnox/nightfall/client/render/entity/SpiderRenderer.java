package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.SpiderModel;
import frostnox.nightfall.client.render.RenderTypeNF;
import frostnox.nightfall.client.render.entity.layer.GlowingLayer;
import frostnox.nightfall.entity.entity.monster.SpiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SpiderRenderer extends AnimatedMobRenderer<SpiderEntity, SpiderModel> {
    public static final ResourceLocation BLACK = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/spider/black.png");
    public static final ResourceLocation BANDED = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/spider/banded.png");
    public static final ResourceLocation BROWN = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/spider/brown.png");

    public SpiderRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new SpiderModel(renderer.bakeLayer(ModelRegistryNF.SPIDER)), 0.25F);
        this.addLayer(new GlowingLayer<>(this, 75, RenderTypeNF.EYES.apply(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/spider/spider_eyes.png"))));
    }

    @Override
    protected float getFlipDegrees(SpiderEntity entity) {
        return 180F;
    }

    @Override
    public ResourceLocation getTextureLocation(SpiderEntity entity) {
        return switch(entity.getSpiderType()) {
            case BLACK -> BLACK;
            case BANDED -> BANDED;
            case BROWN -> BROWN;
        };
    }
}