package frostnox.nightfall.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    private LocalPlayerMixin(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }

    /**
     * @author Frostnox
     * @reason Change suffocation check to use correct bounding box
     */
    @Overwrite
    private boolean suffocatesAt(BlockPos pos) {
        AABB box = this.getBoundingBox().deflate(1.0E-7D);
        return this.level.collidesWithSuffocatingBlock(this, box);
    }

    @ModifyConstant(method = "aiStep", constant = @Constant(floatValue = 6.0F, ordinal = 0))
    private float nightfall$bypassSprintFoodCheck(float min) {
        return Float.NEGATIVE_INFINITY;
    }
}
