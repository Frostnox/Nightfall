package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.ScarabModel;
import frostnox.nightfall.entity.entity.monster.ScarabEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ScarabRenderer extends AnimatedMobRenderer<ScarabEntity, ScarabModel> {
    public static final ResourceLocation STRIPELESS = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/scarab/stripeless.png");

    public ScarabRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new ScarabModel(renderer.bakeLayer(ModelRegistryNF.SCARAB)), 0.225F);
    }

    @Override
    public ResourceLocation getTextureLocation(ScarabEntity pEntity) {
        return STRIPELESS;
    }
}
