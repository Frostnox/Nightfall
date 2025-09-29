package frostnox.nightfall.mixin;

import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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

    /**
     * @author Frostnox
     * @reason Support push interactions between players/vanilla entities and Nightfall entities
     */
    @Overwrite
    public void push(Entity pusher) {
        if(!isPassengerOfSameVehicle(pusher) && !pusher.noPhysics && !noPhysics) {
            double dX = pusher.getX() - getX();
            double dZ = pusher.getZ() - getZ();
            double maxD = Mth.absMax(dX, dZ);
            if(maxD >= 0.01) {
                maxD = Math.sqrt(maxD);
                dX /= maxD;
                dZ /= maxD;
                double scale = 1.0D / maxD;
                if(scale > 1.0D) scale = 1.0D;
                dX *= scale * 0.05;
                dZ *= scale * 0.05;

                if(!isVehicle() && (pusher instanceof ActionableEntity actionable ? actionable.getPushForce() : ActionableEntity.PUSH_MEDIUM) >= LevelUtil.PLAYER_PUSH) {
                    push(-dX, 0.0D, -dZ);
                }
                if(!pusher.isVehicle() && (pusher instanceof ActionableEntity actionable ? actionable.getPushResistance() : ActionableEntity.PUSH_MEDIUM) <= LevelUtil.PLAYER_PUSH) {
                    pusher.push(dX, 0.0D, dZ);
                }
            }
        }
    }
}
