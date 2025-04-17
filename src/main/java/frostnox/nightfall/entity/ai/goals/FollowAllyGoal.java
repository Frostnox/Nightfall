package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FollowAllyGoal extends Goal {
    protected final ActionableEntity mob;
    protected final double speedModifier;
    protected double pathedTargetX;
    protected double pathedTargetY;
    protected double pathedTargetZ;
    protected int recalcTicks;
    protected long lastCanUseCheck;
    protected double maxDistanceSqr;
    protected double strafeStartDistSqr;
    protected boolean strafingClockwise;
    protected boolean strafingBackwards;
    protected int strafingTime = -1;

    public FollowAllyGoal(ActionableEntity entity, double speedIn, double maxDistance, double strafeStartDist) {
        this.mob = entity;
        this.speedModifier = speedIn;
        this.maxDistanceSqr = maxDistance * maxDistance;
        this.strafeStartDistSqr = strafeStartDist * strafeStartDist;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long time = mob.level.getGameTime();
        if(time - lastCanUseCheck < 20L) return false;
        else {
            lastCanUseCheck = time;
            ActionableEntity ally = mob.getAlly();
            if(ally == null) return false;
            if(ally.getTargetPos() != null) return true;
            return mob.distanceToSqr(ally.getX(), ally.getY(), ally.getZ()) > strafeStartDistSqr;
        }
    }

    @Override
    public boolean canContinueToUse() {
        ActionableEntity ally = mob.getAlly();
        if(ally == null) return false;
        if(ally.getTargetPos() != null) return true;
        return mob.distanceToSqr(ally.getX(), ally.getY(), ally.getZ()) > strafeStartDistSqr;
    }

    @Override
    public void start() {
        recalcTicks = 0;
    }

    @Override
    public void stop() {
        mob.getNavigator().stop();
        mob.getMoveControl().strafe(0, 0);
    }

    @Override
    public void tick() {
        ActionableEntity ally = mob.getAlly();
        if(ally != null) {
            double distSqr = mob.distanceToSqr(ally.getX(), ally.getY(), ally.getZ());
            if(ally.getTarget() != null && distSqr <= strafeStartDistSqr) {
                mob.getNavigation().stop();
                strafingTime++;
            }
            else {
                recalcTicks = Math.max(recalcTicks - 1, 0);
                if((recalcTicks == 0 || mob.refreshPath || mob.getNavigator().isDone()) && (pathedTargetX == 0.0D && pathedTargetY == 0.0D && pathedTargetZ == 0.0D || ally.distanceToSqr(pathedTargetX, pathedTargetY, pathedTargetZ) >= 1.0D || mob.getRandom().nextFloat() < 0.05F)) {
                    pathedTargetX = ally.getX();
                    pathedTargetY = ally.getY();
                    pathedTargetZ = ally.getZ();

                    recalcTicks = 2;
                    if(distSqr > 1024.0) recalcTicks += 12;
                    else if(distSqr > 256.0) recalcTicks += 8;
                    else if(distSqr > 25.0) recalcTicks += 4;
                    if(!mob.getNavigator().moveTo(ally, speedModifier)) recalcTicks += 4;
                    if(mob.refreshPath) mob.refreshPath = false;
                }
                mob.getMoveControl().strafe(0, 0);
                strafingTime = -1;
            }
            if(strafingTime >= 20) {
                if(mob.getRandom().nextFloat() < 0.3F) strafingClockwise = !strafingClockwise;
                if(mob.getRandom().nextFloat() < 0.3F) strafingBackwards = !strafingBackwards;
                strafingTime = 0;
            }
            if(strafingTime > -1) {
                if(distSqr > (strafeStartDistSqr * 0.75F)) strafingBackwards = false;
                else if(distSqr < (strafeStartDistSqr * 0.25F)) strafingBackwards = true;
                if(!mob.isInWater()) mob.getMoveControl().strafe(strafingBackwards ? -0.5F : 0.5F, strafingClockwise ? 0.5F : -0.5F);
                mob.lookAt(ally, mob.getMaxYRotPerTick(), mob.getMaxXRotPerTick());
            }
            mob.getLookControl().setLookAt(ally, mob.getMaxYRotPerTick(), mob.getMaxXRotPerTick());
        }
    }
}
