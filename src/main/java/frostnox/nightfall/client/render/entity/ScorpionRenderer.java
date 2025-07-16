package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.ScorpionModel;
import frostnox.nightfall.entity.entity.monster.ScorpionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ScorpionRenderer extends AnimatedMobRenderer<ScorpionEntity, ScorpionModel> {
    public static final ResourceLocation STRIPELESS = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/scorpion/stripeless.png");

    public ScorpionRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new ScorpionModel(renderer.bakeLayer(ModelRegistryNF.SCORPION)), 0.225F);
    }

    @Override
    public ResourceLocation getTextureLocation(ScorpionEntity pEntity) {
        return STRIPELESS;
    }
}
