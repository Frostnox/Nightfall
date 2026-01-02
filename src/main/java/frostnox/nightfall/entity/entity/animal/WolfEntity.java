package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.ai.goal.*;
import frostnox.nightfall.entity.ai.goal.target.TrackNearestTargetGoal;
import frostnox.nightfall.entity.ai.pathfinding.FlankingLandEntityNavigator;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.entity.Diet;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.ContinentalWorldType;
import frostnox.nightfall.world.MoonPhase;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Set;
import java.util.UUID;

public class WolfEntity extends AnimalEntity implements IOrientedHitBoxes {
    public enum Type {
        DIRE, STRIPED, TIMBER
    }
    public enum PackGoal {
        IDLE, HUNT, FLEE
    }
    public static final int GROWL_DURATION = 8 * 20;
    private static final EntityPart[] OBB_PARTS = new EntityPart[]{EntityPart.BODY, EntityPart.NECK, EntityPart.HEAD};
    protected static final EntityDataAccessor<WolfEntity.Type> TYPE = SynchedEntityData.defineId(WolfEntity.class, DataSerializersNF.WOLF_TYPE);
    protected static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(WolfEntity.class, EntityDataSerializers.BOOLEAN);
    public int growlTicks;
    public PackGoal packGoal = PackGoal.IDLE;
    protected final Set<UUID> packMembers = new ObjectArraySet<>(6);
    public @Nullable LivingEntity attackTarget;
    protected int attackTargetDecay = 0;
    public int fleeTicks;

    public WolfEntity(EntityType<? extends WolfEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 90D)
                .add(Attributes.MOVEMENT_SPEED, 0.275F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 4)
                .add(Attributes.FOLLOW_RANGE, 30)
                .add(AttributesNF.HEARING_RANGE.get(), 24);
    }

    public static EnumMap<EntityPart, AnimationData> getHeadAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -2.5F/16F, -7.5F/16F), new Vector3f(0, 0, 0)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, -1F/16F, -1.5F/16F), new Vector3f(0, 0, 0)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(0, 0, 0)));
        return map;
    }

    public WolfEntity.Type getWolfType() {
        return getEntityData().get(TYPE);
    }

    public boolean isSpecial() {
        return getEntityData().get(SPECIAL);
    }

    public boolean canAttackInPack() {
        if(attackTarget == getTarget()) return true;
        else if(packGoal == PackGoal.FLEE || fleeTicks > 0 || getTarget() == null) return false;
        for(UUID id : packMembers) {
            if(((ServerLevel) level).getEntity(id) instanceof WolfEntity wolf && wolf.isAlive() && distanceToSqr(wolf) < 32 * 32 && wolf.packGoal == PackGoal.HUNT && !wolf.shouldFleeFrom(getTarget())) {
                if(wolf.attackTarget == getTarget()) return false;
            }
        }
        return true;
    }

    @Override
    protected void registerGoals() {
        StrafeAttackGoal attackGoal = new StepUpStrafeAttackGoal(this, 1.7D, 60, 7.25) {
            @Override
            protected boolean shouldRecalcAttack() {
                return attackTarget == getTarget() && attackID.equals(ActionsNF.WOLF_GROWL.getId());
            }

            @Override
            protected boolean canPursue() {
                if(ActionTracker.isPresent(mob) && mob.getActionTracker().getActionID().equals(ActionsNF.WOLF_GROWL.getId())) {
                    if(growlTicks <= GROWL_DURATION) return false;
                    else return mob.getTarget() != null && !inStrafeDist;
                }
                return true;
            }

            @Override
            public boolean canContinueToUse() {
                if(fleeTicks > 0) return false;
                else return super.canContinueToUse();
            }

            @Override
            public void stop() {
                super.stop();
                if(mob.isAlive()) mob.getActionTracker().releaseCharge();
                ((WolfEntity) mob).attackTarget = null;
            }
        };
        goalSelector.addGoal(1, new FloatAtHeightGoal(this, 0.8D));
        goalSelector.addGoal(2, new StepUpLandFleeTargetGoal(this, 1.6D, 1.7D) {
            @Override
            protected Vec3 getRandomPos() {
                return DefaultRandomPos.getPosAway(mob, fleeTicks > 0 ? 8 : 32, 8, avoidPos);
            }

            @Override
            public void start() {
                super.start();
                attackTarget = null;
            }

            @Override
            public void stop() {
                LivingEntity target = mob.getTarget();
                BlockPos pos = mob.lastTargetPos;
                super.stop();
                if(target != null && mob.canAttack(target) && !mob.shouldFleeFrom(target)) {
                    mob.lastTargetPos = pos;
                    mob.setTarget(target);
                }
                attackGoal.lastCanUseCheck = 0;
            }
        });
        goalSelector.addGoal(3, new StepUpLandFleeEntityGoal<>(this, LivingEntity.class, 1.6D, 1.7D, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else return entity.getType().is(TagsNF.WOLF_PREDATOR);
        }));
        goalSelector.addGoal(4, attackGoal);
        goalSelector.addGoal(5, new StepUpFleeDamageGoal(this, 1.7D));
        goalSelector.addGoal(6, new EatEntityGoal(this, 1D, 15, 2));
        goalSelector.addGoal(7, new EatBlockGoal(this, 1D, 15, 2));
        goalSelector.addGoal(8, new ReducedWanderLandGoal(this, 0.75D, 3));
        goalSelector.addGoal(9, new RandomLookGoal(this, 0.02F / 2));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new TrackNearestTargetGoal<>(this, LivingEntity.class, true, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else if(entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
            else return entity.getType().is(TagsNF.WOLF_SOLO_PREY) || entity.getType().is(TagsNF.WOLF_PACK_PREY);
        }) {
            @Override
            protected double getFollowDistance() {
                return super.getFollowDistance() * Math.max(0.5, (1D - getSatietyPercent()));
            }
        });
    }

    @Override
    public boolean canTargetFromSound(LivingEntity target) {
        return target.getType().is(TagsNF.WOLF_SOLO_PREY) || target.getType().is(TagsNF.WOLF_PACK_PREY) || target instanceof Player;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target.canBeSeenAsEnemy() && !(target instanceof WolfEntity);
    }

    @Override
    public boolean shouldFleeFrom(LivingEntity target) {
        return (fleeTicks > 0 && !target.getType().is(TagsNF.WOLF_SOLO_PREY)) || (!target.getType().is(TagsNF.WOLF_SOLO_PREY) && getHealth() / getMaxHealth() < 0.4F);
    }

    @Override
    public ResourceLocation pickActionEnemy(double distanceSqr, LivingEntity target) {
        if(distanceSqr < 2 * 2) return ActionsNF.WOLF_BITE.getId();
        else if(growlTicks <= 0 || !canAttackInPack()) return ActionsNF.WOLF_GROWL.getId();
        else {
            attackTarget = target;
            return ActionsNF.WOLF_BITE.getId();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if(!isRemoved() && getActionTracker().isInactive() && getTarget() == null && growlTicks > 0) growlTicks--;
        if(fleeTicks > 0) fleeTicks--;
        if(!level.isClientSide) {
            if(isAlive()) {
                if(randTickCount % 30 == 0 && !packMembers.isEmpty()) {
                    LivingEntity target = getTarget();
                    if(target == null) packGoal = PackGoal.IDLE;
                    else if(!target.getType().is(TagsNF.WOLF_SOLO_PREY)) {
                        PackGoal newGoal;
                        int fleeVote = shouldFleeFrom(target) ? 1 : 0;
                        Set<WolfEntity> wolves = new ObjectArraySet<>(packMembers.size());
                        wolves.add(this);
                        for(UUID id : packMembers) {
                            if(((ServerLevel) level).getEntity(id) instanceof WolfEntity wolf && wolf.isAlive() && distanceToSqr(wolf) < 32 * 32) {
                                wolves.add(wolf);
                                if(wolf.shouldFleeFrom(target)) fleeVote++;
                            }
                        }
                        if(wolves.size() < 2) newGoal = PackGoal.IDLE;
                        else if(fleeVote > wolves.size() / 2) newGoal = PackGoal.FLEE;
                        else if(packGoal != PackGoal.HUNT) {
                            if(target instanceof ActionableEntity prey) {
                                int threats = prey.getType().is(TagsNF.WOLF_THREAT) ? 1 : 0;
                                for(ActionableEntity nearby : level.getEntitiesOfClass(ActionableEntity.class, prey.getBoundingBox().inflate(12))) {
                                    if(nearby.getType().is(TagsNF.WOLF_THREAT) && (prey.getType() == nearby.getType() || prey.canReceiveAlert(nearby))) {
                                        threats++;
                                        if(threats > wolves.size() / 2) break;
                                    }
                                }
                                if(threats <= wolves.size() / 2) {
                                    newGoal = PackGoal.HUNT;
                                    for(WolfEntity wolf : wolves) wolf.growlTicks = GROWL_DURATION * 5;
                                }
                                else newGoal = PackGoal.IDLE;
                            }
                            else { //Players
                                if(wolves.size() < level.getEntitiesOfClass(target.getClass(), target.getBoundingBox().inflate(12)).size()) newGoal = PackGoal.FLEE;
                                else newGoal = PackGoal.HUNT;
                            }
                        }
                        else newGoal = PackGoal.HUNT;
                        for(WolfEntity wolf : wolves) {
                            wolf.packGoal = newGoal;
                            if(newGoal == PackGoal.HUNT) wolf.setTarget(target);
                        }
                    }
                }
                if(randTickCount % (20 * 10) == 0) {
                    float howlChance = 0.007F;
                    if(LevelUtil.isNight(level)) howlChance *= 2;
                    MoonPhase phase = MoonPhase.get(level);
                    if(phase == MoonPhase.FULL) howlChance *= 2;
                    else if(phase == MoonPhase.NEW) howlChance /= 2;
                    if(random.nextFloat() < howlChance) {
                        for(ServerPlayer player : ((ServerLevel) level).players()) {
                            if(player.level.dimension() == level.dimension()) {
                                double distSqr = distanceToSqr(player);
                                if(distSqr < 64 * 64) player.connection.send(new ClientboundSoundEntityPacket(SoundsNF.WOLF_HOWL_NEAR.get(), SoundSource.HOSTILE, this, 4F, 1F));
                                else if(distSqr < 128 * 128) player.connection.send(new ClientboundSoundEntityPacket(SoundsNF.WOLF_HOWL_FAR.get(), SoundSource.HOSTILE, this, 8F, 1F));
                            }
                        }
                    }
                }
                if(attackTarget != null) {
                    attackTargetDecay++;
                    if(attackTargetDecay > 20 * 5) {
                        attackTarget = null;
                        attackTargetDecay = 0;
                        fleeTicks = 10;
                    }
                }
                else attackTargetDecay = 0;
            }
            else attackTarget = null;
        }
    }

    @Override
    protected void simulateTime(int timePassed) {
        growlTicks = Math.max(0, growlTicks - timePassed);
        super.simulateTime(timePassed);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(super.hurt(pSource, pAmount)) {
            if(growlTicks < GROWL_DURATION) growlTicks = GROWL_DURATION;
            return true;
        }
        else return false;
    }

    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return super.calculateFallDamage(pFallDistance, pDamageMultiplier) - 10;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlankingLandEntityNavigator(this, level);
    }

    @Override
    public float getVisionAngle() {
        return 100F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, Type.DIRE);
        entityData.define(SPECIAL, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        int type = getWolfType().ordinal();
        if(type != 0) tag.putInt("type", type);
        boolean special = isSpecial();
        if(special) tag.putBoolean("special", special);
        tag.putInt("growlTicks", growlTicks);
        tag.putInt("packGoal", packGoal.ordinal());
        int i = 0;
        for(UUID id : packMembers) {
            tag.putUUID("packMember" + i, id);
            i++;
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, Type.values()[tag.getInt("type")]);
        getEntityData().set(SPECIAL, tag.getBoolean("special"));
        growlTicks = tag.getInt("growlTicks");
        packGoal = PackGoal.values()[tag.getInt("packGoal")];
        int i = 0;
        while(tag.contains("packMember" + i)) {
            packMembers.add(NbtUtils.loadUUID(tag.get("packMember" + i)));
            i++;
        }
    }

    public static class GroupData extends AgeableMob.AgeableMobGroupData {
        public final Type type;

        public GroupData(Type type) {
            super(0F);
            this.type = type;
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        Type type;
        if(spawnDataIn instanceof GroupData data) type = data.type;
        else {
            float temperature = ChunkData.get(worldIn.getLevel().getChunkAt(blockPosition())).getTemperature(blockPosition());
            if(temperature < 0.3F) type = Type.DIRE;
            else if(temperature > 0.7F) type = Type.STRIPED;
            else type = Type.TIMBER;
            spawnDataIn = new GroupData(type);
        }
        getEntityData().set(TYPE, type);
        if(type == Type.DIRE) getAttribute(AttributesNF.FROST_DEFENSE.get()).setBaseValue(0.5);
        else if(type == Type.TIMBER) getAttribute(AttributesNF.FROST_DEFENSE.get()).setBaseValue(0.25);
        if(random.nextInt() % (type == Type.TIMBER ? 4 : 10) == 0) getEntityData().set(SPECIAL, true);
        for(WolfEntity wolf : worldIn.getEntitiesOfClass(WolfEntity.class, getBoundingBox().inflate(7))) {
            if(!wolf.isAlive()) continue;
            packMembers.add(wolf.getUUID());
            wolf.packMembers.add(getUUID());
        }
        return spawnDataIn;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height - 0.01F;
    }

    @Override
    public float getVoicePitch() {
        return (switch(getWolfType()) {
            case DIRE -> 0.96F;
            case STRIPED -> 1.04F;
            case TIMBER -> 1F;
        }) + (random.nextFloat() - random.nextFloat()) * 0.1F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.WOLF_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.WOLF_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {
        playSound(SoundsNF.WOLF_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    protected int getMaxSatiety() {
        return (int) (ContinentalWorldType.DAY_LENGTH * 1);
    }

    @Override
    public Diet getDiet() {
        return Diet.CARNIVORE;
    }

    @Override
    public boolean canEat(Entity entity) {
        if(super.canEat(entity)) return true;
        else if(entity instanceof LivingEntity livingEntity) return livingEntity.deathTime > 20 && entity.getType().is(TagsNF.EDIBLE_CORPSE);
        else return false;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundsNF.WOLF_EAT.get();
    }

    @Override
    public boolean includeAABB() {
        return !isAlive();
    }

    @Override
    public float getModelScale() {
        return getWolfType() == Type.DIRE ? 17F/16F : 1F;
    }

    @Override
    public Vector3f getOBBTranslation() {
        return new Vector3f(0, 10F/16F, 0);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDefaultAnimMap() {
        EnumMap<EntityPart, AnimationData> map = getGenericAnimMap();
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -2.5F/16F, -7.5F/16F), new Vector3f(0, 0, 0), new Vector3f(0, 13.5F, 0)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, -1F/16F, -1.5F/16F), new Vector3f(0, 0, 0), new Vector3f(0, -1.5F, -6F)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0F)));
        return map;
    }

    @Override
    public EntityPart[] getOrderedOBBParts() {
        return OBB_PARTS;
    }

    @Override
    public OBB[][] getDefaultOBBs() {
        return new OBB[][] {
                new OBB[] {
                        new OBB(7.5F/16F, 6.5F/16F, 14.5F/16F, 0, 0F/16F, 0F/16F)
                },
                new OBB[0],
                new OBB[] {
                        new OBB(6.5F/16F, 5.5F/16F, 4.5F/16F, 0, 1F/16F, 0.5F/16F),
                        new OBB(3.5F/16F, 3.5F/16F, 3.5F/16F, 0, 0F/16F, 1.5F/16F + 2.5F/16F)
                }
        };
    }

    @Override
    public AABB getEnclosingAABB() {
        AABB bb = getBoundingBox();
        return new AABB(bb.minX - 0.5, bb.minY, bb.minZ - 0.5, bb.maxX + 0.5, bb.maxY + 0.5, bb.maxZ + 0.5);
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return boxIndex >= 1 ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
    }
}
