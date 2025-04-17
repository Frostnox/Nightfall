package frostnox.nightfall.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frostnox.nightfall.data.TagsNF;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderDispatcher.class, remap = false)
public abstract class BlockRenderDispatcherMixin {
    /**
     * Allow tagged blocks to skip rendering breaking texture
     */
    @Inject(method = "renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraftforge/client/model/data/IModelData;)V", at = @At(value = "HEAD"), cancellable = true)
    public void nightfall$checkCancelForRenderBreakingTexture(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack pPoseStack, VertexConsumer pConsumer, IModelData modelData, CallbackInfo callbackInfo) {
        if(state.is(TagsNF.NO_BREAKING_TEXTURE)) callbackInfo.cancel();
    }
}
