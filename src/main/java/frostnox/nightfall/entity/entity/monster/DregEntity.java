package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.ai.goal.FleeDamageGoal;
import frostnox.nightfall.entity.ai.goal.FollowAllyGoal;
import frostnox.nightfall.entity.ai.goal.StareAtTargetGoal;
import frostnox.nightfall.entity.ai.goal.target.TrackNearestTargetGoal;
import frostnox.nightfall.entity.ai.pathfinding.LandEntityNavigator;
import frostnox.nightfall.entity.ai.pathfinding.Node;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.entity.SetAllyToClient;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.world.MoonPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class DregEntity extends UndeadEntity {
    public @Nullable UndeadEntity ally; //This is null if no ally is targeted
    public boolean buildDone = true;
    public boolean cowerOnly;
    protected int buildCooldown = 0;
    protected @Nullable ReversePath buildPath;
    protected boolean canBuild = false;
    private final List<Goal> goals = List.of(
            new FollowAllyGoal(this, 1D, 12D, 8D),
            new StareAtTargetGoal(this, true),
            new FleeDamageGoal(this, 0.8D),
            new RandomLookAroundGoal(this));
    private final List<Goal> targetGoals = List.of(new HurtByTargetGoal(this), new TrackNearestTargetGoal<>(this, Player.class, MoonPhase.get(level) != MoonPhase.FULL));

    public DregEntity(EntityType<? extends DregEntity> type, Level worldIn) {
        super(type, worldIn);
        setCowerOnly(false);
    }

    public @Nullable ReversePath getBuildPath() {
        return buildPath;
    }

    public void setCowerOnly(boolean value) {
        cowerOnly = value;
        if(!level.isClientSide) {
            for(Goal goal : goals) goalSelector.removeGoal(goal);
            for(Goal goal : targetGoals) targetSelector.removeGoal(goal);
            if(!cowerOnly) {
                for(int i = 0; i < goals.size(); i++) goalSelector.addGoal(i + 3, goals.get(i));
                for(int i = 0; i < targetGoals.size(); i++) targetSelector.addGoal(i + 1, targetGoals.get(i));
            }
        }
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 70D)
                .add(AttributesNF.WILLPOWER.get(), 20)
                .add(Attributes.MOVEMENT_SPEED, 0.26F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 30)
                .add(AttributesNF.HEARING_RANGE.get(), 15);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new DregActionGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(level.isClientSide && random.nextInt(6) == 0) {
            level.addParticle(ParticleTypesNF.ESSENCE_MOON.get(), getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0, 0, 0);
        }
        if(!isAlive()) return;
        if(ally != null && ally.isRemoved()) ally = null;
        if(!level.isClientSide) {
            if(buildCooldown > 0) buildCooldown--;
            //If necessary, look for new ally and update client
            if(randTickCount % 30 == 0 && (ally == null || ally instanceof DregEntity || distanceToSqr(ally) > 30 * 30)) {
                List<Entity> entities = level.getEntities(this, getBoundingBox().inflate(16D, 8D, 16D));
                UndeadEntity candidate = null;
                int cPrio = 0;
                double cDistSqr = Double.MAX_VALUE;
                for(Entity entity : entities) {
                    int uPrio = 0;
                    if(entity instanceof UndeadEntity undead) {
                        if(undead != ally && undead.hasDregAlly) continue;
                        double uDistSqr = distanceToSqr(undead);
                        if(!undead.isAlive()) uPrio += 100;
                        if(!(undead instanceof DregEntity)) uPrio += 10;
                        if(uDistSqr < cDistSqr) uPrio += 1;
                        if(uPrio > cPrio) {
                            candidate = undead;
                            cPrio = uPrio;
                            cDistSqr = uDistSqr;
                        }
                    }
                }
                if(ally != null) ally.hasDregAlly = false;
                setAlly(candidate);
                if(ally != null) NetworkHandler.toAllTracking(this, new SetAllyToClient(ally.getId(), getId()));
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason pReason) {
        super.remove(pReason);
        if(ally != null) ally.hasDregAlly = false;
    }

    @Override
    public void die(DamageSource pCause) {
        super.die(pCause);
        if(dead && ally != null) ally.hasDregAlly = false;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        setCowerOnly(random.nextInt(1024) == 0);
        equipHumanoid(worldIn.getRandom(), 0.55F, 0.6F, 0.65F, 0.2F,
                MoonPhase.get(worldIn) == MoonPhase.FULL ? 0.25F : 0F);
        return spawnDataIn;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if(cowerOnly) tag.putBoolean("cowerOnly", cowerOnly);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setCowerOnly(tag.getBoolean("cowerOnly"));
    }

    @Override
    public UndeadEntity getAlly() {
        return ally;
    }

    @Override
    public void setAlly(ActionableEntity ally) {
        if(ally instanceof UndeadEntity undead) {
            this.ally = undead;
            undead.hasDregAlly = true;
        }
    }

    @Override
    public boolean canBuild() {
        return getEssence() >= 50F && canBuild;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new LandEntityNavigator(this, level) {
            @Override
            protected boolean isGoalInvalid(Node goal) {
                return !goal.type.walkable || goal.type == NodeType.BUILDABLE_WALKABLE;
            }
        };
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        double y = hitPos.y;
        if(y > 5D/16D && y <= 12D/16D) return EquipmentSlot.LEGS;
        else if(y > 12D/16D && y <= 22D/16D) return EquipmentSlot.CHEST;
        else if(y > 22D/16D) return EquipmentSlot.HEAD;
        return EquipmentSlot.FEET;
    }

    @Override
    public ParticleOptions getHurtParticle() {
        return ParticleTypesNF.BLOOD_DARK_RED.get();
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        if(isDeadOrDying()) return super.getStandingEyeHeight(poseIn, sizeIn);
        else return 1.65F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsNF.DREG_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.DREG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.DREG_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {
        playSound(SoundsNF.DREG_STEP.get(), 0.15F, 1.0F);
    }

    private static class DregActionGoal extends Goal {
        private final DregEntity dreg;

        DregActionGoal(DregEntity dreg) {
            this.dreg = dreg;
        }

        @Override
        public boolean canUse() {
            return dreg.getAlly() != null || dreg.getTarget() != null || dreg.cowerOnly;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            dreg.queueAction();
            dreg.buildDone = true;
            dreg.buildPath = null;
        }

        @Override
        public void tick() {
            if(!ActionTracker.isPresent(dreg)) return;
            IActionTracker capA = dreg.getActionTracker();
            if(!capA.isInactive() && !capA.isCharging()) return;
            if(dreg.cowerOnly) {
                dreg.startAction(ActionsNF.DREG_COWER.getId());
                return;
            }
            UndeadEntity ally = dreg.getAlly();
            ResourceLocation action = ActionsNF.EMPTY.getId();
            //Pick action
            if(ally != null) {
                if(!ally.isAlive() && !ally.resurrecting) {
                    action = ActionsNF.DREG_RESURRECT.getId();
                }
                else if(ally.getTargetPos() != null) {
                    ReversePath path = ally.getNavigator().getActivePath();
                    if(path != null) {
                        dreg.canBuild = true;
                        if(!dreg.canBuild() || dreg.buildCooldown > 0 || path.reachesGoal() || path.getSize() > 4) action = ActionsNF.DREG_BUFF.getId();
                        else {
                            if(dreg.buildDone) {
                                Entity target = ally.getTarget();
                                if(target != null) dreg.buildPath = dreg.getNavigator().findPath(target, 1);
                                else dreg.buildPath = dreg.getNavigator().findPath(ally.lastTargetPos, 1);
                                dreg.buildDone = false;
                                dreg.buildCooldown = 20;
                            }
                            action = ActionsNF.DREG_BUILD.getId();
                        }
                        dreg.canBuild = false;
                    }
                }
                else dreg.buildDone = true;
            }
            else if(dreg.getTarget() != null) action = ActionsNF.DREG_COWER.getId();
            //Update action
            if(!action.equals(capA.getActionID())) {
                if(!capA.isInactive()) dreg.queueAction();
                else dreg.startAction(action);
            }
        }
    }
}
