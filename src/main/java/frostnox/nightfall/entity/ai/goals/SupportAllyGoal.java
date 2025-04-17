package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.CombatUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class SupportAllyGoal extends Goal {
    protected final ActionableEntity mob;
    protected final double speedModifier;
    protected double pathedTargetX;
    protected double pathedTargetY;
    protected double pathedTargetZ;
    protected int ticksUntilNextPathRecalculation;
    protected long lastCanUseCheck;

    public SupportAllyGoal(ActionableEntity entity, double speedIn) {
        this.mob = entity;
        this.speedModifier = speedIn;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long i = this.mob.level.getGameTime();
        if(i - this.lastCanUseCheck < 20L) {
            return false;
        } else {
            this.lastCanUseCheck = i;
            LivingEntity ally = this.mob.getAlly();
            if (ally == null || (this.mob.getTarget() == null && ally instanceof Mob && ((Mob) ally).getTarget() == null)) {
                return false;
            } else return ally.isAlive();
        }
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity ally = this.mob.getAlly();
        if (ally == null || (this.mob.getTarget() == null && ally instanceof Mob && ((Mob) ally).getTarget() == null)) {
            return false;
        } else return ally.isAlive();
    }

    @Override
    public void start() {
        this.ticksUntilNextPathRecalculation = 0;
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity ally = this.mob.getAlly();
        if(ally != null) {
            double dist = mob.getEyePosition().distanceToSqr(ally.getEyePosition());
            this.mob.getLookControl().setLookAt(ally, 9.0F, 9.0F);
            this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
            if(this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D || ally.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D || this.mob.getRandom().nextFloat() < 0.05F)) {
                this.pathedTargetX = ally.getX();
                this.pathedTargetY = ally.getY();
                this.pathedTargetZ = ally.getZ();
                this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                if (dist > 1024.0D) {
                    this.ticksUntilNextPathRecalculation += 10;
                } else if (dist > 256.0D) {
                    this.ticksUntilNextPathRecalculation += 5;
                }
                if (!this.mob.getNavigation().moveTo(ally, this.speedModifier)) {
                    this.ticksUntilNextPathRecalculation += 15;
                }
                this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
            }
            ResourceLocation actionID = mob.pickActionAlly(dist, ally);
            double reach = ActionsNF.get(actionID).getMaxDistToStart(mob);
            float lookAngle = CombatUtil.getRelativeHorizontalAngle(mob.getEyePosition(), ally.getEyePosition(), mob.getYHeadRot());
            if(dist <= reach * reach && mob.isInterruptible() && lookAngle >= -20F && lookAngle <= 20F) {
                mob.startAction(actionID);
            }
        }
    }
}