package frostnox.nightfall.action.npc.skeleton;

import frostnox.nightfall.action.npc.MoveSpeedAction;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.SkeletonEntity;
import frostnox.nightfall.entity.entity.projectile.ArrowEntity;
import frostnox.nightfall.item.IProjectileItem;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;

import java.util.EnumMap;

public class SkeletonShoot extends MoveSpeedAction {
    public SkeletonShoot(int[] duration) {
        super(-0.5F, duration);
    }

    public SkeletonShoot(Properties properties, int... duration) {
        super(properties, -0.5F, duration);
    }

    @Override
    public void onStart(LivingEntity user) {
        if(!user.level.isClientSide) user.playSound(SoundsNF.SKELETON_BOW_PULL.get(), 1F, 0.975F + user.level.random.nextFloat() * 0.05F);
    }

    @Override
    public void onTick(LivingEntity user) {
        super.onTick(user);
        IActionTracker capA = ActionTracker.get(user);
        if(user.level instanceof ServerLevel level && capA.getState() == getChargeState() && !capA.isStunned()) {
            SkeletonEntity skeleton = (SkeletonEntity) user;
            LivingEntity target = skeleton.getTarget();
            if(!user.getMainHandItem().is(TagsNF.BOW)) skeleton.queueAction();
            else if(target != null && skeleton.getSensing().hasLineOfSight(target)) {
                skeleton.queueAction();
                IProjectileItem projectile = skeleton.arrowItem;
                ArrowEntity arrow = new ArrowEntity(user.level, user, projectile);
                arrow.setBaseDamage(projectile.getProjectileDamage());
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                double dX = target.getX() - user.getX();
                double dY = target.getY(0.3D) - arrow.getY();
                double dZ = target.getZ() - user.getZ();
                double dist = Math.sqrt(dX * dX + dZ * dZ);
                arrow.shoot(dX, dY + dist * 0.1, dZ, 2.5F * projectile.getProjectileVelocityScalar(), projectile.getProjectileInaccuracy());
                level.addFreshEntity(arrow);
                skeleton.playSound(getSound().get(), 1.0F, 0.975F + user.level.random.nextFloat() * 0.05F);
            }
        }
    }

    @Override
    public int getRequiredCharge(LivingEntity user) {
        return 0;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 20;
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData leftHand = data.get(EntityPart.HAND_LEFT);
        AnimationData rightHand = data.get(EntityPart.HAND_RIGHT);
        AnimationData leftArm = data.get(EntityPart.ARM_LEFT);
        AnimationData rightArm = data.get(EntityPart.ARM_RIGHT);
        switch(state) {
            case 0 -> {
                mCalc.extend(0, -70, 0);
                rightArm.rCalc.extend(0, 0, 0);
                leftArm.rCalc.extend(0, 0, 0);
                rightHand.rCalc.extend(-90 + pitch, 70, 0);
                leftHand.rCalc.extend(-93 + pitch/4F, 75, 0);
                leftHand.tCalc.add(-1.5F, 0, -0.5F);
            }
            case 1 -> {
                mCalc.freeze();
                rightArm.rCalc.freeze();
                leftArm.rCalc.freeze();
                rightHand.rCalc.freeze();
                leftHand.rCalc.freeze();
                leftHand.toDefaultTranslation();
            }
            case 2 -> {
                mCalc.freeze();
                rightArm.rCalc.freeze();
                leftArm.rCalc.freeze();
                rightHand.rCalc.freeze();
                leftHand.rCalc.freeze();
                leftHand.tCalc.freeze();
            }
            case 3 -> {
                mCalc.extend(0, 0, 0);
                rightArm.toDefaultRotation();
                leftArm.toDefaultRotation();
                rightHand.toDefaultRotation();
                leftHand.toDefaultRotation();
                leftHand.tCalc.freeze();
            }
        }
    }

    @Override
    public float getPitch(LivingEntity user, float partialTicks) {
        return Mth.clamp(user.getViewXRot(partialTicks), -70F, 70F);
    }
}
