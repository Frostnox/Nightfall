package frostnox.nightfall.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.MerborModel;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.entity.animal.MerborEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MerborRenderer extends AnimatedMobRenderer<MerborEntity, MerborModel> {
    public static final ResourceLocation BOG = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/merbor/bog.png");
    public static final ResourceLocation BRINE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/merbor/brine.png");
    public static final ResourceLocation RIVER = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/merbor/river.png");

    public MerborRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new MerborModel(renderer.bakeLayer(ModelRegistryNF.MERBOR)), 0.5F);
    }

    @Override
    protected void scale(MerborEntity merbor, PoseStack stack, float pPartialTickTime) {
        float scale = 17F/16F;
        if(merbor.sex == Sex.MALE) stack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(MerborEntity pEntity) {
        return switch(pEntity.getMerborType()) {
            case BOG -> BOG;
            case BRINE -> BRINE;
            case RIVER -> RIVER;
        };
    }
}
