package frostnox.nightfall.mixin;

import frostnox.nightfall.Nightfall;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
    //Help mitigate Embeddium bug due to large texture atlas: https://github.com/FiniteReality/embeddium/issues/208
    @ModifyVariable(method = "processLoading", at = @At("STORE"), ordinal = 0, remap = false)
    private Map<ResourceLocation, List<Material>> nightfall$removeVanillaBlockTextures(Map<ResourceLocation, List<Material>> map) {
        if(Nightfall.isRubidiumLoaded) {
            for(ResourceLocation atlas : map.keySet()) {
                if(atlas.equals(TextureAtlas.LOCATION_BLOCKS)) {
                    map.get(atlas).removeIf((m) -> m.texture().getNamespace().equals("minecraft")
                            && !m.texture().toString().contains("/fire_0") && !m.texture().toString().contains("/fire_1") && !m.texture().toString().contains("/stick"));
                }
            }
        }
        return map;
    }
}
