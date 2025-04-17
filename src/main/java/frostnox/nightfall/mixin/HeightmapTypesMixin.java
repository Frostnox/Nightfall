package frostnox.nightfall.mixin;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Heightmap.Types.class)
public class HeightmapTypesMixin {
    /**
     * Use leaves tag to determine leaves instead of checking whether the block uses the vanilla leaves class
     */
    @Inject(method = "lambda$static$1", at = @At("HEAD"), cancellable = true)
    private static void nightfall$motionBlockingNoLeavesLambda(BlockState state, CallbackInfoReturnable<Boolean> callbackInfo) {
        if(state.is(BlockTags.LEAVES)) callbackInfo.setReturnValue(false);
    }
}
