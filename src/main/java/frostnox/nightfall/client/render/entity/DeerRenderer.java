package frostnox.nightfall.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.DeerModel;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.entity.animal.DeerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DeerRenderer extends AnimatedMobRenderer<DeerEntity, DeerModel> {
    public static final ResourceLocation BRIAR = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/deer/briar.png");
    public static final ResourceLocation RED = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/deer/red.png");
    public static final ResourceLocation SPOTTED = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/deer/spotted.png");

    public DeerRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new DeerModel(renderer.bakeLayer(ModelRegistryNF.DEER)), 0.4F);
    }

    @Override
    protected void scale(DeerEntity deer, PoseStack stack, float pPartialTickTime) {
        float scale = 17F/16F;
        if(deer.getSex() == Sex.MALE) stack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(DeerEntity pEntity) {
        return switch(pEntity.getDeerType()) {
            case BRIAR -> BRIAR;
            case RED -> RED;
            case SPOTTED -> SPOTTED;
        };
    }
}
