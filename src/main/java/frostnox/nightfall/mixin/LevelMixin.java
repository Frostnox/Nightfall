package frostnox.nightfall.mixin;

import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.ILevelData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.client.EntityLightEngine;
import frostnox.nightfall.world.Weather;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeLevel;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Custom weather logic for continental levels
 */
@Mixin(Level.class)
public abstract class LevelMixin extends CapabilityProvider<Level> implements LevelAccessor, AutoCloseable, IForgeLevel {
    @Shadow protected abstract LevelChunk getChunkAt(BlockPos pos);

    private LevelMixin(Class<Level> baseClass) {
        super(baseClass);
    }

    /**
     * Handle client operations here since there isn't a better place to do this
     */
    @Inject(method = "onBlockStateChange", at = @At("HEAD"))
    private void nightfall$onClientBlockStateChange(BlockPos pos, BlockState blockState, BlockState newState, CallbackInfo callbackInfo) {
        //No need to check for client since server overrides this function
        EntityLightEngine.get().handleBlockUpdate(pos);
    }

    @Inject(method = "isRainingAt", at = @At("HEAD"), cancellable = true)
    private void nightfall$isRainingAt(BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfo) {
        if(LevelData.isPresent((Level) (Object) this)) {
            ILevelData capL = LevelData.get((Level) (Object) this);
            if(!canSeeSky(pos) || getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() > pos.getY()) {
                callbackInfo.setReturnValue(false);
            }
            else {
                LevelChunk chunk = getChunkAt(pos);
                if(!chunk.isEmpty()) callbackInfo.setReturnValue(capL.getWeather(ChunkData.get(getChunkAt(pos)), pos) == Weather.RAIN);
            }
        }
    }

    @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
    private void nightfall$getRainLevel(float partialTick, CallbackInfoReturnable<Float> callbackInfo) {
        LazyOptional<ILevelData> capL = LevelData.getOptional((Level) (Object) this);
        if(capL.isPresent()) {
            callbackInfo.setReturnValue(capL.orElseThrow(() -> new IllegalArgumentException("Null in LazyOptional.")).getGlobalRainLevel());
        }
    }

    @Inject(method = "getThunderLevel", at = @At("HEAD"), cancellable = true)
    private void nightfall$getThunderLevel(float partialTick, CallbackInfoReturnable<Float> callbackInfo) {
        LazyOptional<ILevelData> capL = LevelData.getOptional((Level) (Object) this);
        if(capL.isPresent()) {
            callbackInfo.setReturnValue(capL.orElseThrow(() -> new IllegalArgumentException("Null in LazyOptional.")).getGlobalThunderLevel());
        }
    }
}
