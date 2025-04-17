package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.CockatriceModel;
import frostnox.nightfall.entity.entity.monster.CockatriceEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CockatriceRenderer extends AnimatedMobRenderer<CockatriceEntity, CockatriceModel> {
    public static final ResourceLocation BRONZE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/cockatrice/bronze.png");
    public static final ResourceLocation EMERALD = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/cockatrice/emerald.png");

    public CockatriceRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new CockatriceModel(renderer.bakeLayer(ModelRegistryNF.COCKATRICE)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CockatriceEntity pEntity) {
        return switch(pEntity.getCockatriceType()) {
            case BRONZE -> BRONZE;
            case EMERALD -> EMERALD;
        };
    }
}
