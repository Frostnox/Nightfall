package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.MerborBabyModel;
import frostnox.nightfall.entity.entity.animal.MerborBabyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MerborBabyRenderer extends AnimatedMobRenderer<MerborBabyEntity, MerborBabyModel> {
    public static final ResourceLocation BOG = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/merbor/bog_baby.png");
    public static final ResourceLocation BRINE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/merbor/brine_baby.png");
    public static final ResourceLocation RIVER = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/merbor/river_baby.png");
    public static final ResourceLocation SPECIAL = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/merbor/special_baby.png");

    public MerborBabyRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new MerborBabyModel(renderer.bakeLayer(ModelRegistryNF.MERBOR_BABY)), 0.175F);
    }

    @Override
    public ResourceLocation getTextureLocation(MerborBabyEntity pEntity) {
        if(pEntity.isSpecial()) return SPECIAL;
        else return switch(pEntity.getMerborType()) {
            case BOG -> BOG;
            case BRINE -> BRINE;
            case RIVER -> RIVER;
        };
    }
}
