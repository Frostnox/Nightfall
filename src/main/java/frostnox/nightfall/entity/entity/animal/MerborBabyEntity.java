package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.ai.goal.*;
import frostnox.nightfall.entity.ai.goal.target.TrackNearestTargetGoal;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.EnumMap;

public class MerborBabyEntity extends BabyAnimalEntity implements IOrientedHitBoxes {
    private static final EntityPart[] OBB_PARTS = new EntityPart[]{EntityPart.BODY, EntityPart.NECK, EntityPart.HEAD};
    protected static final EntityDataAccessor<MerborEntity.Type> TYPE = SynchedEntityData.defineId(MerborEntity.class, DataSerializersNF.MERBOR_TYPE);

    public MerborBabyEntity(EntityType<? extends ActionableEntity> type, Level level) {
        super(type, level, (int) ContinentalWorldType.DAY_LENGTH * 3);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 40D)
                .add(Attributes.MOVEMENT_SPEED, 0.235F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 15)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.FIRE_DEFENSE.get(), 0.5)
                .add(AttributesNF.ELECTRIC_DEFENSE.get(), -0.5);
    }

    public MerborEntity.Type getMerborType() {
        return getEntityData().get(TYPE);
    }

    @Override
    protected ActionableEntity createMatureEntity() {
        MerborEntity adult = random.nextBoolean() ? EntitiesNF.MERBOR_TUSKER.get().create(level) : EntitiesNF.MERBOR_SOW.get().create(level);
        adult.finalizeSpawn((ServerLevel) level, level.getCurrentDifficultyAt(blockPosition()), MobSpawnType.CONVERSION, new MerborEntity.GroupData(getMerborType()), null);
        adult.getEntityData().set(TamableAnimalEntity.TAMED, true);
        return adult;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatAtHeightGoal(this, 0.4D));
        goalSelector.addGoal(2, new FollowParentGoal(this, 1.1D));
        goalSelector.addGoal(3, new FleeEntityGoal<>(this, LivingEntity.class, 1.1D, 1.1D, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else return entity.getType().is(TagsNF.MERBOR_PREDATOR);
        }));
        goalSelector.addGoal(4, new FleeDamageGoal(this, 1.1D));
        goalSelector.addGoal(5, new RandomLookGoal(this, 0.02F));
        targetSelector.addGoal(1, new TrackNearestTargetGoal<>(this, MerborEntity.class, true, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else return ((MerborEntity) entity).sex == Sex.FEMALE;
        }));
        targetSelector.addGoal(2, new TrackNearestTargetGoal<>(this, MerborEntity.class, true, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else return ((MerborEntity) entity).sex == Sex.MALE;
        }));
    }

    @Override
    public float getVisionAngle() {
        return 100F;
    }

    @Override
    public float getNavigatorWaypointDist() {
        return 6F/16F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, MerborEntity.Type.RIVER);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("type", getMerborType().ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, MerborEntity.Type.values()[tag.getInt("type")]);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if(getActionTracker().getActionID().equals(getCollapseAction())) return null;
        else return SoundsNF.MERBOR_BABY_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.MERBOR_BABY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.MERBOR_BABY_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        playSound(SoundsNF.MERBOR_BABY_STEP.get(), 0.05F, 1.0F);
    }

    @Override
    public boolean includeAABB() {
        return true;
    }

    @Override
    public Vector3f getOBBTranslation() {
        return new Vector3f(0, 3.5F/16F, 0);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDefaultAnimMap() {
        EnumMap<EntityPart, AnimationData> map = getGenericAnimMap();
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -1F/16F, -1.5F/16F)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F)));
        return map;
    }

    @Override
    public EntityPart[] getOrderedOBBParts() {
        return OBB_PARTS;
    }

    @Override
    public OBB[] getDefaultOBBs() {
        return new OBB[] {
                new OBB(2.25F/16F, 2.25F/16F, 2.25F/16F, 0, 0.5F/16F, 0.5F/16F)
        };
    }

    @Override
    public AABB getEnclosingAABB() {
        AABB bb = getBoundingBox();
        return new AABB(bb.minX - 0.3, bb.minY, bb.minZ - 0.3, bb.maxX + 0.3, bb.maxY + 0.3, bb.maxZ + 0.3);
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return boxIndex >= 0 ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
    }
}
