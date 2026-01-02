package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class StrafeAttackGoal extends RushAttackGoal {
    protected final int strafeSwitchTime;
    protected final double strafeStartDistSqr;
    protected boolean strafingClockwise, inStrafeDist;
    protected int strafingTime = -1;

    public StrafeAttackGoal(ActionableEntity entity, double speedIn, int strafeSwitchTime, double strafeStartDist) {
        super(entity, speedIn);
        this.strafeSwitchTime = strafeSwitchTime;
        strafeStartDistSqr = (strafeStartDist * strafeStartDist) * (0.9F + new XoroshiroRandomSource(entity.getSynchedRandom()).nextFloat() * 0.2F);
    }

    protected boolean canStrafe() {
        return inStrafeDist && !canPursue();
    }

    @Override
    public void stop() {
        super.stop();
        mob.getMoveControl().strafe(0, 0);
        inStrafeDist = false;
    }

    @Override
    public void tick() {
        if(mob.getTarget() != null) {
            double distSqr = mob.distanceToSqr(mob.getTarget());
            if(!inStrafeDist) inStrafeDist = distSqr <= strafeStartDistSqr;
            else if(distSqr > strafeStartDistSqr * 1.3) inStrafeDist = false;
        }
        if(canStrafe() && mob.getTarget() != null) {
            strafingTime++;
            if(strafingTime >= strafeSwitchTime) {
                if(mob.getRandom().nextFloat() < 0.3F) strafingClockwise = !strafingClockwise;
                strafingTime = 0;
            }
            mob.getMoveControl().strafe(0, strafingClockwise ? 0.5F : -0.5F);
            mob.lookAt(mob.getTarget(), mob.getMaxYRotPerTick(), mob.getMaxXRotPerTick());
        }
        else {
            mob.getMoveControl().strafe(0, 0);
            strafingTime = -1;
        }
        super.tick();
    }
}