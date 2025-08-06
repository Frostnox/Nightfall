package frostnox.nightfall.action.player.action;

import frostnox.nightfall.action.player.PlayerAction;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.util.CombatUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public abstract class MoveSpeedPlayerAction extends PlayerAction {
    public final float speedMultiplier;

    public MoveSpeedPlayerAction(float speedMultiplier, int... duration) {
        super(duration);
        this.speedMultiplier = speedMultiplier;
    }

    public MoveSpeedPlayerAction(Properties properties, float speedMultiplier, int... duration) {
        super(properties, duration);
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public void onEnd(LivingEntity user) {
        CombatUtil.removeTransientModifier(user, user.getAttribute(Attributes.MOVEMENT_SPEED), CombatUtil.ACTION_SPEED_ID);
    }

    @Override
    public void onTick(LivingEntity user) {
        IActionTracker capA = ActionTracker.get(user);
        if(!capA.isStunned() && (isChargeable() ? capA.isCharging() : !capA.isInactive())) {
            CombatUtil.addTransientMultiplier(user, user.getAttribute(Attributes.MOVEMENT_SPEED), speedMultiplier, CombatUtil.ACTION_SPEED_ID, "action_speed");
            if(user.level.isClientSide() && speedMultiplier < 0F && !user.isOnGround()) {
                Vec3 velocity = user.getDeltaMovement();
                float modifier = Math.max(0F, 1F + speedMultiplier / 2F);
                //Slow movement in air to better match ground speed; this would be safer to do on the server if possible
                user.setDeltaMovement(velocity.x * modifier, velocity.y, velocity.z * modifier);
            }
        }
        else CombatUtil.removeTransientModifier(user, user.getAttribute(Attributes.MOVEMENT_SPEED), CombatUtil.ACTION_SPEED_ID);
        CombatUtil.alignBodyRotWithHead(user, ActionTracker.get(user));
    }
}
