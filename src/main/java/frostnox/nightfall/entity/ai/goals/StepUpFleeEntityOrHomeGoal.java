package frostnox.nightfall.entity.ai.goals;

import frostnox.nightfall.block.block.nest.NestBlockEntity;
import frostnox.nightfall.entity.IHomeEntity;
import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class StepUpFleeEntityOrHomeGoal<T extends LivingEntity> extends StepUpFleeEntityGoal<T> {
    public StepUpFleeEntityOrHomeGoal(ActionableEntity mob, Class<T> fleeClass, double walkSpeedModifier, double sprintSpeedModifier) {
        super(mob, fleeClass, walkSpeedModifier, sprintSpeedModifier);
    }

    public StepUpFleeEntityOrHomeGoal(ActionableEntity mob, Class<T> fleeClass, double walkSpeedModifier, double sprintSpeedModifier, Predicate<LivingEntity> fleePredicate) {
        super(mob, fleeClass, walkSpeedModifier, sprintSpeedModifier, fleePredicate);
    }

    @Override
    protected Vec3 getRandomPos() {
        BlockPos homePos = ((IHomeEntity) mob).getHomePos();
        if(homePos != null) return Vec3.atBottomCenterOf(homePos);
        else return super.getRandomPos();
    }

    @Override
    public void stop() {
        BlockPos homePos = ((IHomeEntity) mob).getHomePos();
        if(homePos != null && mob.distanceToSqr(Vec3.atBottomCenterOf(homePos)) <= 1D && mob.level.getBlockEntity(homePos) instanceof NestBlockEntity nest) {
            nest.addEntity(mob);
        }
        super.stop();
    }
}
