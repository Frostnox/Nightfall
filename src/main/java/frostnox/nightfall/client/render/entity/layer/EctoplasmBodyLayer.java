package frostnox.nightfall.client.render.entity.layer;

import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.entity.entity.monster.EctoplasmEntity;
import net.minecraft.client.renderer.entity.RenderLayerParent;

public class EctoplasmBodyLayer extends TranslucentBodyLayer<EctoplasmEntity, AnimatedModel<EctoplasmEntity>> {
    public EctoplasmBodyLayer(RenderLayerParent<EctoplasmEntity, AnimatedModel<EctoplasmEntity>> renderer, AnimatedModel<EctoplasmEntity> model) {
        super(renderer, model);
    }

    @Override
    protected float getAlpha(EctoplasmEntity entity) {
        return Math.min(super.getAlpha(entity), entity.getTransparency());
    }
}
