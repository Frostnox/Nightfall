package frostnox.nightfall.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FoodData.class)
public class FoodDataMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 20, ordinal = 0))
    private int nightfall$adjustHealThreshold(int i) {
        return 7;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 18, ordinal = 0))
    private int nightfall$adjustHealThreshold2(int i) {
        return 7;
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

    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 10F, ordinal = 0))
    private float nightfall$adjustMinStarveHealth(float f) {
        return 0F;
    }
}
