package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.WolfModel;
import frostnox.nightfall.entity.entity.animal.WolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class WolfRenderer extends AnimatedMobRenderer<WolfEntity, WolfModel> {
    public static final ResourceLocation DIRE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/wolf/dire.png");
    public static final ResourceLocation STRIPED = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/wolf/striped.png");
    public static final ResourceLocation TIMBER = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/wolf/timber.png");
    public static final ResourceLocation SPECIAL = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/wolf/special.png");

    public WolfRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new WolfModel(renderer.bakeLayer(ModelRegistryNF.WOLF)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(WolfEntity pEntity) {
        if(pEntity.isSpecial()) return SPECIAL;
        else return switch(pEntity.getWolfType()) {
            case DIRE -> DIRE;
            case STRIPED -> STRIPED;
            case TIMBER -> TIMBER;
        };
    }
}