package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
    public float flap, flapSpeed, oFlapSpeed, oFlap, flapping = 1.0F, nextFlap = 1.0F;
    protected @Nullable Type fatherType;

    public DrakefowlEntity(EntityType<? extends TamableAnimalEntity> type, Level level, Sex sex) {
        super(type, level, sex);
    }

    public static DrakefowlEntity createFemale(EntityType<? extends TamableAnimalEntity> type, Level level) {
        return new DrakefowlEntity(type, level, Sex.FEMALE);
    }

    public static DrakefowlEntity createMale(EntityType<? extends TamableAnimalEntity> type, Level level) {
        return new DrakefowlEntity(type, level, Sex.MALE);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 30D)
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
    public boolean canBreedWith(TamableAnimalEntity other) {
        if(other instanceof DrakefowlEntity drakefowl) return drakefowl.sex != sex;
        else return false;
    }

    @Override
    public void breedWith(TamableAnimalEntity other) {
        if(sex == Sex.FEMALE) {
            gestationTime = (int) ContinentalWorldType.DAY_LENGTH;
            fatherType = ((DrakefowlEntity) other).getDrakefowlType();
        }
    }

    @Override
    protected boolean isFeedItem(ItemStack item) {
        return item.is(TagsNF.DRAKEFOWL_FOOD_ITEM);
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
    public void aiStep() {
        super.aiStep();
        oFlap = flap;
        oFlapSpeed = flapSpeed;
        flapSpeed += (onGround ? -1.0F : 4.0F) * 0.3F;
        flapSpeed = Mth.clamp(flapSpeed, 0.0F, 1.0F);
        if(!onGround && flapping < 1.0F) flapping = 1.0F;
        flapping *= 0.9F;
        Vec3 velocity = getDeltaMovement();
        if(!onGround && velocity.y < 0.0D) {
            setDeltaMovement(velocity.multiply(1.0D, 0.6D, 1.0D));
        }
        flap += flapping * 2.0F;
        fallDistance = 0;
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
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CHICKEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
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
        if(fatherType != null) tag.putInt("fatherType", fatherType.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, DrakefowlEntity.Type.values()[tag.getInt("type")]);
        if(tag.contains("fatherType")) fatherType = DrakefowlEntity.Type.values()[tag.getInt("fatherType")];
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
        return null;
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
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -4.5F/16F, -3F/16F)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, -4F/16F, 0F/16F)));
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
