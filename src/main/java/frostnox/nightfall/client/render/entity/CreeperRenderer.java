package frostnox.nightfall.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.CreeperModel;
import frostnox.nightfall.entity.entity.monster.CreeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CreeperRenderer extends AnimatedMobRenderer<CreeperEntity, CreeperModel> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/creeper/creeper.png");

    public CreeperRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new CreeperModel(renderer.bakeLayer(ModelRegistryNF.CREEPER)), 0.5F);
    }

    @Override
    protected void scale(CreeperEntity creeper, PoseStack stack, float pPartialTickTime) {
        float swelling = creeper.getSwelling(pPartialTickTime);
        float f = 1.0F + Mth.sin(swelling * 100.0F) * swelling * 0.01F;
        swelling = Mth.clamp(swelling, 0.0F, 1.0F);
        swelling *= swelling;
        swelling *= swelling;
        float xzScale = (1.0F + swelling * 0.4F) * f;
        float yScale = (1.0F + swelling * 0.1F) / f;
        stack.scale(xzScale, yScale, xzScale);
    }

    @Override
    protected float getWhiteOverlayProgress(CreeperEntity creeper, float pPartialTicks) {
        if(!creeper.isAlive() || creeper.getActionTracker().isStunned()) return 0F;
        float swelling = creeper.getSwelling(pPartialTicks);
        return (int) (swelling * 6.0F) % 2 == 0 ? 0.0F : Mth.clamp(swelling, 0.5F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(CreeperEntity pEntity) {
        return TEXTURE;
    }
}
