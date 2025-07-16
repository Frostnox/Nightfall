package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.PitDevilModel;
import frostnox.nightfall.entity.entity.monster.PitDevilEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PitDevilRenderer extends AnimatedMobRenderer<PitDevilEntity, PitDevilModel> {
    public static final ResourceLocation STRIPELESS = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/pit_devil/stripeless.png");

    public PitDevilRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new PitDevilModel(renderer.bakeLayer(ModelRegistryNF.PIT_DEVIL)), 0.225F);
    }

    @Override
    public ResourceLocation getTextureLocation(PitDevilEntity pEntity) {
        return STRIPELESS;
    }
}
