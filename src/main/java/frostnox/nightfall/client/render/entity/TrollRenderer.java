package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.TrollModel;
import frostnox.nightfall.entity.entity.monster.TrollEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TrollRenderer extends AnimatedMobRenderer<TrollEntity, TrollModel> {
    public static final ResourceLocation STRIPELESS = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/troll/stripeless.png");

    public TrollRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new TrollModel(renderer.bakeLayer(ModelRegistryNF.TROLL)), 0.225F);
    }

    @Override
    public ResourceLocation getTextureLocation(TrollEntity pEntity) {
        return STRIPELESS;
    }
}
