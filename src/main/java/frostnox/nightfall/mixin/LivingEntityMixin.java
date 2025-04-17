package frostnox.nightfall.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    private LivingEntityMixin(EntityType<?> pEntityType, Level level) {
        super(pEntityType, level);
    }

    /**
     * Stop players from emitting poof particles on death.
     */
    @Redirect(method = "tickDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
    private void nightfall$broadcastPoofParticles(Level level, Entity entity, byte id) {
        if(id != 60 || !(entity instanceof Player)) level.broadcastEntityEvent(entity, id);
    }

    @ModifyConstant(method = "jumpFromGround", constant = @Constant(floatValue = 0.2F))
    private float nightfall$adjustSprintJumpBoost(float value) {
        return 0.125F;
    }
}
