package frostnox.nightfall.entity.ai.pathfinding;

import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.util.CombatUtil;
import net.minecraft.world.level.Level;

public class FlankingLandEntityNavigator extends LandEntityNavigator {
    public FlankingLandEntityNavigator(ActionableEntity entity, Level level) {
        super(entity, level);
    }

    public FlankingLandEntityNavigator(NodeManager nodeManager, Level level) {
        super(nodeManager, level);
    }

    @Override
    protected float heuristic(Node from, Node to) {
        if(cachedTarget != null) {
            //Calculate angle between node and target
            float angle = CombatUtil.getRelativeHorizontalAngle(to.pathX - cachedTarget.getX(), to.pathZ - cachedTarget.getZ(), cachedTarget.getYHeadRot());
            //If out of sight, use standard distance heuristic
            if(angle < -90F || angle > 90F) return from.distOctile(to);
            //If in sight, increase proportional to angle
            else return from.distOctile(to) + (90F - Math.abs(angle)) / 90F * 1.5F;
        }
        else return from.distOctile(to);
    }
}
