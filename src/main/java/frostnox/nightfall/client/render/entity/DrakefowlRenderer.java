package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.DrakefowlModel;
import frostnox.nightfall.entity.entity.animal.DrakefowlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DrakefowlRenderer extends AnimatedMobRenderer<DrakefowlEntity, DrakefowlModel> {
    public static final ResourceLocation BRONZE_MALE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/drakefowl/bronze_male.png");
    public static final ResourceLocation EMERALD_MALE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/drakefowl/emerald_male.png");

    public DrakefowlRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new DrakefowlModel(renderer.bakeLayer(ModelRegistryNF.DRAKEFOWL)), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(DrakefowlEntity pEntity) {
        return switch(pEntity.getDrakefowlType()) {
            case BRONZE -> BRONZE_MALE;
            case EMERALD -> EMERALD_MALE;
        };
    }
}
