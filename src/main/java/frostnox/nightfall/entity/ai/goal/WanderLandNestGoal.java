package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.IHomeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class WanderLandNestGoal extends WanderGoal {
    protected final IHomeEntity homeEntity;

    public WanderLandNestGoal(IHomeEntity mob, double pSpeedModifier) {
        super(mob.getEntity(), pSpeedModifier);
        homeEntity = mob;
    }

    public WanderLandNestGoal(IHomeEntity mob, double pSpeedModifier, int timeoutTicks) {
        super(mob.getEntity(), pSpeedModifier, timeoutTicks);
        homeEntity = mob;
    }

    @Nullable
    protected Vec3 getPosition() {
        BlockPos homePos = homeEntity.getHomePos();
        if(mob.isInWaterOrBubble()) {
            Vec3 randPos = homePos == null ? LandRandomPos.getPos(mob, 15, 7) : LandRandomPos.getPosTowards(mob, 15, 7, Vec3.atCenterOf(homePos));
            return randPos == null ? super.getPosition() : randPos;
        }
        else {
            return mob.getRandom().nextFloat() >= 0.001F ?
                    (homePos == null ? LandRandomPos.getPos(mob, 10, 7) : LandRandomPos.getPosTowards(mob, 10, 7, Vec3.atCenterOf(homePos)))
                    : super.getPosition();
        }
    }
}
