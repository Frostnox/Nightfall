package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.SkullwormModel;
import frostnox.nightfall.entity.entity.monster.SkullwormEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SkullwormRenderer extends AnimatedMobRenderer<SkullwormEntity, SkullwormModel> {
    public static final ResourceLocation STRIPELESS = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/skullworm/stripeless.png");

    public SkullwormRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new SkullwormModel(renderer.bakeLayer(ModelRegistryNF.SKULLWORM)), 0.225F);
    }

    @Override
    public ResourceLocation getTextureLocation(SkullwormEntity pEntity) {
        return STRIPELESS;
    }
}
