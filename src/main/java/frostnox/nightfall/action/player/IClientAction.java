package frostnox.nightfall.action.player;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Client-only methods for player actions. Called automatically, do not call on the server.
 */
public interface IClientAction {
    Action getAction();

    default void transformModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        if(!ActionsNF.isEmpty(getAction().chainsFrom().getId())) {
            Action last = getAction().chainsFrom().get();
            //End at chain state of last action
            if(last instanceof IClientAction) ((IClientAction) last).transformModelFP(last.getChainState(), Action.DEFAULT_DURATION, Action.DEFAULT_DURATION, charge, user, data);
        }
        if(state != 0) {
            //Previous states in this action
            this.transformModelFP(state - 1, Action.DEFAULT_DURATION, Action.DEFAULT_DURATION, charge, user, data);
        }
        //Reset duration/offset
        data.rCalc.resetLength(duration, Easing.inOutSine);
        data.tCalc.resetLength(duration, Easing.inOutSine);
        data.sCalc.resetLength(duration, Easing.inOutSine);
        //Animation on a single state here
    }

    default void transformOppositeModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        if(!ActionsNF.isEmpty(getAction().chainsFrom().getId())) {
            Action last = getAction().chainsFrom().get();
            //End at chain state of last action
            if(last instanceof IClientAction) ((IClientAction) last).transformOppositeModelFP(last.getChainState(), Action.DEFAULT_DURATION, Action.DEFAULT_DURATION, charge, user, data);
        }
        if(state != 0) {
            //Previous states in this action
            this.transformOppositeModelFP(state - 1, Action.DEFAULT_DURATION, Action.DEFAULT_DURATION, charge, user, data);
        }
        //Reset duration/offset
        data.rCalc.resetLength(duration, Easing.inOutSine);
        data.tCalc.resetLength(duration, Easing.inOutSine);
        data.sCalc.resetLength(duration, Easing.inOutSine);
        //Animation on a single state here
    }

    default void transformOppositeLayer(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        if(state == 0 && !ActionsNF.isEmpty(getAction().chainsFrom().getId())) {
            Action last = getAction().chainsFrom().get();
            //End at chain state of last action
            if(last instanceof IClientAction) ((IClientAction) last).transformOppositeLayer(last.getChainState(), Action.DEFAULT_DURATION, Action.DEFAULT_DURATION, charge, user, data);
        }
        if(state != 0) {
            //Previous states in this action
            this.transformOppositeLayer(state - 1, Action.DEFAULT_DURATION, Action.DEFAULT_DURATION, charge, user, data);
        }
        //Reset duration/offset
        data.resetLengths(duration, Easing.inOutSine);
        transformOppositeLayerSingle(state, frame, duration, charge, user, data);
    }

    /**
     * Client transformations for opposite of active layer
     * This should only be used for layers separated from the model, ex. held items
     * @param data new AnimationData synced with user's ActionTracker
     */
    default void transformOppositeLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {

    }

    default void transformOppositeHandFP(AnimationCalculator tCalc, int xSide, int side, IActionTracker capA) {
        if(getAction().getTotalStates() > 1) {
            if(capA.getState() == 0 && ActionsNF.isEmpty(getAction().chainsFrom().getId()) || capA.getState() == getAction().getTotalStates() - 1) {
                //Move away
                tCalc.add(0.95F * xSide * side, -0.95F * side, 0);
                //Move back
                if(capA.getState() == getAction().getTotalStates() - 1) tCalc.add(-0.95F * xSide * side, 0.95F * side, 0);
            }
            //Hidden
            else tCalc.setStaticVector(0.95F * xSide * side, -0.95F * side, 0);
        }
    }

    /**
     * Called every tick on the client while the action is being used.
     */
    default void onClientTick(Player player) {

    }
}
