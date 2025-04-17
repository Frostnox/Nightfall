package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.entity.entity.monster.UndeadEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public abstract class UndeadMobRenderer<T extends UndeadEntity, M extends AnimatedModel<T>> extends AnimatedMobRenderer<T, M> {
    public UndeadMobRenderer(EntityRendererProvider.Context renderer, M entity, float shadowRadius) {
        super(renderer, entity, shadowRadius);
    }

    @Override
    protected RenderType getRenderType(T pLivingEntity, boolean pBodyVisible, boolean pTranslucent, boolean pGlowing) {
        if(pTranslucent) return RenderType.entityTranslucent(getTextureLocation(pLivingEntity));
        else return super.getRenderType(pLivingEntity, pBodyVisible, pTranslucent, pGlowing);
    }

    @Override
    protected float getAlpha(T entity) {
        float alpha = super.getAlpha(entity);
        if(alpha < 1F) return alpha;
        else return entity.getTransparency();
    }
}
