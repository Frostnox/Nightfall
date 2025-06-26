package frostnox.nightfall.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(FoodData.class)
public class FoodDataMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 20, ordinal = 0))
    private int nightfall$adjustHealThreshold(int i) {
        return 1;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 18, ordinal = 0))
    private int nightfall$adjustHealThreshold2(int i) {
        return 1;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 10, ordinal = 0))
    private int nightfall$adjustSaturationHealTimer(int i) {
        return 100;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 80, ordinal = 0))
    private int nightfall$adjustHealTimer(int i) {
        return 200;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"), index = 0)
    private float nightfall$adjustExhaustion(float exhaustion) {
        return 0.3F;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean nightfall$disableStarveDamage(Player player, DamageSource damageSource, float damage) {
        return false;
    }
}
