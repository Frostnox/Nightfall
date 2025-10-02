package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.ai.goal.*;
import frostnox.nightfall.entity.ai.goal.target.TrackNearestTargetGoal;
import frostnox.nightfall.entity.entity.monster.CockatriceEntity;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.*;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class DrakefowlEntity extends TamableAnimalEntity implements IOrientedHitBoxes {
    public enum Type {
        BRONZE, EMERALD
    }
    private static final EntityPart[] OBB_PARTS = new EntityPart[]{EntityPart.BODY, EntityPart.NECK, EntityPart.HEAD};
    protected static final EntityDataAccessor<DrakefowlEntity.Type> TYPE = SynchedEntityData.defineId(DrakefowlEntity.class, DataSerializersNF.DRAKEFOWL_TYPE);
    private final Goal fleePlayerGoal = new FleeEntityGoal<>(this, Player.class, 1.2D, 1.3D, (entity) -> {
        Player player = (Player) entity;
        return !player.isDeadOrDying() && !player.isCrouching() && !player.isSpectator() && !player.isCreative();
    });
    private final Goal breedGoal = new BreedGoal(this, 0.8);
    private final Goal lureGoal = new LureGoal(this, 10, 0.9D);
    private final Goal eggGoal = new LayEggGoal(this, BlocksNF.DRAKEFOWL_NEST, 1) {
        @Override
        protected boolean isNestSpotValid(BlockPos pos) {
            return super.isNestSpotValid(pos) && mob.level.getBlockState(pos.below()).is(TagsNF.DRAKEFOWL_NEST_BLOCK);
        }

        @Override
        public boolean canUse() {
            if(LevelUtil.isDayTimeWithin(mob.level, LevelUtil.MORNING_TIME, LevelUtil.NIGHT_TIME)) return false;
            else return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if(LevelUtil.isDayTimeWithin(mob.level, LevelUtil.MORNING_TIME, LevelUtil.NIGHT_TIME)) return false;
            else return super.canContinueToUse();
        }
    };
    public float flap, flapSpeed, oFlapSpeed, oFlap, flapping = 1.0F, nextFlap = 1.0F;
    protected int ticksOffGround = 0;
    public @Nullable Type fatherType;

    public DrakefowlEntity(EntityType<? extends TamableAnimalEntity> type, Level level, Sex sex) {
        super(type, level, sex);
        updateGoals();
    }

    public static DrakefowlEntity createFemale(EntityType<? extends TamableAnimalEntity> type, Level level) {
        return new DrakefowlEntity(type, level, Sex.FEMALE);
    }

    public static DrakefowlEntity createMale(EntityType<? extends TamableAnimalEntity> type, Level level) {
        return new DrakefowlEntity(type, level, Sex.MALE);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.MOVEMENT_SPEED, 0.275F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 30)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.SLASHING_DEFENSE.get(), 0.2)
                .add(AttributesNF.PIERCING_DEFENSE.get(), 0.1)
                .add(AttributesNF.FIRE_DEFENSE.get(), 0.3);
    }

    public DrakefowlEntity.Type getDrakefowlType() {
        return getEntityData().get(TYPE);
    }

    @Override
    protected void updateGoals() {
        if(!level.isClientSide) {
            goalSelector.removeGoal(fleePlayerGoal);
            goalSelector.removeGoal(breedGoal);
            goalSelector.removeGoal(lureGoal);
            goalSelector.removeGoal(eggGoal);
            if(isTamed()) {
                goalSelector.addGoal(6, breedGoal);
                goalSelector.addGoal(7, lureGoal);
                if(sex == Sex.FEMALE) goalSelector.addGoal(8, eggGoal);
            }
            else {
                goalSelector.addGoal(4, fleePlayerGoal);
                if(sex == Sex.FEMALE) goalSelector.addGoal(7, lureGoal);
            }
        }
    }

    @Override
    public boolean canBreedWith(TamableAnimalEntity other) {
        if(other instanceof DrakefowlEntity drakefowl) return drakefowl.sex != sex;
        else return false;
    }

    @Override
    public void breedWith(TamableAnimalEntity other) {
        if(sex == Sex.FEMALE) {
            fatherType = ((DrakefowlEntity) other).getDrakefowlType();
        }
    }

    @Override
    protected boolean checkComfort() {
        return (!LevelData.isPresent(level) || LevelData.get(level).getSeasonalTemperature(ChunkData.get(level.getChunkAt(blockPosition())), blockPosition()) > 0.4F)
                && level.getEntitiesOfClass(TamableAnimalEntity.class, getBoundingBox().inflate(8, 1, 8)).size() <= 25;
    }

    @Override
    public boolean isFeedItem(ItemStack item) {
        return item.is(TagsNF.DRAKEFOWL_FOOD_ITEM);
    }

    @Override
    public ResourceLocation getBreedAction() {
        return ActionsNF.DRAKEFOWL_BREED.getId();
    }

    @Override
    protected void onGestationEnd() {
        fatherType = null;
    }

    @Override
    protected void onFeed() {
        super.onFeed();
        gestationTime = (int) ContinentalWorldType.DAY_LENGTH;
    }

    @Override
    public boolean canTargetFromSound(LivingEntity target) {
        return target.getType().is(TagsNF.DRAKEFOWL_PREY) || target instanceof Player;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target.canBeSeenAsEnemy() && !(target instanceof DrakefowlEntity || target instanceof DrakefowlBabyEntity);
    }

    @Override
    public ResourceLocation pickActionEnemy(double distanceSqr, Entity target) {
        if(distanceSqr > 2 * 2 && random.nextFloat() < Math.min(0.9, distanceSqr / (5 * 5))) return ActionsNF.DRAKEFOWL_SPIT.getId();
        else return ActionsNF.DRAKEFOWL_CLAW.getId();
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatAtHeightGoal(this, 0.4D));
        goalSelector.addGoal(2, new FleeEntityGoal<>(this, LivingEntity.class, 1.2D, 1.3D, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else return entity.getType().is(TagsNF.DRAKEFOWL_PREDATOR);
        }));
        goalSelector.addGoal(5, new FleeDamageGoal(this, 1.3D));
        goalSelector.addGoal(9, new EatEntityGoal(this, 1D, 15, 2));
        goalSelector.addGoal(10, new EatBlockGoal(this, 1D, 15, 2));
        goalSelector.addGoal(11, new ReducedWanderLandGoal(this, 0.8D, 6) {
            @Override
            protected @Nullable Vec3 getPosition() {
                Vec3 pos = super.getPosition();
                return pos != null && mob.level.getBlockState(new BlockPos(pos)).is(BlocksNF.DRAKEFOWL_NEST.get()) ? null : pos;
            }
        });
        goalSelector.addGoal(12, new RandomLookGoal(this, 0.02F / 6));
        if(getType() == EntitiesNF.DRAKEFOWL_ROOSTER.get()) { //Function gets called mid-constructor so can't use sex
            goalSelector.addGoal(3, new RushAttackGoal(this, 1.2D));
            targetSelector.addGoal(1, new HurtByTargetGoal(this));
            targetSelector.addGoal(2, new TrackNearestTargetGoal<>(this, LivingEntity.class, true, (entity) -> {
                if(entity.isDeadOrDying()) return false;
                else return entity.getType().is(TagsNF.DRAKEFOWL_PREY);
            }) {
                @Override
                protected double getFollowDistance() {
                    return super.getFollowDistance() * 2F/3F;
                }
            });
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(level.isClientSide && tickCount == 1) move(MoverType.SELF, new Vec3(0, -0.1, 0)); //Client level not move entity for a while so do it immediately
        if(!onGround) ticksOffGround++;
        else ticksOffGround = 0;
        oFlap = flap;
        oFlapSpeed = flapSpeed;
        boolean shouldFlap = isAlive() && ((!onGround && ticksOffGround > 5) || getActionTracker().getAction().isSpecial());
        flapSpeed += (!shouldFlap ? -1.0F : 4.0F) * 0.3F;
        flapSpeed = Mth.clamp(flapSpeed, 0.0F, 1.0F);
        if(shouldFlap && flapping < 1.0F) flapping = 1.0F;
        flapping *= 0.9F;
        Vec3 velocity = getDeltaMovement();
        if(shouldFlap && velocity.y < 0.0D) {
            setDeltaMovement(velocity.multiply(1.0D, 0.6D, 1.0D));
        }
        flap += flapping * 2.0F;
        if(shouldFlap && level.isClientSide && randTickCount % 11 == 0) {
            ClientEngine.get().playEntitySound(this, SoundsNF.DRAKEFOWL_FLAP.get(), SoundSource.NEUTRAL, 1F, 1F + random.nextFloat(-0.05F, 0.05F));
        }
        fallDistance = 0;
        if(level.isClientSide && isTamed() && sex == Sex.MALE && isAlive() && LevelUtil.isDayTimeExactly(level, LevelUtil.MORNING_TIME)) {
            ClientEngine.get().playUniqueEntitySound(this, SoundsNF.DRAKEFOWL_CROW.get(), SoundSource.NEUTRAL, 2F, 1F);
        }
        if(!level.isClientSide && isSpecial() && getRandom().nextFloat() < 1D / (ContinentalWorldType.DAY_LENGTH * 2)) {
            CockatriceEntity cockatrice = EntitiesNF.COCKATRICE.get().create(level);
            cockatrice.finalizeSpawn((ServerLevel) level, level.getCurrentDifficultyAt(blockPosition()), MobSpawnType.CONVERSION, new CockatriceEntity.GroupData(CockatriceEntity.Type.values()[getDrakefowlType().ordinal()]), null);
            cockatrice.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            discard();
            level.addFreshEntity(cockatrice);
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
            if(temperature > 0.7F) type = Type.BRONZE;
            else type = Type.EMERALD;
            spawnDataIn = new GroupData(type);
        }
        getEntityData().set(TYPE, type);
        if(sex == Sex.MALE && random.nextInt() % 512 == 0) getEntityData().set(SPECIAL, true);
        updateGoals();
        return spawnDataIn;
    }

    @Override
    protected boolean isFlapping() {
        return flyDist > nextFlap;
    }

    @Override
    protected void onFlap() {
        nextFlap = flyDist + flapSpeed / 2.0F;
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    public float getVisionAngle() {
        return 160F;
    }

    @Override
    public float getVoicePitch() {
        if(sex == Sex.MALE) return super.getVoicePitch() * 0.97F;
        else return super.getVoicePitch() * 1.03F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if(getActionTracker().getActionID().equals(getCollapseAction())) return null;
        else return SoundsNF.DRAKEFOWL_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.DRAKEFOWL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.DRAKEFOWL_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        playSound(SoundsNF.DRAKEFOWL_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height - 0.01F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, DrakefowlEntity.Type.EMERALD);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("type", getDrakefowlType().ordinal());
        tag.putInt("ticksOffGround", ticksOffGround);
        if(fatherType != null) tag.putInt("fatherType", fatherType.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, DrakefowlEntity.Type.values()[tag.getInt("type")]);
        ticksOffGround = tag.getInt("ticksOffGround");
        if(tag.contains("fatherType")) fatherType = DrakefowlEntity.Type.values()[tag.getInt("fatherType")];
        updateGoals();
    }

    @Override
    public float getPushResistance() {
        return PUSH_LOW;
    }

    @Override
    public float getPushForce() {
        return PUSH_LOW;
    }

    @Override
    public @Nullable ResourceLocation getCollapseAction() {
        return ActionsNF.DRAKEFOWL_COLLAPSE.getId();
    }

    @Override
    public boolean canEat(BlockState state) {
        if(state.is(TagsNF.DRAKEFOWL_FOOD_BLOCK)) {
            if(state.getBlock() instanceof IFoodBlock foodBlock) return foodBlock.isEatable(state);
            else return true;
        }
        else return false;
    }

    @Override
    public boolean canEat(Entity entity) {
        if(entity instanceof ItemEntity itemEntity) return itemEntity.getItem().is(TagsNF.DRAKEFOWL_FOOD_ITEM);
        else if(entity instanceof LivingEntity livingEntity) {
            return livingEntity.deathTime > 20 && entity.getType().is(TagsNF.EDIBLE_CORPSE) && !(entity instanceof DrakefowlEntity || entity instanceof DrakefowlBabyEntity);
        }
        else return false;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundsNF.DRAKEFOWL_EAT.get();
    }

    @Override
    protected int getMaxSatiety() {
        return (int) ContinentalWorldType.DAY_LENGTH;
    }

    @Override
    public boolean includeAABB() {
        return true;
    }

    @Override
    public Vector3f getOBBTranslation() {
        return new Vector3f(0, 6.5F/16F, 0);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDefaultAnimMap() {
        EnumMap<EntityPart, AnimationData> map = getGenericAnimMap();
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -4.5F/16F, -3F/16F), new Vector3f(0, 0, 0), new Vector3f(0, 17.5F, 0)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, -4F/16F, 0F/16F), new Vector3f(0, 0, 0), new Vector3f(0, -0.5F, -3)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0)));
        return map;
    }

    @Override
    public EntityPart[] getOrderedOBBParts() {
        return OBB_PARTS;
    }

    @Override
    public OBB[] getDefaultOBBs() {
        return new OBB[] {
                new OBB(2.5F/16F, 5.5F/16F, 2.5F/16F, 0, 1.5F/16F, 0),
                new OBB(3.5F/16F, 3.5F/16F, 3.5F/16F, 0, 0.5F/16F, 0.5F/16F)
        };
    }

    @Override
    public AABB getEnclosingAABB() {
        AABB bb = getBoundingBox();
        return new AABB(bb.minX - 0.5, bb.minY, bb.minZ - 0.5, bb.maxX + 0.5, bb.maxY + 0.5, bb.maxZ + 0.5);
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return boxIndex >= 0 ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
    }
}
