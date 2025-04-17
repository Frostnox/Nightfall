package frostnox.nightfall.mixin;

import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Custom day/night cycles with seasonal differences.
 */
@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin {
    @Shadow @Final private ResourceLocation effectsLocation;

    @Inject(method = "timeOfDay", at = @At(value = "HEAD"), cancellable = true)
    public void nightfall$timeOfDay(long dayTime, CallbackInfoReturnable<Float> returnable) {
        if(effectsLocation.equals(ContinentalWorldType.LOCATION)) {
            double d0 = Mth.frac(dayTime / (double) ContinentalWorldType.DAY_LENGTH - 0.25D);
            double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
            returnable.setReturnValue((float)(d0 * 2.0D + d1) / 3.0F);
        }
    }

    @Inject(method = "moonPhase", at = @At(value = "HEAD"), cancellable = true)
    public void nightfall$moonPhase(long dayTime, CallbackInfoReturnable<Integer> returnable) {
        if(effectsLocation.equals(ContinentalWorldType.LOCATION)) {
            //Offset by 5 so starting moon is waxing crescent
            returnable.setReturnValue((int)(dayTime / ContinentalWorldType.DAY_LENGTH % 8L + 5L) % 8);
        }
    }
}
