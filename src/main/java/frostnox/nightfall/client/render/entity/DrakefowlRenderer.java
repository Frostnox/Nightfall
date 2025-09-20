package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.DrakefowlModel;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.entity.animal.DrakefowlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DrakefowlRenderer extends AnimatedMobRenderer<DrakefowlEntity, DrakefowlModel> {
    public static final ResourceLocation BRONZE_MALE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/drakefowl/bronze_male.png");
    public static final ResourceLocation EMERALD_MALE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/drakefowl/emerald_male.png");
    public static final ResourceLocation BRONZE_FEMALE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/drakefowl/bronze_female.png");
    public static final ResourceLocation EMERALD_FEMALE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/drakefowl/emerald_female.png");
    public static final ResourceLocation BRONZE_SPECIAL = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/drakefowl/bronze_special.png");
    public static final ResourceLocation EMERALD_SPECIAL = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/drakefowl/emerald_special.png");

    public DrakefowlRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new DrakefowlModel(renderer.bakeLayer(ModelRegistryNF.DRAKEFOWL)), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(DrakefowlEntity pEntity) {
        if(pEntity.isSpecial()) return switch(pEntity.getDrakefowlType()) {
            case BRONZE -> BRONZE_SPECIAL;
            case EMERALD -> EMERALD_SPECIAL;
        };
        else return switch(pEntity.getDrakefowlType()) {
            case BRONZE -> pEntity.sex == Sex.MALE ? BRONZE_MALE : BRONZE_FEMALE;
            case EMERALD -> pEntity.sex == Sex.MALE ? EMERALD_MALE : EMERALD_FEMALE;
        };
    }
}
