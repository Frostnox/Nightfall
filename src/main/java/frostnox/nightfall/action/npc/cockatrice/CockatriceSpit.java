package frostnox.nightfall.action.npc.cockatrice;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.entity.projectile.PoisonSpitEntity;
import frostnox.nightfall.entity.entity.projectile.SpitEntity;
import frostnox.nightfall.registry.vanilla.GameEventsNF;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

public class CockatriceSpit extends Action {
    public CockatriceSpit(int[] duration) {
        super(duration);
    }

    public CockatriceSpit(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 20;
    }

    @Override
    public void onTick(LivingEntity user) {
        super.onTick(user);
        IActionTracker capA = ActionTracker.get(user);
        if(user.level instanceof ServerLevel level && capA.getState() == 1 && capA.getFrame() == 2 && !capA.isStunned()) {
            ActionableEntity entity = (ActionableEntity) user;
            PoisonSpitEntity spit = new PoisonSpitEntity(level, user);
            BlockPos targetPos = entity.getTargetPos();
            if(targetPos != null) {
                double x, y, z;
                LivingEntity target = entity.getTarget();
                if(target != null) {
                    x = target.getX();
                    y = target.getY(0.33333333D);
                    z = target.getZ();
                }
                else {
                    x = targetPos.getX() + 0.5;
                    y = targetPos.getY();
                    z = targetPos.getZ() + 0.5;
                }
                double d0 = x - user.getX();
                double d1 = y - spit.getY();
                double d2 = z - user.getZ();
                double d3 = Math.sqrt(d0 * d0 + d2 * d2) * 0.2;
                spit.shoot(d0, d1 + d3, d2, 1.5F, 0.5F);
            }
            else spit.shootFromRotation(user, entity.getXRot(), entity.getViewYRot(1F), 0, 1.5F, 0.5F);
            level.addFreshEntity(spit);
            user.playSound(getSound().get(), 1F, 1F);
            user.gameEvent(GameEventsNF.ACTION_SOUND);
        }
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData neck = data.get(EntityPart.NECK);
        AnimationData head = data.get(EntityPart.HEAD);
        AnimationData tail = data.get(EntityPart.TAIL);
        AnimationData wingRight = data.get(EntityPart.WING_RIGHT);
        AnimationData wingLeft = data.get(EntityPart.WING_LEFT);
        switch(state) {
            case 0 -> {
                neck.rCalc.add(-15, 0, 0);
                head.rCalc.add(-15, 0, 0);
                head.tCalc.add(0, 1F, 0F);
                tail.rCalc.add(10, 0, 0);
                wingRight.rCalc.add(8, 0, 8);
                wingLeft.rCalc.add(8, 0, -8);
            }
            case 1 -> {
                neck.rCalc.add(20, 0, 0, Easing.outQuart);
                head.rCalc.add(15, 0, 0, Easing.outQuart);
                head.tCalc.add(0, -1.5F, 0F, Easing.outQuart);
                tail.rCalc.freeze();
                wingRight.rCalc.add(-8, 0, -12.5F, Easing.outQuart);
                wingLeft.rCalc.add(-8, 0, 12.5F, Easing.outQuart);
            }
            case 2 -> {
                head.toDefault();
                neck.toDefaultRotation();
                tail.toDefaultRotation();
                wingRight.toDefaultRotation();
                wingLeft.toDefaultRotation();
            }
        }
    }
}
