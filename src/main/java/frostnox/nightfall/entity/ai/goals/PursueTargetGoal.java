package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class PursueTargetGoal extends Goal {
    protected final ActionableEntity mob;
    protected final double speedModifier;
    protected ReversePath path;
    protected double pathedTargetX;
    protected double pathedTargetY;
    protected double pathedTargetZ;
    protected int recalcTicks;
    protected long lastCanUseCheck;
    protected int pursueTime;
    protected BlockPos lastLastPos = null;

    public PursueTargetGoal(ActionableEntity entity, double speedIn) {
        this.mob = entity;
        this.speedModifier = speedIn;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    protected boolean canPursue() {
        return true;
    }

    @Override
    public boolean canUse() {
        long time = mob.level.getGameTime();
        if(time - lastCanUseCheck < 20L) return false;
        else {
            lastCanUseCheck = time;
            LivingEntity target = mob.getTarget();
            if(target == null || !target.isAlive()) {
                if(mob.lastTargetPos != null) {
                    path = mob.getNavigator().findPath(mob.lastTargetPos, 0);
                    return true;
                }
                else if(mob.getAudioSensing().getClosestHeardPos() != null) {
                    mob.lastTargetPos = mob.getAudioSensing().getClosestHeardPos();
                    path = mob.getNavigator().findPath(mob.lastTargetPos, 0);
                    return true;
                }
                else return false;
            }
            else {
                path = mob.getNavigator().findPath(target, 0);
                return path != null;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        if(mob.lastTargetPos != null && target == null) return !mob.getNavigator().isDone();
        else if(target == null) return false;
        else if(!target.isAlive()) return false;
        //else if(Math.pow(target.getX() - mob.getX(), 2) + Math.pow(target.getZ() - mob.getZ(), 2) < 2.3) return true; //This is unnecessary if below is
        //else if(!followingTargetEvenIfNotSeen) return !mob.getNavigator().isDone(); //This is probably unnecessary
        else if(!mob.isWithinRestriction(target.blockPosition())) return false;
        else return !(target instanceof Player) || !target.isSpectator() && !((Player) target).isCreative();
    }

    @Override
    public void start() {
        mob.getNavigator().moveTo(path, speedModifier);
        mob.setAggressive(true);
        recalcTicks = 0;
    }

    @Override
    public void stop() {
        mob.setTarget(null);
        mob.lastTargetPos = null;
        mob.setAggressive(false);
        mob.getNavigator().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if(target != null) {
            mob.getLookControl().setLookAt(target, mob.getMaxYRotPerTick(), mob.getMaxXRotPerTick());
            double distSqr = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            recalcTicks = Math.max(recalcTicks - 1, 0);
            if(canPursue()) {
                if((recalcTicks == 0 || mob.refreshPath || mob.getNavigator().isDone()) && (pathedTargetX == 0.0D && pathedTargetY == 0.0D && pathedTargetZ == 0.0D || target.distanceToSqr(pathedTargetX, pathedTargetY, pathedTargetZ) >= 1.0D || mob.getRandom().nextFloat() < 0.05F)) {
                    pathedTargetX = target.getX();
                    pathedTargetY = target.getY();
                    pathedTargetZ = target.getZ();

                    recalcTicks = 2;
                    if(distSqr > 1024.0) recalcTicks += 12;
                    else if(distSqr > 256.0) recalcTicks += 8;
                    else if(distSqr > 25.0) recalcTicks += 4;
                    if(!mob.getNavigator().moveTo(target, speedModifier)) recalcTicks += 4;
                    if(mob.refreshPath) mob.refreshPath = false;
                }
            }
        }
        else if(mob.lastTargetPos != null) {
            if(mob.getOnPos().above().equals(mob.lastTargetPos)) {
                mob.lastTargetPos = null;
            }
            else if(lastLastPos != null && lastLastPos.equals(mob.lastTargetPos)) {
                pursueTime++;
                if(pursueTime > 600) {
                    mob.lastTargetPos = null;
                    pursueTime = 0;
                }
            }
            else if(canPursue()) {
                mob.getNavigator().moveTo(mob.lastTargetPos.getX(), mob.lastTargetPos.getY(), mob.lastTargetPos.getZ(), speedModifier);
                pursueTime = 0;
            }
        }
        if(!canPursue()) mob.getNavigator().stop();
        lastLastPos = mob.lastTargetPos;
    }
}
