package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.RabbitModel;
import frostnox.nightfall.entity.entity.animal.RabbitEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RabbitRenderer extends AnimatedMobRenderer<RabbitEntity, RabbitModel> {
    public static final ResourceLocation BRUSH = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/rabbit/brush.png");
    public static final ResourceLocation COTTONTAIL = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/rabbit/cottontail.png");
    public static final ResourceLocation ARCTIC = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/rabbit/arctic.png");
    public static final ResourceLocation STRIPED = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/rabbit/striped.png");

    public RabbitRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new RabbitModel(renderer.bakeLayer(ModelRegistryNF.RABBIT)), 0.225F);
    }

    @Override
    public ResourceLocation getTextureLocation(RabbitEntity pEntity) {
        return switch(pEntity.getRabbitType()) {
            case BRUSH -> BRUSH;
            case COTTONTAIL -> COTTONTAIL;
            case ARCTIC -> ARCTIC;
            case STRIPED -> STRIPED;
        };
    }
}
