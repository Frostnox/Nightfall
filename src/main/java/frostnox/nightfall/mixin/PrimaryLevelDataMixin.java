package frostnox.nightfall.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataMixin implements ServerLevelData, WorldData {
    /**
     * Stop worlds from being marked as experimental
     */
    @ModifyVariable(method = "<init>(Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/CompoundTag;ZIIIFJJIIIZIZZZLnet/minecraft/world/level/border/WorldBorder$Settings;IILjava/util/UUID;Ljava/util/Set;Lnet/minecraft/world/level/timers/TimerQueue;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/levelgen/WorldGenSettings;Lcom/mojang/serialization/Lifecycle;)V", at = @At("HEAD"), ordinal = 0)
    private static Lifecycle nightfall$forceStableLifecycle(Lifecycle lifecycle) {
        return Lifecycle.stable();
    }

    @Inject(method = "getDifficulty", at = @At("TAIL"), cancellable = true)
    private void nightfall$getDifficulty(CallbackInfoReturnable<Difficulty> callbackInfo) {
        Difficulty difficulty = callbackInfo.getReturnValue();
        if(difficulty == Difficulty.EASY || difficulty == Difficulty.HARD) callbackInfo.setReturnValue(Difficulty.NORMAL);
    }
}
