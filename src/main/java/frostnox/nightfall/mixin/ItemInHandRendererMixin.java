package frostnox.nightfall.mixin;

import frostnox.nightfall.capability.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @Redirect(method = "applyEatTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
    private int adjustFoodUseDuration(ItemStack stack) {
        int duration = stack.getUseDuration();
        if(minecraft.player != null && PlayerData.isPresent(minecraft.player)) {
            float temp = PlayerData.get(minecraft.player).getTemperature();
            if(temp > 1F) return Math.round(duration * (1F + Math.min(1F, (temp - 1) * 4F)));
        }
        return duration;
    }
}
