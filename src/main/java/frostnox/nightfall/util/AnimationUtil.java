package frostnox.nightfall.util;

import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class AnimationUtil {
    public static void createDodgeParticles(Player p) {
        //Create block particles at movement spot
        int x = Mth.floor(p.getX());
        int y = Mth.floor(p.getY() - (double)0.2F);
        int z = Mth.floor(p.getZ());
        BlockPos blockpos = new BlockPos(x, y, z);
        BlockState blockstate = p.level.getBlockState(blockpos);
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            IPlayerData capP = PlayerData.get(p);
            float xAmount = CombatUtil.DodgeDirection.get(capP.getDodgeDirection()).getZAmount(); //Flip these since they are aligned for matrix use
            float zAmount = CombatUtil.DodgeDirection.get(capP.getDodgeDirection()).getXAmount();
            float yRot = MathUtil.toRadians(p.getYHeadRot());
            float xSpeed = xAmount * Mth.cos(yRot) - zAmount * Mth.sin(yRot);
            float zSpeed = xAmount * Mth.sin(yRot) + zAmount * Mth.cos(yRot);
            float count;
            //Create more particles for slower players
            if(p.getSpeed() >= 0 && p.getSpeed() < 0.02) count = 5 / (0.02F * 10);
            else count = 8 / (p.getSpeed() * 10);
            for(int i = 0; i < (int) count; i++) {
                p.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(blockpos),
                        p.getX() + ((double) p.level.random.nextFloat() - 0.5D) * (double) p.getBbWidth(),
                        p.getY() + 0.1D,
                        p.getZ() + ((double) p.level.random.nextFloat() - 0.5D) * (double) p.getBbWidth(),
                        xSpeed * -8.0D,
                        1.5D,
                        zSpeed * -8.0D);
            }
        }
    }

    public static void stunPart(ModelPart part, int frame, int duration, float partialTicks, float degrees, int dir) {
        AnimationCalculator calc = new AnimationCalculator(duration / 2, frame, partialTicks, Easing.outQuart);
        int offset = duration / 2;
        part.xRot += MathUtil.toRadians(-degrees * dir) * calc.getProgress();
        if(frame > offset) {
            calc.setEasing(Easing.inOutSine);
            calc.offset = offset;
            calc.length = duration;
            part.xRot += MathUtil.toRadians(degrees * dir) * calc.getProgress();
        }
    }

    public static void stunPartToDefault(ModelPart part, AnimationData data, int frame, int duration, float partialTicks) {
        AnimationData newData = new AnimationData(data);
        //Update with stun data
        newData.update(frame, duration, partialTicks);
        newData.tCalc.setStaticVector(data.tCalc.getTransformations());
        newData.rCalc.setStaticVector(data.rCalc.getTransformations());
        newData.rCalc.setEasing(Easing.outQuart);
        newData.sCalc.setEasing(Easing.outQuart);
        newData.tCalc.extend(data.dTranslation);
        newData.rCalc.extend(data.dRotation);
        newData.sCalc.extend(data.dScale);
        newData.writeToModelPart(part);
    }

    public static void stunPartToDefaultWithPause(ModelPart part, AnimationData data, int frame, int duration, float partialTicks, float degrees, int dir) {
        AnimationData newData = new AnimationData(data);
        //Update with stun data
        newData.update(frame, duration, partialTicks);
        int offset = duration / 2;
        newData.tCalc.setStaticVector(data.tCalc.getTransformations());
        newData.tCalc.length = offset;
        newData.tCalc.setEasing(Easing.outQuart);
        newData.rCalc.setStaticVector(data.rCalc.getTransformations());
        newData.rCalc.add(-degrees * dir, 0, 0);
        newData.rCalc.length = offset;
        newData.rCalc.setEasing(Easing.outQuart);
        newData.sCalc.setStaticVector(data.sCalc.getTransformations());
        newData.sCalc.length = offset;
        newData.sCalc.setEasing(Easing.outQuart);
        if(frame > offset) {
            newData.resetLengths(duration, Easing.inOutSine);
            newData.tCalc.offset = offset;
            newData.tCalc.extend(data.dTranslation);
            newData.rCalc.offset = offset;
            newData.rCalc.extend(data.dRotation);
            newData.sCalc.offset = offset;
            newData.sCalc.extend(data.dScale);
        }
        newData.writeToModelPart(part);
    }

    public static int getOverlayCoords(LivingEntity pLivingEntity, float pU) {
        return OverlayTexture.pack(OverlayTexture.u(pU), OverlayTexture.v(pLivingEntity.hurtTime > 0));
    }

    public static int getActiveSideModifier(Player player) {
        return PlayerData.get(player).getActiveHand() == InteractionHand.OFF_HAND ? -1 : 1;
    }

    public static float getBlockRecoilProgress(Player player, float partialTicks) {
        IPlayerData capP = PlayerData.get(player);
        float block;
        float ticks = player.tickCount - capP.getLastBlockTick();
        if(ticks <= 3) block = AnimationUtil.applyEasing(AnimationUtil.interpolate(Math.max(0, ticks - 1), ticks, partialTicks) / 3F, Easing.none);
        else if(ticks <= 5) block = 1;
        else if(ticks <= 8) block = AnimationUtil.applyEasing(1 - AnimationUtil.interpolate(Math.max(ticks - 6, 0), ticks - 5, partialTicks) / 3F, Easing.none);
        else block = 0;
        return block;
    }

    public static float getClimbProgress(Player player, float partial) {
        IPlayerData capP = PlayerData.get(player);
        return Math.min(1, Mth.lerp(partial, capP.isClimbing() ? Math.max(0, capP.getClimbTicks() - 1) : Math.min(3, capP.getClimbTicks() + 1), capP.getClimbTicks()) / 3F);
    }

    public static boolean isPlayerFacingClimbable(Player player) {
        if(player.onClimbable() && player.level.getBlockState(player.getOnPos().above()).isLadder(player.level, player.getOnPos().above(), player)) {
            float angle = CombatUtil.getRelativeHorizontalAngle(player.getPosition(1), player.getFeetBlockState().getShape(player.level, player.blockPosition()).bounds().move(player.blockPosition()).getCenter(), player.getViewYRot(1));
            return angle >= -75 && angle <= 75;
        }
        return false;
    }

    public static float getAirborneProgress(Player player, float partial) {
        IPlayerData capP = PlayerData.get(player);
        return Math.min(1, Mth.lerp(partial, !player.isOnGround() && !player.isPassenger() && !player.onClimbable() && !capP.isClimbing() && !player.isFallFlying() ? Math.max(0, capP.getAirTicks() - 1) : Math.min(8, capP.getAirTicks() + 1), capP.getAirTicks()) / 8F);
    }

    public static float getCrouchProgress(Player player, float partial) {
        IPlayerData capP = PlayerData.get(player);
        return Mth.clamp(Mth.lerp(partial, player.isCrouching() ? Math.max(0, capP.getCrouchTicks() - 1) : Math.min(3, capP.getCrouchTicks() + 1), capP.getCrouchTicks()) / 3F, 0F, 1F);
    }

    public static float getHoldProgress(Player player, float partial) {
        IPlayerData capP = PlayerData.get(player);
        return Math.min(1, Mth.lerp(partial, !capP.getHeldContents().isEmpty() ? Math.max(0, capP.getHoldTicks() - 1) : Math.min(3, capP.getHoldTicks() + 1), capP.getHoldTicks()) / 3F);
    }

    public static float interpolate(float prevProgress, float currentProgress, float partialTicks) {
        if(prevProgress < 0) prevProgress = 0;
        return (prevProgress) + ((currentProgress - prevProgress) * partialTicks);
    }

    public static double interpolate(double prevProgress, double currentProgress, float partialTicks) {
        if(prevProgress < 0) prevProgress = 0;
        return (prevProgress) + ((currentProgress - prevProgress) * partialTicks);
    }

    /**
     * @param rawAmount cumulative amount (ex. entity limbSwing)
     * @param slow divisor used to control speed, higher values will reduce the speed, > 0
     * @return linear animation progress that goes from 0 to 1 to 0 and so on
     */
    public static float getLinearProgress(float rawAmount, float slow) {
        if(slow <= 0.0F) return 0;
        int iteration = Mth.floor(rawAmount / slow);
        float amount = (rawAmount - slow * iteration) / slow;
        if(iteration % 2 == 0) return amount;
        else return 1F - amount;
    }

    /**
     * @param rawAmount cumulative amount (ex. entity limbSwing)
     * @param slow divisor used to control speed, higher values will reduce the speed, > 0
     * @return linear animation progress that goes from -1 to 1 to -1 and so on
     */
    public static float getSymmetricProgress(float rawAmount, float slow) {
        if(slow <= 0.0F) return 0;
        int iteration = Mth.floor(rawAmount / slow);
        float amount = (rawAmount - slow * iteration) * 2F / slow;
        if(iteration % 2 == 0) return amount - 1F;
        else return 1F - amount;
    }

    /**
     * @param rawAmount cumulative amount (ex. entity limbSwing)
     * @param slow divisor used to control speed, higher values will reduce the speed, > 0
     * @param easing easing to use, only in-out is implemented since in/out creates an asynchronous effect
     * @return animation progress that goes from -1 to 1 to -1 and so on
     */
    public static float getProgress(float rawAmount, float slow, Easing easing, boolean symmetric) {
        float p = symmetric ? getSymmetricProgress(rawAmount, slow) : getLinearProgress(rawAmount, slow);
        if(!symmetric) return easing.apply(p);
        else return switch(easing) {
            case inSine, outSine, inOutSine -> p < 0.0F ? -Easing.outSine.apply(Math.abs(p)) : Easing.outSine.apply(p);
            case inQuart, outQuart, inOutQuart -> p < 0.0F ? -Easing.outQuart.apply(Math.abs(p)) : Easing.outQuart.apply(p);
            case inQuad, outQuad, inOutQuad -> p < 0.0F ? -Easing.outQuad.apply(Math.abs(p)) : Easing.outQuad.apply(p);
            case inCubic, outCubic, inOutCubic -> p < 0.0F ? -Easing.outCubic.apply(Math.abs(p)) : Easing.outCubic.apply(p);
            case inBack, outBack, inOutBack -> p < 0.0F ? -Easing.outBack.apply(Math.abs(p)) : Easing.outBack.apply(p);
            default -> p;
        };
    }

    public static float applyEasing(float p, Easing easing) {
        return easing.apply(p);
    }

    public static HumanoidArm getSideFromHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
    }

    public static float getSideModifier(InteractionHand hand) {
        return getSideFromHand(hand) == HumanoidArm.RIGHT ? 1F : -1F;
    }

    public static AnimationData[] copyAnimationDataArray(AnimationData[] data) {
        AnimationData[] newData = new AnimationData[data.length];
        for(int i = 0; i < data.length; i++) {
            newData[i] = data[i].copy();
        }
        return newData;
    }
}
