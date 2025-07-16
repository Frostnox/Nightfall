package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.SlimeModel;
import frostnox.nightfall.entity.entity.monster.SlimeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SlimeRenderer extends AnimatedMobRenderer<SlimeEntity, SlimeModel> {
    public static final ResourceLocation STRIPELESS = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/slime/stripeless.png");

    public SlimeRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new SlimeModel(renderer.bakeLayer(ModelRegistryNF.SLIME)), 0.225F);
    }

    @Override
    public ResourceLocation getTextureLocation(SlimeEntity pEntity) {
        return STRIPELESS;
    }
}
