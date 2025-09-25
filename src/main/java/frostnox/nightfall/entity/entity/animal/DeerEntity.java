package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.ai.goal.*;
import frostnox.nightfall.entity.ai.sensing.AmplifiedAudioSensing;
import frostnox.nightfall.entity.ai.sensing.AudioSensing;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class DeerEntity extends AnimalEntity implements IOrientedHitBoxes {
    public enum Type {
        BRIAR, RED, SPOTTED
    }
    private static final EntityPart[] OBB_PARTS = new EntityPart[]{EntityPart.BODY, EntityPart.NECK, EntityPart.HEAD};
    protected static final EntityDataAccessor<Type> TYPE = SynchedEntityData.defineId(DeerEntity.class, DataSerializersNF.DEER_TYPE);
    protected static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Sex> SEX = SynchedEntityData.defineId(DeerEntity.class, DataSerializersNF.SEX);
    public int sprintTime, sprintTimeLast;

    public DeerEntity(EntityType<? extends DeerEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected int getMaxSatiety() {
        return (int) (ContinentalWorldType.DAY_LENGTH);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 25D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 4)
                .add(Attributes.FOLLOW_RANGE, 30)
                .add(AttributesNF.HEARING_RANGE.get(), 24)
                .add(AttributesNF.FROST_DEFENSE.get(), 0.2);
    }

    public Type getDeerType() {
        return getEntityData().get(TYPE);
    }

    public boolean isSpecial() {
        return getEntityData().get(SPECIAL);
    }

    public Sex getSex() {
        return getEntityData().get(SEX);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatAtHeightGoal(this, 0.9D));
        goalSelector.addGoal(2, new StepUpFleeEntityGoal<>(this, LivingEntity.class, 1.6D, 1.8D, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else if(entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
            else return entity.getType().is(TagsNF.DEER_PREDATOR);
        }));
        goalSelector.addGoal(3, new StepUpFleeDamageGoal(this, 1.6D));
        goalSelector.addGoal(4, new EatEntityGoal(this, 1.3D, 20, 2));
        goalSelector.addGoal(5, new EatBlockGoal(this, 1.3D, 20, 2));
        goalSelector.addGoal(6, new ReducedWanderLandGoal(this, 0.8D, 8));
    }

    @Override
    public void tick() {
        super.tick();
        sprintTimeLast = sprintTime;
        if(isAlive()) {
            IActionTracker capA = getActionTracker();
            if(capA.isCharging() && capA.getFrame() > 100 && capA.getActionID().equals(ActionsNF.DEER_GRAZE.getId())) {
                if(random.nextFloat() < 0.005F) queueAction();
            }
            if(getStepHeight() >= 1) {
                setSprinting(true);
                if(sprintTime < 9) sprintTime++;
            }
            else {
                setSprinting(false);
                if(sprintTime > 0) sprintTime--;
            }
            if(!level.isClientSide && sprintTime == 0 && capA.isInactive() && getBlockStateOn().is(TagsNF.TILLABLE_SOIL)) {
                if(random.nextFloat() < 0.0015F * (1.9 - getSatietyPercent())) startAction(ActionsNF.DEER_GRAZE.getId());
            }
        }
    }

    @Override
    public boolean canAttack(LivingEntity pTarget) {
        return pTarget.canBeSeenAsEnemy();
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return boxIndex >= 0 ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
    }

    @Override
    protected AudioSensing createAudioSensing() {
        return new AmplifiedAudioSensing(this, 10, 1.25F);
    }

    @Override
    public float getVisionAngle() {
        return 180F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, Type.SPOTTED);
        entityData.define(SPECIAL, false);
        entityData.define(SEX, Sex.FEMALE);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        int type = getDeerType().ordinal();
        if(type != 0) tag.putInt("type", type);
        boolean special = isSpecial();
        if(special) tag.putBoolean("special", special);
        tag.putBoolean("female", getSex() == Sex.FEMALE);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, Type.values()[tag.getInt("type")]);
        getEntityData().set(SPECIAL, tag.getBoolean("special"));
        if(tag.contains("female")) getEntityData().set(SEX, tag.getBoolean("female") ? Sex.FEMALE : Sex.MALE);
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
            if(temperature < 0.35F) type = Type.RED;
            else if(temperature > 0.65F) type = Type.BRIAR;
            else type = Type.SPOTTED;
            spawnDataIn = new GroupData(type);
        }
        getEntityData().set(TYPE, type);
        if(random.nextInt() % 8192 == 0) getEntityData().set(SPECIAL, true);
        if(random.nextBoolean()) getEntityData().set(SEX, Sex.MALE);
        return spawnDataIn;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height - 0.01F;
    }

    @Override
    public float getVoicePitch() {
        if(getSex() == Sex.MALE) return super.getVoicePitch() * 0.8F;
        else return super.getVoicePitch();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.DEER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.DEER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {
        playSound(SoundsNF.DEER_STEP.get(), 0.15F, 1.3F);
    }

    @Override
    public boolean includeAABB() {
        return true;
    }

    @Override
    public float getModelScale() {
        return getSex() == Sex.MALE ? 17F/16F : 1F;
    }

    @Override
    public Vector3f getOBBTranslation() {
        return new Vector3f(0F/16F, 12.5F/16F, 0F/16F);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDefaultAnimMap() {
        EnumMap<EntityPart, AnimationData> map = getGenericAnimMap();
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0, -6.5F/16F, -5.5F/16F), new Vector3f(0, 0, 0)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, -7F/16F, 0F/16F), new Vector3f(15, 0, 0)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(-15, 0, 0)));
        return map;
    }

    @Override
    public EntityPart[] getOrderedOBBParts() {
        return OBB_PARTS;
    }

    @Override
    public OBB[] getDefaultOBBs() {
        return new OBB[] {
                new OBB(3.5F/16F, 8.5F/16F, 3.5F/16F, 0, 4F/16F, 0),
                new OBB(4.5F/16F, 4.5F/16F, 4.5F/16F, 0, 2F/16F, 0)
        };
    }

    @Override
    public AABB getEnclosingAABB() {
        AABB bb = getBoundingBox();
        return new AABB(bb.minX - 0.6, bb.minY, bb.minZ - 0.6, bb.maxX + 0.6, bb.maxY + 0.6, bb.maxZ + 0.6);
    }

    @Override
    public boolean canEat(BlockState state) {
        if(state.is(TagsNF.DEER_FOOD_BLOCK)) {
            if(state.getBlock() instanceof IFoodBlock foodBlock) return foodBlock.isEatable(state);
            else return true;
        }
        else return false;
    }

    @Override
    public boolean canEat(Entity entity) {
        if(entity instanceof ItemEntity itemEntity) return itemEntity.getItem().is(TagsNF.DEER_FOOD_ITEM);
        else return false;
    }

    @Override
    public SoundEvent getEatSound() {
        return null; //TODO:
    }
}
