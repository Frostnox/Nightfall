package frostnox.nightfall.action.player.action;

import com.mojang.math.Vector3f;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.Impact;
import frostnox.nightfall.block.IIgnitable;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.item.item.ActionableAmmoItem;
import frostnox.nightfall.registry.KnowledgeNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;

import java.util.EnumMap;

public class FirestarterShoot extends MoveSpeedPlayerAction {
    public FirestarterShoot(float speedMultiplier, int... duration) {
        super(speedMultiplier, duration);
    }

    public FirestarterShoot(Properties properties, float speedMultiplier, int... duration) {
        super(properties, speedMultiplier, duration);
    }

    @Override
    public boolean canStart(LivingEntity user) {
        return super.canStart(user) && (user.getPose() == Pose.STANDING || user.getPose() == Pose.CROUCHING);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 2;
    }

    @Override
    public void onTick(LivingEntity user) {
        super.onTick(user);
        IActionTracker capA = ActionTracker.get(user);
        if(user.level instanceof ServerLevel level && isStateDamaging(capA.getState()) && capA.getFrame() == 1 && !capA.isStunned() && user instanceof Player player) {
            IPlayerData capP = PlayerData.get(player);
            InteractionHand hand = capP.getActiveHand();
            ItemStack item = user.getItemInHand(hand);
            if(item.getItem() instanceof ActionableAmmoItem firestarter && !player.isUnderWater() && (player.getAbilities().instabuild)) {
                Vec3 start = user.getEyePosition();
                Vec3 look = user.getLookAngle();
                Vec3 end = start.add(look.scale(2.5));
                HitResult hitResult = LevelUtil.getHitResult(level, user, start, end, (entity) -> !entity.isSpectator(), ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY);
                Vec3 hitLoc = hitResult.getLocation();
                Vec3 dist = hitLoc.subtract(start);
                Vec3 center = dist.lengthSqr() > 1.2 * 1.2 ? start.add(look.scale(1.2)) : hitLoc;
                if(hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() != HitResult.Type.MISS) {
                    BlockPos pos = blockHitResult.getBlockPos();
                    BlockState state = level.getBlockState(pos);
                    if(state.getBlock() instanceof IIgnitable ignitable && !ignitable.isIgnited(state)) {
                        if(ignitable.tryToIgnite(level, pos, state, item, TieredHeat.RED)) level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
                    }
                    else {
                        BlockPos adjPos = pos.relative(blockHitResult.getDirection());
                        state = level.getBlockState(adjPos);
                        if(state.isAir()) {
                            BlockState fire = BlocksNF.FIRE.get().getPlacementState(level, adjPos);
                            if(fire.canSurvive(level, adjPos)) {
                                level.setBlock(adjPos, fire, 11);
                                level.gameEvent(player, GameEvent.BLOCK_PLACE, adjPos);
                                capP.addKnowledge(KnowledgeNF.STARTED_FIRE.getId());
                            }
                        }
                    }
                }
                for(Entity entity : level.getEntities(player, new AABB(center.x - 0.6, center.y - 0.6, center.z - 0.6, center.x + 0.6, center.y + 0.6, center.z + 0.6))) {
                    entity.hurt(DamageTypeSource.createEntitySource(user, DamageType.FIRE).setImpact(Impact.MEDIUM).setStun(CombatUtil.STUN_SHORT), 10);
                    entity.setSecondsOnFire(12);
                }

                level.playSound(null, user, getExtraSound().get(), SoundSource.PLAYERS, 1.0F, 0.95F + level.random.nextFloat() * 0.1F);
                level.sendParticles(ParticleTypesNF.FIRE_EXPLOSION.get(), center.x, center.y, center.z, 1, 0, 0, 0, 0);
                if(!player.getAbilities().instabuild) {
                    item.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                }
            }
        }
    }

    @Override
    public void transformModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        super.transformModelFP(state, frame, duration, charge, user, data);
        AnimationCalculator tCalc = data.tCalc;
        AnimationCalculator rCalc = data.rCalc;
        Vector3f dTranslation = data.dTranslation;
        Vector3f dRotation = data.dRotation;
        switch(state) {
            case 0 -> {
                tCalc.extend(-7F/16F, 5F/16F, -1F/16F);
                rCalc.extend(5, 9, 0);
            }
            case 1, 3 -> {
                tCalc.freeze();
                rCalc.freeze();
            }
            case 2 -> {
                tCalc.add(1.5F/16F, -1.5F/16F, 7F/16F, Easing.outCubic);
                rCalc.add(10, 0, 0, Easing.outQuart);
            }
            case 4 -> {
                tCalc.extend(dTranslation);
                rCalc.extend(dRotation);
            }
        }
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        if(data.size() == 6) {
            int side = AnimationUtil.getActiveSideModifier((Player) user);
            AnimationData rightHand = data.get(EntityPart.getSidedHand(side));
            AnimationData rightArm = data.get(EntityPart.getSidedArm(side));
            AnimationData leftArm = data.get(EntityPart.getSidedArm(-side));
            AnimationData leftHand = data.get(EntityPart.getSidedHand(-side));
            switch(state) {
                case 0 -> {
                    mCalc.extend(0, 0, 0);
                    rightArm.rCalc.extend(0, 0, 0);
                    rightHand.rCalc.extend(-90 + pitch, -10, 0);
                    leftArm.rCalc.extend(0, 0, 0);
                    leftHand.rCalc.extend(-115 + pitch, 45 + (pitch < 0 ? pitch / 2 : 0), pitch/3);
                }
                case 1, 3 -> {
                    mCalc.freeze();
                    rightArm.rCalc.freeze();
                    rightHand.rCalc.freeze();
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                }
                case 2 -> {
                    mCalc.extend(0, 35, 0, Easing.outCubic);
                    rightArm.rCalc.freeze();
                    rightHand.rCalc.extend(-45 + pitch, -25, 20 + pitch/3, Easing.outCubic);
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.extend(pitch/2, 0, -15);
                }
                case 4 -> {
                    mCalc.extend(0, 0, 0, Easing.inOutSine);
                    rightArm.rCalc.extend(rightArm.dRotation);
                    leftArm.rCalc.extend(leftArm.dRotation);
                    leftHand.rCalc.extend(leftHand.dRotation);
                    rightHand.rCalc.extend(rightHand.dRotation);
                }
            }
        }
    }
}
