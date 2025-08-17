package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.PitDevilModel;
import frostnox.nightfall.entity.entity.monster.PitDevilEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PitDevilRenderer extends AnimatedMobRenderer<PitDevilEntity, PitDevilModel> {
    public static final ResourceLocation STRIPELESS = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/pit_devil/stripeless.png");
    public static final ResourceLocation STRIPES_0 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/pit_devil/stripes_0.png");
    public static final ResourceLocation STRIPES_1 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/pit_devil/stripes_1.png");
    public static final ResourceLocation STRIPES_2 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/pit_devil/stripes_2.png");
    public static final ResourceLocation SPECIAL = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/pit_devil/special.png");

    public PitDevilRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new PitDevilModel(renderer.bakeLayer(ModelRegistryNF.PIT_DEVIL)), 0.325F);
    }

    @Override
    public ResourceLocation getTextureLocation(PitDevilEntity pEntity) {
        if(pEntity.isSpecial()) return SPECIAL;
        int i = pEntity.getSynchedRandom() % 100;
        if(i < 10) return STRIPELESS;
        else if(i < 40) return STRIPES_0;
        else if(i < 70) return STRIPES_1;
        else return STRIPES_2;
    }
}
