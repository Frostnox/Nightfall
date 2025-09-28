package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.entity.animal.TamableAnimalEntity;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class LureGoal extends Goal {
    private final TamableAnimalEntity entity;
    private final double range, speedModifier;
    private final TargetingConditions targeting;
    protected long lastCanUseCheck;
    protected int cooldown;
    protected @Nullable Player player;

    public LureGoal(TamableAnimalEntity entity, double range, double speedModifier) {
        this.entity = entity;
        this.targeting = TargetingConditions.forNonCombat().range(range)
                .selector((player) -> entity.isFeedItem(player.getMainHandItem()) || entity.isFeedItem(player.getOffhandItem()));
        this.range = range;
        this.speedModifier = speedModifier;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if(cooldown > 0) {
            cooldown--;
            return false;
        }
        else {
            long time = entity.level.getGameTime();
            if(time - lastCanUseCheck < 20L) return false;
            lastCanUseCheck = time;
            player = entity.level.getNearestPlayer(targeting, entity);
            return player != null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if(entity.distanceToSqr(player) > range * range) return canUse();
        else if(!entity.isFeedItem(player.getMainHandItem()) && !entity.isFeedItem(player.getOffhandItem())) return canUse();
        else return true;
    }

    @Override
    public void start() {
        entity.tamable = true;
    }

    @Override
    public void stop() {
        entity.tamable = false;
        player = null;
        entity.getNavigator().stop();
        cooldown = reducedTickDelay(100);
    }

    @Override
    public void tick() {
        entity.getLookControl().setLookAt(player, entity.getMaxYRotPerTick(), entity.getMaxXRotPerTick());
        if(MathUtil.getShortestDistanceSqrBoxToBox(entity.getBoundingBox(), player.getBoundingBox()) < 2 * 2) entity.getNavigator().stop();
        else entity.getNavigator().moveTo(player, speedModifier, 2);
    }
}
