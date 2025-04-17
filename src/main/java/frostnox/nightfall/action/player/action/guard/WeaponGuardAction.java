package frostnox.nightfall.action.player.action.guard;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.item.item.MeleeWeaponItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.ActionToServer;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;

public class WeaponGuardAction extends CounterGuardAction {
    public WeaponGuardAction(int... duration) {
        super(duration);
    }

    public WeaponGuardAction(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public boolean blocksDamageSource(DamageTypeSource source) {
        return !source.isDoT() && source.hasEntity() && !source.isProjectile();
    }

    @Override
    public float getGuardAngle() {
        return 65F;
    }

    @Override
    public void transformOppositeHandFP(AnimationCalculator tCalc, int xSide, int side, IActionTracker capA) {

    }

    @Override
    public void transformModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        super.transformModelFP(state, frame, duration, charge, user, data);
        AnimationCalculator tCalc = data.tCalc;
        AnimationCalculator rCalc = data.rCalc;
        Vector3f dTranslation = data.dTranslation;
        Vector3f dRotation = data.dRotation;
        if(state == 0) {
            Player player = (Player) user;
            float block = AnimationUtil.getBlockRecoilProgress(player, tCalc.partialTicks);
            rCalc.addWithCharge(10 + 20 * block, 55, -50, charge, Easing.outQuart);
            tCalc.addWithCharge(-3.5F/16F, 0.5F/16F - block/16F, 1F/16F + 3F * block/16F, charge, Easing.outQuart);
        }
        else {
            rCalc.extend(dRotation);
            tCalc.extend(dTranslation);
        }
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        if(data.size() == 6) {
            int side = AnimationUtil.getActiveSideModifier((Player) user);
            AnimationData rightHand = data.get(EntityPart.getSidedHand(side));
            AnimationData rightArm = data.get(EntityPart.getSidedArm(side));
            AnimationData leftArm = data.get(EntityPart.getSidedArm(-side));
            if(state == 0) {
                float block = AnimationUtil.getBlockRecoilProgress((Player) user, mCalc.partialTicks);
                Vector3f vec = rightArm.dRotation.copy();
                vec.mul(0.3F);
                vec.add(pitch/3, 0, 0);
                rightArm.rCalc.extend(vec);
                rightHand.rCalc.addWithCharge(-70 - 5 * block, (-45 + -15 * block), (35 - 5 * block), charge);
                Vector3f vec0 = leftArm.dRotation.copy();
                vec0.mul(0.65F);
                vec0.add(8 * block, 0, -8);
                leftArm.rCalc.extend(vec0);
            }
            else {
                rightArm.toDefaultRotation();
                rightHand.toDefaultRotation();
                leftArm.toDefaultRotation();
            }
        }
    }
}
