package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.DrakefowlBabyModel;
import frostnox.nightfall.entity.entity.animal.DrakefowlBabyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DrakefowlBabyRenderer extends AnimatedMobRenderer<DrakefowlBabyEntity, DrakefowlBabyModel> {
    public static final ResourceLocation BRONZE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/drakefowl/bronze_baby.png");
    public static final ResourceLocation EMERALD = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/drakefowl/emerald_baby.png");

    public DrakefowlBabyRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new DrakefowlBabyModel(renderer.bakeLayer(ModelRegistryNF.DRAKEFOWL_BABY)), 0.15F);
    }

    @Override
    public ResourceLocation getTextureLocation(DrakefowlBabyEntity pEntity) {
        return switch(pEntity.getDrakefowlType()) {
            case BRONZE -> BRONZE;
            case EMERALD -> EMERALD;
        };
    }
}
