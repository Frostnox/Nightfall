package frostnox.nightfall.action.npc;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.player.PlayerAction;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.util.CombatUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public abstract class MoveSpeedAction extends Action {
    public final float speedMultiplier;

    public MoveSpeedAction(float speedMultiplier, int... duration) {
        super(duration);
        this.speedMultiplier = speedMultiplier;
    }

    public MoveSpeedAction(Properties properties, float speedMultiplier, int... duration) {
        super(properties, duration);
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public void onEnd(LivingEntity user) {
        CombatUtil.removeTransientMultiplier(user, user.getAttribute(Attributes.MOVEMENT_SPEED), CombatUtil.ACTION_SPEED_ID);
    }

    @Override
    public void onTick(LivingEntity user) {
        IActionTracker capA = ActionTracker.get(user);
        if(!capA.isStunned() && (isChargeable() ? capA.isCharging() : !capA.isInactive())) {
            CombatUtil.addTransientMultiplier(user, user.getAttribute(Attributes.MOVEMENT_SPEED), speedMultiplier, CombatUtil.ACTION_SPEED_ID, "action_speed");
        }
        else CombatUtil.removeTransientMultiplier(user, user.getAttribute(Attributes.MOVEMENT_SPEED), CombatUtil.ACTION_SPEED_ID);
        CombatUtil.alignBodyRotWithHead(user, ActionTracker.get(user));
    }
}
