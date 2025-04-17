package frostnox.nightfall.action.player.action;

import frostnox.nightfall.action.player.PlayerAction;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.AnimationUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;

public class HoldEntity extends PlayerAction {
    public HoldEntity(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        int side = AnimationUtil.getActiveSideModifier((Player) user);
        AnimationData rightHand = data.get(EntityPart.getSidedHand(side));
        AnimationData rightArm = data.get(EntityPart.getSidedArm(side));
        AnimationData leftArm = data.get(EntityPart.getSidedArm(-side));
        AnimationData leftHand = data.get(EntityPart.getSidedHand(-side));
        rightArm.rCalc.setStaticVector(0, 0, 0);
        leftArm.rCalc.setStaticVector(0, 0, 0);
        rightHand.rCalc.setStaticVector(-45, 0, 0);
        leftHand.rCalc.setStaticVector(-45, 0, 0);
    }
}
