package frostnox.nightfall.mixin;

import frostnox.nightfall.capability.PlayerData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Shadow public abstract void eat(int pFoodLevelModifier, float pSaturationLevelModifier);

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

    /**
     * @author Frostnox
     * @reason Allow player body temperature to adjust food saturation
     */
    @Overwrite(remap = false)
    public void eat(Item pItem, ItemStack pStack, @javax.annotation.Nullable net.minecraft.world.entity.LivingEntity entity) {
        if(pItem.isEdible()) {
            FoodProperties foodproperties = pStack.getFoodProperties(entity);
            float saturation = foodproperties.getSaturationModifier();
            if(entity instanceof Player player) {
                float temp = PlayerData.get(player).getTemperature();
                if(temp > 1F) saturation *= 1F - Math.min(1F, (temp - 1) * 4);
            }
            eat(foodproperties.getNutrition(), saturation);
        }
    }
}
