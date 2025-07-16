package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.OlmurModel;
import frostnox.nightfall.entity.entity.monster.OlmurEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class OlmurRenderer extends AnimatedMobRenderer<OlmurEntity, OlmurModel> {
    public static final ResourceLocation STRIPELESS = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/olmur/stripeless.png");

    public OlmurRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new OlmurModel(renderer.bakeLayer(ModelRegistryNF.OLMUR)), 0.225F);
    }

    @Override
    public ResourceLocation getTextureLocation(OlmurEntity pEntity) {
        return STRIPELESS;
    }
}
